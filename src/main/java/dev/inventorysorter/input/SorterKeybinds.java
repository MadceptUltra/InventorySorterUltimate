package dev.inventorysorter.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.inventorysorter.InventorySorterMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class SorterKeybinds {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "general")
    );

    public static final KeyMapping SORT_KEY = new KeyMapping(
        "key.inventory-sorter-ultimate.sort",
        InputConstants.Type.MOUSE,
        InputConstants.MOUSE_BUTTON_MIDDLE,
        CATEGORY
    );

    private SorterKeybinds() {
    }

    public static KeyMapping[] appendTo(KeyMapping[] existing) {
        for (KeyMapping mapping : existing) {
            if (mapping == SORT_KEY) return existing;
        }

        KeyMapping[] updated = new KeyMapping[existing.length + 1];
        System.arraycopy(existing, 0, updated, 0, existing.length);
        updated[existing.length] = SORT_KEY;
        return updated;
    }
}
