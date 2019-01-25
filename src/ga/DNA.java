package ga;

import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.Route;
import util.Util;

import java.util.ArrayList;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class DNA {

    private ArrayList<Depot> depots;
    private ArrayList<Customer> customers;

    // The list of routes represents the gene/chromosome of the DNA.
    private ArrayList<Route> routes;

    private double fitness;

    private int[] vehiclesUsed;

    private int maxVehicleLoad;
    private int totalMaxVehicles;
    private int numberOfVehiclesUsed = 0;

    public DNA(ProblemDTO dto) {
        routes = new ArrayList<>();
        depots = new ArrayList<>(dto.getDepots());
        totalMaxVehicles = depots.get(0).getMaxVehicles() * depots.size();
        vehiclesUsed = new int[depots.size()];
        maxVehicleLoad = depots.get(0).getMaxVehicleLoad();
        customers = new ArrayList<>(dto.getCustomers());

        setRoutes();
    }

    public DNA(ArrayList<Route> routes) {
        this.routes = routes;
    }

    /**
     * Distributes remaining customers on a random route,
     * given that the route has not exceeded it's maximum vehicle load.
     */
    private void distributeCustomers() {
        while (customers.size() > 0) {
            Customer randomCustomer = Util.getRandomCustomer(customers);
            Route route = Util.getRandomRoute(routes);

            int customersOnRoute = route.getCustomers().size();
            if (customersOnRoute < maxVehicleLoad) {
                route.addCustomer(randomCustomer);
                customers.remove(randomCustomer);
            }
        }
    }

    /**
     * Sets all routes by placing customers on different routes with a start and end depot.
     */
    private void setRoutes() {
        while (customers.size() > 0) {
            if (oneVehicleLeft()) {
                distributeCustomers();
                return;
            }

            Route route = new Route();

            int numberOfCustomersOnRoute = Util.getNumberOfCustomersOnRoute(customers, maxVehicleLoad);
            for (int i = 0; i < numberOfCustomersOnRoute; i++) {
                Customer randomCustomer = Util.getRandomCustomer(customers);
                route.addCustomer(randomCustomer);
                customers.remove(randomCustomer);
            }

            route.setStartDepot(getRandomStartDepot());
            route.setEndDepot(Util.getRandomDepot(depots));
            routes.add(route);
        }
//        printRouteInfo();
    }

    private void printRouteInfo() {
        int tot = 0;
        for (Route route : routes) {
            tot += route.getCustomers().size();
            System.out.println("model.Route customer size: " + route.getCustomers().size());
        }

        System.out.println("model.Route size: " + routes.size());
        System.out.println("Total customers: " + tot + "\n");
    }

    private Depot getRandomStartDepot() {
        ArrayList<Depot> availableStartDepots = new ArrayList<>();

        for (int i = 0; i < depots.size(); i++) {
            if (vehiclesUsed[i] < depots.get(i).getMaxVehicles()) {
                availableStartDepots.add(depots.get(i));
            }
        }

        int depotNumber = Util.getRandomNumberInRange(0, availableStartDepots.size() - 1);
        Depot startDepot = availableStartDepots.get(depotNumber);

        for (int i = 0; i < depots.size(); i++) {
            if (depots.get(i).getId() == startDepot.getId()) {
                vehiclesUsed[i] += 1;
                break;
            }
        }

        numberOfVehiclesUsed++;
        return availableStartDepots.get(depotNumber);
    }

    private boolean oneVehicleLeft() {
        return (totalMaxVehicles - numberOfVehiclesUsed) == 1;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}
