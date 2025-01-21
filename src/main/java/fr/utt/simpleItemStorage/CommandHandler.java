package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHandler {
    public static boolean hi(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command");
            return false;
        }
        sender.sendMessage("Hi!");
        XMaterial material = XMaterial.DIAMOND;
        ItemStack item = material.parseItem();
        Player player = (Player) sender;
        player.getInventory().addItem(item);
        player.sendMessage("You have received a " + material.name());
        return true;
    }
}
