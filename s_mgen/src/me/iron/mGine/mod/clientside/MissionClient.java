package me.iron.mGine.mod.clientside;

import api.ModPlayground;
import api.common.GameClient;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.GUI.GUISelectedMissionTab;
import me.iron.mGine.mod.clientside.GUI.GUIMissionListTab;
import me.iron.mGine.mod.clientside.GUI.MissionGUIControlManager;
import me.iron.mGine.mod.clientside.map.MissionMapDrawer;
import me.iron.mGine.mod.clientside.map.TaskMarker;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.network.PacketInteractMission;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

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

    public HashSet<Mission> active = new HashSet<>();
    public HashSet<Mission> available = new HashSet<>();
    public HashSet<Mission> finished = new HashSet<>();
    public HashSet <MissionTask> currentTasks = new HashSet<>();

    public static boolean autoNav = true;
    private Mission selectedMission;

    public void setSelectedMission(Mission selectedMission) {
        this.selectedMission = selectedMission;
        MissionMapDrawer.instance.getMapMarkers().clear();
        if (selectedMission != null) {
            for (MissionTask task: selectedMission.getMissionTasks()) {
                if (task.getTaskSector() != null) {
                    MissionMapDrawer.instance.addMarker(new TaskMarker(task));
                //    ModPlayground.broadcastMessage("task marker");
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
        active.remove(m);
        available.remove(m);
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
}
