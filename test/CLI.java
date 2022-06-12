package test;

import test.Commands.Command;
import test.Commands.DefaultIO;

import java.io.IOException;
import java.util.ArrayList;

public class CLI {

    ArrayList<Command> commands;
    DefaultIO dio;
    Commands c;

    /**
     * Init for the Command object, adds all types of commands to the data commands list
     */
    public CLI(DefaultIO dio) {
        this.dio = dio;
        c = new Commands(dio);
        commands = new ArrayList<>();
        // Command 1
        commands.add(c.new UploadTimeSeriesCsvFile());
        // Command 2
        commands.add(c.new ChangeAlgorithmSettings());
        // Command 3
        commands.add(c.new DetectAnomalies());
        // Command 4
        commands.add(c.new DisplayResults());
        // Command 5
        commands.add(c.new AnalyzeAnomalies());
        // Command 6
        commands.add(c.new Exit());
    }

    /**
     * Prints the main menu of the cli, using the description of every command added
     */
    private void printMainMenu() {
        int counter = 1;
        dio.write("Welcome to the Anomaly Detection Server.\nPlease choose an option:\n");
        for (Command command : commands) {
            dio.write(String.format("%x. %s\n", counter++, command.description));
        }
    }

    /**
     * Starts the CLI iteration and manages and calls the proper command execution given the command ID
     */
    public void start() {
        int commandId = 0;
        // Loop through the CLI until command number 6 is chosen (exit)
        while (commandId != 6) {
            // Main menu display
            printMainMenu();
            // Try to parse the command choice
            try {
                // Expected to have the command ID first and then the proper input for every command,
                // Otherwise an exception will be thrown
                commandId = Integer.parseInt(dio.readText());
                // Execute the proper command
                commands.get(commandId - 1).execute();
            } catch (NumberFormatException | IOException ex) {
                dio.write("Invalid choice, please try again\n");
            }
        }
    }
}