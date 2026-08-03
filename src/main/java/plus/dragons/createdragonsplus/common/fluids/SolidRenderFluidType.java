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

package plus.dragons.createdragonsplus.common.fluids;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.sound.SoundActions;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3f;

/** Common fluid behaviour plus the data consumed by the Fabric client renderer. */
public class SolidRenderFluidType extends FluidType implements FluidVariantAttributeHandler {
    protected static final int NO_ALPHA = 0x00FFFFFF;
    private final int tintColor;
    private final int blockTintColor;
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final Vector3f fogColor;
    private final Supplier<Float> fogDistanceModifier;

    protected SolidRenderFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            int tintColor, Vector3f fogColor, Supplier<Float> fogDistanceModifier) {
        super(properties);
        this.tintColor = tintColor;
        this.blockTintColor = tintColor & NO_ALPHA;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fogColor = fogColor;
        this.fogDistanceModifier = fogDistanceModifier;
    }

    public int getTintColor(FluidStack stack) {
        return tintColor;
    }

    public int getTintColor() {
        return tintColor;
    }

    public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
        return blockTintColor;
    }

    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    public Vector3f getCustomFogColor() {
        return fogColor;
    }

    public float getFogDistanceModifier() {
        return fogDistanceModifier.get();
    }

    @Override
    public Component getName(FluidVariant variant) {
        return getDescription();
    }

    @Override
    public Optional<SoundEvent> getFillSound(FluidVariant variant) {
        return Optional.ofNullable(getSound(SoundActions.BUCKET_FILL));
    }

    @Override
    public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
        return Optional.ofNullable(getSound(SoundActions.BUCKET_EMPTY));
    }

    @Override
    public int getLuminance(FluidVariant variant) {
        return getLightLevel();
    }

    @Override
    public int getTemperature(FluidVariant variant) {
        return getTemperature();
    }

    @Override
    public int getViscosity(FluidVariant variant, net.minecraft.world.level.Level world) {
        return getViscosity();
    }
}
