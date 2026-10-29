package org.my.work.code2026.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaCache {
    private record Entry(String code, long expireAt) {}

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    public void put(String key, String code, long ttlSeconds) {
        cache.put(key, new Entry(code, System.currentTimeMillis() + ttlSeconds * 1000));
    }

    /** 取出即删，过期返回 null */
    public String getAndRemove(String key) {
        Entry e = cache.remove(key);
        if (e == null || e.expireAt() < System.currentTimeMillis()) {
            return null;
        }
        return e.code();
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(en -> en.getValue().expireAt() < now);
    }
}
