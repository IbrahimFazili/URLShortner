import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeList extends Thread {
    static String DB_CONTROLLER = "http://localhost:3000/network";
    static ArrayList<Node> NODE_LIST = new ArrayList<Node>();

    public NodeList(ArrayList<Node> existingNodeList) {
        super("NodeList");
        NODE_LIST = existingNodeList;
    }

    public void run() {
        try {
            while (true) {
                NODE_LIST = fetchNodeList();
                sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Node> fetchNodeList() {
        ArrayList<Node> nodeList = new ArrayList<Node>();

        try {
            URL url = new URL(DB_CONTROLLER);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader dataIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String nodesString = dataIn.readLine();

            // This is extremely fragile. Not sure if there's a better way to do this.
            Pattern addr = Pattern
                    .compile("\"Addr\":\"(http:\\/\\/dh20[1,2][0,6]pc([0-4]\\d|50).utm.utoronto.ca:\\d{4})\"");
            Matcher maddr = addr.matcher(nodesString);

            Pattern id = Pattern.compile("\"ID\":(\\d)");
            Matcher mid = id.matcher(nodesString);

            Pattern state = Pattern.compile("\"State\":(\\d)");
            Matcher mstate = state.matcher(nodesString);

            while (maddr.find() && mid.find() && mstate.find()) {
                // Only add active nodes
                if (Integer.parseInt(mstate.group(1)) == 1) {
                    nodeList.add(
                            new Node(maddr.group(1), Integer.parseInt(mid.group(1)),
                                    Integer.parseInt(mstate.group(1))));
                }
            }

            if (dataIn != null) {
                dataIn.close();
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nodeList;
    }
}
