package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SafetyHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastWool = 0;
    private static long lastWater = 0;
    private static final long WOOL_CD = 50;
    private static final long WATER_CD = 1000;

    public static void tick(ClientPlayerEntity player) {
        if (player.isOnGround()) return;
        if (player.getVelocity().y >= -0.3) return;

        if (tryWaterDrop(player)) return;
        tryWool(player);
    }

    private static void tryWool(ClientPlayerEntity player) {
        ItemStack hand = player.getMainHandStack();
        if (!hand.isIn(ItemTags.WOOL)) return;
        if (System.currentTimeMillis() - lastWool < WOOL_CD) return;

        player.setPitch(90f);
        BlockPos below = player.getBlockPos().down();
        if (!player.getWorld().getBlockState(below).isAir()) return;

        boolean supported = false;
        for (Direction d : Direction.values()) {
            if (!player.getWorld().getBlockState(below.offset(d)).isAir()) {
                supported = true;
                break;
            }
        }
        if (!supported) return;

        Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.9, 0);
        BlockHitResult bhr = new BlockHitResult(hitVec, Direction.UP, below, false);

        if (MC.interactionManager != null) {
            var res = MC.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
            if (res.isAccepted()) {
                player.swingHand(Hand.MAIN_HAND);
                lastWool = System.currentTimeMillis();
            }
        }
    }

    private static boolean tryWaterDrop(ClientPlayerEntity player) {
        if (System.currentTimeMillis() - lastWater < WATER_CD) return false;
        if (MC.interactionManager == null) return false;

        int waterSlot = findWater(player);
        if (waterSlot == -1) return false;

        BlockPos ground = findGround(player);
        if (ground == null) return false;

        double dist = player.getY() - (ground.getY() + 1);
        if (dist < 2.0 || dist > 4.0) return false;

        int oldSlot = player.getInventory().selectedSlot;
        moveToHand(player, waterSlot);

        BlockPos place = player.getBlockPos().down();
        if (!player.getWorld().getBlockState(place).isAir()) place = ground.up();

        Vec3d hitVec = Vec3d.ofCenter(place.down()).add(0, 0.9, 0);
        BlockHitResult bhr = new BlockHitResult(hitVec, Direction.UP, place.down(), false);
        MC.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
        lastWater = System.currentTimeMillis();

        restoreSlot(player, waterSlot, oldSlot);
        return true;
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
        for (int i = 1; i < 25; i++) {
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
