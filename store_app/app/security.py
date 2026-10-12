import jwt
import datetime

ALGORITHM = "HS256"

# Veri tipleri yazılmayabilir

def create_access_token(user_id: int):
    payload = {"user_id": user_id, "type": "access", "exp": datetime.datetime.utcnow() + datetime.timedelta(minutes=15)}
    return jwt.encode(payload, "SUPER_SECRET_KEY!", algorithm=ALGORITHM)

def create_refresh_token(user_id: int):
    expire = datetime.datetime.utcnow() + datetime.timedelta(days=7) # Geçerlilik süresi
    payload = {"user_id": user_id, "type": "refresh", "exp": expire}
    token = jwt.encode(payload, "SUPER_SECRET_KEY!", algorithm=ALGORITHM) # Token
    return token, expire

def decode_token(token: str):
    return jwt.decode(token, "SUPER_SECRET_KEY!", algorithms=[ALGORITHM])