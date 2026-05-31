package gg.embers.tinythings.item.spawnerWrench;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

// Touches RoseStacker's API, so it must only be loaded when RoseStacker is installed.
final class RoseStackerHook {

    private RoseStackerHook() {
    }

    static ItemStack takeSpawnerItem(Block block) {
        RoseStackerAPI api = RoseStackerAPI.getInstance();
        StackedSpawner stacked = api.getStackedSpawner(block);
        if (stacked != null) {
            SpawnerType type = stacked.getSpawnerTile().getSpawnerType();
            int size = stacked.getStackSize();
            api.removeSpawnerStack(stacked);
            return ItemUtils.getSpawnerAsStackedItemStack(type, size);
        }
        SpawnerType type = SpawnerType.empty();
        if (block.getState() instanceof CreatureSpawner spawner && spawner.getSpawnedType() != null) {
            type = SpawnerType.of(spawner.getSpawnedType());
        }
        return ItemUtils.getSpawnerAsStackedItemStack(type, 1);
    }
}
