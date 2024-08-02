package com.community.api.component;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final ConcurrentHashMap<String, String> blacklist = new ConcurrentHashMap<>();

    public void addTokenToBlacklist(String token, String phoneNumber) {
        blacklist.put(token, phoneNumber);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

}