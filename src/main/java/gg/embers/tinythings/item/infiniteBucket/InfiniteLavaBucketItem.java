package gg.embers.tinythings.item.infiniteBucket;

import gg.embers.tinythings.TinyThings;
import java.util.List;
import org.bukkit.Material;

public class InfiniteLavaBucketItem extends InfiniteBucketItem {
    public static final String ID = "infinite_lava_bucket";

    public InfiniteLavaBucketItem(TinyThings plugin) {
        super(plugin, ID, "Infinite Lava Bucket", "§cInfinite Lava Bucket", Material.LAVA_BUCKET,
                List.of("§7Places lava without ever running dry."));
    }
}
