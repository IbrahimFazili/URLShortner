
import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleProxyServer {
  static int minNumHosts = 4;
  static HashMap<String, HostStatus> activeHostMap = new HashMap<String, HostStatus>();
  static ArrayList<String> activeHost = new ArrayList<String>();
  static int RoundRobin = 0;
  static int remoteport = 3005; // this port is the connect port on each host
  static int localport = 8001; // this port is the one clients will connect to proxy server
  static int apiServerPort = 7000;
  static RequestRateTracker requestRateTracker = new RequestRateTracker(1);

  enum HostStatus {
    STARTING,
    OK,
    FAILED_HEALTHCHECK_ONCE,
    FAILED_HEALTHCHECK_TWICE,
    DEAD {
      @Override
      public HostStatus next() {
        return null; // see below for options for this line
      };
    };

    public HostStatus next() {
      // No bounds checking required here, because the last instance overrides
      return values()[ordinal() + 1];
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      if (args.length == 1) {
        // number of hosts was specified
        minNumHosts = Integer.parseInt(args[0]);
      }
      // SimpleProxyServer.requestRateTracker.Disable(); // TODO: test it & enable
      SimpleProxyServer.requestRateTracker.Enable();
      System.out.println("Starting proxy for multiple hosts:" + remoteport + " on port " + localport);
      parseHosts();
      startInitialHosts(remoteport, localport);
      // main method
      runAPIServer(apiServerPort);
      runServer(remoteport, localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  /*
   * Parses the hosts file and adds it to the activeHostMap.
   * This is to keep track of which hosts are in use
   */
  public static void parseHosts() {
    try {
      File file = new File("./hosts");
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String st;

      while ((st = reader.readLine()) != null) {
        String hostName = st;
        activeHostMap.put(hostName, HostStatus.DEAD);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Calls startHost method desiredActiveHosts number of times unless there's a
   * failure
   */
  public static void startInitialHosts(int remoteport, int localport) {
    int activeHosts = 0;
    boolean deployStatus = true;
    try {
      while (deployStatus == true) {
        deployStatus = startHost(remoteport, localport);
        if (deployStatus) {
          activeHosts += 1;
        }
        if (activeHosts == minNumHosts) {
          break;
        }
      }
      healthCheckHosts(remoteport, localport);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Go through each of the hosts and try to establish a connection.
   * Returns true if it can start a host, false if it can't start any host
   */
  public static boolean startHost(int remoteport, int localport) throws Exception {
    try {
      ArrayList<String> keys = new ArrayList<String>(activeHostMap.keySet());
      String[] cmdArray = new String[5];
      cmdArray[0] = "./initializeHost.sh";
      cmdArray[2] = InetAddress.getLocalHost().getHostName();
      cmdArray[3] = String.valueOf(remoteport);
      cmdArray[4] = String.valueOf(localport);

      for (String key : keys) {
        // we can only initialize a host if its not active
        if (activeHostMap.get(key) == HostStatus.DEAD) {
          cmdArray[1] = key;
          Process exec = Runtime.getRuntime().exec(cmdArray);
          int exitStatus = exec.waitFor();
          // existatus will be 1 if there's some error, 0 if its ok
          if (exitStatus == 0) {
            activeHostMap.put(key, HostStatus.STARTING);
            activeHost.add(key);
            return true;
          }
        }
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static boolean startSpecifiedHost(int remoteport, int localport, String hostName) throws Exception {
    try {
      String[] cmdArray = new String[5];
      cmdArray[0] = "./initializeHost.sh";
      cmdArray[2] = InetAddress.getLocalHost().getHostName();
      cmdArray[3] = String.valueOf(remoteport);
      cmdArray[4] = String.valueOf(localport);

      if (activeHostMap.get(hostName) == HostStatus.DEAD) {
        cmdArray[1] = hostName;
        Process exec = Runtime.getRuntime().exec(cmdArray);
        int exitStatus = exec.waitFor();
        // existatus will be 1 if there's some error, 0 if its ok
        if (exitStatus == 0) {
          activeHostMap.put(hostName, HostStatus.STARTING);
          activeHost.add(hostName);
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  /*
   * Calls the teardownActiveHosts script and kills the listening port for every
   * host running
   * mark the keys to false
   */
  public static void tearDownHosts() {
    try {
      String[] cmdArray = new String[] { "./teardownActiveHosts.sh" };
      Process exec = Runtime.getRuntime().exec(cmdArray);
      exec.waitFor();
      ArrayList<String> keys = new ArrayList<String>(activeHostMap.keySet());
      for (String key : keys) {
        activeHostMap.put(key, HostStatus.DEAD);
        activeHost.remove(key);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Calls the teardownHost script and kills the first active host it sees
   * marks the host as not active
   */
  public static void tearSingleHost(String host) throws Exception {
    String[] cmdArray = new String[2];
    cmdArray[0] = "./teardownHost.sh";
    try {
      cmdArray[1] = host;
      activeHost.remove(host);
      activeHostMap.put(host, HostStatus.DEAD);
      Process exec = Runtime.getRuntime().exec(cmdArray);
      exec.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeResponse(Request req, int returnCode, String body) {
    Response res;
    try {
      res = new Response(returnCode, String.format("%s\n", body));
      OutputStream out = req.getSocket().getOutputStream();
      out.write(res.getResponseString().getBytes());
      out.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void runAPIServer(int port) throws IOException {
    HTTPServer server = new HTTPServer(port);
    System.out.print(String.format("Starting API server on port %s\n", port));

    server.HandleFunc("/pinternal/healthcheck", (Request req) -> {
      writeResponse(req, 200, "Proxy is alive");
    });

    server.HandleFunc("/pinternal/addhost", (Request req) -> {
      String hostName = req.getQuery("host");
      try {
        if (hostName != null) {
          boolean didHostStart = startSpecifiedHost(remoteport, localport, hostName);
          if (didHostStart)
            writeResponse(req, 200, "Successfully added host");
          else
            writeResponse(req, 500, "Unable to start host " + hostName);
        } else {
          writeResponse(req, 400, "Invalid host name");
        }
      } catch (Exception e) {
        e.printStackTrace();
        writeResponse(req, 500, "Exception");
      }
    });

    server.HandleFunc("/pinternal/killhost", (Request req) -> {
      String hostName = req.getQuery("host");
      try {
        if (hostName != null && activeHostMap.get(hostName) == HostStatus.OK) {
          tearSingleHost(hostName);
          writeResponse(req, 200, "Successfully terminated host " + hostName);
        } else if (hostName == null) {
          writeResponse(req, 400, "Invalid host name");
        } else {
          writeResponse(req, 500, String.format("Host %s is not active", hostName));
        }
      } catch (Exception e) {
        e.printStackTrace();
        writeResponse(req, 500, "Exception");
      }
    });

    server.HandleFunc("/pinternal/hosts", (Request req) -> {
      StringBuilder stringBuilder = new StringBuilder("");
      activeHost.forEach(host -> {
        stringBuilder.append(String.format("%s|%s,", host, activeHostMap.get(host)));
      });
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      writeResponse(req, 200, stringBuilder.toString());
    });

    server.HandleFunc("/pinternal/requestrate", (Request req) -> {
      writeResponse(req, 200, "" + SimpleProxyServer.requestRateTracker.RequestsInWindow());
    });

    server.HandleFunc("/pinternal/sethosts", (Request req) -> {
      int newHostCount = Integer.parseInt(req.getQuery("hosts"));

      try {
        if (newHostCount <= 0) {
          writeResponse(req, 400, "Invalid number of hosts");
        } else {
          int diff = newHostCount - minNumHosts;
          while (diff > 0) {
            startHost(remoteport, localport);
            diff -= 1;
          }

          minNumHosts = newHostCount;
          writeResponse(req, 200, "Successfully changed number of hosts");
        }
      } catch (Exception e) {
        e.printStackTrace();
        writeResponse(req, 500, "Unable to change number of hosts");
      }
    });

    server.HandleFunc("/pinternal/minhosts", (Request req) -> {
      writeResponse(req, 200, Integer.toString(minNumHosts));
    });
    
    server.start();
  }

  public static void healthCheckHosts(int remoteport, int localport) {
    new Thread() {
      public void run() {
        try {
          while (true) {
            Thread.sleep(5000);
            ArrayList<String> toKill = new ArrayList<>();
            activeHost.stream().forEach(host -> {
              Socket hostSocket = null;
              try {
                hostSocket = new Socket(host, remoteport);
                final OutputStream streamToHost = hostSocket.getOutputStream();
                Request request = new Request("GET", "/hinternal/healthcheck");
                streamToHost.write(request.toString().getBytes(), 0, request.toString().getBytes().length);

                activeHostMap.put(host, HostStatus.OK);
              } catch (ConnectException e) {
                // host does not connect
                HostStatus newStatus = activeHostMap.get(host).next();
                activeHostMap.put(host, newStatus);

                if (newStatus == HostStatus.DEAD)
                  toKill.add(host);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
            toKill.stream().forEach(host -> {
              activeHost.remove(host);
            });
            if (activeHost.size() < minNumHosts) {
              startHost(remoteport, localport);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      };
    }.start();
  }

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(int remoteport, int localport)
      throws IOException {
    // Create a ServerSocket to listen for connections with, this is only for proxy
    // server
    ServerSocket ss = new ServerSocket(localport);

    while (true) {
      Socket client = null, hostServer = null;
      try {

        while (true) {
          String hostName = activeHost.get(RoundRobin);
          hostServer = new Socket(hostName, remoteport);

          // Wait for a connection on the local port
          client = ss.accept();
          new SimpleProxyThread(client, hostServer).start();

          SimpleProxyServer.requestRateTracker.NewRequest();

          int startIndex = RoundRobin;
          RoundRobin = (RoundRobin + 1) % activeHost.size();
          while (activeHostMap.get(activeHost.get(RoundRobin)) != HostStatus.OK && RoundRobin != startIndex) {
            RoundRobin = (RoundRobin + 1) % activeHost.size();
          }
        }

      } catch (IOException e) {
        System.err.println(e);
      } finally {
        try {
          if (hostServer != null)
            hostServer.close();
          if (client != null)
            client.close();
        } catch (IOException e) {
        }
      }
    }
  }
}
