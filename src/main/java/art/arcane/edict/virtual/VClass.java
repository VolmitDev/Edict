package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Command;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * Record of a virtual command category. Represents a position in the tree of commands.
 * The category has at least 1 child and cannot run commands itself (only pass them down).
 * The parent can be null. If that is the case, this is the root category
 * I.e. like a branch in a tree data-structure.
 * @param name the name of the command node
 * @param command the command annotation
 * @param children further node(s)
 * @param permission permission node for this category
 * @param system the command system
 */
public record VClass(@NotNull String name, @NotNull Command command, @NotNull Object instance, @NotNull List<VCommandable> children, @NotNull Permission permission, @NotNull Edict system) implements VCommandable {

    /**
     * Create a new category class.
     * This contains children: All methods of the clazz parameter that are annotated by @Command + any field declarations that are of a type that is annotated by @Command.
     * Note, there is NO check for circular references, so make sure to prevent this yourself.
     * TODO: Fix that
     * @param instance the class to create the edict from
     * @param parent the parent {@link VClass} ({@code null} if clazz is the root)
     * @param system the system
     * @return a new category, or {@code null} if there are no commands in this category
     * @throws MissingResourceException if there is no @Command annotation on this class despite it being called as such
     */
    public static @Nullable VClass fromInstance(@NotNull Object instance, @Nullable VClass parent, @NotNull Edict system) throws MissingResourceException {

        // Class
        Class<?> clazz = instance.getClass();

        // Check for annotation
        if (!clazz.isAnnotationPresent(Command.class)) {
            throw new MissingResourceException("@Command annotation not present on class " + clazz.getSimpleName(), clazz.getSimpleName(), "@Command");
        }

        // Construct edict
        Command annotation = clazz.getDeclaredAnnotation(Command.class);
        VClass category = new VClass(
                annotation.name().isBlank() ? clazz.getSimpleName() : annotation.name(),
                annotation,
                instance,
                new ArrayList<>(),
                system.makePermission(parent == null ? null : parent.permission, annotation.permission()),
                system
        );

        // Loop over method declarations to find commands
        // TODO: Run this by Dan
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + method.getName() + "() not registered because not annotated by @Command"));
                continue;
            }
            method.setAccessible(true);
            annotation = method.getDeclaredAnnotation(Command.class);
            category.children.add(new VMethod(
                    annotation,
                    category,
                    method,
                    VParam.paramsFromMethod(method, system),
                    system.makePermission(category.permission, annotation.permission()),
                    system)
            );
        }

        // Loop over fields to find more command categories
        // TODO: Run this by Dan
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            // Annotation check
            if (!field.getType().isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + field.getName() + " not registered because not annotated by @Command"));
                continue;
            }

            // Construct instance
            Object fInstance = null;
            field.setAccessible(true);
            try {
                if (field.get(instance) != null) {
                    fInstance = field.get(instance);
                } else {
                    for (Constructor<?> constructor : field.getType().getConstructors()) {
                        if (constructor.getParameterCount() == 0) {
                            constructor.setAccessible(true);
                            try {
                                fInstance = constructor.newInstance();
                            } catch (InstantiationException | InvocationTargetException e) {
                                system.w(new StringMessage("Tried constructing class for field " + field.getName() + " but could not due to " + e));
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                system.w(new StringMessage("Tried getting field " + field.getName() + " but could not get access due to " + e));
            }

            // Failed
            if (fInstance == null) {
                system.w(new StringMessage("Field " + field.getName() + " is of a type annotated by @Command but cannot be instantiated!"));
                continue;
            }

            // Success
            VClass subcategory = VClass.fromInstance(fInstance, category, system);
            if (subcategory != null) {
                category.children.add(subcategory);
            }
        }

        // Empty check
        if (category.children.isEmpty()) {
            system.w(new StringMessage(clazz.getSimpleName() + " has no declared commands or subcategories. Not loading the class. This is likely an error!"));
            return null;
        }

        return category;
    }

    /**
     * Sort and filter children nodes by some input and a user.
     * @param options the children (options) to sort & filter
     * @param input the input string to match against
     * @param user the user to check for permissions
     * @return a sorted list consisting of a subset of the children or {@code null} if none matched even slightly or had permission
     */
    public static @NotNull List<VCommandable> sortAndFilterChildren(@NotNull List<VCommandable> options, @NotNull String input, @NotNull User user, double threshold) {
        // TODO: Cache?

        // Get scores & max
        List<Integer> values = new ArrayList<>();
        int max = 0;
        for (VCommandable option : options) {
            int score = option.match(input, user);
            max = Math.max(max, score);
            values.add(score);
        }

        List<VCommandable> result = new ArrayList<>();

        // Return null if none scored higher than the threshold
        if (max < threshold) {
            return result;
        }

        // Retrieve results from options based on max scores
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == max) {
                result.add(options.get(i));
            }
        }
        return result;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public String[] aliases() {
        return command.aliases();
    }

    @Override
    public boolean run(@NotNull List<String> input, @NotNull User user) {

        // Send help when this is the final node
        if (input.isEmpty()) {
            // TODO: Send help
            user.send(new StringMessage(name() + ": Need more input to reach command"));
            return false;
        }

        // Send command further downstream
        for (VCommandable root : sortAndFilterChildren(children, input.get(0), user, system.settings().matchThreshold)) {
            if (root.run(input.subList(1, input.size()), user)) {
                return true;
            }
        }

        return false;
    }
}
