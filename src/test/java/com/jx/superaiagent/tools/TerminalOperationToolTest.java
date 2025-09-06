package com.jx.superaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "d: && dir /b";
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
    }

    @Test
    void executeTerminalCommandPro() {

    }
}