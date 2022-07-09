package art.arcane.edict.util;

/**
 * Sound effects.
 * TODO: Add more SFX
 */
public enum SFX {
    PICK_AN_OPTION("User has to pick an option for a parameter value that could lead to multiple results.");

    /**
     * Description of the sound effect.
     */
    private final String description;

    /**
     * Create a new sound effect enum entry.
     * @param description the description of the sound effect
     */
    SFX (String description) {
        this.description = description;
    }
}
