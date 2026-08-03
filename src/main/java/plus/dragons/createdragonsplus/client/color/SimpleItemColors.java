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

package plus.dragons.createdragonsplus.client.color;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.item.ItemColor;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariant;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;

public class SimpleItemColors {
    public static Supplier<ItemColor> singleLayer(int tintColor) {
        return () -> (stack, tintIndex) -> tintIndex > 0 ? -1 : tintColor;
    }

    public static void registerDyeBuckets() {
        DyeVariantRegistry.all().forEach(variant -> ColorProviderRegistry.ITEM.register(
                singleLayer(tintColor(variant)).get(),
                CDPFluids.DYES_BY_VARIANT.get(variant.id()).getBucket().get()));
    }

    private static int tintColor(DyeVariant variant) {
        return 0xFF000000 | variant.color();
    }
}
