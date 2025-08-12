package com.mialliance.mind.tasks;

import com.mialliance.mind.agents.MindOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class TaskTraversal {

    private static final char TRAVERSAL_SEPARATOR = ':';

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
