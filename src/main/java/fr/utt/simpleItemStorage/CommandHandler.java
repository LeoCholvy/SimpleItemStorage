package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

            plugin.getLogger().info("SimpleItemStorage has been reloaded");
            sender.sendMessage("SimpleItemStorage has been reloaded");

            return true;
        } catch (Exception e) {
            sender.sendMessage("An error occurred while reloading the plugin");
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
                    sender.sendMessage("Server " + serverName + " has been added");
                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while adding the server");
                    e.printStackTrace();
                    sender.sendMessage("An error occurred while adding the server");
                    return false;
                }
            case "remove":

                // FIXME : destroy all the sessions of the server and terminals of the server

                if (args.length < 2) {
                    sender.sendMessage("Usage: /server remove <name>");
                    return false;
                }
                String serverName = args[1];

                try {
                    SISServer server = SISServer.getServer(serverName);
                    if (server == null) {
                        sender.sendMessage("Server " + serverName + " not found");
                        return false;
                    }

                    server.remove();
                    sender.sendMessage("Server " + serverName + " has been removed");
                    return true;

                } catch (Exception e) {
                    sender.sendMessage("An error occurred while removing the server");
                    e.printStackTrace();
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while removing the server");
                    return false;
                }
            case "list":
                try {
                    List<SISServer> servers = SISServer.getServers();

                    if (servers.isEmpty()) {
                        sender.sendMessage("No server found");
                        return true;
                    }

                    for (SISServer server : servers) {
                        sender.sendMessage(server.getId());
                    }

                    sender.sendMessage(servers.size() + " server(s) found !");

                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while listing the servers");
                    sender.sendMessage("An error occurred while listing the servers");
                    e.printStackTrace();
                    return false;
                }
            default:
                sender.sendMessage("Usage: /server <add|remove|list> [name]");
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
            sender.sendMessage("This command can only be executed by a player");
            return false;
        }

        if (args.length == 0) {
            // FIXME
            sender.sendMessage("Usage: /terminal <add|remove|list> [name]");
            return false;
        }

        Player player = (Player) sender;
        String action = args[0];
        switch (action) {
            case "open":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /session <open|get|add> [itemData] [count]");
                    return false;
                }

                String serverName = args[1];
                try {
                    SISSession.createSession(player, serverName);
                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while opening the terminal");
                    sender.sendMessage("An error occurred while opening the terminal");
                    e.printStackTrace();
                    return false;
                }
            case "add":
                SISSession session = SISSession.getSession(player);
                if (session == null) {
                    sender.sendMessage("An error occurred while adding the item");
                    return false;
                }

                ItemStack itemStack = player.getInventory().getItemInMainHand();
                String itemName = XMaterial.matchXMaterial(itemStack.getType()).name();
                if (itemName.equalsIgnoreCase("AIR")) {
                    sender.sendMessage("You can't add air to the terminal");
                    return true;
                }
                if (itemName.equalsIgnoreCase("BUNDLE")) {
                    sender.sendMessage("You can't add bundles to the terminal");
                    return true;
                }

                session.addItem(SISItem.toSISItem(itemStack, session.getServer()));

                // remove the item from the player inventory
                player.getInventory().setItemInMainHand(new ItemStack(XMaterial.AIR.parseMaterial()));
                return true;
            case "get":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /session <open|get|add> [itemData] [count]");
                    return false;
                }

                String itemData = args[1];
                int count = 1;
                if (args.length == 3) {
                    count = Integer.parseInt(args[2]);
                }
                session = SISSession.getSession(player);
                if (session == null) {
                    sender.sendMessage("An error occurred while getting the item");
                    return false;
                }

                try {
                    // check if the item exists in the server
                    if (!session.containsItem(itemData)) {
                        sender.sendMessage("Item not found");
                        return false;
                    }

                    // update the db
                    session.removeItem(itemData, count);

                } catch (SQLException e) {
                    sender.sendMessage("An error occurred while getting the item");
                    e.printStackTrace();
                    return false;
                }


            default:
                sender.sendMessage("Usage: /<command> <open|get|add> [itemData]");
                return false;
        }
    }
}