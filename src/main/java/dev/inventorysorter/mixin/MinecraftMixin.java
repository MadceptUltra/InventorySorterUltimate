package dev.inventorysorter.mixin;

import dev.inventorysorter.InventorySorterMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void inventorySorter$onPreTick(CallbackInfo ci) {
        InventorySorterMod.onTick();
    }
}
