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

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.config.FeaturesConfig;

public final class CDPConditions {
    public static final ResourceLocation CONFIG_FEATURE_ID = CDPCommon.asResource("config_feature");
    private static boolean registered;

    public static void register() {
        if (registered)
            return;
        registered = true;
        ResourceConditions.register(CONFIG_FEATURE_ID, json -> {
            ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "feature"));
            if (!FeaturesConfig.getFeatures().containsKey(id))
                throw new IllegalArgumentException("No config feature with id [" + id + "] exists");
            return FeaturesConfig.isFeatureEnabled(id);
        });
    }

    private CDPConditions() {}
}
