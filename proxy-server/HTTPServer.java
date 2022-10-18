import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

abstract interface RequestHandler {
    public void Handle(Request request);
}

public class HTTPServer extends Thread {
    public final int port;

    public HashMap<String, RequestHandler> routes;

    public HTTPServer(int port) {
        super();
        this.port = port;
        this.routes = new HashMap<>();
    }

    public void HandleFunc(String route, RequestHandler handler) {
        this.routes.put(route, handler);
    }

    @Override
    public void run() {
        try {
            ServerSocket apiServer = new ServerSocket(port);
            while (true) {
                Socket client = null;
                try {
                    while (true) {
                        client = apiServer.accept();
                        (new APIThread(client, this)).run();
                    }

                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
