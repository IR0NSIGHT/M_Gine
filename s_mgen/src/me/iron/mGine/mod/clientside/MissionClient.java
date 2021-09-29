package me.iron.mGine.mod.clientside;

import api.common.GameClient;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.GUI.GUIActiveMissionTab;
import me.iron.mGine.mod.clientside.GUI.MissionGUIControlManager;
import me.iron.mGine.mod.clientside.map.MissionMapDrawer;
import me.iron.mGine.mod.clientside.map.TaskMarker;
import me.iron.mGine.mod.generator.M_GineCore;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;

import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.09.2021
 * TIME: 16:33
 */
public class MissionClient {
    public static MissionClient instance;
    public HashSet<Mission> active = new HashSet<>();
    public HashSet<Mission> available = new HashSet<>();
    public HashSet<Mission> finished = new HashSet<>();
    public HashSet <MissionTask> currentTasks = new HashSet<>();

    public static boolean autoNav = true;
    private Mission selectedMission;

    public void setSelectedMission(Mission selectedMission) {
        this.selectedMission = selectedMission;
        MissionMapDrawer.instance.getMapMarkers().clear();
        for (MissionTask task: selectedMission.getMissionTasks()) {
            if (task.getTaskSector() != null)
                MissionMapDrawer.instance.addMarker(new TaskMarker(task));
        }
        MissionMapDrawer.instance.updateInternalList();
        GUIActiveMissionTab.instance.getSelectedMissionFromClient();
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
            @Override
            public void run() {
                update();
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
        updateAllMissions();
        updateSelectedMission();
        updateSelectedTask();
        updateCurrentTasks();
    }

    private void updateSelectedMission() {
        if (selectedMission != null) {

        }
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

    private void updateAllMissions() {
        //TODO get missions from server
        available.clear();
        active.clear();
        finished.clear();

        for (Mission m: M_GineCore.instance.getMissions()) {
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
    }

    private MissionTask getNextActiveTask(Mission m) {
        for (MissionTask t: m.getMissionTasks()) {
            if (t.getCurrentState().equals(MissionState.IN_PROGRESS)) {
                return t;
            }
        }
        return null;
    }
}
