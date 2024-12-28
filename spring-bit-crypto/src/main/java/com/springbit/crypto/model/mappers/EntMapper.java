package com.springbit.crypto.model.mappers;

import com.springbit.crypto.model.entity.ECrypto;
import com.springbit.crypto.model.entity.EBlock;
import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.dto.Crypto;
import com.springbit.crypto.services.bitquery.JsonBlock;
import org.mapstruct.Mapper;


@Mapper
public interface EntMapper {
    Block mapBlock(EBlock d);

    EBlock mapBlockE(Block dn);

    Crypto mapCrypto(ECrypto ent);

    ECrypto mapCryptoE(Crypto sto);
}