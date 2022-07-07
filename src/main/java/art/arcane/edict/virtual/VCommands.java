package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.command.Command;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
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
 * @param parent parent branches ({@code null} if this is the root)
 * @param children further node(s)
 * @param permission permission node for this category
 * @param system the command system
 */
public record VCommands(@NotNull String name, @NotNull Command command, @Nullable VCommands parent, @NotNull List<VCommandable> children, @NotNull Permission permission, @NotNull Edict system) implements VCommandable {

    /**
     * Create a new category class.
     * This contains children: All methods of the clazz parameter that are annotated by @Command + any field declarations that are of a type that is annotated by @Command.
     * Note, there is NO check for circular references, so make sure to prevent this yourself.
     * TODO: Fix that
     * @param clazz the class to create the edict from
     * @param parent the parent {@link VCommands} ({@code null} if clazz is the root)
     * @param system the system
     * @return a new category, or {@code null} if there are no commands in this category
     * @throws MissingResourceException if there is no @Command annotation on this class despite it being called as such
     */
    public static @Nullable VCommands fromClass(@NotNull Class<?> clazz, @Nullable VCommands parent, @NotNull Edict system) throws MissingResourceException {

        // Check for annotation
        if (!clazz.isAnnotationPresent(Command.class)) {
            throw new MissingResourceException("@Command annotation not present on class", clazz.getSimpleName(), "@Command");
        }

        // Construct edict
        Command annotation = clazz.getDeclaredAnnotation(Command.class);
        VCommands category = new VCommands(
                annotation.name().isBlank() ? clazz.getSimpleName() : annotation.name(),
                annotation,
                parent,
                new ArrayList<>(),
                system.makePermission(parent == null ? null : parent.permission, annotation.permission()),
                system
        );

        // Loop over method declarations to find commands
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + method.getName() + "() not registered because not annotated by @Command"));
                continue;
            }
            annotation = method.getDeclaredAnnotation(Command.class);
            category.children.add(new VCommand(
                    annotation,
                    category,
                    method,
                    VParam.fromMethod(method, system),
                    system.makePermission(category.permission, annotation.permission()),
                    system)
            );
        }

        // Loop over fields to find more command categories
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getType().isAnnotationPresent(Command.class)) {
                system.d(new StringMessage(clazz.getSimpleName() + "#" + field.getName() + " not registered because not annotated by @Command"));
                continue;
            }
            VCommands subcategory = VCommands.fromClass(field.getType(), category, system);
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

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public String[] aliases() {
        return command.aliases();
    }
}
