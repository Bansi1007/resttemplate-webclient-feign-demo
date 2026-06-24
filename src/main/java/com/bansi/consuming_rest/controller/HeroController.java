package com.bansi.consuming_rest.controller;

import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import com.bansi.consuming_rest.service.HeroServiceClient;
import com.bansi.feign.client.HeroClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client")
public class HeroController {
    private final HeroServiceClient heroServiceClient;
    private final HeroClient heroClient;

    public HeroController(HeroServiceClient heroServiceClient, HeroClient heroClient) {
        this.heroServiceClient = heroServiceClient;
        this.heroClient = heroClient;
    }

    @PostMapping("/heroes")
    public void create(@RequestBody HeroRequest request) {
        heroServiceClient.createHero(request);
    }

    @GetMapping("/activeHeroes")
    public List<Hero> getActiveHeroes() {
        if (!heroServiceClient.getActiveHeroes().isEmpty()) {
            return heroServiceClient.getActiveHeroes();
        }
        return null;
    }
    @PutMapping("/heroes/{id}")
    public Hero updateHero(@PathVariable UUID id, @RequestBody HeroRequest heroRequest) {
        return heroServiceClient.updateHero(id, heroRequest);
    }

    @DeleteMapping("/heroes")
    public void deleteHeroes() {
        heroServiceClient.deleteHeroes();
    }

    @GetMapping("heroes/benched")
    public List<Hero> getBenchedHeroes() {
        List<Hero> inactiveheroes = heroServiceClient.getBenchedHeroes();
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
    public Hero toggleHero(@PathVariable UUID id){
        Hero hero = heroClient.toggleHero(id,"");
            return hero;
    }

    @GetMapping("hero/{id}")
    public Hero getHeroById(@PathVariable UUID id) {
        Hero hero = heroClient.getHeroById(id);
        return hero;
    }

    @GetMapping("hero/by-name/{name}")
    public List<Hero> getHeroByName(@PathVariable String name) {
        List<Hero> heroByName = heroClient.getHeroByName(name);
        return heroByName;
    }


    @DeleteMapping("heroes/{id}")
    public void deleteHeroById(@PathVariable UUID id) {
        heroClient.deleteHero(id);
    }

    @GetMapping("heroes/strongest")
    public Hero getStrongestHeroes() {
        Hero strongestHero = heroClient.getStrongestHeroes();
        return strongestHero;
    }

    @GetMapping("heroes/weakest")
    public Hero getWeakestHeroes() {
        Hero hero = heroClient.getWeakestHero();
        return hero;
    }

    @GetMapping("heroes/sorted")
    public List<Hero> getSortedHeroes() {
        List<Hero> sortedHeroList = heroClient.getSortedHeroes();
        return sortedHeroList;
    }

    @GetMapping("heroes/search")
    public List<Hero> getSearchHeroes(@RequestParam("power") String power) {
        List<Hero> searchHeroes = heroClient.getSearchHeroes(power);
        return searchHeroes;
    }

    @GetMapping("heroes/level-range")
    public List<Hero> getLevelRangeHeroes(@RequestParam("min") int min, @RequestParam("max") int max) {
        List<Hero> heroesInGivenRange = heroClient.getLevelRangeHeroes(min, max);
        return heroesInGivenRange;
    }

    @GetMapping("/heroes/count")
    public Mono<String> getHeroesCount() {
        Mono<String> count = heroServiceClient.getHeroesCount();
        return count;
    }

    @GetMapping("heroes/average-level")
    public double getAverageLevelHeroes(){
        double avg = heroServiceClient.getAverageLevelHeroes();
        return avg;
    }

    @PostMapping("heroes/bulk")
    public Mono<List<Hero>> bulkHeroes(@RequestBody List<HeroRequest> heroes) {
        Mono<List<Hero>> bulklist = heroServiceClient.bulkHeroes(heroes);
        return bulklist;
    }

    @GetMapping("heroes/names")
    public Mono<List<String>> getHeroClient() {
        Mono<List<String>> namelist = heroServiceClient.getHeroesNames();
        return namelist;
    }

    @PutMapping("heroes/{id}/rename")
    public Mono<Hero> updateName(@PathVariable UUID id, @RequestParam String name) {
        Mono<Hero>updatedHero = heroServiceClient.updateName(id,name);
        return updatedHero;
    }

}
