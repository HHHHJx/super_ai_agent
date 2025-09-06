package com.jx.superaiagent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder(); // 存储命令输出结果，使用StringBuilder拼接性能更好。
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);  // 创建系统进程，打开cmd命令窗口。
            Process process = builder.start(); // 启动进程，执行命令

            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            // 阻塞当前线程，直到终端命令执行完成。
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }

        return output.toString();
    }

    // 使用Spring AI的ToolContext存储会话专属状态。
    // Spring AI 会在工具调用时自动注入 ToolContext，且每个对话会话（通过 chatId 区分）的 ToolContext 是独立的，因此不同用户的命令状态不会冲突。
    // 后续可以直接集成到ToolCallback[]里，然后在和AI对话的时候调用。
    @Tool(description = "Execute terminal commands (session-aware, multi-round support)")
    public String executeTerminalCommandPro(
            @ToolParam(description = "Command to execute (e.g., 'cd /data', 'ls')") String command,
            ToolContext toolContext) { // 注入ToolContext，用于存储会话状态

        StringBuilder output = new StringBuilder();
        Process process = null;

        // 1. 从ToolContext获取当前会话的工作目录（无则初始化）
        File currentWorkingDir = (File) toolContext.getContext().getOrDefault(
                "terminal_current_dir",
                new File(System.getProperty("user.dir"))
        );

        try {
            // 2. 处理cd命令，更新工作目录
            if (command.trim().toLowerCase().startsWith("cd ")) {
                String targetDir = command.trim().substring(3).trim();
                File newDir = new File(currentWorkingDir, targetDir);
                if (newDir.exists() && newDir.isDirectory()) {
                    currentWorkingDir = newDir.getCanonicalFile();
                    // 将更新后的目录存回ToolContext（绑定当前会话）
                    toolContext.getContext().put("terminal_current_dir", currentWorkingDir);
                    output.append("Changed directory to: ").append(currentWorkingDir.getAbsolutePath());
                } else {
                    output.append("Directory not found: ").append(newDir.getAbsolutePath());
                }
                return output.toString();
            }

            // 3. 执行非cd命令，基于当前目录
            ProcessBuilder builder = getOsProcessBuilder(command);
            builder.directory(currentWorkingDir);
            process = builder.start();

            // 4. 读取输出（同前）
            List<String> stdout = readStream(process.getInputStream());
            List<String> stderr = readStream(process.getErrorStream());
            output.append("Output:\n").append(String.join("\n", stdout)).append("\n");
            if (!stderr.isEmpty()) {
                output.append("Error:\n").append(String.join("\n", stderr)).append("\n");
            }

            int exitCode = process.waitFor();
            output.append("Exit code: ").append(exitCode);

        } catch (IOException | InterruptedException e) {
            output.append("Error: ").append(e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }

        return output.toString();
    }

    // 辅助方法：根据操作系统创建ProcessBuilder（解决跨平台问题）
    private ProcessBuilder getOsProcessBuilder(String command) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            return new ProcessBuilder("/bin/sh", "-c", command);
        }
    }

    // 辅助方法：读取输入流（统一处理stdout和stderr）
    private List<String> readStream(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
