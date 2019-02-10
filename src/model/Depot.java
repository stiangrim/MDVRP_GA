package model;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class Depot implements MapObject {
    private Integer x;
    private Integer y;
    private int id;
    private int adjustedX;
    private int adjustedY;
    private int maxVehicleLoad;
    private int maxVehicles;
    private double maxVehicleDuration;

    public Depot(int maxVehicleDuration, int maxVehicleLoad, int maxVehicles) {
        this.maxVehicleDuration = maxVehicleDuration;
        this.maxVehicleLoad = maxVehicleLoad;
        this.maxVehicles = maxVehicles;
    }

    public double getMaxVehicleDuration() {
        return maxVehicleDuration;
    }

    public void setMaxVehicleDuration(double maxVehicleDuration) {
        this.maxVehicleDuration = maxVehicleDuration;
    }

    public int getMaxVehicleLoad() {
        return maxVehicleLoad;
    }

    public void setMaxVehicleLoad(int maxVehicleLoad) {
        this.maxVehicleLoad = maxVehicleLoad;
    }

    public int getAdjustedX() {
        return adjustedX;
    }

    public void setAdjustedX(int adjustedX) {
        this.adjustedX = adjustedX;
    }

    public int getAdjustedY() {
        return adjustedY;
    }

    public void setAdjustedY(int adjustedY) {
        this.adjustedY = adjustedY;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public void setMaxVehicles(int maxVehicles) {
        this.maxVehicles = maxVehicles;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
