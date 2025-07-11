package com.wallet.cardservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCategory {
    SBP_TRANSFER("Fast Payment System transfer"),
    CASH_WITHDRAWAL("Cash withdrawal"),
    INCOMING_TRANSFER("Incoming transfer"),
    GOODS_PAYMENT("Goods and services payment"),
    SUPERMARKETS("Supermarkets"),
    MOBILE_SERVICES("Mobile services"),
    DIGITAL_GOODS("Digital goods"),
    TRANSPORT("Transport"),
    RAILWAY_TICKETS("Railway tickets"),
    MEDICINE("Healthcare and medicine"),
    HOME_IMPROVEMENT("Home improvement"),
    SPORTS_AND_OUTDOORS("Sports and outdoor equipment"),
    CLOTHING_AND_SHOES("Clothing and footwear"),
    RESTAURANTS_AND_CAFES("Restaurants and cafes"),
    SUBSCRIPTIONS("Online subscriptions"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),
    AUTO_AND_GAS("Car services and fuel"),
    UTILITIES("Utility payments"),
    TAXES("Taxes and fees"),
    INSURANCE("Insurance"),
    CHARITY("Charity"),
    TRAVEL("Travel and tourism"),
    ELECTRONICS("Electronics and appliances"),
    BEAUTY_AND_HEALTH("Beauty and personal care"),
    PETS("Pet supplies"),
    CHILDREN_PRODUCTS("Children's products"),
    DELIVERY("Delivery services"),
    OTHER("Other");

    private final String description;
}
