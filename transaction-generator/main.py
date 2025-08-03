import random
import time
import json
import redis
from enum import Enum
from faker import Faker

class TransactionCategory(Enum):
    SBP_TRANSFER = "Fast Payment System transfer"
    CASH_WITHDRAWAL = "Cash withdrawal"
    GOODS_PAYMENT = "Goods and services payment"
    SUPERMARKETS = "Supermarkets"
    MOBILE_SERVICES = "Mobile services"
    DIGITAL_GOODS = "Digital goods"
    TRANSPORT = "Transport"
    RAILWAY_TICKETS = "Railway tickets"
    MEDICINE = "Healthcare and medicine"
    HOME_IMPROVEMENT = "Home improvement"
    SPORTS_AND_OUTDOORS = "Sports and outdoor equipment"
    CLOTHING_AND_SHOES = "Clothing and footwear"
    RESTAURANTS_AND_CAFES = "Restaurants and cafes"
    SUBSCRIPTIONS = "Online subscriptions"
    ENTERTAINMENT = "Entertainment"
    EDUCATION = "Education"
    AUTO_AND_GAS = "Car services and fuel"
    UTILITIES = "Utility payments"
    TAXES = "Taxes and fees"
    INSURANCE = "Insurance"
    CHARITY = "Charity"
    TRAVEL = "Travel and tourism"
    ELECTRONICS = "Electronics and appliances"
    BEAUTY_AND_HEALTH = "Beauty and personal care"
    PETS = "Pet supplies"
    CHILDREN_PRODUCTS = "Children's products"
    DELIVERY = "Delivery services"
    OTHER = "Other"


faker = Faker()
faker.add_provider(Faker().location_on_land())

ttl = 300 #sec

def generate_offer():
    timestamp = int(time.time())
    return {
        "id": f"pmt-{timestamp}",
        "amount": {
            "value": round(random.uniform(100, 999_999), 2),
            "currency": "RUB"
        },
        "category": random.choice(list(TransactionCategory)).name,
        "location": {
            "vendor": faker.company(),
            "latitude": float(faker.latitude()),
            "longitude": float(faker.longitude())
        },
        "suggestedAt": timestamp
    }

def store_offer(offer):
    r = redis.Redis(host='redis', port=6379, db=0)
    key = f"offer:{offer['id']}"
    r.setex(key, ttl, json.dumps(offer))
    print(f"[+] Stored new offer with TTL {ttl}sec: {key}")

if __name__ == "__main__":
    while True:
        offer = generate_offer()
        store_offer(offer)
        time.sleep(ttl)