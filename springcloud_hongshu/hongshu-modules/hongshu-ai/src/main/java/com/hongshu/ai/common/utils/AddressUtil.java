package com.hongshu.ai.common.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.hongshu.common.core.utils.ip.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

/**
 * 获取地址工具类
 *
 * @author: myj
 * @date: 2023/10/20
 * @version: 1.0.0
 */
@Slf4j
public class AddressUtil {

    static String dbPath;
    static DbConfig config;
    static DbSearcher searcher;

    static {
        dbPath = createFtlFileByFtlArray() + "ip2region.db";
        try {
            config = new DbConfig();
        } catch (DbMakerConfigException e) {
            e.printStackTrace();
        }
        try {
            searcher = new DbSearcher(config, dbPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * IP地址查询
     */
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";

    /**
     * 未知地址
     */
    public static final String UNKNOWN = "XX XX";

    /**
     * 根据ip获取城市地址
     *
     * @param ip
     * @return
     */
    public static String getRealAddressByIP(String ip) {
        String address = UNKNOWN;
        // 内网不查询
        if (IPUtil.internalIp(ip)) {
            return "内网IP";
        }
        try {
            String rspStr = HttpUtil.get(String.format(IP_URL, ip));
            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }
            JSONObject obj = JSONObject.parseObject(rspStr);
            String region = obj.getString("pro");
            String city = obj.getString("city");
            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip);
        }
        return address;
    }

    /**
     * 获取IP地址来源
     *
     * @param content        请求的参数 格式为：name=xxx&pwd=xxx
     * @param encodingString 服务器端请求编码。如GBK,UTF-8等
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getAddresses(String ip, String encodingString) {

        if (!Util.isIpAddress(ip)) {
            log.info("IP地址为空");
            return null;
        }

        // 淘宝IP宕机，目前使用Ip2region：https://github.com/lionsoul2014/ip2region
        String cityInfo = getCityInfo(ip);
        log.info("返回的IP信息：{}", cityInfo);
        return cityInfo;

        // TODO 淘宝接口目前已经宕机，因此暂时注释下面代码
//        try {
//            // 这里调用pconline的接口
//            String urlStr = "http://ip.taobao.com/service/getIpInfo.php";
//
//            // 从http://whois.pconline.com.cn取得IP所在的省市区信息
//            String returnStr = getResult(urlStr, content, encodingString);
//
//            if (returnStr != null) {
//                // 处理返回的省市区信息
//                log.info("调用IP解析接口返回的内容:" + returnStr);
//                String[] temp = returnStr.split(",");
//                //无效IP，局域网测试
//                if (temp.length < 3) {
//                    return "0";
//                }
//
//                // 国家
//                String country = "";
//                // 区域
//                String area = "";
//                // 省
//                String region = "";
//                // 市
//                String city = "";
//                // 县
//                String county = "";
//                // 运营商
//                String isp = "";
//
//                Map<String, Object> map = JsonUtils.jsonToMap(returnStr);
//
//                if (map.get("code") != null) {
//                    Map<String, String> data = (Map<String, String>) map.get("data");
//                    country = data.get("country");
//                    area = data.get("area");
//                    region = data.get("region");
//                    city = data.get("city");
//                    county = data.get("area");
//                    isp = data.get("isp");
//                }
//
//
//                log.info("获取IP地址对应的地址" + country + "=" + area + "=" + region + "=" + city + "=" + county + "=" + isp);
//                StringBuffer result = new StringBuffer();
//                result.append(country);
//                result.append("|");
//                result.append(region);
//                result.append("|");
//                result.append(city);
//                result.append("|");
//                result.append(isp);
//
//                return result.toString();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return null;
//        }
//        return null;
    }

    public static String getCityInfo(String ip) {

        if (org.springframework.util.StringUtils.isEmpty(dbPath)) {
            log.error("Error: Invalid ip2region.db file");
            return null;
        }
        if (config == null || searcher == null) {
            log.error("Error: DbSearcher or DbConfig is null");
            return null;
        }

        //查询算法
        //B-tree, B树搜索（更快）
        int algorithm = DbSearcher.BTREE_ALGORITHM;

        //Binary,使用二分搜索
        //DbSearcher.BINARY_ALGORITHM

        //Memory,加载内存（最快）
        //DbSearcher.MEMORY_ALGORITYM
        try {
            // 使用静态代码块，减少文件读取操作
//            DbConfig config = new DbConfig();
//            DbSearcher searcher = new DbSearcher(config, dbPath);

            //define the method
            Method method = null;
            switch (algorithm) {
                case DbSearcher.BTREE_ALGORITHM:
                    method = searcher.getClass().getMethod("btreeSearch", String.class);
                    break;
                case DbSearcher.BINARY_ALGORITHM:
                    method = searcher.getClass().getMethod("binarySearch", String.class);
                    break;
                case DbSearcher.MEMORY_ALGORITYM:
                    method = searcher.getClass().getMethod("memorySearch", String.class);
                    break;
                default:
            }

            DataBlock dataBlock = null;
            if (Util.isIpAddress(ip) == false) {
                System.out.println("Error: Invalid ip address");
            }

            dataBlock = (DataBlock) method.invoke(searcher, ip);
            String ipInfo = dataBlock.getRegion();
            if (!org.springframework.util.StringUtils.isEmpty(ipInfo)) {
                ipInfo = ipInfo.replace("|0", "");
                ipInfo = ipInfo.replace("0|", "");
            }
            return ipInfo;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 创建ip2region文件
     *
     * @return
     */
    public static String createFtlFileByFtlArray() {
        String ftlPath = "city/";
        return createFtlFile(ftlPath, "ip2region.db");
    }

    /**
     * 创建文件
     *
     * @param ftlPath
     * @param ftlName
     * @return
     */
    private static String createFtlFile(String ftlPath, String ftlName) {
        InputStream certStream = null;
        try {
            //获取当前项目所在的绝对路径
            String proFilePath = System.getProperty("user.dir");

            //获取模板下的路径，然后存放在temp目录下　
            String newFilePath = proFilePath + File.separator + "temp" + File.separator + ftlPath;
            newFilePath = newFilePath.replace("/", File.separator);
            //检查项目运行时的src下的对应路径
            File newFile = new File(newFilePath + ftlName);
            if (newFile.isFile() && newFile.exists()) {
                return newFilePath;
            }
            //当项目打成jar包会运行下面的代码，并且复制一份到src路径下（具体结构看下面图片）
            certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ftlPath + ftlName);
            byte[] certData = org.apache.commons.io.IOUtils.toByteArray(certStream);
            org.apache.commons.io.FileUtils.writeByteArrayToFile(newFile, certData);
            return newFilePath;
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                certStream.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

}
