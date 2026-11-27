package com.skyrpg.life.common.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Minimal server-thread tick scheduler for delayed Life Skills work
 * (leaf decay, tree regeneration, respawn timers).
 */
public final class LifeScheduler {
    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    private LifeScheduler() {}

    public static void schedule(Runnable action, int delayTicks) {
        TASKS.add(new ScheduledTask(action, Math.max(1, delayTicks)));
    }

    public static void tick() {
        Iterator<ScheduledTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (task.remaining-- <= 0) {
                iterator.remove();
                try {
                    task.action.run();
                } catch (Exception exception) {
                    com.skyrpg.life.SkyBlockLife.LOGGER.error("Scheduled Life Skills task failed.", exception);
                }
            }
        }
    }

    public static void clear() {
        TASKS.clear();
    }

    private static final class ScheduledTask {
        private final Runnable action;
        private int remaining;

        private ScheduledTask(Runnable action, int remaining) {
            this.action = action;
            this.remaining = remaining;
        }
    }
}
