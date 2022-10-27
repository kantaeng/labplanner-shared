package org.ucb.c5.labplanner.inventory;

import java.io.Serializable;
import java.util.Arrays;
import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.utils.FileUtils;

/**
 * Reads in a TSV file representing a Box of Samples. It reads in the file and
 * returns and instance of Box
 *
 * @author J. Christopher Anderson
 * @author David Blayvas
 */
public class ParseBox {



    /**
     * Converts a tab-separated table String into a 2-dimensional String[][] array.
     * @param tsv_data The contents of a TSV file as a string.
     * @return A 2-d array representing a table of Strings.
     * @author David Blayvas
     */
    private static String[][] TSVToArray(String tsv_data) {
        String[] data_rows = tsv_data.split("\n");
        String[][] data = new String[data_rows.length][];
        for (int i = 0; i < data_rows.length; i++) {
            String[] row = data_rows[i].split("\t");
            data[i] = row;
        }
        return data;
    }

    private static int[] calcWellCoords(String label) {
        int[] coords = new int[2];
        coords[0] = label.charAt(0) - 65;
        coords[1] = Integer.parseInt(label.substring(1)) - 1;
        return coords;
    }

    public void initiate() throws Exception {
        //TODO: write me, or delete comment if not needed
    }

    public Box run(String path) throws Exception {
        //TODO: write me
        String content = FileUtils.readFile(path);
        String[][] data = TSVToArray(content);

        if (!data[0][0].startsWith(">name: ")) { throw new Exception("Invalid Box serialized name format."); }
        String name = data[0][0].substring(7, data[0][0].length());

        if (!data[1][0].startsWith(">description: ")) { throw new Exception("Invalid Box serialized description format."); }
        String desc = data[1][0].substring(14, data[1][0].length());

        if (!data[2][0].startsWith(">location: ")) { throw new Exception("Invalid Box serialized location format."); }
        String loc = data[2][0].substring(11, data[2][0].length());

        String table_header = String.join("\t", data[4]);
        String table_header_reference = ">>well\tconstruct\tlabel\tside-label\tconcentration\tclone\tculture";
        if (!table_header.equals(table_header_reference)) { throw new Exception("Invalid Box serialized table format."); }

        int width = 1, height = 1;
        // Create a Sample[][] grid only large enough to fit the largest index sample
        for (int row_index = 5; row_index<data.length; row_index++) {
            int[] coords = calcWellCoords(data[row_index][0]);
            width = Math.max(width, coords[0]);
            height = Math.max(height, coords[0]);
        }
        Sample[][] samples = new Sample[width + 1][height + 1];

        // Populate Samples[][] this time
        for (int row_index = 5; row_index<data.length; row_index++) {
            int[] coords = calcWellCoords(data[row_index][0]);

            String construct          = data[row_index][1];
            String label              = data[row_index][2];
            String side_label         = data[row_index][3];
            Sample.Concentration conc = Sample.Concentration.valueOf(data[row_index][4]);
            String clone              = data[row_index][5];
            Sample.Culture culture    = Sample.Culture.valueOf(data[row_index][6]);

            Sample sample = new Sample(label, side_label, conc, construct, culture, clone);
            samples[coords[0]][coords[1]] = sample;
        }

        return new Box(name, desc, loc, samples);
    }

    public static void main(String[] args) throws Exception {
        //Read in an example inventory
        ParseBox parser = new ParseBox();
        parser.initiate();
//        Box abox = parser.run("LabPlannerData/inventory/Box_Lyc6.txt");
        Box abox = parser.run("SerializeBoxExampleBox.txt");

        //Print out contents of box
        System.out.println(abox.getName());
        System.out.println(abox.getDescription());
        System.out.println(abox.getLocation());
        System.out.println(Arrays.toString(abox.getSamples()));
    }
}
