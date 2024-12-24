package com.springbit.crypto.controller;

import com.springbit.crypto.model.dto.Block;
import com.springbit.crypto.model.DodgeRepository;
import com.springbit.crypto.model.EntMapper;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/crypto/dodge")
@RequiredArgsConstructor
public class DodgeController {
    private final EntMapper modelMapper;

    private final DodgeRepository repository;

    @Observed(name = "crypto.blocks", contextualName = "getting-crypto-blocks")
    @GetMapping("/blocks")
    public Flux<Block> getBlocks() {
        return Flux.fromStream(repository.findAll().stream().map(modelMapper::map));
    }
}



