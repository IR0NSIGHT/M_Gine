package me.iron.mGine.mod.network;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 09.10.2021
 * TIME: 13:54
 * packet to send from client to server, to interact with a mission: accept, abort, add/kick player from party etc.
 */
public class PacketInteractMission extends Packet {
    private UUID missionUUID;

    private ArrayList<String> kickList;
    private ArrayList<String> inviteList;
    private boolean leave;

    private boolean accept;
    private boolean abort;

    private boolean delay;

    /**
     * @param missionUUID mission id
     * @param kickList playernames to kick from party
     * @param inviteList playernames to invite to party
     * @param leave leave this mission (if in party)
     * @param accept accept mission -> become captain (if available)
     * @param abort abort mission (if captain)
     * @param delay ask for delay of deadline (if captain)
     */
    public PacketInteractMission(UUID missionUUID, ArrayList<String> kickList, ArrayList<String> inviteList, boolean leave, boolean accept, boolean abort, boolean delay) {
        this.missionUUID = missionUUID;
        this.kickList = kickList;
        this.inviteList = inviteList;
        this.leave = leave;
        this.accept = accept;
        this.abort = abort;
        this.delay = delay;
    }

    public PacketInteractMission() {
    }

    @Override
    public void readPacketData(PacketReadBuffer buffer) throws IOException {
        this.missionUUID = buffer.readObject(UUID.class);
        this.kickList = buffer.readStringList();
        this.inviteList = buffer.readStringList();
        this.leave = buffer.readBoolean();
        this.accept = buffer.readBoolean();
        this.delay = buffer.readBoolean(); //TODO build "ask for delay" functionality into missions
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {

    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        //get mission, abort if doesnt exist.
        Mission m = M_GineCore.instance.getMissionByUUID(missionUUID);
        if (m == null)
            return;

        //invite and kick only as captain
        if (m.getCaptain().equals(playerState.getName())) {
            //kick players
            for (String member: kickList) {
                m.getParty().remove(member);
                PlayerState memberState = GameServerState.instance.getPlayerStatesByName().get(member);
                if (memberState != null)
                    memberState.sendServerMessage(Lng.astr("you have been kicked from mission " + m.getIDString()), ServerMessage.MESSAGE_TYPE_ERROR);
            }

            //invite players
            for (String member: inviteList) {
                //TODO invite method with popup for player: "want to join this mission?"
                PlayerState memberState = GameServerState.instance.getPlayerStatesByName().get(member);
                if (memberState != null)
                    memberState.sendServerMessage(Lng.astr("you have been invited to mission " + m.getIDString()), ServerMessage.MESSAGE_TYPE_ERROR);
            }
            m.updateActiveParty();
        }

        if (leave) {
            //cant leave finished missions.
            if (!m.getState().equals(MissionState.IN_PROGRESS))
                return;

            //assign new captain (next online player), or abandom mission if last player.
            if (m.getCaptain().equals(playerState.getName())) {
                if (m.getActiveParty().iterator().hasNext()) {
                    m.setCaptain(m.getActiveParty().iterator().next().getName());
                } else if (m.getParty().iterator().hasNext()) {
                    m.setCaptain(m.getParty().iterator().next());
                } else {
                    //no members left, mission is abandoned
                    m.onAbandon();
                }
            }
            m.removePartyMember(playerState.getName());
        }

        if (accept) {
            //cant claim non-open missions
            if (!m.getState().equals(MissionState.OPEN))
                return;
            //add to mission, autoset captain if first member
            m.addPartyMember(playerState.getName());
        }

        if (abort) {
            //only captain can abort
            if (!m.getCaptain().equals(playerState.getName()))
                return;

            m.onAbandon();
        }

        if (delay) {
            //only captain can ask for more time in mission
            if (!m.getCaptain().equals(playerState.getName()))
                return;
            m.requestDelay();
        }

        //synch to mission members
        ArrayList<PlayerState> members = new ArrayList<>(m.getActiveParty().size());
        members.addAll(m.getActiveParty());
        M_GineCore.instance.synchMissionTo(new ArrayList<>(Collections.singleton(m.getUuid())),members);
    }

    public ArrayList<String> getKickList() {
        return kickList;
    }

    public void setKickList(ArrayList<String> kickList) {
        this.kickList = kickList;
    }

    public ArrayList<String> getInviteList() {
        return inviteList;
    }

    public void setInviteList(ArrayList<String> inviteList) {
        this.inviteList = inviteList;
    }

    public boolean isLeave() {
        return leave;
    }

    public void setLeave(boolean leave) {
        this.leave = leave;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isAbort() {
        return abort;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public boolean isDelay() {
        return delay;
    }

    public void setDelay(boolean delay) {
        this.delay = delay;
    }
}
