from pydantic import BaseModel, EmailStr
from datetime import datetime

class UserRegister(BaseModel):
    username: str
    email: EmailStr
    password: str

class UserResponse(BaseModel):
    user_id: int
    username: str
    email: EmailStr
    is_active: bool

class UserActivate(BaseModel):
    email: EmailStr
    code: int

class ResendCode(BaseModel):
    email: EmailStr

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class LoginResponse(BaseModel):
    access_token: str
    refresh_token: str
    # token_type: str = "bearer"

class BaseRefreshToken(BaseModel):
    refresh_token: str

class UserLogout(BaseModel):
    refresh_token: str

class CartAdd(BaseModel):
    product_id: int
    quantity: int

class CartUpdate(BaseModel): # Sepette güncelleme.
    quantity: int

class FavoriteUpdate(BaseModel):
    product_id: int

class ReviewUpdate(BaseModel):
    comment: str

class ReviewResponse(BaseModel):
    review_id: int
    user_id: int
    username: str
    comment: str
    review_at: datetime
    is_owner: bool

class ProductResponse(BaseModel):
    product_id: int
    name: str
    description: str
    price: float
    stock: int
    image_url: str
    category: str
    is_active: bool # Uygulama arayüzü için.
    is_favorite: bool # Uygulama arayüzü için.
    has_purchased: bool
    reviews: list[ReviewResponse]

class CartResponse(BaseModel):
    product_id: int
    name: str
    description: str
    image_url: str
    price: float
    quantity: int
    total_price: float
    has_purchased: bool
    reviews: list[ReviewResponse]

class OrderItemResponse(BaseModel):
    product_id: int
    product_name: str
    image_url: str
    quantity: int
    unit_price: float

class OrderResponse(BaseModel):
    id: int
    total_price: float
    status: str
    created_at: datetime
    items: list[OrderItemResponse]