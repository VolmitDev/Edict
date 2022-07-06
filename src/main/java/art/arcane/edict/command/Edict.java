package art.arcane.edict.command;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Edict {

    String NO_PERMISSION_NEEDED = "";

    /**
     * The description of this command.
     */
    String description();

    /**
     * The name of this command.<br>
     * The Method's name by default (someCommand becomes some-command)
     */
    String name() default "";

    /**
     * The aliases of this parameter (instead of just the {@link #name() name} (if specified) or Method Name (name of method))<br>
     * Can be initialized as just a string (e.g. "alias") or as an array (e.g. {"alias1", "alias2"})<br>
     * If someone uses /plugin foo, and you specify alias="f" here, /plugin f will do the exact same.
     */
    String[] aliases() default "";

    /**
     * Whether this command can use senders' context to derive (optional) parameter values.
     * Defaults to {@code false}
     * @return True if context can be used
     */
    boolean contextual() default false;

    /**
     * If the commands functions MUST be run in sync, set this to true.
     * Defaults to {@code false}
     * @return True if the command must be run sync
     */
    boolean sync() default false;

    /**
     * The permissions class that gives the required permission for this command.
     * By default, it requires no permissions
     * @return The permission node for this decree command
     */
    String permission() default NO_PERMISSION_NEEDED;
}
