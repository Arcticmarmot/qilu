package com.marmot.qilu.modules.search.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marmot.qilu.common.config.OllamaChatProperties;
import com.marmot.qilu.common.exception.BizException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OllamaChatClientTest {

    private HttpServer server;
    private OllamaChatClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        OllamaChatProperties properties = new OllamaChatProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        client = new OllamaChatClient(properties);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsNonStreamingChatToOllamaAndParsesAnswer() {
        AtomicReference<JsonNode> request = new AtomicReference<>();
        server.createContext("/api/chat", exchange -> {
            request.set(new ObjectMapper().readTree(exchange.getRequestBody()));
            byte[] body = "{\"message\":{\"role\":\"assistant\",\"content\":\"幂等重试。[1]\"},\"done\":true,\"eval_count\":10}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        assertEquals("幂等重试。[1]", client.chat("system rules", "real post evidence"));
        assertEquals("qwen3.5:4b", request.get().get("model").asText());
        assertFalse(request.get().get("stream").asBoolean());
        assertFalse(request.get().get("think").asBoolean());
        assertEquals(16384, request.get().get("options").get("num_ctx").asInt());
        assertEquals("system", request.get().get("messages").get(0).get("role").asText());
        assertEquals("real post evidence", request.get().get("messages").get(1).get("content").asText());
    }

    @Test
    void reportsOllamaHttpFailureClearly() {
        server.createContext("/api/chat", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });

        BizException error = assertThrows(BizException.class, () -> client.chat("rules", "evidence"));

        assertTrue(error.getMessage().contains("Ollama 调用失败（HTTP 503）"));
    }

    @Test
    void rejectsEmptyOrIncompleteModelResponse() {
        server.createContext("/api/chat", exchange -> {
            byte[] body = "{\"message\":{\"role\":\"assistant\",\"content\":\"\"},\"done\":true}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        BizException error = assertThrows(BizException.class, () -> client.chat("rules", "evidence"));

        assertTrue(error.getMessage().contains("未返回完整回答"));
    }

    @Test
    void reportsReadTimeoutWithoutWaitingIndefinitely() {
        CountDownLatch release = new CountDownLatch(1);
        server.createContext("/api/chat", exchange -> {
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        OllamaChatProperties properties = new OllamaChatProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setReadTimeout(Duration.ofMillis(100));
        OllamaChatClient shortTimeoutClient = new OllamaChatClient(properties);
        try {
            BizException error = assertThrows(BizException.class, () -> shortTimeoutClient.chat("rules", "evidence"));
            assertTrue(error.getMessage().contains("失败或超时"));
        } finally {
            release.countDown();
        }
    }
}
