package ga;

import dto.ProblemDTO;
import model.Route;

import java.util.ArrayList;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class DNA {

    // The list of routes represents the gene/chromosome of the DNA.
    private ArrayList<Route> routes;
    private double fitness;

    DNA(ArrayList<Route> routes) {
        this.routes = routes;
    }

    DNA(ProblemDTO dto) {
        this.routes = new DNAConstructor(dto).getRoutes();
    }

    public double getFitness() {
        return fitness;
    }

    void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}
