package art.arcane.edict.util;

import art.arcane.edict.Edict;
import art.arcane.edict.user.User;
import art.arcane.edict.virtual.VParam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parameter parser.
 */
public class ParameterParser {
    /**
     * Input strings.
     */
    private final @NotNull List<String> input;

    /**
     * Parameters that need values.
     */
    private final @NotNull List<VParam> params;

    /**
     * The user running the command.
     */
    private final @NotNull User user;

    /**
     * The system in which the command is being run.
     */
    private final @NotNull Edict system;

    private final ConcurrentHashMap<VParam, Object> parameters = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, VParam> inputs = new ConcurrentHashMap<>();
    private final List<String> dashBooleanArgs = new ArrayList<>();
    private final List<String> keylessArgs = new ArrayList<>();
    private final List<String> keyedArgs = new ArrayList<>();
    private final List<String> nullArgs = new ArrayList<>();
    private final List<String> badArgs = new ArrayList<>();

    /**
     * Create a new parser
     * @param input the input strings
     * @param params the parameters that need values
     * @param user the user running the command
     * @param system the system in which the command is being run
     */
    public ParameterParser(@NotNull List<String> input, @NotNull List<VParam> params, @NotNull User user, @NotNull Edict system) {
        this.input = input;
        this.params = params;
        this.user = user;
        this.system = system;
    }

    public void parse() {

    }

    private void divideInput() {

        while (!input.isEmpty()) {
            String argument = input.remove(0);
            
        }

        // Split args into correct corresponding handlers
        for (String arg : input) {

            // These are handled later, after other fulfilled options will already have been matched
            List<String> splitArg = new ArrayList<>(List.of(arg.split("=")));

            if (splitArg.size() == 1) {

                if (arg.startsWith("-")) {
                    dashBooleanArgs.add(arg.substring(1));
                } else {
                    keylessArgs.add(arg);
                }
                continue;
            }

            if (splitArg.size() > 2) {
                String oldArg = null;
                while (!arg.equals(oldArg)) {
                    oldArg = arg;
                    arg = arg.replaceAll("==", "=");
                }

                splitArg = new List<>(arg.split("="));

                if (splitArg.size() == 2) {
                    debug("Parameter fixed by replacing '==' with '=' (new arg: " + Color.YELLOW + arg + C.RED + ")", C.RED);
                } else {
                    badArgs.add(arg);
                    continue;
                }
            }

            if (DecreeSystem.settings.allowNullInput && splitArg.get(1).equalsIgnoreCase("null")) {
                debug("Null parameter added: " + Color.YELLOW + arg, Color.GREEN);
                nullArgs.add(splitArg.get(0));
                continue;
            }

            if (splitArg.get(0).isEmpty()) {
                debug("Parameter key has empty value (full arg: " + Color.YELLOW + arg + C.RED + ")", C.RED);
                badArgs.add(arg);
                continue;
            }

            if (splitArg.get(1).isEmpty()) {
                debug("Parameter key: " + Color.YELLOW + splitArg.get(0) + C.RED + " has empty value (full arg: " + Color.YELLOW + arg + C.RED + ")", C.RED);
                badArgs.add(arg);
                continue;
            }

            keyedArgs.add(arg);
        }
    }

    /**
     * Compute parameter objects from string argument inputs
     * @param args The arguments (parameters) to parse into this command
     * @param sender The sender of the command
     * @return A {@link ConcurrentHashMap} from the parameter to the instantiated object for that parameter
     */
    private ConcurrentHashMap<DecreeParameter, Object> computeParameters(List<String> args, DecreeUser sender) {

        /*
         * Apologies for the obscene amount of loops.
         * It is the only way this can be done functionally.
         *
         * Note that despite the great amount of loops the average runtime is still ~O(log(n)).
         * This is because of the ever-decreasing number of arguments & options that are already matched.
         * If all arguments are already matched in the first (quick equals) loop, the runtime is actually O(n)
         */



        // Quick equals
        looping: for (String arg : keyedArgs.copy()) {
            String key = arg.split("\\Q=\\E")[0];
            String value = arg.split("\\Q=\\E")[1];
            for (DecreeParameter option : options) {
                if (option.getNames().contains(key)) {
                    if (parseParamInto(parameters, badArgs, parseExceptionArgs, option, value, sender)) {
                        options.remove(option);
                        keyedArgs.remove(arg);
                    } else if (DecreeSystem.settings.nullOnFailure) {
                        parameters.put(option, nullParam);
                    }
                    continue looping;
                }
            }
        }

        // Ignored case
        looping: for (String arg : keyedArgs.copy()) {
            String key = arg.split("\\Q=\\E")[0];
            String value = arg.split("\\Q=\\E")[1];
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (name.equalsIgnoreCase(key)) {
                        if (parseParamInto(parameters, badArgs, parseExceptionArgs, option, value, sender)) {
                            options.remove(option);
                            keyedArgs.remove(arg);
                        } else if (DecreeSystem.settings.nullOnFailure) {
                            parameters.put(option, nullParam);
                        }
                        continue looping;
                    }
                }
            }
        }

        // Name contains key (key substring of name)
        looping: for (String arg : keyedArgs.copy()) {
            String key = arg.split("\\Q=\\E")[0];
            String value = arg.split("\\Q=\\E")[1];
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (name.contains(key)) {
                        if (parseParamInto(parameters, badArgs, parseExceptionArgs, option, value, sender)) {
                            options.remove(option);
                            keyedArgs.remove(arg);
                        } else if (DecreeSystem.settings.nullOnFailure) {
                            parameters.put(option, nullParam);
                        }
                        continue looping;
                    }
                }
            }
        }

        // Key contains name (name substring of key)
        looping: for (String arg : keyedArgs.copy()) {
            String key = arg.split("\\Q=\\E")[0];
            String value = arg.split("\\Q=\\E")[1];
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (key.contains(name)) {
                        if (parseParamInto(parameters, badArgs, parseExceptionArgs, option, value, sender)) {
                            options.remove(option);
                            keyedArgs.remove(arg);
                        } else if (DecreeSystem.settings.nullOnFailure) {
                            parameters.put(option, nullParam);
                        }
                        continue looping;
                    }
                }
            }
        }

        // Quick equals null
        looping: for (String key : nullArgs.copy()) {
            for (DecreeParameter option : options) {
                if (option.getNames().contains(key)) {
                    parameters.put(option, nullParam);
                    options.remove(option);
                    nullArgs.remove(key);
                    continue looping;
                }
            }
        }

        // Ignored case null
        looping: for (String key : nullArgs.copy()) {
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (name.equalsIgnoreCase(key)) {
                        parameters.put(option, nullParam);
                        options.remove(option);
                        nullArgs.remove(key);
                        continue looping;
                    }
                }
            }
        }

        // Name contains key (key substring of name), null
        looping: for (String key : nullArgs.copy()) {
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (name.contains(key)) {
                        parameters.put(option, nullParam);
                        options.remove(option);
                        nullArgs.remove(key);
                        continue looping;
                    }
                }
            }
        }

        // Key contains name (name substring of key), null
        looping: for (String key : nullArgs.copy()) {
            for (DecreeParameter option : options) {
                for (String name : option.getNames()) {
                    if (key.contains(name)) {
                        parameters.put(option, nullParam);
                        options.remove(option);
                        nullArgs.remove(key);
                        continue looping;
                    }
                }
            }
        }

        // Keyless arguments
        looping: for (DecreeParameter option : options.copy()) {
            if (option.getHandler().supports(boolean.class)) {
                for (String dashBooleanArg : dashBooleanArgs.copy()) {
                    if (option.getNames().contains(dashBooleanArg)) {
                        parameters.put(option, true);
                        dashBooleanArgs.remove(dashBooleanArg);
                        options.remove(option);
                    }
                }

                for (String dashBooleanArg : dashBooleanArgs.copy()) {
                    for (String name : option.getNames()) {
                        if (name.equalsIgnoreCase(dashBooleanArg)) {
                            parameters.put(option, true);
                            dashBooleanArgs.remove(dashBooleanArg);
                            options.remove(option);
                        }
                    }
                }

                for (String dashBooleanArg : dashBooleanArgs.copy()) {
                    for (String name : option.getNames()) {
                        if (name.contains(dashBooleanArg)) {
                            parameters.put(option, true);
                            dashBooleanArgs.remove(dashBooleanArg);
                            options.remove(option);
                        }
                    }
                }

                for (String dashBooleanArg : dashBooleanArgs.copy()) {
                    for (String name : option.getNames()) {
                        if (dashBooleanArg.contains(name)) {
                            parameters.put(option, true);
                            dashBooleanArgs.remove(dashBooleanArg);
                            options.remove(option);
                        }
                    }
                }
            }

            for (String keylessArg : keylessArgs.copy()) {

                if (DecreeSystem.settings.allowNullInput && keylessArg.equalsIgnoreCase("null")) {
                    debug("Null parameter added: " + Color.YELLOW + keylessArg, Color.GREEN);
                    parameters.put(option, nullParam);
                    continue looping;
                }

                try {
                    Object result = option.getHandler().parse(keylessArg);
                    parseExceptionArgs.remove(option);
                    options.remove(option);
                    keylessArgs.remove(keylessArg);
                    parameters.put(option, result);
                    continue looping;

                } catch (DecreeParsingException e) {
                    parseExceptionArgs.put(option, e);
                } catch (DecreeWhichException e) {
                    parseExceptionArgs.remove(option);
                    options.remove(option);
                    keylessArgs.remove(keylessArg);

                    if (DecreeSystem.settings.pickFirstOnMultiple) {
                        parameters.put(option, e.getOptions().get(0));
                    } else {
                        Object result = pickValidOption(sender, e.getOptions(), option);
                        if (result == null) {
                            badArgs.add(keylessArg);
                        } else {
                            parameters.put(option, result);
                        }
                        continue looping;
                    }
                } catch (Throwable e) {
                    // This exception is actually something that is broken
                    debug("Parsing " + Color.YELLOW + keylessArg + C.RED + " into " + Color.YELLOW + option.getName() + C.RED + " failed because of: " + Color.YELLOW + e.getMessage(), C.RED);
                    e.printStackTrace();
                    debug("If you see a handler in the stacktrace that we (" + C.DECREE + "Decree" + C.RED + ") wrote, please report this bug to us.", C.RED);
                    debug("If you see a custom handler of your own, there is an issue with it.", C.RED);
                }
            }
        }

        // Remaining parameters
        for (DecreeParameter option : options.copy()) {
            if (option.hasDefault()) {
                parseExceptionArgs.remove(option);
                try {
                    Object val = option.getDefaultValue();
                    parameters.put(option, val == null ? nullParam : val);
                    options.remove(option);
                } catch (DecreeParsingException e) {
                    if (DecreeSystem.settings.nullOnFailure) {
                        parameters.put(option, nullParam);
                        options.remove(option);
                    } else {
                        debug("Default value " + Color.YELLOW + option.getDefaultRaw() + C.RED + " could not be parsed to " + option.getType().getSimpleName(), C.RED);
                        debug("Reason: " + Color.YELLOW + e.getMessage(), C.RED);
                    }
                } catch (DecreeWhichException e) {
                    debug("Default value " + Color.YELLOW + option.getDefaultRaw() + C.RED + " returned multiple options", C.RED);
                    options.remove(option);
                    if (DecreeSystem.settings.pickFirstOnMultiple) {
                        debug("Adding: " + Color.YELLOW + e.getOptions().get(0), Color.GREEN);
                        parameters.put(option, e.getOptions().get(0));
                    } else {
                        Object result = pickValidOption(sender, e.getOptions(), option);
                        if (result == null) {
                            badArgs.add(option.getDefaultRaw());
                        } else {
                            parameters.put(option, result);
                        }
                    }
                }
            } else if (option.isContextual() && sender.canUseContext()) {
                parseExceptionArgs.remove(option);
                DecreeContextHandler<?> handler;
                try {
                    handler = DecreeSystem.Context.getHandler(option.getType());
                } catch (DecreeException e) {
                    debug("Parameter " + option.getName() + " marked as contextual without available context handler (" + option.getType().getSimpleName() + ").", C.RED);
                    sender.sendMessageRaw(Color.RED + "Parameter " + Color.YELLOW + option.getHelp(sender, true) + C.RED + " marked as contextual without available context handler (" + option.getType().getSimpleName() + "). Please context your admin.");
                    e.printStackTrace();
                    continue;
                }
                Object contextValue = handler.handle(sender);
                debug("Context value for " + Color.YELLOW + option.getName() + Color.GREEN + " set to: " + handler.handleToString(sender), Color.GREEN);
                parameters.put(option, contextValue);
                options.remove(option);
            } else if (parseExceptionArgs.containsKey(option)) {
                debug("Parameter: " + Color.YELLOW + option.getName() + C.RED + " not fulfilled due to parseException: " + parseExceptionArgs.get(option).getMessage(), C.RED);
            }
        }

        // Convert nullArgs
        nullArgs = nullArgs.convert(na -> na + "=null");

        // Debug
        if (DecreeSystem.settings.allowNullInput) {
            debug("Unmatched null argument" + (nullArgs.size() == 1 ? "" : "s") + ": " + Color.YELLOW + (nullArgs.isNotEmpty() ? nullArgs.toString(", ") : "NONE"), nullArgs.isEmpty() ? Color.GREEN : C.RED);
        }
        debug("Unmatched keyless argument" + (keylessArgs.size() == 1 ? "":"s") + ": " + Color.YELLOW + (keylessArgs.isNotEmpty() ? keylessArgs.toString(", ") : "NONE"), keylessArgs.isEmpty() ? Color.GREEN : C.RED);
        debug("Unmatched keyed argument" + (keyedArgs.size() == 1 ? "":"s") + ": " + Color.YELLOW + (keyedArgs.isNotEmpty() ? keyedArgs.toString(", ") : "NONE"), keyedArgs.isEmpty() ? Color.GREEN : C.RED);
        debug("Bad argument" + (badArgs.size() == 1 ? "":"s") + ": " + Color.YELLOW + (badArgs.isNotEmpty() ? badArgs.toString(", ") : "NONE"), badArgs.isEmpty() ? Color.GREEN : C.RED);
        debug("Failed argument" + (parseExceptionArgs.size() <= 1 ? ": ":"s: \n") + Color.YELLOW + (parseExceptionArgs.size() != 0 ? new ArrayList<>(parseExceptionArgs.values()).convert(DecreeParsingException::getMessage).toString("\n") : "NONE"), parseExceptionArgs.isEmpty() ? Color.GREEN : C.RED);
        debug("Unfulfilled parameter" + (options.size() == 1 ? "":"s") + ": " + Color.YELLOW + (options.isNotEmpty() ? options.convert(DecreeParameter::getName).toString(", ") : "NONE"), options.isEmpty() ? Color.GREEN : C.RED);
        debug("Unfulfilled -boolean parameter" + (dashBooleanArgs.size() == 1 ? "":"s") + ": " + Color.YELLOW + (dashBooleanArgs.isNotEmpty() ? dashBooleanArgs.toString(", ") : "NONE"), dashBooleanArgs.isEmpty() ? Color.GREEN : C.RED);

        StringBuilder mappings = new StringBuilder("Parameter mapping:");
        parameters.forEach((param, object) -> mappings
                .append("\n")
                .append(Color.GREEN)
                .append("\u0009 - (")
                .append(Color.YELLOW)
                .append(param.getType().getSimpleName())
                .append(Color.GREEN)
                .append(") ")
                .append(Color.YELLOW)
                .append(param.getName())
                .append(Color.GREEN)
                .append(" → ")
                .append(Color.YELLOW)
                .append(object.toString().replace(String.valueOf(nullParam), "null")));
        options.forEach(param -> mappings
                .append("\n")
                .append(Color.GREEN)
                .append("\u0009 - (")
                .append(Color.YELLOW)
                .append(param.getType().getSimpleName())
                .append(Color.GREEN)
                .append(") ")
                .append(Color.YELLOW)
                .append(param.getName())
                .append(Color.GREEN)
                .append(" → ")
                .append(Color.RED)
                .append("NONE"));

        debug(mappings.toString(), Color.GREEN);

        if (validateParameters(parameters, sender, parseExceptionArgs)) {
            return parameters;
        } else {
            return null;
        }
    }

    /**
     * Instruct the sender to pick a valid option
     * @param sender The sender that must pick an option
     * @param validOptions The valid options that can be picked (as objects)
     * @return The string value for the selected option
     */
    private Object pickValidOption(DecreeUser sender, List<?> validOptions, DecreeParameter parameter) {
        DecreeParameterHandler<?> handler = parameter.getHandler();

        int tries = 3;
        List<String> options = validOptions.convert(handler::toStringForce);
        String result = null;

        sender.sendHeader("Pick a " + parameter.getName() + " (" + parameter.getType().getSimpleName() + ")");
        sender.sendMessageRaw("<gradient:#1ed497:#b39427>This query will expire in 15 seconds.</gradient>");

        while (tries-- > 0 && (result == null || !options.contains(result))) {
            sender.sendMessageRaw("<gradient:#1ed497:#b39427>Please pick a valid option.</gradient>");
            String password = UUID.randomUUID().toString().replaceAll("\\Q-\\E", "");
            int m = 0;

            for (String i : validOptions.convert(handler::toStringForce)) {
                sender.sendMessage("<hover:show_text:'" + gradients[m % gradients.length] + i + "</gradient>'><click:run_command:/decree-future " + password + " " + i + ">" + "- " + gradients[m % gradients.length] + i + "</gradient></click></hover>");
                m++;
            }

            CompletableFuture<String> future = new CompletableFuture<>();
            if (sender.canUseContext()) {
                DecreeSystem.Completer.postFuture(password, future);
                system().playSound(false, DecreeSystem.SFX.Picked, sender);
            } else {
                DecreeSystem.Completer.postConsoleFuture(future);
            }

            try {
                result = future.get(15, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {

            }
        }

        if (result != null && options.contains(result)) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equals(result)) {
                    return validOptions.get(i);
                }
            }
        } else {
            sender.send(Color.RED + "You did not enter a correct option within 3 tries.");
            sender.send(Color.RED + "Please double-check your arguments & option picking.");
        }

        return null;
    }

    /**
     * Validate parameters
     * @param parameters The parameters to validate
     * @param sender The sender of the command
     * @return True if valid, false if not
     */
    private boolean validateParameters(ConcurrentHashMap<DecreeParameter, Object> parameters, DecreeUser sender, ConcurrentHashMap<DecreeParameter, DecreeParsingException> parseExceptions) {
        boolean valid = true;
        for (DecreeParameter parameter : getParameters()) {
            if (!parameters.containsKey(parameter)) {
                debug("Parameter: " + Color.YELLOW + parameter.getName() + Color.RED + " not in mapping.", Color.RED);
                String reason;
                if (parseExceptions.containsKey(parameter)) {
                    DecreeParsingException e = parseExceptions.get(parameter);
                    reason = "(" + Color.YELLOW + e.getType().getSimpleName() + Color.RED + ") failed for " + Color.YELLOW + e.getInput() + Color.RED + ". Reason: " + Color.YELLOW + e.getReason();
                } else {
                    reason = "not specified. Please add.";
                }
                sender.sendMessageRaw(Color.RED + "Parameter: " + Color.YELLOW + parameter.getHelp(sender, true) + Color.RED + " " + reason);
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Parses a parameter into a map after parsing
     * @param parameters The parameter map to store the value into
     * @param parseExceptionArgs Parameters which ran into parseExceptions
     * @param option The parameter type to parse into
     * @param value The value to parse
     * @return True if successful, false if not. Nothing is added on parsing failure.
     */
    private boolean parseParamInto(ConcurrentHashMap<DecreeParameter, Object> parameters, List<String> badArgs, ConcurrentHashMap<DecreeParameter, DecreeParsingException> parseExceptionArgs, DecreeParameter option, String value, DecreeUser sender) {
        try {
            Object val = option.getHandler().parse(value);


            parameters.put(option, val == null ? nullParam : val);
            return true;
        } catch (DecreeWhichException e) {
            debug("Value " + Color.YELLOW + value + C.RED + " returned multiple options", C.RED);
            if (DecreeSystem.settings.pickFirstOnMultiple) {
                debug("Adding: " + Color.YELLOW + e.getOptions().get(0), Color.GREEN);
                parameters.put(option, e.getOptions().get(0));
            } else {
                Object result = pickValidOption(sender, e.getOptions(), option);
                if (result == null) {
                    badArgs.add(option.getDefaultRaw());
                } else {
                    parameters.put(option, result);
                }
            }
            return true;
        } catch (DecreeParsingException e) {
            parseExceptionArgs.put(option, e);
        } catch (Throwable e) {
            DecreeLogger.d("Failed to parse into: '" + option.getName() + "' value '" + value + "'");
            e.printStackTrace();
        }
        return false;
    }
}
