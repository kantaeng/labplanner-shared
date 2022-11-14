package org.ucb.c5.labplanner.inventory;

import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.utils.FileUtils;

import java.util.function.Consumer;

/**
 * Serialize a Box to human-readable text file
 *
 * @author J. Christopher Anderson
 * @author Michael Danielian
 */
public class SerializeBox {

    public void initiate() throws Exception {
    }

    /**
     * @param abox The Box to be serialized
     * @param path The path and filename for the destination file
     * @throws Exception
     */
    public void run(Box abox, String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(">name").append('\t').append(abox.getName()).append("\n");
        sb.append(">description").append('\t').append(abox.getDescription()).append("\n");
        sb.append(">location").append('\t').append(abox.getLocation()).append("\n\n");

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
     * @param abox The Box to be serialized
     * @param path The path and filename for the destination file
     * @throws Exception
     */
    public void runRowColumn(Box abox, String path) throws Exception {

        //develop the string builder
        StringBuilder sb = new StringBuilder();
        sb.append(">name").append('\t').append(abox.getName()).append("\n");
        sb.append(">description").append('\t').append(abox.getDescription()).append("\n");
        sb.append(">location").append('\t').append(abox.getLocation()).append("\n\n");

        //test all six possible arrays that can be input
        for (int i = 0; i < 6; i++) {
            int sbCheckpoint = sb.length();
            boolean flag = false;
            sb.append(inputLabelText(i) + "\t1\t2\t3\t4\t5\t6\t7\t8\t9"+"\n");

            Sample[][] input = abox.getSamples();
            for (int j = 0; j < input.length; j++) {
                sb.append((char)(65 + j)).append("\t");
                for (int k = 0; k < input[0].length; k++) {
                    if (input[j][k] != null || !input[j][k].equals("")) {
                        flag = true;
                        sb.append(SampleFunction(i, input[j][k])).append("\t");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");

            //this means there is no needed text output
            if(!flag) {
                sb.replace(sbCheckpoint, sb.length(), "");
            }
        }

        FileUtils.writeFile(sb.toString(), path);
    }

    private String inputLabelText(int a) {
        switch(a) {
            case 0:
                return ">>label";
            case 1:
                return ">>side_label";
            case 2:
                return ">>concentration";
            case 3:
                return ">>construct";
            case 4:
                return ">>culture";
            case 5:
                return ">>clone";
        }
        return "";
    }

    private String SampleFunction(int i, Sample a) {
        switch(i) {
            case 0:
                if (a.getLabel() == null|| a.getLabel().equals(""))
                    return "";
                return a.getLabel();
            case 1:
                if (a.getSidelabel() == null|| a.getSidelabel().equals(""))
                    return "";
                return a.getSidelabel();
            case 2:
                if (a.getConcentration() == null || a.getConcentration().equals(""))
                    return "";
                return a.getConcentration().toString();
            case 3:
                if (a.getConstruct() == null || a.getConstruct().equals(""))
                    return "";
                return a.getConstruct();
            case 4:
                if (a.getCulture() == null || a.getCulture().equals(""))
                    return "";
                return a.getCulture().toString();
            case 5:
                if (a.getClone() == null || a.getClone().equals("")) {
                    return "";
                }
                return a.getClone();
        }
        return "";
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

        //testing different representations
        ParseBox a = new ParseBox();
        Box abox = a.run("LabPlannerData/inventory/Box_Lyc6.txt");
        String abspath = "runEx.txt";
        String abspath_b = "rowColumnEx.txt";

        //Serialize the box
        SerializeBox parser = new SerializeBox();
        parser.initiate();

        parser.run(abox, abspath);
        parser.runRowColumn(abox, abspath_b);
    }
}
