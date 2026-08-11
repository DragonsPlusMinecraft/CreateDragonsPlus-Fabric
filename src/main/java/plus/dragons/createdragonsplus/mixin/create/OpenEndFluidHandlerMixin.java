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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.common.fluids.pipe.ConsumingOpenPipeEffectHandler;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public abstract class OpenEndFluidHandlerMixin extends FluidTank {
    @Shadow
    @Final
    @Dynamic("Synthetic outer-class reference")
    OpenEndedPipe this$0;

    private OpenEndFluidHandlerMixin(long capacity) {
        super(capacity);
    }

    @Inject(method = "insert", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/OpenEndedPipe$OpenEndFluidHandler;getFluidAmount()J"))
    private void insert$applyConsumingEffect(
            FluidVariant resource, long maxAmount, TransactionContext transaction,
            CallbackInfoReturnable<Long> cir, @Local OpenPipeEffectHandler handler) {
        if (!(handler instanceof ConsumingOpenPipeEffectHandler consuming))
            return;
        FluidStack contained = getFluid().copy();
        TransactionCallback.onSuccess(transaction,
                () -> setFluid(ConsumingOpenPipeEffectHandler.getRemainder(consuming, this$0, contained)));
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/OpenEndedPipe;provideFluidToSpace(Lio/github/fabricators_of_create/porting_lib/fluids/FluidStack;Lnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext;)Z", ordinal = 1))
    private boolean insert$preventConsumingEffectPlacement(
            OpenEndedPipe pipe, FluidStack fluid, TransactionContext transaction,
            Operation<Boolean> original, @Local OpenPipeEffectHandler handler) {
        if (handler instanceof ConsumingOpenPipeEffectHandler)
            return false;
        return original.call(pipe, fluid, transaction);
    }
}
