package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.Polyline;

/**
 * Wrapper for a {@link Polyline} to edit its points.
 *
 * Copyright (c) 2016 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class PolylinePoints implements PointsContainer {

  private final Polyline polyline;

  /**
   * Creates a new instance of PolylinePoints.
   *
   * @param polyline the edited polyline
   */
  public PolylinePoints(Polyline polyline) {
    this.polyline = polyline;
  }

  /**
   * The selected polyline.
   *
   * @return The polyline.
   */
  public Polyline getPolyline() {
    return polyline;
  }

  @Override
  public float[][] getPoints() {
    return polyline.getPoints();
  }

  @Override
  public void setPoints(float[][] newPoints) {
    polyline.setPoints(newPoints);
  }

  @Override
  public void addPoint(int index, float x, float y) {
    polyline.addPoint(x, y, index);
  }

  @Override
  public void removePoint(int index) {
    polyline.removePoint(index);
  }

  @Override
  public boolean isClosed() {
    return polyline.isClosedPath();
  }

  @Override
  public boolean isFilled() {
    return false;
  }

}
