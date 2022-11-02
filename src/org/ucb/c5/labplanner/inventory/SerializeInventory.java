package org.ucb.c5.labplanner.inventory;

import java.io.File;
import org.ucb.c5.labplanner.inventory.model.Box;
import org.ucb.c5.labplanner.inventory.model.Inventory;
import org.ucb.c5.labplanner.inventory.model.Sample;
import org.ucb.c5.utils.FileUtils;

/**
 * Serialize a Box to human-readable text file
 *
 * TODO: add authors
 *
 * @author J. Christopher Anderson
 */
public class SerializeInventory {

    private final SerializeBox boxSerializer = new SerializeBox();

    public void initiate() throws Exception {
        boxSerializer.initiate();
    }

    /**
     * @param abox The Box to be serialized
     * @param abspath The path and filename for the destination file
     * @throws Exception
     */
    public void run(Inventory inventory, String dirpath) throws Exception {
        File dir = new File(dirpath);
        if(!dir.exists()) {
            dir.mkdir();
        }
        for (Box abox : inventory.getBoxes()) {
            boxSerializer.run(abox, dirpath + "/" + abox.getName() + ".txt");
        }
    }

    public static void main(String[] args) throws Exception {
        SerializeInventory parser = new SerializeInventory();
        parser.initiate();
    }
}
