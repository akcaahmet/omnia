from fastapi import FastAPI, Depends, Request
from fastapi.responses import JSONResponse
from typing import Annotated
from database import Session, get_session, create_db_and_tables
from schemas import UserRegister, UserActivate, ResendCode, UserLogin, UserLogout, ProductResponse, CartAdd, CartUpdate, CartResponse, FavoriteUpdate, ReviewUpdate, OrderResponse, OrderItemResponse
from crud import user_register, user_activate, send_code, user_login, verify_token, user_logout, all_products, add_cart, cart_product_update, cart_product_delete, cart_all_product_delete, all_cart, add_favorite, favorite_product_delete, favorite_all_product_delete, all_favorite, add_review, update_review, remove_review, order_complete, get_order
from dependencies import get_current_user_id, get_redis, get_refresh_token
from sqlalchemy.exc import OperationalError
from redis.exceptions import RedisError, ConnectionError as RedisConnectionError
from redis import Redis
from redis_db import redis_client
from contextlib import asynccontextmanager
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from exceptions.handler import custom_rate_limit_handler, postgres_operational_exception_handler, redis_exception_handler, global_exception_handler

SessionDep = Annotated[Session, Depends(get_session)]
UserIdDep = Annotated[int, Depends(get_current_user_id)]
RefreshTokenDep = Annotated[tuple[str, int], Depends(get_refresh_token)]
RedisDep = Annotated[Redis, Depends(get_redis)]

@asynccontextmanager
async def lifespan(app: FastAPI):
    create_db_and_tables()
    try:
        redis_client.ping()
    except Exception as e:
        raise RuntimeError("Redis'e bağlanılamadı") from e
    yield
    redis_client.close()

app = FastAPI(lifespan=lifespan, root_path="/api/v1") # on_startup=[create_db_and_tables],
limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
# app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler) # Hata mesajı özel yazılacak
app.add_exception_handler(RateLimitExceeded, custom_rate_limit_handler)
app.add_exception_handler(OperationalError, postgres_operational_exception_handler)
app.add_exception_handler(RedisError, redis_exception_handler)
app.add_exception_handler(Exception, global_exception_handler)
# CORS ayarı, rate limiting

# origins = ["http://localhost:8000", "http://127.0.0.1:8000"]
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=origins,
#     allow_credentials=True,
#     allow_methods=["GET", "POST", "PATCH", "DELETE"],
#     allow_headers=["Authorization", "Content-Type"]
# )

@app.get("/")
@limiter.limit("10/minute")
def root(request: Request):
    return "Hello World"

@app.post("/auth/register")
@limiter.limit("3/minute")
def register(user: UserRegister, session: SessionDep, redis: RedisDep, request: Request):
    return user_register(user, session, redis)

@app.post("/users/activate")
@limiter.limit("5/minute")
def activate(dependence: UserActivate, session: SessionDep, redis: RedisDep, request: Request):
    return user_activate(dependence, session, redis)

@app.post("/users/resend-activation")
@limiter.limit("1/minute")
def send_activate_code(dependence: ResendCode, session: SessionDep, redis: RedisDep, request: Request):
    return send_code(dependence, session, redis)

@app.post("/auth/login")
@limiter.limit("5/minute")
def login(user: UserLogin, session: SessionDep, request: Request):
    return user_login(user, session)

@app.post("/auth/refresh")
@limiter.limit("30/minute")
def refresh_token(refresh_data: RefreshTokenDep, session: SessionDep, request: Request):
    token, user_id = refresh_data
    return verify_token(token, user_id, session)

@app.post("/auth/logout")
def logout(user: UserLogout, session: SessionDep):
    return user_logout(user, session)

@app.get("/products", response_model=list[ProductResponse]) # Response model belirlenebilir
@limiter.limit("60/minute")
def products(user_id: UserIdDep, session: SessionDep, request: Request):
    return all_products(user_id, session)

@app.post("/cart/items")
@limiter.limit("10/minute")
def cart(dependence: CartAdd, user_id: UserIdDep, session: SessionDep, request: Request):
    return add_cart(dependence, user_id, session)

@app.patch("/cart/items/{product_id}") # Sepette güncelleme (ekleme, çıkarma)
@limiter.limit("60/minute")
def cart_update(product_id: int, dependence: CartUpdate, user_id: UserIdDep, session: SessionDep, request: Request): # Detay sayfası için kullanılacak
    return cart_product_update(product_id, dependence, user_id, session)

@app.delete("/cart/items/{product_id}") # Sepette silme
@limiter.limit("30/minute")
def cart_delete(product_id: int, user_id: UserIdDep, session: SessionDep, request: Request):
    return cart_product_delete(product_id, user_id, session)

@app.delete("/cart") # Sepetteki tüm ürünleri silme
@limiter.limit("10/minute")
def cart_clean(user_id: UserIdDep, session: SessionDep, request: Request):
    return cart_all_product_delete(user_id, session)

@app.get("/cart", response_model=list[CartResponse])
@limiter.limit("60/minute")
def cart_get(user_id: UserIdDep, session: SessionDep, request: Request):
    return all_cart(user_id, session)

@app.post("/favorites") # Sadece ekleme yapılacak
@limiter.limit("30/minute")
def favorite(dependence: FavoriteUpdate, user_id: UserIdDep, session: SessionDep, request: Request):
    return add_favorite(dependence, user_id, session)

@app.delete("/favorites/{product_id}")
@limiter.limit("30/minute")
def favorite_delete(product_id: int, user_id: UserIdDep, session: SessionDep, request: Request):
    return favorite_product_delete(product_id, user_id, session)

@app.delete("/favorites")
@limiter.limit("10/minute")
def favorite_clean(user_id: UserIdDep, session: SessionDep, request: Request):
    return favorite_all_product_delete(user_id, session)

@app.get("/favorites", response_model=list[ProductResponse])
@limiter.limit("60/minute")
def favorite_get(user_id: UserIdDep, session: SessionDep, request: Request):
    return all_favorite(user_id, session)

@app.post("/products/{product_id}/reviews")
@limiter.limit("5/minute")
def review(dependence: ReviewUpdate, product_id: int, user_id: UserIdDep, session: SessionDep, request: Request):
    return add_review(dependence, product_id, user_id, session)

@app.put("/products/{product_id}/reviews/{review_id}")
@limiter.limit("5/minute")
def review_update(dependence: ReviewUpdate, product_id: int, review_id: int, user_id: UserIdDep, session: SessionDep, request: Request):
    return update_review(dependence, product_id, review_id, user_id, session)

@app.delete("/products/{product_id}/reviews/{review_id}")
@limiter.limit("5/minute")
def review_delete(product_id: int, review_id: int, user_id: UserIdDep, session: SessionDep, request: Request):
    return remove_review(product_id, review_id, user_id, session)

@app.post("/order")
@limiter.limit("3/minute")
def order(user_id: UserIdDep, session: SessionDep, request: Request):
    return order_complete(user_id, session)

@app.get("/order", response_model=list[OrderResponse])
@limiter.limit("30/minute")
def order_get(user_id: UserIdDep, session: SessionDep, request: Request):
    return get_order(user_id, session)