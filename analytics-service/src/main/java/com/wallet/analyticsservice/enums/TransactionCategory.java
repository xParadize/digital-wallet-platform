package com.wallet.analyticsservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCategory {
    SBP_TRANSFER("Fast Payment System transfer", false),
    CASH_WITHDRAWAL("Cash withdrawal", false),
    INCOMING_TRANSFER("Incoming transfer", true),
    GOODS_PAYMENT("Goods and services payment", false),
    SUPERMARKETS("Supermarkets", false),
    MOBILE_SERVICES("Mobile services", false),
    DIGITAL_GOODS("Digital goods", false),
    TRANSPORT("Transport", false),
    RAILWAY_TICKETS("Railway tickets", false),
    MEDICINE("Healthcare and medicine", false),
    HOME_IMPROVEMENT("Home improvement", false),
    SPORTS_AND_OUTDOORS("Sports and outdoor equipment", false),
    CLOTHING_AND_SHOES("Clothing and footwear", false),
    RESTAURANTS_AND_CAFES("Restaurants and cafes", false),
    SUBSCRIPTIONS("Online subscriptions", false),
    ENTERTAINMENT("Entertainment", false),
    EDUCATION("Education", false),
    AUTO_AND_GAS("Car services and fuel", false),
    UTILITIES("Utility payments", false),
    TAXES("Taxes and fees", false),
    INSURANCE("Insurance", false),
    CHARITY("Charity", false),
    TRAVEL("Travel and tourism", false),
    ELECTRONICS("Electronics and appliances", false),
    BEAUTY_AND_HEALTH("Beauty and personal care", false),
    PETS("Pet supplies", false),
    CHILDREN_PRODUCTS("Children's products", false),
    DELIVERY("Delivery services", false),
    REWARDS("Rewards", true),
    REFUND("Refund", true),
    OTHER("Other", false);

    private final String description;
    private final boolean isPositive;
}