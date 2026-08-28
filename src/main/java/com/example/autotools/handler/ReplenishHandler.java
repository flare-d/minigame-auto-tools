package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ReplenishHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static int lastSlot = -1;
    private static Item lastItem = null;
    private static long lastClick = 0;
    private static final long COOLDOWN = 100;

    public static void tick(ClientPlayerEntity player) {
        if (MC.interactionManager == null) return;
        if (player.currentScreenHandler != player.playerScreenHandler) return;

        int slot = player.getInventory().selectedSlot;
        ItemStack current = player.getInventory().getStack(slot);

        if (slot != lastSlot) {
            lastSlot = slot;
            lastItem = current.isEmpty() ? null : current.getItem();
            return;
        }

        if (current.isEmpty() && lastItem != null) {
            if (System.currentTimeMillis() - lastClick < COOLDOWN) return;

            int found = findSlot(player, lastItem, slot);
            if (found != -1) {
                int syncId = player.playerScreenHandler.syncId;
                int src = toScreen(found);
                int dst = toScreen(slot);
                MC.interactionManager.clickSlot(syncId, src, 0, SlotActionType.PICKUP, player);
                MC.interactionManager.clickSlot(syncId, dst, 0, SlotActionType.PICKUP, player);
                lastClick = System.currentTimeMillis();
            }
            lastItem = null;
        } else if (!current.isEmpty()) {
            lastItem = current.getItem();
        }
    }

    private static int findSlot(ClientPlayerEntity player, Item item, int exclude) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (i == exclude) continue;
            if (inv.getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private static int toScreen(int invSlot) {
        return (invSlot >= 0 && invSlot < 9) ? 36 + invSlot : invSlot;
    }
}
