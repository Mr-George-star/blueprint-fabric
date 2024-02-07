package net.george.blueprint.core.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface NoteBlockPlayCallback {
    Event<NoteBlockPlayCallback> EVENT = EventFactory.createArrayBacked(NoteBlockPlayCallback.class,
            (listeners) -> (world, pos, state, note, instrument) -> {
                for (NoteBlockPlayCallback callback : listeners) {
                    callback.interact(world, pos, state, note, instrument);
                }
            });

    void interact(World world, BlockPos pos, BlockState state, int note, Instrument instrument);
}
