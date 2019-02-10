package util;

import dto.CustomerDistanceDTO;
import dto.PositionDistanceDTO;
import ga.DNA;
import javafx.scene.paint.Color;
import model.Customer;
import model.Depot;
import model.MapObject;
import model.Vehicle;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by stgr99 on 25/01/2019.
 */
public class Util {

    private static Random random = new Random();

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

    public static Vehicle getRandomVehicle(ArrayList<Vehicle> vehicles) {
        int vehicleNumber = getRandomNumberInRange(0, vehicles.size() - 1);
        return vehicles.get(vehicleNumber);
    }

    public static DNA getRandomDNA(ArrayList<DNA> population) {
        int dnaNumber = getRandomNumberInRange(0, population.size() - 1);
        return population.get(dnaNumber);
    }

    // TODO: Hvert DEPOT skal ha en color
    public static Color getRandomColor(int colorNumber) {
        Color[] colors = {
                Color.GREEN, Color.YELLOW, Color.RED, Color.HOTPINK, Color.DARKORANGE, Color.BLUE,
                Color.SADDLEBROWN, Color.PURPLE, Color.TURQUOISE, Color.DARKSEAGREEN, Color.DEEPSKYBLUE,
                Color.DARKRED, Color.INDIANRED, Color.MEDIUMVIOLETRED, Color.ORANGERED, Color.PALEVIOLETRED,
                Color.BROWN, Color.PINK, Color.ORANGE, Color.MEDIUMPURPLE, Color.YELLOWGREEN, Color.DEEPPINK,
                Color.CADETBLUE, Color.CORNFLOWERBLUE, Color.DARKBLUE, Color.LIGHTSKYBLUE, Color.MIDNIGHTBLUE,
                Color.DEEPPINK, Color.CHARTREUSE, Color.FUCHSIA, Color.DARKSLATEBLUE, Color.BLACK, Color.AQUA,
                Color.CORAL, Color.CRIMSON, Color.DARKGOLDENROD, Color.DARKKHAKI, Color.DARKSALMON, Color.TOMATO,
                Color.BLANCHEDALMOND, Color.DARKOLIVEGREEN, Color.THISTLE, Color.TEAL, Color.TAN, Color.SLATEGREY};

        if (colorNumber > colors.length - 1) {
            return Color.color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        } else {
            return colors[colorNumber];
        }
    }

    public static void reverseCustomers(ArrayList<Customer> customers, int start, int end) {
        if (start < end) {
            ArrayList<Customer> subList = new ArrayList<>(customers.subList(start, end));
            Collections.reverse(subList);

            int n = 0;
            for (int i = start; i < end; i++) {
                customers.set(i, subList.get(n));
                n++;
            }
        }
    }

    public static Double getRoundedDouble(double number, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(number).setScale(decimalPlaces, RoundingMode.CEILING);
        return bd.doubleValue();
    }

    public static void deleteEmptyVehicles(ArrayList<Vehicle> vehicles) {
        ArrayList<Vehicle> vehiclesToDelete = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getCustomers().size() == 0) {
                vehiclesToDelete.add(vehicle);
            }
        }

        vehicles.removeAll(vehiclesToDelete);
    }

    public static ArrayList<Vehicle> deepCopyVehicles(ArrayList<Vehicle> vehicles) {
        ArrayList<Vehicle> deepCopy = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            deepCopy.add(new Vehicle(vehicle.getStartDepot(), vehicle.getEndDepot(), new ArrayList<>(vehicle.getCustomers()), vehicle.getVehicleNumber()));
        }
        return deepCopy;
    }

    public static double calculateDistance(MapObject object1, MapObject object2) {
        int x1 = object1.getAdjustedX();
        int x2 = object2.getAdjustedX();
        int y1 = object1.getAdjustedY();
        int y2 = object2.getAdjustedY();

        return Math.hypot(x1 - x2, y1 - y2);
    }

    public static double calculateVehicleDistance(Vehicle vehicle) {
        double totalDistance = 0.0;

        ArrayList<Customer> customers = vehicle.getCustomers();
        if (customers.size() == 0) {
            return totalDistance;
        }

        totalDistance += calculateDistance(vehicle.getStartDepot(), customers.get(0));
        for (int i = 1; i < customers.size(); i++) {
            totalDistance += calculateDistance(customers.get(i - 1), customers.get(i));
        }
        totalDistance += calculateDistance(customers.get(customers.size() - 1), vehicle.getEndDepot());

        return totalDistance;
    }

    public static double calculateVehicleDistance(Vehicle vehicle, Customer newCustomer) {
        double totalDistance = 0.0;

        ArrayList<Customer> customers = vehicle.getCustomers();
        if (customers.size() == 0) {
            totalDistance += calculateDistance(vehicle.getStartDepot(), newCustomer);
            totalDistance += calculateDistance(newCustomer, vehicle.getEndDepot());
            return totalDistance;
        }

        totalDistance += calculateDistance(vehicle.getStartDepot(), customers.get(0));
        for (int i = 1; i < customers.size(); i++) {
            totalDistance += Util.calculateDistance(customers.get(i - 1), customers.get(i));
        }
        totalDistance += Util.calculateDistance(customers.get(customers.size() - 1), newCustomer);
        totalDistance += Util.calculateDistance(newCustomer, vehicle.getEndDepot());

        return totalDistance;
    }

    public static double calculateVehicleDistance(Vehicle vehicle, Customer newCustomer, int position) {
        double totalDistance = 0.0;

        ArrayList<Customer> customers = vehicle.getCustomers();
        if (customers.size() == 0) {
            totalDistance += calculateDistance(vehicle.getStartDepot(), newCustomer);
            totalDistance += calculateDistance(newCustomer, vehicle.getEndDepot());
            return totalDistance;
        }

        if (position == 0) {
            totalDistance += calculateDistance(vehicle.getStartDepot(), newCustomer);
            totalDistance += calculateDistance(newCustomer, customers.get(0));
        } else {
            totalDistance += calculateDistance(vehicle.getStartDepot(), customers.get(0));
        }

        for (int i = 1; i < customers.size(); i++) {
            if (position == i) {
                totalDistance += calculateDistance(customers.get(i - 1), newCustomer);
            } else if (position == i - 1 && position != 0) {
                totalDistance += calculateDistance(newCustomer, customers.get(i - 1));
                totalDistance += calculateDistance(customers.get(i - 1), customers.get(i));
            } else {
                totalDistance += calculateDistance(customers.get(i - 1), customers.get(i));
            }
        }

        if (position == customers.size() - 1) {
            totalDistance += calculateDistance(newCustomer, customers.get(customers.size() - 1));
            totalDistance += Util.calculateDistance(customers.get(customers.size() - 1), vehicle.getEndDepot());
        } else if (position == customers.size()) {
            totalDistance += Util.calculateDistance(customers.get(customers.size() - 1), newCustomer);
            totalDistance += Util.calculateDistance(newCustomer, vehicle.getEndDepot());
        } else {
            totalDistance += Util.calculateDistance(customers.get(customers.size() - 1), vehicle.getEndDepot());
        }

        return totalDistance;
    }

    public static int calculateLoad(Vehicle vehicle, Customer newCustomer) {
        int load = 0;

        ArrayList<Customer> customers = vehicle.getCustomers();
        for (Customer customer : customers) {
            load += customer.getDemand();
        }
        load += newCustomer.getDemand();

        return load;
    }

    public static String leftPad(String originalString, int length, char padCharacter) {
        StringBuilder paddedString = new StringBuilder(originalString);

        if (originalString.length() == length) {
            return originalString;
        }

        int missingLength = length - originalString.length();
        for (int i = 0; i < missingLength; i++) {
            paddedString.insert(0, padCharacter);
        }

        return paddedString.toString();
    }

    public static PositionDistanceDTO getBestPositionOnVehicle(Vehicle vehicle, Customer newCustomer) {
        int bestVehiclePosition = 0;
        double bestVehicleDistance = Double.MAX_VALUE;

        ArrayList<Customer> customers = vehicle.getCustomers();
        for (int i = 0; i < customers.size() + 1; i++) {
            double vehicleDistance = Util.calculateVehicleDistance(vehicle, newCustomer, i);
            if (vehicleDistance < bestVehicleDistance) {
                bestVehicleDistance = vehicleDistance;
                bestVehiclePosition = i;
            }
        }

        return new PositionDistanceDTO(bestVehiclePosition, bestVehicleDistance);
    }

    public static Depot getClosestDepot(ArrayList<Depot> depots, MapObject mapObject) {
        Depot closestDepot = null;
        double minDistance = Double.MAX_VALUE;

        for (Depot depot : depots) {
            double distance = Util.calculateDistance(depot, mapObject);
            if (distance < minDistance) {
                minDistance = distance;
                closestDepot = depot;
            }
        }

        return closestDepot;
    }

    public static CustomerDistanceDTO getClosestCustomerDTO(ArrayList<Customer> customers, MapObject mapObject) {
        Customer closestCustomer = null;
        double minDistance = Double.MAX_VALUE;

        for (Customer customer : customers) {
            double distance = Util.calculateDistance(customer, mapObject);
            if (distance < minDistance) {
                minDistance = distance;
                closestCustomer = customer;
            }
        }

        return new CustomerDistanceDTO(closestCustomer, minDistance);
    }

    public static int calculateDuration(Vehicle vehicle) {
        int duration = 0;
        ArrayList<Customer> customers = vehicle.getCustomers();
        for (Customer customer : customers) {
            duration += customer.getServiceDuration();
        }

        return duration;
    }

    public static int calculateDuration(Vehicle vehicle, Customer closestCustomer) {
        int duration = 0;
        ArrayList<Customer> customers = vehicle.getCustomers();
        for (Customer customer : customers) {
            duration += customer.getServiceDuration();
        }
        duration += closestCustomer.getServiceDuration();

        return duration;
    }

    public static ArrayList<Customer> getCustomersFromVehicles(ArrayList<Vehicle> vehicles) {
        ArrayList<Customer> customers = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            customers.addAll(vehicle.getCustomers());
        }
        return customers;
    }

    public static void removeCustomersV2(ArrayList<Vehicle> vehicles, ArrayList<Customer> customersToDelete) {
        if (customersToDelete.size() > 0) {

            for (Customer customerToDelete : customersToDelete) {

                outerLoop:
                for (Vehicle vehicle : vehicles) {
                    ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();

                    for (Customer vehicleCustomer : vehicleCustomers) {

                        if (customerToDelete.getId() == vehicleCustomer.getId()) {
                            vehicleCustomers.remove(vehicleCustomer);
                            break outerLoop;
                        }

                    }
                }

            }

        }
    }

    public static void removeCustomers(ArrayList<Vehicle> vehicles, ArrayList<Customer> customersToDelete) {
        if (customersToDelete.size() > 0) {
            ArrayList<Customer> customersToRemove = new ArrayList<>();

            for (Vehicle vehicle : vehicles) {
                ArrayList<Customer> customers = vehicle.getCustomers();
                customersToRemove.clear();

                for (Customer customer : customers) {
                    if (customersToDelete.contains(customer)) {
                        customersToRemove.add(customer);
                    }
                }

                customers.removeAll(customersToRemove);
            }
        }
    }

    public static Depot getBestEndDepot(ArrayList<Depot> depots, Vehicle vehicle) {
        ArrayList<Customer> customers = vehicle.getCustomers();

        if (customers.size() == 0) {
            return Util.getClosestDepot(depots, vehicle.getStartDepot());
        } else {
            Customer lastCustomer = customers.get(customers.size() - 1);
            return Util.getClosestDepot(depots, lastCustomer);
        }
    }

    public static int getNumberOfCustomersOnVehicle(ArrayList<Customer> customers, int maxVehicleLoad) {
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

    public static Depot getRandomStartDepot(ArrayList<Depot> depots, ArrayList<Vehicle> vehicles) {
        if (vehicles.size() == 0) {
            return Util.getRandomDepot(depots);
        }

        int maxVehicles = depots.get(0).getMaxVehicles();
        Depot startDepot = null;
        Collections.shuffle(depots);

        for (Depot depot : depots) {
            int count = 0;
            for (Vehicle vehicle : vehicles) {
                if (depot == vehicle.getStartDepot()) {
                    count++;
                }
            }

            if (count < maxVehicles) {
                startDepot = depot;
                break;
            }
        }
        return startDepot;
    }

    public static int getNumberOfCustomers(ArrayList<Vehicle> vehicles) {
        int count = 0;
        for (Vehicle vehicle : vehicles) {
            count += vehicle.getCustomers().size();
        }
        return count;
    }
}
