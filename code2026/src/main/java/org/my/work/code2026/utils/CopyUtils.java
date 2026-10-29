package org.my.work.code2026.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;


public class CopyUtils {
    private static String packagePath = System.getProperty("user.dir") + "/code2026/src/main/java/org/my/work/code2026";
    private static String resourcesPath = System.getProperty("user.dir") + "/code2026/src/main/resources";
    private static String vuePath = System.getProperty("user.dir") + "/vue/src/views/manager";

    private static String controllerSuffix = "Controller.java";
    private static String serviceSuffix = "Service.java";
    private static String mapperSuffix = "Mapper.java";
    private static String mapperXmlSuffix = "Mapper.xml";
    private static String vueSuffix = ".vue";

    // 拷贝代码小工具
    public static void main(String[] args) {
        // 复制源文件前缀
        String sourceName = "Category";
        // 复制后的目标文件前缀
        String targetName = "Comment";

        // 复制一整套的 controller、Service、Mapper、Mapper.xml
        copyMapperXml(sourceName, targetName);
        copyMapper(sourceName, targetName);
        copyService(sourceName, targetName);
        copyController(sourceName, targetName);

//         复制Vue文件
        copyVue(sourceName, targetName);
    }

    // 复制Mapper.xml
    public static void copyMapperXml(String sourceName, String targetName) {
        // mapper的路径前缀
        String mapperXmlPathPrefix = resourcesPath + "/mapper/";
        // 读取文件的内容
        String content = FileUtil.readUtf8String(mapperXmlPathPrefix + sourceName + mapperXmlSuffix);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Mapepr.xml
        File targetFile = FileUtil.touch(mapperXmlPathPrefix + targetName + mapperXmlSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + mapperXmlSuffix + "复制成功！");
    }


    // 复制Mapper
    public static void copyMapper(String sourceName, String targetName) {
        // mapper的路径前缀
        String mapperPathPrefix = packagePath + "/mapper/";
        // 读取文件的内容
        String content = FileUtil.readUtf8String(mapperPathPrefix + sourceName + mapperSuffix);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Mapepr
        File targetFile = FileUtil.touch(mapperPathPrefix + targetName + mapperSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + mapperSuffix + "复制成功！");
    }

    // 复制Service
    public static void copyService(String sourceName, String targetName) {
        // service的路径前缀
        String servicePathPrefix = packagePath + "/service/";
        // 读取文件的内容
        String content = FileUtil.readUtf8String(servicePathPrefix + sourceName + serviceSuffix);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Service
        File targetFile = FileUtil.touch(servicePathPrefix + targetName + serviceSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + serviceSuffix + "复制成功！");
    }

    // 复制Controller
    public static void copyController(String sourceName, String targetName) {
        // controller的路径前缀
        String controllerPathPrefix = packagePath + "/controller/";
        // 读取文件的内容
        String content = FileUtil.readUtf8String(controllerPathPrefix + sourceName + controllerSuffix);
        // 替换文件的内容
        String result1 = StrUtil.replace(content, sourceName, targetName);// 替换大写的实体类名称
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName), StrUtil.lowerFirst(targetName));// 替换小写的实体类对象名称

        // 创建目标的Controller
        File targetFile = FileUtil.touch(controllerPathPrefix + targetName + controllerSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + controllerSuffix + "复制成功！");
    }

    // 复制Vue
    public static void copyVue(String sourceName, String targetName) {
        // 读取文件的内容
        String content = FileUtil.readUtf8String(vuePath + "/" + sourceName + vueSuffix);
        // 替换文件的内容
        String result = StrUtil.replace(content, "/" + StrUtil.lowerFirst(sourceName),  "/" + StrUtil.lowerFirst(targetName));// 替换小写的后端接口路径

        // 创建目标的Vue
        File targetFile = FileUtil.touch(vuePath + "/" + targetName + vueSuffix);
        FileUtil.writeUtf8String(result, targetFile);
        System.out.println(targetName + vueSuffix + "复制成功！");
    }

}
