package com.example.autotools.handler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class AutoToolHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static int originalSlot = -1;
    private static boolean active = false;
    private static BlockPos targetPos = null;

    public static void onStartBreak(BlockPos pos) {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;
        if (player.currentScreenHandler != player.playerScreenHandler) return;
        originalSlot = player.getInventory().selectedSlot;
        targetPos = pos;
        active = true;
        selectBestTool(player, player.getWorld().getBlockState(pos));
    }

    public static void tick(ClientPlayerEntity player) {
        if (!active) return;
        if (!MC.options.attackKey.isPressed() || isBlockBroken(player)) {
            restore(player);
        }
    }

    private static boolean isBlockBroken(ClientPlayerEntity player) {
        return targetPos == null || player.getWorld().getBlockState(targetPos).isAir();
    }

    private static void restore(ClientPlayerEntity player) {
        if (player.currentScreenHandler != player.playerScreenHandler) {
            active = false;
            targetPos = null;
            originalSlot = -1;
            return;
        }
        if (originalSlot >= 0 && originalSlot < PlayerInventory.getHotbarSize()) {
            player.getInventory().selectedSlot = originalSlot;
        }
        active = false;
        targetPos = null;
        originalSlot = -1;
    }

    private static void selectBestTool(ClientPlayerEntity player, BlockState state) {
        PlayerInventory inv = player.getInventory();
        int bestSlot = -1;
        float bestSpeed = -1f;
        ItemStack current = inv.getMainHandStack();
        float currentSpeed = current.getMiningSpeedMultiplier(state);

        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SwordItem) continue;

            float speed = stack.getMiningSpeedMultiplier(state);

            if (state.isIn(BlockTags.WOOL) && stack.getItem() instanceof ShearsItem
                    && !(current.getItem() instanceof ShearsItem)) {
                bestSlot = i;
                break;
            }

            if (state.isOf(Blocks.COBWEB) && stack.getItem() instanceof ShearsItem
                    && !(current.getItem() instanceof ShearsItem)) {
                bestSlot = i;
                break;
            }

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && bestSlot != inv.selectedSlot) {
            if (currentSpeed <= 1f || bestSpeed > currentSpeed) {
                inv.selectedSlot = bestSlot;
            }
        }
    }
}
