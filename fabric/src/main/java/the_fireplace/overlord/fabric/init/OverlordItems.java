package the_fireplace.overlord.fabric.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static the_fireplace.overlord.OverlordHelper.MODID;

public class OverlordItems {

    public static final Item OWNED_SKELETON_SPAWN_EGG = new SpawnEggItem(OverlordEntities.OWNED_SKELETON_TYPE, 0xC1C1C1, 0xC76462, new Item.Settings().group(ItemGroup.MISC));

    public static void registerItems() {
        registerItem("owned_skeleton_spawn_egg", OWNED_SKELETON_SPAWN_EGG);
    }

    private static void registerItem(String path, Item item) {
        Registry.register(Registry.ITEM, new Identifier(MODID, path), item);
    }
}
