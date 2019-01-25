package util;

import ga.DNA;
import model.Customer;
import model.Depot;
import model.Route;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

/**
 * Created by stgr99 on 25/01/2019.
 */
public class Util {

    public static ArrayList<String> getDataFiles() {
        ArrayList<String> dataFiles = new ArrayList<>();

        final File folder = new File("src/resources/data_files");
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                dataFiles.add(fileEntry.getPath());
            }
        }

        Collections.sort(dataFiles);
        return dataFiles;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min can't be greater than max");
        }

        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static Customer getRandomCustomer(ArrayList<Customer> customers) {
        int customerNumber = getRandomNumberInRange(0, customers.size() - 1);
        return customers.get(customerNumber);
    }

    public static Depot getRandomDepot(ArrayList<Depot> depots) {
        int depotNumber = getRandomNumberInRange(0, depots.size() - 1);
        return depots.get(depotNumber);
    }

    public static Route getRandomRoute(ArrayList<Route> routes) {
        int routeNumber = getRandomNumberInRange(0, routes.size() - 1);
        return routes.get(routeNumber);
    }

    public static DNA getRandomDNA(ArrayList<DNA> population) {
        int dnaNumber = getRandomNumberInRange(0, population.size() - 1);
        return population.get(dnaNumber);
    }

    public static int getNumberOfCustomersOnRoute(ArrayList<Customer> customers, int maxVehicleLoad) {
        int random;

        if (customers.size() == 0) {
            return 0;
        }

        if (customers.size() < maxVehicleLoad) {
            random = getRandomNumberInRange(1, customers.size());
        } else {
            random = getRandomNumberInRange(1, maxVehicleLoad);
        }

        if (random > customers.size()) {
            random = customers.size();
        }

        return random;
    }
}
