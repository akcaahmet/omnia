import random
import smtplib
import ssl
from email.message import EmailMessage
import os
from dotenv import load_dotenv

load_dotenv()
port = 465
smtp_server = "smtp.gmail.com"
sender_email = os.getenv("SENDER_EMAIL")
password = os.getenv("APP_PASSWORD")

def generate_code():
    return str(random.randint(100_000, 999_999))

def send_email(email, code):
    message = EmailMessage()
    message["SUBJECT"] = "Hesap Aktivasyonu"
    message["FROM"] = sender_email
    message["TO"] = email
    message.set_content(f"Aktivasyon kodunuz: {code}")
    context = ssl.create_default_context()
    with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
        server.login(sender_email, password)
        server.send_message(message)
        return code