package com.springbit.crypto.web.crypto;

public record DodgeNetwork(String blockHash, Long blockSize) implements CryptoNetwork {
    public static String getGraphQlQuery() {
        return """
                query DodgeChain {
                bitcoin(network: dogecoin) {
                    blocks(options: {limit: 10, desc: "timestamp.iso8601"}) {
                        blockHash
                                blockSize
                        blockSizeBigInt
                                blockStrippedSize
                        blockVersion
                                blockWeight
                        chainwork
                                difficulty
                        height
                        timestamp {
                            iso8601
                        }
                    }
                } }
                """;
    }
}
