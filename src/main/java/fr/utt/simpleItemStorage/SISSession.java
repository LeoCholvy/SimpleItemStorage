package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.sqlite.core.DB;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SISSession class represents a session for a player in the SimpleItemStorage plugin.
 * It provides methods to create, manage, and destroy sessions, as well as to add and remove items.
 */
public class SISSession {
    private Player player;
    private SISServer server;
    private static Map<Player, SISSession> sessions = new HashMap<>();
    private BukkitTask task;

    /**
     * Constructs a new SISSession for the specified player and server.
     *
     * @param player the player associated with the session
     * @param server the server associated with the session
     */
    public SISSession(Player player, SISServer server) {
        this.player = player;
        this.server = server;
    }

    /**
     * Creates a new session for the specified player and server name.
     * If a session already exists for the player, it is destroyed before creating a new one.
     *
     * @param player the player for whom the session is created
     * @param serverName the name of the server
     * @throws SQLException if an error occurs while retrieving the server
     */
    public static void createSession(Player player, String serverName) throws SQLException {
        if (DbManipulator.getInstance().getServer(serverName) == null) {
            player.sendMessage("The server " + serverName + " does not exist");
            return;
        }
        if (sessions.containsKey(player)) {
            sessions.get(player).destroy();
        }
        SISServer server = SISServer.getServer(serverName);
        SISSession session = new SISSession(player, server);
        sessions.put(player, session);

        session.display();
    }

    /**
     * Retrieves the session associated with the specified player.
     *
     * @param player the player whose session is to be retrieved
     * @return the SISSession object, or null if no session exists for the player
     */
    public static SISSession getSession(Player player) {
        return sessions.get(player);
    }

    /**
     * Displays the items in the session to the player.
     * This method is called periodically to update the player's view.
     */
    private void display() {
        String order = "";
        int page = 1;
        newTask(() -> {
            try {
                List<SISItem> items = this.getPageItemStacks(order, page);

                player.sendMessage("Server: " + this.server.getId());

                // FIXME
                for (SISItem item : items) {
                    player.sendMessage("Item: " + item.getMaterialName() + " Count: " + item.getCount() + " Data: " + item.getData());
                }

                player.sendMessage("--------------------");
            } catch (Exception e) {
                e.printStackTrace();
                SimpleItemStorage.getInstance().getLogger().severe("Failed to get the page item stacks");
                this.player.sendMessage("Failed to get the page item stacks");
            }
        });
    }

    /**
     * Retrieves a page of items from the server with the specified order and page number.
     *
     * @param order the order to sort the items
     * @param page the page number to retrieve
     * @return a list of SISItem objects
     * @throws SQLException if an error occurs while retrieving the items
     */
    private List<SISItem> getPageItemStacks(String order, int page) throws SQLException {
        return DbManipulator.getInstance().getItems(server, order, page);
    }

    /**
     * Destroys the session, canceling any scheduled tasks and removing the session from the map.
     */
    private void destroy() {
        if (task != null) {
            task.cancel();
        }
        sessions.remove(player);
    }

    /**
     * Adds an item to the server associated with the session.
     *
     * @param item the item to add
     */
    public void addItem(SISItem item) {
        newTask(() -> {
            try {
                DbManipulator.getInstance().addItem(item);
                // FIXME: remove this
                player.sendMessage("Item added");
            } catch (SQLException e) {
                e.printStackTrace();
                SimpleItemStorage.getInstance().getLogger().severe("Failed to add the item");
                player.sendMessage("Failed to add the item");
            }
        });
    }

    /**
     * Schedules a new task to run asynchronously.
     *
     * @param runnable the task to run
     */
    private void newTask(Runnable runnable) {
        if (this.task != null) {
            this.task.cancel();
        }
        Bukkit.getScheduler().runTaskAsynchronously(SimpleItemStorage.getInstance(), runnable);
    }

    /**
     * Returns the server associated with the session.
     *
     * @return the server associated with the session
     */
    public SISServer getServer() {
        return this.server;
    }

    /**
     * Checks if the server contains a specific item.
     *
     * @param itemData the data of the item to check
     * @return true if the item is found, false otherwise
     * @throws SQLException if an error occurs while checking the item
     */
    public boolean containsItem(String itemData) throws SQLException {
        return DbManipulator.getInstance().containsItem(server, itemData);
    }

    /**
     * Removes a specified quantity of an item from the server and adds it to the player's inventory.
     *
     * @param itemData the data of the item to remove
     * @param count the quantity of the item to remove
     */
    public void removeItem(String itemData, int count) {
        newTask(() -> {
            try {
                // check if the player inventory is full
                if (player.getInventory().firstEmpty() == -1) {
                    throw new Exception("Your inventory is full");
                }

                DbManipulator.getInstance().removeItem(server, itemData, count);
                // FIXME: remove this
                player.sendMessage("Item removed");

                // add the item to the player inventory
                ItemStack itemStack = SISItem.jsonToItemStack(itemData, count);
                player.getInventory().addItem(itemStack);
            } catch (SQLException e) {
                e.printStackTrace();
                SimpleItemStorage.getInstance().getLogger().severe("Failed to remove the item");
                player.sendMessage("Failed to remove the item");
            } catch (Exception e) {
                player.sendMessage(e.getMessage());
            }
        });
    }
}