package com.hongshu.file.util;

import com.qiniu.storage.Region;

import java.util.Locale;

/**
 * 将系统 OSS 配置中的 region 字符串解析为七牛 Java SDK 的 {@link Region}。
 * 支持七牛控制台常见的 z0/z1/z2 编码及中文区域名；空值时与历史行为一致，默认华北（z1）。
 */
public final class QiniuRegionResolver {

    private QiniuRegionResolver() {
    }

    public static Region resolve(String region) {
        if (region == null || region.isBlank()) {
            return Region.huabei();
        }
        String raw = region.trim();
        String r = raw.toLowerCase(Locale.ROOT);
        return switch (r) {
            case "auto", "autoregion" -> Region.autoRegion();
            case "z0", "huadong", "region0", "cn-east", "east-china" -> Region.huadong();
            case "z1", "huabei", "region1", "cn-north", "north-china" -> Region.huabei();
            case "z2", "huanan", "region2", "cn-south", "south-china" -> Region.huanan();
            case "na0", "beimei", "north-america" -> Region.beimei();
            case "as0", "xinjiapo", "singapore", "southeast-asia" -> Region.xinjiapo();
            case "fog-cn-east-1", "fog_cneast1", "fog" -> Region.regionFogCnEast1();
            default -> resolveChineseOrFallback(raw);
        };
    }

    private static Region resolveChineseOrFallback(String originalTrimmed) {
        if (originalTrimmed.contains("华东")) {
            return Region.huadong();
        }
        if (originalTrimmed.contains("华北")) {
            return Region.huabei();
        }
        if (originalTrimmed.contains("华南")) {
            return Region.huanan();
        }
        if (originalTrimmed.contains("北美")) {
            return Region.beimei();
        }
        if (originalTrimmed.contains("新加坡") || originalTrimmed.contains("东南亚")) {
            return Region.xinjiapo();
        }
        if (originalTrimmed.contains("自动")) {
            return Region.autoRegion();
        }
        return Region.huabei();
    }
}
