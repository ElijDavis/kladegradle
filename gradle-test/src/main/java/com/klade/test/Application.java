package com.klade.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class Application {
    private static final String DEFAULT_API = "https://jsonplaceholder.typicode.com/todos/1";
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        Map<String, Object> payload = probe();
        boolean injected = Boolean.TRUE.equals(payload.get("appNameSet"));
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Klade Gradle Probe</title>
              <style>
                body { margin:0; font-family:ui-sans-serif,system-ui,sans-serif; background:#0b1220; color:#e8eef7; }
                main { max-width:44rem; margin:0 auto; padding:2.5rem 1.25rem; }
                h1 { margin:0 0 .4rem; font-size:1.6rem; }
                .sub { color:#9fb0c8; margin-bottom:1.5rem; }
                .card { background:#121b2c; border:1px solid #24324a; border-radius:12px; padding:1.1rem 1.2rem; margin-bottom:1rem; }
                .row { display:flex; justify-content:space-between; gap:1rem; padding:.45rem 0; border-bottom:1px solid #1e2a40; }
                .row:last-child { border-bottom:0; }
                .k { color:#9fb0c8; }
                .v { font-family:ui-monospace,monospace; word-break:break-all; }
                .ok { color:#4ade80; } .bad { color:#f87171; }
                pre { margin:0; white-space:pre-wrap; word-break:break-word; font-size:.85rem; }
              </style>
            </head>
            <body>
              <main>
                <h1>Klade Gradle / Spring Boot probe</h1>
                <p class="sub">Set <code>APP_NAME</code> and <code>API_URL</code> in Klade. Spring reads them from process env at runtime.</p>
                <div class="card">
                  <div class="row"><span class="k">Status</span><span class="v %s">%s</span></div>
                  <div class="row"><span class="k">APP_NAME</span><span class="v">%s</span></div>
                  <div class="row"><span class="k">API_URL</span><span class="v">%s</span></div>
                </div>
                <div class="card">
                  <div class="row"><span class="k">Fetched JSON</span><span class="k">GET %s</span></div>
                  <pre>%s</pre>
                </div>
              </main>
            </body>
            </html>
            """.formatted(
                injected ? "ok" : "bad",
                injected ? "APP_NAME injected" : "APP_NAME missing",
                esc(String.valueOf(payload.get("appName"))),
                esc(String.valueOf(payload.get("apiUrl"))),
                esc(String.valueOf(payload.get("apiUrl"))),
                esc(String.valueOf(payload.get("remote")))
            );
    }

    @GetMapping("/api")
    public Map<String, Object> api() {
        return probe();
    }

    private Map<String, Object> probe() {
        String appName = System.getenv("APP_NAME");
        String apiUrl = System.getenv("API_URL");
        if (apiUrl == null || apiUrl.isBlank()) {
            apiUrl = DEFAULT_API;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("framework", "springboot-gradle");
        payload.put("appName", (appName == null || appName.isBlank()) ? "(not set)" : appName);
        payload.put("appNameSet", appName != null && !appName.isBlank());
        payload.put("apiUrl", apiUrl);
        payload.put("apiUrlFromEnv", System.getenv("API_URL") != null);
        payload.put("remote", fetchRemote(apiUrl));
        return payload;
    }

    private Object fetchRemote(String apiUrl) {
        try {
            Map<?, ?> body = restTemplate.getForObject(apiUrl, Map.class);
            return body != null ? body : Map.of("error", "empty response");
        } catch (Exception exc) {
            return Map.of("error", exc.getMessage() != null ? exc.getMessage() : "fetch failed");
        }
    }

    private static String esc(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
