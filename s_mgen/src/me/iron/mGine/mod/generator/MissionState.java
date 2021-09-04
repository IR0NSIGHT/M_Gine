package me.iron.mGine.mod.generator;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.09.2021
 * TIME: 14:34
 */
public enum MissionState {
    OPEN("open"),
    IN_PROGRESS("in progress"),
    SUCCESS("success"),
    FAILED("failed"),
    ABORTED("aborted");

    private String name;

    private MissionState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
