import java.io.*;
import java.net.*;
import java.util.regex.*;

import java.util.Date;
import java.util.Random;

public class URLShortnerThread extends Thread {
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String REDIRECT_RECORDED = "redirect_recorded.html";
	static final String REDIRECT_RECORDED_TEMPLATE = "redirect_recorded_template.html";
	static final String REDIRECT_NOT_RECORDED = "redirect_not_recorded.html";
	static final String REDIRECT = "redirect.html";
	static final String HEALTHCHECK_ROUTE = "hinternal/healthcheck";

	// verbose mode
	static final boolean verbose = true;

	static boolean NO_LOG = true;

	private Socket connect = null;
	private String loadBalancerHostName = null;
	private int loadBalancerPort = 8000;
	private LRUCache cache;

	public URLShortnerThread(Socket hostSocket, String loadBalancerHostName, int loadBalancerPort, LRUCache cache) {
		super("URLShortnerThread");
		this.connect = hostSocket;
		this.loadBalancerHostName = loadBalancerHostName;
		this.loadBalancerPort = loadBalancerPort;
		this.cache = cache;
	}

	public void run() {
		log("Inside the thread.");
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			log("Opened input/output streams.");

			String input = in.readLine();

			if (verbose)
				log("first line: " + input);

			Pattern poptions = Pattern.compile("^OPTIONS\\s+/.+$");
			Pattern pput = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
			Pattern pget = Pattern.compile("^(\\S+)\\s+/(\\S+)\\s+(\\S+)$");
			Matcher moptions = poptions.matcher(input);
			Matcher mput = pput.matcher(input);
			Matcher mget = pget.matcher(input);

			if (moptions.matches()) {
				out.println("HTTP/1.1 200 OK");
				out.println("Server: Java HTTP Server/Shortner : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: text/html");
				out.println("Content-length: 0");

				// Enable CORS
				out.println("Access-Control-Allow-Origin: *");
				out.println(
						"Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization");
				out.println("Access-Control-Allow-Credentials: true");
				out.println("Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS");

				out.println();
				out.flush();
			} else if (mput.matches()) {
				String shortResource = mput.group(1);
				String longResource = mput.group(2);
				String httpVersion = mput.group(3);

				String delay = null;

				this.cache.put(shortResource, longResource);

				log("Saving to database");
				if ((delay = save(shortResource, longResource)) != null) {
					log("Successfully saved to database.");
					File templateFile = new File(WEB_ROOT, REDIRECT_RECORDED_TEMPLATE);
					writeShortToTemplate(shortResource, templateFile, loadBalancerHostName, loadBalancerPort, delay);

					File file = new File(WEB_ROOT, REDIRECT_RECORDED);
					int fileLength = (int) file.length();
					String contentMimeType = "text/html";

					// read content to return to client
					byte[] fileData = readFileData(file, fileLength);

					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server/Shortner : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + contentMimeType);
					out.println("Content-length: " + fileLength);

					// Enable CORS
					out.println("Access-Control-Allow-Origin: *");
					out.println(
							"Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization");
					out.println("Access-Control-Allow-Credentials: true");
					out.println("Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS");

					out.println();
					out.flush();

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				} else {
					File file = new File(WEB_ROOT, REDIRECT_NOT_RECORDED);
					int fileLength = (int) file.length();
					String contentMimeType = "text/html";
					// read content to return to client
					byte[] fileData = readFileData(file, fileLength);

					out.println("HTTP/1.1 500 Internal Server Error");
					out.println("Server: Java HTTP Server/Shortner : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + contentMimeType);
					out.println("Content-length: " + fileLength);

					// Enable CORS
					out.println("Access-Control-Allow-Origin: *");
					out.println(
							"Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization");
					out.println("Access-Control-Allow-Credentials: true");
					out.println("Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS");

					out.println();
					out.flush();

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
			} else if (mget.matches()) {
				String method = mget.group(1);
				String shortResource = mget.group(2);
				String httpVersion = mget.group(3);
				if (shortResource.strip().equals(HEALTHCHECK_ROUTE)) {
					handleHealthCheck(out);
					return;
				}

				log("Getting long URL");

				String longResource;
				String cachedURL = this.cache.get(shortResource);
				if (cachedURL != null) {
					log("Found url in cache");
					longResource = cachedURL;
				} else {
					longResource = find(shortResource);
					if (longResource != null)
						this.cache.put(shortResource, longResource);
				}

				if (longResource != null) {
					log("Found long URL. Redirecting");
					File file = new File(WEB_ROOT, REDIRECT);
					int fileLength = (int) file.length();
					String contentMimeType = "text/html";

					// read content to return to client
					byte[] fileData = readFileData(file, fileLength);

					out.println("HTTP/1.1 307 Temporary Redirect");
					out.println("Location: " + longResource);
					out.println("Server: Java HTTP Server/Shortner : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + contentMimeType);
					out.println("Content-length: " + fileLength);

					// Enable CORS
					out.println("Access-Control-Allow-Origin: *");
					out.println(
							"Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization");
					out.println("Access-Control-Allow-Credentials: true");
					out.println("Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS");

					out.println();
					out.flush();

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				} else {
					log("Could not find long URL.");
					File file = new File(WEB_ROOT, FILE_NOT_FOUND);
					int fileLength = (int) file.length();
					String content = "text/html";
					byte[] fileData = readFileData(file, fileLength);

					out.println("HTTP/1.1 404 File Not Found");
					out.println("Server: Java HTTP Server/Shortner : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);

					// Enable CORS
					out.println("Access-Control-Allow-Origin: *");
					out.println(
							"Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization");
					out.println("Access-Control-Allow-Credentials: true");
					out.println("Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS");

					out.println();
					out.flush();

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
			}
		} catch (Exception e) {
			log("Server error");
		} finally {
			try {
				in.close();
				out.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				log("Error closing stream : " + e.getMessage());
			}

			if (verbose) {
				log("Connection closed.\n");
			}
		}
	}

	private static String find(String shortURL) {
		if (NodeList.NODE_LIST.size() == 0) {
			log("No active nodes for find request.");
			return null;
		}

		String longURL = null;

		try {
			int nodeIndex = new Random().nextInt(NodeList.NODE_LIST.size());

			URL url = new URL(
					String.format("%s/get?short=%s", NodeList.NODE_LIST.get(nodeIndex).getAddr(), shortURL));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedReader dataIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			longURL = dataIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			log(e.getMessage());
		}

		return longURL;
	}

	private static String save(String shortURL, String longURL) {
		if (NodeList.NODE_LIST.size() == 0) {
			log("No active nodes for save request.");
			return null;
		}

		String result = null;

		try {
			String params = String.format("short=%s&long=%s", shortURL, longURL);
			int nodeIndex = new Random().nextInt(NodeList.NODE_LIST.size());

			URL url = new URL(
					String.format("%s/set?short=%s&long=%s", NodeList.NODE_LIST.get(nodeIndex).getAddr(),
							shortURL,
							longURL));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));

			connection.setDoOutput(true);
			connection.setUseCaches(false);

			BufferedOutputStream dataOut = new BufferedOutputStream(connection.getOutputStream());

			dataOut.write(params.getBytes());
			dataOut.flush();
			dataOut.close();

			BufferedReader dataIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			result = dataIn.readLine();

			if (dataIn != null) {
				dataIn.close();
			}
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			log(e.getMessage());
			return null;
		}

		return result;
	}

	private static byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	private static void writeShortToTemplate(String shortURL, File templateFile, String loadBalancerHostName,
			int loadBalancerHostPort, String delay) throws IOException, UnknownHostException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(templateFile)));

		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.contains("$short")) {
				line = line.replace("$short", String.format("http://%s.utm.utoronto.ca:%d/%s", loadBalancerHostName,
						loadBalancerHostPort, shortURL));
			}

			if (line.contains("$delay")) {
				line = line.replace("$delay", delay);
			}

			sb.append(line + "\r\n");
		}
		in.close();

		BufferedWriter out = new BufferedWriter(new FileWriter(REDIRECT_RECORDED));
		out.write(sb.toString());

		out.flush();
		out.close();

		log("Wrote short to template.");
	}

	private static void handleHealthCheck(PrintWriter out) {
		out.println("HTTP/1.1 200 OK");
		out.println("Server: Java HTTP Server/Shortner : 1.0");
		out.println("Date: " + new Date());
		out.println();
		out.flush();
	}

	public static void log(String msg) {
		if (NO_LOG)
			return;

		try {
			String cmd = String.format("echo '[%s]: %s\\n' >> %s.thread%d.log", new Date().toString(), msg,
					InetAddress.getLocalHost().getHostName(), Thread.currentThread().getId());
			String[] cmdarray = new String[] { "/bin/sh", "-c", cmd };
			Runtime.getRuntime().exec(cmdarray, null,
					new File(String.format("logs/%s", InetAddress.getLocalHost().getHostName())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
