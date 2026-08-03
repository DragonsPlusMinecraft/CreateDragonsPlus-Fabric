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

package plus.dragons.createdragonsplus.client;

/**
 * Development-run handshake between the optional JEI plugin and the client
 * smoke-test lifecycle. It is inert in normal launches.
 */
public final class JeiSmokeTestStatus {
    private static volatile boolean categoriesRegistered;
    private static volatile boolean catalystsRegistered;
    private static volatile int dynamicMixingRecipes;

    private JeiSmokeTestStatus() {}

    public static void categoriesRegistered(boolean allFourCategoriesPresent) {
        categoriesRegistered = allFourCategoriesPresent;
    }

    public static void catalystsRegistered() {
        catalystsRegistered = true;
    }

    public static void recipesRegistered(int dynamicMixingRecipeCount) {
        dynamicMixingRecipes = dynamicMixingRecipeCount;
    }

    public static boolean isReady() {
        return categoriesRegistered && catalystsRegistered && dynamicMixingRecipes > 0;
    }

    public static String summary() {
        return "categories=" + categoriesRegistered + ", catalysts=" + catalystsRegistered
                + ", dynamicMixingRecipes=" + dynamicMixingRecipes;
    }
}
