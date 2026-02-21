/*
 * GNU General Public License v3
 *
 * PaperTweaks, a performant replacement for the VanillaTweaks datapacks.
 *
 * Copyright (C) 2021-2026 Machine_Maker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.machinemaker.papertweaks.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for scheduling tasks that works on both Bukkit and Folia.
 * Automatically detects if running on Folia and uses region-aware schedulers.
 */
public final class SchedulerUtil {

    private static final boolean IS_FOLIA;

    static {
        boolean isFolia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (final ClassNotFoundException e) {
            isFolia = false;
        }
        IS_FOLIA = isFolia;
    }

    private SchedulerUtil() {
    }

    /**
     * Check if the server is running Folia.
     *
     * @return true if running on Folia
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Run a task on the main thread (or entity's region for Folia).
     *
     * @param plugin the plugin
     * @param task the task to run
     */
    public static void runTask(final Plugin plugin, final Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task later on the main thread (or region for Folia).
     *
     * @param plugin the plugin
     * @param task the task to run
     * @param delay the delay in ticks
     */
    public static void runTaskLater(final Plugin plugin, final Runnable task, final long delay) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Run a task asynchronously.
     *
     * @param plugin the plugin
     * @param task the task to run
     */
    public static void runTaskAsynchronously(final Plugin plugin, final Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Run a task for a specific entity.
     *
     * @param plugin the plugin
     * @param entity the entity
     * @param task the task to run
     * @param retired callback if entity is retired/removed
     */
    public static void runEntityTask(final Plugin plugin, final Entity entity, final Runnable task, final @Nullable Runnable retired) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
        } else {
            if (entity.isValid()) {
                Bukkit.getScheduler().runTask(plugin, task);
            } else if (retired != null) {
                retired.run();
            }
        }
    }

    /**
     * Run a task for a specific entity with a delay.
     *
     * @param plugin the plugin
     * @param entity the entity
     * @param task the task to run
     * @param retired callback if entity is retired/removed
     * @param delay the delay in ticks
     */
    public static void runEntityTaskLater(final Plugin plugin, final Entity entity, final Runnable task, final @Nullable Runnable retired, final long delay) {
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), retired, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (entity.isValid()) {
                    task.run();
                } else if (retired != null) {
                    retired.run();
                }
            }, delay);
        }
    }

    /**
     * Run a repeating task for a specific entity.
     *
     * @param plugin the plugin
     * @param entity the entity
     * @param task the task to run
     * @param retired callback if entity is retired/removed
     * @param delay initial delay in ticks
     * @param period period between runs in ticks
     * @return the task wrapper
     */
    public static Task runEntityTaskTimer(final Plugin plugin, final Entity entity, final Consumer<Task> task, final @Nullable Runnable retired, final long delay, final long period) {
        if (IS_FOLIA) {
            final ScheduledTask scheduledTask = entity.getScheduler().runAtFixedRate(plugin, foliaTask -> task.accept(new Task(foliaTask)), retired, delay, period);
            return new Task(scheduledTask);
        } else {
            final org.bukkit.scheduler.BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (entity.isValid()) {
                    task.accept(Task.EMPTY);
                } else {
                    if (retired != null) {
                        retired.run();
                    }
                }
            }, delay, period);
            return new Task(bukkitTask);
        }
    }

    /**
     * Run a task at a specific location.
     *
     * @param plugin the plugin
     * @param location the location
     * @param task the task to run
     */
    public static void runAtLocation(final Plugin plugin, final Location location, final Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task at a specific location with a delay.
     *
     * @param plugin the plugin
     * @param location the location
     * @param task the task to run
     * @param delay the delay in ticks
     */
    public static void runAtLocationLater(final Plugin plugin, final Location location, final Runnable task, final long delay) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Run a repeating task at a specific location.
     *
     * @param plugin the plugin
     * @param location the location
     * @param task the task to run
     * @param delay initial delay in ticks
     * @param period period between runs in ticks
     * @return the task wrapper
     */
    public static Task runAtLocationTimer(final Plugin plugin, final Location location, final Consumer<Task> task, final long delay, final long period) {
        if (IS_FOLIA) {
            final ScheduledTask scheduledTask = Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, foliaTask -> task.accept(new Task(foliaTask)), delay, period);
            return new Task(scheduledTask);
        } else {
            final org.bukkit.scheduler.BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(Task.EMPTY), delay, period);
            return new Task(bukkitTask);
        }
    }

    /**
     * Run a repeating global task.
     *
     * @param plugin the plugin
     * @param task the task to run
     * @param delay initial delay in ticks
     * @param period period between runs in ticks
     * @return the task wrapper
     */
    public static Task runTaskTimer(final Plugin plugin, final Consumer<Task> task, final long delay, final long period) {
        if (IS_FOLIA) {
            final ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, foliaTask -> task.accept(new Task(foliaTask)), delay, period);
            return new Task(scheduledTask);
        } else {
            final org.bukkit.scheduler.BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(Task.EMPTY), delay, period);
            return new Task(bukkitTask);
        }
    }

    /**
     * Run a repeating asynchronous task.
     *
     * @param plugin the plugin
     * @param task the task to run
     * @param delay initial delay in milliseconds
     * @param period period between runs in milliseconds
     * @return the task wrapper
     */
    public static Task runTaskTimerAsynchronously(final Plugin plugin, final Consumer<Task> task, final long delay, final long period) {
        if (IS_FOLIA) {
            final ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, foliaTask -> task.accept(new Task(foliaTask)), delay, period, TimeUnit.MILLISECONDS);
            return new Task(scheduledTask);
        } else {
            // Convert milliseconds to ticks (20 ticks per second)
            final long delayTicks = delay / 50;
            final long periodTicks = period / 50;
            final org.bukkit.scheduler.BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> task.accept(Task.EMPTY), delayTicks, periodTicks);
            return new Task(bukkitTask);
        }
    }

    /**
     * Wrapper class for scheduled tasks that works on both Bukkit and Folia.
     */
    public static class Task {
        private static final Task EMPTY = new Task(null, null);

        private final ScheduledTask foliaTask;
        private final org.bukkit.scheduler.BukkitTask bukkitTask;

        private Task(final ScheduledTask foliaTask) {
            this.foliaTask = foliaTask;
            this.bukkitTask = null;
        }

        private Task(final org.bukkit.scheduler.BukkitTask bukkitTask) {
            this.foliaTask = null;
            this.bukkitTask = bukkitTask;
        }

        private Task(final ScheduledTask foliaTask, final org.bukkit.scheduler.BukkitTask bukkitTask) {
            this.foliaTask = foliaTask;
            this.bukkitTask = bukkitTask;
        }

        /**
         * Cancel this task.
         */
        public void cancel() {
            if (this.foliaTask != null) {
                this.foliaTask.cancel();
            } else if (this.bukkitTask != null) {
                this.bukkitTask.cancel();
            }
        }

        /**
         * Check if this task is cancelled.
         *
         * @return true if cancelled
         */
        public boolean isCancelled() {
            if (this.foliaTask != null) {
                return this.foliaTask.isCancelled();
            } else if (this.bukkitTask != null) {
                return this.bukkitTask.isCancelled();
            }
            return true;
        }
    }
}
