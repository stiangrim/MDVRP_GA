package ga;

import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.MapObject;
import model.Route;
import util.Util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class Population {

    private ArrayList<DNA> population;
    private ArrayList<DNA> matingPool;
    private ProblemDTO problemDTO;
    private int populationSize;
    private int generations;
    private double mutationRate;
    private double bestFitness = Integer.MAX_VALUE;
    private DNA bestDNA = null;

    public Population(ProblemDTO problemDTO, int populationSize, double mutationRate) {
        population = new ArrayList<>();
        matingPool = new ArrayList<>();

        this.problemDTO = problemDTO;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
    }

    public void setPopulation() {
        for (int i = 0; i < populationSize; i++) {
            DNA dna = new DNA(problemDTO);
            population.add(dna);
        }
    }

    public ArrayList<DNA> getPopulation() {
        return this.population;
    }

    public void calculateFitness() {
        for (DNA dna : population) {
            double totalFitness = 0.0;

            ArrayList<Route> routes = dna.getRoutes();
            for (Route route : routes) {
                ArrayList<Customer> customers = route.getCustomers();

                totalFitness += calculateDistance(route.getStartDepot(), customers.get(0));
                for (int i = 0; i < customers.size() - 1; i++) {
                    totalFitness += calculateDistance(customers.get(i), customers.get(i + 1));
                }
                totalFitness += calculateDistance(customers.get(customers.size() - 1), route.getEndDepot());
            }

            if (totalFitness < bestFitness) {
                bestFitness = totalFitness;
                bestDNA = dna;
            }

            dna.setFitness(totalFitness);
        }
    }

    private double calculateDistance(MapObject object1, MapObject object2) {
        int x1 = object1.getAdjustedX();
        int x2 = object2.getAdjustedX();
        int y1 = object1.getAdjustedY();
        int y2 = object2.getAdjustedY();

        return Math.hypot(x1 - x2, y1 - y2);
    }

    public DNA getBestDNA() {
        return this.bestDNA;
    }

    private DNA getBestDNA(ArrayList<DNA> population) {
        double bestFitness = (double) Integer.MAX_VALUE;
        DNA bestDNA = null;

        for (DNA dna : population) {
            if (dna.getFitness() < bestFitness) {
                bestFitness = dna.getFitness();
                bestDNA = dna;
            }
        }

        return bestDNA;
    }

    /**
     * Tournament Selection
     *
     * @param numberOfContestants number of random contestants to be ranked.
     */
    public void naturalSelection(int numberOfContestants) {
        matingPool.clear();
        ArrayList<DNA> contestants = new ArrayList<>();

        while (matingPool.size() < populationSize) {
            contestants.clear();
            for (int i = 0; i < numberOfContestants; i++) {
                contestants.add(Util.getRandomDNA(population));
            }
            matingPool.add(getBestDNA(contestants));
        }
    }

    public void crossover(boolean mutate) {
        population.clear();

        while (population.size() < populationSize) {
            DNA parent1 = Util.getRandomDNA(matingPool);
            DNA parent2 = Util.getRandomDNA(matingPool);

            ArrayList<DNA> children = crossover(parent1, parent2);
            DNA child1 = children.get(0);
            DNA child2 = children.get(1);

            if (mutate && Util.getRandomDouble(0, 1) < mutationRate) {
                mutate(child1);
                mutate(child2);
            }

            population.add(child1);
            population.add(child2);
        }

        generations++;
    }

    private void mutate(DNA dna) {
        ArrayList<Route> routes = dna.getRoutes();
        Route randomRoute = Util.getRandomRoute(routes);
        ArrayList<Customer> customers = randomRoute.getCustomers();

        double random = Util.getRandomDouble(0, 1);
        if (random > 0 && random < 1) {
            inverseMutation(customers);
        }
    }

    private void inverseMutation(ArrayList<Customer> customers) {
        int start = Util.getRandomNumberInRange(0, customers.size() - 1);
        int end = Util.getRandomNumberInRange(start, customers.size() - 1);
        Util.reverseCustomers(customers, start, end);
    }

    /**
     * Using a simple crossover method which generates two children with swapped elements from its parents.
     * <p>
     * Read more at: https://ac.els-cdn.com/S0307904X11005105/1-s2.0-S0307904X11005105-main.pdf?_tid=aadbca2e-34da-453f-a9c0-d1e58c6540b6&acdnat=1548518875_68c60a5c9eb10e75a2d1a6f2feef56f1
     *
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return The child which combines genes from both parents.
     */
    private ArrayList<DNA> crossover(DNA parent1, DNA parent2) {
        ArrayList<DNA> children = new ArrayList<>();

        Route parent1Route = Util.getRandomRoute(parent1.getRoutes());
        Route parent2Route = Util.getRandomRoute(parent2.getRoutes());

        ArrayList<Customer> child1Customers = new ArrayList<>(parent1Route.getCustomers());
        ArrayList<Customer> child2Customers = new ArrayList<>(parent2Route.getCustomers());

        child1Customers = getChildCustomers(child1Customers);
        child2Customers = getChildCustomers(child2Customers);

        Route child1Route = new Route(parent1Route.getStartDepot(), parent1Route.getEndDepot(), child1Customers);
        Route child2Route = new Route(parent2Route.getStartDepot(), parent2Route.getEndDepot(), child2Customers);

        ArrayList<Route> child1Routes = new ArrayList<>();
        ArrayList<Route> child2Routes = new ArrayList<>();

        for (Route route : parent1.getRoutes()) {
            if (route == parent1Route) {
                child1Routes.add(child1Route);
            } else {
                child1Routes.add(route);
            }
        }

        for (Route route : parent2.getRoutes()) {
            if (route == parent2Route) {
                child2Routes.add(child2Route);
            } else {
                child2Routes.add(route);
            }
        }

        children.add(new DNA(child1Routes));
        children.add(new DNA(child2Routes));
        return children;
    }

    private ArrayList<Customer> getChildCustomers(ArrayList<Customer> parentCustomers) {
        int element1 = Util.getRandomNumberInRange(0, parentCustomers.size() - 1);
        int element2 = Util.getRandomNumberInRange(0, parentCustomers.size() - 1);

        Collections.swap(parentCustomers, element1, element2);

        return parentCustomers;
    }

    private void deleteEmptyRoutes(DNA dna) {
        ArrayList<Route> routes = dna.getRoutes();
        ArrayList<Route> routesToDelete = new ArrayList<>();

        for (Route route : routes) {
            if (route.getCustomers().size() == 0) {
                routesToDelete.add(route);
            }
        }

        dna.getRoutes().removeAll(routesToDelete);
    }

    // routes: [2, 3, 5] [1, 8] [7, 4, 9] [6]
    // customersToDelete: [6, 7]
    private void removeCustomers(ArrayList<Route> routes, ArrayList<Customer> customersToDelete) {
        ArrayList<Customer> customersToRemove = new ArrayList<>();

        for (Route route : routes) {
            ArrayList<Customer> customers = route.getCustomers();
            customersToRemove.clear();

            for (Customer customer : customers) {
                if (customersToDelete.contains(customer)) {
                    customersToRemove.add(customer);
                }
            }

            customers.removeAll(customersToRemove);
        }
    }

    private Depot getStartDepot(Route parent1Route, Route parent2Route) {
        Depot startDepot;

        ArrayList<Depot> startDepots = new ArrayList<>();
        startDepots.add(parent1Route.getStartDepot());
        startDepots.add(parent2Route.getStartDepot());
        startDepot = Util.getRandomDepot(startDepots);

        return startDepot;
    }

    private Depot getEndDepot(Route parent1Route, Route parent2Route) {
        ArrayList<Depot> endDepots = new ArrayList<>();
        endDepots.add(parent1Route.getEndDepot());
        endDepots.add(parent2Route.getEndDepot());

        return Util.getRandomDepot(endDepots);
    }

    public int getGenerations() {
        return generations;
    }
}
