package io.lumine.mythic.lib.util.loadingorder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DependencyCycleCheck {
    public final Map<String, DependencyNode> registry = new HashMap<>();
    public final boolean[] visited, inStack;
    public final Stack<DependencyNode> stack = new Stack<>();

    public DependencyCycleCheck() {

        // Register plugins
        int counter = 0;
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            final DependencyNode loaded = new DependencyNode(counter, plugin);
            registry.put(loaded.name, loaded);
            counter++;
        }

        // Load adjacencies
        registry.values().forEach(pl -> pl.loadAdjacency(this));

        // Initialize table
        visited = new boolean[registry.size()];
        inStack = new boolean[registry.size()];
    }

    // Function to check for the cycle.
    @Nullable
    public Stack<DependencyNode> checkCycle() {

        // Iterating for i = 0 To i = V - 1
        // to detect cycle in different
        // DFS trees.
        for (DependencyNode root : registry.values()) {
            final DependencyNode check = recursiveCheck(root);
            if (check != null) {
                stack.add(check);
                return stack;
            }
        }

        // Returning false, if no cycle is found.
        return null;
    }

    @Nullable
    private DependencyNode recursiveCheck(DependencyNode plugin) {
        // Check if node exists in the
        // recursive stack.
        if (inStack[plugin.id]) return plugin;

        // Check if node is already visited.
        if (visited[plugin.id]) return null;

        // Marking node as visited.
        visited[plugin.id] = true;

        // Marking node to be present in
        // recursive stack.
        inStack[plugin.id] = true;
        stack.add(plugin);

        // Iterate for all adjacent of
        // 'node'.
        for (DependencyNode dep : plugin.children.values()) {
            // Recurse for 'v'.
            final DependencyNode check = recursiveCheck(dep);
            if (check != null) return check;
        }

        // Mark 'node' to be removed
        // from the recursive stack.
        inStack[plugin.id] = false;
        stack.pop();

        // Return false if no cycle exists.
        return null;
    }
}
