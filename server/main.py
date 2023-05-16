from fastapi import FastAPI
from controller.wifi_controller import WifiController
from models.database import mongodb
from pydantic import BaseModel, Field

app = FastAPI()
wifi_controller = WifiController()

class WifiItem(BaseModel):
    wifi: dict[str, str] = Field()

@app.on_event("startup")
def on_app_start():
    mongodb.connect()

@app.on_event("shutdown")
async def on_app_shutdown():
    mongodb.close()

@app.post("/save")
def save_wifi_data(data: WifiItem):
    res = wifi_controller.save_wifi_data(data.wifi)
    return res

@app.get("/get")
def get_wifi_data():
    wifi = wifi_controller.get_wifi_data()
    return wifi