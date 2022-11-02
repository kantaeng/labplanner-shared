package org.ucb.c5.crispr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ucb.c5.constructionfile.model.Antibiotic;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.constructionfile.model.Digestion;
import org.ucb.c5.constructionfile.model.Ligation;
import org.ucb.c5.constructionfile.model.Modifications;
import org.ucb.c5.constructionfile.model.Oligo;
import org.ucb.c5.constructionfile.model.PCR;
import org.ucb.c5.constructionfile.model.Polynucleotide;
import org.ucb.c5.constructionfile.model.Step;
import org.ucb.c5.constructionfile.model.Transformation;
import org.ucb.c5.utils.Pair;

/**
 *
 * @author J. Christopher Anderson
 */
public class CrisprConstructionFactory {

    private final List<String> templates = new ArrayList<>();
    private final String strain = "Mach1";
    private final String template = "pTargetF";
    private final String templateSeq = "catgttctttcctgcgttatcccctgattctgtggataaccgtattaccgcctttgagtgagctgataccgctcgccgcagccgaacgaccgagcgcagcgagtcagtgagcgaggaagcggaagagcgcctgatgcggtattttctccttacgcatctgtgcggtatttcacaccgcatatgctggatccttgacagctagctcagtcctaggtataatactagtcatcgccgcagcggtttcaggttttagagctagaaatagcaagttaaaataaggctagtccgttatcaacttgaaaaagtggcaccgagtcggtgctttttttgaattctctagagtcgacctgcagaagcttagatctattaccctgttatccctactcgagttcatgtgcagctccataagcaaaaggggatgataagtttatcaccaccgactatttgcaacagtgccgttgatcgtgctatgatcgactgatgtcatcagcggtggagtgcaatgtcatgagggaagcggtgatcgccgaagtatcgactcaactatcagaggtagttggcgtcatcgagcgccatctcgaaccgacgttgctggccgtacatttgtacggctccgcagtggatggcggcctgaagccacacagtgatattgatttgctggttacggtgaccgtaaggcttgatgaaacaacgcggcgagctttgatcaacgaccttttggaaacttcggcttcccctggagagagcgagattctccgcgctgtagaagtcaccattgttgtgcacgacgacatcattccgtggcgttatccagctaagcgcgaactgcaatttggagaatggcagcgcaatgacattcttgcaggtatcttcgagccagccacgatcgacattgatctggctatcttgctgacaaaagcaagagaacatagcgttgccttggtaggtccagcggcggaggaactctttgatccggttcctgaacaggatctatttgaggcgctaaatgaaaccttaacgctatggaactcgccgcccgactgggctggcgatgagcgaaatgtagtgcttacgttgtcccgcatttggtacagcgcagtaaccggcaaaatcgcgccgaaggatgtcgctgccgactgggcaatggagcgcctgccggcccagtatcagcccgtcatacttgaagctagacaggcttatcttggacaagaagaagatcgcttggcctcgcgcgcagatcagttggaagaatttgtccactacgtgaaaggcgagatcaccaaggtagtcggcaaataagatgccgctcgccagtcgattggctgagctcataagttcctattccgaagttccgcgaacgcgtaaaggatctaggtgaagatcctttttgataatctcatgaccaaaatcccttaacgtgagttttcgttccactgagcgtcagaccccgtagaaaagatcaaaggatcttcttgagatcctttttttctgcgcgtaatctgctgcttgcaaacaaaaaaaccaccgctaccagcggtggtttgtttgccggatcaagagctaccaactctttttccgaaggtaactggcttcagcagagcgcagataccaaatactgtccttctagtgtagccgtagttaggccaccacttcaagaactctgtagcaccgcctacatacctcgctctgctaatcctgttaccagtggctgctgccagtggcgataagtcgtgtcttaccgggttggactcaagacgatagttaccggataaggcgcagcggtcgggctgaacggggggttcgtgcacacagcccagcttggagcgaacgacctacaccgaactgagatacctacagcgtgagctatgagaaagcgccacgcttcccgaagggagaaaggcggacaggtatccggtaagcggcagggtcggaacaggagagcgcacgagggagcttccagggggaaacgcctggtatctttatagtcctgtcgggtttcgccacctctgacttgagcgtcgatttttgtgatgctcgtcaggggggcggagcctatggaaaaacgccagcaacgcggcctttttacggttcctggccttttgctggccttttgctca";
    private final Antibiotic antibiotic = Antibiotic.Spec;
    private final CrisprDesignOligos oligoDesigner = new CrisprDesignOligos();

    public void initiate() throws Exception {
        templates.add(template);
        oligoDesigner.initiate();
    }

    /**
     *
     * @param target a name for the sequence targeted for knockout
     * @param dna the sequence wherein the gRNA should react
     * @return
     * @throws Exception
     */
    public ConstructionFile run(String target, String dna) throws Exception {
        //Name the product plasmid
        String pdtName = "pTarg-" + target;
        
        //Design the oligos
        Pair<String, String> oligos = oligoDesigner.run(dna);
        Oligo forOligo = new Oligo(target + "F", oligos.getKey(), "Forward oligo for gRNA for " + target);
        Oligo revOligo = new Oligo(target + "R", oligos.getValue(), "Reverse oligo for gRNA for " + target);

        //Hard-code a ConstructionFile describing a CRISPR Experiment
        List<Step> steps = new ArrayList<>();

        //pcr ca4238,ca4239 on pTargetF	(3927 bp, ipcr)
        steps.add(new PCR(forOligo.getName(), revOligo.getName(), templates, "ipcr"));

        //digest pcr with SpeI,DpnI	(spedig)
        List<String> enzymes = new ArrayList<>();
        enzymes.add("SpeI");
        steps.add(new Digestion("pcr", enzymes, 1, "spedig"));

        //ligate dig	(lig)
        List<String> digs = new ArrayList<>();
        digs.add("dig");
        steps.add(new Ligation(digs, "lig"));

        //transform lig	(Mach1, Spec)
        steps.add(new Transformation("lig", strain, antibiotic, pdtName));

        //Put in the sequences of the two oligos and template
        Map<String, Polynucleotide> sequences = new HashMap<>();
        sequences.put(forOligo.getName(), new Polynucleotide(forOligo.getSequence(), "", "", false, false, false, Modifications.hydroxyl, Modifications.hydroxyl));
        sequences.put(revOligo.getName(), new Polynucleotide(revOligo.getSequence(), "", "", false, false, false, Modifications.hydroxyl, Modifications.hydroxyl));
        sequences.put(template, new Polynucleotide(templateSeq, true));

        //Instantiate the Construction File
        ConstructionFile constf = new ConstructionFile(steps, pdtName, sequences);
        return constf;
    }

    public static void main(String[] args) throws Exception {
        //Initializze the Function
        CrisprConstructionFactory factory = new CrisprConstructionFactory();
        factory.initiate();

        String cds = "ATGTCTTATTCAAAGCATGGCATCGTACAAGAAATGAAGACGAAATACCATATGGAAGGCAGTGTCAATGGCCATGAATTTACGATCGAAGGTGTAGGAACTGGGTACCCTTACGAAGGGAAACAGATGTCCGAATTAGTGATCATCAAGCCTGCGGGAAAACCCCTTCCATTCTCCTTTGACATACTGTCATCAGTCTTTCAATATGGAAACCGTTGCTTCACAAAGTACCCGGCAGACATGCCTGACTATTTCAAGCAAGCATTCCCAGATGGAATGTCATATGAAAGGTCATTTCTATTTGAGGATGGAGCAGTTGCTACAGCCAGCTGGAACATTCGACTCGAAGGAAATTGCTTCATCCACAAATCCATCTTTCATGGCGTAAACTTTCCCGCTGATGGACCCGTAATGAAAAAGAAGACCATTGACTGGGATAAGTCCTTCGAAAAAATGACTGTGTCTAAAGAGGTGCTAAGAGGTGACGTGACTATGTTTCTTATGCTCGAAGGAGGTGGTTCTCACAGATGCCAATTTCACTCCACTTACAAAACAGAGAAGCCGGTCACACTGCCCCCGAATCATGTCGTAGAACATCAAATTGTGAGGACCGACCTTGGCCAAAGTGCAAAAGGCTTTACAGTCAAGCTGGAAGCACATGCCGCGGCTCATGTTAACCCTTTGAAGGTTAAATAA";
        String target = "agfp";  //Name of the gene being knocked out
        
        //Run the factory
        ConstructionFile constf = factory.run(target, cds);

        //Print it out
        for (Step astep : constf.getSteps()) {
            System.out.println(astep.getOperation() + "   " + astep.getProduct() + "   " + astep.getClass().toString());
        }
    }

}
