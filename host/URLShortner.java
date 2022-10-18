import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;


public class URLShortner {
	// port to listen connection
	static int PORT = 8080;

	static String LOAD_BALANCER_HOST_NAME = "dh2020pc05";
	static int LOAD_BALANCER_PORT = 8000;
	static boolean NO_LOG = true;
	static LRUCache cache = new LRUCache(1000);

	public static void main(String[] args) {
		try {
			Pattern port = Pattern.compile("^\\d{4}$");
			if (args.length >= 3) {
				if (!port.matcher(args[1]).matches() || !port.matcher(args[2]).matches()) {
					log("Invalid balancer / host port specified.");
					return;
				}

				// TODO: Account for 1 and 2 arguments
				LOAD_BALANCER_HOST_NAME = args[0];
				PORT = Integer.parseInt(args[1]);
				LOAD_BALANCER_PORT = Integer.parseInt(args[2]);
				NodeList.DB_CONTROLLER = String.format("http://%s.utm.utoronto.ca:3000/network", LOAD_BALANCER_HOST_NAME);
			}

			setup();
			log("Setup successful.");

			ServerSocket serverConnect = new ServerSocket(PORT);
			log("Server started.\nListening for connections on port : " + PORT + " ...\n");

			// we listen until user halts server execution
			while (true) {
				try {
					while (true) {
						log("Waiting for load balancer connection.");
						Socket client = serverConnect.accept();
						new URLShortnerThread(client, LOAD_BALANCER_HOST_NAME, LOAD_BALANCER_PORT, cache).start();
						log("Created thread.");
					}
				} catch (IOException e) {
					e.printStackTrace();
					log(e.getMessage());
				} finally {
					try {
						if (serverConnect != null) {
							serverConnect.close();
							log("Server closed.");
						}
					} catch (IOException e) {
						log("Server failed to close");
					}
				}
			}
		} catch (IOException e) {
			log("Server Connection error : " + e.getMessage());
		}
	}

	public static void setup() {
		ArrayList<Node> initialNodeList = NodeList.fetchNodeList();
		new NodeList(initialNodeList).start();

		setupLogger();
		log("Node list thread started.");
	}

	public static void setupLogger() {
		if (NO_LOG) return;

		try {
			String cmd = String.format("mkdir -p logs/%s", InetAddress.getLocalHost().getHostName());
			String[] cmdarray = new String[] { "/bin/sh", "-c", cmd };
			Runtime.getRuntime().exec(cmdarray);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String msg) {
		if (NO_LOG)
			return;

		try {
			String cmd = String.format("echo '[%s]: %s\\n' >> %s.log", new Date().toString(), msg,
					InetAddress.getLocalHost().getHostName());
			String[] cmdarray = new String[] { "/bin/sh", "-c", cmd };
			Runtime.getRuntime().exec(cmdarray, null,
					new File(String.format("logs/%s", InetAddress.getLocalHost().getHostName())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
