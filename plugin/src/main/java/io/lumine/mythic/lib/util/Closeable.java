package io.lumine.mythic.lib.util;

/**
 * Used everywhere in the MMO plugins. Closeable objects are objects
 * which contain temporary information or features which must be
 * canceled or closed when disposing of the object.
 * - abilities running on a timer (need to cancel the bukkit task)
 * - player data instances holding Bukkit objects...
 *
 * @author jules
 */
public interface Closeable {

    void close();
}
