package fr.utt.simpleItemStorage;

import fr.utt.simpleItemStorage.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class SimpleItemStorage extends JavaPlugin {
    private static SimpleItemStorage instance = null;
    /**
     * Default constructor using Singleton pattern
     */
    public SimpleItemStorage() {
        super();
        instance = this;
    }
    /**
     * Get the instance of the plugin
     * @return The instance of the plugin
     */
    public static SimpleItemStorage getInstance() {
        return instance;
    }

    /**
     * This method is called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        this.getLogger().info("SimpleItemStorage has been enabled");

        ConfigManipulator.getInstance();
        DbManipulator.getInstance();
    }

    /**
     * This method is called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        this.getLogger().info("SimpleItemStorage has been disabled");

        DbManipulator.getInstance().disconnect();
    }


    /**
     * This method is called when a command is executed by a player
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The label of the command
     * @param args The arguments of the command
     * @return true if the command was successfully executed, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Get the command label in lowercase and remove the plugin name if it is present
        String llabel = label.toLowerCase();
        String l = "simpleitemstorage:";
        if (llabel.startsWith(l)) {
            llabel = llabel.substring(l.length());
        }


        switch (llabel) {
            case "reload":
                return CommandHandler.reload(sender, command, label, args, this);
            case "test":
                try {
                    return CommandHandler.test(sender, command, label, args, this);
                } catch (Exception e) {
                    sender.sendMessage("An error occurred while testing the plugin");
                    e.printStackTrace();
                    return false;
                }
            case "server":
                return CommandHandler.server(sender, command, label, args);
            case "terminal":
                return CommandHandler.terminal(sender, command, label, args);
            default:
                return false;
        }
    }

    /**
     * This method is called when the plugin is reloaded
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        ConfigManipulator.getInstance().reload();
    }
}
