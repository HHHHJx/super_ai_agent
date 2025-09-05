package com.jx.superaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// 测试exa API 联网搜索功能
@SpringBootTest
class ExaWebSearchToolTest {
    @Value("${search-api.api-key}")
    private String apiKey;

    @Test
    void doExaWebSearchTool() {
        ExaWebSearchTool tool = new ExaWebSearchTool(apiKey);
        String query = "American";
        String result = tool.exaSearch(query);
        assertNotNull(result);
    }



}