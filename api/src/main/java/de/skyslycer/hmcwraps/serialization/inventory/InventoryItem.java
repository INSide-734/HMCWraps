package de.skyslycer.hmcwraps.serialization.inventory;

import de.skyslycer.hmcwraps.serialization.item.SerializableItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class InventoryItem extends SerializableItem {

    private @Nullable HashMap<String, HashMap<String, List<String>>> actions;
    private @Nullable List<Integer> fills;

    public InventoryItem(String id, String name, @Nullable Boolean glow, @Nullable List<String> lore, @Nullable List<String> flags,
                            @Nullable Integer modelId, @Nullable Map<String, Integer> enchantments, @Nullable Integer amount,
                            @Nullable String color, @Nullable String nbt, @Nullable Integer durability, @Nullable String skullOwner, @Nullable String skullTexture,
                            @Nullable String trim, @Nullable String trimMaterial, @Nullable String equippableSlot, @Nullable String equippableModel) {
        super(id, name, glow, lore, flags, modelId, enchantments, amount, color, nbt, durability, skullOwner, skullTexture, trim, trimMaterial, equippableSlot, equippableModel);
    }

    public InventoryItem() {
    }

    @Nullable
    public HashMap<String, HashMap<String, List<String>>> getActions() {
        return actions;
    }

    @Nullable
    public List<Integer> getFills() {
        return fills;
    }

}
