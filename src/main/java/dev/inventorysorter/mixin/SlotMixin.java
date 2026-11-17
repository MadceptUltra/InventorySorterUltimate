package dev.inventorysorter.mixin;

import dev.inventorysorter.mixininterface.ISlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements ISlot {
    @Shadow
    public int index;

    @Shadow
    @Final
    private int slot;

    @Override
    public int inventorySorter$getIndex() {
        return index;
    }

    @Override
    public int inventorySorter$getSlot() {
        return slot;
    }
}
