import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class Response {

    public enum ResponseCode{

        OK(200, "OK"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVOR_RESPONSE(500, "Internal Server Response");

        private final int statusCode;
        private final String statusMessage;
        public static final HashMap<Integer, ResponseCode> responseCodeMap = new HashMap<>();

        static {
            for (ResponseCode rc : ResponseCode.values()){
                responseCodeMap.put(rc.statusCode, rc);
            }
        }

        private ResponseCode(int statusCode, String statusMessage){
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
        }

        public int getStatusCode(){
            return this.statusCode;
        }

        public String getStatusMessage(){
            return this.statusMessage;
        }
    }

    private int statusCode;
    private String responseBody;

	public Response(int returnCode, String longResource, String serverMessage) throws IOException {
        String contentMimeType = "text/html";
        ResponseCode responseCode = ResponseCode.responseCodeMap.get(returnCode);
        responseBody = "";
        responseBody += String.format("HTTP/1.1 %d %s\n", responseCode.getStatusCode(), responseCode.getStatusMessage());
        responseBody += String.format("Location: %s\n", longResource);
        responseBody += String.format("Server: %s\n", serverMessage);
        responseBody += "Access-Control-Allow-Origin: *\n";
        responseBody += "Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization\n";
        responseBody += "Access-Control-Allow-Credentials: true\n";
        responseBody += "Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS\n";
        responseBody += "Date: " + new Date() + "\n";
        responseBody += "Content-type: " + contentMimeType + "\n";
    }

    public Response(int returnCode, String body) throws IOException {
        String contentMimeType = "text/html";
        ResponseCode responseCode = ResponseCode.responseCodeMap.get(returnCode);

        responseBody = getCommonHeader(responseCode);
        responseBody += "Content-type: " + contentMimeType + "\n";
        responseBody += "Content-length: " + body.length() + "\n";
        responseBody += "\n";
        responseBody += body;
    }

    public Response(int returnCode, String contentType, byte[] body) throws IOException {
        String contentMimeType = contentType;
        ResponseCode responseCode = ResponseCode.responseCodeMap.get(returnCode);

        responseBody = getCommonHeader(responseCode);
        responseBody += "Content-type: " + contentMimeType + "\n";
        responseBody += "Content-length: " + body.length + "\n";
        responseBody += "\n";
        responseBody += body;
    }

    public Response(BufferedReader reader) throws IOException{
        String line = reader.readLine();
        String[] firstLine = line.split(" ");
        assert firstLine.length == 3;
        statusCode = Integer.parseInt(firstLine[1]);
    }
    private String getCommonHeader(ResponseCode responseCode)
    {
        String responseBody;
        responseBody = "";
        responseBody += String.format("HTTP/1.1 %d %s\n", responseCode.getStatusCode(), responseCode.getStatusMessage());
        responseBody += String.format("Server: %s\n", "Java Proxy Server API");
        responseBody += "Date: " + new Date() + "\n";

        // Enable CORS
        responseBody += "Access-Control-Allow-Origin: *\n";
        responseBody += "Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization\n";
        responseBody += "Access-Control-Allow-Credentials: true\n";
        responseBody += "Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS\n";

        return responseBody;
    }

    public String getResponseString(){
        return this.responseBody;
    }

    public int getStatusCode(){
        return this.statusCode;
    }
}
