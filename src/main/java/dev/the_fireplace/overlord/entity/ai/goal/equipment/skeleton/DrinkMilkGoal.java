package dev.the_fireplace.overlord.entity.ai.goal.equipment.skeleton;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.overlord.domain.entity.AnimatedMilkDrinker;
import dev.the_fireplace.overlord.domain.inventory.InventorySearcher;
import dev.the_fireplace.overlord.entity.ArmyEntity;
import dev.the_fireplace.overlord.entity.ai.goal.equipment.SwapEquipmentGoal;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Predicate;

public class DrinkMilkGoal<T extends ArmyEntity & AnimatedMilkDrinker> extends SwapEquipmentGoal
{
    protected static final Predicate<ItemStack> IS_MILK = stack -> stack.getItem() == Items.MILK_BUCKET;
    protected final T armyEntity;
    protected final InventorySearcher inventorySearcher;
    protected int swapBackSlot;

    protected byte drinkingTicks;

    public DrinkMilkGoal(T armyEntity) {
        super(armyEntity);
        this.armyEntity = armyEntity;
        this.inventorySearcher = DIContainer.get().getInstance(InventorySearcher.class);
    }

    @Override
    public boolean canStart() {
        return super.canStart()
            && inventorySearcher.hasSlotMatching(armyEntity.getInventory(), IS_MILK)
            && armyEntity.canDrinkMilk();
    }

    @Override
    public void start() {
        super.start();
        Integer milkSlot = this.inventorySearcher.getFirstSlotMatching(armyEntity.getInventory(), IS_MILK);
        if (milkSlot != null) {
            int offHandSlot = this.armyEntity.getOffHandSlot();
            Inventory inventory = this.armyEntity.getInventory();
            ItemStack milkStack = inventory.removeStack(milkSlot);
            ItemStack offHandStack = inventory.removeStack(offHandSlot);
            inventory.setStack(offHandSlot, milkStack);
            inventory.setStack(milkSlot, offHandStack);
            this.swapBackSlot = milkSlot;
            this.armyEntity.startDrinkingMilkAnimation();
            this.drinkingTicks = armyEntity.getEquipmentSwapTicks();
        }
    }

    @Override
    public boolean shouldContinue() {
        return this.drinkingTicks > 0;
    }

    @Override
    public void tick() {
        if (--this.drinkingTicks > 0) {
            return;
        }

        this.armyEntity.completeDrinkingMilk();
        int offHandSlot = this.armyEntity.getOffHandSlot();
        Inventory inventory = this.armyEntity.getInventory();
        ItemStack milkStack = inventory.removeStack(offHandSlot);
        ItemStack offHandStack = inventory.removeStack(swapBackSlot);
        inventory.setStack(offHandSlot, offHandStack);
        milkStack.decrement(1);
        this.armyEntity.giveItemStack(milkStack.isEmpty() ? new ItemStack(Items.BUCKET) : milkStack);
    }
}
