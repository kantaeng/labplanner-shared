package org.ucb.c5.labplanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.ucb.c5.constructionfile.model.*;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.inventory.model.Location;
import org.ucb.c5.labplanner.inventory.model.Sample.Concentration;
import org.ucb.c5.labplanner.labpacket.model.Cleanup;
import org.ucb.c5.labplanner.labpacket.model.Gel;
import org.ucb.c5.labplanner.labpacket.model.LabPacket;
import org.ucb.c5.labplanner.labpacket.model.LabSheet;
import org.ucb.c5.labplanner.labpacket.model.Reagent;
import org.ucb.c5.labplanner.labpacket.model.Recipe;
import org.ucb.c5.utils.Pair;

/**
 *
 * @author Shahar Schwartz
 */
public class LabPacketFactory {

    public void initiate() throws Exception {
    }

    /**
     * Assumes all samples that are inputs or to-be-created are already in the
     * inventory, and the inventory is indexed
     *
     * @param experimentName
     * @param cfList
     * @param inventory
     * @return
     * @throws Exception
     */
    public LabPacket run(String experimentName, 
            List<ConstructionFile> cfList, Inventory inventory) throws Exception {
        
        //Index the steps by operation
        List<Step> pcrSteps = new ArrayList<>();
        List<Step> digestSteps = new ArrayList<>();
        List<Step> ligateSteps = new ArrayList<>();
        List<Step> assemblySteps = new ArrayList<>();
        List<Step> transformSteps = new ArrayList<>();

        for (ConstructionFile cf : cfList) {
            for (Step astep : cf.getSteps()) {
                switch (astep.getOperation()) {
                    case pcr:
                        pcrSteps.add(astep);
                        break;
                    case digest:
                        digestSteps.add(astep);
                        break;
                    case ligate:
                        ligateSteps.add(astep);
                        break;
                    case assemble:
                        assemblySteps.add(astep);
                        break;
                    case transform:
                        transformSteps.add(astep);
                        break;
                }
            }
        }

        //Create the LabSheets
        List<LabSheet> labsheets = new ArrayList<>();
        labsheets.addAll(handlePCR(pcrSteps, experimentName, inventory));
        labsheets.addAll(handleDigest(digestSteps, experimentName, inventory));
        labsheets.addAll(handleLigate(ligateSteps, experimentName, inventory));
        labsheets.addAll(handleAssemble(assemblySteps, experimentName, inventory));
        labsheets.addAll(handleTransform(transformSteps, experimentName, inventory));

        //Package it up and return
        LabPacket packet = new LabPacket(labsheets);
        return packet;
    }

    private List<LabSheet> handlePCR(List<Step> pcrSteps, String expName, Inventory inventory) throws Exception {
        List<LabSheet> sheets = new ArrayList<>();
        if (pcrSteps.isEmpty()) {
            return sheets;
        }

        /**
         * ***************
         * Create a PCR sheet
         */
        {
            String title = expName + ": PCR";
            String program = "PG3K55";  //TODO:  abstract this as its own Function (lookup on size)
            String protocol = "PrimeSTAR";
            String instrument = "Thermocycler 2A";

            //Create the recipe
            List<Pair<Reagent, Double>> mastermix = null; //TODO:  implement for 4+ samples
            List<Pair<Reagent, Double>> reaction = new ArrayList<>();
            reaction.add(new Pair(Reagent.ddH2O, 32.0));
            reaction.add(new Pair(Reagent.PrimeSTAR_GXL_Buffer_5x, 10.0));
            reaction.add(new Pair(Reagent.PrimeSTAR_dNTP_Mixture_2p5mM, 4.0));
            reaction.add(new Pair(Reagent.primer1, 1.0));
            reaction.add(new Pair(Reagent.primer2, 1.0));
            reaction.add(new Pair(Reagent.template, 1.0));
            reaction.add(new Pair(Reagent.PrimeSTAR_GXL_DNA_Polymerase, 1.0));

            Recipe recipe = new Recipe(mastermix, reaction);

            //Create the notes
            List<String> notes = new ArrayList<>();
            notes.add("Never let enzymes warm up!  Only take the enzyme cooler out of the freezer\n"
                    + "when you are actively using it, and only take the tubes out of it when actively\n"
                    + "dispensing. Hold the enzyme tube by the top of the tube while dispensing\n"
                    + "and do not place it in a rack.");

            //Pull out locations
            List<Location> sources = new ArrayList<>();
            List<Location> destinations = new ArrayList<>();
            for (Step astep : pcrSteps) {
                PCR pcr = (PCR) astep;

                //Pull out sources for oligos and templates
                {
                    String forOligo = pcr.getOligo1();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(forOligo);
                    inner:
                    for (Location loc : forLocs) {
                        Concentration conc = inventory.getConcentration(loc);
                        if (conc == Concentration.uM10) {
                            chosenLoc = loc;
                            break;
                        }
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + forOligo);
                    }
                    sources.add(chosenLoc);
                }

                {
                    String revOligo = pcr.getOligo2();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(revOligo);
                    inner:
                    for (Location loc : forLocs) {
                        Concentration conc = inventory.getConcentration(loc);
                        if (conc == Concentration.uM10) {
                            chosenLoc = loc;
                            break;
                        }
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + revOligo);
                    }
                    sources.add(chosenLoc);
                }

                {
                    List<String> templates = pcr.getTemplates();
                    for (String template : templates) {
                        Location chosenLoc = null;
                        Set<Location> forLocs = inventory.getLocations(template);
                        inner:
                        for (Location loc : forLocs) {
                            Concentration conc = inventory.getConcentration(loc);
                            if (conc == Concentration.dil20x) {
                                chosenLoc = loc;
                                break;
                            }
                        }

                        if (chosenLoc == null) {
                            throw new Exception("Null location for " + template);
                        }
                        sources.add(chosenLoc);
                    }
                }

            } //end steps

            //Package the PCR sheet
            LabSheet sheet = new LabSheet(title, pcrSteps, sources, destinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }

        /**
         * ***************
         * Create a Gel sheet
         */
        List<Location> zymoDestinations = new ArrayList<>();
        {
            String title = expName + ": Gel";
            String program = null;
            String protocol = null;
            String instrument = null;
            Recipe recipe = null;
            List<String> notes = new ArrayList<>();

            //Pull out locations          
            for (Step astep : pcrSteps) {
                PCR pcr = (PCR) astep;

                //Pull out the PCR product zymo locations
                {
                    String pdtName = pcr.getProduct();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(pdtName);
                    inner:
                    for (Location loc : forLocs) {  //This will retrieve the zymo cleanup samples
                        Concentration conc = inventory.getConcentration(loc);
                        if (conc == Concentration.zymo) {
                            chosenLoc = loc;
                            break;
                        }
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + pdtName);
                    }
                    zymoDestinations.add(chosenLoc);
                }
            }

            //Create the steps
            List<Step> gelSteps = new ArrayList<>();
            for (Location loc : zymoDestinations) {
                String label = loc.getLabel();
                int size = 3800;  //TODO:  replace with lookup based on sequence length from CF
                Gel gs = new Gel(label, size);
                gelSteps.add(gs);
            }

            //Package the Gel sheet
            LabSheet sheet = new LabSheet(title, gelSteps, new ArrayList<>(), new ArrayList<>(), program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }

        /**
         * **************************
         * Create a zymo Cleanup sheet
         */
        {
            String title = expName + ": Cleanup";
            String program = null;
            String protocol = null;
            String instrument = null;
            Recipe recipe = null;
            List<String> notes = new ArrayList<>();

            //Create the steps
            List<Step> zymoSteps = new ArrayList<>();
            for (Location loc : zymoDestinations) {
                String label = loc.getLabel();
                double volume = 25.0;
                Cleanup cu = new Cleanup(label, volume);
                zymoSteps.add(cu);
            }

            //Package the Gel sheet
            LabSheet sheet = new LabSheet(title, zymoSteps, new ArrayList<>(), zymoDestinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
        return sheets;
    }

    private List<LabSheet> handleDigest(List<Step> digestSteps, String expName, Inventory inventory) {
        List<LabSheet> sheets = new ArrayList<>();
        if (digestSteps.isEmpty()) {
            return sheets;
        }

        String title = expName + ": Digest";
        List<Location> sources = new ArrayList<>();
        List<Location> destinations = new ArrayList<>();
        String program = "";
        String protocol = "";
        String instrument = "";
        List<String> notes = new ArrayList<>();
        List<Pair<Reagent, Double>> mastermix = null; //TODO:  implement for 4+ samples
        List<Pair<Reagent, Double>> reaction = new ArrayList<>();
        reaction.add(new Pair(Reagent.ddH2O, 39.0 - X.size())); //TODO figure out how to add and isolate enzymes: what is Step's relationship with this function?
        reaction.add(new Pair(Reagent.PrimeSTAR_GXL_Buffer_5x, 10.0)); //TODO: replace with correct buffer
        reaction.add(new Pair(Reagent.template, 1.0));


        Recipe recipe = new Recipe(mastermix, reaction);



        //Package the Digestion sheet
        //Create a zymo sheet
        return sheets;
    }

    private List<LabSheet> handleLigate(List<Step> ligateSteps, String expName, Inventory inventory) {
        List<LabSheet> sheets = new ArrayList<>();

        String title = expName + ": Ligation";
        List<Location> sources = new ArrayList<>();
        List<Location> destinations = new ArrayList<>();
        String program = "";
        String protocol = "";
        String instrument;
        List<String> notes;
        Recipe reaction;

        //Create a ligation sheet
        //TODO:  create a ligation labsheet and add to sheets
        return sheets;
    }

    private List<LabSheet> handleAssemble(List<Step> assemblySteps, String expName, Inventory inventory) throws Exception {
        List<LabSheet> sheets = new ArrayList<>();

        if (assemblySteps.isEmpty()) {
            return sheets;
        }

        /**
         * ***************
         * Create an assembly sheet
         */
        {
            //Create the header data
            String title = expName + ": Assembly";

            String program = ""; //TODO: Detect if GG or Gibson
            String protocol = ""; //TODO: Detect if GG or Gibson
            String instrument = "Thermocycler 2A";

            //Create the recipe
            //TODO: Detect if GG or Gibson
            List<Pair<Reagent, Double>> mastermix = null; //TODO:  implement for 4+ samples
            List<Pair<Reagent, Double>> reaction = new ArrayList<>();
            Recipe recipe = new Recipe(mastermix, reaction);

            //Create the notes
            List<String> notes = new ArrayList<>();
            notes.add("Never let enzymes warm up!  Only take the enzyme cooler out of the freezer\n"
                    + "when you are actively using it, and only take the tubes out of it when actively\n"
                    + "dispensing. Hold the enzyme tube by the top of the tube while dispensing\n"
                    + "and do not place it in a rack.");

            //Pull out locations
            List<Location> sources = new ArrayList<>();
            List<Location> destinations = new ArrayList<>();
            for (Step astep : assemblySteps) {
                Assembly assem = (Assembly) astep;

                //Pull out sources for fragments
                {
                    List<String> frags = assem.getFragments();
                    for (String frag : frags) {
                        Location chosenLoc = null;
                        Set<Location> forLocs = inventory.getLocations(frag);
                        inner:
                        for (Location loc : forLocs) {
                            Concentration conc = inventory.getConcentration(loc);
                            if (conc == Concentration.zymo) {
                                chosenLoc = loc;
                                break;
                            }
                        }

                        if (chosenLoc == null) {
                            throw new Exception("Null location for " + frag);
                        }
                        sources.add(chosenLoc);
                    }
                }
            } //end steps

            //Package the PCR sheet
            LabSheet sheet = new LabSheet(title, assemblySteps, sources, destinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
        return sheets;
    }

    private List<LabSheet> handleTransform(List<Step> transformSteps, String expName, Inventory inventory) {
        List<LabSheet> sheets = new ArrayList<>();
        //Create a Transform sheet
        //TODO:  create a Transform labsheet and add to sheets

        //Create a Pick sheet
        //TODO:  create a picking/inoculation labsheet and add to sheets
        //Create a Miniprep sheet
        //TODO:  create a miniprep labsheet and add to sheets
        return sheets;
    }

    public static void main(String[] args) {

    }

}
