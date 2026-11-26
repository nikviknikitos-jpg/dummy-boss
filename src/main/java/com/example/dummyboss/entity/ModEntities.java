package com.example.dummyboss.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static EntityType<CursedDummyEntity> CURSED_DUMMY;

    public static void register() {
        CURSED_DUMMY = Registry.register(Registries.ENTITY_TYPE,
                new Identifier("dummyboss", "cursed_dummy"),
                FabricEntityTypeBuilder.<CursedDummyEntity>createMob()
                        .spawnGroup(SpawnGroup.MONSTER)
                        .entityFactory(CursedDummyEntity::new)
                        .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                        .build());
    }
}
