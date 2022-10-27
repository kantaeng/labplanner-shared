package org.ucb.c5.labplanner.inventory.model;

/**
 * The location of a sample in the inventory
 * 
 * @author J. Christopher Anderson
 */
public class Location {
    private final String boxname;    //The name of the box a sample is in
    private final int row;   //The row within the box, starting with 0
    private final int col;   //The column within the box, starting with 0
    private final String label;   //What's written on the top of the tube
    private final String sidelabel; //What's written on the side of the tube

    public Location(String boxname, int row, int col, String label, String sidelabel) {
        this.boxname = boxname;
        this.row = row;
        this.col = col;
        this.label = label;
        this.sidelabel = sidelabel;
    }
    
    public String getBoxname() {
        return boxname;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getLabel() {
        return label;
    }

    public String getSidelabel() {
        return sidelabel;
    }
}
