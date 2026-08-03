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

package plus.dragons.createdragonsplus.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;
import static plus.dragons.createdragonsplus.common.registry.CDPBlocks.FLUID_HATCH;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.RARE_BLAZE_PACKAGE;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.RARE_MARBLE_GATE_PACKAGE;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.config.CDPConfig;

public final class CDPCreativeModeTabs {
    public static final ResourceLocation BASE_ID = CDPCommon.asResource("base");
    private static CreativeModeTab base;

    public static void register() {
        if (base == null)
            base = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BASE_ID, createBase());
    }

    public static CreativeModeTab base() {
        if (base == null)
            throw new IllegalStateException("Creative mode tab has not been registered");
        return base;
    }

    private static CreativeModeTab createBase() {
        return FabricItemGroup.builder()
                .title(REGISTRATE.addLang("itemGroup", BASE_ID, CDPCommon.NAME))
                .icon(RARE_MARBLE_GATE_PACKAGE::asStack)
                .displayItems(CDPCreativeModeTabs::buildBaseContents)
                .build();
    }

    private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (CDPConfig.features().fluidHatch.get())
            output.accept(FLUID_HATCH);
        if (CDPConfig.features().blazeUpgradeSmithingTemplate.get())
            output.accept(BLAZE_UPGRADE_SMITHING_TEMPLATE);
        if (CDPConfig.features().dyeFluids.get())
            for (var variant : DyeVariantRegistry.all()) {
                if (!variant.isAvailable())
                    continue;
                CDPFluids.DYES_BY_VARIANT.get(variant.id()).getBucket().ifPresent(output::accept);
            }
        if (CDPConfig.features().dragonBreathFluid.get())
            CDPFluids.DRAGON_BREATH.getBucket().ifPresent(output::accept);
        output.accept(RARE_BLAZE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
        output.accept(RARE_MARBLE_GATE_PACKAGE, TabVisibility.SEARCH_TAB_ONLY);
    }

    private CDPCreativeModeTabs() {}
}
