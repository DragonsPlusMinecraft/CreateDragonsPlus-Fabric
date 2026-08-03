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

package plus.dragons.createdragonsplus.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tterrag.registrate.fabric.RegistryObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeBuilder<R extends Recipe<?>, B extends BaseRecipeBuilder<R, ?>> implements Consumer<Consumer<FinishedRecipe>> {
    protected final @Nullable String directory;
    protected final List<ConditionJsonProvider> conditions = new ArrayList<>();
    protected @Nullable ResourceLocation id;

    protected BaseRecipeBuilder(@Nullable String directory) {
        this.directory = directory;
    }

    protected abstract B builder();

    public abstract FinishedRecipe build();

    public @Nullable String getDirectory() {
        return directory;
    }

    public @Nullable ResourceLocation getId() {
        return id;
    }

    public List<ConditionJsonProvider> getConditions() {
        return List.copyOf(conditions);
    }

    protected final ResourceLocation outputId() {
        if (id == null)
            throw new IllegalStateException("Recipe id has not been set");
        return directory == null ? id : new ResourceLocation(id.getNamespace(), directory + "/" + id.getPath());
    }

    @Override
    public final void accept(Consumer<FinishedRecipe> output) {
        FinishedRecipe recipe = build();
        if (conditions.isEmpty()) {
            output.accept(recipe);
            return;
        }

        output.accept(new FinishedRecipe() {
            private void addConditions(JsonObject json) {
                JsonArray array = new JsonArray();
                conditions.stream().map(ConditionJsonProvider::toJson).forEach(array::add);
                json.add(ResourceConditions.CONDITIONS_KEY, array);
            }

            @Override
            public void serializeRecipeData(JsonObject json) {
                recipe.serializeRecipeData(json);
                addConditions(json);
            }

            @Override
            public ResourceLocation getId() {
                return recipe.getId();
            }

            @Override
            public net.minecraft.world.item.crafting.RecipeSerializer<?> getType() {
                return recipe.getType();
            }

            @Override
            public @Nullable JsonObject serializeAdvancement() {
                JsonObject advancement = recipe.serializeAdvancement();
                if (advancement != null)
                    addConditions(advancement);
                return advancement;
            }

            @Override
            public @Nullable ResourceLocation getAdvancementId() {
                return recipe.getAdvancementId();
            }
        });
    }

    public B withId(ResourceLocation id) {
        this.id = id;
        return builder();
    }

    public B withCondition(ConditionJsonProvider condition) {
        conditions.add(condition);
        return builder();
    }

    public final B withoutCondition(ConditionJsonProvider condition) {
        conditions.add(DefaultResourceConditions.not(condition));
        return builder();
    }

    public final B withAllCondition(ConditionJsonProvider... conditions) {
        Collections.addAll(this.conditions, conditions);
        return builder();
    }

    public final B withAnyCondition(ConditionJsonProvider... conditions) {
        this.conditions.add(DefaultResourceConditions.or(conditions));
        return builder();
    }

    public final B withMod(String mod) {
        return withCondition(DefaultResourceConditions.allModsLoaded(mod));
    }

    public final B withoutMod(String mod) {
        return withoutCondition(DefaultResourceConditions.allModsLoaded(mod));
    }

    public final B withItem(ResourceLocation location) {
        return withCondition(DefaultResourceConditions.registryContains(Registries.ITEM, location));
    }

    public final B withItem(RegistryObject<? extends Item> item) {
        return withItem(item.getId());
    }

    public final B withoutItem(ResourceLocation location) {
        return withoutCondition(DefaultResourceConditions.registryContains(Registries.ITEM, location));
    }

    public final B withoutItem(RegistryObject<? extends Item> item) {
        return withoutItem(item.getId());
    }

    public final B withTag(ResourceLocation location) {
        return withCondition(DefaultResourceConditions.tagsPopulated(TagKey.create(Registries.ITEM, location)));
    }

    public final B withTag(TagKey<Item> tag) {
        return withTag(tag.location());
    }

    public final B withoutTag(ResourceLocation location) {
        return withoutCondition(DefaultResourceConditions.tagsPopulated(TagKey.create(Registries.ITEM, location)));
    }

    public final B withoutTag(TagKey<Item> tag) {
        return withoutTag(tag.location());
    }
}
