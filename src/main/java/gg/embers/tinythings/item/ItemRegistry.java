/*
 * Decompiled with CFR 0.152.
 */
package gg.embers.tinythings.item;

import gg.embers.tinythings.item.TinyItem;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemRegistry {
    private final Map<String, TinyItem> items = new LinkedHashMap<String, TinyItem>();

    public void register(TinyItem tinyItem) {
        this.items.put(tinyItem.id().toLowerCase(), tinyItem);
    }

    public TinyItem get(String string) {
        return string == null ? null : this.items.get(string.toLowerCase());
    }

    public Collection<TinyItem> all() {
        return Collections.unmodifiableCollection(this.items.values());
    }

    public int size() {
        return this.items.size();
    }
}

