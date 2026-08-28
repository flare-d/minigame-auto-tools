package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WoolSafetyHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastWool = 0;
    private static final long COOLDOWN = 100;

    public static void tick(ClientPlayerEntity player) {
        if (player.isOnGround()) return;
        if (player.getVelocity().y >= -0.3) return;

        ItemStack hand = player.getMainHandStack();
        if (!hand.isIn(ItemTags.WOOL)) return;
        if (System.currentTimeMillis() - lastWool < COOLDOWN) return;

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
}
