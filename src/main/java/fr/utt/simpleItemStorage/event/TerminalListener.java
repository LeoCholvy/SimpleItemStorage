package fr.utt.simpleItemStorage.event;

import fr.utt.simpleItemStorage.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

import static fr.utt.simpleItemStorage.SISPrinter.printDebugPlayer;
import static fr.utt.simpleItemStorage.SISPrinter.printPlayer;

/**
 * Listener class for handling terminal-related events in the SimpleItemStorage plugin.
 */
public class TerminalListener implements Listener {

    /**
     * Handles the BlockBreakEvent to prevent breaking terminal blocks.
     *
     * @param event the BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // FIXME : can't cancel event in async
        // Bukkit.getScheduler().runTaskAsynchronously(SimpleItemStorage.getInstance(), () -> {
            Player player = event.getPlayer();
            printDebugPlayer("BlockBreakEvent", player);
            if (SISTerminal.isTerminalBlock(event.getBlock())) {
                event.setCancelled(true);
            }
        // });
    }

    // TODO : add event for terminal block break (all of them)

    /**
     * Sets the texture of a terminal block using a base64 encoded skin.
     *
     * @param block the block to set the texture for
     */
    private void setTerminalTexture(Block block) {
        SkullCreator.blockWithBase64(block, SimpleItemStorage.getInstance().getConfig().getString("terminal_skin"));
    }

    /**
     * Handles the PlayerInteractEvent to manage interactions with terminal blocks.
     *
     * @param event the PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(SimpleItemStorage.getInstance(), () -> {
            Player player = event.getPlayer();
            printDebugPlayer("PlayerInteractEvent", player);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (SISTerminal.isTerminalBlock(event.getClickedBlock())) {
                    // event.setCancelled(true);
                    SISTerminal terminal = SISTerminal.getTerminal(event.getClickedBlock());
                    if (terminal == null) {
                        printPlayer("Error while getting terminal", player);
                        return;
                    }

                    try {
                        SISSession session = SISSession.createSession(player, terminal.getServerId());
                        if (session == null) {
                            throw new Exception("Session not found");
                        }
                    } catch (Exception e) {
                        printPlayer("Error while getting session", player);
                    }
                }
            }
        });
    }

    /**
     * Handles the BlockPlaceEvent to manage the placement of terminal blocks.
     *
     * @param event the BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (!SISTerminal.isTerminalItem(item)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(SimpleItemStorage.getInstance(), () -> {
            String id = "";
            try {
                id = SISServer.addServer(player);
            } catch (SQLException e) {
                printPlayer("Error while adding server", player);
            }

            try {
                SISTerminal.addTerminal(event.getBlock(), id);
            } catch (SQLException e) {
                printPlayer("Error while adding terminal", player);
            }

            printDebugPlayer("Terminal placed, id = " + id, player);
        });
    }
}