package com.star.easydoc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 远程单词映射服务
 * 
 * @author wangchao
 */
public class RemoteWordMapService {
    
    private static final Logger LOGGER = Logger.getInstance(RemoteWordMapService.class);
    private static final int CONNECT_TIMEOUT = 5000; // 5秒连接超时
    private static final int READ_TIMEOUT = 10000; // 10秒读取超时
    
    /**
     * 从远程URL获取单词映射
     * 
     * @param url 远程URL
     * @return 单词映射Map，如果获取失败返回空Map
     */
    public static Map<String, String> fetchRemoteWordMap(String url) {
        Map<String, String> wordMap = new LinkedHashMap<>();
        
        if (url == null || url.trim().isEmpty()) {
            LOGGER.warn("Remote word map URL is empty");
            return wordMap;
        }
        
        try {
            LOGGER.info("Fetching remote word map from: " + url);
            
            URL remoteUrl = new URL(url.trim());
            HttpURLConnection connection = (HttpURLConnection) remoteUrl.openConnection();
            
            // 设置请求属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "EasyJavaDoc-Plugin");
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.warn("Failed to fetch remote word map, HTTP response code: " + responseCode);
                return wordMap;
            }
            
            // 读取响应内容
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    
                    // 跳过空行和注释行
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                        continue;
                    }
                    
                    // 解析键值对
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            
                            if (!key.isEmpty() && !value.isEmpty()) {
                                wordMap.put(key, value);
                                LOGGER.debug("Loaded word mapping: " + key + " -> " + value);
                            }
                        } else {
                            LOGGER.warn("Invalid word mapping format at line " + lineNumber + ": " + line);
                        }
                    } else {
                        LOGGER.warn("Invalid word mapping format at line " + lineNumber + ": " + line);
                    }
                }
            }
            
            LOGGER.info("Successfully loaded " + wordMap.size() + " word mappings from remote URL");
            
        } catch (IOException e) {
            LOGGER.error("Failed to fetch remote word map from URL: " + url, e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while fetching remote word map", e);
        }
        
        return wordMap;
    }
    
    /**
     * 合并远程单词映射到本地映射
     * 远程映射会覆盖本地相同key的映射
     * 
     * @param localWordMap 本地单词映射
     * @param remoteUrl 远程URL
     * @return 合并后的单词映射
     */
    public static Map<String, String> mergeRemoteWordMap(Map<String, String> localWordMap, String remoteUrl) {
        Map<String, String> mergedMap = new LinkedHashMap<>();
        
        // 先添加本地映射
        if (localWordMap != null) {
            mergedMap.putAll(localWordMap);
        }
        
        // 获取远程映射并覆盖本地映射
        Map<String, String> remoteWordMap = fetchRemoteWordMap(remoteUrl);
        if (!remoteWordMap.isEmpty()) {
            int overwriteCount = 0;
            for (Map.Entry<String, String> entry : remoteWordMap.entrySet()) {
                if (mergedMap.containsKey(entry.getKey())) {
                    overwriteCount++;
                }
                mergedMap.put(entry.getKey(), entry.getValue());
            }
            
            LOGGER.info("Merged remote word map: " + remoteWordMap.size() + " remote mappings, " 
                       + overwriteCount + " local mappings overwritten");
        }
        
        return mergedMap;
    }
    
    /**
     * 验证远程URL是否可访问
     * 
     * @param url 远程URL
     * @return true表示可访问，false表示不可访问
     */
    public static boolean validateRemoteUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            URL remoteUrl = new URL(url.trim());
            HttpURLConnection connection = (HttpURLConnection) remoteUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "EasyJavaDoc-Plugin");
            
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
            
        } catch (Exception e) {
            LOGGER.debug("Remote URL validation failed for: " + url, e);
            return false;
        }
    }
} 