package com.bansi.consuming_rest.util;

public class AppConstants {
    private AppConstants() {}

    // RestTemplate paths
    public static final String HEROES = "/heroes";
    public static final String ACTIVE_HEROES = "/heroes/active";
    public static final String BENCHED_HEROES = "/heroes/benched";
    public static final String TOGGLE_SUFFIX = "/toggle";

    // WebClient paths
    public static final String HEROES_COUNT = "/heroes/count";
    public static final String AVERAGE_LEVEL = "/heroes/average-level";
    public static final String BULK = "/heroes/bulk";
    public static final String NAMES = "/heroes/names";
    public static final String RENAME = "/heroes/{id}/rename";

    // Query param keys
    public static final String NAME_PARAM = "name";
}
