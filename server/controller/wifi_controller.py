from models.database import mongodb

class WifiController:
    def save_wifi_data(self, data):
        id = mongodb.db.test.insert_one(data).inserted_id
        return str(id)

    def get_wifi_data(self):
        data = mongodb.db.test.find({})
        for i in data:
            print(i)
        return data