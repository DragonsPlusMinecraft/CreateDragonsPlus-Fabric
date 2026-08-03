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

package plus.dragons.createdragonsplus.data;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.tterrag.registrate.providers.ProviderType;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import plus.dragons.createdragonsplus.client.ponder.CDPPonderPlugin;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.data.internal.CDPRecipeProvider;

public class CDPData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        bootstrap();
        ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
        FabricDataGenerator.Pack pack = generator.createPack();
        REGISTRATE.setExistingFileHelper(helper);
        REGISTRATE.setupDatagen(pack, helper);
        pack.addProvider((FabricDataOutput output) -> new CDPRecipeProvider(output));
    }

    private static void bootstrap() {
        boolean ponderPluginPresent = PonderIndex.streamPlugins()
                .anyMatch(plugin -> plugin.getModId().equals(CDPCommon.ID));
        if (!ponderPluginPresent) {
            PonderIndex.addPlugin(new CDPPonderPlugin());
        }
        REGISTRATE.registerBuiltinLocalization("interface")
                .registerBuiltinLocalization("tooltips")
                .registerForeignLocalization();
        REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> PonderIndex
                .getLangAccess()
                .provideLang(CDPCommon.ID, provider::add));
    }
}
