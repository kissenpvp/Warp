package net.kissenpvp.commands.warp;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import net.kissenpvp.core.api.util.PageBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The ListWarp class containing a command to list global warps.
 *
 * <p>The {@code ListWarp} class defines a command named "warplist" (with an alias "listwarps") for players
 * to list global warp locations. It uses the provided {@link CommandPayload} to retrieve the global warp cache
 * and displays it in a paginated format using the {@link PageBuilder} and the {@link Warp} plugin.
 *
 * @see CommandData
 * @see CommandTarget
 * @see PageBuilder
 * @see LocationNode
 * @see Warp
 */
public class ListWarp {

    /**
     * Command handler for listing global warps.
     *
     * <p>The {@code listWarpCommand} method is a command handler that allows players to list global
     * warp locations. It takes a {@link CommandPayload} containing the sender and an optional page number
     * to paginate the list. The command retrieves the global warp cache and displays it using the
     * {@link PageBuilder} and the {@link Warp} plugin.</p>
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
    @CommandData(value = "warplist", aliases = "listwarps", target = CommandTarget.PLAYER)
    public void listWarpCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("page") @NotNull Optional<Integer> page)
    {
        Player player = (Player) commandPayload.getSender();
        Warp plugin = Warp.getPlugin(Warp.class);

        MetaList<LocationNode> cache = plugin.getWarps();
        plugin.validate(!cache.isEmpty(), Component.translatable("server.warp.list.empty"));

        Component title = Component.text("Warp");
        PageBuilder<LocationNode> pageBuilder = new PageBuilder<>(cache);

        player.sendMessage(plugin.generateComponent(title, commandPayload.getLabel(), pageBuilder, page.orElse(1)));
    }
}
