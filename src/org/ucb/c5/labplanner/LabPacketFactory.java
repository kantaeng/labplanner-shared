package org.ucb.c5.labplanner;

import java.util.*;

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

    private final Map<String, Reagent> enzymeMap = new HashMap<String, Reagent>();

    public void initiate() throws Exception {
        enzymeMap.put("Phusion", Reagent.Phusion);
        enzymeMap.put("Q5_polymerase", Reagent.Q5_polymerase);
        enzymeMap.put("PrimeSTAR_GXL_DNA_Polymerase", Reagent.PrimeSTAR_GXL_DNA_Polymerase);
        enzymeMap.put("DpnI", Reagent.DpnI);
        enzymeMap.put("BamHI", Reagent.BamHI);
        enzymeMap.put("BglII", Reagent.BglII);
        enzymeMap.put("BsaI", Reagent.BsaI);
        enzymeMap.put("BsmBI", Reagent.BsmBI);
        enzymeMap.put("T4_DNA_ligase", Reagent.T4_DNA_ligase);
        enzymeMap.put("SpeI", Reagent.SpeI);
        enzymeMap.put("XhoI", Reagent.XhoI);
        enzymeMap.put("XbaI", Reagent.XbaI);
        enzymeMap.put("PstI", Reagent.PstI);
        enzymeMap.put("Hindiii", Reagent.Hindiii);
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


    private List<LabSheet> handleDigest(List<Step> digestSteps, String expName, Inventory inventory) throws Exception {
        List<LabSheet> sheets = new ArrayList<>();

        if (digestSteps.isEmpty()) {
            return sheets;
        }

        String title = expName + ": Digest";
        String program = "";
        String protocol = "";
        String instrument = "";
        List<String> notes = new ArrayList<>();

        //Pull out locations
        List<Location> sources = new ArrayList<>();
        List<Location> destinations = new ArrayList<>();
        List<Pair<Reagent, Double>> reaction = new ArrayList<>();

        for (Step astep : digestSteps) {
            Digestion digest = (Digestion) astep;

            //Pull out sources for enzymes and substrates
            {
                String forSubstrate = digest.getSubstrate();
                Location chosenLoc = null;
                Set<Location> forLocs = inventory.getLocations(forSubstrate);
                for (Location loc : forLocs) {
                    Concentration conc = inventory.getConcentration(loc);
                    if (conc == Concentration.zymo || conc == Concentration.miniprep
                            || conc == Concentration.dil20x) {
                        chosenLoc = loc;
                        break;
                    }
                }

                if (chosenLoc == null) {
                    throw new Exception("Null location for " + forSubstrate);
                }
                sources.add(chosenLoc);
                reaction.add(new Pair(Reagent.template, 1.0));
            }

            {
                List<String> enzymes = digest.getEnzymes();
                Location chosenLoc;
                for (String enzyme : enzymes) {
                    chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(enzyme);
                    for (Location loc : forLocs) {
                        chosenLoc = loc;
                        break;
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + enzyme);
                    }
                    sources.add(chosenLoc);

                    Reagent reagent = enzymeMap.get(enzyme);
                    if (reagent == null) {
                        throw new Exception("No enzyme matches query " + enzyme);
                    }
                    reaction.add(new Pair(reagent, 1.0));
                }
            }
            reaction.add(new Pair(Reagent.NEB_Buffer_2_10x, 2.0));
            double sum = 0;
            for (Pair p : reaction) {
                sum = sum + (double) p.getValue();
            }
            reaction.add(new Pair(Reagent.ddH2O, 20.0 - sum));
            Recipe recipe = new Recipe(reaction, null);
            LabSheet sheet = new LabSheet(title, digestSteps, sources, destinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);

        }
        /**
         * ***************
         * Create a Gel sheet
         */
        List<Location> zymoDestinations = new ArrayList<>();
        {
            title = expName + ": Gel";
            program = null;
            protocol = null;
            instrument = null;
            Recipe recipe = null;
            notes = new ArrayList<>();

            //Pull out locations
            for (Step astep : digestSteps) {
                Digestion digest = (Digestion) astep;

                //Pull out the digest product zymo locations
                {
                    String pdtName = digest.getProduct();
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
            title = expName + ": Cleanup";
            program = null;
            protocol = null;
            instrument = null;
            Recipe recipe = null;
            notes = new ArrayList<>();

            //Create the steps
            List<Step> zymoSteps = new ArrayList<>();
            for (Location loc : zymoDestinations) {
                String label = loc.getLabel();
                double volume = 20.0;
                Cleanup cu = new Cleanup(label, volume);
                zymoSteps.add(cu);
            }

            //Package the Gel sheet
            LabSheet sheet = new LabSheet(title, zymoSteps, new ArrayList<>(), zymoDestinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
        return sheets;
    }

    private List<LabSheet> handleLigate(List<Step> ligateSteps, String expName, Inventory inventory) throws Exception {
        List<LabSheet> sheets = new ArrayList<>();

        if (ligateSteps.isEmpty()) {
            return sheets;
        }

        String title = expName + ": Ligation";
        String program = "";
        String protocol = "";
        String instrument = "";
        List<String> notes = new ArrayList<>();

        //Pull out locations
        List<Location> sources = new ArrayList<>();
        List<Location> destinations = new ArrayList<>();
        List<Pair<Reagent, Double>> reaction = new ArrayList<>();

        for (Step astep : ligateSteps) {
            Ligation ligation = (Ligation) astep;

            //Pull out sources for fragments

            {
                List<String> fragments = ligation.getFragments();
                Location chosenLoc;
                int count = 0;
                for (String fragment : fragments) {
                    chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(fragment);
                    for (Location loc : forLocs) {
                        Concentration conc = inventory.getConcentration(loc);
                        if (conc == Concentration.zymo || conc == Concentration.dil20x) {
                            chosenLoc = loc;
                            break;
                        }
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + fragment);
                    }
                    sources.add(chosenLoc);
                    Reagent reagent = null;
                    if (count == 0) {
                        reagent = Reagent.frag1;
                    } else if (count == 1) {
                        reagent = Reagent.frag2;
                    } else if (count == 2) {
                        reagent = Reagent.frag3;
                    } else if (count == 3) {
                        reagent = Reagent.frag4;
                    }

                    if (reagent == null) {
                        throw new Exception("Using more than 4 fragments at one is not recommended for ligation");
                    }
                    reaction.add(new Pair(reagent, 1.0));
                    count++;
                }
            }
            reaction.add(new Pair(Reagent.T4_DNA_ligase, 1.0));
            reaction.add(new Pair(Reagent.T4_DNA_Ligase_Buffer_10x, 2.0));
            double sum = 0;
            for (Pair p : reaction) {
                sum = sum + (double) p.getValue();
            }
            reaction.add(new Pair(Reagent.ddH2O, 20.0 - sum));
            Recipe recipe = new Recipe(reaction, null);
            LabSheet sheet = new LabSheet(title, ligateSteps, sources, destinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);

        }
        /**
         * ***************
         * Create a Gel sheet
         */
        List<Location> zymoDestinations = new ArrayList<>();
        {
            title = expName + ": Gel";
            program = null;
            protocol = null;
            instrument = null;
            Recipe recipe = null;
            notes = new ArrayList<>();

            //Pull out locations
            for (Step astep : ligateSteps) {
                Ligation ligation = (Ligation) astep;

                //Pull out the digest product zymo locations
                {
                    String pdtName = ligation.getProduct();
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
            title = expName + ": Cleanup";
            program = null;
            protocol = null;
            instrument = null;
            Recipe recipe = null;
            notes = new ArrayList<>();

            //Create the steps
            List<Step> zymoSteps = new ArrayList<>();
            for (Location loc : zymoDestinations) {
                String label = loc.getLabel();
                double volume = 20.0;
                Cleanup cu = new Cleanup(label, volume);
                zymoSteps.add(cu);
            }

            //Package the Gel sheet
            LabSheet sheet = new LabSheet(title, zymoSteps, new ArrayList<>(), zymoDestinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
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

