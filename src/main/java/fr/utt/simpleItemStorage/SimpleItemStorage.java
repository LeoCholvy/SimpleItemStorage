package fr.utt.simpleItemStorage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import static fr.utt.simpleItemStorage.CommandHandler.*;

public final class SimpleItemStorage extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("SimpleItemStorage has been enabled");
        this.getLogger().info("Hello World!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("SimpleItemStorage has been disabled");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label) {
            case "hello":
                sender.sendMessage("Hello World!");
                return true;
            case "hi":
                return hi(sender, command, label, args);
            case "simpleitemstorage:reload", "simpleitemstorage:rl":
                return reload(sender, command, label, args, this);
            default:
                return false;
        }
    }
}
