from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi

from config import MONGO_DB_URL


class MongoDB:
    def __init__(self):
        self.client = None
        self.db = None

    def connect(self):
        self.client = MongoClient(MONGO_DB_URL, server_api=ServerApi('1'))
        try:
            self.client.admin.command('ping')
            self.db = self.client.wifi
            print("Pinged your deployment. You successfully connected to MongoDB!")
        except Exception as e:
            print(e)

    def close(self):
        self.client.close()

mongodb = MongoDB()
