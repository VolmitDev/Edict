package art.arcane.edict.virtual;

import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.handler.ParameterHandler;
import art.arcane.edict.util.BKTreeIndexer;
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
import java.util.*;

/**
 * Record of a virtual command category. Represents a position in the tree of commands.
 * The category has at least 1 child and cannot run commands itself (only pass them down).
 * The parent can be null. If that is the case, this is the root category
 * I.e. like a branch in a tree data-structure.
 * @param name the name of the command node
 * @param command the command annotation
 * @param indexer indexer of further node(s)
 * @param permission permission node for this category
 * @param system the command system
 */
public record VClass(@NotNull String name, @NotNull Command command, @NotNull Object instance, @Nullable VClass parent, @NotNull List<VCommandable> children, @NotNull BKTreeIndexer indexer, @NotNull Permission permission, @NotNull Edict system) implements VCommandable {

    /**
     * Create a new category class.
     * This contains children: All methods of the clazz parameter that are annotated by @Command + any field declarations that are of a type that is annotated by @Command.
     * @param instance the class to create the edict from
     * @param parent the parent {@link VClass} ({@code null} if clazz is the root)
     * @param system the system
     * @return a new category, or {@code null} if there are no commands in this category or {@code null} if this would introduce a circular reference
     * @throws MissingResourceException if there is no @Command annotation on this class despite it being called as such
     * @throws NullPointerException if the {@link ParameterHandler} for any of the parameters of any methods of this class or any of its children is not registered
     * or if the {@link ContextHandler} for any of the contextual parameter of any methods of the {@code commandRoots} or any of its children is not registered
     */
    public static @Nullable VClass fromInstance(@NotNull Object instance, @Nullable VClass parent, @NotNull Edict system) throws MissingResourceException, NullPointerException {

        // Class
        Class<?> clazz = instance.getClass();

        // Circular reference check
        VClass p = parent;
        while (p != null) {
            if (parent.instance.getClass().equals(clazz)) {
                return null;
            }
            p = p.parent;
        }

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
                parent,
                new ArrayList<>(),
                new BKTreeIndexer(),
                system.makePermission(parent == null ? null : parent.permission, annotation.permission()),
                system
        );

        // Loop over method declarations to find commands
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
        for (Field field : clazz.getDeclaredFields()) {

            // Annotation check
            if (!field.getType().isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + field.getName() + " not registered because not annotated by @Command"));
                continue;
            }

            field.setAccessible(true);

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

        // Add children to tree
        category.indexer.addAll(category.children);

        return category;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public String[] aliases() {
        return command.aliases();
    }

    /**
     * The tree indexer of the commandable.
     * @return the tree indexer of the commandable
     */
    public @NotNull BKTreeIndexer indexer() {
        return indexer;
    }

    @Override
    public boolean run(@NotNull List<String> input, @NotNull User user) {

        // Send help when this is the final node
        if (input.isEmpty()) {
            // TODO: Send category help
            user.send(new StringMessage(name() + ": Need more input to reach command"));
            return true;
        }

        // Get children
        List<VCommandable> children = indexer.search(
                input.get(0),
                system.getSettings().matchThreshold,
                vCommandable -> user.hasPermission(vCommandable.permission())
        );

        // Send command further downstream
        for (VCommandable child : children) {
            if (child.run(input.subList(1, input.size()), user)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + command.hashCode() + instance.hashCode() + children.hashCode() + indexer.hashCode() + system.hashCode() + permission.hashCode();
    }
}
