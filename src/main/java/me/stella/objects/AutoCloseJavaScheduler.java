package me.stella.objects;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class AutoCloseJavaScheduler {

    private final Set<Future<?>> tasks;
    private final ScheduledExecutorService scheduler;

    public AutoCloseJavaScheduler(ScheduledExecutorService agent) {
        this.scheduler = agent;
        this.tasks = new HashSet<>();
        scheduleWithFixedDelay(() -> {
            try {
                (new HashSet<>(tasks)).forEach(task -> {
                    if(task.isDone()) {
                        try {
                            task.cancel(true);
                            tasks.remove(task);
                        } catch(Exception err) {}
                    }
                });
                System.gc();
            } catch(Exception err) { err.printStackTrace(); }
        }, 10L, 30L, TimeUnit.SECONDS);
    }

    public int getRunningTasks() {
        return this.tasks.size();
    }

    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    // invoke shutdown logic
    public void shutdown() {
        clearTasks();
        System.gc();
        scheduler.shutdown();
    }

    public void clearTasks() {
        (new HashSet<>(tasks)).forEach(task -> {
            try {
                task.cancel(true);
                tasks.remove(task);
            } catch(Exception err) {}
        });
    }

    public <T> Future<?> submit(Runnable task, T result) {
        Future<?> future = scheduler.submit(task, result);
        tasks.add(future); return future;
    }

    public Future<?> submit(Runnable task) {
        Future<?> future = scheduler.submit(task);
        tasks.add(future); return future;
    }

    public <V> Future<?> submit(Callable<V> task) {
        Future<?> future = scheduler.submit(task);
        tasks.add(future); return future;
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(task, delay, timeUnit);
        tasks.add(scheduledFuture);
        return scheduledFuture;
    }

    public <V> ScheduledFuture<?> schedule(Callable<V> task, long delay, TimeUnit timeUnit) {
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(task, delay, timeUnit);
        tasks.add(scheduledFuture);
        return scheduledFuture;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long interval, TimeUnit timeUnit) {
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, initialDelay, interval, timeUnit);
        tasks.add(scheduledFuture); return scheduledFuture;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, initialDelay, delay, timeUnit);
        tasks.add(scheduledFuture); return scheduledFuture;
    }

}