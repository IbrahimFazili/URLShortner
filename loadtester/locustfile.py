import os
from typing import Tuple, Union
from locust import FastHttpUser, task, events
from locust.user.wait_time import constant_throughput
from locust.runners import MasterRunner
import random
import string

ABORT_ON_FAILURE = False
TESTING_DB = False
DB_NODES = [
        "dh2020pc26.utm.utoronto.ca",
        "dh2026pc01.utm.utoronto.ca",
        "dh2026pc02.utm.utoronto.ca",
        "dh2020pc15.utm.utoronto.ca",
        "dh2020pc13.utm.utoronto.ca",
        # "dh2020pc01.utm.utoronto.ca"
    ]

@events.init.add_listener
def on_locust_init(environment, **_kwargs):
    if isinstance(environment.runner, MasterRunner):
        print(
            "Initializing Locust for load testing..."
        )

def GenRandomURLPair() -> Tuple[str, str]:
    longResource = "http://"+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
    shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
    return shortResource, longResource

class TestUser(FastHttpUser):
    wait_time = constant_throughput(2000)

    shortURLs = []

    # helper
    def SetNewURLPair(self, short: str, long: str) -> bool:
        if TESTING_DB:
            res = self.client.post(f"http://{random.choice(DB_NODES)}:5000/set?short={short}&long={long}", name="/set")
        else:
            res = self.client.put(f"/?short={short}&long={long}", name="/put")
        if res.status_code < 400:
            return True
        return False

    @task(30)
    def put_urlpair(self):
        short, long = GenRandomURLPair()
        if self.SetNewURLPair(short, long):
            self.shortURLs.append(short)

    @task(70)
    def get_longurl(self):
        if len(self.shortURLs) == 0:
            return

        if TESTING_DB:
            self.client.get(f"http://{random.choice(DB_NODES)}:5000/get?short={random.choice(self.shortURLs)}", name="/get")
        else:
            self.client.get(f"/{random.choice(self.shortURLs)}", name="/get")
