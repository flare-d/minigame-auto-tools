package com.example.autotools;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.util.ActionResult;
import com.example.autotools.handler.*;

public class AutoToolsMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient) return ActionResult.PASS;
            AutoToolHandler.onStartBreak(pos);
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            AutoToolHandler.tick(client.player);
            BridgeHandler.tick(client.player);
            ReplenishHandler.tick(client.player);
            WoolSafetyHandler.tick(client.player);
            WaterDropHandler.tick(client.player);
            PickupNotifier.tick(client.player);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            ResourceHud.render(drawContext);
            PickupNotifier.render(drawContext);
        });
    }
}
