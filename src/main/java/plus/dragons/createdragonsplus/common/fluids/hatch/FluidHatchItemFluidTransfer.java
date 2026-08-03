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

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class FluidHatchItemFluidTransfer {
    static TransferResult tryDrainItemToTank(
            ItemStack stack, Storage<FluidVariant> tank, FilteringBehaviour filter) {
        ItemFluidStorage item = getItemFluidStorage(stack, false);
        if (item == null)
            return TransferResult.EMPTY;

        for (var view : item.storage().nonEmptyViews()) {
            FluidVariant variant = view.getResource();
            if (variant.isBlank() || !filter.test(new FluidStack(variant, view.getAmount())))
                continue;
            try (Transaction transaction = Transaction.openOuter()) {
                long moved = StorageUtil.move(
                        item.storage(), tank, variant::equals, view.getAmount(), transaction);
                if (moved <= 0)
                    continue;
                transaction.commit();
                stack.shrink(1);
                return new TransferResult(new FluidStack(variant, moved), getResult(item.context()));
            }
        }
        return TransferResult.EMPTY;
    }

    static TransferResult tryFillItemFromTank(
            ItemStack stack, Storage<FluidVariant> tank, FilteringBehaviour filter) {
        ItemFluidStorage item = getItemFluidStorage(stack, true);
        if (item == null)
            return TransferResult.EMPTY;

        for (var view : tank.nonEmptyViews()) {
            FluidVariant variant = view.getResource();
            if (variant.isBlank() || !filter.test(new FluidStack(variant, view.getAmount())))
                continue;
            try (Transaction transaction = Transaction.openOuter()) {
                long moved = StorageUtil.move(
                        tank, item.storage(), variant::equals, view.getAmount(), transaction);
                if (moved <= 0)
                    continue;
                transaction.commit();
                stack.shrink(1);
                return new TransferResult(new FluidStack(variant, moved), getResult(item.context()));
            }
        }
        return TransferResult.EMPTY;
    }

    static boolean canItemBeFilled(ItemStack stack) {
        ItemFluidStorage item = getItemFluidStorage(stack, true);
        return item != null && item.storage().supportsInsertion();
    }

    static boolean canItemBeEmptied(ItemStack stack) {
        ItemFluidStorage item = getItemFluidStorage(stack, false);
        return item != null && item.storage().supportsExtraction()
                && item.storage().nonEmptyIterator().hasNext();
    }

    private static @Nullable ItemFluidStorage getItemFluidStorage(ItemStack stack, boolean forFilling) {
        ItemStack split = stack.copy();
        split.setCount(1);
        MutableContainerItemContext context = new MutableContainerItemContext(split);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage == null)
            return null;
        if (forFilling && !GenericItemFilling.isFluidHandlerValid(split, storage))
            return null;
        return new ItemFluidStorage(context, storage);
    }

    private static ItemStack getResult(MutableContainerItemContext context) {
        return context.getItemVariant().toStack(TransferUtil.truncateLong(context.getAmount()));
    }

    private record ItemFluidStorage(
            MutableContainerItemContext context, Storage<FluidVariant> storage) {}

    record TransferResult(FluidStack fluidStack, ItemStack result) {
        public static final TransferResult EMPTY = new TransferResult(FluidStack.EMPTY, ItemStack.EMPTY);

        public boolean isEmpty() {
            return fluidStack.isEmpty();
        }
    }

    private FluidHatchItemFluidTransfer() {}
}
