package com.star.easydoc.service;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 远程单词映射服务测试
 */
public class RemoteWordMapServiceTest {

    @Test
    public void testValidateRemoteUrl() {
        // Test with null and empty URL
        assertFalse(RemoteWordMapService.validateRemoteUrl(null));
        assertFalse(RemoteWordMapService.validateRemoteUrl(""));
        assertFalse(RemoteWordMapService.validateRemoteUrl("   "));
        
        // Test with invalid URL
        assertFalse(RemoteWordMapService.validateRemoteUrl("invalid-url"));
        
        // Test with a valid URL (this will make an actual HTTP request)
        // Note: This test requires internet connection
        String validUrl = "https://raw.githubusercontent.com/pig-mesh/easy_javadoc/refs/heads/master/keywords.txt";
        assertTrue("Valid URL should be accessible", RemoteWordMapService.validateRemoteUrl(validUrl));
    }

    @Test
    public void testFetchRemoteWordMap() {
        // Test with null and empty URL
        Map<String, String> result1 = RemoteWordMapService.fetchRemoteWordMap(null);
        assertTrue("Should return empty map for null URL", result1.isEmpty());
        
        Map<String, String> result2 = RemoteWordMapService.fetchRemoteWordMap("");
        assertTrue("Should return empty map for empty URL", result2.isEmpty());
        
        // Test with valid URL (this will make an actual HTTP request)
        // Note: This test requires internet connection
        String validUrl = "https://raw.githubusercontent.com/pig-mesh/easy_javadoc/refs/heads/master/keywords.txt";
        Map<String, String> result3 = RemoteWordMapService.fetchRemoteWordMap(validUrl);
        assertNotNull("Should return non-null map", result3);
        
        // Check that expected mappings are present
        assertTrue("Should contain 'dataset' mapping", result3.containsKey("dataset"));
        assertEquals("Should map 'dataset' to '知识库'", "知识库", result3.get("dataset"));
        
        assertTrue("Should contain 'embedding' mapping", result3.containsKey("embedding"));
        assertEquals("Should map 'embedding' to '向量'", "向量", result3.get("embedding"));
        
        assertTrue("Should contain 'status' mapping", result3.containsKey("status"));
        assertEquals("Should map 'status' to '状态'", "状态", result3.get("status"));
        
        assertTrue("Should contain 'usage' mapping", result3.containsKey("usage"));
        assertEquals("Should map 'usage' to '用量'", "用量", result3.get("usage"));
    }

    @Test
    public void testMergeRemoteWordMap() {
        // Create local word map
        Map<String, String> localMap = new java.util.HashMap<>();
        localMap.put("local1", "本地1");
        localMap.put("local2", "本地2");
        localMap.put("dataset", "本地数据集"); // This should be overridden by remote
        
        // Test with valid URL (this will make an actual HTTP request)
        String validUrl = "https://raw.githubusercontent.com/pig-mesh/easy_javadoc/refs/heads/master/keywords.txt";
        Map<String, String> mergedMap = RemoteWordMapService.mergeRemoteWordMap(localMap, validUrl);
        
        assertNotNull("Should return non-null merged map", mergedMap);
        
        // Check that local mappings are preserved
        assertEquals("Should preserve local1 mapping", "本地1", mergedMap.get("local1"));
        assertEquals("Should preserve local2 mapping", "本地2", mergedMap.get("local2"));
        
        // Check that remote mappings override local ones
        assertEquals("Remote should override local dataset mapping", "知识库", mergedMap.get("dataset"));
        
        // Check that remote mappings are added
        assertEquals("Should include remote embedding mapping", "向量", mergedMap.get("embedding"));
        assertEquals("Should include remote status mapping", "状态", mergedMap.get("status"));
        assertEquals("Should include remote usage mapping", "用量", mergedMap.get("usage"));
    }
} 