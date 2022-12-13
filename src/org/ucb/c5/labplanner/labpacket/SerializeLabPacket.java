package org.ucb.c5.labplanner.labpacket;

import org.ucb.c5.labplanner.LabPacketFactory;
import org.ucb.c5.labplanner.labpacket.model.LabPacket;
import org.ucb.c5.labplanner.labpacket.model.LabSheet;

import java.util.List;

/**
 * Inputs a LabPacket and serializes it to files in a specified folder.
 * The LabPacket contains both LabSheets and modified Boxes. The task
 * of serializing a LabSheet is relayed to SerializeLabSheet, while 
 * serializing a Box is relayed to SerializeBox
 * 
 * @author Michael Danielian
 * @author J. Christopher Anderson
 */

public class SerializeLabPacket {

    private SerializeLabSheet serializeSheet;

    public void initiate() throws Exception {
        serializeSheet = new SerializeLabSheet();
        serializeSheet.initiate();
    }
    
    /**
     * @param packet    the LabPacket to be serialized
     * @param path      the path to the folder to put the files
     * @throws Exception 
     */
    public void run(LabPacket packet, String path) throws Exception {
        List<LabSheet> sheets = packet.getSheets();
        int l = sheets.size();

        for (int i = 0; i < l; i++) {
            serializeSheet.run(sheets.get(i), path);
        }
    }
    
    public static void main(String[] args) throws Exception {
        SerializeLabPacket serialize = new SerializeLabPacket();
        serialize.initiate();

        LabPacketFactory factory = new LabPacketFactory();
        factory.initiate();

        LabPacket a = null;
        String path = "serializedLabPacket.txt";

        //LabPacket a = new LabPacket();
        //LabPacket a = factory.run();

        serialize.run(a, path);
    }
}
