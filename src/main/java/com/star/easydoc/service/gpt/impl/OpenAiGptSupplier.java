package com.star.easydoc.service.gpt.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.star.easydoc.common.util.HttpUtil;
import org.apache.commons.lang.StringUtils;

/**
 * OpenAI GPT API实现 文档 https://platform.openai.com/docs/api-reference/chat
 *
 * @author wangchao
 * @date 2024/03/05
 */
public class OpenAiGptSupplier extends AbstractGptSupplier {

    /** 超时 */
    private static final int TIMEOUT = 30 * 1000;

    @Override
    public String chat(String content) {
        
        String baseUrl = getConfig().getOpenaiBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "https://api.openai.com";
        }
        
        // 确保URL以/结尾，避免重复斜杠
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        String url = baseUrl + "v1/chat/completions";

        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", "Bearer " + getConfig().getOpenaiApiKey());
        headers.put("Content-Type", "application/json");

        Message message = new Message();
        message.setContent(content);
        message.setRole("user");
        
        OpenAiRequest request = new OpenAiRequest();
        request.setModel(getConfig().getOpenaiModel());
        request.setMessages(Lists.newArrayList(message));
        
        // 设置温度参数
        if (getConfig().getOpenaiTemperature() != null) {
            request.setTemperature(getConfig().getOpenaiTemperature());
        }
        
        // 设置top_p参数（OpenAI中topK用top_p代替）
        if (getConfig().getOpenaiTopK() != null) {
            // 将topK转换为top_p (0.0-1.0范围)
            double topP = Math.min(1.0, Math.max(0.0, getConfig().getOpenaiTopK() / 100.0));
            request.setTopP(topP);
        }

        try {
            String response = HttpUtil.postJson(url, headers, JSON.toJSONString(request), TIMEOUT);
            
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("OpenAI API响应为空");
            }

            com.alibaba.fastjson2.JSONObject jsonResponse = JSON.parseObject(response);
            
            // 检查API错误
            if (jsonResponse.containsKey("error")) {
                com.alibaba.fastjson2.JSONObject error = jsonResponse.getJSONObject("error");
                String errorMessage = error.getString("message");
                String errorType = error.getString("type");
                throw new RuntimeException("OpenAI API错误: " + errorType + " - " + errorMessage);
            }
            
            // 解析正常响应
            if (!jsonResponse.containsKey("choices") || jsonResponse.getJSONArray("choices").isEmpty()) {
                throw new RuntimeException("OpenAI API响应格式错误：缺少choices字段");
            }
            
            com.alibaba.fastjson2.JSONArray choices = jsonResponse.getJSONArray("choices");
            com.alibaba.fastjson2.JSONObject firstChoice = choices.getJSONObject(0);
            
            if (!firstChoice.containsKey("message")) {
                throw new RuntimeException("OpenAI API响应格式错误：缺少message字段");
            }
            
            com.alibaba.fastjson2.JSONObject messageObj = firstChoice.getJSONObject("message");
            String result = messageObj.getString("content");
            
            if (StringUtils.isBlank(result)) {
                throw new RuntimeException("OpenAI API返回内容为空");
            }

            // 处理返回结果，确保返回正确的javadoc格式
            return formatJavadocResult(result.trim());
            
        } catch (Exception e) {
            throw new RuntimeException("调用OpenAI API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 格式化javadoc结果
     * 
     * @param result AI返回的原始结果
     * @return 格式化后的javadoc注释
     */
    private String formatJavadocResult(String result) {
        if (StringUtils.isBlank(result)) {
            return "/** */";
        }
        
        // 如果已经是完整的javadoc格式，直接返回
        if (result.startsWith("/**") && result.endsWith("*/")) {
            return result;
        }
        
        // 如果包含javadoc标记但格式不完整，提取内容重新格式化
        if (result.contains("/**") && result.contains("*/")) {
            String content = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(result, "/**"), "*/");
            if (StringUtils.isNotBlank(content)) {
                return "/**" + content + "*/";
            }
        }
        
        // 如果没有javadoc标记，添加标记
        return "/**" + (result.startsWith(" ") ? "" : " ") + result + (result.endsWith(" ") ? "" : " ") + "*/";
    }

    private static class OpenAiRequest {

        /** 所要调用的模型编码 */
        private String model = "gpt-3.5-turbo";
        /** 当前对话信息列表 */
        private List<Message> messages;
        /** 是否开启流式模式 */
        private Boolean stream = false;
        /** 温度参数 (0.0-2.0) */
        private Double temperature = 0.7;
        /** top_p参数 (0.0-1.0) */
        private Double topP;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }

        public Boolean getStream() {
            return stream;
        }

        public void setStream(Boolean stream) {
            this.stream = stream;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }
    }

    private static class Message {
        /** 消息的角色信息，system，assistant，user */
        private String role;
        /** 消息内容 */
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
} 