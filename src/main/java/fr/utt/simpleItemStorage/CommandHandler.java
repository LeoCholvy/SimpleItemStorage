package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    /**
     * Handle the reload command, it just reloads the plugin
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

    //FIXME: This method is just a test, it should be removed
    public static boolean test(CommandSender sender, Command command, String label, String[] args, SimpleItemStorage plugin) throws IOException, ClassNotFoundException, InvalidConfigurationException {
        sender.sendMessage(plugin.getConfig().getString("test"));

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        ItemStack itemInHand;
        try {
            itemInHand = player.getInventory().getItemInMainHand();
        } catch (Exception e) {
            sender.sendMessage("An error occurred while getting the item in hand");
            return false;
        }
        if (XMaterial.matchXMaterial(itemInHand) == XMaterial.AIR) {
            player.sendMessage("You are not holding any item");
        } else if (XMaterial.matchXMaterial(itemInHand).name().contains("BUNDLE")) {
            player.sendMessage("You are holding a bundle and it can't be stored");
        } else {
            Gson gson = new Gson();
            String itemName = gson.toJson(XItemStack.serialize(itemInHand));
            // String itemName = XItemStack.serialize(itemInHand).toString();

            // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            // ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            // objectStream.writeObject(itemInHand);
            // objectStream.close();
            // String base64Item = Base64.getEncoder().encodeToString(byteStream.toByteArray());
            // String itemData = base64Item;

            // NBTItem nbtItem = new NBTItem(itemInHand);
            // String itemData = nbtItem.toString();

            // YamlConfiguration yaml = new YamlConfiguration();
            // yaml.set("item", itemInHand);
            // StringWriter writer = new StringWriter();
            // yaml.save(String.valueOf(writer));
            // String itemData = writer.toString();

            sender.sendMessage("You are holding " + itemName + " x" + itemInHand.getAmount());

            // copy the item in hand with the two string
            Map<String, Object> itemDataMap = gson.fromJson(itemName, new TypeToken<Map<String,Object>>(){}.getType());
            ItemStack copiedItem = XItemStack.deserialize(itemDataMap);
            // add nbttags

            // NBTContainer nbtContainer = new NBTContainer(itemData);
            // ItemStack copiedItem = NBTItem.convertNBTtoItem(nbtContainer);

            // byte[] itemDataBytes = Base64.getDecoder().decode(itemData);
            // ByteArrayInputStream newByteStream = new ByteArrayInputStream(itemDataBytes);
            // ObjectInputStream objectInput = new ObjectInputStream(newByteStream);
            // ItemStack copiedItem = (ItemStack) objectInput.readObject();
            // objectInput.close();

            // ItemStack copiedItem = XItemStack.deserialize();

            // YamlConfiguration yaml2 = new YamlConfiguration();
            // yaml.loadFromString(itemData);
            // ItemStack copiedItem = yaml.getItemStack("item");

            player.getInventory().addItem(copiedItem);
        }

        return true;
    }

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
                    String serverName = DbManipulator.getInstance().addServer(player);
                    sender.sendMessage("Server " + serverName + " has been added");

                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while adding the server");
                    sender.sendMessage("An error occurred while adding the server");
                    e.printStackTrace();
                    return false;
                }
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /server remove <name>");
                    return false;
                }

                String serverName = args[1];
                try {
                    DbManipulator.getInstance().removeServer(serverName);
                    sender.sendMessage("Server " + serverName + " has been removed");

                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while removing the server");
                    sender.sendMessage("An error occurred while removing the server");
                    e.printStackTrace();
                    return false;
                }
            case "list":
                try {
                    List<String> servers = DbManipulator.getInstance().getServersNames();
                    if (servers.isEmpty()) {
                        sender.sendMessage("No server found");
                        return true;
                    }

                    for (String server : servers) {
                        sender.sendMessage(server);
                    }

                    sender.sendMessage(servers.size() + " server(s) found !");

                    return true;
                } catch (Exception e) {
                    SimpleItemStorage.getInstance().getLogger().severe("An error occurred while getting the servers names");
                    sender.sendMessage("An error occurred while getting the servers names");
                    return false;
                }
            default:
                sender.sendMessage("Usage: /server <add|remove|list> [name]");
                return false;
        }
    }

    public static boolean terminal(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
