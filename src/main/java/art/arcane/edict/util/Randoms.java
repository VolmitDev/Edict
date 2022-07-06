package art.arcane.edict.util;

import java.util.Objects;

/**
 * Math
 *
 * @author cyberpwn
 */
@SuppressWarnings("SpellCheckingInspection")
public class Randoms {

    /**
     * Get true or false based on random percent
     *
     * @param d between 0 and 1
     * @return true if true
     */
    public static boolean r(Double d) {
        return Math.random() < Objects.requireNonNullElse(d, 0.5);
    }

    /**
     * Get true or false, randomly
     */
    public static boolean r() {
        return Math.random() < 0.5;
    }

    /**
     * Get a random int from to (inclusive)
     *
     * @param f the lower bound
     * @param t the upper bound
     * @return the value
     */
    public static int irand(int f, int t) {
        return f + (int) (Math.random() * ((t - f) + 1));
    }

    /**
     * Get a random float from to (inclusive)
     *
     * @param f the lower bound
     * @param t the upper bound
     * @return the value
     */
    public static float frand(float f, float t) {
        return f + (float) (Math.random() * ((t - f) + 1));
    }

    /**
     * Get a random double from to (inclusive)
     *
     * @param f the lower bound
     * @param t the upper bound
     * @return the value
     */
    public static double drand(double f, double t) {
        return f + (Math.random() * ((t - f) + 1));
    }
}
