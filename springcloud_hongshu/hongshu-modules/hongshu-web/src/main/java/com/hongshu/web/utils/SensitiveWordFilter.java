package com.hongshu.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 敏感词过滤工具类 - 基于DFA算法
 *
 * @author hongshu
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /**
     * 敏感词库（DFA算法的HashMap树）
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 最小匹配规则
     */
    public static final int MIN_MATCH_TYPE = 1;

    /**
     * 最大匹配规则
     */
    public static final int MAX_MATCH_TYPE = 2;

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        try {
            // 从配置文件加载敏感词
            Set<String> sensitiveWords = loadSensitiveWords();
            log.info("加载敏感词库，共{}个敏感词", sensitiveWords.size());
            initSensitiveWordMap(sensitiveWords);
        } catch (Exception e) {
            log.error("初始化敏感词库失败", e);
            // 使用默认敏感词
            Set<String> defaultWords = getDefaultSensitiveWords();
            initSensitiveWordMap(defaultWords);
        }
    }

    /**
     * 从文件加载敏感词
     */
    private Set<String> loadSensitiveWords() {
        Set<String> words = new HashSet<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        words.add(line);
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            log.warn("加载敏感词文件失败，使用默认敏感词", e);
        }
        
        // 如果文件为空，使用默认敏感词
        if (words.isEmpty()) {
            words = getDefaultSensitiveWords();
        }
        
        return words;
    }

    /**
     * 默认敏感词库
     */
    private Set<String> getDefaultSensitiveWords() {
        Set<String> words = new HashSet<>();
        // 基础敏感词
        words.add("傻逼");
        words.add("操你妈");
        words.add("去死");
        words.add("垃圾");
        words.add("sb");
        words.add("nmsl");
        words.add("cnm");
        // 可以继续添加更多
        return words;
    }

    /**
     * 初始化敏感词库为HashMap树
     */
    private void initSensitiveWordMap(Set<String> sensitiveWords) {
        sensitiveWordMap = new HashMap<>(sensitiveWords.size());
        for (String word : sensitiveWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            Map<String, Object> nowMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                char keyChar = word.charAt(i);
                String key = String.valueOf(keyChar);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> wordMap = (Map<String, Object>) nowMap.get(key);
                
                if (wordMap != null) {
                    nowMap = wordMap;
                } else {
                    Map<String, Object> newWordMap = new HashMap<>();
                    newWordMap.put("isEnd", "0");
                    nowMap.put(key, newWordMap);
                    nowMap = newWordMap;
                }
                
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 判断文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int matchFlag = checkSensitiveWord(text, i, MAX_MATCH_TYPE);
            if (matchFlag > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中的所有敏感词
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> sensitiveWords = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return sensitiveWords;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i, MAX_MATCH_TYPE);
            if (length > 0) {
                sensitiveWords.add(text.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWords;
    }

    /**
     * 替换敏感词为*
     */
    public String replaceSensitiveWord(String text, char replaceChar) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text);
        Set<String> words = getSensitiveWords(text);
        for (String word : words) {
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                replacement.append(replaceChar);
            }
            int index = result.indexOf(word);
            while (index != -1) {
                result.replace(index, index + word.length(), replacement.toString());
                index = result.indexOf(word, index + replacement.length());
            }
        }
        return result.toString();
    }

    /**
     * 检查文字中是否包含敏感字符
     */
    private int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        boolean flag = false;
        int matchFlag = 0;
        Map<String, Object> nowMap = sensitiveWordMap;
        
        for (int i = beginIndex; i < txt.length(); i++) {
            char word = txt.charAt(i);
            String key = String.valueOf(word);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> wordMap = (Map<String, Object>) nowMap.get(key);
            
            if (wordMap != null) {
                matchFlag++;
                if ("1".equals(wordMap.get("isEnd"))) {
                    flag = true;
                    if (MIN_MATCH_TYPE == matchType) {
                        break;
                    }
                }
                nowMap = wordMap;
            } else {
                break;
            }
        }
        
        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * 添加敏感词
     */
    public void addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        Set<String> words = new HashSet<>();
        words.add(word);
        initSensitiveWordMap(words);
    }
}
