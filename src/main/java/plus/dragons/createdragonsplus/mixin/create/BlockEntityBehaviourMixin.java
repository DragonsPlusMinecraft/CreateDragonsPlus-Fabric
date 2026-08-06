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

package plus.dragons.createdragonsplus.mixin.create;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.common.behaviours.BehaviourProvider;
import plus.dragons.createdragonsplus.common.registry.CDPCapabilities;

@Mixin(value = BlockEntityBehaviour.class, remap = false)
public class BlockEntityBehaviourMixin {
    @Inject(method = "get(Lnet/minecraft/world/level/block/entity/BlockEntity;Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;)Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;", at = @At("HEAD"), cancellable = true, remap = true)
    private static <T extends BlockEntityBehaviour> void get$getBehaviourProvider(
            BlockEntity blockEntity, BehaviourType<T> type, CallbackInfoReturnable<T> cir) {
        if (blockEntity == null || blockEntity instanceof SmartBlockEntity)
            return;
        Level level = blockEntity.getLevel();
        if (level == null)
            return;
        BehaviourProvider provider = CDPCapabilities.BEHAVIOUR_PROVIDER.find(level, blockEntity.getBlockPos(), null);
        if (provider == null)
            return;
        T behaviour = provider.getBehaviour(type);
        if (behaviour != null)
            cir.setReturnValue(behaviour);
    }
}
