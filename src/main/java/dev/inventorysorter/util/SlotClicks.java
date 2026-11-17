package dev.inventorysorter.util;

import dev.inventorysorter.mixininterface.ISlot;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public final class SlotClicks {
    private static final int MAIN_FIRST = 9;
    private static final int MAIN_LAST = 35;
    private static final int HOTBAR_FIRST = 0;
    private static final int HOTBAR_LAST = 8;

    private SlotClicks() {
    }

    public static void click(int slotId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, slotId, 0, ContainerInput.PICKUP, mc.player);
    }

    public static void transfer(int fromId, int toId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        boolean cursorWasEmpty = mc.player.containerMenu.getCarried().isEmpty();
        click(fromId);
        click(toId);
        if (cursorWasEmpty && !mc.player.containerMenu.getCarried().isEmpty()) {
            click(fromId);
        }
    }

    public static int findEmptyPlayerSlotId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return -1;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        for (int range = 0; range < 2; range++) {
            int first = range == 0 ? MAIN_FIRST : HOTBAR_FIRST;
            int last = range == 0 ? MAIN_LAST : HOTBAR_LAST;
            for (Slot slot : menu.slots) {
                if (!(slot.container instanceof Inventory)) {
                    continue;
                }
                int containerIndex = slot.getContainerSlot();
                if (containerIndex < first || containerIndex > last) {
                    continue;
                }
                if (slot.getItem().isEmpty()) {
                    return ((ISlot) slot).inventorySorter$getIndex();
                }
            }
        }
        return -1;
    }

    public static void dropCarried() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.gameMode != null && !mc.player.containerMenu.getCarried().isEmpty()) {
            mc.gameMode.handleContainerInput(
                mc.player.containerMenu.containerId,
                AbstractContainerMenu.SLOT_CLICKED_OUTSIDE,
                0,
                ContainerInput.PICKUP,
                mc.player
            );
        }
    }
}
