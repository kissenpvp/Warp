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

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The DeleteWarp class containing a command to delete a global warp.
 *
 * <p>The {@code DeleteWarp} class defines a command named "warpdelete" (with an alias "deletewarp")
 * for players to delete global warp locations. It also includes a tab completer for the "warpdelete" command.
 *
 * @see CommandData
 * @see CommandTarget
 * @see TabCompleter
 * @see LocationNode
 * @see Player
 * @see Warp
 */
public class DeleteWarp {

    /**
     * Command handler for deleting a global warp.
     *
     * <p>The {@code deleteWarpCommand} method is a command handler that allows players to delete global
     * warp locations. It takes a {@link CommandPayload} containing the sender and the warp name as an argument.
     * The command removes the global warp with the specified name from the cache and saves the updated cache.
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param warpName       the name of the global warp to be deleted
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see LocationNode
     * @see Player
     * @see Warp
     */
    @CommandData(value = "warpdelete", description = "Removes the specified warp from the list of available warps.", aliases = "deletewarp", target = CommandTarget.PLAYER)
    public void deleteWarpCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("warp") String warpName) {
        Component warpComponent = Component.text(warpName);
        if(Warp.getPlugin(Warp.class).getWarps().removeIf(warp -> warp.name().equals(warpName)))
        {
            commandPayload.getSender().sendMessage(Component.translatable("server.warp.delete.success", warpComponent));
            return;
        }
        throw new OperationException(Component.translatable("server.warp.warpname.invalid", warpComponent));
    }

    /**
     * Tab completer for the "warpdelete" command.
     *
     * <p>The {@code deleteWarpTabCompleter} method provides tab completion suggestions for the "warpdelete" command.
     * It returns a set of global warp names from the cache.</p>
     *
     * @return an unmodifiable set of global warp names for tab completion
     * @see TabCompleter
     * @see LocationNode
     * @see Warp
     */
    @TabCompleter("warpdelete")
    public @NotNull @Unmodifiable Set<String> deleteWarpTabCompleter()
    {
        return Warp.getPlugin(Warp.class).getWarps().stream().map(LocationNode::name).collect(Collectors.toUnmodifiableSet());
    }
}
