package model;

/**
 * Created by stgr99 on 24/01/2019.
 */
public interface MapObject {
    int getId();

    void setId(int id);

    Integer getX();

    void setX(Integer x);

    Integer getY();

    void setY(Integer y);

    int getAdjustedX();

    void setAdjustedX(int adjustedX);

    int getAdjustedY();

    void setAdjustedY(int adjustedY);
}
