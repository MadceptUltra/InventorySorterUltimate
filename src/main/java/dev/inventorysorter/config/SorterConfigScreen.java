package dev.inventorysorter.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.inventorysorter.input.SorterKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class SorterConfigScreen extends Screen {
    private static final int MAX_DELAY_SLIDER = 20;

    private final Screen parent;
    private boolean listeningForKey = false;
    private Button keybindButton;
    private Button resetKeyButton;

    public SorterConfigScreen(Screen parent) {
        super(Component.translatable("config.inventory-sorter-ultimate.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        SorterConfig config = SorterConfig.get();
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int rowWidth = 220;
        int leftX = centerX - rowWidth / 2;

        // 1. Sorting Enabled toggle
        addRenderableWidget(
            Button.builder(getEnabledLabel(config.enabled), button -> {
                config.enabled = !config.enabled;
                button.setMessage(getEnabledLabel(config.enabled));
                SorterConfig.save();
            })
            .pos(leftX, startY)
            .size(rowWidth, 20)
            .tooltip(Tooltip.create(Component.translatable("config.inventory-sorter-ultimate.enabled.tooltip")))
            .build()
        );

        // 2. Sorting Delay slider (0 - 20 ticks)
        double initialSliderValue = Mth.clamp(config.sortingDelay / (double) MAX_DELAY_SLIDER, 0.0, 1.0);
        AbstractSliderButton delaySlider = new AbstractSliderButton(
            leftX,
            startY + 26,
            rowWidth,
            20,
            getDelayLabel(config.sortingDelay),
            initialSliderValue
        ) {
            @Override
            protected void updateMessage() {
                setMessage(getDelayLabel(SorterConfig.get().sortingDelay));
            }

            @Override
            protected void applyValue() {
                SorterConfig.get().sortingDelay = Mth.clamp((int) Math.round(this.value * MAX_DELAY_SLIDER), 0, MAX_DELAY_SLIDER);
                SorterConfig.save();
            }
        };
        delaySlider.setTooltip(Tooltip.create(Component.translatable("config.inventory-sorter-ultimate.delay.tooltip")));
        addRenderableWidget(delaySlider);

        // 3. Disable in Creative toggle
        addRenderableWidget(
            Button.builder(getCreativeLabel(config.disableInCreative), button -> {
                config.disableInCreative = !config.disableInCreative;
                button.setMessage(getCreativeLabel(config.disableInCreative));
                SorterConfig.save();
            })
            .pos(leftX, startY + 52)
            .size(rowWidth, 20)
            .tooltip(Tooltip.create(Component.translatable("config.inventory-sorter-ultimate.disable_creative.tooltip")))
            .build()
        );

        // 4. Sort Keybind button + Reset button
        keybindButton = Button.builder(getKeybindLabel(), _ -> {
            listeningForKey = true;
            updateKeybindButtons();
        })
        .pos(leftX, startY + 78)
        .size(rowWidth - 54, 20)
        .tooltip(Tooltip.create(Component.translatable("config.inventory-sorter-ultimate.sort_key.tooltip")))
        .build();
        addRenderableWidget(keybindButton);

        resetKeyButton = Button.builder(Component.translatable("controls.reset"), _ -> {
            listeningForKey = false;
            applySortKey(SorterKeybinds.SORT_KEY.getDefaultKey());
        })
        .pos(leftX + rowWidth - 50, startY + 78)
        .size(50, 20)
        .build();
        addRenderableWidget(resetKeyButton);
        updateKeybindButtons();

        // Done button
        addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, _ -> onClose())
                .pos(centerX - 100, startY + 118)
                .size(200, 20)
                .build()
        );
    }

    private void applySortKey(InputConstants.Key key) {
        SorterKeybinds.SORT_KEY.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
        updateKeybindButtons();
    }

    private void updateKeybindButtons() {
        if (keybindButton != null) {
            keybindButton.setMessage(getKeybindLabel());
        }
        if (resetKeyButton != null) {
            resetKeyButton.active = !SorterKeybinds.SORT_KEY.isDefault();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (listeningForKey) {
            listeningForKey = false;
            applySortKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listeningForKey) {
            listeningForKey = false;
            if (event.key() == InputConstants.KEY_ESCAPE) {
                applySortKey(InputConstants.UNKNOWN);
            } else {
                applySortKey(InputConstants.getKey(event));
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        if (this.font != null) {
            int titleX = (this.width - this.font.width(this.title)) / 2;
            graphics.text(this.font, this.title, titleX, 20, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void onClose() {
        SorterConfig.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private static Component getEnabledLabel(boolean enabled) {
        return CommonComponents.optionStatus(Component.translatable("config.inventory-sorter-ultimate.enabled"), enabled);
    }

    private static Component getDelayLabel(int delay) {
        return Component.translatable("config.inventory-sorter-ultimate.delay", delay);
    }

    private static Component getCreativeLabel(boolean disableInCreative) {
        return CommonComponents.optionStatus(Component.translatable("config.inventory-sorter-ultimate.disable_creative"), disableInCreative);
    }

    private Component getKeybindLabel() {
        Component keyName = SorterKeybinds.SORT_KEY.getTranslatedKeyMessage();
        if (listeningForKey) {
            keyName = Component.literal("> ")
                .append(keyName.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                .append(" <")
                .withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("config.inventory-sorter-ultimate.sort_key", keyName);
    }
}
