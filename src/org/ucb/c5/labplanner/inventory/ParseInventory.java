package org.ucb.c5.labplanner.inventory;

import java.util.List;
import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Inventory;

/**
 * Reads in a folder contain TSV files representing Boxes.  It parses
 * Box objects, populates HashMaps of the Samples described, and returns
 * an instance of Inventory.
 * 
 * TODO: add authors
 * @author J. Christopher Anderson
 */
public class ParseInventory {
    
    public void initiate()  throws Exception {
        //TODO: write me, or delete comment if not needed
    }
    
    public Inventory run(String path) throws Exception {
        //TODO: write me
        return null;
    }
    
    public static void main(String[] args) throws Exception {
        //Read in an example inventory
        ParseInventory parser = new ParseInventory();
        parser.initiate();
        Inventory inv = parser.run("LabPlannerData/inventory");
        
        //Print out contents of inventory
        List<Box> boxes = inv.getBoxes();
        for(Box abox : boxes) {
            System.out.println(abox.getName());
        }
    }
}
