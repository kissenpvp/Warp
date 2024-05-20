package net.kissenpvp.commands.warp;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The SetWarp class containing a command to set a global warp.
 *
 * <p>The {@code SetWarp} class defines a command named "warpset" (with an alias "setwarp") for players
 * to set global warp locations. It uses the provided {@link CommandPayload} to retrieve the sender and warp name,
 * then adds the new global warp or updates an existing one accordingly. It also includes a contract method
 * for creating the {@link Runnable} to be executed when setting the warp.
 *
 * @see CommandData
 * @see CommandTarget
 * @see Runnable
 * @see LocationNode
 * @see Warp
 */
public class SetWarp {


    /**
     * Command handler for setting a global warp.
     *
     * <p>The {@code setWarpCommand} method is a command handler that allows players to set global
     * warp locations. It takes a {@link CommandPayload} containing the sender and the warp name as an
     * argument. The command checks if the warp name already exists and prompts the player to confirm
     * overwriting an existing warp. It then executes the {@link Runnable} returned by the {@code getRunnable}
     * method to set or update the global warp.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param warpName       the name of the global warp to be set or updated
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see Runnable
     * @see LocationNode
     * @see Warp
     */
    @CommandData(value = "warpset", aliases = "setwarp", target = CommandTarget.PLAYER)
    public void setWarpCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("warp") String warpName)
    {
        Player player = (Player) commandPayload.getSender();
        MetaList<LocationNode> warps = Warp.getPlugin(Warp.class).getWarps();

        Component warpComponent = Component.text(warpName);
        LocationNode warp = new LocationNode(warpName, player.getLocation());
        if (warps.stream().anyMatch(home -> home.name().equals(warpName)))
        {
            commandPayload.confirmRequest(() ->
            {
                warps.replaceOrInsert(warp);
                player.sendMessage(Component.translatable("server.warp.name.exist", warpComponent));
            }).suppressMessage(true).send();
            return;
        }

        warps.add(warp);
        player.sendMessage(Component.translatable("server.warp.create.success", warpComponent));
    }
}
