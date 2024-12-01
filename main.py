from datetime import date

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from fastapi import Request
from pydantic import BaseModel
from pymongo import MongoClient

# for testing, you can update this one to your student ID
student_list = [123456789, "123456789", 1155226712, "1155226712"]

# initiate FastAPI
app = FastAPI()

# connect to atlas
DB_NAME = 'A4'
COLLECTION_NAME1 = 'chatrooms'
COLLECTION_NAME2 = 'messages'
COLLECTION_NAME3 = 'tokens'

ATLAS_URI = 'mongodb+srv://imzhangxiangbo:MTodRaDOvI0vJujY@cluster0.4donu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0'
atlas_client = MongoClient(ATLAS_URI)
db = atlas_client[DB_NAME]

chatrooms_collection = db[COLLECTION_NAME1]
messages_collection = db[COLLECTION_NAME2]
tokens_collection = db[COLLECTION_NAME3]

# get chatroom information
@app.get("/get_chatrooms")
async def get_chatrooms():
    chatroom = chatrooms_collection.find_one({}, {"_id": 0})
    return chatroom


# get message information according to chatroom_id
@app.get("/get_messages")
async def get_messages(chatroom_id: int):
    # check whether chatroom_id is correct or not
    if chatroom_id not in [2, 3, 4]:
        print("ERROR chatroom_id")
        raise HTTPException(status_code=400, detail="ERROR chatroom_id")

    message = messages_collection.find_one({"chatroom_id": chatroom_id}, {"_id": 0, "chatroom_id": 0})
    return message


# post message to server
@app.post("/send_message/")
async def send_message(request: Request):
    item = await request.json()
    print(request, "\n", item)
    list_of_keys = list(item.keys())

    # check the format of information input
    if len(list_of_keys) != 5:
        print("ERROR list_of_keys")
        raise HTTPException(status_code=400, detail="ERROR list_of_keys")

    if "chatroom_id" not in item.keys() or item["chatroom_id"] not in [2, 3, 4, "2", "3", "4"]:
        print("ERROR chatroom_id")
        raise HTTPException(status_code=400, detail="ERROR chatroom_id")

    if "user_id" not in item.keys() or item["user_id"] not in student_list:
        print("ERROR user_id")
        raise HTTPException(status_code=400, detail="ERROR user_id")

    if "name" not in item.keys() or len(item["name"]) > 20:
        print("ERROR name length")
        raise HTTPException(status_code=400, detail="ERROR name length")

    if "message" not in item.keys() or len(item["message"]) > 200:
        print("ERROR message length")
        raise HTTPException(status_code=400, detail="ERROR message length")

    # remove "chatroom_id" before insertion
    chatroom_id = item["chatroom_id"]
    item.pop("chatroom_id")

    # check whether successfully insert data
    try:
        messages_collection.update_one(
            {"chatroom_id": chatroom_id},

            {"$push": {
                "data.messages": {
                    "$each": [item],
                    "$position": 0
                }
            }
            }
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

    data = {"status": "OK"}
    print(request, "\n", data)
    return JSONResponse(content=jsonable_encoder(data))


# post FCM token to server
@app.post("/submit_push_token/")
async def submit_push_token(request: Request):
    item = await request.json()
    print(request, "\n", item)
    user_id = item["user_id"]
    token = item["token"]

    # check whether the user_id existing or not
    try:
        existing_token = tokens_collection.find_one({"user_id": user_id})

        if existing_token:
            tokens_collection.update_one(
                {"user_id": user_id}
                , {"$set": {"token": token}}
            )
        else:
            tokens_collection.insert_one(
                {"user_id": user_id, "token": token}
            )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

    data = {"status": "OK"}
    print(request, "\n", data)
    return JSONResponse(content=jsonable_encoder(data))

# from app.py
# define a route, binding a function to a URL (e.g. GET method) of the server
@app.get("/")
async def root():
    return {"message": "Hello World"}  # the API returns a JSON response

@app.get("/demo/")
async def get_demo(a: int = 0, b: int = 0, status_code=200):
    sum = a+b
    data = {"sum": sum, "date": date.today()}
    return JSONResponse(content=jsonable_encoder(data))

class DemoItem(BaseModel):
    a: int
    b: int

@app.post("/demo/")
async def post_demo(item: DemoItem):
    print(item)
    if item.a + item.b == 10:
        data = {"status": "OK"}
        return JSONResponse(content=jsonable_encoder(data))

    data = {"status": "ERROR"}
    return JSONResponse(content=jsonable_encoder(data))

