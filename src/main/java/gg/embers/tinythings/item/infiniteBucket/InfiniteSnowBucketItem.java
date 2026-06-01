package gg.embers.tinythings.item.infiniteBucket;

import gg.embers.tinythings.TinyThings;
import java.util.List;
import org.bukkit.Material;

public class InfiniteSnowBucketItem extends InfiniteBucketItem {
    public static final String ID = "infinite_snow_bucket";

    public InfiniteSnowBucketItem(TinyThings plugin) {
        super(plugin, ID, "Infinite Snow Bucket", "§fInfinite Snow Bucket", Material.POWDER_SNOW_BUCKET,
                List.of("§7Places powder snow without ever running dry."));
    }
}
