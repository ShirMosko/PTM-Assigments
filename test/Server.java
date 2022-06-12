package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

    public interface ClientHandler {
        void session(Socket sessionSocket);
    }

    volatile boolean stop;

    public Server() {
        stop = false;
    }


    private void startServer(int port, ClientHandler client) {
        try {
            ServerSocket server = new ServerSocket(port);
            server.setSoTimeout(1000);
            try {
                while (!stop) {
                    Socket aClient = server.accept();
                    client.session(aClient);
                    aClient.close();
                }
            } catch (SocketTimeoutException ignored) {}
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // runs the server in its own thread
    public void start(int port, ClientHandler ch) {
        new Thread(() -> startServer(port, ch)).start();
    }

    public void stop() {
        stop = true;
    }
}
