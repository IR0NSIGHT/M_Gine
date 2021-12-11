package me.iron.mGine.mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import glossar.GlossarCategory;
import glossar.GlossarEntry;
import glossar.GlossarInit;
import me.iron.mGine.mod.clientside.MissionClient;
import me.iron.mGine.mod.clientside.map.SpriteList;
import me.iron.mGine.mod.debug.DebugCommand;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.network.PacketInteractMission;
import me.iron.mGine.mod.network.PacketMissionSynch;
import org.schema.schine.resource.ResourceLoader;

import java.sql.SQLException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:26
 */
public class ModMain extends StarMod {
    public static ModMain instance;
    @Override
    public void onEnable() {
        instance = this;
        PacketUtil.registerPacket(PacketMissionSynch.class);
        PacketUtil.registerPacket(PacketInteractMission.class);
        StarLoader.registerCommand(new DebugCommand());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent serverInitializeEvent) {
        super.onServerCreated(serverInitializeEvent);
        new M_GineCore();
        try {
            new DataBaseManager();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //DebugUI.init();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent clientInitializeEvent) {
        new MissionClient();
        super.onClientCreated(clientInitializeEvent);
        GlossarInit.initGlossar(this);
        GlossarInit.addCategory(getWiki());
    }

    @Override
    public void onResourceLoad(ResourceLoader resourceLoader) {
        super.onResourceLoad(resourceLoader);
        SpriteList.loadSprites();
    }

    private GlossarCategory getWiki() {
        GlossarCategory cat = new GlossarCategory("Missions mGine");
        cat.addEntry(new GlossarEntry("Introduction","mGine procedurally generates missions. The missions are availalbe to everyone on the server, until being claimed. Unclaimed missions dissapear after ~45 minutes and are replaced by newly generated ones.\n\n" +
                "Missions are acceptable quests, that consist of one or more tasks the player must complete, in order to successfully finish the mission and gain the reward.\n\n" +
                "Most missions have a time limit, if the time is exceeded, the mission automatically fails. If you are the mission's party-captain, you can request more time for the mission, in exchange for a reduced reward.\n" +
                "Missions do NOT persist between server restarts." +
                ""));
        cat.addEntry(new GlossarEntry("Multiplayer","Planned feature\n\nMissions can be done by multiple players at once: by a party.\n" +
                "The player who claimed the mission originally is the party-captain. He/She can invite other players to join the party or remove them from the party. The reward is split equally between members upon completion."));
        cat.addEntry(new GlossarEntry("GUI","To open the GUI, press left-control+m. This will open a window with 4 tabs:\n" +
                "Selected\n" +
                "Your currently selected mission appears here. The tab allows you to accept a mission, abort it, request delay, add/remove party members and see the specific tasks required to complete the mission.\n" +
                "\nOpen\n" +
                "This tab shows a list of all available missions, that are visible from your position. Only missions that are closer than 16 sectors to your position are visible.\n" +
                "\nActive\n" +
                "This tab lists all missions which you are currently doing. There is no limit to the amount of missions you can do at the same time, altough beware that most missions come with a time limit.\n" +
                "\nFinished\n" +
                "This tab shows the list of finished missions, both successful and unsuccessful. (Currently broken)"));
        cat.addEntry(new GlossarEntry("Map","Available missions:\n" +
                "The map shows all available missions marked by a yellow exclamationmark '!'. If your current position is less than 16 sectors away from the mission, a circle around the exclamation mark appears. This means that the mission's details are now visible to you. " +
                "You can either see the details by clicking the exclamationmark, or by opening your mission GUI. \n" +
                "\nSelected mission:\n" +
                "You can select missions by clicking them on the map or in the GUI's list. The selected mission's tasks are shown on the map, with icons that differ based on the task's type. Not all tasks have a specific location, those are not shown on the map." +
                "The icon will turn red if its failed, orange if its open, yellow if its active, and green once it is completed.\n" +
                " The tasks are connected with lines, depending on the order in which they must be completed. You can rightclick tasks to directly navigate to them."));
        cat.addEntry(new GlossarEntry("Politics","All missions will impact the political relations you have with the questgiving NPC-faction. A successfull mission will improve relations, a failed mission will worsen relations. Some missions carry extra political punishments if you act in a certain way. " +
                "The cargo transport mission for example will massively impact your relation if you steal the cargo. More dangerous missions generally give more political bonus than peaceful ones.\n" +
                "\nReputation rank:\n" +
                "All missions have a minimum reputation rank which you must have with the questgiving NPC-faction to be able to do the mission." +
                "\n\n Reputation ranks directly depend on the amount of political points you have with the faction. The points can be seen in the faction-menu under 'NPC-Diplomacy'."));
        cat.addEntry(new GlossarEntry("Mission types","Currently 4 types of missions are available (with more types planned):\n\n" +
                "Destroy:\n" +
                "Destroy a pirate station for one of the big NPC factions, then report back to the faction.\n\n" +
                "Cargo:\n" +
                "Transport cargo from one station to another one. Requires a freighter.\n\n" +
                "Patrol:\n" +
                "Patrol sectors in the territory of the client. (Very boring, can't recommend)\n\n" +
                "Scan: \n" +
                "Scan multiple unknown stations for the client faction. Risk of encountering pirate stations."));
        return cat;
    }
}
