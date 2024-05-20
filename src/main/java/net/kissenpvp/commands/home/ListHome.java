package net.kissenpvp.commands.home;

import com.google.common.base.Preconditions;
import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.database.savable.SavableMap;
import net.kissenpvp.core.api.util.PageBuilder;
import net.kissenpvp.core.api.util.PageImplementation;
import net.kissenpvp.paper.api.base.Context;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

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

    private static @NotNull SavableMap getRepository(@NotNull Player player) {
        return player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
    }

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

        SavableMap repository = player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
        List<LocationNode> homeList = repository.getListNotNull("home_list", LocationNode.class);
        plugin.validate(!homeList.isEmpty(), Component.translatable("server.home.list.empty"));

        Component home = Component.text("Home");
        PageBuilder<LocationNode> pageBuilder = generatePageBuilder(homeList);

        player.sendMessage(plugin.generateComponent(home, commandPayload.getLabel(), pageBuilder, page.orElse(1)));
    }

    /**
     * Generates a {@link PageBuilder} for paginating a list of home locations.
     *
     * <p>The {@code generatePageBuilder} method creates a {@link PageBuilder} using the provided
     * {@link List} of {@link LocationNode}. It utilizes the {@link PageImplementation} from the
     * Kissen framework to implement pagination.</p>
     *
     * @param homeList the list of {@link LocationNode} representing home locations
     * @return a {@link PageBuilder} for paginating the list of home locations
     * @throws NullPointerException if the homeList is {@code null}
     * @see PageBuilder
     * @see LocationNode
     */
    private @NotNull PageBuilder<LocationNode> generatePageBuilder(@NotNull List<LocationNode> homeList) {
        PageImplementation pageImplementation = Bukkit.getKissen().getImplementation(PageImplementation.class);
        return pageImplementation.createPageBuilder(homeList);
    }
}
