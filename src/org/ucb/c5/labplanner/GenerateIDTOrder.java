package org.ucb.c5.labplanner;

import java.util.ArrayList;
import java.util.List;
import org.ucb.c5.constructionfile.model.Oligo;
import org.ucb.c5.utils.FileUtils;

/**
 *
 * @author J. Christopher Anderson
 */
public class GenerateIDTOrder {

    public void initiate() throws Exception {}

    public void run(List<Oligo> oligoList, String filepath) throws Exception {
        //Put all the oligo data into a StringBuilder
        StringBuilder sb = new StringBuilder();
        for(Oligo oligo : oligoList) {
            sb.append(oligo.getName()).append("\t");
            sb.append(oligo.getSequence()).append("\t");
            String scale = "25nm";
            if(oligo.getSequence().length() > 59) {
                scale = "100nm";
            }
            sb.append(scale).append("\tSTD\t");
            sb.append(oligo.getDescription()).append("\t");
            sb.append("\n");
        }
        
        //Write to file
        FileUtils.writeFile(sb.toString(), filepath);
    }

    public static void main(String[] args) throws Exception {
        GenerateIDTOrder gio = new GenerateIDTOrder();
        gio.initiate();
        
        List<Oligo> oligos = new ArrayList<>();
        oligos.add(new Oligo("ca998", "gtatcacgaggcagaatttcag", "Forward sequencing of most biobrick plasmids"));
        oligos.add(new Oligo("KB005", "ccatatctagagACCCAAAAGCAAGAGGTGATTCTAGTTggtggttaatgaaaattaacttacttactagaaat", "Forward XbaI oligo for part 3c "));

        gio.run(oligos, "GenerateIDTOrder_example_output.txt"
);
        
    }
}
