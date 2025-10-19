package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.strategy.IStrategy;

import java.util.Random;

public class IdleStrategy implements IStrategy {

    private static final Random RANDOM = new Random();

    private final int lowerTickBound;
    private final int upperTickBound;

    private int generatedTime;
    private int passedTime;

    private boolean completed;

    public IdleStrategy(int timeToIdle) {
        this(timeToIdle, timeToIdle);
    }

    public IdleStrategy(int lowerBound, int upperBound) {
        lowerTickBound = lowerBound;
        upperTickBound = upperBound;
        generatedTime = 0;
        passedTime = 0;
        completed = false;
    }

    @Override
    public void start() {
        this.completed = false;
        generatedTime = RANDOM.nextInt(lowerTickBound, upperTickBound);
        passedTime = 0;
    }

    @Override
    public void tick() {
        if (!completed && passedTime++ >= generatedTime) {
            completed = true;
        }
    }

    @Override
    public void stop(boolean successful) {}

    @Override
    public boolean canPerform() {
        return true;
    }

    @Override
    public boolean isComplete() {
        return completed;
    }

}
