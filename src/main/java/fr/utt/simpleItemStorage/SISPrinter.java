package fr.utt.simpleItemStorage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SISPrinter {
    private static boolean isDebugmode () {
        return SimpleItemStorage.getInstance().getConfig().getBoolean("debug");
    }

    public static void printDebug (String message) {
        if (isDebugmode()) {
            SimpleItemStorage.getInstance().getLogger().info("[DEBUG] " + message);
        }
    }
    public static void printDebugError (String message) {
        if (isDebugmode()) {
            SimpleItemStorage.getInstance().getLogger().severe("[DEBUG] " + message);
        }
    }
    public static void printDebugPlayer (String message, Player player) {
        if (isDebugmode()) {
            player.sendMessage("[DEBUG] " + message);
        }
    }
    public static void printDebugPlayer (String message, CommandSender sender) {
        if (sender instanceof Player) {
            printDebugPlayer(message, (Player) sender);
        }
    }

    public static void print (String message) {
        SimpleItemStorage.getInstance().getLogger().info(message);
    }
    public static void printError (String message) {
        SimpleItemStorage.getInstance().getLogger().severe(message);
    }
    public static void printPlayer (String message, Player player) {
        player.sendMessage(message);
    }
    public static void printPlayer (String message, CommandSender sender) {
        if (sender instanceof Player) {
            printPlayer(message, (Player) sender);
        }
    }
}
