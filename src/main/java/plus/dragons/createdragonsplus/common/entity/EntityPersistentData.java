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

package plus.dragons.createdragonsplus.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/** Loader-neutral access to entity data persisted under Forge's legacy NBT key. */
public final class EntityPersistentData {
    public static final String NBT_KEY = "ForgeData";

    public static CompoundTag get(Entity entity) {
        return ((Holder) entity).createDragonsPlus$getPersistentData();
    }

    public interface Holder {
        CompoundTag createDragonsPlus$getPersistentData();
    }

    private EntityPersistentData() {}
}
