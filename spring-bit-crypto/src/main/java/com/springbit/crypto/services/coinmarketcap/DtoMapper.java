package com.springbit.crypto.services.coinmarketcap;

import com.springbit.crypto.model.entity.ECrypto;
import com.springbit.crypto.services.CryptoType;
import com.springbit.crypto.model.dto.Crypto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface DtoMapper {
    Crypto mapD(JsonCrypto jc);

    ECrypto mapE(JsonCrypto js);

    default String map(CryptoType type) {
        return switch (type) {
            case BITCOIN -> "BTC";
            case ETHEREUM -> "ETH";
            case DOGECOIN -> "DOGE";
            case UNKNOWN -> "";
        };
    }
}
