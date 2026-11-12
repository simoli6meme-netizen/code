package org.my.work.code2026.common;

import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码处理统一入口：加密、校验、识别存量明文。
 *
 * 为什么不用各 Service 直接调 encoder.matches：
 * 1. 库里还有历史明文数据，需要过渡期兼容（见 matches）；
 * 2. 判断"这条要不要升级"的逻辑集中在一处，避免三份实现走偏。
 *
 * 过渡期策略（渐进式哈希升级）：存量明文首次登录校验通过后，就地改写成 BCrypt 密文。
 * 这样不需要停机刷库，也不存在"迁移没跑完所有人都登不上"的风险。
 */
@Component
public class PasswordHelper {

    @Resource
    private PasswordEncoder passwordEncoder;

    /** 加密；null 原样返回，避免把 null 变成 null 的哈希串 */
    public String encode(String raw) {
        return raw == null ? null : passwordEncoder.encode(raw);
    }

    /** 是否已经是 BCrypt 密文（$2a$ / $2b$ / $2y$ 开头，总长 60） */
    public boolean isEncoded(String stored) {
        if (stored == null) {
            return false;
        }
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }

    /**
     * 校验密码。
     * 密文走 BCrypt；存量明文走等值比较——只用于过渡，迁移完成后这条分支永远不会命中。
     */
    public boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (isEncoded(stored)) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }

    /** 这条记录是否需要在登录成功后升级成密文 */
    public boolean needsUpgrade(String stored) {
        return stored != null && !isEncoded(stored);
    }
}
