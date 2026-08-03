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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class CDPFluidUnits {
    public static final long BUCKET = FluidConstants.BUCKET;
    public static final long BOTTLE = FluidConstants.BOTTLE;
    /** Forge baseline recipes used 250 mB, i.e. one quarter bucket. */
    public static final long QUARTER_BUCKET = FluidConstants.BUCKET / 4;
    private static final float DRAGON_BREATH_CLOUD_RADIUS_PER_BUCKET = 2.0F;
    private static final int DRAGON_BREATH_CLOUD_DURATION_PER_BUCKET = 200;

    public static float dragonBreathCloudRadius(long amount) {
        return (float) (Math.max(0L, amount) * (double) DRAGON_BREATH_CLOUD_RADIUS_PER_BUCKET / BUCKET);
    }

    public static int dragonBreathCloudDuration(long amount) {
        double duration = Math.max(0L, amount) * (double) DRAGON_BREATH_CLOUD_DURATION_PER_BUCKET / BUCKET;
        return (int) Math.min(Integer.MAX_VALUE, duration);
    }

    private CDPFluidUnits() {}
}
