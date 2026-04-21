package com.hongshu.web.service.web;

import com.alibaba.fastjson2.JSON;
import com.hongshu.web.domain.vo.XiaohongshuItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 小红书爬虫服务
 * 使用Python脚本进行爬取
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class XiaohongshuSpiderService {

    /**
     * Python脚本路径（需要根据实际部署路径调整）
     */
    private static final String PYTHON_SCRIPT_PATH = "scripts/xiaohongshu_spider.py";
    
    /**
     * 获取Python命令
     * 优先使用配置的环境变量，否则尝试多个可能的命令
     */
    private String getPythonCommand() {
        // 优先使用配置的环境变量
        String pythonCmd = System.getenv("PYTHON_CMD");
        if (pythonCmd != null && !pythonCmd.trim().isEmpty()) {
            log.info("使用环境变量中的Python命令: {}", pythonCmd);
            return pythonCmd.trim();
        }
        
        // 尝试多个可能的Python命令
        String[] possibleCommands = System.getProperty("os.name").toLowerCase().contains("win") 
                ? new String[]{"python", "py", "python3"}
                : new String[]{"python3", "python3.9", "python3.8", "python3.7", "python"};
        
        for (String cmd : possibleCommands) {
            if (isCommandAvailable(cmd)) {
                log.info("找到可用的Python命令: {}", cmd);
                return cmd;
            }
        }
        
        // 如果都找不到，默认返回python3（让错误更明确）
        log.warn("未找到可用的Python命令，将使用默认值: python3");
        return "python3";
    }
    
    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据关键词爬取小红书内容
     *
     * @param keyword  关键词
     * @param type     类型：image-图片, video-视频, note-笔记
     * @param sortType 排序方式：0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏
     * @param noteTime 笔记时间：0-不限, 1-一天内, 2-一周内, 3-半年内
     * @param page     页码
     * @param pageSize 每页数量
     * @param cookie   Cookie值（必需，用于登录后的请求）
     * @return 爬取结果列表
     */
    public List<XiaohongshuItemVO> spiderByKeyword(String keyword, String type, Integer sortType, Integer noteTime, Integer page, Integer pageSize, String cookie) {
        log.info("开始爬取小红书 - 关键词: {}, 类型: {}, 排序方式: {}, 笔记时间: {}, 页码: {}, 每页数量: {}, 是否使用Cookie: {}", 
                keyword, type, sortType, noteTime, page, pageSize, cookie != null && !cookie.isEmpty());

        try {
            // 构建Python命令
            String scriptPath = getScriptPath();
            String pythonCmd = getPythonCommand();
            List<String> command = new ArrayList<>();
            command.add(pythonCmd);
            command.add(scriptPath);
            command.add("--keyword");
            command.add(keyword);
            command.add("--type");
            command.add(type != null ? type : "note");
            command.add("--sortType");
            command.add(String.valueOf(sortType != null ? sortType : 0));
            command.add("--noteTime");
            command.add(String.valueOf(noteTime != null ? noteTime : 0));
            command.add("--page");
            command.add(String.valueOf(page != null && page > 0 ? page : 1));
            command.add("--pageSize");
            command.add(String.valueOf(pageSize != null && pageSize > 0 ? pageSize : 10));
            
            // 如果提供了Cookie，添加到命令参数
            if (cookie != null && !cookie.trim().isEmpty()) {
                command.add("--cookie");
                command.add(cookie.trim());
            }

            log.info("执行Python命令: {}", String.join(" ", command));

            // 执行Python脚本
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // 不重定向错误流，分别读取stdout和stderr
            Process process;
            try {
                process = processBuilder.start();
            } catch (java.io.IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("No such file or directory")) {
                    String errorMsg = String.format(
                        "找不到Python命令 '%s'。请确保服务器已安装Python3，或通过环境变量PYTHON_CMD指定Python命令路径。\n" +
                        "例如：export PYTHON_CMD=/usr/bin/python3",
                        pythonCmd
                    );
                    log.error(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
                throw e;
            }

            // 读取标准输出（JSON数据）
            StringBuilder stdoutOutput = new StringBuilder();
            StringBuilder stderrOutput = new StringBuilder();
            
            // 使用线程分别读取stdout和stderr，避免阻塞
            Thread stdoutReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdoutOutput.append(line).append("\n");
                        log.debug("Python stdout: {}", line);
                    }
                } catch (Exception e) {
                    log.error("读取Python stdout失败", e);
                }
            });
            
            Thread stderrReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderrOutput.append(line).append("\n");
                        log.debug("Python stderr: {}", line);
                    }
                } catch (Exception e) {
                    log.error("读取Python stderr失败", e);
                }
            });
            
            stdoutReader.start();
            stderrReader.start();
            
            // 等待进程结束
            int exitCode = process.waitFor();
            
            // 等待读取线程完成
            stdoutReader.join(5000); // 最多等待5秒
            stderrReader.join(5000);
            
            String stdoutStr = stdoutOutput.toString().trim();
            String stderrStr = stderrOutput.toString().trim();
            
            if (exitCode != 0) {
                log.error("Python脚本执行失败，退出码: {}, stderr: {}", exitCode, stderrStr);
                
                // 检查是否是缺少依赖的错误
                if (stderrStr.contains("ModuleNotFoundError") || stderrStr.contains("No module named")) {
                    throw new RuntimeException("Python依赖未安装，请执行: pip install requests beautifulsoup4 urllib3");
                }
                
                throw new RuntimeException("Python脚本执行失败，退出码: " + exitCode + ", 错误: " + stderrStr);
            }

            // 解析JSON结果 - 只从stdout读取
            String jsonOutput = stdoutStr;
            log.info("Python脚本返回结果长度: {}", jsonOutput.length());

            // 查找JSON部分（可能包含其他输出）
            int jsonStart = jsonOutput.indexOf("[");
            int jsonEnd = jsonOutput.lastIndexOf("]");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonOutput = jsonOutput.substring(jsonStart, jsonEnd + 1);
            } else if (jsonOutput.isEmpty()) {
                log.warn("Python脚本未返回任何输出，stderr: {}", stderrStr);
                throw new RuntimeException("Python脚本未返回有效数据");
            }

            List<XiaohongshuItemVO> results = JSON.parseArray(jsonOutput, XiaohongshuItemVO.class);
            
            // 判断是否为真实数据（非模拟数据）
            int realDataCount = 0;
            if (results != null) {
                for (XiaohongshuItemVO item : results) {
                    // 模拟数据的特征：id以mock_开头，或cover是placeholder
                    if (item.getId() != null && !item.getId().startsWith("mock_") 
                        && (item.getCover() == null || !item.getCover().contains("via.placeholder.com"))) {
                        realDataCount++;
                    }
                }
            }
            
            log.info("爬取完成，共获取 {} 条结果，其中真实数据 {} 条", 
                    results != null ? results.size() : 0, realDataCount);
            
            // 如果检测到是模拟数据，返回空列表
            if (realDataCount == 0 && results != null && results.size() > 0) {
                log.warn("⚠️ 检测到模拟数据，返回空列表。请检查Cookie是否有效或API是否正常");
                return new ArrayList<>();
            }
            
            // 如果结果为空或只有真实数据，直接返回
            return results != null ? results : new ArrayList<>();

        } catch (Exception e) {
            log.error("爬取小红书失败", e);
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取Python脚本路径
     * 优先使用配置的绝对路径，然后尝试classpath，最后使用相对路径
     */
    private String getScriptPath() {
        // 优先使用环境变量或系统属性配置的脚本路径
        String configPath = System.getenv("XIAOHONGSHU_SCRIPT_PATH");
        if (configPath == null || configPath.trim().isEmpty()) {
            configPath = System.getProperty("xiaohongshu.script.path");
        }
        if (configPath != null && !configPath.trim().isEmpty()) {
            String path = configPath.trim();
            java.io.File file = new java.io.File(path);
            if (file.exists() && file.isFile()) {
                log.info("使用配置的脚本路径: {}", path);
                return path;
            } else {
                log.warn("配置的脚本路径不存在: {}", path);
            }
        }
        
        // 尝试从classpath获取（开发环境）
        java.net.URL resource = getClass().getClassLoader().getResource("scripts/xiaohongshu_spider.py");
        if (resource != null) {
            try {
                // 如果是文件系统路径（开发环境）
                if ("file".equals(resource.getProtocol())) {
                    return new java.io.File(resource.toURI()).getAbsolutePath();
                } else {
                    // 如果是jar包中的资源，需要提取到临时文件
                    log.warn("脚本在jar包中，需要提取到临时文件");
                }
            } catch (Exception e) {
                log.warn("无法获取classpath中的脚本路径", e);
            }
        }
        
        // 使用绝对路径（生产环境，从日志看是 /app/scripts/xiaohongshu_spider.py）
        String[] possiblePaths = {
            "/app/scripts/xiaohongshu_spider.py",
            System.getProperty("user.dir") + "/" + PYTHON_SCRIPT_PATH,
            System.getProperty("user.dir") + "/scripts/xiaohongshu_spider.py"
        };
        
        for (String path : possiblePaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists() && file.isFile()) {
                log.info("使用脚本路径: {}", path);
                return path;
            }
        }
        
        // 如果都找不到，返回默认路径（让错误更明确）
        String defaultPath = "/app/scripts/xiaohongshu_spider.py";
        log.warn("未找到脚本文件，将使用默认路径: {}", defaultPath);
        return defaultPath;
    }
}
