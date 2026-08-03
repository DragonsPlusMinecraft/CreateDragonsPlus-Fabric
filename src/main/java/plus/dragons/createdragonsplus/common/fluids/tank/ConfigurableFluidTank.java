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

package plus.dragons.createdragonsplus.common.fluids.tank;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ConfigurableFluidTank extends SmartFluidTank {
    protected Predicate<FluidStack> insertion = Predicates.alwaysTrue();
    protected Predicate<FluidStack> extraction = Predicates.alwaysTrue();

    public ConfigurableFluidTank(long capacity, Consumer<FluidStack> updateCallback) {
        super(capacity, updateCallback);
    }

    public ConfigurableFluidTank allowInsertion() {
        this.insertion = Predicates.alwaysTrue();
        return this;
    }

    public ConfigurableFluidTank allowInsertion(Predicate<FluidStack> inputPredicate) {
        this.insertion = inputPredicate;
        return this;
    }

    public ConfigurableFluidTank forbidInsertion() {
        this.insertion = Predicates.alwaysFalse();
        return this;
    }

    public ConfigurableFluidTank allowExtraction() {
        this.extraction = Predicates.alwaysTrue();
        return this;
    }

    public ConfigurableFluidTank allowExtration(Predicate<FluidStack> contentPredicate) {
        this.extraction = contentPredicate;
        return this;
    }

    public ConfigurableFluidTank forbidExtraction() {
        this.extraction = Predicates.alwaysFalse();
        return this;
    }

    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction, boolean forced) {
        return forced ? super.insert(resource, maxAmount, transaction) : insert(resource, maxAmount, transaction);
    }

    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction, boolean forced) {
        return forced ? super.extract(resource, maxAmount, transaction) : extract(resource, maxAmount, transaction);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!resource.isBlank() && insertion.test(new FluidStack(resource, maxAmount)))
            return super.insert(resource, maxAmount, transaction);
        return 0L;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        FluidStack stored = getFluid();
        if (!stored.isEmpty() && extraction.test(stored))
            return super.extract(resource, maxAmount, transaction);
        return 0L;
    }
}
