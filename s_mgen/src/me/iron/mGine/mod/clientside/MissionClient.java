package me.iron.mGine.mod.clientside;

import api.ModPlayground;
import api.common.GameClient;

import api.listener.Listener;
import api.listener.events.player.PlayerChangeSectorEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.mGine.mod.ModMain;
import me.iron.mGine.mod.clientside.GUI.GUISelectedMissionTab;
import me.iron.mGine.mod.clientside.GUI.GUIMissionListTab;
import me.iron.mGine.mod.clientside.GUI.MissionGUIControlManager;
import me.iron.mGine.mod.clientside.map.MapMarker;
import me.iron.mGine.mod.clientside.map.MissionMapDrawer;
import me.iron.mGine.mod.clientside.map.TaskMarker;
import me.iron.mGine.mod.generator.Mission;
import me.iron.mGine.mod.generator.MissionState;
import me.iron.mGine.mod.generator.MissionTask;
import me.iron.mGine.mod.network.PacketInteractMission;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;

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

    private HashMap<UUID,Mission> uuidMissionHashMap = new HashMap<>();

    public static boolean autoNav = true;
    private Mission selectedMission;

    private boolean drawOpenMarkers;
    private ArrayList<TaskMarker> activeTaskMarkers = new ArrayList<>();
    public void setSelectedMission(Mission selectedMission) {
        for (TaskMarker t: activeTaskMarkers) {
            MissionMapDrawer.instance.getMapMarkers().remove(t);
        }
        activeTaskMarkers.clear();
        this.selectedMission = selectedMission;
        if (selectedMission != null) {
            for (MissionTask task: selectedMission.getMissionTasks()) {
                if (task.getTaskSector() != null) {
                    TaskMarker t = new TaskMarker(task);
                    MissionMapDrawer.instance.addMarker(t);
                    activeTaskMarkers.add(t);
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

        StarLoader.registerListener(PlayerChangeSectorEvent.class, new Listener<PlayerChangeSectorEvent>() {
            @Override
            public void onEvent(PlayerChangeSectorEvent playerChangeSectorEvent) {
                updateSelectedMission();
            }
        },ModMain.instance);
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
        if (selectedMission == null)
            return;

        for (Mission m: finished) {
            if (selectedMission.equals(m)) {
                setSelectedMission(m);
                return;
            }
        }
        for (Mission m: available) {
            if (selectedMission.equals(m) && getMission(m.getUuid())!=null) {
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


        setSelectedMission(null); //old mission doesnt exist anymore in lists.
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
        uuidMissionHashMap.put(m.getUuid(),m);
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
    //    finished.remove(m); dont delete finished mission (edit: why not`?)
        uuidMissionHashMap.remove(m.getUuid());
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

    public void setOpenQuestMarkers(HashMap<UUID,Vector3i> positions) {
        MissionMapDrawer.instance.updateOpenMarkers(positions);
    }

    public Mission getMission(UUID uuid) {
        return uuidMissionHashMap.get(uuid);
    }

    public boolean isDrawOpenMarkers() {
        return drawOpenMarkers;
    }

    public void setDrawOpenMarkers(boolean drawOpenMarkers) {
        this.drawOpenMarkers = drawOpenMarkers;
    }
}
