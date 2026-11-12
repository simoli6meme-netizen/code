package org.my.work.code2026.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 商品读缓存（Cache-Aside 模式）。
 * <p>
 * 三兄弟防护都在这里：
 * 1. 穿透：数据库查不到时缓存一个短 TTL 的空值标记，下次直接命中；
 * 2. 击穿：热点 key 失效时只允许一个线程回源重建，其它线程短暂等待后读缓存（互斥锁）；
 * 3. 雪崩：TTL = 基础时长 + 随机抖动，避免同一时刻大批量 key 一起过期。
 * <p>
 * 一致性用「先改库、再删缓存」：所有会改 goods 表的写操作都要调 evictAll()。
 * Redis 挂了要能降级——缓存异常不影响主流程，直接回源查 MySQL。
 **/
@Component
public class GoodsCache {

    private static final Logger log = LoggerFactory.getLogger(GoodsCache.class);

    public static final String PREFIX = "goods:cache:";
    private static final String LOCK_PREFIX = "goods:lock:";
    private static final String EMPTY = "__EMPTY__";
    private static final Duration BASE_TTL = Duration.ofSeconds(120);
    private static final Duration EMPTY_TTL = Duration.ofSeconds(60);
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 读缓存；未命中则回源并回填。
     *
     * @param cacheKey 业务 key，例如 detail:14 / list:ab12cd...
     * @param type     反序列化类型，用 TypeReference 保留 List 的泛型
     * @param loader   回源查数据库
     */
    public <T> T query(String cacheKey, TypeReference<T> type, Supplier<T> loader) {
        String key = PREFIX + cacheKey;
        String cached = read(key);
        if (EMPTY.equals(cached)) {
            return null;                       // 命中空值标记，不再打数据库
        }
        T hit = parse(cached, type);
        if (hit != null) {
            log.debug("商品缓存命中 key={}", cacheKey);
            return hit;
        }

        String lockKey = LOCK_PREFIX + cacheKey;
        Boolean locked = tryLock(lockKey);
        if (!Boolean.TRUE.equals(locked)) {
            // 没抢到重建锁：等持锁线程写完，最多等 500ms；超时直接回源（不至于卡死请求）
            for (int i = 0; i < 10; i++) {
                sleep(50);
                String again = read(key);
                if (EMPTY.equals(again)) {
                    return null;
                }
                T retry = parse(again, type);
                if (retry != null) {
                    return retry;
                }
            }
            return loader.get();
        }

        try {
            T data = loader.get();             // 数据库异常照常抛出，交给全局异常处理
            try {
                if (isEmpty(data)) {
                    redis.opsForValue().set(key, EMPTY, EMPTY_TTL);
                } else {
                    redis.opsForValue().set(key, objectMapper.writeValueAsString(data), randomTtl());
                }
                log.debug("商品缓存回源重建 key={}", cacheKey);
            } catch (Exception e) {
                log.warn("写商品缓存失败，本次直接返回数据库结果 key={}", cacheKey, e);
            }
            return data;
        } finally {
            redis.delete(lockKey);
        }
    }

    /**
     * 写操作统一调用：先改库，事务提交后再删缓存。
     * 如果在事务提交前就删，另一个读请求可能把「未提交的旧值」查出来重新塞进缓存，造成脏读。
     * 无事务时（比如简单新增）直接删。
     */
    public void evictAllAfterCommit() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    GoodsCache.this.evictAll();
                }
            });
        } else {
            evictAll();
        }
    }

    /** 立即删掉所有商品缓存（删而不是改，避免并发写产生脏值） */
    public int evictAll() {
        List<String> keys = scanKeys(ScanOptions.scanOptions().match(PREFIX + "*").count(200).build());
        if (keys.isEmpty()) {
            return 0;
        }
        redis.delete(keys);
        log.debug("商品缓存已清理 {} 个 key", keys.size());
        return keys.size();
    }

    /** 调试用：看当前缓存里有哪些 key */
    public List<String> keys() {
        return scanKeys(ScanOptions.scanOptions().match(PREFIX + "*").count(200).build());
    }

    /** 查询条件做 MD5，避免中文/特殊字符直接进 Redis key */
    public static String digest(String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ==================== 内部方法 ====================

    private String read(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("读商品缓存失败，降级回源 key={}", key, e);
            return null;
        }
    }

    private Boolean tryLock(String lockKey) {
        try {
            return redis.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
        } catch (Exception e) {
            log.warn("获取重建锁失败，降级回源 key={}", lockKey, e);
            return null;
        }
    }

    private <T> T parse(String json, TypeReference<T> type) {
        if (json == null || EMPTY.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("商品缓存反序列化失败，按未命中处理", e);
            return null;
        }
    }

    private boolean isEmpty(Object data) {
        return data == null || (data instanceof List<?> list && list.isEmpty());
    }

    /** 120s + 0~60s 抖动 */
    private Duration randomTtl() {
        return BASE_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(0, 61));
    }

    /** 用 SCAN 而不是 KEYS，避免大 key 空间下阻塞 Redis 主线程 */
    private List<String> scanKeys(ScanOptions options) {
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("扫描商品缓存失败", e);
        }
        return keys;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
