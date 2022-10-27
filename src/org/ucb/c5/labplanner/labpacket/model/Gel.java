package org.ucb.c5.labplanner.labpacket.model;

import java.util.List;
import org.ucb.c5.constructionfile.model.Operation;
import org.ucb.c5.constructionfile.model.Step;

/**
 *
 * @author J. Christopher Anderson
 */
public class Gel implements Step {
    private final String sample;
    private final int size;

    public Gel(String sample, int size) {
        this.sample = sample;
        this.size = size;
    }

    public String getSample() {
        return sample;
    }

    public int getSize() {
        return size;
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
