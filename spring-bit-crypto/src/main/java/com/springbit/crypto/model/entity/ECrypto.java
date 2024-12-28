package com.springbit.crypto.model.entity;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "crypto_quote")
@Data
public class ECrypto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(value="name")
    private String name;

    @Column(value="symbol")
    private String symbol;


    @Column(value="date")
    private LocalDateTime quoteDate;

    @Column(value="usd")
    private Float quotePrice;
}
