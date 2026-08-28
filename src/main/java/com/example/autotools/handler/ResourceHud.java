package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Map;

public class ResourceHud {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static void render(DrawContext ctx) {
        if (MC.player == null) return;

        int iron = count(Items.IRON_INGOT);
        int gold = count(Items.GOLD_INGOT);
        int diamond = count(Items.DIAMOND);
        int emerald = count(Items.EMERALD);

        TextRenderer tr = MC.textRenderer;
        int gap = 44;
        int total = gap * 4;
        int sw = ctx.getScaledWindowWidth();
        int x = (sw - total) / 2;
        int y = getYOffset();

        draw(ctx, tr, x, y, Items.IRON_INGOT, iron, 0xFFFFFF);
        draw(ctx, tr, x + gap, y, Items.GOLD_INGOT, gold, 0xFFD700);
        draw(ctx, tr, x + gap * 2, y, Items.DIAMOND, diamond, 0x00FFFF);
        draw(ctx, tr, x + gap * 3, y, Items.EMERALD, emerald, 0x50C878);
    }

    private static int getYOffset() {
        int y = 4;
        if (MC.inGameHud == null || MC.inGameHud.getBossBarHud() == null) return y;

        int bars = getBossBarCount();
        if (bars > 0) y += bars * 14 + 2;
        return y;
    }

    @SuppressWarnings("unchecked")
    private static int getBossBarCount() {
        try {
            Field field = net.minecraft.client.gui.hud.BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);
            Map<?, ?> map = (Map<?, ?>) field.get(MC.inGameHud.getBossBarHud());
            return map != null ? map.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void draw(DrawContext ctx, TextRenderer tr, int x, int y, net.minecraft.item.Item item, int count, int color) {
        ItemStack s = new ItemStack(item);
        ctx.drawItem(s, x, y);
        ctx.drawTextWithShadow(tr, Text.literal(String.valueOf(count)), x + 17, y + 4, color);
    }

    private static int count(net.minecraft.item.Item item) {
        if (MC.player == null) return 0;
        PlayerInventory inv = MC.player.getInventory();
        int sum = 0;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item)) sum += s.getCount();
        }
        return sum;
    }
}
