package de.starrunner.sweethome3d;

/**
 * Marks an object which may be changed by {@link PointsModel}.
 *
 * Copyright (c) 2016 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public interface PointsContainer {

  /**
   * Finds the points in the object.
   * 
   * @return the list of points
   */
  float[][] getPoints();

  /**
   * Sets the points in the object.
   * 
   * @param newPoints the new list of points to set
   */
  void setPoints(float[][] newPoints);

  /**
   * Adds a point to the object.
   *
   * @param index the index of the new point
   * @param x the x part of the coordinate
   * @param y the y part of the coordinate
   */
  void addPoint(int index, float x, float y);

  /**
   * Removes a point from the object.
   *
   * @param index the index of the point
   */
  void removePoint(int index);

  /**
   * Indicates that this container is a closed path.
   *
   * @return {@code true} to close the path from the last point to the first point
   */
  boolean isClosed();

  /**
   * Indicates that this container has a background
   *
   * @return {@code true} to display a background for the object
   */
  boolean isFilled();

}
