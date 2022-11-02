package org.ucb.c5.labplanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.ucb.c5.constructionfile.SerializeConstructionFile;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.crispr.CrisprConstructionFactory;
import org.ucb.c5.labplanner.inventory.SerializeInventory;
import org.ucb.c5.labplanner.labpacket.SerializeLabPacket;
import org.ucb.c5.labplanner.model.Experiment;
import org.ucb.c5.utils.FileUtils;

/**
 *
 * @author J. Christopher Anderson
 */
public class CRISPRExample {

    public static void main(String[] args) throws Exception {
        //Input the specification (the CDS to knock out here)
        String cds = "ATGTCTTATTCAAAGCATGGCATCGTACAAGAAATGAAGACGAAATACCATATGGAAGGCAGTGTCAATGGCCATGAATTTACGATCGAAGGTGTAGGAACTGGGTACCCTTACGAAGGGAAACAGATGTCCGAATTAGTGATCATCAAGCCTGCGGGAAAACCCCTTCCATTCTCCTTTGACATACTGTCATCAGTCTTTCAATATGGAAACCGTTGCTTCACAAAGTACCCGGCAGACATGCCTGACTATTTCAAGCAAGCATTCCCAGATGGAATGTCATATGAAAGGTCATTTCTATTTGAGGATGGAGCAGTTGCTACAGCCAGCTGGAACATTCGACTCGAAGGAAATTGCTTCATCCACAAATCCATCTTTCATGGCGTAAACTTTCCCGCTGATGGACCCGTAATGAAAAAGAAGACCATTGACTGGGATAAGTCCTTCGAAAAAATGACTGTGTCTAAAGAGGTGCTAAGAGGTGACGTGACTATGTTTCTTATGCTCGAAGGAGGTGGTTCTCACAGATGCCAATTTCACTCCACTTACAAAACAGAGAAGCCGGTCACACTGCCCCCGAATCATGTCGTAGAACATCAAATTGTGAGGACCGACCTTGGCCAAAGTGCAAAAGGCTTTACAGTCAAGCTGGAAGCACATGCCGCGGCTCATGTTAACCCTTTGAAGGTTAAATAA";
        String target = "agfp";  //Name of the gene being knocked out
        int experimentId = 33;

        //Make the construction file and package as a List
        CrisprConstructionFactory factory = new CrisprConstructionFactory();
        factory.initiate();
        ConstructionFile constf = factory.run(target, cds);
        List<ConstructionFile> cfList = new ArrayList<>();
        cfList.add(constf);

        //Print it out
        SerializeConstructionFile cfSerializer = new SerializeConstructionFile();
        cfSerializer.initiate();
        String text = cfSerializer.run(constf);
        System.out.println(text);

        //Design the Experiment
        ExperimentFactory ceg = new ExperimentFactory();
        ceg.initiate();
        Experiment exp = ceg.run(target + "KO", experimentId, cfList, null);

        /**
         * *****************
         * Serialize Everything
        *******************
         */
        File dir = new File("CRISPRExample");
        dir.mkdir();

        //Serialize the Inventory
        SerializeInventory si = new SerializeInventory();
        si.initiate();
        si.run(exp.getInventory(), "CRISPRExample/CRISPRExample_Inventory");

        //Serialize the oligo order
        GenerateIDTOrder sol = new GenerateIDTOrder();
        sol.initiate();
        sol.run(exp.getOligos(), "CRISPRExample/CRISPRExample_IDTorder.txt");

        //Serialize the CF's
        SerializeConstructionFile scf = new SerializeConstructionFile();
        scf.initiate();
        for (ConstructionFile cf : exp.getCfs()) {
            String textCF = scf.run(constf);
            FileUtils.writeFile(textCF, "CRISPRExample/Construction of " + cf.getPdtName() + ".txt");
        }

        //Serialize the LabPacket
        SerializeLabPacket slp = new SerializeLabPacket();
        slp.initiate();
        slp.run(exp.getLabpacket(), "CRISPRExample/CRISPRExample_LabPacket");
        
        System.out.println("done");
    }
}
