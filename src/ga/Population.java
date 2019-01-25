package ga;

import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.MapObject;
import model.Route;
import util.Util;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class Population {

    private ArrayList<DNA> population;
    private ArrayList<DNA> matingPool;
    private ProblemDTO problemDTO;
    private int populationSize;
    private double mutationRate;

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

            dna.setFitness(totalFitness);
//            System.out.println("Total fitness: " + totalFitness);
        }
    }

    private double calculateDistance(MapObject object1, MapObject object2) {
        int x1 = object1.getAdjustedX();
        int x2 = object2.getAdjustedX();
        int y1 = object1.getAdjustedY();
        int y2 = object2.getAdjustedY();

        return Math.hypot(x1 - x2, y1 - y2);
    }

    public DNA getBestDNA(ArrayList<DNA> population) {
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

        while (matingPool.size() < populationSize) {
            ArrayList<DNA> contestants = new ArrayList<>();
            for (int i = 0; i < numberOfContestants; i++) {
                contestants.add(Util.getRandomDNA(population));
            }
            matingPool.add(getBestDNA(contestants));
        }
    }

    public ArrayList<DNA> getPopulation() {
        return population;
    }

    public void crossover() {
        population.clear();

        while (population.size() < populationSize) {
            DNA parent1 = Util.getRandomDNA(matingPool);
            DNA parent2 = Util.getRandomDNA(matingPool);
            ArrayList<DNA> children = crossover(parent1, parent2);
            population.add(children.get(0));
            population.add(children.get(1));
        }
    }

    /**
     * Using Partially Mapped Crossover Operator.
     * Read more at: https://www.hindawi.com/journals/cin/2017/7430125/
     *
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return The child which combines genes from both parents.
     */
    private ArrayList<DNA> crossover(DNA parent1, DNA parent2) {
        ArrayList<DNA> children = new ArrayList<>();
        ArrayList<Route> parent1Routes = parent1.getRoutes();
        ArrayList<Route> parent2Routes = parent2.getRoutes();
        ArrayList<Route> childRoutes = new ArrayList<>();
        ArrayList<Customer> addedCustomers = new ArrayList<>();

        int minRoutes = Math.min(parent1Routes.size(), parent2Routes.size());
        int maxRoutes = Math.max(parent1Routes.size(), parent2Routes.size());
        int numberOfRoutes = Util.getRandomNumberInRange(minRoutes, maxRoutes);

        // Should consider only changing one depot, instead of everyone
        // CHANGING ONLY ONE ROUTE:
        numberOfRoutes = 1;

        for (int i = 0; i < numberOfRoutes; i++) {
            Route childRoute = combineRoutes(parent1Routes, parent2Routes, addedCustomers, i);
            childRoutes.add(childRoute);
        }

        return children;
    }

    private Route combineRoutes(ArrayList<Route> parent1Routes, ArrayList<Route> parent2Routes,
                                ArrayList<Customer> addedCustomers, int routeNumber) {
        Route parent1Route = null;
        Route parent2Route = null;

        if (parent1Routes.size() > routeNumber) {
            parent1Route = parent1Routes.get(routeNumber);
        }
        if (parent2Routes.size() > routeNumber) {
            parent2Route = parent2Routes.get(routeNumber);
        }

        Depot startDepot = getStartDepot(parent1Route, parent2Route);
        Depot endDepot = getEndDepot(parent1Route, parent2Route);
        ArrayList<Customer> customers = getCustomers(parent1Route, parent2Route, addedCustomers);

        return new Route(startDepot, endDepot, customers);
    }

    // TODO
    private ArrayList<Customer> getCustomers(Route parent1Route, Route parent2Route, ArrayList<Customer> addedCustomers) {
        ArrayList<Customer> customers = new ArrayList<>();

        if (parent1Route == null) {
            ArrayList<Customer> parent2Customers = parent2Route.getCustomers();
            for (Customer customer : parent2Customers) {
                if (!addedCustomers.contains(customer)) {
                    customers.add(customer);
                    addedCustomers.add(customer);
                }
            }
        } else if (parent2Route == null) {
            ArrayList<Customer> parent1Customers = parent1Route.getCustomers();
            for (Customer customer : parent1Customers) {
                if (!addedCustomers.contains(customer)) {
                    customers.add(customer);
                    addedCustomers.add(customer);
                }
            }
        } else {
            ArrayList<Customer> parent1Customers = parent1Route.getCustomers();
            ArrayList<Customer> parent2Customers = parent2Route.getCustomers();
            int minCustomerSize = Math.min(parent1Customers.size(), parent2Customers.size());

            // If at least one route only contains 1 customer.
            if (minCustomerSize == 1) {
                Customer firstCustomer = parent1Customers.get(0);

            }

            int cutPoint1 = Util.getRandomNumberInRange(0, minCustomerSize - 1);
            int cutPoint2 = Util.getRandomNumberInRange(cutPoint1, minCustomerSize - 1);

            // P1 = [3]
            // P3 = [2, 5, 9, 8]

            //




            // P1 = [3, 4, 7, 6]
            // P2 = [8, 9, 3, 2, 1, 7]

            // P1 = [3 | 4, 7 | 6]
            // P2 = [8 | 9, 3 | 2, 1, 7]

            // O1 = [x | 9, 3 | x]
            // O2 = [x | 4, 7 | x, x, x]

            // O1 = [x | 9, 3 | 6]
            // O2 = [8 | 4, 7 | 2, 1, x]

            // O1 = [8 | 9, 3 | 6]
            // O2 = [8 | 4, 7 | 2, 1, 3]


        }


        return customers;
    }

    private Depot getStartDepot(Route parent1Route, Route parent2Route) {
        Depot startDepot;

        if (parent1Route == null) {
            startDepot = Objects.requireNonNull(parent2Route).getStartDepot();
        } else if (parent2Route == null) {
            startDepot = parent1Route.getStartDepot();
        } else {
            ArrayList<Depot> startDepots = new ArrayList<>();
            startDepots.add(parent1Route.getStartDepot());
            startDepots.add(parent2Route.getStartDepot());
            startDepot = Util.getRandomDepot(startDepots);
        }

        return startDepot;
    }

    private Depot getEndDepot(Route parent1Route, Route parent2Route) {
        Depot endDepot;

        if (parent1Route == null) {
            endDepot = parent2Route.getEndDepot();
        } else if (parent2Route == null) {
            endDepot = parent1Route.getEndDepot();
        } else {
            ArrayList<Depot> endDepots = new ArrayList<>();
            endDepots.add(parent1Route.getEndDepot());
            endDepots.add(parent2Route.getEndDepot());
            endDepot = Util.getRandomDepot(endDepots);
        }

        return endDepot;
    }
}
