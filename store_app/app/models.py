from sqlmodel import SQLModel, Field
from pydantic import EmailStr
from datetime import datetime

class User(SQLModel, table=True):
    __tablename__ = "users"
    id: int | None = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True, max_length=50)
    email: EmailStr = Field(index=True, unique=True, max_length=255)
    password_hash: str = Field(max_length=255)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    is_active: bool = Field(default=False)

class RefreshToken(SQLModel, table=True):
    __tablename__ = "refresh_tokens"
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id", ondelete="CASCADE", index=True)
    token: str = Field(unique=True, index=True)
    expires_at: datetime
    created_at: datetime = Field(default_factory=datetime.utcnow)
    revoked: bool = Field(default=False)

class Category(SQLModel, table=True):
    __tablename__ = "categories"
    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(unique=True, max_length=255)

class Product(SQLModel, table=True):
    __tablename__ = "products"
    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(max_length=255)
    description: str
    price: float
    stock: int = Field(default=0)
    image_url: str
    category_id: int = Field(foreign_key="categories.id", ondelete="CASCADE", index=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    is_active: bool = Field(default=True)

class Cart(SQLModel, table=True):
    __tablename__ = "carts"
    id: int | None = Field(default=None, primary_key=True)
    user_id : int = Field(foreign_key="users.id", ondelete="CASCADE", index=True)
    product_id: int = Field(foreign_key="products.id", ondelete="CASCADE", index=True)
    quantity: int = Field(default=1) # Bu kaldırılabilir
    created_at: datetime = Field(default_factory=datetime.utcnow)

class Favorite(SQLModel, table=True):
    __tablename__ = "favorites"
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id", ondelete="CASCADE", index=True)
    product_id: int = Field(foreign_key="products.id", ondelete="CASCADE", index=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)

class Order(SQLModel, table=True):
    __tablename__ = "orders"
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id", ondelete="CASCADE", index=True)
    total_price: float
    status: str = Field(default="PENDING", max_length=30)
    created_at: datetime = Field(default_factory=datetime.utcnow)

class OrderItem(SQLModel, table=True):
    __tablename__ = "order_items"
    id: int | None = Field(default=None, primary_key=True)
    order_id: int = Field(foreign_key="orders.id", ondelete="CASCADE", index=True)
    product_id: int = Field(foreign_key="products.id", ondelete="CASCADE", index=True)
    quantity: int
    unit_price: float

class Review(SQLModel, table=True):
    __tablename__ = "reviews"
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id", ondelete="CASCADE", index=True)
    product_id: int = Field(foreign_key="products.id", ondelete="CASCADE", index=True)
    comment: str = Field(max_length=255)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)