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

public class ScaffoldHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastPlace = 0;
    private static final long COOLDOWN = 50;

    public static boolean isScaffolding(ClientPlayerEntity player) {
        ItemStack hand = player.getMainHandStack();
        if (!hand.isIn(ItemTags.WOOL)) return false;
        BlockPos below = player.getBlockPos().down();
        return player.getWorld().getBlockState(below).isAir();
    }

    public static void tick(ClientPlayerEntity player) {
        if (MC.interactionManager == null) return;
        if (System.currentTimeMillis() - lastPlace < COOLDOWN) return;

        ItemStack hand = player.getMainHandStack();
        if (!hand.isIn(ItemTags.WOOL)) return;
        if (player.getVelocity().y > 0.1) return;

        BlockPos below = player.getBlockPos().down();
        if (!player.getWorld().getBlockState(below).isAir()) return;

        BlockPos support = findSupport(player, below);
        if (support == null) return;

        Direction face = directionFrom(support, below);
        Vec3d hitVec = Vec3d.ofCenter(support).add(Vec3d.of(face.getVector()).multiply(0.5));
        BlockHitResult bhr = new BlockHitResult(hitVec, face, support, false);

        var result = MC.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
        if (result.isAccepted()) {
            player.swingHand(Hand.MAIN_HAND);
            lastPlace = System.currentTimeMillis();
        }
    }

    private static BlockPos findSupport(ClientPlayerEntity player, BlockPos target) {
        for (Direction d : Direction.values()) {
            BlockPos n = target.offset(d);
            if (!player.getWorld().getBlockState(n).isAir()) return n;
        }
        return null;
    }

    private static Direction directionFrom(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        if (dx == 1) return Direction.EAST;
        if (dx == -1) return Direction.WEST;
        if (dy == 1) return Direction.UP;
        if (dy == -1) return Direction.DOWN;
        if (dz == 1) return Direction.SOUTH;
        if (dz == -1) return Direction.NORTH;
        return Direction.UP;
    }
}
