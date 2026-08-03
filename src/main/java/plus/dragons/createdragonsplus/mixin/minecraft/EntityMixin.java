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

package plus.dragons.createdragonsplus.mixin.minecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.common.entity.EntityPersistentData;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityPersistentData.Holder {
    @Unique
    private CompoundTag createDragonsPlus$persistentData = new CompoundTag();

    @Override
    public CompoundTag createDragonsPlus$getPersistentData() {
        return createDragonsPlus$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void createDragonsPlus$writePersistentData(
            CompoundTag entityTag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!createDragonsPlus$persistentData.isEmpty())
            entityTag.put(EntityPersistentData.NBT_KEY, createDragonsPlus$persistentData.copy());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void createDragonsPlus$readPersistentData(CompoundTag entityTag, CallbackInfo ci) {
        createDragonsPlus$persistentData = entityTag.contains(EntityPersistentData.NBT_KEY)
                ? entityTag.getCompound(EntityPersistentData.NBT_KEY).copy()
                : new CompoundTag();
    }
}
