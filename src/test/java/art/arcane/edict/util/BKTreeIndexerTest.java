package art.arcane.edict.util;

import art.arcane.edict.virtual.VCommandable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class BKTreeIndexerTest extends BKTreeIndexer {

    @BeforeEach
    void setUp() {
        addAll(List.of(new VCommandable[]{
                t("aaa"),
                t("bbb"),
                t("ccc")
        }));
    }

    @Test
    public void testDLD() {
        assertEquals(0, DAMERAU_LEVENSHTEIN_DISTANCE.distance("aaa", "aaa"));
        assertEquals(2, DAMERAU_LEVENSHTEIN_DISTANCE.distance("a", "aaa"));
        assertEquals(2, DAMERAU_LEVENSHTEIN_DISTANCE.distance("aaa", "a"));
        assertEquals(2, DAMERAU_LEVENSHTEIN_DISTANCE.distance("aaa", "bbb"));
        assertEquals(3, DAMERAU_LEVENSHTEIN_DISTANCE.distance("adda", "bbb"));
    }

    @Test
    public void testDLDAdapter() {
        assertEquals(0, DLD_EDICT_ADAPTER.distance(t("aaa"), t("aaa")));
        assertEquals(1, DLD_EDICT_ADAPTER.distance(t("aaa"), t("a")));
        assertEquals(2, DLD_EDICT_ADAPTER.distance(t("a"), t("aaa")));
        assertEquals(1, DLD_EDICT_ADAPTER.distance(t("aa"), t("bb")));
        assertEquals(2, DLD_EDICT_ADAPTER.distance(t("abc"), t("def")));
    }

    @Contract(value = "_ -> new", pure = true)
    private @NotNull VCommandable t(@NotNull String name) {
        return new BKTreeIndexable(name);
    }
}