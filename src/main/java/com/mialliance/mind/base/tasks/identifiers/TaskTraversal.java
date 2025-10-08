package com.mialliance.mind.base.tasks.identifiers;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.CompoundTask;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class TaskTraversal {

    private static final char TRAVERSAL_SEPARATOR = ':';

    /**
     * A utility function used to traverse a Domain and attempt to find a Task based on a String path.
     * @param domain The Behavior Domain of which to traverse through.
     * @param path The path to traverse to.
     * @return An Optional with the found Task, or an empty Optional if no task was found.
     * @param <O> The MindOwner of which the Task and Domain is meant for.
     */
    @NotNull
    public static <O extends MindOwner> Optional<BaseTask<O>> findChild(@NotNull CompoundTask<O> domain, @NotNull String path) {
        if (!isValidIdentifierPath(path)) return Optional.empty();
        String[] parts = path.split(":");

        CompoundTask<O> currentTask = domain;

        for (String id : parts) {
            CompoundTask.TaskSearchResult<O, BaseTask<O>> foundTask = currentTask.findChild(id);
            if (!foundTask.isFound()) return Optional.empty();

            BaseTask<O> obtained = foundTask.getTask();
            if (obtained instanceof CompoundTask<O> cTask) {
                currentTask = cTask;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean isValidIdentifierPath(String identifier) {
        for (char character : identifier.toCharArray()) {
            if (!isValid(character)) return false;
        }
        return true;
    }

    private static boolean isValid(char character) {
        return (character >= '0' && character <= '9') || (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_' || character == ':' || character == '-';
    }
}
