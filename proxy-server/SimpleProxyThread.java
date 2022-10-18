
import java.io.*;
import java.net.*;

public class SimpleProxyThread extends Thread {

    private Socket clientSocket = null;
    private Socket hostSocket = null;

    public SimpleProxyThread(Socket clientSocket, Socket hostSocket) {
        super("SimpleProxyThread");
        this.clientSocket = clientSocket;
        this.hostSocket = hostSocket;
    }

    public void run() {
        byte[] clientRequest = new byte[1024];
        byte[] serverResponse = new byte[1024];

        try (
            final InputStream streamFromClient = clientSocket.getInputStream();
            final OutputStream streamToClient = clientSocket.getOutputStream();
            final InputStream streamFromServer = hostSocket.getInputStream();
            final OutputStream streamToServer = hostSocket.getOutputStream();
            ){
                int bytesRead, bytesReadFromServer;
                try {
                    if ((bytesRead = streamFromClient.read(clientRequest)) != -1) {
                        streamToServer.write(clientRequest, 0, bytesRead);
                        streamToServer.flush();

                        while ((bytesReadFromServer = streamFromServer.read(serverResponse)) != -1) {
                            streamToClient.write(serverResponse, 0, bytesReadFromServer);
                            streamToClient.flush();
                        }
                    }

                    hostSocket.close();
                    clientSocket.close();
                    } catch (IOException e) {
                }

                // the client closed the connection to us, so close our
                // connection to the server.
                try {
                    streamToServer.write("Closing this host bye bye".getBytes());
                    streamToServer.close();
                    } catch (IOException e) {
                }
            } catch (IOException e){
                e.printStackTrace();
            }
    }
}