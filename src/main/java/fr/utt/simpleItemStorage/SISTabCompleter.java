package fr.utt.simpleItemStorage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The SISTabCompleter class provides tab completion for commands in the SimpleItemStorage plugin.
 */
public class SISTabCompleter implements TabCompleter {
    /**
     * Handles tab completion for the specified command.
     *
     * @param commandSender the sender of the command
     * @param command the command
     * @param s the alias of the command
     * @param strings the arguments passed to the command
     * @return a list of possible completions for the final argument
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        String prefix = "simpleitemstorage:";

        if (isCommand(command.getName(), "server")) {
            if (strings.length == 1) {
                completions.add("add");
                completions.add("remove");
                completions.add("list");
            }
            if (strings.length == 2) {
                if (strings[0].equalsIgnoreCase("remove")) {
                    try {
                        SISServer.getServers().forEach(server -> completions.add(server.getId()));
                    } catch (SQLException e) {
                        // Handle exception (optional logging)
                    }
                }
            }
        } else if (isCommand(command.getName(), "session")) {
            if (strings.length == 1) {
                completions.add("open");
                completions.add("get");
                completions.add("add");
            }
            if (strings.length == 2) {
                if (strings[0].equalsIgnoreCase("open")) {
                    try {
                        SISServer.getServers().forEach(server -> completions.add(server.getId()));
                    } catch (SQLException e) {
                        // Handle exception (optional logging)
                    }
                }
                if (strings[0].equalsIgnoreCase("get")) {
                    if (commandSender instanceof Player) {
                        Player player = (Player) commandSender;
                        SISSession session = SISSession.getSession(player);
                        if (session != null) {
                            SISServer server = session.getServer();
                            try {
                                server.getItems().forEach(item -> completions.add(item.getData()));
                            } catch (SQLException e) {
                                // Handle exception (optional logging)
                            }
                        }
                    }
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase()))
                .toList();
    }

    /**
     * Checks if the specified command matches the given name, with or without the prefix.
     *
     * @param command the command to check
     * @param name the name to match
     * @return true if the command matches the name, false otherwise
     */
    private boolean isCommand(String command, String name) {
        String prefix = "simpleitemstorage:";
        return command.equalsIgnoreCase(prefix + name) || command.equalsIgnoreCase(name);
    }
}