package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class CommandHandler {
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

    public static boolean test(CommandSender sender, Command command, String label, String[] args, SimpleItemStorage plugin) {
        sender.sendMessage(plugin.getConfig().getString("test"));

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null) {
            player.sendMessage("You are not holding any item");
            return true;
        } else {
            String itemName = XMaterial.matchXMaterial(itemInHand.getType()).name();
            sender.sendMessage("You are holding " + itemName + " x" + itemInHand.getAmount());
        }

        return true;
    }
}
