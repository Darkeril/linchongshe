package com.hongshu.common.core.utils.ip;

import com.hongshu.common.core.utils.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * 获取地址类
 *
 *
 */
public class AddressUtils {
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // 未知地址
    public static final String UNKNOWN = "未知";

    // ip2region searcher，使用懒加载单例模式
    private static Searcher searcher = null;
    private static final Object lock = new Object();

    /**
     * 初始化ip2region searcher
     */
    private static void initSearcher() {
        if (searcher != null) {
            return;
        }
        synchronized (lock) {
            if (searcher != null) {
                return;
            }
            try {
                // 1. 读取 classpath 下的 ip2region.xdb
                ClassPathResource resource = new ClassPathResource("ip2region.xdb");
                // 2. 拷贝到临时文件
                File tempFile = File.createTempFile("ip2region", ".xdb");
                try (InputStream in = resource.getInputStream();
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
                // 3. 用临时文件路径初始化
                searcher = Searcher.newWithFileOnly(tempFile.getAbsolutePath());
                log.info("ip2region searcher 初始化成功");
            } catch (Exception e) {
                log.error("ip2region searcher 初始化失败，将使用外部API作为备选方案", e);
            }
        }
    }

    public static String getRealAddressByIP(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        if (IpUtils.internalIp(ip)) {
            return "内网";
        }

        // 优先使用ip2region本地数据库
        try {
            initSearcher();
            if (searcher != null) {
                String ipInfo = searcher.search(ip);
                if (StringUtils.isNotEmpty(ipInfo)) {
                    // ipInfo 格式: "中国|0|浙江省|杭州市|电信"
                    String[] parts = ipInfo.split("\\|");
                    if (parts.length >= 4) {
                        String province = parts[2].trim();
                        String city = parts[3].trim();
                        
                        // 处理无效值
                        if ("0".equals(province)) {
                            province = null;
                        }
                        if ("0".equals(city)) {
                            city = null;
                        }
                        
                        // 拼接地址
                        StringBuilder sb = new StringBuilder();
                        if (province != null && !province.isEmpty()) {
                            sb.append(province);
                        }
                        if (city != null && !city.isEmpty()) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(city);
                        }
                        
                        if (sb.length() > 0) {
                            return sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("使用ip2region解析IP地址失败 {}: {}，尝试使用外部API", ip, e.getMessage());
        }

        // 如果ip2region失败，尝试使用外部API作为备选方案
        return getRealAddressByIPFromAPI(ip);
    }

    /**
     * 使用外部API获取IP地址（备选方案）
     */
    private static String getRealAddressByIPFromAPI(String ip) {
        try {
            String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
            String rspStr = com.hongshu.common.core.utils.HttpUtils.sendGet(
                IP_URL, "ip=" + ip + "&json=true", com.hongshu.common.core.constant.Constants.GBK);
            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }
            com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(rspStr);
            String region = obj.getString("pro");
            String city = obj.getString("city");
            String addr = obj.getString("addr");

            // 拼接详细地址，优先显示 region/city，如果都没有就用 addr
            StringBuilder sb = new StringBuilder();
            if (region != null) sb.append(region);
            if (city != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(city);
            }
            if (sb.length() == 0 && addr != null) {
                sb.append(addr);
            }
            return sb.length() > 0 ? sb.toString() : UNKNOWN;
        } catch (Exception e) {
            log.error("获取地理位置异常 {}: {}", ip, e.getMessage());
            return UNKNOWN;
        }
    }
}
