package net.kissenpvp.commands.home;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.util.PageBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * The ListHome class containing a command to list a player's homes.
 *
 * <p>The {@code ListHome} class defines a command named "homelist" (with aliases "listhomes") for players
 * to list their home locations. It uses the provided {@link CommandPayload} to retrieve the player's home
 * list and displays it in a paginated format using the {@link PageBuilder} and the {@link Warp} plugin.
 *
 * @see CommandData
 * @see CommandTarget
 * @see PageBuilder
 * @see LocationNode
 * @see Warp
 */
public class ListHome {

    /**
     * Command handler for listing a player's homes.
     *
     * <p>The {@code listHomeCommand} method is a command handler that allows players to list their home
     * locations. It takes a {@link CommandPayload} containing the sender and an optional page number to
     * paginate the list. The command retrieves the player's home list and displays it using the {@link PageBuilder}
     * and the {@link Warp} plugin.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param page           the optional page number for pagination
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see PageBuilder
     * @see LocationNode
     * @see Warp
     */
    @CommandData(value = "homelist", aliases = "listhomes", target = CommandTarget.PLAYER)
    public void listHomeCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("page") @NotNull Optional<Integer> page) {
        Player player = (Player) commandPayload.getSender();
        Warp plugin = Warp.getPlugin(Warp.class);

        List<LocationNode> homeList = Warp.getRepository(player).getListNotNull("home_list", LocationNode.class);
        plugin.validate(!homeList.isEmpty(), Component.translatable("server.home.list.empty"));

        Component home = Component.text("Home");
        PageBuilder<LocationNode> pageBuilder = new PageBuilder<>(homeList);

        player.sendMessage(plugin.generateComponent(home, commandPayload.getLabel(), pageBuilder, page.orElse(1)));
    }
}
