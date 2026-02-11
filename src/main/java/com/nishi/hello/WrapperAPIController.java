package com.nishi.hello;

import org.springframework.http.*;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;
@RestController
@RequestMapping("/api/wrapper")
public class WrapperAPIController {

    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)) // 10MB in bytes
            .build();

    // CHANGE: Return types must be Mono<ResponseEntity<Object>>
    @GetMapping
    public Mono<ResponseEntity<Object>> proxyGet(@RequestBody ProxyRequest request) {
        return execute(request, HttpMethod.GET);
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> proxyPost(@RequestBody ProxyRequest request) {
        return execute(request, HttpMethod.POST);
    }

    @PatchMapping
    public Mono<ResponseEntity<Object>> proxyPatch(@RequestBody ProxyRequest request) {
        return execute(request, HttpMethod.PATCH);
    }

    @DeleteMapping
    public Mono<ResponseEntity<Object>> proxyDelete(@RequestBody ProxyRequest request) {
        return execute(request, HttpMethod.DELETE);
    }

    /**
     * Shared logic to execute the actual call
     */
    private Mono<ResponseEntity<Object>> execute(ProxyRequest request, HttpMethod method) {
        if (request.getUrl() == null || request.getUrl().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("URL field is missing"));
        }

        return webClient.method(method)
                .uri(request.getUrl())
                .headers(h -> {
                    if (request.getHeaders() != null) {
                        request.getHeaders().forEach(h::add);
                    }
                })
                .body(request.getBody() != null ? BodyInserters.fromValue(request.getBody()) : BodyInserters.empty())
                .retrieve()
                .toEntity(Object.class)
                .timeout(Duration.ofMillis(request.getTimeout()))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                                .body("Target API Error: " + e.getMessage())
                ));
    }
}