package org.ucb.c5.labplanner.labpacket.model;

import java.util.List;
import org.ucb.c5.utils.Pair;

/**
 * The ingredients for setting up a DNA modification reaction, such
 * as a PCR, digest, or the like
 * 
 * A mastermix is premixed reagents that are then dispensed to individual
 * reactions.  One of the Reagents is itself mastermix for use in the 
 * reaction field.
 * 
 * The mastermix may be null if none is needed
 * 
 * @author J. Christopher Anderson
 */
public class Recipe {
    private final List<Pair<Reagent, Double>>  mastermix;  //The reagent and volume in uL
    private final List<Pair<Reagent, Double>>  reaction;  //The reagent and volume in uL

    public Recipe(List<Pair<Reagent, Double>> mastermix, List<Pair<Reagent, Double>> reaction) {
        this.mastermix = mastermix;
        this.reaction = reaction;
    }

    public List<Pair<Reagent, Double>> getMastermix() {
        return mastermix;
    }

    public List<Pair<Reagent, Double>> getReaction() {
        return reaction;
    }
}
