package com.mialliance.mind.tasks;

/**
 * TaskStates represent states of {@link PrimitiveTask PrimitiveTasks} when ticking their activities. Each state returned
 * in the {@link PrimitiveTask#tick(TaskOwner)} function indicates to the Planner that a different action should occur
 * in regard to the current Task.
 */
public enum TaskState {

    /**
     * SUCCESS represents a successful operation of a Task's execution, ending the Task and continuing to the next,
     * or making a new plan if the task was the last task in the queue.
     */
    SUCCESS,

    /**
     * FAILURE represents a failed operation of a Task's execution, ending the Task and forcing the Planner to make
     * a new plan to adjust to the new state. The current plan then, consequently, gets discarded.
     */
    FAILURE,

    /**
     * PROCESSING represents an unfinished operation of a Task's execution, indicating to the Planner that it should
     * tick this Task again and again until the result is complete with the current task.
     */
    PROCESSING
}
