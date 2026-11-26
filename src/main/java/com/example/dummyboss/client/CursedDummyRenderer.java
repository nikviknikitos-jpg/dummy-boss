package com.example.dummyboss.client;

import com.example.dummyboss.entity.CursedDummyEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class CursedDummyRenderer extends BipedEntityRenderer<CursedDummyEntity, BipedEntityModel<CursedDummyEntity>> {
    private static final Identifier TEX = new Identifier("dummyboss", "textures/entity/cursed_dummy.png");

    public CursedDummyRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(CursedDummyEntity entity) {
        return TEX;
    }
}
