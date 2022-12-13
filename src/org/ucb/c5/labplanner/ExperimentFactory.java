package org.ucb.c5.labplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.constructionfile.model.Oligo;
import org.ucb.c5.constructionfile.model.Polynucleotide;
import org.ucb.c5.labplanner.OligoListFactory;
import org.ucb.c5.labplanner.LabPacketFactory;
import org.ucb.c5.labplanner.InventoryFactory;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.labpacket.model.LabPacket;
import org.ucb.c5.labplanner.model.Experiment;
import org.ucb.c5.utils.Pair;

/**
 * Highest-level Function for designing a single CRISPR experiment
 *
 * @author J. Christopher Anderson
 * @author David Blayvas
 */
public class ExperimentFactory {

    private final OligoListFactory oligolistFactory = new OligoListFactory();
    private final InventoryFactory inventoryFactory = new InventoryFactory();
    private final LabPacketFactory labPacketFactory = new LabPacketFactory();

    public void initiate() throws Exception {
        oligolistFactory.initiate();
        inventoryFactory.initiate();
        labPacketFactory.initiate();
    }

    public Experiment run(String experimentName, int experimentId, 
            List<ConstructionFile> cfList, Inventory oldInventory) throws Exception {
        //Extract the Oligos
        List<Oligo> oligoList = oligolistFactory.run(cfList);

        //Create the Inventory
        Inventory inventory = inventoryFactory.run(experimentName, experimentId, cfList, oldInventory);

        //Create the LabPacket
        LabPacket packet = labPacketFactory.run(experimentName, cfList, inventory);

        //Package up and return the Experiment object
        Map<String, Polynucleotide> sequences = new HashMap<>();
        
        for (ConstructionFile cf : cfList) {
            sequences.putAll(cf.getSequences());
        }

        Experiment exp = new Experiment(experimentName, cfList, oligoList, sequences, packet, inventory);
        return exp;
    }
}
