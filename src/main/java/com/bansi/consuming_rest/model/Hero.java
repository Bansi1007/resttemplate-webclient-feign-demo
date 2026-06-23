package com.bansi.consuming_rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Hero {
    UUID id;
    String name;
    int level;
    String power;
    @JsonProperty
    boolean isActive;
    public Hero(String name, int level, String power, Boolean isActive) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.level = level;
        this.power = power;
        this.isActive = isActive;
    }
    public Hero(HeroRequest heroRequest) {
        this.id = UUID.randomUUID();
        this.name = heroRequest.getName();
        this.level = heroRequest.getLevel();
        this.power = heroRequest.getPower();
        this.isActive = heroRequest.isActive();
    }
}
