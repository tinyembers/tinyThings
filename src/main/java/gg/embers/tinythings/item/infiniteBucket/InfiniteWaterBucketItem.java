package gg.embers.tinythings.item.infiniteBucket;

import gg.embers.tinythings.TinyThings;
import java.util.List;
import org.bukkit.Material;

public class InfiniteWaterBucketItem extends InfiniteBucketItem {
    public static final String ID = "infinite_water_bucket";

    public InfiniteWaterBucketItem(TinyThings plugin) {
        super(plugin, ID, "Infinite Water Bucket", "§bInfinite Water Bucket", Material.WATER_BUCKET,
                List.of("§7Places water without ever running dry."));
    }
}
