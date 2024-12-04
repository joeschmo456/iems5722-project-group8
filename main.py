# uvicorn main:app --host 127.0.0.1 --port 8086

from datetime import date

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from fastapi import Request
from pydantic import BaseModel
from pymongo import MongoClient

from sqlalchemy import create_engine, Column, Integer, String, Text, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# for testing, you can update this one to your student ID
student_list = [123456789, "123456789", 1155226712, "1155226712"]

# initiate FastAPI
app = FastAPI()

# connect to atlas
DB_NAME = 'A4'
COLLECTION_NAME1 = 'chatrooms'
COLLECTION_NAME2 = 'messages'
COLLECTION_NAME3 = 'tokens'

# ATLAS_URI = 'mongodb+srv://imzhangxiangbo:MTodRaDOvI0vJujY@cluster0.4donu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0'
# atlas_client = MongoClient(ATLAS_URI)
# db = atlas_client[DB_NAME]

# chatrooms_collection = db[COLLECTION_NAME1]
# messages_collection = db[COLLECTION_NAME2]
# tokens_collection = db[COLLECTION_NAME3]

# MySQL连接配置
DATABASE_URL = "mysql+pymysql://root:root@localhost/android_project"
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


# 定义User类，对应数据库中的user表
class User(Base):
    __tablename__ = 'user'
    # 定义id字段，对应数据库中的自增主键
    id = Column(Integer, primary_key=True, autoincrement=True)
    # 定义account字段，对应数据库中的用户账号，设置为非空
    account = Column(String(255), nullable=False)
    # 定义password字段，对应数据库中的用户密码，设置为非空
    password = Column(String(255), nullable=False)
    # 定义nickname字段，对应数据库中的用户昵称，可为空
    nickname = Column(String(255))
    # 定义remark字段，对应数据库中的备注，类型为Text，可为空
    remark = Column(String(255))
    # 定义avatar字段，对应数据库中的头像（路径或URL），可为空
    avatar = Column(String(255))
    # 定义has_login字段，对应数据库中的登录状态，默认为False，类型为布尔型
    has_login = Column(Boolean, default=False)


# 请求体模型
class UserLogin(BaseModel):
    account: str
    password: str


# 登录和注册接口
@app.post("/login")
def login(user: UserLogin):
    db = SessionLocal()
    print(user)
    #     return 1
    db_user = db.query(User).filter(User.account == user.account).first()

    if db_user is None:
        # 用户没有注册，自动注册
        new_user = User(account=user.account, password=user.password, has_login=True)
        db.add(new_user)
        db.commit()
        return {"message": "用户注册成功"}

    # 用户已注册，验证密码
    if db_user.password == user.password:
        db_user.has_login = True
        db.commit()
        return {"message": "登录成功"}
    else:
        raise HTTPException(status_code=400, detail="密码错误")

import json
from typing import List, Optional, Dict, Any
import requests
import datetime
import hashlib
import hmac
import base64
import urllib.parse
import json
from pydantic import BaseModel
from typing import Union
class CCMessage(BaseModel):
    role: str
    content: str


class ChatRequest(BaseModel):
    messages: list[CCMessage]
    model: Optional[str] = None
    stream: bool = False

def genHunyuanLiteSignatureHeaders(payload_string: str, id: str, key: str) -> Dict[str, str]:
    secret_id = id
    secret_key = key

    service = 'hunyuan'
    host = 'hunyuan.tencentcloudapi.com'
    endpoint = f'https://{host}'
    action = 'ChatCompletions'
    version = '2023-09-01'
    algorithm = 'TC3-HMAC-SHA256'

    timestamp = int(datetime.datetime.now().timestamp())
    date = datetime.datetime.utcfromtimestamp(timestamp).strftime('%Y-%m-%d')

    http_request_method = 'POST'
    canonical_uri = '/'
    canonical_querystring = ''
    contentType = 'application/json; charset=utf-8'
    payload = payload_string

    canonical_headers = (
            'content-type:' + contentType + '\n' +
            'host:' + host + '\n' +
            'x-tc-action:' + action.lower() + '\n'
    )
    signed_headers = 'content-type;host;x-tc-action'
    hashed_request_payload = hashlib.sha256(payload.encode('utf-8')).hexdigest()

    canonical_request = (
            http_request_method + '\n' +
            canonical_uri + '\n' +
            canonical_querystring + '\n' +
            canonical_headers + '\n' +
            signed_headers + '\n' +
            hashed_request_payload
    )

    credential_scope = f'{date}/{service}/tc3_request'
    hashed_canonical_request = hashlib.sha256(canonical_request.encode('utf-8')).hexdigest()
    string_to_sign = (
            algorithm + '\n' +
            str(timestamp) + '\n' +
            credential_scope + '\n' +
            hashed_canonical_request
    )

    def sign(key: bytes, msg: str) -> bytes:
        hmac_sha256 = hmac.new(key, msg.encode('utf-8'), hashlib.sha256)
        return hmac_sha256.digest()

    secret_date = sign(('TC3' + secret_key).encode('utf-8'), date)
    secret_service = sign(secret_date, service)
    secret_signing = sign(secret_service, 'tc3_request')
    signature = hmac.new(secret_signing, string_to_sign.encode('utf-8'), hashlib.sha256).hexdigest()

    authorization = (
            algorithm + ' Credential=' + secret_id + '/' + credential_scope + ', SignedHeaders=' + signed_headers + ', Signature=' + signature
    )

    headers = {
        "X-TC-Action": action,
        "X-TC-Version": version,
        "X-TC-Timestamp": str(timestamp),
        "Content-Type": contentType,
        "Authorization": authorization,
    }

    return headers

def queryCusLLMSpecList(platform: str) -> list:
    return [{"cusLlm": "tencent_Hunyuan_Lite", "model": "hunyuan-lite"}]

@app.post("/tencent-cc-resp")
async def tencent_cc_resp(request: ChatRequest):
    specs = queryCusLLMSpecList("tencent")
    model = request.model or \
            next((e["model"] for e in specs if e["cusLlm"] == "tencent_Hunyuan_Lite"), None)
    if model is None:
        raise HTTPException(status_code=400, detail="Model not found")

    temp_body = {
        "Model": model,
        "Stream": request.stream,
        "Messages": [{"Role": m.role, "Content": m.content} for m in request.messages]
    }

    secret_id = "AKIDx15DNYo9QVP8evxlaSF768EPZkyGd6a3"  # 这里需要替换为实际获取用户密钥的逻辑，比如从配置文件、数据库等获取
    secret_key = "UsGe1VxNxV0U4eI1qlfrHjGkLyiVME99"  # 同理，替换为实际的密钥
    header = genHunyuanLiteSignatureHeaders(
        json.dumps(temp_body),
        secret_id,
        secret_key
    )

    try:
        response = requests.post(
            "https://hunyuan.tencentcloudapi.com",
            headers=header,
            json=temp_body
        )
        response.raise_for_status()
        if request.stream:
            # 处理流式响应，这里简化处理，实际可能需要更精细的逻辑来解析和返回流式数据
            return {"stream_data": response.iter_lines()}
        else:
            return response.json()
    except requests.RequestException as e:
        raise HTTPException(status_code=500, detail=f"Request error: {str(e)}")

# var TENCENT_SECRET_ID = "AKIDx15DNYo9QVP8evxlaSF768EPZkyGd6a3";
# var TENCENT_SECRET_KEY = "UsGe1VxNxV0U4eI1qlfrHjGkLyiVME99";

class ProfileEdit(BaseModel):
    password: str
    # 定义nickname字段，对应数据库中的用户昵称，可为空
    nickname: str
    # 定义remark字段，对应数据库中的备注，类型为Text，可为空
    remark: str
    # 定义avatar字段，对应数据库中的头像（路径或URL），可为空
    avatar: str


# 编辑个人信息
@app.get("/get_profiles")
def get_profiles(account: str):
    db = SessionLocal()
    print(account)
    # 匹配用户信息
    db_user = db.query(User).filter(User.account == account).first()
    print(db_user)
    # 返回搜索内容
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")

    # 将用户信息转换为字典
    user_dict = {
        "id": db_user.id,
        "account": db_user.account,
        "password": db_user.password,
        "nickname": db_user.nickname,
        "remark": db_user.remark,
        "avatar": db_user.avatar,
        "has_login": db_user.has_login
    }
    print(user_dict)

    # 返回搜索内容
    print({"data": {"profiles": [user_dict]}, "status": "OK"})
    return {"data": {"profiles": [user_dict]}, "status": "OK"}


# 编辑个人信息
@app.post("/edit_profiles")
async def edit_profiles(request: Request):
    item = await request.json()
    print(request, "\n", item)

    db = SessionLocal()
    # 匹配用户信息
    db_user = db.query(User).filter(User.account == item["account"]).first()
    print(db_user)

    db_user.password = item["password"]
    print(db_user.password)
    db_user.nickname = item["nickname"]
    print(db_user.nickname)
    db.commit()

    data = {"status": "OK"}
    print(request, "\n", data)
    return JSONResponse(content=jsonable_encoder(data))

# log out
@app.post("/logout")
async def logout(request: Request):
    item = await request.json()
    print(request, "\n", item)

    db = SessionLocal()
    # 匹配用户信息
    db_user = db.query(User).filter(User.account == item["account"]).first()
    print(db_user)

    db_user.has_login = item["loginStatus"]
    db.commit()

    data = {"status": "OK"}
    print(request, "\n", data)
    return JSONResponse(content=jsonable_encoder(data))


# get chatroom information
@app.get("/get_chatrooms")
async def get_chatrooms():
    chatroom = chatrooms_collection.find_one({}, {"_id": 0})
    return chatroom


# get message information according to chatroom_id
@app.get("/get_messages")
async def get_messages(chatroom_id: int):
    # check whether chatroom_id is correct or not
    # if chatroom_id not in [2, 3, 4]:
    #     print("ERROR chatroom_id")
    #     raise HTTPException(status_code=400, detail="ERROR chatroom_id")

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

    # if "chatroom_id" not in item.keys() or item["chatroom_id"] not in [2, 3, 4, 5, "2", "3", "4", "5"]:
    #     print("ERROR chatroom_id")
    #     raise HTTPException(status_code=400, detail="ERROR chatroom_id")
    #
    # if "user_id" not in item.keys() or item["user_id"] not in student_list:
    #     print("ERROR user_id")
    #     raise HTTPException(status_code=400, detail="ERROR user_id")
    #
    # if "name" not in item.keys() or len(item["name"]) > 20:
    #     print("ERROR name length")
    #     raise HTTPException(status_code=400, detail="ERROR name length")
    #
    # if "message" not in item.keys() or len(item["message"]) > 200:
    #     print("ERROR message length")
    #     raise HTTPException(status_code=400, detail="ERROR message length")

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
    sum = a + b
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
