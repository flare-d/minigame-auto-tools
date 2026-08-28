package com.example.autotools.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssistHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final double REACH_SQ = 6.0 * 6.0;
    private static LivingEntity target = null;

    public static void setTarget(LivingEntity entity) {
        target = entity;
    }

    public static void tick(ClientPlayerEntity player) {
        if (target == null) return;

        if (target.isRemoved() || !target.isAlive()
                || target.squaredDistanceTo(player) > REACH_SQ
                || !player.canSee(target)) {
            target = null;
            return;
        }

        aimSmooth(player, target);

        if (MC.currentScreen == null && MC.interactionManager != null
                && player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            MC.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    }

    private static void aimSmooth(ClientPlayerEntity player, LivingEntity target) {
        Vec3d eye = player.getEyePos();
        Vec3d c = target.getBoundingBox().getCenter();
        double dx = c.x - eye.x;
        double dy = c.y - eye.y;
        double dz = c.z - eye.z;
        double h = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float targetPitch = MathHelper.clamp((float) -Math.toDegrees(Math.atan2(dy, h)), -90f, 90f);

        float yaw = player.getYaw();
        float pitch = player.getPitch();

        float diffYaw = MathHelper.wrapDegrees(targetYaw - yaw);
        float diffPitch = targetPitch - pitch;

        player.setYaw(yaw + diffYaw * 0.5f);
        player.setPitch(pitch + diffPitch * 0.5f);
    }
}
