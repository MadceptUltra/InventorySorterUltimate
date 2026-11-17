package dev.inventorysorter.util;

import dev.inventorysorter.mixininterface.ISlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ContainerSorter {
    private static final int HOTBAR_FIRST = 0;
    private static final int HOTBAR_LAST = 8;
    private static final int MAIN_FIRST = 9;
    private static final int MAIN_LAST = 35;

    private final AbstractContainerScreen<?> screen;
    private final List<SortableSlot> slots = new ArrayList<>();
    private final List<SlotMove> moves = new ArrayList<>();

    private int nextMove;
    private int cooldown;
    private boolean failed;

    public ContainerSorter(AbstractContainerScreen<?> screen, Slot originSlot) {
        this.screen = screen;

        Section originSection = sectionOf(screen, originSlot);
        if (originSection == null) {
            failed = true;
            return;
        }

        for (Slot slot : screen.getMenu().slots) {
            if (sectionOf(screen, slot) == originSection) {
                slots.add(new SortableSlot(((ISlot) slot).inventorySorter$getIndex(), slot.getItem().copy()));
            }
        }
        slots.sort(Comparator.comparingInt(slot -> slot.slotId));

        planMoves();
    }

    public static boolean canSort(AbstractContainerScreen<?> screen, Slot slot) {
        Section section = sectionOf(screen, slot);
        return section == Section.PLAYER_INVENTORY || section == Section.CONTAINER;
    }

    public boolean isInvalid() {
        return failed;
    }

    public boolean tick(int delayTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (failed || mc.player == null || mc.gameMode == null || mc.gui.screen() != screen) {
            return true;
        }
        if (nextMove >= moves.size()) {
            return true;
        }
        if (cooldown < delayTicks) {
            cooldown++;
            return false;
        }
        cooldown = 0;

        SlotMove move = moves.get(nextMove++);
        SlotClicks.transfer(move.from(), move.to());
        return false;
    }

    private void planMoves() {
        mergePartialStacks();
        orderSlots();
    }

    private void mergePartialStacks() {
        List<List<SortableSlot>> groups = new ArrayList<>();
        for (SortableSlot slot : slots) {
            if (!isPartialStack(slot.stack)) {
                continue;
            }

            List<SortableSlot> group = null;
            for (List<SortableSlot> candidate : groups) {
                if (ItemStack.isSameItemSameComponents(slot.stack, candidate.get(0).stack)) {
                    group = candidate;
                    break;
                }
            }
            if (group == null) {
                group = new ArrayList<>();
                groups.add(group);
            }
            group.add(slot);
        }

        for (List<SortableSlot> group : groups) {
            mergeGroup(group);
        }
    }

    private void mergeGroup(List<SortableSlot> group) {
        int targetIndex = 0;
        for (int sourceIndex = 1; sourceIndex < group.size(); sourceIndex++) {
            SortableSlot source = group.get(sourceIndex);
            while (!source.stack.isEmpty() && targetIndex < sourceIndex) {
                SortableSlot target = group.get(targetIndex);
                if (target.stack.getCount() >= target.stack.getMaxStackSize()) {
                    targetIndex++;
                    continue;
                }

                int space = target.stack.getMaxStackSize() - target.stack.getCount();
                int amount = Math.min(space, source.stack.getCount());
                moves.add(new SlotMove(source.slotId, target.slotId));
                target.stack = target.stack.copyWithCount(target.stack.getCount() + amount);
                int left = source.stack.getCount() - amount;
                source.stack = left > 0 ? source.stack.copyWithCount(left) : ItemStack.EMPTY;
            }
        }
    }

    private void orderSlots() {
        for (int i = 0; i < slots.size(); i++) {
            int best = i;
            for (int j = i + 1; j < slots.size(); j++) {
                if (comesBefore(slots.get(j).stack, slots.get(best).stack)) {
                    best = j;
                }
            }

            if (best != i && !slots.get(best).stack.isEmpty()) {
                SortableSlot from = slots.get(best);
                SortableSlot to = slots.get(i);
                moves.add(new SlotMove(from.slotId, to.slotId));

                ItemStack swap = from.stack;
                from.stack = to.stack;
                to.stack = swap;
            }
        }
    }

    private static boolean isPartialStack(ItemStack stack) {
        return !stack.isEmpty() && stack.isStackable() && stack.getCount() < stack.getMaxStackSize();
    }

    private static boolean comesBefore(ItemStack candidate, ItemStack current) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (current.isEmpty()) {
            return true;
        }

        int order = BuiltInRegistries.ITEM.getKey(candidate.getItem())
            .compareTo(BuiltInRegistries.ITEM.getKey(current.getItem()));
        if (order != 0) {
            return order < 0;
        }
        if (candidate.getCount() != current.getCount()) {
            return candidate.getCount() > current.getCount();
        }
        return candidate.getDamageValue() > current.getDamageValue();
    }

    private static Section sectionOf(AbstractContainerScreen<?> screen, Slot slot) {
        if (slot.container instanceof Inventory) {
            if (screen instanceof CreativeModeInventoryScreen
                && ((ISlot) slot).inventorySorter$getIndex() <= HOTBAR_LAST) {
                return Section.IGNORED;
            }

            int containerIndex = ((ISlot) slot).inventorySorter$getSlot();
            if (containerIndex >= HOTBAR_FIRST && containerIndex <= HOTBAR_LAST) {
                return Section.HOTBAR;
            }
            if (containerIndex >= MAIN_FIRST && containerIndex <= MAIN_LAST) {
                return Section.PLAYER_INVENTORY;
            }
            return Section.IGNORED;
        }

        if ((screen instanceof ContainerScreen || screen instanceof ShulkerBoxScreen)
            && slot.container instanceof SimpleContainer) {
            return Section.CONTAINER;
        }
        return Section.IGNORED;
    }

    private enum Section {
        PLAYER_INVENTORY,
        CONTAINER,
        HOTBAR,
        IGNORED
    }

    private static final class SortableSlot {
        final int slotId;
        ItemStack stack;

        SortableSlot(int slotId, ItemStack stack) {
            this.slotId = slotId;
            this.stack = stack;
        }
    }

    private record SlotMove(int from, int to) {
    }
}
