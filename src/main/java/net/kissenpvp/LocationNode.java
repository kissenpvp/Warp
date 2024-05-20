package net.kissenpvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The LocationNode record representing a named location in a specific world with coordinates.
 *
 * <p>The {@code LocationNode} record is an immutable data structure used to represent a named location in a
 * specific world with x, y, and z coordinates. It provides a convenient way to encapsulate and manage
 * location-related information.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * {@code
 * // Create a new LocationNode using a home name and Bukkit Location.
 * String homeName = "Home";
 * Location location = // get a Bukkit Location...
 * LocationNode homeNode = new LocationNode(homeName, location);
 * }
 * </pre>
 *
 * @param name      the name of the location
 * @param worldName the name of the world where the location is situated
 * @param x         the x-coordinate of the location
 * @param y         the y-coordinate of the location
 * @param z         the z-coordinate of the location
 * @see Location
 */
public record LocationNode(@NotNull String name, @NotNull String worldName, double x, double y, double z)
{
    /**
     * Constructs a LocationNode using a home name and a Bukkit Location.
     *
     * <p>The {@code LocationNode} constructor creates a new instance using the provided home name
     * and a Bukkit Location. It extracts the world name and coordinates from the given Location.</p>
     *
     * <p>Example usage:</p>
     *
     * <pre>
     * {@code
     * String name = "Home";
     * Location location = // get a Bukkit Location...
     * LocationNode homeNode = new LocationNode(name, location);
     * }
     * </pre>
     *
     * @param name  the name of the location
     * @param location  the Bukkit Location from which to extract world name and coordinates
     * @throws NullPointerException if either name or location is {@code null}
     * @see Location
     */
    public LocationNode(@NotNull String name, @NotNull Location location)
    {
        this(name, location.getWorld().getName(), location.x(), location.y(), location.z());
    }

    @Contract(" -> new")
    public @NotNull Location toLocation()
    {
        return new Location(Bukkit.getWorld(worldName()), x(), y(), z());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationNode that = (LocationNode) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
