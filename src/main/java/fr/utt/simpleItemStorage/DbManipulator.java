package fr.utt.simpleItemStorage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManipulator {
    private static String url = null;
    private static DbManipulator instance = null;
    private Connection connection = null;
    private static final String currentVersion = "1";
    private DbManipulator() {
        this.url = "jdbc:sqlite:" + SimpleItemStorage.getInstance().getDataFolder() + "/database.db";
        new File(SimpleItemStorage.getInstance().getDataFolder().getPath()).mkdirs();
        this.connect();
        this.initTables();
    }

    public static DbManipulator getInstance() {
        if (instance == null) {
            instance = new DbManipulator();
        }
        return instance;
    }

    private void initTables() {
        try {
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Servers (\n" +
                    "    id INTEGER PRIMARY KEY,\n" +
                    "    owner TEXT NOT NULL,\n" +
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
                    "    FOREIGN KEY (ServerId) REFERENCES SIS_Servers (id)\n" +
                    ");");
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS SIS_Infos (\n" +
                    "    version TEXT NOT NULL\n" +
                    ");");
            var resultSet = this.connection.createStatement().executeQuery("SELECT version FROM SIS_Infos LIMIT 1;");
            if (resultSet.next()) {
                String version = resultSet.getString("version");
                if (!currentVersion.equals(version)) {
                    SimpleItemStorage.getInstance().getLogger().severe("Database version mismatch");
                    throw new SQLException("Database version mismatch");
                }
            }


            this.connection.createStatement().execute("INSERT INTO SIS_Infos (version) VALUES ('" + currentVersion + "');");
            SimpleItemStorage.getInstance().getLogger().info("Tables has been created in the database");
        } catch (SQLException e) {
            SimpleItemStorage.getInstance().getLogger().severe("Failed to create the table");
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            this.connection = DriverManager.getConnection(url);
            SimpleItemStorage.getInstance().getLogger().info("Connection to the database has been established");
        } catch (SQLException e) {
            SimpleItemStorage.getInstance().getLogger().severe("Failed to connect to the database");
            e.printStackTrace();
        }

    }

    public void disconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
                SimpleItemStorage.getInstance().getLogger().info("Connection to the database has been closed");
                instance = null;
            }
        } catch (SQLException e) {
            SimpleItemStorage.getInstance().getLogger().severe("Failed to close the connection to the database");
            e.printStackTrace();
        }
    }
}
