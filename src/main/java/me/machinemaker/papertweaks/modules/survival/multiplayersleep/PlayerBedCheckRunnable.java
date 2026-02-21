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
package me.machinemaker.papertweaks.modules.survival.multiplayersleep;

import java.util.function.Consumer;
import me.machinemaker.papertweaks.utils.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

class PlayerBedCheckRunnable implements Runnable {

    private final Player player;
    private final Consumer<Player> sleepingCallback;
    private SchedulerUtil.Task task;

    PlayerBedCheckRunnable(final Player player, final Consumer<Player> sleepingCallback) {
        this.player = player;
        this.sleepingCallback = sleepingCallback;
    }

    @Override
    public void run() {
        if (this.player.getSleepTicks() >= 100) {
            this.sleepingCallback.accept(this.player);
        }
    }

    public SchedulerUtil.Task runTaskTimer(final Plugin plugin, final long delay, final long period) {
        this.task = SchedulerUtil.runEntityTaskTimer(plugin, this.player, t -> this.run(), null, delay, period);
        return this.task;
    }
}
