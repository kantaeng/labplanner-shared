package org.ucb.c5.labplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.constructionfile.model.Digestion;
import org.ucb.c5.constructionfile.model.PCR;
import org.ucb.c5.constructionfile.model.Step;
import org.ucb.c5.constructionfile.model.Transformation;
import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.inventory.model.Location;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.labplanner.inventory.model.Sample.Culture;
import org.ucb.c5.utils.Pair;

/**
 * Inputs a construction file and assigns tube locations for all samples
 *
 * It currently assumes all samples will fit into a single 9x9 slot box
 * 
 * The preexisting inventory (variable oldInventory) may be null. In such a case
 * it is assumed that there are no existing samples or boxes, and an entirely 
 * new inventory is created.
 * 
 * If oldInventory is populated, existing samples discussed in the experiment are
 * identified and new samples are not generated.  Otherwise, new samples are
 * generated and added to a new box.  The final inventory contains all the boxes
 * and samples of the original inventory with the additional samples generated
 * anew in the Experiment.
 *
 * @author J. Christopher Anderson
 */
public class InventoryFactory {

    private final int BOX_SIZE = 9;  //Assume 9x9 boxes
    private final int NUM_MINIPREPS = 4;  //Assume 4 minipreps are generated

    public void initiate() throws Exception {
    }

    /**
     * @param experimentName The name of the experiment, e.g. "lysis1" or "lycopene36"
     * @param experimentId  An externally assigned integer code for the experiment, ie 33
     * @param cfList  The list of construction files, assumed to have been simulated
     * @param oldInventory  The state of the inventory prior to run
     * @return  A modified copy of the inventory with new samples assigned during run
     * @throws Exception 
     */
    public Inventory run(String experimentName, int experimentId,
            List<ConstructionFile> cfList, Inventory oldInventory) throws Exception {
        
        //Initialize the box assignments
        Sample[][] samples = new Sample[BOX_SIZE][BOX_SIZE];
        Pair<Integer, Integer> currloc = new Pair<>(0, 0); //row, col

        //Create samples for ever sample-generating step in every construction file
        for (ConstructionFile cf : cfList) {
            for (Step astep : cf.getSteps()) {
                switch (astep.getOperation()) {
                    case pcr:
                        currloc = assignPCRSamples((PCR) astep, experimentId, samples, currloc);
                        break;
                    case digest:
                        currloc = assignDigestedSamples((Digestion) astep, experimentId, samples, currloc);
                        break;
                    case transform:
                        currloc = assignMiniprepSamples((Transformation) astep, experimentId, samples, currloc);
                        break;
                }
            }
        }

        Box abox = new Box(experimentName, "Materials for " + experimentName, "minus20", samples);
        List<Box> boxes = new ArrayList<Box>();
        boxes.add(abox);

        //Index the samples by Construct name
        Map<String, Set<Location>> constructToLocations = new HashMap<>();
        for (int row = 0; row < abox.getSamples().length; row++) {
            for (int col = 0; col < abox.getSamples()[row].length; col++) {
                Sample asample = abox.getSamples()[row][col];
                if(asample == null) {
                    continue;
                }
                Set<Location> locations = constructToLocations.get(asample.getConstruct());
                if (locations == null) {
                    locations = new HashSet<>();
                }
                locations.add(new Location(abox.getName(), row, col, asample.getLabel(), asample.getSidelabel()));
                constructToLocations.put(asample.getConstruct(), locations);
            }
        }

        //Index the data by location
        Map<Location, Sample.Concentration> locToConc = new HashMap<>();
        Map<Location, String> locToClone = new HashMap<>();
        Map<Location, Culture> locToCulture = new HashMap<>();
        
        for(String construct : constructToLocations.keySet()) {
            Set<Location> locations = constructToLocations.get(construct);
            for(Location loc : locations) {
                Sample sam = samples[loc.getRow()][loc.getCol()];
                locToConc.put(loc, sam.getConcentration());
                locToClone.put(loc, sam.getClone());
                locToCulture.put(loc, sam.getCulture());
            }
        }

        //Package up and return
        Inventory inventory = new Inventory(boxes, constructToLocations, locToConc, locToClone, locToCulture);
        return inventory;
    }

    private Pair<Integer, Integer> assignPCRSamples(PCR pcr, int experimentId, Sample[][] samples, Pair<Integer, Integer> currloc) throws Exception {
        //Create Samples for each template
        for (String tempName : pcr.getTemplates()) {
            Sample template = new Sample(tempName + " dil",
                    tempName + " dil",
                    Sample.Concentration.dil20x,
                    tempName,
                    null,
                    null);
            samples[currloc.getKey()][currloc.getValue()] = template;
            currloc = getNextLocation(currloc);
        }

        //Create Samples for the two 100 uM stocks of the oligos
        Sample for100 = new Sample(pcr.getOligo1(),
                "",
                Sample.Concentration.uM100,
                pcr.getOligo1(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = for100;
        currloc = getNextLocation(currloc);

        Sample rev100 = new Sample(pcr.getOligo2(),
                "",
                Sample.Concentration.uM100,
                pcr.getOligo2(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = rev100;
        currloc = getNextLocation(currloc);

        //Create Samples for the 10 uM stocks of the oligos
        Sample for10 = new Sample("10 uM " + pcr.getOligo1(),
                "10 uM " + pcr.getOligo1(),
                Sample.Concentration.uM10,
                pcr.getOligo1(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = for10;
        currloc = getNextLocation(currloc);

        Sample rev10 = new Sample("10 uM " + pcr.getOligo2(),
                "10 uM " + pcr.getOligo2(),
                Sample.Concentration.uM10,
                pcr.getOligo2(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = rev10;
        currloc = getNextLocation(currloc);

        //Create Sample for the zymo cleanup of the PCR product
        Sample zymo = new Sample("z" + experimentId,
                "z" + experimentId + " - " + pcr.getProduct(),
                Sample.Concentration.zymo,
                pcr.getProduct(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = zymo;
        currloc = getNextLocation(currloc);

        return currloc;
    }

    private Pair<Integer, Integer> assignDigestedSamples(Digestion digestion, int experimentId, Sample[][] samples, Pair<Integer, Integer> currloc) throws Exception {
        //Create Sample for the zymo cleanup of the digested product
        Sample zymo = new Sample("d" + + experimentId,
                "d" + experimentId + " - " + digestion.getProduct(),
                Sample.Concentration.zymo,
                digestion.getProduct(),
                null,
                null);
        samples[currloc.getKey()][currloc.getValue()] = zymo;
        currloc = getNextLocation(currloc);

        return currloc;
    }

    private Pair<Integer, Integer> assignMiniprepSamples(Transformation transformation, int experimentId, Sample[][] samples, Pair<Integer, Integer> currloc) throws Exception {
        //Assign positions for minipreps of clones
        for (int i = 0; i < NUM_MINIPREPS; i++) {
            //Create the clone letter
            int irow = 65 + i;
            String clone =  experimentId + ((char) irow) + "";

            //Create and assign the miniprep sample
            Sample mini = new Sample(transformation.getProduct() + " " + clone,
                    transformation.getProduct() + " " + clone,
                    Sample.Concentration.miniprep,
                    transformation.getProduct(),
                    Culture.primary,
                    clone);
            samples[currloc.getKey()][currloc.getValue()] = mini;
            currloc = getNextLocation(currloc);
        }

        return currloc;
    }

    /**
     * Assigns the next available position moving left to right (col) then top
     * to bottom (row)
     *
     * @param currloc
     * @return
     */
    private Pair<Integer, Integer> getNextLocation(Pair<Integer, Integer> currloc) throws Exception {
        int row = currloc.getKey();
        int col = currloc.getValue();

        //If it's reached the end of the row, increment to next row
        if (col == BOX_SIZE - 1) {
            row++;
            col = 0;
        } else {
            col++;
        }

        if (row >= BOX_SIZE || col >= BOX_SIZE) {
            throw new Exception("Box is already full");
        }

        return new Pair(row, col);
    }

    public static void main(String[] args) throws Exception {
        InventoryFactory ow = new InventoryFactory();
        ow.initiate();
    }

}
