
import java.io.*;
import java.net.*;

public class APIThread extends Thread {

    private Socket clientSocket = null;
    private HTTPServer server = null;

    public APIThread(Socket clientSocket, HTTPServer server) {
        super("APIThread");
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public void run()
    {
        try
        {
            Request request = new Request(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())), this.clientSocket);
            String basePath = request.getPath().split("\\?")[0];
            RequestHandler handler = this.server.routes.get(basePath);
            if ( handler != null )
            {
                handler.Handle(request);
            } else
            {
                byte[] res = (new Response(404, "Invalid route\n")).getResponseString().getBytes();
                final OutputStream streamToClient = clientSocket.getOutputStream();
                streamToClient.write(res);
                streamToClient.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}