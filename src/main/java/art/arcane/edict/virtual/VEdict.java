package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.command.Command;
import art.arcane.edict.message.StringMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

/**
 * Record of a virtual edict, based on a position in the tree of commands.
 * If the edict has children, it has no command functionality and only functions as a gateway.
 * I.e. like a branch in a tree data-structure.
 * @param command the command annotation
 * @param parent parent branches ({@code null} if this is the root)
 * @param children further nodes
 * @param params the parameters of this method {@link VParam}s
 * @param system the command system
 */
public record VEdict(Command command, @Nullable VEdict parent, @Nullable List<VEdict> children, @Nullable List<VParam> params, @NotNull Edict system) {

    /**
     * Create a new VEdict from a class (say, a command category).
     * This contains children: All methods of the clazz parameter + any field declarations that are of a type that is also annotated by @Command.
     * Note, there is NO check for circular references, which can absolutely destroy the system.
     * TODO: Fix that
     * @param clazz the class to create the edict from
     * @param parent the parent VEdict
     * @param system the system
     * @return a new VEdict or null if there are no commands in this VEdict
     * @throws MissingResourceException if there is no @Command annotation on this class despite it being called as such
     * @throws NoSuchElementException if there are no commands or subcategories declared in this command class
     */
    public static VEdict fromClass(@NotNull Class<?> clazz, @Nullable VEdict parent, @NotNull Edict system) throws MissingResourceException, NoSuchElementException {

        // Check for annotation
        if (!clazz.isAnnotationPresent(Command.class)) {
            throw new MissingResourceException("@Command annotation not present on class", clazz.getSimpleName(), "@Command");
        }

        // Construct edict
        VEdict edict = new VEdict(clazz.getDeclaredAnnotation(Command.class), parent, new ArrayList<>(), null, system);
        assert edict.children != null;

        // Loop over method declarations to find commands
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + method.getName() + "() not registered because not annotated by @Command"));
                continue;
            }
            edict.children.add(new VEdict(method.getDeclaredAnnotation(Command.class), edict, null, VParam.fromMethod(method, system), system));
        }

        // Loop over fields to find more command categories
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getType().isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + field.getName() + " not registered because not annotated by @Command"));
                continue;
            }
            VEdict subcategory = VEdict.fromClass(field.getType(), edict, system);
            if (subcategory != null) {
                edict.children.add(subcategory);
            }
        }

        // Empty check
        if (edict.children.isEmpty()) {
            system.w(new StringMessage(clazz.getSimpleName() + " has no declared commands or subcategories. Not loading the class. This is likely an error!"));
            return null;
        }

        return edict;
    }
}
