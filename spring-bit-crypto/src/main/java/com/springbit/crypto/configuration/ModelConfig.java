package com.springbit.crypto.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springbit.crypto.model.mappers.EntMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.mapstruct.factory.Mappers;

@Configuration
public class ModelConfig {
    @Bean
    public EntMapper modelMapper() {
        return Mappers.getMapper( EntMapper.class );
    }
}
