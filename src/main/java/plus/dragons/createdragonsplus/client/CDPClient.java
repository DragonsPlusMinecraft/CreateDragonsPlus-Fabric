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

package plus.dragons.createdragonsplus.client;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import plus.dragons.createdragonsplus.client.color.SimpleItemColors;
import plus.dragons.createdragonsplus.client.model.CDPPartialModels;
import plus.dragons.createdragonsplus.client.ponder.CDPPonderPlugin;
import plus.dragons.createdragonsplus.client.renderer.blockentity.BlazeBlockEntityClient;
import plus.dragons.createdragonsplus.client.renderer.fluid.CDPFluidRendering;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeClientHooks;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;

public class CDPClient implements ClientModInitializer {
    public CDPClient() {
        CDPPartialModels.register();
    }

    @Override
    public void onInitializeClient() {
        CDPFluidRendering.register();
        SimpleItemColors.registerDyeBuckets();
        PonderIndex.addPlugin(new CDPPonderPlugin());
        BlazeClientHooks.registerTickHook(BlazeBlockEntityClient::tick);
        BlockEntityRendererRegistry.register(CDPBlockEntities.FLUID_HATCH.get(), SmartBlockEntityRenderer::new);
    }
}
