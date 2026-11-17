package dev.inventorysorter;

import dev.inventorysorter.config.SorterConfig;
import dev.inventorysorter.mixin.AbstractContainerScreenAccessor;
import dev.inventorysorter.util.ContainerSorter;
import dev.inventorysorter.util.SlotClicks;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InventorySorterMod implements ClientModInitializer {
    public static final String MOD_ID = "inventory-sorter-ultimate";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private static ContainerSorter activeSorter;

    @Override
    public void onInitializeClient() {
        SorterConfig.load();
    }

    public static boolean trySort(AbstractContainerScreen<?> screen) {
        SorterConfig config = SorterConfig.get();
        Minecraft mc = Minecraft.getInstance();

        if (!config.enabled || activeSorter != null || mc.player == null || mc.gameMode == null) {
            return false;
        }
        if (mc.player.isCreative() && config.disableInCreative) {
            return false;
        }

        Slot hoveredSlot = ((AbstractContainerScreenAccessor) screen).inventorySorter$getHoveredSlot();
        if (hoveredSlot == null || !ContainerSorter.canSort(screen, hoveredSlot)) {
            return false;
        }

        if (!mc.player.containerMenu.getCarried().isEmpty()) {
            int emptySlotId = SlotClicks.findEmptyPlayerSlotId();
            if (emptySlotId != -1) {
                SlotClicks.click(emptySlotId);
            } else {
                SlotClicks.dropCarried();
            }
        }

        ContainerSorter sorter = new ContainerSorter(screen, hoveredSlot);
        if (sorter.isInvalid()) {
            return false;
        }

        activeSorter = sorter;
        return true;
    }

    public static void onTick() {
        if (activeSorter != null && activeSorter.tick(SorterConfig.get().sortingDelay)) {
            activeSorter = null;
        }
    }

    public static void cancelSort() {
        activeSorter = null;
    }
}
