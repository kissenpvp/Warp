package net.kissenpvp.commands.home;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.command.annotations.TabCompleter;
import net.kissenpvp.core.api.command.exception.OperationException;
import net.kissenpvp.core.api.database.savable.SavableMap;
import net.kissenpvp.paper.api.base.Context;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The HomeCommand class containing commands to teleport to a player's home.
 *
 * <p>The {@code HomeCommand} class defines a command named "home" for players to teleport to their
 * home locations. It also includes a tab completer for the "home" command and a private method to
 * handle the teleportation logic.
 *
 * @see CommandData
 * @see CommandTarget
 * @see TabCompleter
 * @see LocationNode
 * @see Player
 */
public class HomeCommand {

    private static @NotNull SavableMap getRepository(@NotNull Player player) {
        return player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
    }

    /**
     * Command handler for teleporting to a player's home.
     *
     * <p>The {@code homeCommand} method is a command handler that allows players to teleport to their
     * home locations. It takes a {@link CommandPayload} containing the sender and the home name as an
     * argument. The command checks the player's home list and teleports them if a matching home name
     * is found.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param homeName       the name of the home to teleport to
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see LocationNode
     * @see Player
     */
    @CommandData(value = "home", target = CommandTarget.PLAYER)
    public void homeCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("home") String homeName) {
        Player player = (Player) commandPayload.getSender();
        List<LocationNode> homeList = getRepository(player).getListNotNull("home_list", LocationNode.class);

        Component name = Component.text(homeName);
        Component message = Component.translatable("server.home.teleport.success", name);
        if (!Warp.getPlugin(Warp.class).searchLocation(player, homeName, message, homeList)) {
            throw new OperationException(Component.translatable("server.home.homename.invalid", name));
        }
    }

    /**
     * Tab completer for the "home" command.
     *
     * <p>The {@code homeTabCompleter} method provides tab completion suggestions for the "home" command.
     * It returns a set of home names from the player's home list.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender
     * @return an unmodifiable set of home names for tab completion
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see TabCompleter
     * @see LocationNode
     * @see Player
     */
    @TabCompleter("home")
    public @NotNull @Unmodifiable Set<String> homeTabCompleter(@NotNull CommandPayload<CommandSender> commandPayload) {
        Player player = (Player) commandPayload.getSender();
        return getRepository(player).getListNotNull("home_list", LocationNode.class).stream().map(LocationNode::name).collect(Collectors.toUnmodifiableSet());
    }
}
