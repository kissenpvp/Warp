package net.kissenpvp.commands.home;

import net.kissenpvp.core.api.config.options.OptionInteger;
import org.jetbrains.annotations.NotNull;

/**
 * A configuration option representing the maximum number of homes a player can have when no permission is set.
 *
 */
public class MaxHomes extends OptionInteger {
    @Override
    public @NotNull String getGroup() {
        return "homes";
    }

    @Override
    public @NotNull String getDescription() {
        return "This setting regulates how much homes can a player have, when no permission is set.";
    }

    @Override
    public @NotNull Integer getDefault() {
        return 3;
    }
}
