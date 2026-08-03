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

package plus.dragons.createdragonsplus.common.fluids;

import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;

/** Associates Registrate's Fabric flowable fluids with a Porting Lib FluidType. */
public final class TypedFlowableFluid {
    public static class Flowing extends SimpleFlowableFluid.Flowing {
        private final FluidType fluidType;

        public Flowing(Properties properties, FluidType fluidType) {
            super(properties);
            this.fluidType = fluidType;
        }

        @Override
        public FluidType getFluidType() {
            return fluidType;
        }
    }

    public static class Source extends SimpleFlowableFluid.Source {
        private final FluidType fluidType;

        public Source(Properties properties, FluidType fluidType) {
            super(properties);
            this.fluidType = fluidType;
        }

        @Override
        public FluidType getFluidType() {
            return fluidType;
        }
    }

    private TypedFlowableFluid() {}
}
