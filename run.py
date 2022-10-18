import argparse, os, subprocess

PROJ_PATH = os.path.realpath(__file__).rstrip("/run.py")
JAVA_BUILD_SCRIPT = "./build_java_files.sh"
PROXY_TEARDOWN_SCRIPT = "./teardownActiveHosts.sh"
NODE_ENV_SCRIPT = "./setupNodeEnv.sh"
APP_START_SCRIPT = "./startApp.sh"

SUCCESS_RETURN_CODE = 0

numHostsToDeploy = 1
numDBNodesToDeploy = 1
dataReplicationFactor = 1

def main():
    print(f"Setting script working directory to {PROJ_PATH}")
    os.chdir(PROJ_PATH)

    print("Building Proxy & URLShortner")
    retcode = subprocess.call([f"{JAVA_BUILD_SCRIPT}"])
    assert retcode == SUCCESS_RETURN_CODE

    print("Building Web Application")
    os.chdir(os.path.join(PROJ_PATH, "admin-dashboard"))
    retcode = subprocess.call([f"{NODE_ENV_SCRIPT}"])
    assert retcode == SUCCESS_RETURN_CODE

    os.chdir(PROJ_PATH)
    databaseController = subprocess.Popen(["./db/DBController/DBController", "-rf", str(dataReplicationFactor), "-n", str(numDBNodesToDeploy), "-port", "3000"], stdout=subprocess.PIPE)
    print(f"Started Database Controller with pid {databaseController.pid}")

    os.chdir(os.path.join(PROJ_PATH, "proxy-server"))
    proxyServer = subprocess.Popen(["java", "SimpleProxyServer", str(numHostsToDeploy)], stdout=subprocess.PIPE)
    print(f"Started proxy server with pid {proxyServer.pid}")

    os.chdir(os.path.join(PROJ_PATH, "admin-dashboard"))
    retcode = subprocess.call([f"{APP_START_SCRIPT}"])
    print(f"Started web application")

    while True:
        userInput = input("Enter 'q' to stop the Shortner").strip().rstrip('\n')
        if userInput == "q":
            break

    proxyServer.kill()
    databaseController.kill()

    os.chdir(os.path.join(PROJ_PATH, "proxy-server"))
    retcode = subprocess.call([PROXY_TEARDOWN_SCRIPT], stdout=subprocess.PIPE)
    print(f"Ran proxy proxy + host teardown script with return code {retcode}")

    os.chdir(os.path.join(PROJ_PATH, "db", "DBController"))
    retcode = subprocess.call(["python3", "teardown.py"])
    print(f"Ran database teardown script with return code {retcode}")

    os.chdir(os.path.join(PROJ_PATH, "db", "DBController"))
    retcode = subprocess.call("kill $(lsof -i | grep node | awk '{print $2}')", shell=True)
    print(f"Closed web application with return code {retcode}")

if __name__ == "__main__":
    argparser = argparse.ArgumentParser(
        description="Launch URL shortner"
    )

    argparser.add_argument(
        "-hosts",
        type=int,
        default=1,
        help="Number of hosts to initially launch with",
    )

    argparser.add_argument(
        "-rf",
        type=int,
        default=1,
        help="Data replication factor for the database",
    )

    argparser.add_argument(
        "-dbnodes",
        type=int,
        default=1,
        help="Number of database nodes to initally launch with",
    )

    args = argparser.parse_args()
    numHostsToDeploy = args.hosts
    numDBNodesToDeploy = args.dbnodes
    dataReplicationFactor = args.rf

    main()
