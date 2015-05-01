package de.starrunner.sweethome3d;

import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.*;

import de.starrunner.components.event.AbstractObjectEdit;

/**
 * Elevates all selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ElevationEdit extends AbstractObjectEdit<List<ElevationEdit.FurnitureState>> {
  private static final long serialVersionUID = 2351221765188077761L;

  private final Home home;
  private final List<Selectable> selectedItems;
  private float elevation;

  /**
   * Creates a new instance of the resize edit for undo / redo .
   *
   * @param home the home
   */
  public ElevationEdit(Home home) {
    super(new ArrayList<FurnitureState>());
    // Save the current state of the selected items
    this.home = home;
    selectedItems = home.getSelectedItems();
    for (Selectable item : selectedItems) {
      if (item instanceof HomePieceOfFurniture) {
        target.add(new FurnitureState((HomePieceOfFurniture) item));
      }
    }
  }

  @Override
  public void doAction() {
    transform(elevation);
    home.setSelectedItems(selectedItems);
  }

  @Override
  public void undoAction() {
    transform(0);
    home.setSelectedItems(selectedItems);
  }

  @Override
  public String getPresentationName() {
    return Msg.msg("ElevationView.dialogTitle");
  }

  /**
   * Elevates all selected furntiture objects by the given amount.
   *
   * @param elevation the count of centimeters to elevate (or to drop for negative values)
   */
  public void elevate(float elevation) {
    this.elevation = elevation;
    transform(elevation);
  }

  private void transform(float elevation) {
    for (FurnitureState state : target) {
      state.furniture.setElevation(state.elevation + elevation);
    }
  }

  /**
   * Saves the state of a furniture.
   */
  public static final class FurnitureState {
    private final HomePieceOfFurniture furniture;
    private final float elevation;

    /**
     * Creates a new furniture state.
     *
     * @param furniture the associated furniture
     */
    public FurnitureState(HomePieceOfFurniture furniture) {
      this.furniture = furniture;
      elevation = furniture.getElevation();
    }

  }

}
