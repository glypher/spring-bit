package com.springbit.crypto.model;

import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.web.crypto.DodgeNetwork;
import org.mapstruct.Mapper;


@Mapper
public interface EntMapper {
    Dodge map(DodgeNetwork dn);

    Block map(Dodge d);
}