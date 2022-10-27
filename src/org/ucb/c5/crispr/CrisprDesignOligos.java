package org.ucb.c5.crispr;

import org.ucb.c5.utils.Pair;

/**
 *
 * @author J. Christopher Anderson
 */
public class CrisprDesignOligos {
  private final int SPACER_LENGTH = 20;
    
    public void initiate() throws Exception {}
    
    public Pair<String,String> run(String cds) throws Exception {
 	if(cds.length() < 23) {
            throw new IllegalArgumentException("CDS is too short to contain a target site");
        }

	cds = cds.toUpperCase();
 	if(!cds.matches("[ATCG]+")) {
            throw new IllegalArgumentException("CDS is not DNA");
        }
        
        //Calculate the start index for the NGG PAM sequence
        int PAM_start = cds.indexOf("GG", SPACER_LENGTH + 2) -1;
        
        if(PAM_start < 0) {
            throw new IllegalArgumentException ("CDS contains no target site on this strand");
        }
        
        //Construct the oligos and return
        String spacer = cds.substring(PAM_start - SPACER_LENGTH, PAM_start);
        
        //                 tail><SpeI>              <gRNA_anneal---------->
        String foroligo = "ccataACTAGT" + spacer + "gttttagagctagaaatagcaag";
        
        //                           tail><SpeI><J23119_anneal------->
        return new Pair<>(foroligo, "ctcagACTAGTattatacctaggactgagctag");    
    }
    
 
    public static void main(String[] args) throws Exception {
        //Create some example arguments, here the amilGFP coding sequence
        String dna = "ATGTCTTATTCAAAGCATGGCATCGTACAAGAAATGAAGACGAAATACCATATGGAAGGCAGTGTCAATGGCCATGAATTTACGATCGAAGGTGTAGGAACTGGGTACCCTTACGAAGGGAAACAGATGTCCGAATTAGTGATCATCAAGCCTGCGGGAAAACCCCTTCCATTCTCCTTTGACATACTGTCATCAGTCTTTCAATATGGAAACCGTTGCTTCACAAAGTACCCGGCAGACATGCCTGACTATTTCAAGCAAGCATTCCCAGATGGAATGTCATATGAAAGGTCATTTCTATTTGAGGATGGAGCAGTTGCTACAGCCAGCTGGAACATTCGACTCGAAGGAAATTGCTTCATCCACAAATCCATCTTTCATGGCGTAAACTTTCCCGCTGATGGACCCGTAATGAAAAAGAAGACCATTGACTGGGATAAGTCCTTCGAAAAAATGACTGTGTCTAAAGAGGTGCTAAGAGGTGACGTGACTATGTTTCTTATGCTCGAAGGAGGTGGTTCTCACAGATGCCAATTTCACTCCACTTACAAAACAGAGAAGCCGGTCACACTGCCCCCGAATCATGTCGTAGAACATCAAATTGTGAGGACCGACCTTGGCCAAAGTGCAAAAGGCTTTACAGTCAAGCTGGAAGCACATGCCGCGGCTCATGTTAACCCTTTGAAGGTTAAATAA";
        
        //Instantiate and initiate the Function
        CrisprDesignOligos func = new CrisprDesignOligos();
        func.initiate();
        
        //Run the function on the example
        Pair<String,String> oligos = func.run(dna);
        
        //Print out the result
        System.out.println("Forward oligo: " + oligos.getKey());
        System.out.println("Reverse oligo: " + oligos.getValue());
    }
}