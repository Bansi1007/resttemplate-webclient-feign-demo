package com.bansi.consuming_rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeroRequest {
    String name;
    int level;
    String power;
    @JsonProperty
    boolean isActive;
    public HeroRequest(String name, int level, String power, boolean isActive) {
        this.name = name;
        this.level = level;
        this.power = power;
        this.isActive = isActive;
    }
}
