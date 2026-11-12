package org.my.work.code2026.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import jakarta.servlet.http.HttpServletResponse;
import org.my.work.code2026.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件相关操作接口。
 *
 * 安全约束（避免路径穿越与任意文件读写）：
 * 1. 上传一律用 UUID 重命名，不信任浏览器提交的原始文件名；
 * 2. 只允许图片后缀，挡住 jsp/html/svg/exe 之类可被解析或执行的文件；
 * 3. 下载参数必须匹配纯文件名白名单，落盘/读取前再做 normalize + startsWith 校验；
 * 4. 限制单文件大小，异常走日志而不是 System.out 吞掉。
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    /** 下载路径参数白名单：不含斜杠与反斜杠，直接挡住 ../ 和绝对路径 */
    private static final String SAFE_NAME = "[A-Za-z0-9._-]+";

    /** 可选：显式指定上传目录；不配则取后端进程工作目录下的 files/ */
    @Value("${file.upload-dir:}")
    private String uploadDir;

    /** 上传目录的绝对路径，统一在这里规范化 */
    private Path root() {
        String dir = (uploadDir == null || uploadDir.isBlank())
                ? System.getProperty("user.dir") + "/files/"
                : uploadDir;
        return Paths.get(dir).toAbsolutePath().normalize();
    }

    /** 三个上传入口共用的落盘逻辑，返回可公开访问的文件名 */
    private String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件不能超过 10MB");
        }
        String original = file.getOriginalFilename();
        if (original == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 统一分隔符后只取最后一段，去掉浏览器可能带上的 C:\fakepath\ 或 ../../ 前缀
        String clean = original.replace('\\', '/');
        clean = clean.substring(clean.lastIndexOf('/') + 1);
        int dot = clean.lastIndexOf('.');
        String ext = dot < 0 ? "" : clean.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("只允许上传 jpg/jpeg/png/gif/webp/bmp 图片");
        }

        String saved = System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path root = root();
        Files.createDirectories(root);
        Path target = root.resolve(saved).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        file.transferTo(target.toFile());
        return saved;
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            return Result.success("/files/download/" + store(file));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        if (!fileName.matches(SAFE_NAME)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Path root = root();
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            String contentType = Files.probeContentType(target);
            if (contentType != null) {
                response.setContentType(contentType);
            }
            Files.copy(target, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("文件下载失败 fileName={}", fileName, e);
        }
    }

    /**
     * wang-editor编辑器文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        Map<String, Object> resMap = new HashMap<>();
        try {
            String url = "/files/download/" + store(file);
            // wangEditor 要求的成功响应结构
            resMap.put("errno", 0);
            resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", url)));
        } catch (IllegalArgumentException e) {
            resMap.put("errno", 1);
            resMap.put("message", e.getMessage());
        } catch (IOException e) {
            log.error("wangEditor 文件上传失败", e);
            resMap.put("errno", 1);
            resMap.put("message", "上传失败，请稍后重试");
        }
        return resMap;
    }

}
