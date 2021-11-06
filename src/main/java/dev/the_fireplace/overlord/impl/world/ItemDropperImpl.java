package dev.the_fireplace.overlord.impl.world;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.overlord.domain.world.EntitySpawner;
import dev.the_fireplace.overlord.domain.world.ItemDropper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Random;

@Implementation
public final class ItemDropperImpl implements ItemDropper {

    private final Random random = new Random();
    private final EntitySpawner spawner;

    @Inject
    public ItemDropperImpl(EntitySpawner spawner) {
        this.spawner = spawner;
    }

    @Override
    @Nullable
    public ItemEntity dropItem(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty()) {
            return null;
        }
        double itemSpawnY = entity.getEyeY() - 0.3f;
        ItemEntity itemEntity = new ItemEntity(entity.getEntityWorld(), entity.getX(), itemSpawnY, entity.getZ(), stack);
        itemEntity.setPickupDelay(40);
        float horizontalVelocityMult = random.nextFloat() / 2;
        float horizontalDirection = random.nextFloat() * (float) Math.PI * 2;
        itemEntity.setVelocity(
            -MathHelper.sin(horizontalDirection) * horizontalVelocityMult,
            0.2f,
            MathHelper.cos(horizontalDirection) * horizontalVelocityMult
        );

        spawner.spawn(entity.getEntityWorld(), itemEntity);
        
        return itemEntity;
    }
    
    @Override
    @Nullable
    public ItemEntity throwItem(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty()) {
            return null;
        }
        double itemSpawnY = entity.getEyeY() - 0.3f;
        ItemEntity itemEntity = new ItemEntity(entity.world, entity.getX(), itemSpawnY, entity.getZ(), stack);
        itemEntity.setPickupDelay(40);
        itemEntity.setThrower(entity.getUuid());
        float g = MathHelper.sin(entity.getPitch() * (float) Math.PI / 180);
        float j = MathHelper.cos(entity.getPitch() * (float) Math.PI / 180);
        float k = MathHelper.sin(entity.getYaw() * (float) Math.PI / 180);
        float l = MathHelper.cos(entity.getYaw() * (float) Math.PI / 180);
        float m = random.nextFloat() * (float) Math.PI * 2;
        float n = 0.02f * random.nextFloat();
        itemEntity.setVelocity(
            (double) (-k * j * 0.3f) + Math.cos(m) * (double) n,
            -g * 0.3f + 0.1f + (random.nextFloat() - random.nextFloat()) * 0.1f,
            (double) (l * j * 0.3f) + Math.sin(m) * (double) n
        );

        spawner.spawn(entity.getEntityWorld(), itemEntity);

        return itemEntity;
    }
}
