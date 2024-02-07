package net.george.blueprint.core.mixin.client;

import net.george.blueprint.client.screen.SlabfishHatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SkinOptionsScreen.class)
public class SkinOptionsScreenMixin extends GameOptionsScreen {
    public SkinOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @ModifyVariable(method = "init", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 1, shift = At.Shift.AFTER))
    public int init(int i) {
        MinecraftClient minecraft = this.getClient();
        ++i;
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, new TranslatableText(SlabfishHatScreen.SLABFISH_SCREEN_KEY), (button) -> minecraft.setScreen(new SlabfishHatScreen(this)), (button, stack, mouseX, mouseY) -> this.renderOrderedTooltip(stack, minecraft.textRenderer.wrapLines(new TranslatableText(SlabfishHatScreen.SLABFISH_SCREEN_KEY + ".tooltip", new LiteralText("patreon.com/teamabnormals").styled(style -> style.withColor(TextColor.parse("#FF424D")))), 200), mouseX, mouseY)));
        return i;
    }
}
