package com.serenitydojo.playwright.todomvc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

public class OpenApplication {
    private static Process frontendProcess;

    @BeforeAll
    static void startFrontendApp() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\nodejs\\npm.cmd", "run", "serve");
        pb.directory(new File("../react-todomvc"));
        pb.inheritIO();
        frontendProcess = pb.start();


        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> isAppRunning("http://localhost:7002"));
    }

    private static boolean isAppRunning(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.connect();
            return conn.getResponseCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    @AfterAll
    static void stopFrontendApp() {
        if (frontendProcess != null && frontendProcess.isAlive()) {
            ProcessHandle handle = frontendProcess.toHandle();


            handle.descendants().forEach(ProcessHandle::destroy);
            handle.destroy();


            try {
                boolean exited = frontendProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    System.out.println("Force killing frontend and children...");
                    handle.descendants().forEach(ProcessHandle::destroyForcibly);
                    handle.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handle.destroyForcibly();
            }
        }
    }
}
