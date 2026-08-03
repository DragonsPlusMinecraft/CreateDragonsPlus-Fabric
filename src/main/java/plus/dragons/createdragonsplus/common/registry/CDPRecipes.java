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

import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;
import plus.dragons.createdragonsplus.common.recipe.RecipeTypeInfo;

public final class CDPRecipes {
    public static final RecipeTypeInfo<ColoringRecipe> COLORING = create("coloring",
            () -> new ProcessingRecipeSerializer<>(ColoringRecipe::new));
    public static final RecipeTypeInfo<FreezingRecipe> FREEZING = create("freezing",
            () -> new ProcessingRecipeSerializer<>(FreezingRecipe::new));
    public static final RecipeTypeInfo<SandingRecipe> SANDING = create("sanding",
            () -> new ProcessingRecipeSerializer<>(SandingRecipe::new));
    public static final RecipeTypeInfo<EndingRecipe> ENDING = create("ending",
            () -> new ProcessingRecipeSerializer<>(EndingRecipe::new));
    private static final List<RecipeTypeInfo<?>> ALL = List.of(COLORING, FREEZING, SANDING, ENDING);

    public static void register() {
        ALL.forEach(RecipeTypeInfo::register);
    }

    private static <R extends Recipe<?>> RecipeTypeInfo<R> create(
            String name, Supplier<? extends RecipeSerializer<R>> serializer) {
        return new RecipeTypeInfo<>(name, serializer);
    }

    private CDPRecipes() {}
}
