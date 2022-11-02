package org.ucb.c5.labplanner.inventory.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.c5.labplanner.inventory.model.Sample.Concentration;
import org.ucb.c5.labplanner.inventory.model.Sample.Culture;

/**
 * Representation of the complete set of boxes and samples.  Information
 * is doubly represented to facilitate both iteration and lookup.
 * 
 * @author J. Christopher Anderson
 */
public class Inventory {
    private final List<Box> boxes;  //all the boxes in the inventory

    private final Map<String, Set<Location>> constructToLocations;   //Quick lookup of samples by construct name
    private final Map<Location, Concentration> locToConc;   //Quick lookup by Concentration
    private final Map<Location, String> locToClone;   //Quick lookup by Clone
    private final Map<Location, Culture> locToCulture;   //Quick lookup by Culture

    public Inventory(List<Box> boxes, Map<String, Set<Location>> constructToLocations, Map<Location, Concentration> locToConc, Map<Location, String> locToClone, Map<Location, Culture> locToCulture) {
        this.boxes = boxes;
        this.constructToLocations = constructToLocations;
        this.locToConc = locToConc;
        this.locToClone = locToClone;
        this.locToCulture = locToCulture;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public Set<Location> getLocations(String constructName) {
        return constructToLocations.get(constructName);
    }
    
    public Concentration getConcentration(Location location) {
        return locToConc.get(location);
    }
    
    public String getClone(Location location) {
        return locToClone.get(location);
    }
    
    public Culture getCulture(Location location) {
        return locToCulture.get(location);
    }
}
