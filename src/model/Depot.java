package model;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class Depot implements MapObject {
    private int id;
    private int maxRouteDuration;
    private int maxVehicleLoad;
    private int vehiclesUsed = 0;
    private int maxVehicles;
    private int adjustedX;
    private int adjustedY;
    private Integer x;
    private Integer y;

    public Depot(int maxRouteDuration, int maxVehicleLoad, int x, int y) {
        this.maxRouteDuration = maxRouteDuration;
        this.maxVehicleLoad = maxVehicleLoad;
        this.x = x;
        this.y = y;
    }

    public Depot(int maxRouteDuration, int maxVehicleLoad, int maxVehicles) {
        this.maxRouteDuration = maxRouteDuration;
        this.maxVehicleLoad = maxVehicleLoad;
        this.maxVehicles = maxVehicles;
    }

    public int getMaxRouteDuration() {
        return maxRouteDuration;
    }

    public void setMaxRouteDuration(int maxRouteDuration) {
        this.maxRouteDuration = maxRouteDuration;
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

    public int getVehiclesUsed() {
        return vehiclesUsed;
    }

    public void setVehiclesUsed(int vehiclesUsed) {
        this.vehiclesUsed = vehiclesUsed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
