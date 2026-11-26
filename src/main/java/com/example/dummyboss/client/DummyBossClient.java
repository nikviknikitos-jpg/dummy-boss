package com.example.dummyboss.client;

import com.example.dummyboss.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DummyBossClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.CURSED_DUMMY, CursedDummyRenderer::new);
    }
}
