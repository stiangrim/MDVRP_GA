package dto;

import model.Customer;
import model.Depot;

import java.util.ArrayList;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class ProblemDTO {
    private ArrayList<Customer> customers;
    private ArrayList<Depot> depots;

    public ProblemDTO(ArrayList<Customer> customers, ArrayList<Depot> depots) {
        this.customers = customers;
        this.depots = depots;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<Depot> getDepots() {
        return depots;
    }

}
