/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.client.renderer.blockentity;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;

public final class BlazeBlockEntityClient {
    private BlazeBlockEntityClient() {}

    public static void tick(BlazeBlockEntity blockEntity) {
        if (VisualizationManager.supportsVisualization(blockEntity.getLevel()))
            return;

        tickAnimation(blockEntity);
    }

    public static void tickAnimation(BlazeBlockEntity blockEntity) {
        boolean active = blockEntity.getHeatLevelFromBlock().isAtLeast(BlazeBurnerBlock.HeatLevel.FADING)
                && blockEntity.isActive();
        if (active) {
            blockEntity.headAngle.chase((AngleHelper.horizontalAngle(blockEntity.getBlockState()
                    .getOptionalValue(BlazeBurnerBlock.FACING)
                    .orElse(Direction.SOUTH)) + 180) % 360, .125f, Chaser.EXP);
            blockEntity.headAngle.tickChaser();
        } else {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (blockEntity.isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (blockEntity.getBlockPos().getX() + 0.5);
                double dz = z - (blockEntity.getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = blockEntity.headAngle.getValue()
                    + AngleHelper.getShortestAngleDiff(blockEntity.headAngle.getValue(), target);
            blockEntity.headAngle.chase(target, .25f, Chaser.exp(5));
            blockEntity.headAngle.tickChaser();
        }

        blockEntity.headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        blockEntity.headAnimation.tickChaser();
    }
}
