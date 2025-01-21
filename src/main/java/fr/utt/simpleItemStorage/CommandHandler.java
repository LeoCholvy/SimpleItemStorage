package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CommandHandler {
    public static boolean hi(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command");
            return false;
        }
        sender.sendMessage("Hi!");
        XMaterial material = XMaterial.DIAMOND;
        ItemStack item = material.parseItem();
        item.setAmount(12);
        Player player = (Player) sender;
        // player.getInventory().addItem(item);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage("You have received a " + material.name());
        return true;
    }


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
}
