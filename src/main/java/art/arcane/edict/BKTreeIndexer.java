package art.arcane.edict;

import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import art.arcane.edict.virtual.VCommandable;
import edu.gatech.gtri.bktree.BkTreeSearcher;
import edu.gatech.gtri.bktree.Metric;
import edu.gatech.gtri.bktree.MutableBkTree;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Stream;

public class BKTreeIndexer {

    /**
     * Damerau-Levenshtein Distance Algorithm.
     * Based on pseudocode found at <a href="https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance">Wikipedia - DL-Distance</a>.
     * Modified to be lenient towards abbreviations by effectively ignoring characters in the name (2nd argument) past the length of the input (1st argument).
     */
    private static final Metric<String> DAMERAU_LEVENSHTEIN_DISTANCE = (input, name) -> {

        // Custom modification
        int l2 = Math.min(input.length(), name.length());

        Integer[][] map = new Integer[input.length() + 1][l2 + 1];

        for (int i = 0; i <= input.length(); i++) {
            map[i][0] = i;
        }

        for (int i = 0; i <= l2; i++) {
            map[0][i] = i;
        }

        int cost;
        for (int i = 1; i <= input.length(); i++) {
            for (int j = 1; j <= l2; j++) {
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

        return map[input.length()][l2];
    };

    /**
     * Adapter for {@link #DAMERAU_LEVENSHTEIN_DISTANCE} for {@link VCommandable} constructs.
     */
    private static final Metric<VCommandable> DLD_EDICT_ADAPTER = (input, vClass) -> {

        // Min distance (best)
        OptionalInt result = vClass.allNames().stream().mapToInt(name -> DAMERAU_LEVENSHTEIN_DISTANCE.distance(input.name(), name)).min();

        // Null result
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Second argument has no names!");
        }

        return result.getAsInt();
    };

    /**
     * BK-Tree (<a href="https://github.com/gtri/bk-tree">GitHub</a>) for {@link VCommandable} elements.
     */
    private final MutableBkTree<VCommandable> bkTree = new MutableBkTree<>(DLD_EDICT_ADAPTER);

    /**
     * Searcher of the {@link #bkTree}.
     */
    private final BkTreeSearcher<VCommandable> searcher = new BkTreeSearcher<>(bkTree);

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
     * @param key the key
     * @param matchThreshold the percentage threshold
     * @param permissible function from a {@link VCommandable} to a boolean for whether the commandable can be run in current context
     * @return the best matching commandable objects (all with the same match value)
     */
    public @NotNull List<VCommandable> search(@NotNull String key, double matchThreshold, Function<? extends VCommandable, Object> permissible) {

        // Retrieve matches from tree.
        Stream<BkTreeSearcher.Match<? extends VCommandable>> matches = searcher.search(
                new VCommandable() {
            @Override
            public @NotNull String name() {
                return key;
            }

            @Override
            public @NotNull String[] aliases() {
                return new String[0];
            }

            @Override
            public @NotNull Permission permission() {
                return null;
            }

            @Override
            public @NotNull Edict system() {
                return null;
            }

            @Override
            public boolean run(@NotNull List<String> input, @NotNull User user) {
                return false;
            }
        },
                (int) (key.length() * matchThreshold)
        ).stream().filter(match -> permissible.apply(match.getMatch()));

        // Find best match(es) if any.
        if (matches.findAny().isEmpty()) {
            return new ArrayList<>();
        } else {
            int bestMatch = matches.mapToInt(BkTreeSearcher.Match::getDistance).max().orElse(-1);
            return matches.filter(match -> match.getDistance() == bestMatch).map(match -> (VCommandable) match.getMatch()).toList();
        }
    }
}
