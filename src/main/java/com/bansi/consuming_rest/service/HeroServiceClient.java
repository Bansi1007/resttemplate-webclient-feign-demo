package com.bansi.consuming_rest.service;

import com.bansi.consuming_rest.auth.SuperheroTokenProvider;
import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import com.bansi.consuming_rest.util.AppConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

@Service
public class HeroServiceClient {
    @Value("${superhero.api.base-url}")
    private String baseUrl;

    @Autowired
    private SuperheroTokenProvider tokenProvider;


    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private int attempt = 0;


    public HeroServiceClient(RestTemplate restTemplate, WebClient webClient, RetryRegistry retryRegistry) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.retryRegistry = retryRegistry;
    }

    private static final Logger log = LoggerFactory.getLogger(HeroServiceClient.class);

    //1
    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "createFallback")
    public void createHero(HeroRequest request) {
        log.info("Creating hero with request={}", request);
        String url = baseUrl + AppConstants.HEROES;
        restTemplate.postForObject(url, request, HeroRequest.class);
        log.info("Created hero successfully");
    }

    private void createFallback(HeroRequest request, Throwable t) {
        log.error("createHero unavailable for request={}: {}", request, t.getMessage(), t);
    }


    @Retry(name = "HeroServiceClient", fallbackMethod = "fallback")
    @CircuitBreaker(name = "HeroServiceClient")
    public List<Hero> getActiveHeroes() {
        attempt++;
        log.info("Fetching active heroes,attempt = {}", attempt);
        Instant now = Instant.now();
        String url = baseUrl + AppConstants.ACTIVE_HEROES;
        List<Hero> heroes = restTemplate.getForObject(url, List.class);
        log.info("Fetched active heroes on attempt = {}", heroes != null ? heroes.size() : 0, attempt);
        attempt = 0;
        return heroes;
    }

    public List<Hero> fallback(Throwable ex) {
        log.error("3 retries failed. Sending service unavailable " +
                "to consumer:{} Please don't send any more requests", ex.getMessage(), ex);
        attempt = 0;
        return Collections.emptyList();
    }


    // For loop
//    public List<Hero> getActiveHeroes() {
//        for (int i = 1; i <= 3; i++) {
//            attempt++;
//            try {
//                String url = baseUrl + "/heroes/active";
//                List<Hero> activeHeroList = restTemplate.getForObject(url, List.class);
//                if (activeHeroList != null) {
//                    return activeHeroList;
//                }
//                return Collections.emptyList();
//            } catch (Exception e) {
//                System.out.println("Retry " + attempt + ": failed");
//
//                //https://stackoverflow.com/questions/9587673/thread-sleep-vs-timeunit-seconds-sleep
//                if (attempt <= 3) {
//                    try {
//                        TimeUnit.SECONDS.sleep(10);
//                    } catch (InterruptedException ie) {
//                        System.out.println(ie.getMessage());
//                    }
//                }
//            }
//        }
//
//        System.out.println("3 retries failed. Sending service unavailable to consumer. Please dont send any more requests");
//
//        return Collections.emptyList();
//    }


    // 2
//    public List<Hero> getActiveHeroes() {
//        String url = baseUrl + "/heroes/active";
//            List<Hero> heroes = restTemplate.getForObject(url, List.class);
//            return heroes;
//    }


    //3

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "updateFallback")
    public Hero updateHero(UUID id, HeroRequest heroRequest) {
        log.info("Updating hero id={} with request={}", id, heroRequest);
        String url = baseUrl + AppConstants.HEROES + id;
        Hero hero = new Hero(heroRequest);
        ResponseEntity<Hero> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(hero),
                Hero.class
        );
        log.info("Updated hero id={}", id);
        return response.getBody();
    }

    private Hero updateFallback(UUID id, HeroRequest heroRequest, Throwable t) {
        log.error("updated hero unavailable for id = {}", id, t.getMessage(), t);
        Hero hero = new Hero();
        hero.setId(id);
        return hero;
    }

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "deleteFallback")
    public void deleteHeroes() {
        log.info("Deleting all heroes");
        String url = baseUrl + AppConstants.HEROES;
        restTemplate.delete(url);
        log.info("Deleted all heroes successfully");
    }

    private void deleteFallback(Throwable t) {
        log.error("delete hero unavailable for request={}", t.getMessage(), t);
    }


    // @RateLimiter(name = "HeroServiceClient",fallbackMethod = "rateLimitFallback")
    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "benchedFallback")
    public List<Hero> getBenchedHeroes() {
        log.info("Fetching benched heroes");
        try {
            return callSuperhero();
        } catch (HttpClientErrorException e) {
            log.warn("Token likely expired (status={}), forcing refresh and retrying", e.getStatusCode());
            tokenProvider.forceRefresh();
            return callSuperhero();
        }
    }

    private List<Hero> benchedFallback(Throwable t) {
        log.error("benched hero unavailable for request={}", t.getMessage(), t);
        return Collections.emptyList();
    }


    private List<Hero> callSuperhero() {
        String url = baseUrl + AppConstants.BENCHED_HEROES;
        log.debug("RestTemplate interceptors attached: {}", restTemplate.getInterceptors().size());
        Hero[] heroes = restTemplate.getForObject(url, Hero[].class);
        log.info("Fetched {} benched heroes", heroes != null ? heroes.length : 0);
        return Arrays.asList(heroes);
    }


    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "toggleFallback")
    public Hero toggleHero(UUID id) {
        log.info("Toggling hero id={}", id);
        String url = baseUrl + AppConstants.HEROES +"/"+ id + AppConstants.TOGGLE_SUFFIX;
        Hero hero = restTemplate.patchForObject(url, "", Hero.class);
        log.info("Hero id={} toggled successfully", id);
        return hero;
    }

    private Hero toggleFallback(UUID id, Throwable t) {
        log.error("toggleHero unavailable for id={}: {}", id, t.getMessage(), t);
        Hero hero = new Hero();
        hero.setId(id);
        return hero;
    }

    //web client
    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "countFallback")
    public Mono<String> getHeroesCount() {
        log.info("Fetching heroes count");
        return webClient.get()
                .uri(AppConstants.HEROES_COUNT)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(count -> log.info("Heroes count={}", count))
                .doOnError(e -> log.error("Error fetching heroes count: {}", e.getMessage(), e));
    }


    private Mono<String> countFallback(Throwable t) {
        log.error("getHeroesCount unavailable: {}", t.getMessage(), t);
        return Mono.just("unavailable");
    }

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "avgFallback")
    public double getAverageLevelHeroes() {
        log.info("Fetching average hero level");
        return webClient.get()
                .uri(AppConstants.AVERAGE_LEVEL)
                .retrieve()
                .bodyToMono(Double.class)
                .block();
    }

    private double avgFallback(Throwable t) {
        log.error("getAverageLevelHeroes unavailable, returning 0.0: {}", t.getMessage(), t);
        return 0.0;
    }

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "bulkFallback")
    public Mono<List<Hero>> bulkHeroes(@RequestBody List<HeroRequest> heroes) {
        log.info("Bulk creating {} heroes", heroes.size());
        return webClient.post()
                .uri(AppConstants.BULK)
                .bodyValue(heroes)
                .retrieve()
                .bodyToFlux(Hero.class)
                .collectList()
                .doOnNext(list -> log.info("Bulk creation completed, {} heroes created", list.size()))
                .doOnError(e -> log.error("Error during bulk hero creation: {}", e.getMessage(), e));
    }

    private Mono<List<Hero>> bulkFallback(List<HeroRequest> heroes, Throwable t) {
        log.error("bulkHeroes unavailable for {} requested heroes: {}", heroes.size(), t.getMessage(), t);
        return Mono.just(List.of());
    }

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "namesFallback")
    public Mono<List<String>> getHeroesNames() {
        log.info("Fetching hero names");
        return webClient.get()
                .uri(AppConstants.NAMES)
                .retrieve()
                .bodyToMono(String[].class)
                .map(Arrays::asList)
                .doOnNext(names -> log.info("Fetched {} hero names", names.size()))
                .doOnError(e -> log.error("Error fetching hero names: {}", e.getMessage(), e));
    }

    private Mono<List<String>> namesFallback(Throwable t) {
        log.error("getHeroesNames unavailable: {}", t.getMessage(), t);
        return Mono.just(List.of());
    }

    @CircuitBreaker(name = "HeroServiceClient", fallbackMethod = "renameFallback")
    public Mono<Hero> updateName(UUID id, String name) {
        log.info("Renaming hero id={} to name={}", id, name);
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(AppConstants.RENAME)
                        .queryParam(AppConstants.NAME_PARAM, name)
                        .build(id)) // Binds 'id' to the path variable
                .retrieve()
                .bodyToMono(Hero.class)
                .doOnNext(hero -> log.info("Hero id={} renamed to {}", id, name))
                .doOnError(e -> log.error("Error renaming hero id={}: {}", id, e.getMessage(), e));
    }

    private Mono<Hero> renameFallback(UUID id, String name, Throwable t) {
        log.error("updateName unavailable for id={}: {}", id, t.getMessage(), t);
        return Mono.empty();
    }


}
