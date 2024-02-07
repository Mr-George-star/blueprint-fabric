package net.george.blueprint.core.mixin.client;

import net.george.blueprint.core.events.AnimateFluidTickEvent;
import net.george.blueprint.core.events.AnimateTickEvent;
import net.george.blueprint.core.mixin.invoker.FluidInvoker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Redirect(method = "randomBlockDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    public void blockRandomBlockDisplayTick(Block instance, BlockState state, World world, BlockPos pos, Random random) {
        if (!AnimateTickEvent.onAnimateTick(state, world, pos, random)) {
            instance.randomDisplayTick(state, world, pos, random);
        }
    }

    @Redirect(method = "randomBlockDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;randomDisplayTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    public void fluidRandomBlockDisplayTick(FluidState instance, World world, BlockPos pos, Random random) {
        if (!AnimateFluidTickEvent.onAnimateFluidTick(world, pos, instance, random)) {
            ((FluidInvoker)instance.getFluid()).randomDisplayTick(world, pos, instance, random);
        }
    }
}
