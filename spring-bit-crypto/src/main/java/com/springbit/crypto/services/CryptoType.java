package com.springbit.crypto.services;

import lombok.Getter;


@Getter
public enum CryptoType {
    BITCOIN("BTC"),
    ETHEREUM("ETH"),
    DOGECOIN("DOGE"),
    UNKNOWN("UNKNOWN");

    private final String symbol;

    CryptoType(String symbol) {
        this.symbol = symbol;
    }

    public static CryptoType map(String type) {
        for (CryptoType cryptoType : CryptoType.values()) {
            if (cryptoType.getSymbol().equalsIgnoreCase(type)) {
                return cryptoType;
            }
        }
        return UNKNOWN;
    }
}
