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

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import java.util.concurrent.atomic.AtomicInteger;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import plus.dragons.createdragonsplus.client.color.SimpleItemColors;
import plus.dragons.createdragonsplus.client.model.CDPPartialModels;
import plus.dragons.createdragonsplus.client.ponder.CDPPonderPlugin;
import plus.dragons.createdragonsplus.client.renderer.blockentity.BlazeBlockEntityClient;
import plus.dragons.createdragonsplus.client.renderer.fluid.CDPFluidRendering;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeClientHooks;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;

public class CDPClient implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        CDPFluidRendering.register();
        SimpleItemColors.registerDyeBuckets();
        PonderIndex.addPlugin(new CDPPonderPlugin());
        CDPPartialModels.register();
        BlazeClientHooks.registerTickHook(BlazeBlockEntityClient::tick);
        BlockEntityRendererRegistry.register(CDPBlockEntities.FLUID_HATCH.get(), SmartBlockEntityRenderer::new);
        registerSmokeTestShutdown();
    }

    private static void registerSmokeTestShutdown() {
        if (!Boolean.getBoolean("create_dragons_plus.client_smoke_test"))
            return;
        boolean jeiSmokeTest = Boolean.getBoolean("create_dragons_plus.jei_smoke_test");
        if (jeiSmokeTest && !FabricLoader.getInstance().isModLoaded("jei"))
            throw new IllegalStateException("The JEI client smoke test requires JEI to be installed");
        var readyTicks = new AtomicInteger();
        var totalTicks = new AtomicInteger();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (totalTicks.incrementAndGet() > 1200) {
                throw new IllegalStateException("Create: Dragons Plus client smoke test timed out; JEI state: "
                        + JeiSmokeTestStatus.summary());
            }
            boolean ready = jeiSmokeTest
                    ? client.level != null && JeiSmokeTestStatus.isReady()
                    : client.screen != null;
            if (!ready || readyTicks.incrementAndGet() < (jeiSmokeTest ? 100 : 200))
                return;
            verifyClientRegistrations();
            LOGGER.info("Create: Dragons Plus client smoke test passed (JEI state: {}); shutting down cleanly",
                    JeiSmokeTestStatus.summary());
            client.stop();
        });
    }

    private static void verifyClientRegistrations() {
        SimpleItemColors.verifyDyeBuckets();
        boolean bulkSandingRegistered = PonderIndex.getSceneAccess().getRegisteredEntries().stream()
                .anyMatch(entry -> AllBlocks.ENCASED_FAN.getId().equals(entry.getKey())
                        && CDPCommon.asResource("bulk_sanding").equals(entry.getValue().getSchematicLocation()));
        if (!bulkSandingRegistered)
            throw new IllegalStateException("Bulk Sanding Ponder scene was not registered");
    }
}
