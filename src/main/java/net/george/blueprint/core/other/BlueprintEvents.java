package net.george.blueprint.core.other;

import net.george.blueprint.core.events.NoteBlockPlayCallback;
import net.george.blueprint.core.util.DataUtil;
import net.george.blueprint.core.util.NetworkUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * @author abigailfails
 */
public final class BlueprintEvents {
    public static final String NOTE_KEY = "minecraft:note";
    public static List<DataUtil.CustomNoteBlockInstrument> SORTED_CUSTOM_NOTE_BLOCK_INSTRUMENTS = null;

    public static void registerEvents() {
        onNoteBlockPlay();
    }

    private static void onNoteBlockPlay() {
        NoteBlockPlayCallback.EVENT.register((world, pos, state, note, iinstrument) -> {
            if (SORTED_CUSTOM_NOTE_BLOCK_INSTRUMENTS != null) {
                if (!world.isClient) {
                    BlockPointer source = new BlockPointerImpl((ServerWorld) world, pos.offset(Direction.DOWN));
                    for (DataUtil.CustomNoteBlockInstrument instrument : SORTED_CUSTOM_NOTE_BLOCK_INSTRUMENTS) {
                        if (instrument.test(source)) {
                            SoundEvent sound = instrument.getSound();
                            world.playSound(null, pos, sound, SoundCategory.RECORDS, 3.0F, (float) Math.pow(2.0D, (note - 12) / 12.0D));
                            NetworkUtil.spawnParticle(NOTE_KEY, (ServerWorld)world, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, note / 24.0D, 0.0D, 0.0D);
                            break;
                        }
                    }
                }
            }
        });
    }
}
