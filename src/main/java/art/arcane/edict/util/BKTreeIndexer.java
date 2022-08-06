package art.arcane.edict.util;

import art.arcane.edict.message.CompoundMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import art.arcane.edict.virtual.VCommandable;
import edu.gatech.gtri.bktree.BkTreeSearcher;
import edu.gatech.gtri.bktree.Metric;
import edu.gatech.gtri.bktree.MutableBkTree;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class BKTreeIndexer {

    /**
     * Damerau-Levenshtein Distance Algorithm.<br>
     * Based on pseudocode found at <a href="https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance">Wikipedia - DL-Distance</a>.
     */
    protected static final Metric<String> DAMERAU_LEVENSHTEIN_DISTANCE = (name, input) -> {

        Integer[][] map = new Integer[input.length()][name.length()];

        for (int i = 0; i < input.length(); i++) {
            map[i][0] = i;
        }

        for (int i = 0; i < name.length(); i++) {
            map[0][i] = i;
        }

        int cost;
        for (int i = 1; i < input.length(); i++) {
            for (int j = 1; j < name.length(); j++) {
                if (input.charAt(i) == name.charAt(j)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                map[i][j] = Math.min(Math.min(
                        map[i-1][j] + 1,        // Deletion
                        map[i][j-1] + 1),       // Insertion
                        map[i-1][j-1] + cost    // Substitution
                );

                if (i > 1 && j > 1 && input.charAt(i) == name.charAt(j-1) && input.charAt(i-1) == name.charAt(j)) {
                    map[i][j] = Math.min(
                            map[i][j],          // Previous
                            map[i-2][j-2] + 1   // Transposition
                    );
                }
            }
        }

        return (int) map[input.length() - 1][name.length() - 1];
    };

    /**
     * Adapter for {@link #DAMERAU_LEVENSHTEIN_DISTANCE} for {@link VCommandable} constructs.
     */
    protected static final Metric<VCommandable> DLD_EDICT_ADAPTER = (vClass, input) -> {

        // Min distance (best)
        OptionalInt result = vClass.allNames().stream().mapToInt(name -> {
            if (name.equals(input.name())) {
                return 0;
            } else if (name.startsWith(input.name())) {
                return 1;
            } else if (input.name().startsWith(name)) {
                return 2;
            } else {
                return DAMERAU_LEVENSHTEIN_DISTANCE.distance(input.name(), name);
            }
        }).min();

        // Null result
        if (result.isEmpty()) {
            throw new IllegalArgumentException("First argument has no names!");
        }

        return result.getAsInt();
    };

    /**
     * BK-Tree (<a href="https://github.com/gtri/bk-tree">GitHub</a>) for {@link VCommandable} elements.
     */
    protected final MutableBkTree<VCommandable> bkTree = new MutableBkTree<>(DLD_EDICT_ADAPTER);

    /**
     * Searcher of the {@link #bkTree}.
     */
    protected final BkTreeSearcher<VCommandable> searcher = new BkTreeSearcher<>(bkTree);

    /**
     * Construct a tree indexer.
     * @param values the values of the tree. Cannot be modified after. Should be all children of the class.
     */
    public void addAll(Iterable<? extends VCommandable> values) {
        bkTree.addAll(values);
    }

    /**
     * Search the tree with some key.
     * The threshold is the percentage of the input string that has to match the name, discarding characters in the name after the length of the input string's length.
     * This is subject to rounding and - since fuzzy searching is effectively guessing - mistakes. The human mind is impossible to fully understand.
     * Only returns the elements exceeding the matchThreshold and with the highest value in the set (equally likely solutions).
     * @param key the key
     * @param matchThreshold the percentage threshold
     * @param permissible function from a {@link VCommandable} to a boolean for whether the commandable can be run in current context
     * @return the best matching commandable objects (all with the same match value)
     */
    public @NotNull List<VCommandable> search(@NotNull String key, double matchThreshold, @NotNull Function<VCommandable, Boolean> permissible) {
        return search(key, matchThreshold, permissible, true);
    }

    /**
     * Search the tree with some key.
     * The threshold is the percentage of the input string that has to match the name, discarding characters in the name after the length of the input string's length.
     * This is subject to rounding and - since fuzzy searching is effectively guessing - mistakes. The human mind is impossible to fully understand.
     * @param key the key
     * @param matchThreshold the percentage threshold
     * @param permissible function from a {@link VCommandable} to a boolean for whether the commandable can be run in current context
     * @param forceMax force return only matches with the highest value in set. Still respects {@code matchThreshold}.
     * @return the best matching commandable objects (all with the same match value)
     */
    public @NotNull List<VCommandable> search(@NotNull String key, double matchThreshold, @NotNull Function<VCommandable, Boolean> permissible, boolean forceMax) {

        // Retrieve matches from tree.
        Set<BkTreeSearcher.Match<? extends VCommandable>> matches = searcher.search(
                new BKTreeIndexable(key, new String[0]),
                (int) Math.round((key.length() * (1 - matchThreshold)))
        );

        // Apply permissions
        matches.removeIf(match -> !permissible.apply(match.getMatch()));

        // Find best match(es) if any.
        if (matches.isEmpty()) {
            return new ArrayList<>();
        } else {
            if (forceMax) {
                int bestMatch = matches.stream().mapToInt(BkTreeSearcher.Match::getDistance).min().orElse(-1);
                return matches.stream().filter(match -> match.getDistance() == bestMatch).map(match -> (VCommandable) match.getMatch()).toList();
            } else {
                return matches.stream().map(m -> (VCommandable) m.getMatch()).toList();
            }
        }
    }

    /**
     * Placeholder class for a VCommandable
     * @param name the name of the search input
     */
    protected record BKTreeIndexable(@NotNull String name, @NotNull String[] aliases) implements VCommandable {

        @Override
        public @NotNull String name() {
            return name;
        }

        @Override
        public @NotNull String[] aliases() {
            return aliases;
        }

        /**
         * Parent commandable.
         *
         * @return the parent commandable
         */
        @Override
        public @NotNull VCommandable parent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Permission permission() {
            throw new UnsupportedOperationException();
        }

        /**
         * Send help to a user.
         *
         * @param user the user
         */
        @Override
        public @NotNull CompoundMessage getHelpFor(@NotNull User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean run(@NotNull List<String> input, @NotNull User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<String> suggest(@NotNull List<String> input, @NotNull User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void networkString(@NotNull StringBuilder builder, @NotNull String indent, @NotNull String currentIndent) {
            throw new UnsupportedOperationException();
        }
    }
}
