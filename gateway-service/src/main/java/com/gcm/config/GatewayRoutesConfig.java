package com.gcm.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayRoutesConfig implements RouteDefinitionRepository {

    private List<RouteConfig> routes;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes)
                .map(this::createRouteDefinition);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return Mono.empty();
    }

    private RouteDefinition createRouteDefinition(RouteConfig route) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(route.getId());
        definition.setUri(URI.create("lb://" + route.getServiceId()));
        definition.setPredicates(List.of(createPathPredicate(route.getPath())));
        definition.setFilters(createRouteFilters(route));

        return definition;
    }

    private List<FilterDefinition> createRouteFilters(RouteConfig route) {
        List<FilterDefinition> filters = new ArrayList<>();

        if (route.getAddRequestHeaders() != null) {
            filters.addAll(createFilters(route.getAddRequestHeaders()));
        }
        filters.add(createCircuitBreakerFilter(route.getServiceId()));

        return filters;
    }

    private PredicateDefinition createPathPredicate(String path) {
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("pattern", path);

        return predicate;
    }

    private List<FilterDefinition> createFilters(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(this::createFilter)
                .collect(Collectors.toList());
    }

    private FilterDefinition createFilter(Map.Entry<String, String> entry) {
        FilterDefinition filter = new FilterDefinition();
        filter.setName("AddRequestHeader");
        filter.addArg("name", entry.getKey());
        filter.addArg("value", entry.getValue());

        return filter;
    }

    private FilterDefinition createCircuitBreakerFilter(String serviceId) {
        FilterDefinition filter = new FilterDefinition();
        filter.setName("CircuitBreaker");
        filter.addArg("name", serviceId);
        filter.addArg("fallbackUri", "forward:/fallback/" + serviceId);

        return filter;
    }

    @Data
    public static class RouteConfig {
        private String id;
        private String path;
        private String serviceId;
        private Map<String, String> addRequestHeaders;
    }
}