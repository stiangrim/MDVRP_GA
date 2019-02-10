package ga;

import dto.ProblemDTO;
import model.Vehicle;

import java.util.ArrayList;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class DNA {

    // The list of vehicles represents the gene/chromosome of the DNA.
    private ArrayList<Vehicle> vehicles;
    private double fitness;

    DNA(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    DNA(ProblemDTO dto, boolean optimizePopulation) {
        this.vehicles = new DNAConstructor(dto, optimizePopulation).getVehicles();
    }

    public double getFitness() {
        return fitness;
    }

    void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
}
