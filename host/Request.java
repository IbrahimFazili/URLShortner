import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class Request {
    private HashMap<String, String> headers;
    private HashMap<String, String> queryParameters;
    private final String method;
    private final String path;
    private final String httpVersionString;
    private byte[] body;

    private Socket socket;

    static String QUERY_PARAMETER = "\\?";
    static String AMP_PARAMETER = "\\&";
    static String EQUAL_PARAMETER = "\\=";

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        this.httpVersionString = "HTTP/1.1";
        this.headers = new HashMap<>();
        this.headers.put("Date: ", new Date().toString());
    }

    public Request(BufferedReader reader, Socket socket) throws IOException {
        this.socket = socket;
        headers = new HashMap<>();
        queryParameters = new HashMap<>();

        String line = reader.readLine();
        String[] firstLine = line.split(" ");
        assert firstLine.length == 3;
        method = firstLine[0];
        path = firstLine[1];
        httpVersionString = firstLine[2];

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                // end of header
                break;
            }

            String[] terms = line.split(":");
            headers.put(terms[0], terms[1].strip());
        }

        final int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

        ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
        while (reader.ready() && bodyStream.size() < contentLength) {
            bodyStream.write(reader.read());
        }

        body = bodyStream.toByteArray();

        // handle query parameters
        String[] params = path.split(QUERY_PARAMETER);
        if (params.length > 1) {
            String[] queryString = params[1].split(AMP_PARAMETER);
            for (String s : queryString) {
                String[] param = s.split(EQUAL_PARAMETER);
                queryParameters.put(param[0], param[1]);
            }
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getMethod() {
        return this.method;
    }

    public String getQuery(String query) {
        return this.queryParameters.get(query);
    }

    public String getPath() {
        return this.path;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    @Override
    public String toString() {
        String s = method + " " + path + " " + httpVersionString + "\n";
        for (String headerType : headers.keySet()) {
            s += headerType + ": " + headers.get(headerType) + "\n";
        }

        if (body != null)
            s += new String(body);

        return s;
    }
}
