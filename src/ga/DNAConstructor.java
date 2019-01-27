package ga;

import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.Route;
import util.Util;

import java.util.ArrayList;

/**
 * Created by stgr99 on 26/01/2019.
 */
public class DNAConstructor {
    private ArrayList<Depot> depots;
    private ArrayList<Customer> customers;

    private int[] vehiclesUsed;
    private int maxVehicleLoad;
    private int totalMaxVehicles;
    private int numberOfVehiclesUsed = 0;

    DNAConstructor(ProblemDTO dto) {
        depots = new ArrayList<>(dto.getDepots());
        totalMaxVehicles = depots.get(0).getMaxVehicles() * depots.size();
        vehiclesUsed = new int[depots.size()];
        maxVehicleLoad = depots.get(0).getMaxVehicleLoad();
        customers = new ArrayList<>(dto.getCustomers());
    }

    /**
     * Sets all routes by placing customers on different routes with a start and end depot.
     */
    ArrayList<Route> getRoutes() {
        ArrayList<Route> routes = new ArrayList<>();

        while (customers.size() > 0) {
            if (oneVehicleLeft()) {
                return distributeCustomers(routes);
            }

            Route route = new Route();
            route.setStartDepot(getRandomStartDepot());

            int numberOfCustomersOnRoute = Util.getNumberOfCustomersOnRoute(customers, maxVehicleLoad);
            for (int i = 0; i < numberOfCustomersOnRoute; i++) {
                Customer randomCustomer = Util.getRandomCustomer(customers);
                route.addCustomer(randomCustomer);
                customers.remove(randomCustomer);
            }

            route.setEndDepot(Util.getRandomDepot(depots));
            routes.add(route);
        }

        return routes;
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

    /**
     * Distributes remaining customers on a random route,
     * given that the route has not exceeded it's maximum vehicle load.
     */
    private ArrayList<Route> distributeCustomers(ArrayList<Route> routes) {
        while (customers.size() > 0) {
            Customer randomCustomer = Util.getRandomCustomer(customers);
            Route route = Util.getRandomRoute(routes);

            int customersOnRoute = route.getCustomers().size();
            if (customersOnRoute < maxVehicleLoad) {
                route.addCustomer(randomCustomer);
                customers.remove(randomCustomer);
            }
        }
        return routes;
    }

    public ArrayList<Depot> getDepots() {
        return depots;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public int[] getVehiclesUsed() {
        return vehiclesUsed;
    }

    public int getMaxVehicleLoad() {
        return maxVehicleLoad;
    }

    public int getTotalMaxVehicles() {
        return totalMaxVehicles;
    }

    public int getNumberOfVehiclesUsed() {
        return numberOfVehiclesUsed;
    }
}
