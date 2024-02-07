package net.george.blueprint.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.george.blueprint.common.command.argument.EnumArgument;
import net.george.blueprint.common.command.argument.ModIdArgument;
import net.george.blueprint.core.api.config.ConfigTracker;
import net.george.blueprint.core.api.config.ModConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.File;

@SuppressWarnings("unused")
public class ConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("config").then(ShowFile.register()));
    }

    public static class ShowFile {
        public ShowFile() {
        }

        static ArgumentBuilder<ServerCommandSource, ?> register() {
            return (CommandManager.literal("showfile").requires((commandSource) -> commandSource.hasPermissionLevel(0)))
                    .then(CommandManager.argument("mod", ModIdArgument.modIdArgument())
                            .then(CommandManager.argument("type", EnumArgument.enumArgument(ModConfig.Type.class))
                                    .executes(ShowFile::showFile)));
        }

        private static int showFile(CommandContext<ServerCommandSource> context) {
            String modId = context.getArgument("mod", String.class);
            ModConfig.Type type = context.getArgument("type", ModConfig.Type.class);
            String configFileName = ConfigTracker.INSTANCE.getConfigFileName(modId, type);
            if (configFileName != null) {
                File f = new File(configFileName);
                context.getSource().sendFeedback(new TranslatableText("commands.config.getwithtype", modId, type,
                        (new LiteralText(f.getName())).formatted(Formatting.UNDERLINE).styled((style) ->
                                style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, f.getAbsolutePath())))), true);
            } else {
                context.getSource().sendFeedback(new TranslatableText("commands.config.noconfig", modId, type), true);
            }

            return 0;
        }
    }
}
