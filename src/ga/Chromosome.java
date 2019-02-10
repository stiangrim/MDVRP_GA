package ga;

import dto.ProblemDTO;
import model.Vehicle;

import java.util.ArrayList;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class Chromosome {

    // The list of vehicles represents the genes of the chromosome.
    private ArrayList<Vehicle> vehicles;

    // The total distance of all vehicles, represents the fitness value.
    private double fitness;

    Chromosome(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    Chromosome(ProblemDTO dto, boolean optimizePopulation) {
        this.vehicles = new ChromosomeConstructor(dto, optimizePopulation).getVehicles();
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
