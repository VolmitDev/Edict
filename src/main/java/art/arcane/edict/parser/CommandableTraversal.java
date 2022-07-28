package art.arcane.edict.parser;

import art.arcane.edict.Edict;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.user.User;
import art.arcane.edict.virtual.VClass;
import art.arcane.edict.virtual.VCommandable;
import edu.gatech.gtri.bktree.BkTreeSearcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * @param input
 * @param remaining
 * @param result
 * @param depth
 */
public record CommandableTraversal(@NotNull String input, @NotNull List<String> remaining, @NotNull VCommandable result, int depth) {

    /**
     * Command cache.
     */
    private static final ConcurrentHashMap<Query, CommandableTraversal> commandCache = new ConcurrentHashMap<>();

    /**
     * Finds the best match for a certain input, user and root. Removes the head of the input since we are already at the root.
     * @param root the root to search from
     * @param input the input to search with
     * @param user the user to search for
     * @param system the system to search in
     * @return the best matching commandable traversal for the indicated depth search.
     */
    public static @NotNull CommandableTraversal find(@NotNull VClass root, @NotNull String input, @NotNull User user, @NotNull Edict system) {
        List<String> inSplit = List.of(cleanCommand(input).split(" "));
        Query query = new Query(root, inSplit.subList(1, inSplit.size() - 1));
        system.d(new StringMessage("Running query: '" + String.join(", ", query.input) + "' on '" + query.root.name() + "'"));
        return commandCache.computeIfAbsent(query, (in) -> {
            List<CommandableTraversal> c = find(in, user, system);
            system.d(new StringMessage("Found: '" + String.join(" ", c.stream().map(t -> t.result.name()).toList()) + "'"));
            int deepest = -1;
            CommandableTraversal deepestT = null;
            for (CommandableTraversal commandableTraversal : c) {
                if (commandableTraversal.depth > deepest) {
                    deepest = commandableTraversal.depth;
                    deepestT = commandableTraversal;
                }
            }
            if (deepestT == null) {
                system.d(new StringMessage("None found so making blank"));
                deepestT = new CommandableTraversal(
                        input,
                        query.input(),
                        root,
                        0
                );
            }
            system.d(new StringMessage("Deepest: '" + deepestT.result.name() + "' with depth " + deepestT.depth));
            return deepestT;
        });
    }

    /**
     * Find the next node in the traversal.
     * @param query the query for the remaining traversal
     * @param user the user
     * @param system the system
     * @return the traversal endpoints (or null if there are no successful ones)
     */
    private static @NotNull List<CommandableTraversal> find(@NotNull Query query, @NotNull User user, @NotNull Edict system) {
        List<CommandableTraversal> paths = new ArrayList<>();

        if (query.input.isEmpty()) {
            return List.of(new CommandableTraversal(
                    "",
                    new ArrayList<>(),
                    query.root,
                    0
            ));
        }

        List<VCommandable> next = findNextFor(query.root, query.input.get(0), user, system);
        system.d(new StringMessage("Next options: '" + String.join(" ", next.stream().map(VCommandable::name).toList()) + "'"));
        next.forEach(possible -> {
            if (possible instanceof VClass c) {
                find(new Query(c, query.input.subList(1, query.input.size() - 1)), user, system).forEach(f ->
                        paths.add(new CommandableTraversal(
                                "",
                                f.remaining,
                                f.result,
                                f.depth + 1
                        ))
                );
            } else {
                paths.add(new CommandableTraversal(
                        "",
                        query.input.subList(1, query.input.size() - 1),
                        possible,
                        1
                ));
            }
        });

        return paths;
    }

    /**
     * Find the next possible {@link VCommandable} for a node.
     * @param current the current node
     * @param input the input for the next node
     * @param user the user
     * @param system the system
     * @return a list of {@link VCommandable}s representing the next possibilities, which can be empty
     */
    private static @NotNull List<VCommandable> findNextFor(@NotNull VClass current, @NotNull String input, @NotNull User user, @NotNull Edict system) {
        // Grade all children
        Set<BkTreeSearcher.Match<? extends VCommandable>> children = current.indexer().search(input);

        // Filter un-permitted children
        int keyMatchThreshold = (int) Math.round((input.length() * (1 - system.getSettings().matchThreshold)));
        Stream<BkTreeSearcher.Match<? extends VCommandable>> matchStream = children.stream()
                .filter(c -> user.hasPermission(c.getMatch().permission()))
                .filter(c -> c.getDistance() <= keyMatchThreshold);

        // Get best distance
        int best = Integer.MAX_VALUE;
        List<BkTreeSearcher.Match<? extends VCommandable>> matches = matchStream.toList();
        for (BkTreeSearcher.Match<? extends VCommandable> match : matches) {
            if (best > match.getDistance()) {
                best = match.getDistance();
            }
        }

        // No match check
        if (best == Integer.MAX_VALUE) {
            return new ArrayList<>();
        }

        // Filter matches by best distance
        int finalBest = best;
        return matches.stream()
                .filter(c -> c.getDistance() == finalBest)
                .sorted((o1, o2) -> o2.getDistance() - o1.getDistance())
                .map(c -> (VCommandable) c.getMatch())
                .toList();
    }

    /**
     * Clean the input command.
     * Performs the following actions:<br>
     *  - Remove all double spaces<br>
     *  - Remove spaces before equal signs<br>
     *  - Remove all double ='s and -'s
     * @param command the input command
     * @return the cleaned command
     */
    private static @NotNull String cleanCommand(@NotNull String command) {
        command = command.strip();
        while (command.contains("  ")) {
            command = command.replace("  ", " ");
        }
        while (command.contains("==")) {
            command = command.replace("==", "=");
        }
        while (command.contains("--")) {
            command = command.replace("--", "-");
        }
        return command.replace(" =", "=");
    }

    /**
     * Query class, containing a root {@link VClass} and input string list.
     */
    private record Query(@NotNull VClass root, @NotNull List<String> input){}
}
