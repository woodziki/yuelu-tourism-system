package com.yuelu.controller;

import com.yuelu.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${ai.base-url:}")
    private String aiBaseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return Result.error("消息不能为空");
        }
        if (mockEnabled) {
            return Result.success(new ChatResponse("【测试模式】收到问题：" + request.getMessage()));
        }

        if (restTemplate == null) {
            return Result.success(new ChatResponse("内部配置错误：RestTemplate未注入。"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false); 

        // 极简传递：因为云端 Bot 已经自带人设和联网功能，我们只需要传用户说的话！
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        body.put("messages", messages);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(aiBaseUrl, entity, Map.class);
            Map data = response.getBody();

            if (data != null && data.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        String answer = (String) message.get("content");
                        return Result.success(new ChatResponse(answer));
                    }
                }
            }
            return Result.success(new ChatResponse("AI 返回格式异常，请查看后端控制台或检查 API-KEY 余量。"));

        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(new ChatResponse("AI 请求失败，请检查网络或 API-KEY 是否正确：" + e.getMessage()));
        }
    }

    public static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatResponse {
        private String answer;
        public ChatResponse() {}
        public ChatResponse(String answer) { this.answer = answer; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}
