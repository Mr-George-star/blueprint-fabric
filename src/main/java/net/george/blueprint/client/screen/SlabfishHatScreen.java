package net.george.blueprint.client.screen;

import net.george.blueprint.client.RewardHandler;
import net.george.blueprint.core.api.config.ForgeConfigSpec;
import net.george.blueprint.core.util.NetworkUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Locale;

/**
 * The {@link Screen} responsible for the slabfish patreon settings.
 *
 * <p>For more information, visit the <a href="https://www.patreon.com/teamabnormals">Patreon</a>></p>
 *
 * @author Jackson
 */
public class SlabfishHatScreen extends Screen {
    public static final String SLABFISH_SCREEN_KEY = "blueprint.screen.slabfish_settings";
    private final Screen parent;

    public SlabfishHatScreen(Screen parent) {
        super(new TranslatableText(SLABFISH_SCREEN_KEY + ".title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int i = 0;

        for (RewardHandler.SlabfishSetting setting : RewardHandler.SlabfishSetting.values()) {
            ForgeConfigSpec.ConfigValue<Boolean> configValue = setting.getConfigValue();

            this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, this.getOptionName(setting, configValue.get()), (button) -> {
                boolean enabled = !configValue.get();
                configValue.set(enabled);
                button.setMessage(this.getOptionName(setting, enabled));
                NetworkUtil.updateSlabfish(RewardHandler.SlabfishSetting.getConfig());
            }, (button, stack, mouseX, mouseY) -> this.renderOrderedTooltip(stack, this.textRenderer.wrapLines(new TranslatableText("blueprint.config.slabfish_hat." + setting.name().toLowerCase(Locale.ROOT) + ".tooltip"), 200), mouseX, mouseY)));
            ++i;
        }

        if (i % 2 == 1) {
            ++i;
        }

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20, ScreenTexts.DONE, (button) -> this.getClient().setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        drawCenteredText(stack, this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void close() {
        this.getClient().setScreen(this.parent);
    }

    private Text getOptionName(RewardHandler.SlabfishSetting setting, boolean enabled) {
        return ScreenTexts.composeToggleText(new TranslatableText("blueprint.config.slabfish_hat." + setting.name().toLowerCase(Locale.ROOT)), enabled);
    }
}
