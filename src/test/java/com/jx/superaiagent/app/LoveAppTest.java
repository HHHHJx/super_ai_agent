package com.jx.superaiagent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;


    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRagCloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRagCloud(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
//        // 测试文件操作：读取文件
//        testMessage("从保存文件的地方读取编程导航.txt里的内容");

//        // 测试文件操作：写入文件
//        testMessage("从保存文件的地方读取编程导航.txt里的内容，然后将里面的内容写入一个新的文件，名字为编程导航2.txt");

//        // 测试 PDF 生成
//        testMessage("生成一份内容是关于恋爱建议的PDF。");

//        // 测试终端操作：执行代码
//        testMessage("使用命令行执行罗列当前目录下所有文件的命令，注意我是Windows系统");

//        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，请你联网搜索，然后推荐几个2025年适合情侣的小众打卡地？");

//        // 测试网页抓取
//        testMessage("抓取这个网页的信息，https://www.codefather.cn");

        // 测试资源下载
        testMessage("从这个网页中下载图片https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png，并保存为01.png");

    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
