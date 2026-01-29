package com.example.user_service.utils.bloom;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.BitSet;

public abstract class BaseBloomFilter {

    protected final int size;
    protected final int hashCount;
    protected final BitSet bitset;

    protected BaseBloomFilter(int size, int hashCount) {
        this.size = size;
        this.hashCount = hashCount;
        this.bitset = new BitSet(size);
    }

    protected abstract String normalize(String value);

    public void add(String value) {
        String normalized = normalize(value);
        int[] hashes = getHashes(normalized);
        for (int hash : hashes) {
            bitset.set(Math.floorMod(hash, size));
        }
    }

    public boolean mightContain(String value) {
        String normalized = normalize(value);
        int[] hashes = getHashes(normalized);
        for (int hash : hashes) {
            if (!bitset.get(Math.floorMod(hash, size))) {
                return false;
            }
        }
        return true;
    }

    protected int[] getHashes(String value) {
        int[] hashes = new int[hashCount];
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);

            for (int i = 0; i < hashCount; i++) {
                int hash = 0;
                for (int j = i * 4; j < (i + 1) * 4; j++) {
                    hash = (hash << 8) | (digest[j] & 0xFF);
                }
                hashes[i] = hash;
            }
        } catch (Exception e) {
            throw new RuntimeException("Bloom hash generation failed", e);
        }

        return hashes;
    }
}
