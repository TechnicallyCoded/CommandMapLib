package com.tcoded.commandmaplib;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"CallToPrintStackTrace", "unused"})
public final class CommandMapLib {

    public static void registerCommand(PluginCommand plCommand) {
        registerCommand(plCommand.getPlugin().getName().toLowerCase(), plCommand);
    }

    public static void registerCommand(String prefix, Command command) {
        try {
            CommandMap commandMap = getCommandMap();
            commandMap.register(prefix, command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void registerAliases(Plugin plugin, Command command, String... aliases) {
        try {
            CommandMap commandMap = getCommandMap();

            String prefix = plugin.getDescription().getName().toLowerCase();
            for (String alias : aliases) {
                commandMap.register(alias, prefix, command);
            }

            if (command instanceof PluginCommand) {
                PluginCommand pluginCommand = (PluginCommand) command;
                pluginCommand.getAliases().addAll(Arrays.asList(aliases));
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Command getRegisteredCommand(String commandName) {
        try {
            CommandMap commandMap = getCommandMap();
            HashMap<String, Command> knownCommands = getKnownCommands(commandMap);

            // Get main command
            String lowerCaseMainCmdName = commandName.toLowerCase();
            return knownCommands.get(lowerCaseMainCmdName);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void unregisterCommand(PluginCommand command) {
        try {
            Plugin plugin = command.getPlugin();

            CommandMap commandMap = getCommandMap();
            HashMap<String, Command> knownCommands = getKnownCommands(commandMap);

            String prefix = plugin.getDescription().getName().toLowerCase();

            // Unregister main command
            String lowerCaseMainCmdName = command.getName().toLowerCase();
            knownCommands.remove(lowerCaseMainCmdName);
            knownCommands.remove(prefix + ":" + lowerCaseMainCmdName);

            // Aliases
            List<String> aliasesLowerCase = command.getAliases().stream().map(String::toLowerCase).collect(Collectors.toList());

            // Without prefix
            for (String alias : aliasesLowerCase) {
                knownCommands.remove(alias);
            }

            // With prefix
            for (String alias : aliasesLowerCase) {
                String combined = prefix + ":" + alias;
                knownCommands.remove(combined);
            }

            command.setLabel("");
            command.setAliases(new ArrayList<>());
            command.unregister(commandMap);

        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, Command> getKnownCommands(CommandMap commandMap) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // noinspection unchecked
        Class<SimpleCommandMap> commandMapClass = (Class<SimpleCommandMap>) Class.forName("org.bukkit.command.SimpleCommandMap");
        Field knownCommandsField = commandMapClass.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        // noinspection unchecked
        return (HashMap<String, Command>) knownCommandsField.get(commandMap);
    }

    private static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Server server = Bukkit.getServer();
        final Field bukkitCommandMap = server.getClass().getDeclaredField("commandMap");

        bukkitCommandMap.setAccessible(true);
        return (CommandMap) bukkitCommandMap.get(server);
    }

}
