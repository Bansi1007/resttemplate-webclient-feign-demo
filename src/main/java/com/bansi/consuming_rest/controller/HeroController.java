package com.bansi.consuming_rest.controller;

import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import com.bansi.consuming_rest.service.HeroServiceClient;
import com.bansi.feign.client.HeroClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/client")
public class HeroController {
    //private static final Log log = LogFactory.getLog(HeroController.class);
    private final HeroServiceClient heroServiceClient;
    private final HeroClient heroClient;


    public HeroController(HeroServiceClient heroServiceClient, HeroClient heroClient) {
        this.heroServiceClient = heroServiceClient;
        this.heroClient = heroClient;
    }


    @PostMapping("/heroes")
    public void create(@RequestBody HeroRequest request) {
        log.debug("Creating Hero");
        heroServiceClient.createHero(request);
    }


    @GetMapping("/activeHeroes")
    public ResponseEntity<List<Hero>> getActiveHeroes() {
        List<Hero> heroes = heroServiceClient.getActiveHeroes();

        if (heroes.isEmpty()) {
            log.info("no active heroes found");
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.emptyList());
        }
        log.info("Fetched {} active heroes", heroes.size());
        return ResponseEntity.ok(heroes);
    }


    //    @GetMapping("/activeHeroes")
//    public List<Hero> getActiveHeroes(){
//        if (!heroServiceClient.getActiveHeroes().isEmpty()) {
//            return heroServiceClient.getActiveHeroes();
//        }
//        return null;
//    }
    @PutMapping("/heroes/{id}")
    public Hero updateHero(@PathVariable UUID id, @RequestBody HeroRequest heroRequest) {
        log.info("Updating hero id={} with request={}", id, heroRequest);
        Hero updated = heroServiceClient.updateHero(id, heroRequest);
        log.info("Hero id={} updated successfully", id);
        return updated;
    }

    @DeleteMapping("/heroes")
    public void deleteHeroes() {
        log.info("Deleting all heroes");
        heroServiceClient.deleteHeroes();
        log.info("All heroes deleted successfully");
    }

    @GetMapping("heroes/benched")
    public List<Hero> getBenchedHeroes() {
        List<Hero> inactiveheroes = heroServiceClient.getBenchedHeroes();
        log.info("Fetched {} benched heroes", inactiveheroes.size());
        return inactiveheroes;
    }
    //Patch with rest template - working
//    @PatchMapping("/heroes/{id}/toggle")
//    public Hero toggleHero(@PathVariable UUID id){
//        Hero hero = heroServiceClient.toggleHero(id);
//        return hero;
//    }

    //Patch with feign client
    @PatchMapping("hero/{id}/toggle")
    public Hero toggleHero(@PathVariable UUID id) {
        log.info("toggle hero id={}", id);
        Hero hero = heroClient.toggleHero(id, "");
        log.info("Hero id={} toggled successfully", hero.isActive());
        return hero;
    }

    @GetMapping("hero/{id}")
    public Hero getHeroById(@PathVariable UUID id) {
        log.info("get hero id={}", id);
        Hero hero = heroClient.getHeroById(id);
        log.info("Hero id={} found successfully", id);
        return hero;
    }

    @GetMapping("hero/by-name/{name}")
    public List<Hero> getHeroByName(@PathVariable String name) {
        log.info("get hero by name {}", name);
        List<Hero> heroByName = heroClient.getHeroByName(name);
        log.info("Hero name={} found successfully", name);
        return heroByName;
    }


    @DeleteMapping("heroes/{id}")
    public void deleteHeroById(@PathVariable UUID id) {
        log.info("delete hero id={}", id);
        heroClient.deleteHero(id);
        log.info("Hero id={} deleted successfully", id);
    }

    @GetMapping("heroes/strongest")
    public Hero getStrongestHeroes() {
        log.info("get strongest heroes");
        Hero strongestHero = heroClient.getStrongestHeroes();
        log.info("Hero id={} found successfully", strongestHero != null ? strongestHero.getId() : null);
        return strongestHero;
    }

    @GetMapping("heroes/weakest")
    public Hero getWeakestHeroes() {
        log.info("get weakest heroes");
        Hero hero = heroClient.getWeakestHero();
        log.info("Hero id={} found successfully", hero != null ? hero.getId() : null);
        return hero;
    }

    @GetMapping("heroes/sorted")
    public List<Hero> getSortedHeroes() {
        log.info("get sorted heroes");
        List<Hero> sortedHeroList = heroClient.getSortedHeroes();
        log.info("Feched {} sorted heroes successfully", sortedHeroList.size());
        return sortedHeroList;
    }

    @GetMapping("heroes/search")
    public List<Hero> getSearchHeroes(@RequestParam("power") String power) {
        log.info("Searching heroes by power={}", power);
        List<Hero> searchHeroes = heroClient.getSearchHeroes(power);
        log.info("Found {} heroes with power={}", searchHeroes.size(), power);
        return searchHeroes;
    }

    @GetMapping("heroes/level-range")
    public List<Hero> getLevelRangeHeroes(@RequestParam("min") int min, @RequestParam("max") int max) {
        log.info("get level range heroes by min={}, max={}", min, max);
        List<Hero> heroesInGivenRange = heroClient.getLevelRangeHeroes(min, max);
        log.info("Found {} heroes in level range [{}-{}]", heroesInGivenRange.size(), min, max);
        return heroesInGivenRange;
    }

    @GetMapping("/heroes/count")
    public Mono<String> getHeroesCount() {
        log.info("get heroes count");
        Mono<String> count = heroServiceClient.getHeroesCount();
        return count.doOnNext(count1 -> log.info("Heroes count={}", count));
    }

    @GetMapping("heroes/average-level")
    public double getAverageLevelHeroes() {
        log.info("get average level heroes");
        double avg = heroServiceClient.getAverageLevelHeroes();
        log.info("average level heroes={}", avg);
        return avg;
    }

    @PostMapping("heroes/bulk")
    public Mono<List<Hero>> bulkHeroes(@RequestBody List<HeroRequest> heroes) {
        log.info("bulk heroes={}", heroes);
        Mono<List<Hero>> bulklist = heroServiceClient.bulkHeroes(heroes);
        return bulklist.doOnNext(list -> log.info("Bulk creation completed, {} heroes created", list.size()));
    }

    @GetMapping("heroes/names")
    public Mono<List<String>> getHeroClient() {
        log.info("get heroes names");
        Mono<List<String>> namelist = heroServiceClient.getHeroesNames();
        return namelist.doOnNext(names -> log.info("Fetched {} hero names", names.size()));
    }

    @PutMapping("heroes/{id}/rename")
    public Mono<Hero> updateName(@PathVariable UUID id, @RequestParam String name) {
        log.info("update heroes name={}", name);
        Mono<Hero> updatedHero = heroServiceClient.updateName(id, name);
        return updatedHero.doOnNext(hero -> log.info("Hero rename={}", id, name));
    }

}
