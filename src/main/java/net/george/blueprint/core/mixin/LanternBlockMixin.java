package net.george.blueprint.core.mixin;

import net.george.blueprint.common.block.wood.WoodPostBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LanternBlock.class)
public class LanternBlockMixin {
    @Inject(method = "canPlaceAt", at = @At("RETURN"), cancellable = true)
    public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.get(LanternBlock.HANGING) && world.getBlockState(pos.up()).getBlock() instanceof WoodPostBlock) {
            cir.setReturnValue(true);
        }
    }
}
