package com.mialliance.mind.base.tasks.identifiers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class TaskIdentifier {

    private final ArrayDeque<String> components;

    TaskIdentifier(String... components) {
        this.components = new ArrayDeque<>();
        for (String component : components) {
            this.components.push(component);
        }
    }

    private TaskIdentifier(TaskIdentifier old) {
        this.components = new ArrayDeque<>();
        for (String str : old.components) {
            this.components.push(str);
        }
    }

    public void push(String name) {
        this.components.push(name);
    }

    public synchronized String pop() {
        return this.components.pop();
    }

    public synchronized String peek() {
        return this.components.peek();
    }

    public int size() {
        return this.components.size();
    }

    public TaskIdentifier copy() {
        return new TaskIdentifier();
    }

    public static TaskIdentifier of(@NotNull String... components) {
        return new TaskIdentifier(components);
    }

    public static TaskIdentifier of() {
        return new TaskIdentifier();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        components.forEach(str -> builder.append(str).append(':'));
        return builder.deleteCharAt(builder.length()-1).toString();
    }

}
