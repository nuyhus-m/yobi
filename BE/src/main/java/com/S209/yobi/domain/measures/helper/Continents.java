package com.S209.yobi.domain.measures.helper;

public class Continents {
    public static final String CONTINENT_NORTH_AMERICA = "NA";
    public static final String CONTINENT_SOUTH_AMERICA = "SA";
    public static final String CONTINENT_ASIA = "AS";
    public static final String CONTINENT_EUROPE = "EU";
    public static final String CONTINENT_OCEANIA = "OC";
    public static final String CONTINENT_AFRICA = "AF";
    public static final String CONTINENT_ANTARCTICA = "AN";

    public static String countryToContinent(String codeOrName) {
        if (codeOrName == null) return null;

        String input = codeOrName.toUpperCase();

        switch (input) {
            case "KR":
            case "KOREA":
            case "ASIA":
                return CONTINENT_ASIA;
            case "US":
            case "USA":
            case "CANADA":
            case "NORTH AMERICA":
            case "NA":
                return CONTINENT_NORTH_AMERICA;
            case "FR":
            case "DE":
            case "EUROPE":
            case "EU":
                return CONTINENT_EUROPE;
            case "AFRICA":
            case "AF":
                return CONTINENT_AFRICA;
            default:
                return null;
        }
    }
}