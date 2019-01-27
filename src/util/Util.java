package util;

import ga.DNA;
import javafx.scene.paint.Color;
import model.Customer;
import model.Depot;
import model.Route;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

/**
 * Created by stgr99 on 25/01/2019.
 */
public class Util {

    private static Random random = new Random();
    private static DecimalFormat df = new DecimalFormat();

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

    public static double getRandomDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Min can't be greater than max");
        }

        return min + random.nextDouble() * (max - min);
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min can't be greater than max");
        }

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

    public static Color getRandomColor(int colorNumber) {
        Color[] colors = {
                Color.GREEN, Color.YELLOW, Color.RED, Color.HOTPINK, Color.DARKORANGE, Color.YELLOWGREEN,
                Color.SADDLEBROWN, Color.PURPLE, Color.DEEPPINK, Color.DARKOLIVEGREEN, Color.DARKSEAGREEN,
                Color.DARKRED, Color.INDIANRED, Color.MEDIUMVIOLETRED, Color.ORANGERED, Color.PALEVIOLETRED,
                Color.BROWN, Color.PINK, Color.ORANGE, Color.MEDIUMPURPLE};

        if (colorNumber > colors.length) {
            return Color.color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        } else {
            return colors[colorNumber];
        }
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

    public static ArrayList<Customer> reverseCustomers(ArrayList<Customer> customers, int start, int end) {
        if (start < end) {
            ArrayList<Customer> subList = new ArrayList<>(customers.subList(start, end));
            Collections.reverse(subList);

            int n = 0;
            for (int i = start; i < end; i++)
            {
                customers.set(i, subList.get(n));
                n++;
            }
        }

        return customers;
    }

    public static Double getRoundedDouble(double number, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(number).setScale(decimalPlaces, RoundingMode.CEILING);
        return bd.doubleValue();
    }
}
