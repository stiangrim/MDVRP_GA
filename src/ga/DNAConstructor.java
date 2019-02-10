package ga;

import dto.CustomerDistanceDTO;
import dto.PositionDistanceDTO;
import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.Vehicle;
import util.Util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by stgr99 on 26/01/2019.
 */
class DNAConstructor {
    private ProblemDTO dto;
    private ArrayList<Depot> depots;
    private ArrayList<Customer> customers;

    private int vehicleNumber = 0;
    private int maxVehicles;
    private int totalMaxVehicles;
    private int maxVehicleLoad;
    private double maxVehicleDuration;
    private boolean optimizePopulation;

    DNAConstructor(ProblemDTO dto, boolean optimizePopulation) {
        this.dto = dto;
        this.optimizePopulation = optimizePopulation;
        depots = new ArrayList<>(dto.getDepots());
        customers = new ArrayList<>(dto.getCustomers());

        maxVehicles = depots.get(0).getMaxVehicles();
        totalMaxVehicles = maxVehicles * depots.size();
        maxVehicleLoad = depots.get(0).getMaxVehicleLoad();
        maxVehicleDuration = depots.get(0).getMaxVehicleDuration();
    }

    ArrayList<Vehicle> getVehicles() {
        if (optimizePopulation) {
            ArrayList<Vehicle> vehicles = createVehicles();

            boolean solutionFound = addOptimizedCustomers(vehicles);
            while (!solutionFound) {
                vehicles = createVehicles();
                customers = new ArrayList<>(dto.getCustomers());
                solutionFound = addOptimizedCustomers(vehicles);
            }

            Util.deleteEmptyVehicles(vehicles);
            return vehicles;
        } else {
            return getVehiclesWithCustomers();
        }
    }

    private ArrayList<Vehicle> createVehicles() {
        ArrayList<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < maxVehicles; i++) {
            for (Depot depot : depots) {
                Vehicle vehicle = new Vehicle(depot, depot, vehicleNumber);
                vehicles.add(vehicle);
                vehicleNumber++;
            }
        }

        return vehicles;
    }

    private boolean addOptimizedCustomers(ArrayList<Vehicle> vehicles) {
        int failCount = 0;
        boolean triedToFixDNA = false;

        while (customers.size() > 0) {

            if (failCount > 1000) {
                return false;
            } else if (failCount > 500 && !triedToFixDNA) {
                fixDNA(vehicles);
                triedToFixDNA = true;
            }

            Vehicle randomVehicle = Util.getRandomVehicle(vehicles);
            Customer closestCustomer = getBestCustomer(customers, randomVehicle);

            int vehicleLoad = Util.calculateLoad(randomVehicle, closestCustomer);
            if (vehicleLoad > maxVehicleLoad) {
                failCount++;
                continue;
            }

            PositionDistanceDTO dto = Util.getBestPositionOnVehicle(randomVehicle, closestCustomer);
            int bestPosition = dto.getPosition();
            double distance = dto.getDistance();

            if (maxVehicleDuration != 0 && distance > maxVehicleDuration) {
                failCount++;
                continue;
            }

            randomVehicle.addCustomer(closestCustomer, bestPosition);
            randomVehicle.setEndDepot(Util.getBestEndDepot(depots, randomVehicle));
            randomVehicle.setLoad(vehicleLoad);
            customers.remove(closestCustomer);
        }

        return true;
    }

    private void fixDNA(ArrayList<Vehicle> vehicles) {
        if (Util.getNumberOfCustomers(vehicles) > 360) {
            System.out.println("0");
        }

        // Fill some vehicles with customers, if they have space for it.
        ArrayList<Integer> vehicleNumberGivers = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicleNumberGivers.contains(vehicle.getVehicleNumber())) {
                continue;
            }

            double duration = Util.calculateVehicleDistance(vehicle);
            ArrayList<Customer> customers = vehicle.getCustomers();

            if (duration <= maxVehicleDuration) {
                for (Vehicle otherVehicle : vehicles) {
                    if (otherVehicle == vehicle) {
                        continue;
                    }

                    ArrayList<Customer> otherCustomers = otherVehicle.getCustomers();
                    ArrayList<Customer> toBeRemoved = new ArrayList<>();
                    for (Customer otherCustomer : otherCustomers) {
                        PositionDistanceDTO dto = Util.getBestPositionOnVehicle(vehicle, otherCustomer);
                        int bestPosition = dto.getPosition();
                        double newDuration = dto.getDistance();
                        int load = Util.calculateLoad(vehicle, otherCustomer);

                        if (newDuration <= maxVehicleDuration && load <= maxVehicleLoad) {
                            vehicleNumberGivers.add(otherVehicle.getVehicleNumber());
                            customers.add(bestPosition, otherCustomer);
                            toBeRemoved.add(otherCustomer);
                        }
                    }
                    otherCustomers.removeAll(toBeRemoved);
                }
            }
        }

        if (Util.getNumberOfCustomers(vehicles) > 360) {
            System.out.println("1");
        }

        // Set last customer first, if it makes fitness better.
        for (Vehicle vehicle : vehicles) {
            double oldDistance = Util.calculateVehicleDistance(vehicle);

            ArrayList<Customer> customers = vehicle.getCustomers();
            if (customers.size() > 0) {
                Customer lastCustomer = customers.get(customers.size() - 1);

                customers.remove(lastCustomer);
                customers.add(0, lastCustomer);

                double newDistance = Util.calculateVehicleDistance(vehicle);
                if (newDistance > oldDistance) {
                    customers.remove(0);
                    customers.add(customers.size(), lastCustomer);
                }
            }
        }

        if (Util.getNumberOfCustomers(vehicles) > 360) {
            System.out.println("2");
        }

        // Check if customers overlaps vehicles, and can be rearranged.
        for (Vehicle vehicle : vehicles) {
            ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();
            ArrayList<Customer> customersToBeRemoved = new ArrayList<>();

            for (Customer vehicleCustomer : vehicleCustomers) {
                tryToRearrange(vehicles, vehicle, vehicleCustomer, customersToBeRemoved);
            }
            vehicleCustomers.removeAll(customersToBeRemoved);
        }
        Util.deleteEmptyVehicles(vehicles);

        if (Util.getNumberOfCustomers(vehicles) > 360) {
            System.out.println("3");
        }

        // Swap mutation
        for (Vehicle vehicle : vehicles) {
            ArrayList<Customer> customers = vehicle.getCustomers();

            if (customers.size() > 0) {
                for (int i = 0; i < 100; i++) {
                    int element1 = Util.getRandomNumberInRange(0, customers.size() - 1);
                    int element2 = Util.getRandomNumberInRange(0, customers.size() - 1);

                    double oldDistance = Util.calculateVehicleDistance(vehicle);
                    Collections.swap(customers, element1, element2);
                    double newDistance = Util.calculateVehicleDistance(vehicle);

                    if (newDistance >= oldDistance) {
                        Collections.swap(customers, element1, element2);
                    }
                }
            }
        }

        // Inverse mutation
        for (Vehicle vehicle : vehicles) {
            ArrayList<Customer> customers = vehicle.getCustomers();

            if (customers.size() > 0) {
                for (int i = 0; i < 100; i++) {
                    int start = Util.getRandomNumberInRange(0, customers.size() - 1);
                    int end = Util.getRandomNumberInRange(start, customers.size() - 1);

                    double oldDistance = Util.calculateVehicleDistance(vehicle);
                    Util.reverseCustomers(customers, start, end);
                    double newDistance = Util.calculateVehicleDistance(vehicle);

                    if (newDistance >= oldDistance) {
                        Util.reverseCustomers(customers, start, end);
                    }
                }
            }
        }
    }

    private void tryToRearrange(ArrayList<Vehicle> vehicles, Vehicle currentVehicle, Customer vehicleCustomer,
                                ArrayList<Customer> customersToBeRemoved) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle == currentVehicle) {
                continue;
            }

            ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();
            double oldDistance = Util.calculateVehicleDistance(vehicle);
            PositionDistanceDTO dto = Util.getBestPositionOnVehicle(vehicle, vehicleCustomer);
            int bestPosition = dto.getPosition();
            double newDistance = dto.getDistance();

            if (newDistance <= oldDistance) {
                if (Util.calculateLoad(vehicle, vehicleCustomer) > maxVehicleLoad) {
                    continue;
                }
                vehicleCustomers.add(bestPosition, vehicleCustomer);
                customersToBeRemoved.add(vehicleCustomer);
            }

        }
    }

    private Customer getBestCustomer(ArrayList<Customer> customers, Vehicle vehicle) {
        Customer bestCustomer = null;
        double minDistance = Double.MAX_VALUE;

        // Get closest customer to vehicle's start depot.
        ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();
        if (vehicleCustomers.size() == 0) {
            CustomerDistanceDTO dto = Util.getClosestCustomerDTO(customers, vehicle.getStartDepot());
            double distance = dto.getDistance();
            if (distance < minDistance) {
                minDistance = distance;
                bestCustomer = dto.getCustomer();
            }
        }

        // Get closest customer to every customer on vehicle
        for (Customer placedCustomer : vehicleCustomers) {
            CustomerDistanceDTO dto = Util.getClosestCustomerDTO(customers, placedCustomer);
            double distance = dto.getDistance();
            if (distance < minDistance) {
                minDistance = distance;
                bestCustomer = dto.getCustomer();
            }
        }

        return bestCustomer;
    }

    private ArrayList<Vehicle> getVehiclesWithCustomers() {
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        int numberOfVehicles = Util.getRandomNumberInRange(1, totalMaxVehicles);
        for (int i = 0; i < numberOfVehicles; i++) {
            vehicles.add(new Vehicle(Util.getRandomStartDepot(depots, vehicles), Util.getRandomDepot(depots), vehicleNumber++));
        }

        while (customers.size() > 0) {
            Collections.shuffle(vehicles);
            Customer randomCustomer = Util.getRandomCustomer(customers);

            boolean vehicleFound = false;
            for (Vehicle vehicle : vehicles) {
                int load = Util.calculateLoad(vehicle, randomCustomer);
                if (load > maxVehicleLoad) {
                    continue;
                }

                vehicle.addCustomer(randomCustomer);
                customers.remove(randomCustomer);
                vehicleFound = true;
                break;
            }

            if (!vehicleFound) {
                vehicles.add(new Vehicle(Util.getRandomStartDepot(depots, vehicles), Util.getRandomDepot(depots), vehicleNumber++));
            }
        }

        Util.deleteEmptyVehicles(vehicles);
        return vehicles;
    }
}
