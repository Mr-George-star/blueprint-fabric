package net.george.blueprint.core.mixin;

import net.george.blueprint.core.events.NoteBlockPlayCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    @Shadow @Final
    public static IntProperty NOTE;
    @Shadow @Final
    public static EnumProperty<Instrument> INSTRUMENT;

    @Inject(method = "onSyncedBlockEvent", at = @At("HEAD"))
    public void onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
        NoteBlockPlayCallback.EVENT.invoker().interact(world, pos, state, state.get(NOTE), state.get(INSTRUMENT));
    }
}
