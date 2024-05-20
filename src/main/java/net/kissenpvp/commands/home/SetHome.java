package net.kissenpvp.commands.home;

import net.kissenpvp.LocationNode;
import net.kissenpvp.Warp;
import net.kissenpvp.core.api.command.CommandPayload;
import net.kissenpvp.core.api.command.CommandTarget;
import net.kissenpvp.core.api.command.annotations.ArgumentName;
import net.kissenpvp.core.api.command.annotations.CommandData;
import net.kissenpvp.core.api.command.exception.OperationException;
import net.kissenpvp.core.api.config.ConfigurationImplementation;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import net.kissenpvp.core.api.database.savable.SavableMap;
import net.kissenpvp.paper.api.base.Context;
import net.kissenpvp.paper.api.permission.PaperPermission;
import net.kissenpvp.paper.api.permission.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The SetHome class containing a command to set a player's home.
 *
 * <p>The {@code SetHome} class defines a command named "homeset" (with aliases "sethome") for players
 * to set their home locations. It uses the provided {@link CommandPayload} and the home name to create
 * a new {@link LocationNode} and manage the player's home list.
 *
 * @see CommandData
 * @see CommandTarget
 */
public class SetHome {

    private static final String PERMISSION_PREFIX = "kissen.command.homeset.";

    /**
     * Command handler for setting a player's home.
     *
     * <p>The {@code setHomeCommand} method is a command handler that allows players to set their home
     * locations. It takes a {@link CommandPayload} containing the sender and an argument representing
     * the home name. The command checks permissions, existing home names, and manages the player's
     * home list accordingly.</p>
     *
     * @param commandPayload the {@link CommandPayload} containing the sender and arguments
     * @param homeName       the name of the home to be set
     * @throws ClassCastException if the sender is not a {@link Player}
     * @see CommandData
     * @see CommandTarget
     * @see LocationNode
     * @see MetaList
     */
    @CommandData(value = "homeset", aliases = "sethome", target = CommandTarget.PLAYER)
    public void setHomeCommand(@NotNull CommandPayload<CommandSender> commandPayload, @ArgumentName("home") String homeName) {
        Player player = (Player) commandPayload.getSender();

        SavableMap repository = player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
        MetaList<LocationNode> homeList = repository.getListNotNull("home_list", LocationNode.class);

        test(player, homeList); // throws if max homes reached

        LocationNode locationNode = new LocationNode(homeName, player.getLocation());
        Runnable runnable = () -> {
            if (homeList.replace(locationNode)!=0) {
                player.sendMessage(Component.translatable("server.home.name.edited", Component.text(homeName)));
                return;
            }
            homeList.add(locationNode);
            player.sendMessage(Component.translatable("server.home.create.success", Component.text(homeName)));
        };

        if (homeList.stream().anyMatch(home -> home.name().equals(homeName))) {
            player.sendMessage(Component.translatable("server.home.name.exist", Component.text(homeName)));
            commandPayload.confirmRequest(runnable).suppressMessage(true).send();
            return;
        }

        runnable.run();
    }

    /**
     * Tests whether a player is allowed to create a new home in a given list of locations.
     *
     * <p>The {@code test} method checks if the player has the required permission to create a new home.
     * If the player has the specific permission for the next home, the method returns true. Otherwise,
     * it checks if the player has reached the maximum allowed homes based on their permissions. If the
     * maximum is not exceeded, the method returns true; otherwise, it sends a message to the player
     * indicating that the maximum number of homes is reached and returns false.</p>
     *
     * @param player the Bukkit {@link Player} attempting to create a new home
     * @param list   the {@link MetaList} of {@link LocationNode} representing existing homes
     * @throws OperationException if the user already has all his homes set.
     * @see Player
     * @see MetaList
     * @see LocationNode
     */
    private void test(@NotNull Player player, @NotNull MetaList<LocationNode> list) throws OperationException {
        if (player.hasPermission(PERMISSION_PREFIX + list.size() + 1)) {
            return;
        }

        int maxHomes = getMaxHomes(player);
        if (maxHomes >= list.size() + 1) {
            return;
        }

        throw new OperationException(Component.translatable("server.home.create.maxreached", Component.text(maxHomes)));
    }

    /**
     * Retrieves the maximum number of homes a player can have based on their permissions.
     *
     * <p>The {@code getMaxHomes} method calculates the maximum number of homes a player can have by
     * inspecting their Paper permissions. It filters, maps, and sorts the relevant permissions, then
     * returns the highest number. If no valid home permissions are found, it returns 0 (TODO optionally, a default value could be considered in the future).
     * </p>
     *
     * @param player the Bukkit {@link Player} for whom to retrieve the maximum number of homes
     * @return the maximum number of homes the player can have
     * @throws NullPointerException if the player is {@code null}
     * @see Player
     */
    private int getMaxHomes(@NotNull Player player) {
        Set<Permission> permissions = player.getPermissionList();
        List<Integer> numbers = permissions.stream().filter(isHomePermission()).map(cutPrefix()).sorted().toList();

        if (numbers.isEmpty()) {
            ConfigurationImplementation config = Bukkit.getKissen().getImplementation(ConfigurationImplementation.class);
            return config.getSetting(MaxHomes.class);
        }

        return numbers.getLast();
    }

    /**
     * Returns a {@link Function} to extract an integer from a {@link Permission} by cutting the prefix.
     *
     * <p>The {@code cutPrefix} method returns a {@link Function} that takes a {@link Permission} and
     * extracts an integer by removing the specified prefix. This function is pure and does not modify the
     * original permission object.</p>
     *
     * @return a pure {@link Function} extracting an integer from a {@link Permission} by cutting the prefix
     * @see Permission
     */
    @Contract(pure = true, value = "-> new")
    private @NotNull Function<Permission, Integer> cutPrefix() {
        return permission -> {
            String number = permission.getName().substring(PERMISSION_PREFIX.length());
            return Integer.parseInt(number); // safe
        };
    }

    /**
     * Returns a {@link Predicate} to check if a {@link Permission} represents a home permission.
     *
     * <p>The {@code isHomePermission} method returns a {@link Predicate} that checks if a {@link Permission}
     * represents a home permission. It verifies the permission name starts with the specified prefix and is valid,
     * and attempts to parse the remaining part as an integer. This predicate is pure and does not modify the
     * original permission object.</p>
     *
     * @return a pure {@link Predicate} checking if a {@link Permission} represents a home permission
     * @see Permission
     */
    @Contract(pure = true, value = "-> new")
    private @NotNull Predicate<Permission> isHomePermission() {
        return paperPermission -> {
            if (!paperPermission.getName().startsWith(PERMISSION_PREFIX) || !paperPermission.isValid()) {
                return false;
            }

            try {
                Integer.parseInt(paperPermission.getName().substring(PERMISSION_PREFIX.length()));
                return true;
            } catch (NumberFormatException ignored) {
            }
            return false;
        };
    }
}
