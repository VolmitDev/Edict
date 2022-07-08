package art.arcane.edict;

import art.arcane.edict.virtual.VCommandable;
import art.arcane.edict.virtual.VIndexable;
import edu.gatech.gtri.bktree.BkTreeSearcher;
import edu.gatech.gtri.bktree.Metric;
import edu.gatech.gtri.bktree.MutableBkTree;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;
import java.util.Set;

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
     * Adapter for {@link #DAMERAU_LEVENSHTEIN_DISTANCE} for {@link VIndexable} constructs.
     */
    private static final Metric<VIndexable> DLD_EDICT_ADAPTER = (input, vCommandable) -> {
        if (!(vCommandable instanceof VCommandable)) {
            throw new IllegalArgumentException("Second argument must be VCommandable!");
        }

        OptionalInt result = ((VCommandable) vCommandable).allNames().stream().mapToInt(name -> DAMERAU_LEVENSHTEIN_DISTANCE.distance(input.name(), name)).min();

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Second argument has no names!");
        }

        return result.getAsInt();
    };

    /**
     * BK-Tree (<a href="https://github.com/gtri/bk-tree">GitHub</a>) for {@link VIndexable} elements.
     */
    private final MutableBkTree<VIndexable> bkTree = new MutableBkTree<>(DLD_EDICT_ADAPTER);

    private final BkTreeSearcher<VIndexable> searcher;

    public BKTreeIndexer(VCommandable... values) {
        bkTree.addAll(values);
        searcher = new BkTreeSearcher<>(bkTree);
    }

    public boolean search(@NotNull String key, double matchThreshold) {

        return searcher.search(new VIndexable() {
            @Override
            public @NotNull String name() {
                return key;
            }

            @Override
            public @NotNull String[] aliases() {
                return new String[0];
            }
        }, (int) (key.length() * matchThreshold)).size() > 0;
    }




}
