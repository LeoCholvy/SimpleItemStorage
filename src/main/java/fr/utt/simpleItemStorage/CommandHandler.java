package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.sql.SQLException;
import java.util.List;

import static fr.utt.simpleItemStorage.SISPrinter.*;

public class CommandHandler {
    /**
     * Handle the reload command, it just reloads the plugin.
     *
     * @param sender the sender of the command
     * @param command the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @param plugin the plugin
     * @return true if the command has been handled, false otherwise
     */
    public static boolean reload(CommandSender sender, Command command, String label, String[] args, SimpleItemStorage plugin) {
        try {
            PluginManager pluginManager = plugin.getServer().getPluginManager();
            plugin.reloadConfig();
            pluginManager.disablePlugin(plugin);

            pluginManager.enablePlugin(plugin);
            ConfigManipulator.getInstance().reload();

            // plugin.getLogger().info("SimpleItemStorage has been reloaded");
            // sender.sendMessage("SimpleItemStorage has been reloaded");
            print("SimpleItemStorage has been reloaded");
            printPlayer("SimpleItemStorage has been reloaded", sender);

            return true;
        } catch (Exception e) {
            // sender.sendMessage("An error occurred while reloading the plugin");
            printPlayer("§cAn error occurred while reloading the plugin", sender);
            return false;
        }
    }

    /**
     * Handle the server command, which can add, remove, or list servers.
     *
     * @param sender the sender of the command
     * @param command the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return true if the command has been handled, false otherwise
     */
    public static boolean server(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /server <add|remove|list> [name]");
            return false;
        }

        Player player = (Player) sender;
        String action = args[0];
        switch (action) {
            case "add":
                try {
                    String serverName = SISServer.addServer(player);
                    // sender.sendMessage("Server " + serverName + " has been added");
                    printPlayer("Server " + serverName + " has been added", sender);
                    return true;
                } catch (Exception e) {
                    // SimpleItemStorage.getInstance().getLogger().severe("An error occurred while adding the server");
                    printError("An error occurred while adding the server");
                    e.printStackTrace();
                    // sender.sendMessage("An error occurred while adding the server");
                    printPlayer("§cAn error occurred while adding the server", sender);
                    return false;
                }
            case "remove":

                // FIXME : destroy all the sessions of the server and terminals of the server

                if (args.length < 2) {
                    // sender.sendMessage("Usage: /server remove <name>");
                    printPlayer("Usage: /server remove <name>", sender);
                    return false;
                }
                String serverName = args[1];

                try {
                    SISServer server = SISServer.getServer(serverName);
                    if (server == null) {
                        // sender.sendMessage("Server " + serverName + " not found");
                        printPlayer("Server " + serverName + " not found", sender);
                        return false;
                    }

                    server.remove();
                    // sender.sendMessage("Server " + serverName + " has been removed");
                    printPlayer("Server " + serverName + " has been removed", sender);
                    return true;

                } catch (Exception e) {
                    // sender.sendMessage("An error occurred while removing the server");
                    printPlayer("§cAn error occurred while removing the server", sender);
                    e.printStackTrace();
                    // SimpleItemStorage.getInstance().getLogger().severe("An error occurred while removing the server");
                    printError("An error occurred while removing the server");
                    return false;
                }
            case "list":
                try {
                    List<SISServer> servers = SISServer.getServers();

                    if (servers.isEmpty()) {
                        // sender.sendMessage("No server found");
                        printPlayer("No server found", sender);
                        return true;
                    }

                    for (SISServer server : servers) {
                        // sender.sendMessage(server.getId());
                        printPlayer(server.getId(), sender);
                    }

                    // sender.sendMessage(servers.size() + " server(s) found !");
                    printPlayer(servers.size() + " server(s) found !", sender);

                    return true;
                } catch (Exception e) {
                    // SimpleItemStorage.getInstance().getLogger().severe("An error occurred while listing the servers");
                    printError("An error occurred while listing the servers");
                    // sender.sendMessage("An error occurred while listing the servers");
                    printPlayer("§cAn error occurred while listing the servers", sender);
                    e.printStackTrace();
                    return false;
                }
            default:
                // sender.sendMessage("Usage: /server <add|remove|list> [name]");
                printPlayer("Usage: /server <add|remove|list> [name]", sender);
                return false;
        }
    }

    /**
     * Handle the session command, which can open, add, or get items from a session.
     *
     * @param sender the sender of the command
     * @param command the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return true if the command has been handled, false otherwise
     */
    public static boolean session(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // sender.sendMessage("This command can only be executed by a player");
            printPlayer("This command can only be executed by a player", sender);
            return false;
        }

        if (args.length == 0) {
            printPlayer("Usage: /<command> <open|get|add> [itemData] [count]", sender);
            return false;
        }

        Player player = (Player) sender;
        String action = args[0];
        switch (action) {
            case "open":
                if (args.length < 2) {
                    // sender.sendMessage("Usage: /session <open|get|add> [itemData] [count]");
                    printPlayer("Usage: /session <open|get|add> [itemData] [count]", sender);
                    return false;
                }

                String serverName = args[1];
                SISSession session;
                try {
                    session = SISSession.createSession(player, serverName);
                    if (args.length >= 3 && session != null) {
                        String order = args[2];
                        if (order.equalsIgnoreCase("sort")) {
                            order = null;
                        }

                        // player.sendMessage("Order: " + order);
                        printDebugPlayer("Order: " + order, player);

                        session.display(order);
                    }

                    return true;
                } catch (Exception e) {
                    // SimpleItemStorage.getInstance().getLogger().severe("An error occurred while opening the terminal");
                    // sender.sendMessage("An error occurred while opening the terminal");
                    printError("An error occurred while opening the terminal");
                    printDebugPlayer("An error occurred while opening the terminal", player);
                    e.printStackTrace();
                    return false;
                }
                // break;
            case "add":
                session = SISSession.getSession(player);
                if (session == null) {
                    // sender.sendMessage("An error occurred while adding the item");
                    printPlayer("You need to open a terminal first", sender);
                    return false;
                }

                ItemStack itemStack = player.getInventory().getItemInMainHand();
                String itemName = XMaterial.matchXMaterial(itemStack.getType()).name();
                if (itemName.equalsIgnoreCase("AIR")) {
                    // sender.sendMessage("You can't add air to the terminal");
                    printPlayer("You can't add air to the terminal", sender);
                    return true;
                }
                if (itemName.equalsIgnoreCase("BUNDLE")) {
                    // sender.sendMessage("You can't add bundles to the terminal");
                    printPlayer("You can't add bundles to the terminal", sender);
                    return true;
                }

                session.addItem(SISItem.toSISItem(itemStack, session.getServer()));

                // remove the item from the player inventory
                player.getInventory().setItemInMainHand(new ItemStack(XMaterial.AIR.parseMaterial()));
                return true;
                // break;
            case "get":
                if (args.length < 2) {
                    // sender.sendMessage("Usage: /session <open|get|add> [itemData] [count]");
                    printPlayer("Usage: /session <open|get|add> [itemData] [count]", sender);
                    return false;
                }

                String itemData = args[1];
                int count = 1;
                if (args.length == 3) {
                    count = Integer.parseInt(args[2]);
                }
                session = SISSession.getSession(player);
                if (session == null) {
                    // sender.sendMessage("An error occurred while getting the item");
                    printPlayer("You need to open a terminal first", sender);
                    return false;
                }

                try {
                    // check if the item exists in the server
                    if (!session.containsItem(itemData)) {
                        // sender.sendMessage("Item not found");
                        printPlayer("Item not found", sender);
                        return false;
                    }

                    // update the db
                    session.removeItem(itemData, count);

                } catch (SQLException e) {
                    // sender.sendMessage("An error occurred while getting the item");
                    printPlayer("An error occurred while getting the item", sender);
                    printError("An error occurred while getting the item");
                    e.printStackTrace();
                    return false;
                }

                return true;
            default:
                // sender.sendMessage("Usage: /<command> <open|get|add> [itemData]");
                printPlayer("Usage: /<command> <open|get|add> [itemData]", sender);
                return false;
        }
    }

    public static void terminal(CommandSender sender, Command command, String label, String[] args) {
        // if (!(sender instanceof Player)) {
        //     sender.sendMessage("This command can only be executed by a player");
        //     return;
        // }
        // Player player = (Player) sender;
        // ItemStack item = SISTerminal.getTerminalItemStack();
        // player.getInventory().addItem(item);
    }
}