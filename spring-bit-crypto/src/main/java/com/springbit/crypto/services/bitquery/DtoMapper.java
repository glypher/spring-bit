package com.springbit.crypto.services.bitquery;

import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.entity.EBlock;
import org.mapstruct.Mapper;

@Mapper
public interface DtoMapper {
    Block mapD(JsonBlock jb);

    EBlock mapE(JsonBlock jb);
}

