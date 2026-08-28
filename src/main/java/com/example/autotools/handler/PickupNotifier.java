package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class PickupNotifier {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final List<Note> notes = new ArrayList<>();
    private static final Map<Item, Integer> prev = new HashMap<>();
    private static int tick = 0;

    public static void tick(ClientPlayerEntity player) {
        if (++tick % 5 != 0) return;

        PlayerInventory inv = player.getInventory();
        Map<Item, Integer> cur = new HashMap<>();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty()) cur.merge(s.getItem(), s.getCount(), Integer::sum);
        }

        for (Map.Entry<Item, Integer> e : cur.entrySet()) {
            int diff = e.getValue() - prev.getOrDefault(e.getKey(), 0);
            if (diff > 0) add(e.getKey(), diff);
        }

        prev.clear();
        prev.putAll(cur);
        notes.removeIf(n -> System.currentTimeMillis() - n.time > 3000);
    }

    private static void add(Item item, int count) {
        for (Note n : notes) {
            if (n.item == item) {
                n.count += count;
                n.time = System.currentTimeMillis();
                return;
            }
        }
        notes.add(new Note(item, count));
    }

    public static void render(DrawContext ctx) {
        if (notes.isEmpty()) return;
        TextRenderer tr = MC.textRenderer;
        int y = 20;
        int h = 16;

        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            ItemStack stack = new ItemStack(n.item);
            String label = stack.getName().getString() + " +" + n.count;
            int tw = tr.getWidth(label);
            int x = 4;
            int py = y + i * h;

            ctx.drawItem(stack, x, py);
            ctx.drawTextWithShadow(tr, Text.literal(label), x + 18, py + 4, 0xFFFFFF);
        }
    }

    private static class Note {
        Item item;
        int count;
        long time;
        Note(Item item, int count) { this.item = item; this.count = count; this.time = System.currentTimeMillis(); }
    }
}
