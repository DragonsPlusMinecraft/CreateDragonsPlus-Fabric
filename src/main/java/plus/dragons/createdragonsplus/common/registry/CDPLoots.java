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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import plus.dragons.createdragonsplus.config.CDPConfig;

public final class CDPLoots {
    private static final Object2IntMap<ResourceLocation> BLAZE_UPGRADE_SMITHING_TEMPLATE = Util.make(
            new Object2IntOpenHashMap<>(),
            map -> {
                map.put(BuiltInLootTables.BASTION_TREASURE, 1);
                map.put(BuiltInLootTables.BASTION_OTHER, 10);
                map.put(BuiltInLootTables.BASTION_BRIDGE, 10);
                map.put(BuiltInLootTables.BASTION_HOGLIN_STABLE, 10);
                map.put(BuiltInLootTables.NETHER_BRIDGE, 10);
            });

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, table, source) -> {
            if (CDPConfig.features().blazeUpgradeSmithingTemplate.get()
                    && BLAZE_UPGRADE_SMITHING_TEMPLATE.containsKey(id))
                addBlazeUpgradeSmithingTemplate(table, BLAZE_UPGRADE_SMITHING_TEMPLATE.getInt(id));
        });
    }

    private static void addBlazeUpgradeSmithingTemplate(LootTable.Builder table, int totalWeight) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE).setWeight(1));
        if (totalWeight > 1)
            pool.add(EmptyLootItem.emptyItem().setWeight(totalWeight - 1));
        table.withPool(pool);
    }

    private CDPLoots() {}
}
