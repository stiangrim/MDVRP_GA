package util;

import dto.ProblemDTO;
import model.Customer;
import model.Depot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class FileHandler {

    private int maxVehicles = 0;
    private int numberOfCustomers = 0;
    private int numberOfDepots = 0;

    private ArrayList<Customer> customers;
    private ArrayList<Depot> depots;

    public ProblemDTO getFileInformation(String filePath) {
        customers = new ArrayList<>();
        depots = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            int i = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                // Sets variables given in the first line.
                if (i == 0) {
                    setVariables(line);
                }

                // model.Route information
                else if (i > 0 && i < numberOfDepots + 1) {
                    setRouteInformation(line);
                }

                // model.Customer information
                else if (i > numberOfDepots && i < numberOfDepots + numberOfCustomers + 1) {
                    setCustomerInformation(line);
                } else if (i > numberOfDepots + numberOfCustomers) {
                    setDepotInformation(line);
                }

                i++;
            }

            return new ProblemDTO(customers, depots);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setVariables(String line) {
        String[] variables = line.split("\\s+");

        maxVehicles = Integer.parseInt(variables[0]);
        numberOfCustomers = Integer.parseInt(variables[1]);
        numberOfDepots = Integer.parseInt(variables[2]);
    }

    private void setRouteInformation(String line) {
        String[] variables = line.split("\\s+");

        int maxDuration = Integer.parseInt(variables[0]);
        int maxLoad = Integer.parseInt(variables[1]);

        Depot depot = new Depot(maxDuration, maxLoad, maxVehicles);
        depots.add(depot);
    }

    private void setCustomerInformation(String line) {
        ArrayList<String> variables = new ArrayList<>(Arrays.asList(line.split("\\s+")));

        // First element in variables gives whitespace, due to file.
        if (variables.get(0).equals("")) {
            variables.remove(0);
        }

        int id = Integer.parseInt(variables.get(0));
        int x = Integer.parseInt(variables.get(1));
        int y = Integer.parseInt(variables.get(2));
        int serviceDuration = Integer.parseInt(variables.get(3));
        int demand = Integer.parseInt(variables.get(4));

        Customer customer = new Customer(id, x, y, serviceDuration, demand);
        customers.add(customer);
    }

    private void setDepotInformation(String line) {
        String[] variables = line.split("\\s+");

        int id = Integer.parseInt(variables[0]);
        int x = Integer.parseInt(variables[1]);
        int y = Integer.parseInt(variables[2]);

        for (Depot list : depots) {
            if (list.getX() == null) {
                list.setId(id);
                list.setX(x);
                list.setY(y);
                break;
            }
        }
    }
}
