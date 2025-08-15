package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.agents.MindOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class TaskTraversal {

    private static final char TRAVERSAL_SEPARATOR = ':';

    /**
     * Constructs a Task Path to the indicated task.
     * @param ids The task IDs that should be traversed, in order.
     * @return The constructed String which should indicate the path.
     */
    @NotNull
    public static String createPath(@NotNull String... ids) {
        // Use a StringBuilder to avoid creating unnecessary intermediary objects when merging
        // IDs.
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            builder.append(id).append(TRAVERSAL_SEPARATOR);
        }
        return builder.toString();
    }

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
            Optional<BaseTask<O>> foundTask = currentTask.findChild(id);
            if (foundTask.isEmpty()) return Optional.empty();

            BaseTask<O> obtained = foundTask.get();
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
