package org.ucb.c5.constructionfile.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author J. Christopher Anderson
 */
public class Oligo {
    private final String name;
    private final String sequence;
    private final String description;

    public Oligo(String name, String sequence, String description) {
        this.name = name;
        this.sequence = sequence;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }

    public String getDescription() {
        return description;
    }    
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(">").append(name).append("     ").append(sequence).append("\n");
        out.append(sequence);
        return out.toString();
    }
}
