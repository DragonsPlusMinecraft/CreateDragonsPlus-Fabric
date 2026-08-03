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

package plus.dragons.createdragonsplus.common;

import com.simibubi.create.foundation.item.ItemDescription;
import io.github.fabricators_of_create.porting_lib.event.common.AddPackFindersEvent;
import net.createmod.catnip.lang.FontHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.resources.ResourceManager;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.common.fluids.dye.RegisterDyeVariantsEvent;
import plus.dragons.createdragonsplus.common.recipe.RecipeConverter;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.common.registry.CDPBlockFreezers;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPCauldrons;
import plus.dragons.createdragonsplus.common.registry.CDPConditions;
import plus.dragons.createdragonsplus.common.registry.CDPCreativeModeTabs;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItemAttributes;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPLoots;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.internal.CDPRuntimeRecipeProvider;
import plus.dragons.createdragonsplus.data.runtime.RuntimePackResources;
import plus.dragons.createdragonsplus.integration.CDPIntegrationContributions;
import plus.dragons.createdragonsplus.integration.arts_and_crafts.ArtsAndCraftsExtension;
import plus.dragons.createdragonsplus.integration.dye_depot.DyeDepotExtension;

public class CDPCommon implements ModInitializer {
    public static final String ID = "create_dragons_plus";
    public static final String NAME = "Create: Dragons Plus";
    public static final String PERSISTENT_DATA_KEY = "CreateDragonsPlusData";
    public static final CDPRegistrate REGISTRATE = new CDPRegistrate(ID)
            .setTooltipModifier(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    private static final IdentifiableResourceReloadListener RELOAD_LISTENER = new SimpleSynchronousResourceReloadListener() {
        @Override
        public ResourceLocation getFabricId() {
            return asResource("runtime_recipe_caches");
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            CDPFanProcessingTypes.COLORING.values().forEach(type -> type.get().recreateCache());
            CDPItemAttributes.recreateCache();
            RecipeConverter.invalidateCaches();
        }
    };

    static {
        REGISTRATE.addLang("pack", asResource("runtime"), NAME);
        REGISTRATE.addLang("pack", asResource("runtime"), "description", NAME + " Runtime Generated Resources");
    }

    @Override
    public void onInitialize() {
        CDPConfig.register();
        registerOptionalIntegrations();
        bootstrapDyeVariants();

        // Load every Registrate declaration before the single Fabric registration pass.
        CDPCauldrons.register();
        CDPFluids.register();
        CDPBlocks.register();
        CDPBlockEntities.register();
        CDPItems.register();
        CDPCriterions.register();
        CDPConditions.register();
        CDPFanProcessingTypes.register();
        CDPItemAttributes.register();
        REGISTRATE.register();

        // Registries referenced by these callbacks must already contain their entries.
        CDPRecipes.register();
        CDPCreativeModeTabs.register();
        CDPLoots.register();
        CDPCauldrons.initialize();
        CDPFluids.initialize();
        CDPBlockFreezers.register();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RELOAD_LISTENER);
        AddPackFindersEvent.EVENT.register(CDPCommon::addPackFinders);
        UpdateRecipesEvent.EVENT.register(CDPRuntimeRecipeProvider::buildRecipesForUpdate);
    }

    private static void registerOptionalIntegrations() {
        DyeDepotExtension.register();
        ArtsAndCraftsExtension.register();
    }

    private static void bootstrapDyeVariants() {
        if (DyeVariantRegistry.isFrozen())
            return;
        var builder = new DyeVariantRegistry.Builder();
        DyeColors.registerVanilla(builder);
        CDPIntegrationContributions.gatherDyeVariants(new RegisterDyeVariantsEvent(builder));
        DyeVariantRegistry.freeze(builder.build());
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA)
            return;
        var pack = new RuntimePackResources("runtime", PackType.SERVER_DATA, Position.TOP);
        pack.addDataProvider(new CDPRuntimeRecipeProvider(pack.getPackOutput()));
        event.addRepositorySource(pack);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(ID, path);
    }
}
