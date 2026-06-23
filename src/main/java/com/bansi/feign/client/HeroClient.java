package com.bansi.feign.client;

import com.bansi.consuming_rest.model.Hero;
import jakarta.websocket.server.PathParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hero-service", url = "http://localhost:8080")
public interface HeroClient {

    @GetMapping("/heroes/{id}")
    Hero getHeroById(@PathVariable UUID id);

    @GetMapping("/heroes/by-name/{name}")
    List<Hero> getHeroByName(@PathVariable String name);


//    @PatchMapping ("/heroes/{id}/toggle")
//    Hero toggleHero(@PathVariable("id") UUID id, @RequestBody String body);

    @DeleteMapping("/heroes/{id}")
    void deleteHero(@PathVariable UUID id);

    @GetMapping("heroes/strongest")
    Hero getStrongestHeroes();

    @GetMapping("heroes/weakest")
    Hero getWeakestHero();

    @GetMapping("heroes/sorted")
    List<Hero> getSortedHeroes();

    @GetMapping("heroes/search")
    List<Hero> getSearchHeroes(@RequestParam("power") String power);

    @GetMapping("heroes/level-range")
    List<Hero> getLevelRangeHeroes(@RequestParam("min") int min, @RequestParam("max") int max);
}
