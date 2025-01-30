package fr.utt.simpleItemStorage;

import fr.utt.simpleItemStorage.event.TerminalListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.utt.simpleItemStorage.SISPrinter.print;

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
        // this.getLogger().info("SimpleItemStorage has been enabled");
        print("SimpleItemStorage has been enabled");

        ConfigManipulator.getInstance();
        DbManipulator.getInstance();

        this.initTabCompleter();


        List<Listener> listeners = Arrays.asList(new TerminalListener());
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }

        SISRecipes.initRecipes();
    }

    private void initTabCompleter() {
        SISTabCompleter tabCompleter = new SISTabCompleter();
        List<String> commands = Arrays.asList("server", "terminal", "session");
        for (String command : commands) {
            this.getCommand(command).setTabCompleter(tabCompleter);
            this.getCommand("simpleitemstorage:" + command).setTabCompleter(tabCompleter);
        }
    }

    /**
     * This method is called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // this.getLogger().info("SimpleItemStorage has been disabled");
        print("SimpleItemStorage has been disabled");

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
            case "server":
                // Execute the command asynchronously because it may take some time, we need to call the db api
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> CommandHandler.server(sender, command, label, args));
                return true;
            case "session":
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> CommandHandler.session(sender, command, label, args));
                return true;
            case "terminal":
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> CommandHandler.terminal(sender, command, label, args));
                return true;
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
