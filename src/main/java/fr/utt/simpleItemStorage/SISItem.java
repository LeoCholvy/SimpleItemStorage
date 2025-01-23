package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

/**
 * The SISItem class represents an item in the SimpleItemStorage plugin.
 * It provides methods to convert between ItemStack and SISItem, and to serialize/deserialize item data.
 */
public class SISItem {
    private String MaterialName;
    private String ServerId;
    private String count;
    private String data;

    /**
     * Constructs a new SISItem with the specified material name, server ID, count, and data.
     *
     * @param MaterialName the name of the material
     * @param ServerId the ID of the server
     * @param count the count of the item
     * @param data the serialized data of the item
     */
    public SISItem(String MaterialName, String ServerId, String count, String data) {
        this.MaterialName = MaterialName;
        this.ServerId = ServerId;
        this.count = count;
        this.data = data;
    }

    /**
     * Converts an ItemStack to an SISItem for the specified server.
     *
     * @param item the ItemStack to convert
     * @param server the server to associate with the item
     * @return the converted SISItem
     */
    public static SISItem toSISItem(ItemStack item, SISServer server) {
        String MaterialName = XMaterial.matchXMaterial(item.getType()).name();
        String ServerId = server.getId();
        String count = item.getAmount() + "";
        Gson gson = new Gson();
        ItemStack itemClone = item.clone();
        itemClone.setAmount(1);
        String data = gson.toJson(XItemStack.serialize(itemClone));
        return new SISItem(MaterialName, ServerId, count, data);
    }

    /**
     * Returns the material name of the item.
     *
     * @return the material name
     */
    public String getMaterialName() {
        return MaterialName;
    }

    /**
     * Returns the count of the item.
     *
     * @return the count of the item
     */
    public int getCount() {
        return Integer.parseInt(count);
    }

    /**
     * Returns the serialized data of the item.
     *
     * @return the serialized data
     */
    public String getData() {
        return data;
    }

    /**
     * Returns the server ID associated with the item.
     *
     * @return the server ID
     */
    public String getServerId() {
        return ServerId;
    }

    /**
     * Converts a JSON string to an ItemStack with the specified count.
     *
     * @param json the JSON string representing the item
     * @param count the count of the item
     * @return the deserialized ItemStack
     */
    public static ItemStack jsonToItemStack(String json, int count) {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        ItemStack item = XItemStack.deserialize(map);
        item.setAmount(count);
        return item;
    }
}