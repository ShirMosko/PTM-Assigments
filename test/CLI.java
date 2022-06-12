package test;

import java.io.IOException;
import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

    ArrayList<Command> commands;
    DefaultIO dio;
    Commands c;

    public CLI(DefaultIO dio) {
        this.dio = dio;
        c = new Commands(dio);
        commands = new ArrayList<>();
        //numbered by order 1-6
        commands.add(c.new UploadTimeSeriesCsvFile());
        commands.add(c.new algorithm_settings());
        commands.add(c.new detect_anomalies());
        commands.add(c.new display_results());
        commands.add(c.new upload_anomalies_and_analyze_results());
        commands.add(c.new exit());

    }

    public void start() {

        int commandNum = 0;
        while (commandNum != 6) {
            int count = 1;
            dio.write("Welcome to the Anomaly Detection Server.\\nPlease choose an option:\\n");
            for (Command command : commands) {
                dio.write(String.format("%x. %s\n", count++, command.description));
                count++;
            }

            try {
                // Expected to have the command ID first and then the proper input for every command,
                // Otherwise an exception will be thrown
                commandNum = Integer.parseInt(dio.readText());
                // Execute the proper command
                commands.get(commandNum - 1).execute();
            } catch (NumberFormatException | IOException ex) {
                dio.write("Invalid choice, please try again\n");
            }
        }
    }
}
