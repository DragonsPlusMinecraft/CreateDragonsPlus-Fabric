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

package plus.dragons.createdragonsplus.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import plus.dragons.createdragonsplus.common.CDPCommon;

public final class CDPConfig {
    private static final CDPCommonConfig COMMON_CONFIG = new CDPCommonConfig();
    private static final CDPClientConfig CLIENT_CONFIG = new CDPClientConfig();
    private static final CDPServerConfig SERVER_CONFIG = new CDPServerConfig();
    private static final ForgeConfigSpec COMMON_SPEC = createSpec(COMMON_CONFIG);
    private static final ForgeConfigSpec CLIENT_SPEC = createSpec(CLIENT_CONFIG);
    private static final ForgeConfigSpec SERVER_SPEC = createSpec(SERVER_CONFIG);
    private static boolean registered;

    private CDPConfig() {}

    private static ForgeConfigSpec createSpec(net.createmod.catnip.config.ConfigBase config) {
        return new ForgeConfigSpec.Builder().configure(builder -> {
            config.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue();
    }

    public static void register() {
        if (registered)
            return;
        registered = true;
        Util.make(COMMON_SPEC, spec -> ForgeConfigRegistry.INSTANCE.register(CDPCommon.ID, Type.COMMON, spec));
        Util.make(CLIENT_SPEC, spec -> ForgeConfigRegistry.INSTANCE.register(CDPCommon.ID, Type.CLIENT, spec));
        Util.make(SERVER_SPEC, spec -> ForgeConfigRegistry.INSTANCE.register(CDPCommon.ID, Type.SERVER, spec));
    }

    public static CDPCommonConfig common() {
        return COMMON_CONFIG;
    }

    public static CDPClientConfig client() {
        return CLIENT_CONFIG;
    }

    public static CDPServerConfig server() {
        return SERVER_CONFIG;
    }

    public static CDPFeaturesConfig features() {
        return COMMON_CONFIG.features;
    }

    public static CDPRecipesConfig recipes() {
        return SERVER_CONFIG.recipes;
    }

    public static CDPDyeFluidConfig dyeFluid() {
        return SERVER_CONFIG.dyeFluid;
    }
}
