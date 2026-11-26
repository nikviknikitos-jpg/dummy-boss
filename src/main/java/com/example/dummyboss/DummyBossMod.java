package com.example.dummyboss;

import com.example.dummyboss.entity.ModEntities;
import com.example.dummyboss.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyBossMod implements ModInitializer {
    public static final String MODID = "dummyboss";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ModItems.register();
        ModEntities.register();
        FabricDefaultAttributeRegistry.register(ModEntities.CURSED_DUMMY,
                com.example.dummyboss.entity.CursedDummyEntity.createAttributes());
        CommandRegistrationCallback.EVENT.register((d, ra, e) ->
                d.register(net.minecraft.server.command.CommandManager.literal("dummy").executes(ctx -> {
                    var p = ctx.getSource().getPlayer();
                    if (p != null) {
                        p.getInventory().insertStack(new net.minecraft.item.ItemStack(ModItems.DUMMY_SPAWNER));
                        ctx.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Cursed Dummy given"), false);
                        return 1;
                    }
                    return 0;
                })));
        LOGGER.info("DummyBossMod initialized");
    }
}
