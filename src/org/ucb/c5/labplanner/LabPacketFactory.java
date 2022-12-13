package org.ucb.c5.labplanner;

import java.util.*;

import org.ucb.c5.constructionfile.model.*;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.inventory.model.Location;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.labplanner.inventory.model.Sample.Concentration;
import org.ucb.c5.labplanner.labpacket.model.Cleanup;
import org.ucb.c5.labplanner.labpacket.model.Gel;
import org.ucb.c5.labplanner.labpacket.model.LabPacket;
import org.ucb.c5.labplanner.labpacket.model.LabSheet;
import org.ucb.c5.labplanner.labpacket.model.Reagent;
import org.ucb.c5.labplanner.labpacket.model.Recipe;
import org.ucb.c5.utils.Pair;

import javax.swing.*;

import static org.ucb.c5.labplanner.inventory.model.Sample.Concentration.miniprep;
import static org.ucb.c5.labplanner.inventory.model.Sample.Concentration.zymo;

/**
 *
 * @author Shahar Schwartz
 */
public class LabPacketFactory {

    private final Map<String, Reagent> enzymeMap = new HashMap<String, Reagent>();
    private final Map<String, Reagent> strainMap = new HashMap<String, Reagent>();

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

        strainMap.put("zymo_10b", Reagent.zymo_10b);
        strainMap.put("Zymo_5a", Reagent.Zymo_5a);
        strainMap.put("JM109", Reagent.JM109);
        strainMap.put("DH10B", Reagent.DH10B);
        strainMap.put("MC1061", Reagent.MC1061);
        strainMap.put("Ec100D_pir116", Reagent.Ec100D_pir116);
        strainMap.put("Ec100D_pir_plus", Reagent.Ec100D_pir_plus);

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

        {
        for (Step astep : digestSteps) {
            Digestion digest = (Digestion) astep;

            //Pull out sources for enzymes and substrates
            {
                String forSubstrate = digest.getSubstrate();
                Location chosenLoc = null;
                Set<Location> forLocs = inventory.getLocations(forSubstrate);
                for (Location loc : forLocs) {
                    Concentration conc = inventory.getConcentration(loc);
                    if (conc == Concentration.zymo || conc == miniprep
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

        {
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

    private List<LabSheet> handleTransform(List<Step> transformSteps, String expName, Inventory inventory) throws Exception {
        List<LabSheet> sheets = new ArrayList<>();
        //Create a Transform sheet
        //TODO:  create a Transform labsheet and add to sheets
        if (transformSteps.isEmpty()) {
            return sheets;
        }
        {
        String title = expName + ": Transform";
        String program = "";
        String protocol = "";
        String instrument = "";
        List<String> notes = new ArrayList<>();

        List<Location> sources = new ArrayList<>();
        List<Location> destinations = new ArrayList<>();
        List<Pair<Reagent, Double>> reaction = new ArrayList<>();

        {
            for (Step astep : transformSteps) {
                Transformation transformation = (Transformation) astep;

                //Pull out sources for dna, strain, and antibiotic
                {
                    String forDNA = transformation.getDna();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(forDNA);
                    for (Location loc : forLocs) {
                        Concentration conc = inventory.getConcentration(loc);
                        if (conc == Concentration.zymo || conc == miniprep
                                || conc == Concentration.dil20x) {
                            chosenLoc = loc;
                            break;
                        }
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + forDNA);
                    }
                    sources.add(chosenLoc);
                    reaction.add(new Pair(Reagent.template, 1.0));
                }

                {
                    String forStrain = transformation.getStrain();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(forStrain);
                    for (Location loc : forLocs) {
                        chosenLoc = loc;
                        break;
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + forStrain);
                    }
                    sources.add(chosenLoc);
                    Reagent reagent = strainMap.get(forStrain);
                    reaction.add(new Pair(reagent, 250.0));
                }

                {
                    String forAntibiotic = transformation.getAntibiotic().name();
                    Location chosenLoc = null;
                    Set<Location> forLocs = inventory.getLocations(forAntibiotic);
                    for (Location loc : forLocs) {
                        chosenLoc = loc;
                        break;
                    }

                    if (chosenLoc == null) {
                        throw new Exception("Null location for " + forAntibiotic);
                    }
                    sources.add(chosenLoc);
                    reaction.add(new Pair(transformation.getAntibiotic(), 2.5));
                }
                reaction.add(new Pair(Reagent.lb, 400));
                if (transformation.getAntibiotic().equals(Antibiotic.Spec)){
                   reaction.add(new Pair(Reagent.lb_agar_100ug_ml_specto, 1));
                } else if (transformation.getAntibiotic().equals(Antibiotic.Kan)){
                    reaction.add(new Pair(Reagent.lb_agar_50ug_ml_kan, 1));
                } else if (transformation.getAntibiotic().equals(Antibiotic.Cam)){
                    reaction.add(new Pair(Reagent.lb_agar_100ug_ml_cm, 1));
                } else if (transformation.getAntibiotic().equals(Antibiotic.Amp)){
                    reaction.add(new Pair(Reagent.lb_agar_100ug_ml_amp, 1));
                }
            }
            Recipe recipe = new Recipe(reaction, null);
            LabSheet sheet = new LabSheet(title, transformSteps, sources, destinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
    }

        //Create a Pick sheet
        //TODO:  create a picking/inoculation labsheet and add to sheets
        {
        List<Location> pickDestinations = new ArrayList<>();
            {
                String title = expName + ": Pick";
                String program = null;
                String protocol = null;
                String instrument = null;
                Recipe recipe = null;
                List<String> notes = new ArrayList<>();

                //Pull out locations
                for (Step astep : transformSteps) {
                    Transformation transformation = (Transformation) astep;

                    //Pull out the transformation products
                    {
                        String pdtName = transformation.getProduct();
                        Location chosenLoc = null;
                        Set<Location> forLocs = inventory.getLocations(pdtName);
                        inner:
                        for (Location loc : forLocs) {
                            Concentration conc = inventory.getConcentration(loc);
                            chosenLoc = loc;
                            break;
                        }

                        if (chosenLoc == null) {
                            throw new Exception("Null location for " + pdtName);
                        }
                        pickDestinations.add(chosenLoc);
                    }
                }

                //Create the steps
                //TODO replace everything under here with pick sheet, not gel
                List<Step> pickSteps = new ArrayList<>();
                for (Location loc : pickDestinations) {
                    String label = loc.getLabel();
                    int size = 3800;
                    Gel gs = new Gel(label, size);
                    pickSteps.add(gs);
                }

                //Package the pick sheet
                LabSheet sheet = new LabSheet(title, pickSteps, new ArrayList<>(), new ArrayList<>(), program, protocol, instrument, notes, recipe);
                sheets.add(sheet);
            }


        //Create a Miniprep sheet
        //TODO:  create a miniprep labsheet and add to sheets

            String title = expName + ": Miniprep";
            String program = null;
            String protocol = null;
            String instrument = null;
            Recipe recipe = null;
            List<String> notes = new ArrayList<>();

            //Create the steps
            List<Step> miniprepSteps = new ArrayList<>();
            for (Location loc : pickDestinations) {
                String label = loc.getLabel();
                double volume = 20.0;
                Cleanup cu = new Cleanup(label, volume);
                miniprepSteps.add(cu);
            }

            //Package the Gel sheet
            LabSheet sheet = new LabSheet(title, miniprepSteps, new ArrayList<>(), pickDestinations, program, protocol, instrument, notes, recipe);
            sheets.add(sheet);
        }
        return sheets;
    }

    public static void main(String[] args) throws Exception {
//        LabPacketFactory lpf = new LabPacketFactory();
//        lpf.initiate();
//        ArrayList<ConstructionFile> cfList = new ArrayList<>();
//
//        Sample[][] samples = new Sample[9][9];
//        samples[0][0] = new Sample("miniprep", null, Concentration.miniprep, null, Sample.Culture.primary, null);
//        samples[0][1] = new Sample("zymo", null, Concentration.zymo, null, Sample.Culture.primary, null);
//        samples[0][2] = new Sample("uM100", null, Concentration.uM100, null, Sample.Culture.primary, null);
//        samples[0][3] = new Sample("uM10", null, Concentration.uM10, null, Sample.Culture.primary, null);
//        samples[0][4] = new Sample("dil20x", null, Concentration.dil20x, null, Sample.Culture.primary, null);
//        samples[1][0] = new Sample("oligo1", null, Concentration.uM10, null,Sample.Culture.primary, null);
//        samples[1][1] = new Sample("oligo2", null, Concentration.uM10, null,Sample.Culture.primary, null);
//
//        org.ucb.c5.labplanner.inventory.model.Box box = new org.ucb.c5.labplanner.inventory.model.Box("box1", null, "location", samples);
//        List<org.ucb.c5.labplanner.inventory.model.Box> boxes = new ArrayList<>();
//        boxes.add(box);
//
//        Location one = new Location("box1", 0, 0, "miniprep", null);
//        Location two = new Location("box1", 0, 1, "zymo", null);
//        Location three = new Location("box1", 0, 2, "uM100", null);
//        Location four = new Location("box1", 0, 3, "uM10", null);
//        Location five = new Location("box1", 0, 4, "dil20x", null);
//        Location oligo1 = new Location("box1", 1, 0, "oligo1", null);
//
//        //populate hashmaps
//        HashMap<String, Set<Location>> constructToLocations = new HashMap<>();
//        HashMap<Location, Sample.Concentration> locToConc = new HashMap<>();
//        HashMap<Location, String> locToClone = new HashMap<>();
//        HashMap<Location, Sample.Culture> locToCulture = new HashMap<>();
//
//        locToConc.put(one, miniprep);
//        locToConc.put(two, zymo);
//        locToConc.put(three, Concentration.uM100);
//        locToConc.put(four, Concentration.uM10);
//        locToConc.put(five, Concentration.dil20x);
//
//        locToCulture.put(one, Sample.Culture.primary);
//        locToCulture.put(two, Sample.Culture.primary);
//        locToCulture.put(three, Sample.Culture.primary);
//        locToCulture.put(four, Sample.Culture.primary);
//        locToCulture.put(five, Sample.Culture.primary);
//
//        Inventory inventory = new Inventory(boxes, constructToLocations, locToConc, locToClone, locToCulture);
//        lpf.run("exp", cfList, inventory);
//
//
//        //put the reagents in the hashmaps as well
//
//        PCR pcr = new PCR("oligo1", "oligo2", null, null);
//        Digestion digest = new Digestion(null, null, 1, null);
//        Ligation ligate = new Ligation(null, null);
//        Assembly assembly = new Assembly(null, null, null);
//        Transformation transform = new Transformation(null, null, null, null);
//        ArrayList<Step> steps = new ArrayList<>();
//        steps.add(pcr);
//        steps.add(digest);
//        steps.add(ligate);
//        steps.add(assembly);
//        steps.add(transform);
//
//        ConstructionFile cf = new ConstructionFile(steps, null, new HashMap<String, Polynucleotide>());
//        cfList.add(cf);
//        lpf.run("exp", cfList, inventory);

    }
}

