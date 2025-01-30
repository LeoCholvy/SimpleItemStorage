package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fr.utt.simpleItemStorage.SISPrinter.printDebug;

/**
 * Class representing a terminal in the SimpleItemStorage plugin.
 */
public class SISTerminal {
    private String id;
    private String locX;
    private String locY;
    private String locZ;
    private String serverId;
    private String active;
    private String world;

    /**
     * Constructs a SISTerminal object.
     *
     * @param id the terminal ID
     * @param locX the X coordinate of the terminal
     * @param locY the Y coordinate of the terminal
     * @param locZ the Z coordinate of the terminal
     * @param serverId the server ID associated with the terminal
     * @param active the active status of the terminal
     * @param world the world where the terminal is located
     */
    public SISTerminal(String id, String locX, String locY, String locZ, String serverId, String active, String world) {
        this.id = id;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.serverId = serverId;
        this.active = active;
        this.world = world;
    }

    /**
     * Creates an ItemStack representing a terminal item.
     *
     * @return the terminal ItemStack
     */
    public static ItemStack getTerminalItemStack() {
        // base 64
        String texture = SimpleItemStorage.getInstance().getConfig().getString("terminal_skin");

        ItemStack item = SkullCreator.itemFromBase64(texture);

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        Objects.requireNonNull(meta);

        // Set the display name of the item
        meta.setDisplayName("Terminal");

        // Set the lore of the item
        List<String> lore = new ArrayList<>();
        lore.add("§7A terminal to access the storage");
        meta.setLore(lore);

        // Tag to identify the terminal
        meta.setCustomModelData(7); // serves no purpose unless a resource pack is used
        NamespacedKey key = new NamespacedKey(SimpleItemStorage.getInstance(), "terminal");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Checks if a block is a terminal block.
     *
     * @param block the block to check
     * @return true if the block is a terminal block, false otherwise
     */
    public static boolean isTerminalBlock(Block block) {
        Objects.requireNonNull(block);

        // Quick check
        if (!(XBlock.isSimilar(block, XMaterial.PLAYER_HEAD) || XBlock.isSimilar(block, XMaterial.PLAYER_WALL_HEAD))) {
            printDebug("Block is not a PLAYER_HEAD or PLAYER_WALL_HEAD");
            return false;
        }

        if (SISTerminal.getTerminal(block) == null) {
            printDebug("Block is not a terminal");
            return false;
        }

        return true;
    }

    /**
     * Retrieves a terminal associated with a block.
     *
     * @param block the block to get the terminal from
     * @return the SISTerminal object, or null if not found
     */
    public static SISTerminal getTerminal(Block block) {
        try {
            return DbManipulator.getInstance().getTerminal(block);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an item is a terminal item.
     *
     * @param item the item to check
     * @return true if the item is a terminal item, false otherwise
     */
    public static boolean isTerminalItem(ItemStack item) {
        Objects.requireNonNull(item);
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(SimpleItemStorage.getInstance(), "terminal");
        return container.has(key, PersistentDataType.INTEGER);
    }

    /**
     * Adds a terminal to the database.
     *
     * @param block the block representing the terminal
     * @param id the terminal ID
     * @throws SQLException if a database access error occurs
     */
    public static void addTerminal(Block block, String id) throws SQLException {
        DbManipulator.getInstance().addTerminal(block, id);
    }

    /**
     * Initializes terminal textures.
     */
    public static void initTextures() {
        // TODO: Implement texture initialization
    }

    /**
     * Gets the server ID associated with the terminal.
     *
     * @return the server ID
     */
    public String getServerId() {
        return serverId;
    }
}