package org.ucb.c5.labplanner.labpacket.model;

import java.util.List;

/**
 * Wrapper class for the bolus of LabSheets
 * and modified Box files resulting from
 * simulation of an entire Experiment
 * 
 * @author J. Christopher Anderson
 */
public class LabPacket {
    private final List<LabSheet> sheets;

    public LabPacket(List<LabSheet> sheets) {
        this.sheets = sheets;
    }

    public List<LabSheet> getSheets() {
        return sheets;
    }
}
