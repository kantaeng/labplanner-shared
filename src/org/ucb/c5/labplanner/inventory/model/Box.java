package org.ucb.c5.labplanner.inventory.model;

/**
 *
 * @author J. Christopher Anderson
 */
public class Box {
    private final String name;         //name of the box, ie lysis1, and of the file
    private final String description;  //a description of the contents of the box
    private final String location;     //ie which freezer
    private final Sample[][] samples;  //What's in each well, or null

    public Box(String name, String description, String location, Sample[][] samples) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.samples = samples;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Sample[][] getSamples() {
        return samples;
    }
}
