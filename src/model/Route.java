package model;

import model.Customer;
import model.Depot;

import java.util.ArrayList;

/**
 * Created by stgr99 on 24/01/2019.
 */
public class Route {

    private Depot startDepot;
    private Depot endDepot;
    private ArrayList<Customer> customers;

    public Route() {
        customers = new ArrayList<>();
    }

    public Route(Depot startDepot, Depot endDepot, ArrayList<Customer> customers) {
        this.startDepot = startDepot;
        this.endDepot = endDepot;
        this.customers = customers;
    }

    public Depot getStartDepot() {
        return startDepot;
    }

    public void setStartDepot(Depot startDepot) {
        this.startDepot = startDepot;
    }

    public Depot getEndDepot() {
        return endDepot;
    }

    public void setEndDepot(Depot endDepot) {
        this.endDepot = endDepot;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }
}
