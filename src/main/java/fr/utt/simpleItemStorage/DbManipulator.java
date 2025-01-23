package fr.utt.simpleItemStorage;

import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static fr.utt.simpleItemStorage.SISPrinter.print;
import static fr.utt.simpleItemStorage.SISPrinter.printError;

/**
 * The DbManipulator class is responsible for managing the database operations
 * for the SimpleItemStorage plugin. It provides methods to connect to the database,
 * initialize tables, and perform CRUD operations on the database.
 */
public class DbManipulator {
    private static String url = null;
    private static DbManipulator instance = null;
    private Connection connection = null;
    private static final String currentVersion = "1";

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the database connection and tables.
     */
    private DbManipulator() {
        this.url = "jdbc:sqlite:" + SimpleItemStorage.getInstance().getDataFolder() + "/database.db";
        new File(SimpleItemStorage.getInstance().getDataFolder().getPath()).mkdirs();
        this.connect();
        this.initTables();
    }

    /**
     * Returns the singleton instance of the DbManipulator class.
     * If the instance does not exist, it creates a new one.
     *
     * @return the singleton instance of DbManipulator
     */
    public synchronized static DbManipulator getInstance() {
        if (instance == null) {
            instance = new DbManipulator();
        }
        return instance;
    }

    /**
     * Initializes the database tables if they do not already exist.
     */
    private synchronized void initTables() {
        try {
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Servers (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    owner TEXT NOT NULL,\n" +
                    "    UUID TEXT,\n" +
                    "    lvl INTEGER DEFAULT 0,\n" +
                    "    date TEXT NOT NULL,\n" +
                    "    nbDiamonds INTEGER DEFAULT 0,\n" +
                    "    active BOOLEAN DEFAULT FALSE\n" +
                    ");");
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Terminals (\n" +
                    "    id INTEGER PRIMARY KEY,\n" +
                    "    locX INTEGER NOT NULL,\n" +
                    "    locY INTEGER NOT NULL,\n" +
                    "    locZ INTEGER NOT NULL,\n" +
                    "    ServerId INTEGER NOT NULL,\n" +
                    "    active BOOLEAN DEFAULT FALSE,\n" +
                    "    FOREIGN KEY (ServerId) REFERENCES SIS_Servers (id)\n" +
                    ");");
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Items (\n" +
                    "    MaterialName TEXT NOT NULL,\n" +
                    "    ServerId INTEGER NOT NULL,\n" +
                    "    count INTEGER NOT NULL,\n" +
                    "    data JSON,\n" +
                    "    UNIQUE (ServerId, data),\n" +
                    "    FOREIGN KEY (ServerId) REFERENCES SIS_Servers (id)\n" +
                    ");");
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Infos (\n" +
                    "    version TEXT NOT NULL\n" +
                    ");");
            var resultSet = this.connection.createStatement().executeQuery("SELECT version FROM SIS_Infos LIMIT 1;");
            if (resultSet.next()) {
                String version = resultSet.getString("version");
                if (!currentVersion.equals(version)) {
                    // SimpleItemStorage.getInstance().getLogger().severe("Database version mismatch");
                    printError("Database version mismatch");
                    throw new SQLException("Database version mismatch");
                }
            } else {
                this.connection.createStatement().execute("INSERT INTO SIS_Infos (version) VALUES ('" + currentVersion + "');");
            }

            // SimpleItemStorage.getInstance().getLogger().info("Tables has been created in the database");
            print("Tables has been created in the database");
        } catch (SQLException e) {
            // SimpleItemStorage.getInstance().getLogger().severe("Failed to create the table");
            printError("Failed to create the table");
            e.printStackTrace();
        }
    }

    /**
     * Establishes a connection to the database.
     */
    private synchronized void connect() {
        try {
            this.connection = DriverManager.getConnection(url);
            // SimpleItemStorage.getInstance().getLogger().info("Connection to the database has been established");
            print("Connection to the database has been established");
        } catch (SQLException e) {
            // SimpleItemStorage.getInstance().getLogger().severe("Failed to connect to the database");
            printError("Failed to connect to the database");
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection to the database.
     */
    public synchronized void disconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
                // SimpleItemStorage.getInstance().getLogger().info("Connection to the database has been closed");
                print("Connection to the database has been closed");
                instance = null;
            }
        } catch (SQLException e) {
            // SimpleItemStorage.getInstance().getLogger().severe("Failed to close the connection to the database");
            printError("Failed to close the connection to the database");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the names of all active servers.
     *
     * @return a list of server names
     * @throws Exception if an error occurs while retrieving the server names
     */
    public synchronized List<String> getServersNames() throws Exception {
        String query = "SELECT id FROM SIS_Servers WHERE active = 1;";
        List<String> servers = new ArrayList<>();
        try {
            var resultSet = this.connection.createStatement().executeQuery(query);
            while (resultSet.next()) {
                servers.add(resultSet.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Failed to get the servers names");
        }
        return servers;
    }

    /**
     * Retrieves all active servers.
     *
     * @return a list of SISServer objects
     * @throws SQLException if an error occurs while retrieving the servers
     */
    public synchronized List<SISServer> getServers() throws SQLException {
        List<SISServer> servers = new ArrayList<>();
        String query = "SELECT * FROM SIS_Servers WHERE active = 1;";
        var resultSet = this.connection.createStatement().executeQuery(query);
        while (resultSet.next()) {
            servers.add(new SISServer(resultSet.getString("id"),
                                       resultSet.getString("owner"),
                                       resultSet.getString("UUID"),
                                       resultSet.getString("lvl"),
                                       resultSet.getString("date"),
                                       resultSet.getString("nbDiamonds")));
        }

        return servers;
    }

    /**
     * Adds a new server to the database.
     *
     * @param player the player who owns the server
     * @return the ID of the newly added server
     * @throws SQLException if an error occurs while adding the server
     */
    public synchronized String addServer(Player player) throws SQLException {
        String date = String.valueOf(System.currentTimeMillis());
        String owner = player.getName();

        String query;

        if (ConfigManipulator.isOnlineMode()) {
            String uuid = player.getUniqueId().toString();
            query = "INSERT INTO SIS_Servers (owner,UUID,date,active) VALUES (\n" +
                    "  '" + owner + "',\n" +
                    "  '" + uuid + "',\n" +
                    "  '" + date + "',\n" +
                    "  1\n" +
                    "); SELECT * FROM SIS_Servers;";
        } else {
            query = "INSERT INTO SIS_Servers (owner,date,active) VALUES (\n" +
                    "  '" + owner + "',\n" +
                    "  '" + date + "',\n" +
                    "  1\n" +
                    "); SELECT * FROM SIS_Servers;";
        }

        this.connection.createStatement().executeUpdate(query);

        // we assume that the id is the last one since we can't delete a server
        var resultSet = this.connection.createStatement().executeQuery("SELECT MAX(id) AS max_id FROM SIS_Servers;");
        if (resultSet.next()) {
            return resultSet.getString("max_id");
        } else {
            throw new SQLException("Failed to get the server id");
        }
    }

    /**
     * Marks a server as inactive in the database.
     *
     * @param serverName the name of the server to remove
     * @throws SQLException if an error occurs while removing the server
     */
    public synchronized void removeServer(String serverName) throws SQLException {
        String query = "UPDATE SIS_Servers SET active = 0 WHERE id = " + serverName + ";";
        int rowsDeleted = this.connection.createStatement().executeUpdate(query);
        if (rowsDeleted == 0) {
            throw new SQLException("Failed to remove the server");
        }
        if (rowsDeleted > 1) {
            throw new SQLException("Multiple servers removed");
        }
    }

    /**
     * Retrieves a server by its name.
     *
     * @param serverName the name of the server to retrieve
     * @return the SISServer object, or null if the server is not found
     * @throws SQLException if an error occurs while retrieving the server
     */
    public synchronized SISServer getServer(String serverName) throws SQLException {
        String query = "SELECT * FROM SIS_Servers WHERE id = " + serverName + " AND active = 1;";
        var resultSet = this.connection.createStatement().executeQuery(query);
        if (resultSet.next()) {
            return new SISServer(resultSet.getString("id"),
                                 resultSet.getString("owner"),
                                 resultSet.getString("UUID"),
                                 resultSet.getString("lvl"),
                                 resultSet.getString("date"),
                                 resultSet.getString("nbDiamonds"));
        } else {
            return null;
        }
    }

    /**
     * Retrieves items from a server with pagination and optional ordering.
     *
     * @param server the server to retrieve items from
     * @param order the order to sort the items
     * @param page the page number to retrieve
     * @return a list of SISItem objects
     * @throws SQLException if an error occurs while retrieving the items
     */
    public synchronized List<SISItem> getItems(SISServer server, String order, int page) throws SQLException {
        String query = "SELECT * FROM SIS_Items WHERE ServerId = " + server.getId() +
                       DbManipulator.parseOrder(order) +
                       " LIMIT 10 OFFSET " + (page - 1) * 10 + ";";
        SimpleItemStorage.getInstance().getLogger().info("Query: " + query);
        List<SISItem> items = new ArrayList<>();
        var resultSet = this.connection.createStatement().executeQuery(query);
        while (resultSet.next()) {
            items.add(new SISItem(resultSet.getString("MaterialName"),
                                   resultSet.getString("ServerId"),
                                   resultSet.getString("count"),
                                   resultSet.getString("data")));
        }
        return items;
    }

    /**
     * Retrieves all items from a server. This method is not recommended for use due to its heavy load.
     *
     * @param server the server to retrieve items from
     * @return a list of SISItem objects
     * @throws SQLException if an error occurs while retrieving the items
     */
    public List<SISItem> getItems(SISServer server) throws SQLException {
        String query = "SELECT * FROM SIS_Items WHERE ServerId = " + server.getId() + ";";
        List<SISItem> items = new ArrayList<>();

        // On crée un statement
        try (var statement = this.connection.createStatement()) {

            // On soumet l'exécution à un ExecutorService
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<List<SISItem>> future = executor.submit(() -> {
                List<SISItem> result = new ArrayList<>();
                try (var rs = statement.executeQuery(query)) {
                    while (rs.next()) {
                        result.add(new SISItem(
                                rs.getString("MaterialName"),
                                rs.getString("ServerId"),
                                rs.getString("count"),
                                rs.getString("data")
                        ));
                    }
                }
                return result;
            });

            // On attend 10 ms (un tick minecraft c'est 50 ms)
            try {
                // get(timeout, TimeUnit) lève TimeoutException si le temps est dépassé
                items = future.get(10, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // Si la requête prend plus de 100 ms, on la “cancel”…
                statement.cancel();
                future.cancel(true);
                // … et on peut lever une exception, ou renvoyer une liste vide, etc.
                throw new SQLException("Query timed out after 10 ms", e);
            } catch (Exception e) {
                // Gérer les autres exceptions
                throw new SQLException("Error during query execution", e);
            } finally {
                executor.shutdownNow();
            }
        }

        return items;
    }

    /**
     * Parses the order string to generate an SQL ORDER BY clause.
     *
     * @param order the order string
     * @return the SQL ORDER BY clause
     */
    private static String parseOrder(String order) {
        if (order == null) {
            // Default ordering if order is null
            return " ORDER BY MaterialName ASC";
        } else if (order.isEmpty()) {
            // Do nothing if order is an empty string
            return "";
        } else {
            // If order is provided, filter rows where MaterialName contains the substring
            return " AND MaterialName LIKE '%" + order.replace("'", "''") + "%'";
        }
    }

    /**
     * Adds an item to the database. If the item already exists, updates the count.
     *
     * @param item the item to add
     * @throws SQLException if an error occurs while adding the item
     */
    public synchronized void addItem(SISItem item) throws SQLException {
        //1. check if the item already exists
        String queryCheck = "SELECT count FROM SIS_Items WHERE data = '" + item.getData() + "' AND ServerId = " + item.getServerId() + ";";
        // 1 row expected or 0 since we have a unique constraint
        var resultSet = this.connection.createStatement().executeQuery(queryCheck);
        if (resultSet.next()) {
            //2. if it does, update the count
            int count = resultSet.getInt("count") + item.getCount();
            String queryUpdate = "UPDATE SIS_Items SET count = " + count + " WHERE MaterialName = '" + item.getMaterialName() + "' AND ServerId = " + item.getServerId() + ";";
            this.connection.createStatement().executeUpdate(queryUpdate);
            return;
        }

        //3. if it doesn't, insert the item
        String query = "INSERT INTO SIS_Items (MaterialName,ServerId,count,data) VALUES (\n" +
                       "  '" + item.getMaterialName() + "',\n" +
                       "  " + item.getServerId() + ",\n" +
                       "  " + item.getCount() + ",\n" +
                       "  '" + item.getData() + "'\n" +
                       ");";
        this.connection.createStatement().executeUpdate(query);
    }

    /**
     * Checks if a server contains a specific item.
     *
     * @param server the server to check
     * @param itemData the data of the item to check
     * @return true if the item is found, false otherwise
     * @throws SQLException if an error occurs while checking the item
     */
    public synchronized boolean containsItem(SISServer server, String itemData) throws SQLException {
        String query = "SELECT count(*) FROM SIS_Items WHERE ServerId = " + server.getId() + " AND data = '" + itemData + "';";
        var resultSet = this.connection.createStatement().executeQuery(query);
        return resultSet.getInt(1) > 0;
    }

    /**
     * Removes an item from a server.
     *
     * @param server the server to remove the item from
     * @param itemData the data of the item to remove
     * @throws Exception if an error occurs while removing the item
     */
    public void removeItem(SISServer server, String itemData) throws Exception {
        this.removeItem(server, itemData, 1);
    }

    /**
     * Removes a specified quantity of an item from a server.
     *
     * @param server the server to remove the item from
     * @param itemData the data of the item to remove
     * @param count the quantity of the item to remove
     * @throws Exception if an error occurs while removing the item
     */
    public synchronized void removeItem(SISServer server, String itemData, int count) throws Exception {
        //1. get the count of the item
        int currentCount = this.getAmoutOfItem(server, itemData);

        if (currentCount - count < 0) {
            throw new Exception("Not enough items");
        }

        if (currentCount == 0) {
            throw new Exception("Item not found");
        }

        //2. update the count
        String query = "UPDATE SIS_Items SET count = " + (currentCount - count) + " WHERE ServerId = " + server.getId() + " AND data = '" + itemData + "';";
        this.connection.createStatement().executeUpdate(query);
    }

    /**
     * Retrieves the quantity of a specific item from a server.
     *
     * @param server the server to check
     * @param itemData the data of the item to check
     * @return the quantity of the item
     * @throws SQLException if an error occurs while retrieving the item quantity
     */
    private synchronized int getAmoutOfItem(SISServer server, String itemData) throws SQLException {
        String query = "SELECT count FROM SIS_Items WHERE ServerId = " + server.getId() + " AND data = '" + itemData + "';";
        var resultSet = this.connection.createStatement().executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count");
        } else {
            return 0;
        }
    }
}