package net.kissenpvp.commands.home;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.command.annotations.TabCompleter;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import net.kissenpvp.core.api.database.savable.SavableMap;
import net.kissenpvp.paper.api.base.Context;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The DeleteHome class containing a command to delete a player's home.
 *
 * <p>The {@code DeleteHome} class defines a command named "homedelete" (with aliases "deletehome",
 * "delhome", "homedel") for players to delete their home locations. It also includes a tab completer
 * for the "homedelete" command.
 *
 * @see CommandData
 * @see CommandTarget
 * @see TabCompleter
 * @see LocationNode
 * @see Player
 */
public class DeleteHome {

    private static @NotNull SavableMap getRepository(@NotNull Player player) {
        return player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
    }

    /**
     * Command handler for deleting a player's home.
     *
     * <p>The {@code deleteHomeCommand} method is a command handler that allows players to delete their
     * home locations. It takes a {@link CommandPayload} containing the sender and the home name as an
     * argument. The command removes the home with the specified name from the player's home list.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param homeName       the name of the home to be deleted
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see LocationNode
     * @see Player
     */
    @CommandData(value = "homedelete", aliases = {"deletehome", "delhome", "homedel"}, target = CommandTarget.PLAYER)
    public void deleteHomeCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("home") String homeName) {
        Player player = (Player) commandPayload.getSender();
        MetaList<LocationNode> homeList = getRepository(player).getListNotNull("home_list", LocationNode.class);
        if (homeList.removeIf(warp -> warp.name().equals(homeName))) {
            player.sendMessage(Component.translatable("server.home.delete.success", Component.text(homeName)));
            return;
        }
        player.sendMessage(Component.translatable("server.home.homename.invalid", Component.text(homeName)));
    }

    /**
     * Tab completer for the "homedelete" command.
     *
     * <p>The {@code deleteHomeTabCompleter} method provides tab completion suggestions for the "homedelete" command.
     * It returns a set of home names from the player's home list.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender
     * @return an unmodifiable set of home names for tab completion
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see TabCompleter
     * @see LocationNode
     * @see Player
     */
    @TabCompleter("homedelete")
    public @NotNull @Unmodifiable Set<String> deleteHomeTabCompleter(@NotNull CommandPayload<CommandSender> commandPayload) {
        Player player = (Player) commandPayload.getSender();
        return getRepository(player).getListNotNull("home_list", LocationNode.class).stream().map(LocationNode::name).collect(Collectors.toUnmodifiableSet());
    }
}
