package test;


import test.Commands.DefaultIO;
import test.Server.ClientHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler {

    public void clientInteraction(SocketIO clientSocket) {
        CLI cli = new CLI(clientSocket);
        cli.start();
    }

    @Override
    public void session(Socket sessionSocket) {
        try {
            SocketIO aClient = new SocketIO(sessionSocket.getInputStream(), sessionSocket.getOutputStream());
            clientInteraction(aClient);
            aClient.write("bye\n");
            aClient.close();
        } catch (IOException ignored) {
        }
    }

    public class SocketIO implements DefaultIO {
        Scanner in;
        PrintWriter out;

        public SocketIO(InputStream fromClient, OutputStream toClient) {
            this.in = new Scanner(fromClient);
            this.out = new PrintWriter(toClient);
        }

        @Override
        public String readText() {
            return in.nextLine();
        }

        @Override
        public void write(String text) {
            out.print(text);
            out.flush();
        }

        @Override
        public float readVal() {
            return in.nextFloat();
        }

        @Override
        public void write(float val) {
            in.close();
            out.close();
        }

        public void close() {
            in.close();
            out.close();
        }
    }
}