package dev.inventorysorter.mixin;

import dev.inventorysorter.InventorySorterMod;
import dev.inventorysorter.input.SorterKeybinds;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void inventorySorter$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (SorterKeybinds.SORT_KEY.matchesMouse(event)) {
            if (InventorySorterMod.trySort((AbstractContainerScreen<?>) (Object) this)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void inventorySorter$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (getFocused() instanceof EditBox editBox && editBox.canConsumeInput()) {
            return;
        }

        if (SorterKeybinds.SORT_KEY.matches(event)) {
            if (InventorySorterMod.trySort((AbstractContainerScreen<?>) (Object) this)) {
                cir.setReturnValue(true);
            }
        }
    }
}
