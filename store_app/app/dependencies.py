from fastapi import HTTPException, Depends
from fastapi.security import OAuth2PasswordBearer
from security import decode_token
from redis_db import redis_client

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

def get_current_user_id(token: str = Depends(oauth2_scheme)):
    try:
        payload = decode_token(token)
        return payload["user_id"]
    except Exception:
        raise HTTPException(status_code=401, detail="Oturum bulunamadı")

def get_refresh_token(token: str = Depends(oauth2_scheme)):
    try:
        payload = decode_token(token)
        if payload.get("type") != "refresh":
            raise HTTPException(status_code=401, detail="Geçersiz refresh token")
        return token, payload["user_id"]
    except:
        raise HTTPException(status_code=401, detail="Oturum süresi doldu")
    
def get_redis():
    return redis_client