package uk.ac.standrews.cs.valipop.simulationEntities;

public class Surname {

    // TODO why does this exist?

    final private String name;

    public Surname(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
}
