package org.ucb.c5.labplanner.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.constructionfile.model.Oligo;
import org.ucb.c5.constructionfile.model.Polynucleotide;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.labpacket.model.LabPacket;

/**
 *
 * @author J. Christopher Anderson
 */
public class Experiment {
    private final String name;
    private final List<ConstructionFile> cfs;
    private final List<Oligo> oligos;
    private final Map<String, Polynucleotide> nameToPoly;
    private final LabPacket labpacket;
    private final Inventory inventory;

    public Experiment(String name, List<ConstructionFile> cfs,List<Oligo> oligos, Map<String, Polynucleotide> nameToPoly, LabPacket labpacket, Inventory inventory) {
        this.name = name;
        this.cfs = cfs;
        this.oligos = oligos;
        this.nameToPoly = nameToPoly;
        this.labpacket = labpacket;
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public List<ConstructionFile> getCfs() {
        return cfs;
    }

    public List<Oligo> getOligos() {
        return oligos;
    }

    public Map<String, Polynucleotide> getNameToPoly() {
        return nameToPoly;
    }

    public LabPacket getLabpacket() {
        return labpacket;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}
