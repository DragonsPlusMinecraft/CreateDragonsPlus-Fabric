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

package plus.dragons.createdragonsplus.mixin.create;

import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.common.fluids.CDPFluidUnits;

@Mixin(value = FluidFillingBehaviour.class, remap = false)
public abstract class FluidFillingBehaviourMixin extends FluidManipulationBehaviour {
    private FluidFillingBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @ModifyVariable(method = "tryDeposit", at = @At(value = "STORE", ordinal = 0), name = "evaporate")
    private boolean tryDeposite$isVaporizedOnPlacement(
            boolean vaporize, Fluid fluid, BlockPos root, TransactionContext transaction) {
        var fluidStack = new FluidStack(fluid, CDPFluidUnits.BUCKET);
        return vaporize || fluid.getFluidType().isVaporizedOnPlacement(getWorld(), root, fluidStack);
    }

    @Inject(method = "tryDeposit", at = @At(value = "INVOKE", target = "Lio/github/fabricators_of_create/porting_lib/transfer/callbacks/TransactionCallback;onSuccess(Lnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext;Ljava/lang/Runnable;)Lio/github/fabricators_of_create/porting_lib/transfer/callbacks/TransactionSuccessCallback;"), cancellable = true)
    private void tryDeposit$onVaporize(
            Fluid fluid, BlockPos root, TransactionContext transaction,
            CallbackInfoReturnable<Boolean> cir) {
        var fluidStack = new FluidStack(fluid, CDPFluidUnits.BUCKET);
        var type = fluid.getFluidType();
        if (!type.isVaporizedOnPlacement(getWorld(), root, fluidStack))
            return;
        TransactionCallback.onSuccess(transaction,
                () -> type.onVaporize(null, getWorld(), root, fluidStack.copy()));
        cir.setReturnValue(true);
    }
}
