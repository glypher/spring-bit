package com.springbit.crypto.model.entity;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;

@Entity
@Table(name = "blocks")
@Data
public class EBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(value="symbol")
    private String symbol;

    @Column(value="block_hash")
    private String blockHash;

    @Column(value="block_size")
    private Long blockSize;
}
