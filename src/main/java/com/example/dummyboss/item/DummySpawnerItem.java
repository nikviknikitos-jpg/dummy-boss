package com.example.dummyboss.item;

import com.example.dummyboss.entity.CursedDummyEntity;
import com.example.dummyboss.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DummySpawnerItem extends Item {
    public DummySpawnerItem(Settings s) { super(s); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity sp && world instanceof ServerWorld sw) {
            BlockHitResult hit = (BlockHitResult) sp.raycast(6.0, 1.0f, false);
            BlockPos pos = hit.getBlockPos().up();
            CursedDummyEntity dummy = ModEntities.CURSED_DUMMY.create(sw);
            if (dummy != null) {
                dummy.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                dummy.setOwner(sp);
                sw.spawnEntity(dummy);
            }
            sw.playSound(null, sp.getBlockPos(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            sp.getStackInHand(hand).decrement(1);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
