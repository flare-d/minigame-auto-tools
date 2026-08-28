package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WaterDropHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastWater = 0;
    private static final long COOLDOWN = 1000;

    public static void tick(ClientPlayerEntity player) {
        if (MC.interactionManager == null) return;
        if (player.currentScreenHandler != player.playerScreenHandler) return;
        if (player.isOnGround()) return;
        if (player.fallDistance < 3.0f) return;
        if (System.currentTimeMillis() - lastWater < COOLDOWN) return;
        if (BridgeHandler.placedThisTick) return;

        int waterSlot = findWater(player);
        if (waterSlot == -1) return;

        BlockPos ground = findGround(player);
        if (ground == null) return;

        double dist = player.getY() - (ground.getY() + 1);
        if (dist < 1.5 || dist > 4.0) return;

        int oldSlot = player.getInventory().selectedSlot;
        moveToHand(player, waterSlot);

        BlockPos place = player.getBlockPos().down();
        if (!player.getWorld().getBlockState(place).isAir()) {
            place = ground.up();
        }
        if (!player.getWorld().getBlockState(place).isAir()) {
            restoreSlot(player, waterSlot, oldSlot);
            return;
        }

        BlockPos support = place.down();
        if (player.getWorld().getBlockState(support).isAir()) {
            support = ground;
        }

        Vec3d hitVec = Vec3d.ofCenter(support).add(0, 0.9, 0);
        BlockHitResult bhr = new BlockHitResult(hitVec, Direction.UP, support, false);

        MC.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
        lastWater = System.currentTimeMillis();

        restoreSlot(player, waterSlot, oldSlot);
    }

    private static int findWater(ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (inv.getStack(i).isOf(Items.WATER_BUCKET)) return i;
        }
        return -1;
    }

    private static BlockPos findGround(ClientPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        for (int i = 1; i < 30; i++) {
            BlockPos d = pos.down(i);
            if (!player.getWorld().getBlockState(d).isAir()) return d;
        }
        return null;
    }

    private static void moveToHand(ClientPlayerEntity player, int slot) {
        PlayerInventory inv = player.getInventory();
        if (slot < PlayerInventory.getHotbarSize()) {
            inv.selectedSlot = slot;
            return;
        }
        int syncId = player.playerScreenHandler.syncId;
        int src = toScreen(slot);
        int dst = toScreen(inv.selectedSlot);
        MC.interactionManager.clickSlot(syncId, src, 0, SlotActionType.PICKUP, player);
        MC.interactionManager.clickSlot(syncId, dst, 0, SlotActionType.PICKUP, player);
    }

    private static void restoreSlot(ClientPlayerEntity player, int waterSlot, int oldSlot) {
        PlayerInventory inv = player.getInventory();
        if (waterSlot < PlayerInventory.getHotbarSize()) {
            inv.selectedSlot = oldSlot;
            return;
        }
        int syncId = player.playerScreenHandler.syncId;
        int src = toScreen(waterSlot);
        int dst = toScreen(oldSlot);
        MC.interactionManager.clickSlot(syncId, src, 0, SlotActionType.PICKUP, player);
        MC.interactionManager.clickSlot(syncId, dst, 0, SlotActionType.PICKUP, player);
    }

    private static int toScreen(int invSlot) {
        return (invSlot >= 0 && invSlot < 9) ? 36 + invSlot : invSlot;
    }
}
