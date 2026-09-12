package com.example.jobapplication;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

/**
 * 简历投递记录系统启动类
 *
 * 桌面应用双击入口：
 * 1) 若 8082 端口已被本系统占用（已在运行），则只打开浏览器，不重复启动；
 * 2) 否则正常启动，启动完成后自动打开浏览器进入系统。
 */
@SpringBootApplication
@MapperScan("com.example.jobapplication.mapper")
public class JobApplicationSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationSystemApplication.class);

    /** 与 application.yml 中 server.port 保持一致 */
    private static final int SERVER_PORT = 8082;

    public static void main(String[] args) {
        if (isPortListening(SERVER_PORT)) {
            log.warn("端口 {} 已被占用，系统可能已在运行，直接打开浏览器", SERVER_PORT);
            openBrowser("http://localhost:" + SERVER_PORT);
            return;
        }
        SpringApplication.run(JobApplicationSystemApplication.class, args);
    }

    /**
     * 启动完成后自动打开浏览器
     */
    @Bean
    public ApplicationRunner openBrowserRunner(@Value("${server.port:8082}") int port) {
        return args -> openBrowser("http://localhost:" + port);
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", url});
            }
        } catch (Exception e) {
            log.warn("自动打开浏览器失败，请手动访问 {}", url, e);
        }
    }

    private static boolean isPortListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 800);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}