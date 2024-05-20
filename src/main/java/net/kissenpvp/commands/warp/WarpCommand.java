package net.kissenpvp.commands.warp;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.command.annotations.TabCompleter;
import net.kissenpvp.core.api.command.exception.OperationException;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The WarpCommand class containing commands related to teleporting to global warps.
 *
 * <p>The {@code WarpCommand} class defines a command named "warp" for players to teleport to global warp locations.
 * It includes command handlers for the "warp" command and a tab completer for providing warp names.
 *
 * @see CommandData
 * @see CommandTarget
 * @see TabCompleter
 * @see LocationNode
 * @see Player
 * @see Warp
 */
public class WarpCommand {

    /**
     * Command handler for teleporting to a global warp.
     *
     * <p>The {@code warpCommand} method is a command handler that allows players to teleport to a global
     * warp location. It takes a {@link CommandPayload} containing the sender and the warp name as an argument.
     * The command searches for the specified warp in the global warp cache and teleports the player if found,
     * displaying success or error messages accordingly.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param warpName       the name of the global warp to teleport to
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see LocationNode
     * @see Player
     * @see Warp
     */
    @CommandData(value = "warp", target = CommandTarget.PLAYER)
    public void warpCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("warp") String warpName) {
        Player player = (Player) commandPayload.getSender();
        Warp plugin = Warp.getPlugin(Warp.class);

        Component name = Component.text(warpName);
        Component message = Component.translatable("server.warp.teleport.success", name);
        if(!plugin.searchLocation(player, warpName, message))
        {
            throw new OperationException(Component.translatable("server.warp.warpname.invalid", name));
        }
    }


    /**
     * Tab completer for the "warp" command.
     *
     * <p>The {@code warpTabCompleter} method provides tab completion suggestions for the "warp" command.
     * It returns an unmodifiable set of global warp names from the cache for tab completion.</p>
     *
     * @return an unmodifiable set of global warp names for tab completion
     * @see TabCompleter
     * @see LocationNode
     * @see Warp
     */
    @TabCompleter("warp")
    public @NotNull @Unmodifiable Set<String> warpTabCompleter() {
        Warp plugin = Warp.getPlugin(Warp.class);
        return plugin.getWarps().stream().map(LocationNode::name).collect(Collectors.toUnmodifiableSet());
    }
}
