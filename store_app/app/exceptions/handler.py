from fastapi import Request
from fastapi.responses import JSONResponse
from slowapi.errors import RateLimitExceeded
from sqlalchemy.exc import OperationalError
from redis.exceptions import RedisError, ConnectionError as RedisConnectionError

def custom_rate_limit_handler(request: Request, exc: RateLimitExceeded):
    return JSONResponse(
        status_code=429,
        content={
            "success": False,
            "message": "Çok fazla istek gönderdiniz. Lütfen 1 dakika sonra tekrar deneyin.",
            "error_code": "RATE_LIMIT_EXCEEDED"
        }
    )

def postgres_operational_exception_handler(request: Request, exc: OperationalError):
    return JSONResponse(
        status_code=503,
        content={
            "success": False,
            "message": "Veri tabanı geçici olarak kullanılamıyor. Lütfen daha sonra tekrar deneyiniz.",
            "error_code": "DATABASE_UNAVAILABLE"
        }
    )

def redis_exception_handler(request: Request, exc: RedisError):
    if isinstance(exc, RedisConnectionError):
        return JSONResponse(
            status_code=503,
            content={
                "success": False,
                "message": "Önbellek servisine erişilemiyor.",
                "error_code": "CACHE_UNAVAILABLE"
            }
        )
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "message": "Önbellek işleminde bir hata oluştu.",
            "error_code": "CACHE_ERROR"
        }
    )

def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "message": "Beklenmeyen bir hata oluştu.",
            "error_code": "INTERNAL_SERVER_ERROR"
        }
    )