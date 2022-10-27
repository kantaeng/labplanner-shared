package org.ucb.c5.labplanner.inventory.model;

/**
 * Representation of a DNA sample, or a tube containing it
 * 
 * @author J. Christopher Anderson
 */
public class Sample {

    private final String label;   //What's written on the top of the tube
    private final String sidelabel;  //What's written on the side of the tube
    private final Concentration concentration;  //The amount or type of DNA present
    private final String construct;  //The name of the DNA matching the construction file
    private final Culture culture;  //For minipreps only, how many rounds of isolation
    private final String clone;   //Which isolate of several of the same construct

    public enum Culture {
        library,
        primary,
        secondary,
        tertiary
    }
    public enum Concentration {
        miniprep,   //A plasmid miniprep
        zymo,       //A purified DNA product
        uM100,      //Oligo concentration for stocks
        uM10,       //Oligo concentration for PCR
        uM266,      //Oligo concentration for sequencing
        dil20x,     //A diluted plasmid or other DNA
        gene        //A gene synthesis order
    }

    public Sample(String label, String sidelabel, Concentration concentration, String construct, Culture culture, String clone) {
        this.label = label;
        this.sidelabel = sidelabel;
        this.concentration = concentration;
        this.construct = construct;
        this.culture = culture;
        this.clone = clone;
    }

    public String getLabel() {
        return label;
    }

    public String getSidelabel() {
        return sidelabel;
    }

    public Concentration getConcentration() {
        return concentration;
    }

    public String getConstruct() {
        return construct;
    }

    public Culture getCulture() {
        return culture;
    }

    public String getClone() {
        return clone;
    }
}
