package me.iron.mGine.mod.clientside;

import api.common.GameClient;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.GUI.GUISelectedMissionTab;
import me.iron.mGine.mod.clientside.GUI.GUIMissionListTab;
import me.iron.mGine.mod.clientside.GUI.MissionGUIControlManager;
import me.iron.mGine.mod.clientside.map.MapIcon;
import me.iron.mGine.mod.clientside.map.MapMarker;
import me.iron.mGine.mod.clientside.map.MissionMapDrawer;
import me.iron.mGine.mod.clientside.map.TaskMarker;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.network.PacketInteractMission;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.09.2021
 * TIME: 16:33
 */
public class MissionClient {
    public static MissionClient instance;
    public GUIMissionListTab guiActiveMissionsList;
    public GUIMissionListTab guiOpenMissionsList;
    public GUIMissionListTab guiFinishedMissionsList;
    public GUISelectedMissionTab selectedMissionTab;

    public HashMap<Vector3i, MapMarker> openQuestMarkers = new HashMap<>(); //list of open quest markers
    //mission sublists
    public HashSet<Mission> active = new HashSet<>();
    public HashSet<Mission> available = new HashSet<>();
    public HashSet<Mission> finished = new HashSet<>();
    public HashSet <MissionTask> currentTasks = new HashSet<>(); //unused i think

    public static boolean autoNav = true;
    private Mission selectedMission;

    private boolean drawOpenMarkers;

    public void setSelectedMission(Mission selectedMission) {
        for (MissionTask t: selectedMission.getMissionTasks()) {
            MissionMapDrawer.instance.getMapMarkers().remove(new TaskMarker(t)); //TODO this is jank do it better.
        }
        this.selectedMission = selectedMission;
        if (selectedMission != null) {
            for (MissionTask task: selectedMission.getMissionTasks()) {
                if (task.getTaskSector() != null) {
                    MissionMapDrawer.instance.addMarker(new TaskMarker(task));
                }
            }
        }
        MissionMapDrawer.instance.updateInternalList();
        updateGUILists();
    }

    public Mission getSelectedMission() {
        return selectedMission;
    }

    public MissionTask getSelectedTask() {
        return selectedTask;
    }

    private MissionTask selectedTask;
    public MissionClient() {
        instance = this;
        MissionMapDrawer drawer = new MissionMapDrawer();
        new StarRunnable(){
            long lastRan = System.currentTimeMillis();
            @Override
            public void run() {
                if (lastRan + 2000 < System.currentTimeMillis()) {
                    lastRan =System.currentTimeMillis();
                //    update();
                }
            }
        }.runTimer(ModMain.instance,5);

        //register the GUI window controller
        new StarRunnable() {
            @Override
            public void run() {
                MissionGUIControlManager controlManager = new MissionGUIControlManager(GameClientState.instance);
                ModGUIHandler.registerNewControlManager(ModMain.instance.getSkeleton(), controlManager);
            }
        }.runLater(ModMain.instance,100);
    }

    public void update() {
        updateSelectedMission();
        updateSelectedTask();
        updateCurrentTasks();
        updateGUILists();
    }

    private void updateGUILists() {
        if (guiFinishedMissionsList == null || guiOpenMissionsList == null || guiActiveMissionsList == null || selectedMissionTab == null)
            return;

        guiFinishedMissionsList.setMissions(finished);
        guiOpenMissionsList.setMissions(available);
        guiActiveMissionsList.setMissions(active);
        selectedMissionTab.update();
    }

    private void updateSelectedMission() { //TODO gets called every frame.
    //    ModPlayground.broadcastMessage("client update selected mission");
        if (selectedMission == null)
            return;

        for (Mission m: finished) {
            if (selectedMission.equals(m)) {
                setSelectedMission(m);
                return;
            }
        }
        for (Mission m: available) {
            if (selectedMission.equals(m)) {
                setSelectedMission(m);
                return;
            }
        }
        for (Mission m: active) {
            if (selectedMission.equals(m)) {
                setSelectedMission(m);
                return;
            }
        }


        selectedMission = null; //old mission doesnt exist anymore in lists.
    }

    private void updateSelectedTask() {
        if (selectedMission == null) {
            selectedTask = null;
            return;
        }
        if (selectedTask == null || selectedTask.mission != selectedMission || !selectedTask.getCurrentState().equals(MissionState.IN_PROGRESS)) {
            selectedTask = getNextActiveTask(selectedMission);
        }
    }
    private void onSelectedMissionChange() {

    }

    public void navigateTo(Vector3i sector) {
        GameClient.getClientController().getClientGameData().setWaypoint(sector);
    }
    private void updateCurrentTasks() {
        //clear tasks
        currentTasks.clear();
        //rebuild if theres a mission
        if (selectedMission != null) {
            for (MissionTask t : selectedMission.getMissionTasks()) {
                if (t.getCurrentState().equals(MissionState.IN_PROGRESS))
                    currentTasks.add(t);
            }
        }
    }

    public void overwriteMissions(Collection<Mission> missions) {
        //TODO get missions from server
        available.clear();
        active.clear();
        finished.clear();

        for (Mission m: missions) {
            addMission(m);
        }
    //    ModPlayground.broadcastMessage("OVERWRITE");
        update();
    }

    public void addMissions(Collection<Mission> missions) {
        for (Mission m : missions) {
            addMission(m);
        }
        update();
    }

    public void addMission(Mission m) {
        available.remove(m);
        active.remove(m);
        finished.remove(m);
        switch (m.getState()) {
            case OPEN:
            {
                available.add(m);
                break;
            }
            case IN_PROGRESS: {
                active.add(m);
                break;
            }
            default:
            {
                finished.add(m);
                break;
            }
        }
    }

    public void removeMissions(Collection<UUID> uuids) {
        for (UUID uuid: uuids) {
            Mission m = new Mission(uuid);
            removeMission(m);
            if (active.contains(m)||available.contains(m)||finished.contains(m)) {
                System.out.println("mission remains after being deleted.");
            }
        }
        update();
    }

    /**
     * will delete the mission from this client and its GUI. EXCLUDES FINISHED MISSIONS!
     * @param m
     */
    public void removeMission(Mission m) {
        active.remove(m);
        available.remove(m);
    //    finished.remove(m);
    }
    private MissionTask getNextActiveTask(Mission m) {
        for (MissionTask t: m.getMissionTasks()) {
            if (t.getCurrentState().equals(MissionState.IN_PROGRESS)) {
                return t;
            }
        }
        return null;
    }

    /**
     * request on server to accept or abort mission
     * @param id UUID of mission
     * @param accept true: accept, false: abort
     */
    public void requestAcceptToggleMission(UUID id, boolean accept) {
        PacketInteractMission packet = new PacketInteractMission(id);
        packet.setAccept(accept);
        packet.setAbort(!accept);
        packet.sendToServer();
    }

    public void setOpenQuestMarkers(ArrayList<Vector3i> markers) {
        ArrayList<Vector3i> removeQueue = new ArrayList<>();

        //flag old markers
        for (Vector3i v: openQuestMarkers.keySet()) {
            if (markers.contains(v)) {
                //no action needed
            } else {
                //flag for removing
                removeQueue.add(v);
            }
        }

        //delete old markers from drawer
        for (Vector3i v: removeQueue) {
            MissionMapDrawer.instance.removeMarker(openQuestMarkers.get(v));
            openQuestMarkers.remove(v);
        }

        Vector3f halfSectorOffset = MissionMapDrawer.posFromSector(new Vector3i(1,1,1),true);
        halfSectorOffset.scale(0.5f);
        //add new markers to list and drawer
        for (Vector3i v: markers) {
           // if (!openQuestMarkers.containsKey(v)) {
                //add new marker
                MapMarker questMarker = new MapMarker(v,"open mission", MapIcon.WP_QUEST,MissionMapDrawer.brightYellow) {
                    @Override
                    public boolean canDraw() {
                        return MissionClient.instance.isDrawOpenMarkers();
                    }
                };
            //    questMarker.getMapPos().add(halfSectorOffset);
            //    questMarker.setBaseScale(0.03f); //instead of 0.1 -> smaller.
                openQuestMarkers.put(v,questMarker);
                MissionMapDrawer.instance.addMarker(questMarker);
          // }
        }
        MissionMapDrawer.instance.updateInternalList();
    }

    public boolean isDrawOpenMarkers() {
        return drawOpenMarkers;
    }

    public void setDrawOpenMarkers(boolean drawOpenMarkers) {
        this.drawOpenMarkers = drawOpenMarkers;
    }
}
