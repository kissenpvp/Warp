package net.kissenpvp;

import net.kissenpvp.commands.home.*;
import net.kissenpvp.commands.warp.DeleteWarp;
import net.kissenpvp.commands.warp.ListWarp;
import net.kissenpvp.commands.warp.SetWarp;
import net.kissenpvp.commands.warp.WarpCommand;
import net.kissenpvp.core.api.command.exception.OperationException;
import net.kissenpvp.core.api.database.connection.DatabaseConnection;
import net.kissenpvp.core.api.database.connection.DatabaseImplementation;
import net.kissenpvp.core.api.database.meta.Table;
import net.kissenpvp.core.api.database.meta.list.MetaList;
import net.kissenpvp.core.api.database.savable.SavableMap;
import net.kissenpvp.core.api.util.PageBuilder;
import net.kissenpvp.paper.api.base.Context;
import net.kissenpvp.visual.api.theme.ThemeProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The Warp class, extending {@link JavaPlugin}.
 *
 * <p>The {@code Warp} class serves as a Bukkit plugin for managing warp locations within the server.
 * It extends {@link JavaPlugin} to utilize Bukkit's plugin functionality.</p>
 *
 * @see JavaPlugin
 */
public class Warp extends JavaPlugin {

    private static final String COMMAND_TEMPLATE;
    private static final TranslatableComponent LIST_ENTRY;
    private static final TranslatableComponent TELEPORT_COMPONENT;

    static {
        COMMAND_TEMPLATE = "/%s %s";
        LIST_ENTRY = Component.translatable("server.home.list.entry");
        TELEPORT_COMPONENT = Component.translatable("server.home.teleport.chat");
    }

    private MetaList<LocationNode> warpList;

    private static @NotNull Table getTable() {
        DatabaseImplementation databaseImplementation = Bukkit.getKissen().getImplementation(DatabaseImplementation.class);
        DatabaseConnection connection = databaseImplementation.getConnection("private").orElse(databaseImplementation.getPrimaryConnection());
        return connection.createTable("warp_table");
    }

    public static @NotNull SavableMap getRepository(@NotNull Player player) {
        return player.getUser(Context.LOCAL).getRepository(Warp.getPlugin(Warp.class));
    }

    @Override
    public void onEnable() {
        String homePrefix = "The home ";
        String warpPrefix = "The warp ";
        String override = "Do you want to override it? Type /confirm to confirm or /cancel to cancel. This request will expire after 30 seconds.";

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerCommand(this, new SetHome(), new DeleteHome(), new HomeCommand(), new ListHome());
        pluginManager.registerCommand(this, new DeleteWarp(), new SetWarp(), new WarpCommand(), new ListWarp());

        pluginManager.registerSetting(new MaxHomes(), this);

        pluginManager.registerTranslation("server.home.create.success", new MessageFormat("Successfully created the home {0}."), this);
        pluginManager.registerTranslation("server.home.delete.success", new MessageFormat("Successfully deleted the home {0}."), this);
        pluginManager.registerTranslation("server.home.teleport.success", new MessageFormat("Successfully teleported to the home {0}."), this);
        pluginManager.registerTranslation("server.home.homename.invalid", new MessageFormat("You do not have a home referenced by {0}."), this);
        pluginManager.registerTranslation("server.home.name.exist", new MessageFormat(String.format("%s{0} already exists. %s", homePrefix, override)), this);
        pluginManager.registerTranslation("server.home.name.edited", new MessageFormat(homePrefix + "{0} was successfully processed."), this);
        pluginManager.registerTranslation("server.home.list.empty", new MessageFormat("You have no homes."), this);
        pluginManager.registerTranslation("server.home.list.entry", new MessageFormat("{0} {1}"), this);
        pluginManager.registerTranslation("server.home.teleport.chat", new MessageFormat("[Teleport]"), this);
        pluginManager.registerTranslation("server.home.create.maxreached", new MessageFormat("You can not create more than {0} homes."), this);

        pluginManager.registerTranslation("server.warp.create.success", new MessageFormat("Successfully created the warp {0}."), this);
        pluginManager.registerTranslation("server.warp.delete.success", new MessageFormat("Successfully deleted the warp {0}."), this);
        pluginManager.registerTranslation("server.warp.teleport.success", new MessageFormat("Successfully teleported to the warp {0}."), this);
        pluginManager.registerTranslation("server.warp.warpname.invalid", new MessageFormat(warpPrefix + "{0} was not found."), this);
        pluginManager.registerTranslation("server.warp.name.exist", new MessageFormat(String.format("%s{0} already exists. %s", warpPrefix, override)), this);
        pluginManager.registerTranslation("server.warp.name.edited", new MessageFormat(warpPrefix + "{0} was successfully processed."), this);
        pluginManager.registerTranslation("server.warp.list.empty", new MessageFormat("There have no warps available."), this);
        pluginManager.registerTranslation("server.warp.list.entry", new MessageFormat("{0} {1}"), this);
        pluginManager.registerTranslation("server.warp.teleport.chat", new MessageFormat("[Teleport]"), this);
        getTable().registerMeta(this).getCollection("warp_list", LocationNode.class).join();
    }

    /**
     * Validates the specified boolean expression and throws a {@link OperationException} with the provided {@link Component} message
     * if the expression evaluates to false.
     *
     * <p>The {@code validate} method is used to check a given boolean expression. If the expression is false,
     * a {@link OperationException} is thrown with the specified {@link Component} message. This method helps ensure that
     * certain conditions are met before proceeding with further execution.</p>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * // Example 1: Validating a condition
     * validate(x > 0, Component.text("The value must be greater than 0"));
     *
     * // Example 2: Validating a non-null object
     * SomeObject obj = // initialize object
     * validate(obj != null, Component.text("The object must not be null"));
     * }
     * </pre>
     *
     * @param expression the boolean expression to be validated
     * @param message    the {@link Component} message to be included in the {@link OperationException} if the validation fails
     * @throws OperationException   if the specified boolean expression is false
     * @throws NullPointerException if the specified {@link Component} message is `null`
     */
    public void validate(boolean expression, @NotNull Component message) {
        if (!expression) {
            throw new OperationException(message);
        }
    }

    /**
     * The search method to find a location by name and teleport the player to it.
     *
     * <p>The {@code search} method is used to search for a location by name in a collection of {@link LocationNode}.
     * If a match is found, it teleports the specified player to that location and returns true. Otherwise, it returns false.</p>
     *
     * <p>There are two overloaded versions of the {@code search} method:</p>
     * <ul>
     *     <li>The first version without the collection parameter searches in the global cache using {@link #getWarps()}.</li>
     *     <li>The second version with the collection parameter allows searching in a specific collection of {@link LocationNode}.</li>
     * </ul>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * // The player executes a search for the location named "MyWarp" and teleports if found.
     * boolean success = search(player, "MyWarp", Component.text("Teleport successful!"));
     * if (!success) {
     *     player.sendMessage(Component.text("Location not found."));
     * }
     * }
     * </pre>
     *
     * @param player          the player to teleport
     * @param name            the name of the location to search for
     * @param teleportMessage the message to display upon successful teleportation
     * @return true if the location is found and the player is teleported, false otherwise
     * @throws NullPointerException if any of the parameters is {@code null}
     * @see LocationNode
     * @see Player
     */
    public boolean searchLocation(@NotNull Player player, @NotNull String name, @NotNull Component teleportMessage) {
        return searchLocation(player, name, teleportMessage, getWarps());
    }

    /**
     * The search method to find a location by name in a specific collection and teleport the player to it.
     *
     * <p>The {@code search} method is used to search for a location by name in the specified collection of {@link LocationNode}.
     * If a match is found, it teleports the specified player to that location and returns true. Otherwise, it returns false.</p>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * // The player executes a search for the location named "MyHome" in a custom collection and teleports if found.
     * boolean success = search(player, "MyHome", Component.text("Teleport successful!"), customLocationNodes);
     * if (!success) {
     *     player.sendMessage(Component.text("Location not found."));
     * }
     * }
     * </pre>
     *
     * @param player          the player to teleport
     * @param name            the name of the location to search for
     * @param teleportMessage the message to display upon successful teleportation
     * @param nodes           the collection of {@link LocationNode} to search within
     * @return true if the location is found and the player is teleported, false otherwise
     * @throws NullPointerException if any of the parameters is {@code null}
     * @see LocationNode
     * @see Player
     */
    public boolean searchLocation(@NotNull Player player, @NotNull String name, @NotNull Component teleportMessage, @NotNull Collection<LocationNode> nodes) {
        for (LocationNode location : nodes) {
            if (Objects.equals(name, location.name())) {
                teleport(teleportMessage, location, player);
                return true;
            }
        }
        return false;
    }

    /**
     * Teleports the player to the specified home location.
     *
     * <p>The {@code teleport} method handles the teleportation logic for the "home" command. It teleports
     * the player to the specified {@link LocationNode} and provides visual and auditory effects.</p>
     *
     * @param message      the component representing the home name for the teleport message
     * @param locationNode the {@link LocationNode} representing the home location to teleport to
     * @param player       the {@link Player} to be teleported
     * @throws NullPointerException if any of the parameters is {@code null}
     * @see LocationNode
     * @see Player
     */
    private void teleport(@NotNull Component message, @NotNull LocationNode locationNode, @NotNull Player player) {
        player.teleport(locationNode.toLocation());
        player.playSound(player.getLocation(), "minecraft:block.stone.step", SoundCategory.AMBIENT, 1.0f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 255, true));
        player.sendMessage(message);
    }

    /**
     * Generates a {@link Component} for displaying a page of location entries with a specified title and label.
     *
     * <p>The {@code generateComponent} method constructs a {@link Component} containing a header, a list of location entries,
     * and a footer. The entries are retrieved using the provided {@link PageBuilder} for a specific page. Each entry is
     * formatted as a clickable list item with a teleport command associated with it.</p>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * Component title = Component.text("Locations");
     * String label = "Warp: ";
     * PageBuilder<LocationNode> pageBuilder = new LocationPageBuilder();
     * int page = 1;
     *
     * Component generatedComponent = generateComponent(title, label, pageBuilder, page);
     * player.sendMessage(generatedComponent);
     * }
     * </pre>
     *
     * @param title       the title component for the generated component
     * @param label       the label for the list entries
     * @param pageBuilder the {@link PageBuilder} for managing location entries
     * @param page        the page number to generate
     * @return a {@link Component} representing the generated page
     * @throws NullPointerException if any of the parameters (title, label, or pageBuilder) are {@code null}
     * @see PageBuilder
     * @see LocationNode
     */
    public @NotNull Component generateComponent(@NotNull Component title, @NotNull String label, @NotNull PageBuilder<LocationNode> pageBuilder, int page) {
        TextComponent.Builder builder = Component.text().append(pageBuilder.getHeader(title, page)).appendNewline();

        Stream<LocationNode> homes = pageBuilder.getEntries(page).stream();
        builder.append(homes.map(toListEntry(label)).toArray(Component[]::new));

        builder.append(pageBuilder.getFooter(title, page));
        return builder.asComponent();
    }

    /**
     * Converts a {@link LocationNode} to a list entry {@link Component} with a teleport command.
     *
     * <p>The {@code toListEntry} method takes a label and returns a {@link Function} that converts a {@link LocationNode}
     * into a list entry {@link Component}. The entry is clickable, allowing the player to execute a teleport command.</p>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * String label = "Warp: ";
     * Function<LocationNode, Component> listEntryFunction = toListEntry(label);
     * LocationNode home = // get a location node...
     * Component listEntry = listEntryFunction.apply(home);
     * }
     * </pre>
     *
     * @param label the label to be used in the teleport command
     * @return a {@link Function} converting {@link LocationNode} to list entry {@link Component}
     * @throws NullPointerException if the label is {@code null}
     * @see LocationNode
     * @see Component
     */
    private @NotNull Function<LocationNode, Component> toListEntry(@NotNull String label) {
        return location -> {
            String command = COMMAND_TEMPLATE.formatted(label, location.name());
            ClickEvent clickEvent = ClickEvent.runCommand(command);
            Component teleportComponent = TELEPORT_COMPONENT.clickEvent(clickEvent).color(ThemeProvider.general());

            return LIST_ENTRY.arguments(teleportComponent, Component.text(location.name()));
        };
    }

    /**
     * Retrieves an unmodifiable set of {@link LocationNode} objects from the cache.
     *
     * <p>The {@code getCache} method returns an unmodifiable {@link Set} containing {@link LocationNode} objects
     * stored in the cache. Modifying the returned set will result in an {@link UnsupportedOperationException}.</p>
     *
     * @return an unmodifiable {@link Set} of {@link LocationNode} objects from the cache
     * @throws UnsupportedOperationException if attempting to modify the returned set
     * @see LocationNode
     * @see Collections#unmodifiableSet(Set)
     */
    public @NotNull @Unmodifiable MetaList<LocationNode> getWarps() {
        return warpList;
    }
}
