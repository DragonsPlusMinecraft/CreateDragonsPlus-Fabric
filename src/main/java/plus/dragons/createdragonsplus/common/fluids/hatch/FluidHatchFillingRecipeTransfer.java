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

package plus.dragons.createdragonsplus.common.fluids.hatch;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FluidHatchFillingRecipeTransfer {
    public static boolean canItemBeFilled(Level level, ItemStack stack) {
        RecipeWrapper input = input(stack);
        if (SequencedAssemblyRecipe.getRecipe(level, input, AllRecipeTypes.FILLING.getType(), FillingRecipe.class).isPresent())
            return true;
        return AllRecipeTypes.FILLING.find(input, level).isPresent();
    }

    public static OptionalLong getRequiredAmountForItem(Level level, ItemStack stack, FluidStack availableFluid) {
        return findRecipe(level, stack, availableFluid)
                .map(FillingRecipe::getRequiredFluid)
                .map(ingredient -> ingredient.getRequiredAmount())
                .map(OptionalLong::of)
                .orElseGet(OptionalLong::empty);
    }

    public static Optional<ItemStack> fillItem(Level level, long requiredAmount, ItemStack stack, FluidStack availableFluid) {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);

        return findRecipe(level, stack, toFill)
                .map(recipe -> {
                    List<ItemStack> results = recipe.rollResults();
                    availableFluid.shrink(requiredAmount);
                    stack.shrink(1);
                    return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
                });
    }

    private static Optional<FillingRecipe> findRecipe(Level level, ItemStack stack, FluidStack availableFluid) {
        RecipeWrapper input = input(stack);
        net.minecraft.world.item.crafting.RecipeType<FillingRecipe> fillingType = AllRecipeTypes.FILLING.getType();
        var sequencedRecipe = SequencedAssemblyRecipe.getRecipe(level,
                input,
                fillingType,
                FillingRecipe.class,
                recipe -> recipe.matches(input, level) && recipe.getRequiredFluid().test(availableFluid));
        if (sequencedRecipe.isPresent())
            return sequencedRecipe;

        for (FillingRecipe recipe : level.getRecipeManager().getAllRecipesFor(fillingType)) {
            if (recipe.matches(input, level) && recipe.getRequiredFluid().test(availableFluid))
                return Optional.of(recipe);
        }
        return Optional.empty();
    }

    private static RecipeWrapper input(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, stack);
        return new RecipeWrapper(handler);
    }
}
