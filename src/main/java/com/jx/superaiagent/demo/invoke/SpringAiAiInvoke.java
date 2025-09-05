package com.jx.superaiagent.demo.invoke;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 使用Spring AI Alibaba 框架调用AI大模型
 */

// 使用注解@Component 将该类注入到Spring容器中，然后实现了CommandLineRunner接口，在SpringBoot启动时执行。
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("你好，我是jx"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}
