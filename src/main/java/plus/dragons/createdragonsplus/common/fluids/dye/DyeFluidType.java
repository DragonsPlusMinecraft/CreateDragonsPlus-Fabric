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

package plus.dragons.createdragonsplus.common.fluids.dye;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.sound.SoundActions;
import net.createmod.catnip.theme.Color;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;
import plus.dragons.createdragonsplus.config.CDPConfig;

public final class DyeFluidType extends SolidRenderFluidType {
    private final DyeVariant variant;

    private DyeFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            int tintColor, Vector3f fogColor, DyeVariant variant) {
        super(properties, stillTexture, flowingTexture, tintColor, fogColor, DyeFluidType::getVisibility);
        this.variant = variant;
    }

    public static DyeFluidType create(DyeVariant variant, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        int rgbColor = variant.color();
        int tintColor = 0xFF000000 | rgbColor;
        Vector3f fogColor = new Color(rgbColor, false).asVectorF();
        FluidType.Properties properties = FluidType.Properties.create()
                .descriptionId(Util.makeDescriptionId("fluid", CDPCommon.asResource(variant.fluidName())))
                .fallDistanceModifier(0)
                .canExtinguish(true)
                .supportsBoating(true)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
        return new DyeFluidType(properties, stillTexture, flowingTexture, tintColor, fogColor, variant);
    }

    private static float getVisibility() {
        return CDPConfig.client().dyeVisionMultiplier.getF() / 256;
    }

    public DyeVariant getVariant() {
        return variant;
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return level.dimensionType().ultraWarm();
    }
}
