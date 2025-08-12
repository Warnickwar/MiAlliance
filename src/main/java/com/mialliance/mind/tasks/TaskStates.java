package com.mialliance.mind.tasks;

public enum TaskStates {

    SUCCESS,    // Task successfully executed and finished
    FAILURE,    // Task failed to execute and exited early
    PROCESSING  // Task is still currently processing, do nothing
}
