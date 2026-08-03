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

package plus.dragons.createdragonsplus.common.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.entity.EntityPersistentData;
import plus.dragons.createdragonsplus.common.fluids.CDPFluidUnits;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.common.recipe.RecipeTypeInfo;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.common.registry.CDPCauldrons;
import plus.dragons.createdragonsplus.common.registry.CDPCreativeModeTabs;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItemAttributes;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.common.registry.CDPRecipes;
import plus.dragons.createdragonsplus.integration.ModIntegration;

public final class CDPGameTests implements FabricGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20)
    public static void fluidUnitsAndTransactions(GameTestHelper helper) {
        assertEquals(FluidConstants.BUCKET, CDPFluidUnits.BUCKET, "bucket unit");
        assertEquals(FluidConstants.BOTTLE, CDPFluidUnits.BOTTLE, "bottle unit");
        assertEquals(FluidConstants.BUCKET / 4, CDPFluidUnits.QUARTER_BUCKET, "quarter-bucket unit");
        assertEquals(2.0F, CDPFluidUnits.dragonBreathCloudRadius(CDPFluidUnits.BUCKET),
                "dragon breath cloud radius per bucket");
        assertEquals(200, CDPFluidUnits.dragonBreathCloudDuration(CDPFluidUnits.BUCKET),
                "dragon breath cloud duration per bucket");

        ConfigurableFluidTank tank = new ConfigurableFluidTank(CDPFluidUnits.BUCKET, fluid -> {});
        FluidVariant water = FluidVariant.of(Fluids.WATER);

        try (Transaction simulation = Transaction.openOuter()) {
            assertEquals(CDPFluidUnits.BUCKET, tank.insert(water, CDPFluidUnits.BUCKET, simulation),
                    "simulated insertion");
        }
        assertEquals(0L, tank.getFluid().getAmount(), "simulation must roll back");

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(CDPFluidUnits.BUCKET,
                    tank.insert(water, CDPFluidUnits.BUCKET * 2, transaction),
                    "insertion must stop at capacity");
            transaction.commit();
        }
        assertEquals(CDPFluidUnits.BUCKET, tank.getFluid().getAmount(), "committed insertion");

        CompoundTag serialized = tank.writeToNBT(new CompoundTag());
        ConfigurableFluidTank restored = new ConfigurableFluidTank(CDPFluidUnits.BUCKET, fluid -> {});
        restored.readFromNBT(serialized);
        assertEquals(water, restored.getFluid().getType(), "serialized fluid variant");
        assertEquals(CDPFluidUnits.BUCKET, restored.getFluid().getAmount(), "serialized fluid amount");

        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction nested = outer.openNested()) {
                assertEquals(CDPFluidUnits.QUARTER_BUCKET,
                        tank.extract(water, CDPFluidUnits.QUARTER_BUCKET, nested),
                        "nested simulated extraction");
            }
            assertEquals(CDPFluidUnits.BUCKET, tank.getFluid().getAmount(), "nested rollback");
            outer.commit();
        }
        assertEquals(CDPFluidUnits.BUCKET, tank.getFluid().getAmount(), "outer commit after nested rollback");

        tank.forbidExtraction();
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0L, tank.extract(water, CDPFluidUnits.BUCKET, transaction), "filtered extraction");
            assertEquals(CDPFluidUnits.BUCKET,
                    tank.extract(water, CDPFluidUnits.BUCKET, transaction, true),
                    "forced extraction");
            transaction.commit();
        }
        assertEquals(0L, tank.getFluid().getAmount(), "committed forced extraction");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20)
    public static void dragonBreathVaporizationUnits(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        CDPFluids.DRAGON_BREATH_TYPE.onVaporize(null, helper.getLevel(), pos,
                new FluidStack(FluidVariant.of(CDPFluids.DRAGON_BREATH.getSource()), CDPFluidUnits.BUCKET));

        var clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class, new AABB(pos).inflate(1.0D));
        assertEquals(1, clouds.size(), "dragon breath vaporization cloud count");
        AreaEffectCloud cloud = clouds.get(0);
        assertEquals(2.0F, cloud.getRadius(), "dragon breath vaporization cloud radius");
        assertEquals(200, cloud.getDuration(), "dragon breath vaporization cloud duration");
        assertEquals(true, EntityPersistentData.get(cloud).getBoolean("DragonBreath"),
                "dragon breath vaporization persistent marker");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    public static void fluidHatchBucketRoundTrip(GameTestHelper helper) {
        BlockPos tankPos = new BlockPos(1, 1, 1);
        BlockPos hatchPos = new BlockPos(2, 1, 1);
        helper.setBlock(tankPos, AllBlocks.FLUID_TANK.getDefaultState());
        helper.setBlock(hatchPos,
                CDPBlocks.FLUID_HATCH.getDefaultState().setValue(CDPBlocks.FLUID_HATCH.get().FACING, Direction.WEST));

        helper.runAfterDelay(2, () -> {
            var level = helper.getLevel();
            BlockPos absoluteTankPos = helper.absolutePos(tankPos);
            BlockPos absoluteHatchPos = helper.absolutePos(hatchPos);
            var tankBlockEntity = level.getBlockEntity(absoluteTankPos);
            Storage<FluidVariant> tank = FluidStorage.SIDED.find(
                    level, absoluteTankPos, null, tankBlockEntity, Direction.EAST);
            if (tank == null) {
                throw new GameTestAssertException("Create fluid tank did not expose Fabric fluid storage");
            }

            FluidVariant water = FluidVariant.of(Fluids.WATER);
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(CDPFluidUnits.BUCKET,
                        tank.insert(water, CDPFluidUnits.BUCKET, transaction),
                        "fluid tank setup insertion");
                transaction.commit();
            }

            var player = helper.makeMockSurvivalPlayer();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(absoluteHatchPos), Direction.EAST, absoluteHatchPos, false);
            var hatchState = level.getBlockState(absoluteHatchPos);
            var fillResult = hatchState.use(level, player, InteractionHand.MAIN_HAND, hit);
            if (!fillResult.consumesAction()) {
                throw new GameTestAssertException("Fluid Hatch rejected tank-to-bucket transfer");
            }
            assertEquals(Items.WATER_BUCKET, player.getMainHandItem().getItem(), "filled bucket result");
            assertEquals(0L, storedAmount(tank, water), "tank after filling bucket");

            var emptyResult = hatchState.use(level, player, InteractionHand.MAIN_HAND, hit);
            if (!emptyResult.consumesAction()) {
                throw new GameTestAssertException("Fluid Hatch rejected bucket-to-tank transfer");
            }
            assertEquals(Items.BUCKET, player.getMainHandItem().getItem(), "emptied bucket result");
            assertEquals(CDPFluidUnits.BUCKET, storedAmount(tank, water), "tank after emptying bucket");
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20)
    public static void registryIds(GameTestHelper helper) {
        assertRegistryId(BuiltInRegistries.BLOCK, CDPBlocks.FLUID_HATCH.get(), "fluid_hatch");
        assertRegistryId(BuiltInRegistries.BLOCK, CDPCauldrons.DRAGON_BREATH_CAULDRON.get(),
                "dragon_breath_cauldron");
        assertRegistryId(BuiltInRegistries.ITEM, CDPBlocks.FLUID_HATCH.asItem(), "fluid_hatch");
        assertRegistryId(BuiltInRegistries.BLOCK_ENTITY_TYPE, CDPBlockEntities.FLUID_HATCH.get(), "fluid_hatch");
        assertRegistryId(BuiltInRegistries.ITEM, CDPItems.RARE_BLAZE_PACKAGE.get(), "rare_blaze_package");
        assertRegistryId(BuiltInRegistries.ITEM, CDPItems.RARE_MARBLE_GATE_PACKAGE.get(), "rare_marble_gate_package");
        assertRegistryId(BuiltInRegistries.ITEM, CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get(),
                "blaze_upgrade_smithing_template");
        assertRegistryId(BuiltInRegistries.CREATIVE_MODE_TAB, CDPCreativeModeTabs.base(), "base");

        assertRegistryId(BuiltInRegistries.FLUID, CDPFluids.DRAGON_BREATH.getSource(), "dragon_breath");
        assertRegistryId(BuiltInRegistries.FLUID, CDPFluids.DRAGON_BREATH.get(), "flowing_dragon_breath");
        assertEquals(CDPCommon.asResource("dragon_breath"),
                PortingLibFluids.FLUID_TYPES.getKey(CDPFluids.DRAGON_BREATH_TYPE),
                "dragon breath fluid type ID");
        assertEquals("fluid.create_dragons_plus.dragon_breath", CDPFluids.DRAGON_BREATH_TYPE.getDescriptionId(),
                "dragon breath fluid translation key");
        assertRegistryContains(BuiltInRegistries.BLOCK, "dragon_breath");
        assertRegistryContains(BuiltInRegistries.ITEM, "dragon_breath_bucket");

        for (var variant : DyeVariantRegistry.all()) {
            var entry = CDPFluids.DYES_BY_VARIANT.get(variant.id());
            String fluidName = variant.fluidName();
            assertRegistryId(BuiltInRegistries.FLUID, entry.getSource(), fluidName);
            assertRegistryId(BuiltInRegistries.FLUID, entry.get(), "flowing_" + fluidName);
            assertRegistryContains(BuiltInRegistries.BLOCK, fluidName);
            assertRegistryContains(BuiltInRegistries.ITEM, fluidName + "_bucket");
            assertEquals(CDPCommon.asResource(fluidName),
                    PortingLibFluids.FLUID_TYPES.getKey(CDPFluids.DYE_TYPES.get(variant.id())),
                    fluidName + " fluid type ID");
            assertEquals("fluid.create_dragons_plus." + fluidName,
                    CDPFluids.DYE_TYPES.get(variant.id()).getDescriptionId(), fluidName + " fluid translation key");
            assertEquals(CDPCommon.asResource(variant.fanProcessingName()),
                    CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(
                            CDPFanProcessingTypes.COLORING.get(variant.id()).get()),
                    variant.fanProcessingName() + " fan processing ID");
        }

        assertEquals(CDPCommon.asResource("freezing"),
                CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(CDPFanProcessingTypes.FREEZING.get()),
                "freezing fan processing ID");
        assertEquals(CDPCommon.asResource("sanding"),
                CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(CDPFanProcessingTypes.SANDING.get()),
                "sanding fan processing ID");
        assertEquals(CDPCommon.asResource("ending"),
                CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(CDPFanProcessingTypes.ENDING.get()),
                "ending fan processing ID");

        assertEquals(CDPCommon.asResource("freezable"),
                CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.getKey(CDPItemAttributes.FREEZABLE.value()),
                "freezable item attribute ID");
        assertEquals(CDPCommon.asResource("sandable"),
                CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.getKey(CDPItemAttributes.SANDABLE.value()),
                "sandable item attribute ID");
        assertEquals(CDPCommon.asResource("endable"),
                CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.getKey(CDPItemAttributes.ENDABLE.value()),
                "endable item attribute ID");
        assertEquals(CDPCommon.asResource("stainable"),
                CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.getKey(CDPItemAttributes.STAINABLE.value()),
                "stainable item attribute ID");
        assertEquals(CDPCommon.asResource("stat"), CDPCriterions.STAT.getId(), "criterion trigger ID");
        for (RecipeTypeInfo<?> recipe : List.of(
                CDPRecipes.COLORING, CDPRecipes.FREEZING, CDPRecipes.SANDING, CDPRecipes.ENDING)) {
            assertEquals(recipe.getId(), BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()),
                    recipe.getId() + " recipe type ID");
            assertEquals(recipe.getId(), BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer()),
                    recipe.getId() + " recipe serializer ID");
        }
        assertOptionalRegistration(ModIntegration.DYE_DEPOT, "dye_depot_amber_dye");
        assertOptionalRegistration(ModIntegration.ARTS_AND_CRAFTS, "arts_and_crafts_bleached_dye");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20)
    public static void fanProcessingAvailableAfterReload(GameTestHelper helper) {
        assertFanProcessingAvailable(helper);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = "cdp_reload", timeoutTicks = 200)
    public static void recipesSurviveExplicitReload(GameTestHelper helper) {
        long runtimePolishingBefore = countRuntimePolishingRecipes(helper);
        if (runtimePolishingBefore == 0) {
            throw new GameTestAssertException("No CDP runtime Sandpaper Polishing recipes were loaded before reload");
        }

        var server = helper.getLevel().getServer();
        var selectedPacks = List.copyOf(server.getPackRepository().getSelectedIds());
        server.reloadResources(selectedPacks).whenComplete((ignored, error) -> server.execute(() -> {
            if (error != null) {
                helper.fail("Explicit resource reload failed: " + error);
                return;
            }
            try {
                assertEquals(runtimePolishingBefore, countRuntimePolishingRecipes(helper),
                        "CDP runtime polishing recipe count after reload");
                assertFanProcessingAvailable(helper);
                helper.succeed();
            } catch (Throwable throwable) {
                helper.fail("Post-reload assertions failed: " + throwable.getMessage());
            }
        }));
    }

    private static void assertFanProcessingAvailable(GameTestHelper helper) {
        assertRecipeTypeNotEmpty(helper, CDPRecipes.FREEZING, "freezing");
        assertRecipeTypeNotEmpty(helper, CDPRecipes.ENDING, "ending");

        var level = helper.getLevel();
        var redColoring = CDPFanProcessingTypes.COLORING.get(new ResourceLocation("minecraft", "red")).get();
        if (!redColoring.canProcess(new ItemStack(Blocks.WHITE_WOOL), level)) {
            throw new GameTestAssertException("Bulk Coloring did not discover the vanilla white-to-red wool recipe");
        }
        if (!CDPFanProcessingTypes.SANDING.get().canProcess(AllItems.ROSE_QUARTZ.asStack(), level)) {
            throw new GameTestAssertException("Bulk Sanding did not discover Create's rose quartz polishing recipe");
        }
    }

    private static long countRuntimePolishingRecipes(GameTestHelper helper) {
        return helper.getLevel().getRecipeManager()
                .getAllRecipesFor(AllRecipeTypes.SANDPAPER_POLISHING.getType()).stream()
                .filter(recipe -> recipe.getId().getNamespace().equals(CDPCommon.ID))
                .count();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void assertRecipeTypeNotEmpty(
            GameTestHelper helper, RecipeTypeInfo<?> recipeType, String displayName) {
        RecipeType<?> type = recipeType.getType();
        if (helper.getLevel().getRecipeManager().getAllRecipesFor((RecipeType) type).isEmpty()) {
            throw new GameTestAssertException("No " + displayName + " recipes were available after reload");
        }
    }

    private static <T> void assertRegistryId(Registry<T> registry, T value, String path) {
        assertEquals(CDPCommon.asResource(path), registry.getKey(value), path + " registry ID");
    }

    private static <T> void assertRegistryContains(Registry<T> registry, String path) {
        var id = CDPCommon.asResource(path);
        if (!registry.containsKey(id)) {
            throw new GameTestAssertException(path + " is missing from registry " + registry.key().location());
        }
        assertEquals(id, registry.getKey(registry.get(id)), path + " registry ID");
    }

    private static long storedAmount(Storage<FluidVariant> storage, FluidVariant variant) {
        long amount = 0;
        for (var view : storage.nonEmptyViews()) {
            if (view.getResource().equals(variant))
                amount += view.getAmount();
        }
        return amount;
    }

    private static void assertOptionalRegistration(ModIntegration integration, String fluidPath) {
        var fluidId = CDPCommon.asResource(fluidPath);
        var bucketId = CDPCommon.asResource(fluidPath + "_bucket");
        assertEquals(integration.enabled(), BuiltInRegistries.FLUID.containsKey(fluidId),
                integration.id() + " fluid registration state");
        assertEquals(integration.enabled(), BuiltInRegistries.ITEM.containsKey(bucketId),
                integration.id() + " bucket registration state");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) {
            throw new GameTestAssertException(description + ": expected " + expected + ", got " + actual);
        }
    }
}
