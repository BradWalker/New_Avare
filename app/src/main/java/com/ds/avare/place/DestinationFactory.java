package com.ds.avare.place;

/**
 * Created by zkhan on 2/9/17.
 */

public class DestinationFactory {
    public static Destination build(String name, String type) {
        return switch (type) {
            case Destination.GPS -> new GpsDestination(name);
            case Destination.MAPS -> new MapsDestination(name);
            case Destination.UDW -> new UDWDestination(name);
            default -> new DatabaseDestination(name, type);
        };
    }
}
