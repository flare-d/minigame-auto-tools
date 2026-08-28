package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BridgeHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastPlace = 0;
    private static final long COOLDOWN = 120;
    private static final long SAFETY_COOLDOWN = 60;
    private static long lastSafetyPlace = 0;
    public static boolean placedThisTick = false;
    private static final Map<BlockPos, Long> recentPlacements = new HashMap<>();
    private static final long PLACEMENT_MEMORY_MS = 3000;

    public static void tick(ClientPlayerEntity player) {
        placedThisTick = false;
        if (MC.interactionManager == null) return;
        if (player.currentScreenHandler != player.playerScreenHandler) return;

        cleanupRecent();

        // Safety: если под ногами нет твердого блока и мы падаем — срочно ставим
        if (!player.isOnGround() && player.getVelocity().y < -0.08) {
            BlockPos below = player.getBlockPos().down();
            if (isUnsafe(player, below)) {
                if (System.currentTimeMillis() - lastSafetyPlace >= SAFETY_COOLDOWN) {
                    tryPlace(player, below, true);
                }
            }
        }

        if (System.currentTimeMillis() - lastPlace < COOLDOWN) return;

        ItemStack hand = player.getMainHandStack();
        if (!(hand.getItem() instanceof BlockItem)) return;

        if (MC.crosshairTarget != null && MC.crosshairTarget.getType() == HitResult.Type.BLOCK) return;

        BlockPos target = findTarget(player);
        if (target == null) return;
        if (!player.getWorld().getBlockState(target).isAir()) return;
        if (player.squaredDistanceTo(Vec3d.ofCenter(target)) > 16.0) return;

        tryPlace(player, target, false);
    }

    private static boolean isUnsafe(ClientPlayerEntity player, BlockPos pos) {
        if (!player.getWorld().getBlockState(pos).isAir()) {
            return player.getWorld().getBlockState(pos).getCollisionShape(player.getWorld(), pos).isEmpty();
        }
        return true;
    }

    private static void tryPlace(ClientPlayerEntity player, BlockPos target, boolean safety) {
        BlockPos support = findSupport(player, target);
        if (support == null) return;
        if (player.getWorld().getBlockState(support).isAir()) return;

        Direction face = directionFrom(support, target);
        Vec3d hitVec = Vec3d.ofCenter(support).add(Vec3d.of(face.getVector()).multiply(0.5));
        BlockHitResult bhr = new BlockHitResult(hitVec, face, support, false);

        var result = MC.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
        if (result.isAccepted()) {
            player.swingHand(Hand.MAIN_HAND);
            recentPlacements.put(target, System.currentTimeMillis());
            if (safety) {
                lastSafetyPlace = System.currentTimeMillis();
            } else {
                lastPlace = System.currentTimeMillis();
            }
            placedThisTick = true;
        }
    }

    private static void cleanupRecent() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, Long>> it = recentPlacements.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() > PLACEMENT_MEMORY_MS) {
                it.remove();
            }
        }
    }

    private static BlockPos findTarget(ClientPlayerEntity player) {
        BlockPos feet = player.getBlockPos();

        if (player.getWorld().getBlockState(feet.down()).isAir()) {
            return feet.down();
        }

        Direction dir = yawToDirection(player.getYaw());
        BlockPos forward = feet.offset(dir);

        if (player.getWorld().getBlockState(forward).isAir()
                && player.getWorld().getBlockState(forward.down()).isAir()) {
            return forward;
        }

        BlockPos forwardDown = forward.down();
        if (player.getWorld().getBlockState(forwardDown).isAir()
                && player.getWorld().getBlockState(forwardDown.down()).isAir()
                && player.getVelocity().y < -0.1) {
            return forwardDown;
        }

        return null;
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

    private static Direction yawToDirection(float yaw) {
        float n = MathHelper.wrapDegrees(yaw);
        if (n >= -45f && n < 45f) return Direction.SOUTH;
        if (n >= 45f && n < 135f) return Direction.WEST;
        if (n >= 135f || n < -135f) return Direction.NORTH;
        return Direction.EAST;
    }
}
