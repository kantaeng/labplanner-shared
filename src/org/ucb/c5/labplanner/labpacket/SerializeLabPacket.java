package org.ucb.c5.labplanner.labpacket;

import org.ucb.c5.labplanner.labpacket.model.LabPacket;

/**
 * Inputs a LabPacket and serializes it to files in a specified folder.
 * The LabPacket contains both LabSheets and modified Boxes.  The task
 * of serializing a LabSheet is relayed to SerializeLabSheet, while 
 * serializing a Box is relayed to SerializeBox
 * 
 * TODO: add authors
 * @author J. Christopher Anderson
 */
public class SerializeLabPacket {
    
    public void initiate()  throws Exception {
        //TODO: write me, or delete comment if not needed
    }
    
    /**
     * @param packet    the LabPacket to be serialized
     * @param path      the path to the folder to put the files
     * @throws Exception 
     */
    public void run(LabPacket packet, String path) throws Exception {
        //TODO: write me
    }
    
    public static void main(String[] args) {
        //TODO: create an example to demo run
    }
}
