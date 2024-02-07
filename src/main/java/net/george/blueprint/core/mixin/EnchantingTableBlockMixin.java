package net.george.blueprint.core.mixin;

import net.george.blueprint.core.other.tags.BlueprintBlockTags;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @Inject(method = "canAccessBookshelf", at = @At("HEAD"), cancellable = true)
    private static void canAccessBookshelf(World world, BlockPos tablePos, BlockPos bookshelfOffset, CallbackInfoReturnable<Boolean> cir) {
        boolean checkOthers = world.getBlockState(tablePos.add(bookshelfOffset)).isIn(BlueprintBlockTags.BOOKSHELVES) && world.isAir(tablePos.add(bookshelfOffset.getX() / 2, bookshelfOffset.getY(), bookshelfOffset.getZ() / 2));
        if (checkOthers) {
            cir.setReturnValue(true);
        }
    }
}
