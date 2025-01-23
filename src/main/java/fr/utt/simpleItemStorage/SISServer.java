package fr.utt.simpleItemStorage;

import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

/**
 * The SISServer class represents a server in the SimpleItemStorage plugin.
 * It provides methods to add, retrieve, and remove servers, as well as to get items from a server.
 */
public class SISServer {
    private String id;
    private String owner;
    private String uuid;
    private String lvl;
    private String date;
    private String nbDiamonds;

    /**
     * Constructs a new SISServer with the specified parameters.
     *
     * @param id the ID of the server
     * @param owner the owner of the server
     * @param uuid the UUID of the server
     * @param lvl the level of the server
     * @param date the creation date of the server
     * @param nbDiamonds the number of diamonds on the server
     */
    public SISServer(String id, String owner, String uuid, String lvl, String date, String nbDiamonds) {
        this.id = id;
        this.owner = owner;
        this.uuid = uuid;
        this.lvl = lvl;
        this.date = date;
        this.nbDiamonds = nbDiamonds;
    }

    /**
     * Adds a new server for the specified player.
     *
     * @param player the player who owns the server
     * @return the ID of the newly added server
     * @throws SQLException if an error occurs while adding the server
     */
    public static String addServer(Player player) throws SQLException {
        return DbManipulator.getInstance().addServer(player);
    }

    /**
     * Retrieves a server by its name.
     *
     * @param serverName the name of the server to retrieve
     * @return the SISServer object, or null if the server is not found
     * @throws SQLException if an error occurs while retrieving the server
     */
    public static SISServer getServer(String serverName) throws SQLException {
        return DbManipulator.getInstance().getServer(serverName);
    }

    /**
     * Retrieves all active servers.
     *
     * @return a list of SISServer objects
     * @throws SQLException if an error occurs while retrieving the servers
     */
    public static List<SISServer> getServers() throws SQLException {
        return DbManipulator.getInstance().getServers();
    }

    /**
     * Removes the server from the database.
     *
     * @throws SQLException if an error occurs while removing the server
     */
    public void remove() throws SQLException {
        DbManipulator.getInstance().removeServer(this.id);
    }

    /**
     * Returns the ID of the server.
     *
     * @return the ID of the server
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves all items from the server.
     *
     * @return a list of SISItem objects
     * @throws SQLException if an error occurs while retrieving the items
     */
    public List<SISItem> getItems() throws SQLException {
        return DbManipulator.getInstance().getItems(this);
    }
}