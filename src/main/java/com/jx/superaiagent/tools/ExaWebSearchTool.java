package com.jx.superaiagent.tools;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Exa Web Search工具
 * 提供基于Exa的网络搜索功能
 */
@Slf4j
public class ExaWebSearchTool {

    private final String exaApiKey;

    // 确认API地址与官方一致（避免拼写错误）
    private static final String SEARCH_API_URL = "https://api.exa.ai/search";

    public ExaWebSearchTool(String exaApiKey) {
        // 初始化时校验API密钥，避免空值导致后续请求失败
        if (ObjectUtil.isEmpty(exaApiKey)) {
            throw new IllegalArgumentException("Exa API Key 不能为空，请检查配置");
        }
        this.exaApiKey = exaApiKey;
    }

    /**
     * 执行网络搜索
     *
     * @param searchQuery 搜索内容
     * @return 搜索结果摘要列表
     */
    @Tool(description = "使用EXA提供的Web Search功能进行网络搜索。如果出现搜索失败，可以尝试多次调用该工具")
    public String exaSearch(
            @ToolParam(description = "搜索内容，需具体明确（如\"2024年LLM最新研究进展\"）")
            String searchQuery) {
        // 校验搜索关键词，避免空请求
        if (ObjectUtil.isEmpty(searchQuery) || searchQuery.trim().length() < 2) {
            log.warn("搜索关键词为空或过短：{}", searchQuery);
            return "搜索关键词不能为空且长度需至少2个字符，请重新输入";
        }
        log.info("调用 EXA API 搜索关键词：{}", searchQuery);

        try {
            // 1. 构建【符合Exa API要求】的请求参数
            // 正确结构：{"query":"xxx", "contents":{"text":true, "maxCharacters":1000}}
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("query", searchQuery.trim()); // 去除关键词前后空格，避免无效请求

            // 配置contents（获取文本内容，限制最大长度1000字符，避免返回内容过长）
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> textConfig = new HashMap<>();
            textConfig.put("true", true); // 启用文本获取（API要求该字段控制是否返回文本）
            textConfig.put("maxCharacters", 1000); // 可选：限制文本长度，避免冗余
            contents.put("text", textConfig);
            paramMap.put("contents", contents); // 将contents作为顶层参数（关键修复）

            // 2. 转换为JSON字符串（Hutool的JSONUtil会自动处理嵌套结构）
            String requestBodyJson = JSONUtil.toJsonStr(paramMap);
            log.debug("Exa API 请求体：{}", requestBodyJson); // 调试时打印请求体，便于排查格式问题

            // 3. 发送HTTP POST请求（确保Header完整）
            HttpResponse response = HttpRequest.post(SEARCH_API_URL)
                    .header("x-api-key", exaApiKey) // 认证Header（必须正确）
                    .header("Content-Type", "application/json") // 明确JSON格式（必须）
                    .body(requestBodyJson)
                    .timeout(10000) // 增加超时时间（10秒），避免网络波动导致超时
                    .execute();

            // 4. 处理响应（修复摘要解析逻辑）
            int status = response.getStatus();
            String body = response.body();
            log.debug("Exa API 响应状态码：{}，响应内容：{}", status, body);

            if (status == 200 && ObjectUtil.isNotEmpty(body)) {
                JSONObject jsonResponse = JSONUtil.parseObj(body);
                JSONArray resultsArray = jsonResponse.getJSONArray("results");

                if (resultsArray != null && !resultsArray.isEmpty()) {
                    StringBuilder resultBuilder = new StringBuilder();
                    resultBuilder.append("本次搜索共找到 ").append(resultsArray.size()).append(" 条相关结果：\n\n");

                    for (int i = 0; i < resultsArray.size(); i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        // 从API响应中正确提取字段（参考官方响应结构）
                        String title = ObjectUtil.defaultIfNull(result.getStr("title"), "无标题");
                        String url = ObjectUtil.defaultIfNull(result.getStr("url"), "无链接");
                        // 修复：直接获取result.text（字符串类型），而非嵌套的snippet
                        String snippet = ObjectUtil.defaultIfNull(result.getStr("text"), "无摘要信息");
                        // 可选：补充发布时间、作者等信息
                        String publishedDate = ObjectUtil.defaultIfNull(result.getStr("publishedDate"), "未知时间");

                        // 格式化结果（清晰易读，AI可直接使用）
                        resultBuilder.append("【结果 ").append(i + 1).append("】\n");
                        resultBuilder.append("标题：").append(title).append("\n");
                        resultBuilder.append("发布时间：").append(publishedDate).append("\n");
                        resultBuilder.append("链接：").append(url).append("\n");
                        resultBuilder.append("摘要：").append(snippet.substring(0, Math.min(snippet.length(), 200))).append("...\n"); // 截取前200字符，避免过长
                        resultBuilder.append("-------------------------\n");
                    }
                    log.info("联网搜索结果为：{}", resultBuilder.toString());
                    return resultBuilder.toString();
                } else {
                    return "未找到与「" + searchQuery + "」相关的结果";
                }
            } else {
                log.error("Exa API 请求失败，状态码：{}，响应内容：{}", status, body);
                // 根据400错误的具体响应内容，返回更明确的提示（API通常会说明参数错误原因）
                String errorMsg = ObjectUtil.isNotEmpty(body) ? body : "无详细错误信息";
                return "搜索请求失败（状态码：" + status + "），错误详情：" + errorMsg;
            }
        } catch (Exception e) {
            log.error("调用 Exa 搜索服务时发生异常", e);
            return "搜索过程中发生系统错误：" + e.getMessage();
        }
    }
}