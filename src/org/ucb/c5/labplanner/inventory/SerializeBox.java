package org.ucb.c5.labplanner.inventory;

import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.utils.FileUtils;

/**
 * Serialize a Box to human-readable text file
 *
 * TODO: add authors
 *
 * @author J. Christopher Anderson
 */
public class SerializeBox {

    public void initiate() throws Exception {
    }

    /**
     * @param abox The Box to be serialized
     * @param abspath The path and filename for the destination file
     * @throws Exception
     */
    public void run(Box abox, String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(">name: ").append(abox.getName()).append("\n");
        sb.append(">description: ").append(abox.getDescription()).append("\n");
        sb.append(">location: ").append(abox.getLocation()).append("\n\n");

        //Put in sample information
        sb.append(">>well\tconstruct\tlabel\tside-label\tconcentration\tclone\tculture\n");
        Sample[][] samples = abox.getSamples();
        for (int row = 0; row < samples.length; row++) {
            for (int col = 0; col < samples[row].length; col++) {
                Sample sam = samples[row][col];
                if (sam == null) {
                    continue;
                }
                sb.append(calcWellLabel(row,col)).append("\t");
                sb.append(sam.getConstruct()).append("\t");
                sb.append(sam.getLabel()).append("\t");
                sb.append(sam.getSidelabel()).append("\t");
                sb.append(sam.getConcentration()).append("\t");
                sb.append(sam.getClone()).append("\t");
                sb.append(sam.getCulture()).append("\n");
            }
        }

        FileUtils.writeFile(sb.toString(), path);
    }

    /**
     * Converts the [0,0] representation to A1
     * @param row
     * @param col
     * @return 
     */
    private String calcWellLabel(int row, int col) {
        col++;
        int irow = 65 + row;
        char crow = (char) irow;
        String out = "" + crow + col;
        return out;
    }

    public static void main(String[] args) throws Exception {
        Sample[][] wells = new Sample[9][9];
        //Create 8 samples
        for (int x = 0; x < 8; x++) {
            Sample asample = new Sample("Sample" + x,
                    "Sample" + x,
                    Sample.Concentration.miniprep,
                    "construct" + x,
                    Sample.Culture.primary,
                    "" + x);
            wells[x][x] = asample;
        }

        //Create an example instance of Box and serialize
        Box abox = new Box("SerializeBoxExampleBox", "A dummy box", "minus20", wells);
        String abspath = "SerializeBoxExampleBox.txt"; //TODO:  put in a path
        //Serialize the box
        SerializeBox parser = new SerializeBox();
        parser.initiate();
        parser.run(abox, abspath);
    }
}
