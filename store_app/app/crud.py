from fastapi import HTTPException
from schemas import UserRegister, UserResponse, UserActivate, UserLogin, LoginResponse, UserLogout, ProductResponse, CartAdd, CartUpdate, CartResponse, FavoriteUpdate, ReviewUpdate, ReviewResponse, OrderResponse, OrderItemResponse
from sqlmodel import Session, select, delete, exists
from models import User, RefreshToken, Category, Product, Cart, Favorite, Order, OrderItem, Review
from security import create_access_token, create_refresh_token, decode_token
import bcrypt
from utils import generate_code, send_email
from datetime import datetime, UTC, timedelta
from redis import Redis
    
def user_register(user: UserRegister, db: Session, redis: Redis):
    if db.exec(select(User).where(User.username==user.username)).first():
        raise HTTPException(status_code=409, detail="Kullanıcı adı zaten mevcut")
    if db.exec(select(User).where(User.email==user.email)).first():
        raise HTTPException(status_code=409, detail="Email zaten mevcut")
    hashed_password = bcrypt.hashpw(user.password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
    new_user = User(username=user.username, email=user.email, password_hash=hashed_password)
    db.add(new_user)
    try:
        db.commit()
        db.refresh(new_user)
    except Exception:
        db.rollback()
        raise HTTPException(status_code=500, detail="Kayıt oluşturulamadı")
    code = generate_code()
    try:
        redis.set(new_user.id, code, ex=600)
        send_email(user.email, code)
        return UserResponse(user_id=new_user.id, username=new_user.username, email=new_user.email, is_active=new_user.is_active)
    except Exception:
        raise HTTPException(status_code=500, detail="Doğrulama kodu gönderilemedi")
        
def send_code(dependence: UserActivate, db: Session, redis: Redis):
    user_db = db.exec(select(User).where(User.email==dependence.email)).first()
    if user_db is None:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    if user_db.is_active:
        raise HTTPException(status_code=409, detail="Hesap zaten aktif")
    code = redis.get(user_db.id)
    if code is not None:
        redis.delete(user_db.id) 
    try:
        code = generate_code()
        redis.set(user_db.id, code, ex=600)
        send_email(user_db.email, code)
        return UserResponse(user_id=user_db.id, username=user_db.username, email=user_db.email, is_active=user_db.is_active)
    except Exception:
        raise HTTPException(status_code=500, detail="Doğrulama kodu gönderilemedi")
    
def user_activate(dependence: UserActivate, db: Session, redis: Redis):
    user_db = db.exec(select(User).where(User.email==dependence.email)).first()
    if user_db is None:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")
    if user_db.is_active:
        raise HTTPException(status_code=409, detail="Hesap zaten aktif")
    code = redis.get(user_db.id)
    if code is None:
        code = generate_code()
        redis.set(user_db.id, code, ex=600)
        send_email(user_db.email, code)
        raise HTTPException(status_code=410, detail="Kodun süresi dolmuş")
    if int(code) != dependence.code:
        raise HTTPException(status_code=400, detail="Kod hatalı")
    try:
        user_db.is_active = True
        db.commit()
    except Exception:
        db.rollback()
        raise HTTPException(status_code=500, detail="Hesap aktifleştirilemedi")
    redis.delete(user_db.id)
    return UserResponse(user_id=user_db.id, username=user_db.username, email=user_db.email, is_active=user_db.is_active)
    
def user_login(user: UserLogin, db: Session):
    user_db = db.exec(select(User).where(User.email==user.email)).one_or_none()
    if user_db is None or not bcrypt.checkpw(user.password.encode("utf-8"), user_db.password_hash.encode("utf-8")):
        raise HTTPException(status_code=401, detail="E posta veya şifre yanlış")
    if not user_db.is_active:
        raise HTTPException(status_code=403, detail="Hesabınız aktif değil")
    access_token = create_access_token(user_db.id)
    refresh_token, expire = create_refresh_token(user_db.id)
    refresh = RefreshToken(user_id=user_db.id, token=refresh_token, expires_at=expire)
    db.add(refresh)
    db.commit()
    return LoginResponse(access_token=access_token, refresh_token=refresh_token)

def verify_token(refresh_token: str, user_id: int, db: Session):
    token_db = db.exec(select(RefreshToken).where(RefreshToken.token==refresh_token, RefreshToken.user_id==user_id, RefreshToken.revoked==False)).first()
    if token_db is None:
        raise HTTPException(status_code=401, detail="Oturum süreniz doldu")
    if token_db.expires_at <= datetime.utcnow():
        raise HTTPException(status_code=401, detail="Oturum süreniz doldu")
    access_token = create_access_token(token_db.user_id)
    return access_token

def user_logout(user: UserLogout, db: Session):
    refresh_token_db = db.exec(select(RefreshToken).where(RefreshToken.token==user.refresh_token, RefreshToken.revoked==False)).first()
    if refresh_token_db is None:
        raise HTTPException(status_code=401, detail="Geçersiz oturum")
    refresh_token_db.revoked=True
    db.commit()
    return refresh_token_db.user_id

def all_products(user_id: int, db: Session):
    purchase_exists = exists(select(OrderItem.id).join(Order, Order.id==OrderItem.order_id).where(Order.user_id==user_id, OrderItem.product_id==Product.id, Order.status.in_(["COMPLETE"])))
    product_db = db.exec(select(Product, Category.name, Favorite, Review, User.username, purchase_exists).join(Category).outerjoin(Favorite, (Favorite.product_id==Product.id) & (Favorite.user_id==user_id)).outerjoin(Review, Review.product_id==Product.id).outerjoin(User, User.id==Review.user_id)).all()
    products = {}
    for (product, category_name, favorite, review, username, has_purchased) in product_db:
        if product.id not in products:
            products[product.id] = ProductResponse(
                product_id=product.id,
                name=product.name,
                description=product.description,
                price=product.price,
                stock=product.stock,
                image_url=product.image_url,
                category=category_name,
                is_active=product.is_active,
                is_favorite=favorite is not None,
                has_purchased=has_purchased,
                reviews=[]
            )
        if review:
            products[product.id].reviews.append(
                ReviewResponse(
                    review_id=review.id,
                    user_id=review.user_id,
                    username=username,
                    comment=review.comment,
                    review_at=review.updated_at,
                    is_owner=review.user_id==user_id
                )
            )
    return list(products.values())

def add_cart(dependence: CartAdd, user_id: int, db: Session):
    product_db = db.get(Product, dependence.product_id)
    if product_db is None:
        raise HTTPException(status_code=404, detail="Ürün bulunamadı")
    if not product_db.is_active:
        raise HTTPException(status_code=422, detail="Ürün satışta değil")
    if dependence.quantity <= 0:
        raise HTTPException(status_code=400, detail="Geçersiz ürün adedi")
    if dependence.quantity > product_db.stock:
        raise HTTPException(status_code=422, detail="Stoktan fazla ürün")
    cart_db = db.exec(select(Cart).where(Cart.user_id==user_id, Cart.product_id==dependence.product_id)).first()
    if cart_db:
        if cart_db.quantity + dependence.quantity > product_db.stock:
            raise HTTPException(status_code=422, detail="Stoktan fazla ürün")
        cart_db.quantity += dependence.quantity
        db.commit()
        db.refresh(cart_db)
        return CartAdd(product_id=dependence.product_id, quantity=dependence.quantity)
    product_cart = Cart(user_id=user_id, product_id=dependence.product_id, quantity=dependence.quantity)
    db.add(product_cart)
    db.commit()
    db.refresh(product_cart)
    return CartAdd(product_id=product_cart.product_id, quantity=product_cart.quantity)

def cart_product_update(product_id: int, dependence: CartUpdate, user_id: int, db: Session):
    product_db = db.get(Product, product_id)
    if product_db is None:
        raise HTTPException(status_code=404, detail="Ürün bulunamadı")
    if not product_db.is_active:
        raise HTTPException(status_code=422, detail="Ürün satışta değil")
    if dependence.quantity <= 0:
        raise HTTPException(status_code=400, detail="Geçersiz ürün adedi")
    if dependence.quantity > product_db.stock:
        raise HTTPException(status_code=422, detail="Stoktan fazla ürün")
    cart_db = db.exec(select(Cart).where(Cart.user_id==user_id, Cart.product_id==product_id)).first()
    if cart_db is None:
        raise HTTPException(status_code=200, detail="Ürün sepette bulunamadı")
    cart_db.quantity = dependence.quantity
    db.commit()
    return cart_db

def cart_product_delete(product_id: int, user_id: int, db: Session): # select kullanmadan delete ile de yapılabilir
    cart_db = db.exec(select(Cart).where(Cart.user_id==user_id, Cart.product_id==product_id)).first()
    if cart_db is None:
        raise HTTPException(status_code=200, detail="Ürün sepette bulunamadı")
    db.delete(cart_db)
    db.commit()
    return product_id

def cart_all_product_delete(user_id: int, db: Session):
    cart_products = db.exec(delete(Cart).where(Cart.user_id==user_id))
    db.commit()
    return cart_products

def all_cart(user_id: int, db: Session):
    purchase_exists = exists(select(OrderItem.id).join(Order, Order.id==OrderItem.order_id).where(Order.user_id==user_id, OrderItem.product_id==Product.id, Order.status.in_(["COMPLETE"])))
    cart_db = db.exec(select(Cart, Product, Review, User.username, purchase_exists).join(Product, Cart.product_id==Product.id).outerjoin(Review, Product.id==Review.product_id).outerjoin(User, User.id==Review.user_id).where(Cart.user_id==user_id)).all()
    products = {}
    for cart, product, review, username, has_purchased in cart_db:
        if product.id not in products:
            products[product.id] = CartResponse(
                product_id=product.id,
                name=product.name,
                description=product.description,
                image_url=product.image_url,
                price=product.price,
                quantity=cart.quantity,
                total_price=product.price * cart.quantity,
                has_purchased=has_purchased,
                reviews=[]
            )
        if review is not None:
            products[product.id].reviews.append(
                ReviewResponse(
                    review_id=review.id,
                    user_id=review.user_id,
                    username=username,
                    comment=review.comment,
                    review_at=review.updated_at,
                    is_owner=review.user_id == user_id
                )
            )
    return list(products.values())

def add_favorite(dependence: FavoriteUpdate, user_id: int, db: Session):
    product_db = db.get(Product, dependence.product_id)
    if product_db is None:
        raise HTTPException(status_code=404, detail="Ürün bulunamadı")
    if not product_db.is_active:
        raise HTTPException(status_code=200, detail="Ürün satışta değil")
    favorite_db = db.exec(select(Favorite).where(Favorite.user_id==user_id, Favorite.product_id==dependence.product_id)).first()
    if favorite_db:
        raise HTTPException(status_code=200, detail="Ürün favorilerde ekli")
    product_favorite = Favorite(user_id=user_id, product_id=dependence.product_id)
    db.add(product_favorite)
    db.commit()
    db.refresh(product_favorite)
    return product_favorite.product_id

def favorite_product_delete(product_id: int, user_id: int, db: Session):
    favorite_db = db.exec(select(Favorite).where(Favorite.user_id==user_id, Favorite.product_id==product_id)).first()
    if favorite_db is None:
        raise HTTPException(status_code=204, detail="Ürün favorilerde bulunamadı")
    db.delete(favorite_db)
    db.commit()
    return favorite_db.product_id

def favorite_all_product_delete(user_id: int, db: Session):
    favorite_products = db.exec(delete(Favorite).where(Favorite.user_id==user_id))
    db.commit()
    return favorite_products

def all_favorite(user_id: int, db: Session):
    purchase_exists = exists(select(OrderItem.id).join(Order, Order.id==OrderItem.order_id).where(Order.user_id==user_id, OrderItem.product_id==Product.id, Order.status.in_(["COMPLETE"])))
    favorite_db = db.exec(select(Product, Category.name, Favorite, Review, User.username, purchase_exists).join(Category).join(Favorite, Product.id==Favorite.product_id).outerjoin(Review, Product.id==Review.product_id).outerjoin(User, User.id==Review.user_id).where(Favorite.user_id==user_id)).all()
    products = {}
    for product, category_name, favorite, review, username, has_purchased in favorite_db:
        if product.id not in products:
            products[product.id] = ProductResponse(
                product_id=product.id,
                name=product.name,
                description=product.description,
                price=product.price,
                stock=product.stock,
                image_url=product.image_url,
                category=category_name,
                is_active=product.is_active,
                is_favorite=favorite is not None,
                has_purchased=has_purchased,
                reviews=[]
            )
        if review is not None:
            products[product.id].reviews.append(
                ReviewResponse(
                    review_id=review.id,
                    user_id=review.user_id,
                    username=username,
                    comment=review.comment,
                    review_at=review.updated_at,
                    is_owner=review.user_id == user_id
                )
            )
    return list(products.values())

def add_review(dependence: ReviewUpdate, product_id: int, user_id: int, db: Session):
    product_db = db.get(Product, product_id)
    if product_db is None:
        raise HTTPException(status_code=404, detail="Ürün bulunamadı")
    order_db = db.exec(select(OrderItem.product_id).join(Order, Order.id==OrderItem.order_id).where(Order.user_id==user_id, Order.status=="COMPLETE", OrderItem.product_id==product_id)).first()
    if not order_db:
        raise HTTPException(status_code=403, detail="Ürünü satın almadığınız için yorum yapamazsınız")
    review_db = db.exec(select(Review).where(Review.user_id==user_id, Review.product_id==product_id)).first()
    if review_db:
        raise HTTPException(status_code=409, detail="Ürüne zaten yorum yaptınız")
    review = Review(user_id=user_id, product_id=product_id, comment=dependence.comment)
    db.add(review)
    db.commit()
    db.refresh(review)
    return review.product_id

def update_review(dependence: ReviewUpdate, product_id: int, review_id: int, user_id: int, db: Session):
    product_db = db.get(Product, product_id)
    if product_db is None:
        raise HTTPException(status_code=404, detail="Ürün bulunamadı")
    review_db = db.exec(select(Review).where(Review.id==review_id, Review.product_id==product_id)).first()
    if review_db is None:
        raise HTTPException(status_code=404, detail="Yorum bulunamadı")
    if review_db.user_id != user_id: # url için kontrol
        raise HTTPException(status_code=403, detail="Yorumu güncelleyemezsiniz")
    review_db.comment = dependence.comment
    review_db.updated_at = datetime.utcnow()
    db.add(review_db)
    db.commit()
    db.refresh(review_db)
    return review_db.product_id

def remove_review(product_id: int, review_id: int, user_id: int, db: Session):
    review_db = db.exec(select(Review).where(Review.id==review_id, Review.product_id==product_id, Review.user_id==user_id)).first()
    if review_db is None:
        raise HTTPException(status_code=403, detail="Yorum bulunamadı")
    db.delete(review_db)
    db.commit()
    return review_db.product_id

def get_order(user_id: int, db: Session):
    orders = db.exec(select(Order, OrderItem, Product).join(OrderItem, OrderItem.order_id==Order.id).join(Product, Product.id==OrderItem.product_id).where(Order.user_id==user_id)).all()
    result = {}
    for order, item, product in orders:
        if order.id not in result:
            result[order.id] = OrderResponse(
                id=order.id,
                total_price=order.total_price,
                status=order.status,
                created_at=order.created_at,
                items=[]
            )

        result[order.id].items.append(
            OrderItemResponse(
                product_id=product.id,
                product_name=product.name,
                image_url=product.image_url,
                quantity=item.quantity,
                unit_price=item.unit_price
            )
        )
    return list(result.values())[::-1]

def order_complete(user_id: int, db: Session):
    cart_items = db.exec(select(Cart).where(Cart.user_id==user_id)).all()
    if not cart_items:
        raise HTTPException(status_code=400, detail="Sepetiniz boş")
    total_price = 0
    order_items_data = []
    for cart_item in cart_items:
        product_db = db.get(Product, cart_item.product_id)
        if not product_db:
            raise HTTPException(status_code=404, detail="Sepetteki ürün bulunamadı")
        total_price += product_db.price * cart_item.quantity
        order_items_data.append({"product_id": product_db.id, "quantity": cart_item.quantity, "unit_price": product_db.price})
    order_db = Order(user_id=user_id, total_price=total_price)
    db.add(order_db)
    db.commit()
    db.refresh(order_db)
    for item in order_items_data:
        order_item = OrderItem(order_id=order_db.id, product_id=item["product_id"], quantity=item["quantity"], unit_price=item["unit_price"])
        db.add(order_item)
    for cart_item in cart_items:
        db.delete(cart_item)
    db.commit()
    return order_db.id