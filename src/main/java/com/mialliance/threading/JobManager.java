package com.mialliance.threading;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public final class JobManager {

    // Deallocated at startup to prevent taking up unnecessary memory
    @Nullable
    private static ThreadPoolExecutor threadPool = null;

    public static boolean isAlive() {
        return threadPool != null;
    }

    /**
     * Used when starting the Server, or joining a Server if any client jobs are needed.
     */
    public static void open() {
        if (threadPool == null) {
            threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }
    }

    /**
     * Used when shutting down the Server, or disconnecting from the Server if any client jobs are needed.
     */
    public static void close() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
    }

    /**
     * Submits a job to the Thread Pool to complete asynchronously.
     * @param job The job to execute.
     * @return Null if Threads are not available to submit to, or a Future for the result otherwise.
     * @param <V> The type of value to return.
     */
    @Nullable
    public static <V> Future<V> submitJob(@NotNull Callable<V> job) {
        if (threadPool != null) {
            return threadPool.submit(job);
        }
        return null;
    }

    /**
     * Submits a job to the Thread Pool to complete asynchronously.
     * @param job The job to execute.
     * @return Null if Threads are not available to submit to, or a Future for the result otherwise.
     * @param <V> The type of value to return.
     */
    @Nullable
    public static <V> Future<V> submitJob(@NotNull Runnable job, @NotNull V returnObject) {
        if (threadPool != null) {
            return threadPool.submit(job, returnObject);
        }
        return null;
    }

    /**
     * Submits a job to the Thread Pool to complete asynchronously.
     * @param job The job to execute.
     * @return Null if Threads are not available to submit to, or a Future representing the job, which will hold null upon job completion.
     */
    @Nullable
    public static Future<?> submitJob(@NotNull Runnable job) {
        if (threadPool != null) {
            return threadPool.submit(job);
        }
        return null;
    }

}
