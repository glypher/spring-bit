package com.springbit.crypto.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dodge")
@Data
public class Dodge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="block_hash")
    private String blockHash;

    @Column(name="block_size")
    private Long blockSize;
}
