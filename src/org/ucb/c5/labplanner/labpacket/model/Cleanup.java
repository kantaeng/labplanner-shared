package org.ucb.c5.labplanner.labpacket.model;

import java.util.List;
import org.ucb.c5.constructionfile.model.Operation;
import org.ucb.c5.constructionfile.model.Step;
import org.ucb.c5.labplanner.inventory.model.Location;

/**
 *
 * @author J. Christopher Anderson
 */
public class Cleanup implements Step {
    private final String sample;
    private final double volume;

    public Cleanup(String sample, double volume) {
        this.sample = sample;
        this.volume = volume;
    }

    public String getSample() {
        return sample;
    }

    public double getVolume() {
        return volume;
    }
    
    @Override
    public Operation getOperation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProduct() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getInputs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
