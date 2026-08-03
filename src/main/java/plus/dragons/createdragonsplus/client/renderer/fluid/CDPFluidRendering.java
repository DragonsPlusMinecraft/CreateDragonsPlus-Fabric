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

package plus.dragons.createdragonsplus.client.renderer.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;

public final class CDPFluidRendering {
    private static final float BASE_WATER_FOG_DISTANCE = 96.0F;

    public static void register() {
        CDPFluids.DYES_BY_VARIANT.forEach((variantId, entry) -> register(entry, CDPFluids.DYE_TYPES.get(variantId)));
        register(CDPFluids.DRAGON_BREATH, CDPFluids.DRAGON_BREATH_TYPE);

        FogEvents.RENDER_FOG.register((mode, fogType, camera, partialTick, renderDistance, nearDistance,
                farDistance, shape, fogData) -> {
            SolidRenderFluidType fluidType = getSubmergedFluidType(camera);
            if (fluidType == null)
                return false;
            fogData.setFogShape(FogShape.CYLINDER);
            fogData.setNearPlaneDistance(-8.0F);
            fogData.setFarPlaneDistance(Math.max(0.25F,
                    BASE_WATER_FOG_DISTANCE * fluidType.getFogDistanceModifier()));
            return true;
        });
        FogEvents.SET_COLOR.register((data, partialTicks) -> {
            SolidRenderFluidType fluidType = getSubmergedFluidType(data.getCamera());
            if (fluidType == null)
                return;
            Vector3f color = fluidType.getCustomFogColor();
            data.setRed(color.x());
            data.setGreen(color.y());
            data.setBlue(color.z());
        });
    }

    private static void register(FluidEntry<?> entry, SolidRenderFluidType fluidType) {
        Fluid source = entry.getSource();
        Fluid flowing = entry.get();
        var handler = new SimpleFluidRenderHandler(
                fluidType.getStillTexture(), fluidType.getFlowingTexture(), fluidType.getTintColor());
        FluidRenderHandlerRegistry.INSTANCE.register(source, flowing, handler);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), source, flowing);

        if (FluidRenderHandlerRegistry.INSTANCE.get(source) != handler
                || FluidRenderHandlerRegistry.INSTANCE.get(flowing) != handler) {
            throw new IllegalStateException("Create: Dragons Plus fluid render handler was not registered");
        }
        int actualColor = handler.getFluidColor(null, null, source.defaultFluidState()) & 0x00FFFFFF;
        int expectedColor = fluidType.getTintColor() & 0x00FFFFFF;
        if (actualColor != expectedColor) {
            throw new IllegalStateException("Create: Dragons Plus fluid tint mismatch: expected "
                    + Integer.toHexString(expectedColor) + ", got " + Integer.toHexString(actualColor));
        }
    }

    private static @Nullable SolidRenderFluidType getSubmergedFluidType(Camera camera) {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return null;
        BlockPos pos = camera.getBlockPosition();
        FluidState state = level.getFluidState(pos);
        if (camera.getPosition().y >= pos.getY() + state.getHeight(level, pos))
            return null;
        return state.getFluidType() instanceof SolidRenderFluidType fluidType ? fluidType : null;
    }

    private CDPFluidRendering() {}
}
