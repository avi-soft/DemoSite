package com.community.api.component;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklist {

    private final Set<String> blacklistedTokens = new HashSet<>();

    public void blacklistToken(String tokenId) {
        blacklistedTokens.add(tokenId);
    }

    public boolean isTokenBlacklisted(String tokenId) {
        return blacklistedTokens.contains(tokenId);
    }
}
