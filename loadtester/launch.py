import argparse
import multiprocessing
import os
import subprocess
from typing import List, Union
import json

active_processes: List["subprocess.Popen[bytes]"] = []
DIR_PATH = os.path.realpath(__file__).rstrip(f"{os.path.sep}launch.py")
SOLO_LAUNCH_CMD = f"python3 -m locust -f {os.path.join(DIR_PATH, 'locustfile.py')}"

DEPLOY_WORKER_SCRIPT = "/student/guptalak/Desktop/CSC409/repo_a1group68/a1/a1/loadtester/deployworker.sh"
KILL_WORKER_SCRIPT = "/student/guptalak/Desktop/CSC409/repo_a1group68/a1/a1/loadtester/killworker.sh"
MASTER_ADDR = "dh2020pc06.utm.utoronto.ca"
USE_REMOTE_WORKERS = True
remoteMachines: List[str] = []
active_workers: List[str] = []

def WorkerCount(arg) -> int:
    try:
        v = int(arg)
    except ValueError:
        raise argparse.ArgumentTypeError("Arg should be a valid int")

    if v < 0:
        raise argparse.ArgumentTypeError("Arg must be greater than 0")

    return v


def get_launch_cmd(is_master=False):
    return f"python3 -m locust -f {os.path.join(DIR_PATH, 'locustfile.py')} --{'master' if is_master else 'worker'}"


def run_master_node(is_distributed=True):
    try:
        master_node = subprocess.Popen(
            get_launch_cmd(is_master=True).split(" ")
            if is_distributed
            else SOLO_LAUNCH_CMD.split(" ")
        )
    except Exception as e:
        print(e)
        exit()
    print(f"Master node running with pid = {master_node.pid}")
    active_processes.append(master_node)


def run_worker_nodes(worker_count, remote=False):
    if not remote:
        for _ in range(worker_count):
            worker = subprocess.Popen(get_launch_cmd().split(" "))
            print(f"Worker node running with pid = {worker.pid}")
            active_processes.append(worker)
    else:
        for host in remoteMachines:
            if worker_count == 0:
                break
            retcode = subprocess.call([DEPLOY_WORKER_SCRIPT, host, MASTER_ADDR])
            if retcode == 0:
                print(f"Deployed worker {host}")
                active_workers.append(host)
            worker_count -= 1


def main(worker_count: Union[int, None] = None):
    with open( "hosts.json", "rb" ) as f:
        hosts = json.loads(f.read())["hosts"]
        for host in hosts:
            remoteMachines.append(host)

    active_processes.clear()
    run_master_node(is_distributed=worker_count is None or worker_count > 0)

    if not USE_REMOTE_WORKERS:
        # leave one thread to avoid system freezes | 1 already used by master node
        max_workers = multiprocessing.cpu_count() - 2
        usable_workers = (
            min(worker_count, max_workers) if worker_count is not None else max_workers
        )

        run_worker_nodes(usable_workers)
    else:
        run_worker_nodes(worker_count if worker_count is not None else 1, USE_REMOTE_WORKERS)

    while True:
        usr_cmd = input('Enter "q" to exit').rstrip("\n")
        if usr_cmd == "q":
            for worker in active_workers:
                retcode = subprocess.call([KILL_WORKER_SCRIPT, worker])
                print(f"Killed worker {worker} with retcode {retcode}")
            
            for process in active_processes:
                process.kill()
            break


if __name__ == "__main__":
    argparser = argparse.ArgumentParser(
        description="Launch locust load tester in distributed mode"
    )

    argparser.add_argument(
        "--workers",
        type=WorkerCount,
        default=None,
        help="Number of workers for the load test. Setting this to 0 launches locust in non-distributed mode",
    )

    args = argparser.parse_args()
    main(args.workers)
