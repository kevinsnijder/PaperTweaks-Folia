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
package me.machinemaker.papertweaks.utils.runnables;

import com.google.inject.Inject;
import me.machinemaker.papertweaks.utils.SchedulerUtil;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class TimerRunnable implements Runnable {

    protected final Plugin plugin;
    private SchedulerUtil.Task currentTask;

    @Inject
    protected TimerRunnable(final Plugin plugin) {
        this.plugin = plugin;
    }

    private static void checkScheduled(final SchedulerUtil.Task task) {
        if (task == null || task.isCancelled()) {
            throw new IllegalStateException("Not scheduled yet");
        }
    }

    private static void checkNotYetScheduled(final SchedulerUtil.Task task) {
        if (task != null && !task.isCancelled()) {
            throw new IllegalStateException("Already scheduled");
        }
    }

    public synchronized SchedulerUtil.Task runTaskTimer(final long delay, final long period) throws IllegalStateException {
        checkNotYetScheduled(this.currentTask);
        this.currentTask = SchedulerUtil.runTaskTimer(this.plugin, task -> this.run(), delay, period);
        return this.currentTask;
    }

    public synchronized SchedulerUtil.Task runTaskTimerAsynchronously(final long delay, final long period) throws IllegalStateException {
        checkNotYetScheduled(this.currentTask);
        this.start();
        this.currentTask = SchedulerUtil.runTaskTimerAsynchronously(this.plugin, task -> this.run(), delay, period);
        return this.currentTask;
    }

    protected void start() {
    }

    public synchronized void cancel() {
        if (this.currentTask != null) {
            this.currentTask.cancel();
        }
    }

    public synchronized boolean isCancelled() {
        return this.currentTask == null || this.currentTask.isCancelled();
    }
}
