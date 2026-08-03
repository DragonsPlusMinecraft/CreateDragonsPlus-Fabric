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

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider.IntrinsicImpl;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry;
import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry.InteractionInformation;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.fluids.CDPFluidUnits;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;
import plus.dragons.createdragonsplus.common.fluids.TypedFlowableFluid;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonBreathFluidType;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragondBreathLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonsBreathOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidOpenPipeEffect;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidType;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariant;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders;
import plus.dragons.createdragonsplus.data.tag.IntrinsicTagRegistry;

public final class CDPFluids {
    public static final ModTags MOD_TAGS = new ModTags();
    public static final CommonTags COMMON_TAGS = new CommonTags();
    public static final Map<ResourceLocation, DyeFluidType> DYE_TYPES = new LinkedHashMap<>();
    public static final Map<ResourceLocation, FluidEntry<TypedFlowableFluid.Flowing>> DYES_BY_VARIANT = new LinkedHashMap<>();

    private static final ResourceLocation DRAGON_BREATH_STILL = REGISTRATE.asResource("fluid/dragon_breath_still");
    private static final ResourceLocation DRAGON_BREATH_FLOW = REGISTRATE.asResource("fluid/dragon_breath_flow");
    public static final DragonBreathFluidType DRAGON_BREATH_TYPE = DragonBreathFluidType.create(
            DRAGON_BREATH_STILL, DRAGON_BREATH_FLOW);

    static {
        for (var variant : DyeVariantRegistry.all())
            DYES_BY_VARIANT.put(variant.id(), dye(variant));
    }

    public static final FluidEntry<TypedFlowableFluid.Flowing> DRAGON_BREATH = REGISTRATE
            .fluid("dragon_breath", DRAGON_BREATH_STILL, DRAGON_BREATH_FLOW,
                    properties -> new TypedFlowableFluid.Flowing(properties, DRAGON_BREATH_TYPE))
            .lang("Dragon's Breath")
            .fluidAttributes(() -> DRAGON_BREATH_TYPE)
            .fluidProperties(properties -> properties
                    .blastResistance(100F)
                    .levelDecreasePerBlock(2)
                    .flowSpeed(2)
                    .tickRate(30))
            .source(properties -> new TypedFlowableFluid.Source(properties, DRAGON_BREATH_TYPE))
            .tag(COMMON_TAGS.dragonBreath, MOD_TAGS.fanEndingCatalysts)
            .block(DragondBreathLiquidBlock::new)
            .properties(properties -> properties.lightLevel(state -> 15))
            .lang("Dragon's Breath")
            .build()
            .bucket()
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .lang("Dragon's Breath Bucket")
            .tag(CDPItems.COMMON_TAGS.dragonBreathBuckets)
            .build()
            .setData(ProviderType.RECIPE, (context, provider) -> {
                CreateRecipeBuilders.emptying(context.getId().withPath("dragon_breath"))
                        .require(Items.DRAGON_BREATH)
                        .output(context.get(), CDPFluidUnits.QUARTER_BUCKET)
                        .output(Items.GLASS_BOTTLE)
                        .withCondition(CDPConfig.features().dragonBreathFluid)
                        .build(provider);
                CreateRecipeBuilders.filling(context.getId().withPath("dragon_breath"))
                        .require(context.get(), CDPFluidUnits.QUARTER_BUCKET)
                        .require(Items.GLASS_BOTTLE)
                        .output(Items.DRAGON_BREATH)
                        .withCondition(CDPConfig.features().dragonBreathFluid)
                        .build(provider);
            })
            .register();

    public static void register() {
        REGISTRATE.registerFluidTags(MOD_TAGS);
        REGISTRATE.registerFluidTags(COMMON_TAGS);
        DYE_TYPES.forEach((variantId, type) -> Registry.register(
                PortingLibFluids.FLUID_TYPES,
                CDPCommon.asResource(DyeVariantRegistry.get(variantId).orElseThrow().fluidName()),
                type));
        Registry.register(PortingLibFluids.FLUID_TYPES, CDPCommon.asResource("dragon_breath"), DRAGON_BREATH_TYPE);
    }

    public static void initialize() {
        Reactions.registerFluidInteractions();
        Reactions.registerOpenPipeEffects();
        registerDispenserBehavior();
    }

    private static void registerDispenserBehavior() {
        DYES_BY_VARIANT.values().forEach(entry -> DispenserBlock.registerBehavior(entry.getBucket().get(), StandardDispenserBehaviour.INSTANCE));
        DispenserBlock.registerBehavior(DRAGON_BREATH.getBucket().get(), StandardDispenserBehaviour.INSTANCE);
    }

    private static FluidEntry<TypedFlowableFluid.Flowing> dye(DyeVariant variant) {
        ResourceLocation stillTexture = REGISTRATE.asResource("fluid/dye_still");
        ResourceLocation flowingTexture = REGISTRATE.asResource("fluid/dye_flow");
        String name = variant.fluidName();
        TagKey<Fluid> tag = COMMON_TAGS.dyesByVariant.get(variant.id());
        DyeFluidType type = DyeFluidType.create(variant, stillTexture, flowingTexture);
        DYE_TYPES.put(variant.id(), type);
        return REGISTRATE
                .fluid(name, stillTexture, flowingTexture,
                        properties -> new TypedFlowableFluid.Flowing(properties, type))
                .fluidAttributes(() -> type)
                .fluidProperties(properties -> properties.blastResistance(100))
                .block((fluid, properties) -> new DyeLiquidBlock(variant, fluid, properties))
                .build()
                .source(properties -> new TypedFlowableFluid.Source(properties, type))
                .bucket()
                .transform(builder -> tagDyeBucket(builder, variant))
                .model((context, provider) -> provider.withExistingParent(context.getName(), provider.modLoc("dye_bucket")))
                .build()
                .transform(builder -> tagDyeFluid(builder, variant, tag))
                .setData(ProviderType.RECIPE, (context, provider) -> {
                    var fromItem = CreateRecipeBuilders.mixing(context.getId().withPath(name + "_from_item"))
                            .require(variant.dyeItemTag())
                            .require(Fluids.WATER, CDPFluidUnits.QUARTER_BUCKET)
                            .output(context.get(), CDPFluidUnits.QUARTER_BUCKET)
                            .withCondition(CDPConfig.features().dyeFluids);
                    var fromFluid = CreateRecipeBuilders.mixing(context.getId().withPath(name + "_from_fluid"))
                            .require(context.get(), CDPFluidUnits.QUARTER_BUCKET)
                            .output(variant.dyeItemId())
                            .requiresHeat(HeatCondition.HEATED)
                            .withCondition(CDPConfig.features().dyeFluids);
                    if (variant.requiredModId() != null) {
                        var condition = DefaultResourceConditions.allModsLoaded(variant.requiredModId());
                        fromItem.withCondition(condition);
                        fromFluid.withCondition(condition);
                    }
                    fromItem.build(provider);
                    fromFluid.build(provider);
                })
                .register();
    }

    private static <I extends BucketItem, P> ItemBuilder<I, P> tagDyeBucket(
            ItemBuilder<I, P> builder, DyeVariant variant) {
        var tag = CDPItems.COMMON_TAGS.dyeBucketsByVariant.get(variant.id());
        if (variant.requiredModId() == null)
            return builder.tag(tag);
        CDPItems.COMMON_TAGS.addOptional(tag, CDPCommon.asResource(variant.fluidName() + "_bucket"));
        return builder;
    }

    private static <T extends SimpleFlowableFluid, P> FluidBuilder<T, P> tagDyeFluid(
            FluidBuilder<T, P> builder, DyeVariant variant, TagKey<Fluid> tag) {
        if (variant.requiredModId() == null)
            return builder.tag(tag, variant.coloringCatalystFluidTag());
        COMMON_TAGS.addOptional(tag, CDPCommon.asResource(variant.fluidName()));
        COMMON_TAGS.addOptional(tag, CDPCommon.asResource("flowing_" + variant.fluidName()));
        MOD_TAGS.addOptional(variant.coloringCatalystFluidTag(), CDPCommon.asResource(variant.fluidName()));
        MOD_TAGS.addOptional(variant.coloringCatalystFluidTag(),
                CDPCommon.asResource("flowing_" + variant.fluidName()));
        return builder;
    }

    public static class ModTags extends IntrinsicTagRegistry<Fluid, IntrinsicImpl<Fluid>> {
        public final TagKey<Fluid> fanEndingCatalysts = tag("fan_processing_catalysts/ending", "Bulk Ending Catalysts");

        public ModTags() {
            super(CDPCommon.ID, Registries.FLUID);
        }

        @Override
        public void generate(IntrinsicImpl<Fluid> provider) {
            provider.addTag(fanEndingCatalysts);
            DyeVariantRegistry.all().forEach(variant -> provider.addTag(variant.coloringCatalystFluidTag()));
        }
    }

    public static class CommonTags extends IntrinsicTagRegistry<Fluid, IntrinsicImpl<Fluid>> {
        public final TagKey<Fluid> dyes = tag("dyes", "Dyes");
        public final Map<ResourceLocation, TagKey<Fluid>> dyesByVariant = new LinkedHashMap<>();
        public final TagKey<Fluid> dragonBreath = tag("dragon_breath", "Dragon's Breath");

        protected CommonTags() {
            super("c", Registries.FLUID);
            for (var variant : DyeVariantRegistry.all()) {
                var tag = tag("dyes/" + variant.serializedName(), variant.displayName() + " Dye");
                dyesByVariant.put(variant.id(), tag);
                if (variant.requiredModId() == null)
                    addTag(dyes, tag);
                else
                    addOptionalTag(dyes, tag.location());
            }
        }
    }

    public static final class Reactions {
        private static final Map<FluidType, BlockState> LAVA_INTERACTIONS = new HashMap<>();
        private static final Map<FluidType, BlockState> DYE_LAVA_INTERACTIONS = new HashMap<>();

        private static void onPipeCollisionFlow(PipeCollisionEvent.Flow event) {
            FluidType first = event.getFirstFluid().getFluidType();
            FluidType second = event.getSecondFluid().getFluidType();
            if (first == PortingLibFluids.LAVA_TYPE && LAVA_INTERACTIONS.containsKey(second))
                event.setState(LAVA_INTERACTIONS.get(second));
            else if (second == PortingLibFluids.LAVA_TYPE && LAVA_INTERACTIONS.containsKey(first))
                event.setState(LAVA_INTERACTIONS.get(first));
        }

        private static void onPipeCollisionSpill(PipeCollisionEvent.Spill event) {
            Fluid world = event.getWorldFluid();
            Fluid pipe = event.getPipeFluid();
            FluidType worldType = world.getFluidType();
            FluidType pipeType = pipe.getFluidType();
            if (worldType == PortingLibFluids.LAVA_TYPE)
                setSpillResult(event, world, pipeType);
            else if (pipeType == PortingLibFluids.LAVA_TYPE)
                setSpillResult(event, pipe, worldType);
        }

        private static void setSpillResult(PipeCollisionEvent.Spill event, Fluid lava, FluidType other) {
            if (DYE_LAVA_INTERACTIONS.containsKey(other))
                event.setState(DYE_LAVA_INTERACTIONS.get(other));
            else if (LAVA_INTERACTIONS.containsKey(other))
                event.setState(lava.isSource(lava.defaultFluidState())
                        ? Blocks.OBSIDIAN.defaultBlockState()
                        : LAVA_INTERACTIONS.get(other));
        }

        public static @Nullable BlockState getDyeLavaInteraction(FluidType type) {
            return DYE_LAVA_INTERACTIONS.get(type);
        }

        private static void registerFluidInteractions() {
            boolean generateConcrete = CDPConfig.common().features.dyeFluidsLavaInteractionGenerateColoredConcrete.get();
            DYES_BY_VARIANT.forEach((id, entry) -> {
                var variant = DyeVariantRegistry.get(id).orElseThrow();
                var type = DYE_TYPES.get(id);
                var block = BuiltInRegistries.BLOCK.get(variant.concreteBlockId());
                var result = generateConcrete && block != Blocks.AIR
                        ? block.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState();
                LAVA_INTERACTIONS.put(type, result);
                DYE_LAVA_INTERACTIONS.put(type, result);
                FluidInteractionRegistry.addInteraction(PortingLibFluids.LAVA_TYPE,
                        new InteractionInformation(type, result));
            });
            LAVA_INTERACTIONS.put(DRAGON_BREATH_TYPE, Blocks.END_STONE.defaultBlockState());
            FluidInteractionRegistry.addInteraction(PortingLibFluids.LAVA_TYPE, new InteractionInformation(
                    DRAGON_BREATH_TYPE,
                    state -> state.isSource()
                            ? Blocks.OBSIDIAN.defaultBlockState()
                            : Blocks.END_STONE.defaultBlockState()));

            PipeCollisionEvent.FLOW.register(Reactions::onPipeCollisionFlow);
            PipeCollisionEvent.SPILL.register(Reactions::onPipeCollisionSpill);
            FluidPlaceBlockCallback.EVENT.register(Reactions::whenFluidsMeet);
        }

        private static @Nullable BlockState whenFluidsMeet(
                LevelAccessor level, net.minecraft.core.BlockPos pos, BlockState blockState) {
            FluidState lava = blockState.getFluidState();
            if (!lava.is(FluidTags.LAVA))
                return null;
            for (Direction direction : Direction.values()) {
                FluidState adjacent = level.getFluidState(pos.relative(direction));
                FluidType adjacentType = adjacent.getFluidType();
                if (DYE_LAVA_INTERACTIONS.containsKey(adjacentType))
                    return DYE_LAVA_INTERACTIONS.get(adjacentType);
                if (adjacentType == DRAGON_BREATH_TYPE)
                    return lava.isSource()
                            ? Blocks.OBSIDIAN.defaultBlockState()
                            : Blocks.END_STONE.defaultBlockState();
            }
            return null;
        }

        private static void registerOpenPipeEffects() {
            DYES_BY_VARIANT.forEach((id, entry) -> DyeVariantRegistry.get(id)
                    .ifPresent(variant -> OpenPipeEffectHandler.REGISTRY.register(
                            entry.getSource(), new DyeFluidOpenPipeEffect(variant))));
            OpenPipeEffectHandler.REGISTRY.register(DRAGON_BREATH.getSource(), new DragonsBreathOpenPipeEffect());
        }

        private Reactions() {}
    }

    private CDPFluids() {}
}
