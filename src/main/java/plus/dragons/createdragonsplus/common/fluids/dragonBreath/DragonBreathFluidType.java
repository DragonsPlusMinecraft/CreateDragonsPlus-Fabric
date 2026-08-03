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

package plus.dragons.createdragonsplus.common.fluids.dragonBreath;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.sound.SoundActions;
import net.createmod.catnip.theme.Color;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.entity.EntityPersistentData;
import plus.dragons.createdragonsplus.common.fluids.CDPFluidUnits;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;
import plus.dragons.createdragonsplus.config.CDPConfig;

public final class DragonBreathFluidType extends SolidRenderFluidType {
    private DragonBreathFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            int tintColor, Vector3f fogColor) {
        super(properties, stillTexture, flowingTexture, tintColor, fogColor, DragonBreathFluidType::getVisibility);
    }

    public static DragonBreathFluidType create(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        Vector3f fogColor = new Color(0xDE9DC5, false).asVectorF();
        FluidType.Properties properties = FluidType.Properties.create()
                .descriptionId(Util.makeDescriptionId("fluid", CDPCommon.asResource("dragon_breath")))
                .rarity(Rarity.UNCOMMON)
                .density(3000)
                .viscosity(6000)
                .lightLevel(15)
                .motionScale(0.07)
                .canSwim(false)
                .canDrown(false)
                .pathType(BlockPathTypes.DAMAGE_OTHER)
                .adjacentPathType(null)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.DRAGON_FIREBALL_EXPLODE)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA);
        return new DragonBreathFluidType(properties, stillTexture, flowingTexture, 0xFFFFFFFF, fogColor);
    }

    private static float getVisibility() {
        return CDPConfig.client().dragonBreathVisionMultiplier.getF() / 256;
    }

    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        boolean falling = entity.getDeltaMovement().y <= 0.0;
        double y = entity.getY();
        entity.moveRelative(0.02F, movementVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        BlockPos entityPos = entity.blockPosition();
        double fluidSurface = entityPos.getY() + state.getHeight(entity.level(), entityPos);
        double immersedHeight = Math.max(0, fluidSurface - entity.getY());
        if (immersedHeight <= entity.getFluidJumpThreshold()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
            entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, falling, entity.getDeltaMovement()));
        } else {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5));
        }
        if (gravity != 0.0)
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, -gravity / 4.0, 0.0));
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (entity.horizontalCollision
                && entity.isFree(deltaMovement.x, deltaMovement.y + 0.6F - entity.getY() + y, deltaMovement.z))
            entity.setDeltaMovement(deltaMovement.x, 0.3F, deltaMovement.z);
        return true;
    }

    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x * 0.95F,
                movement.y + (movement.y < 0.06F ? 5.0E-4F : 0.0F), movement.z * 0.95F);
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return level.dimensionType().ultraWarm();
    }

    @Override
    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
        SoundEvent sound = getSound(player, level, pos, SoundActions.FLUID_VAPORIZE);
        level.playSound(player, pos, sound != null ? sound : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX(), pos.getY(), pos.getZ());
        cloud.setOwner(player);
        cloud.setParticle(ParticleTypes.DRAGON_BREATH);
        cloud.setRadius(CDPFluidUnits.dragonBreathCloudRadius(stack.getAmount()));
        cloud.setDuration(CDPFluidUnits.dragonBreathCloudDuration(stack.getAmount()));
        cloud.setRadiusPerTick(-0.01F);
        cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
        EntityPersistentData.get(cloud).putBoolean("DragonBreath", true);
        level.levelEvent(2006, pos, -1);
        level.addFreshEntity(cloud);
    }
}
