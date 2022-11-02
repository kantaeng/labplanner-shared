package org.ucb.c5.labplanner.labpacket.model;

import java.util.List;
import org.ucb.c5.constructionfile.model.Step;
import org.ucb.c5.labplanner.inventory.model.Location;

/**
 * Generalized representation of an individual LabSheet.
 * Contains a superset of fields for describing specific PCR,
 * Digest, Zymo Cleanup, Assembly, Gel, Ligation, and Transformation
 * sheets.
 * 
 * @author J. Christopher Anderson
 */
public class LabSheet {
   private final String title;    //What is displayed at the top of the LabSheet
   private final List<Step> steps;  //ConstructionFile Steps executed on this sheet
   private final List<Location> sources; //Samples to retrieve from Boxes
   private final List<Location> destinations;  //Samples to add new to Boxes
   private final String program;  //Program to run on a thermocycler
   private final String protocol;  //Specific protocol to use, ie PrimeStar or Phusion
   private final String instrument; //Which instrument to put reactions/plates into
   private final List<String> notes;  //Any notes to display as alerts
   private final Recipe reaction;  //Ingredients for setting up the reaction

    public LabSheet(String title, List<Step> steps, List<Location> sources, List<Location> destinations, String program, String protocol, String instrument, List<String> notes, Recipe reaction) {
        this.title = title;
        this.steps = steps;
        this.sources = sources;
        this.destinations = destinations;
        this.program = program;
        this.protocol = protocol;
        this.instrument = instrument;
        this.notes = notes;
        this.reaction = reaction;
    }

    public LabSheet(boolean add) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getTitle() {
        return title;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<Location> getSources() {
        return sources;
    }

    public List<Location> getDestinations() {
        return destinations;
    }

    public String getProgram() {
        return program;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getInstrument() {
        return instrument;
    }

    public List<String> getNotes() {
        return notes;
    }

    public Recipe getReaction() {
        return reaction;
    }
}
