package com.example.dummyboss.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item DUMMY_SPAWNER;
    public static ItemGroup TAB;

    public static void register() {
        DUMMY_SPAWNER = Registry.register(Registries.ITEM,
                new Identifier("dummyboss", "dummy_spawner"),
                new DummySpawnerItem(new Item.Settings().maxCount(16)));
        TAB = Registry.register(Registries.ITEM_GROUP,
                new Identifier("dummyboss", "dummyboss_tab"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.dummyboss.dummyboss_tab"))
                        .icon(() -> new ItemStack(DUMMY_SPAWNER))
                        .entries((ctx, entries) -> entries.add(DUMMY_SPAWNER))
                        .build());
    }
}
