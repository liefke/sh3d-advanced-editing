package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Adds the following actions to SweetHome 3D:
 * 
 * <ul>
 *   <li>Resize</li>
 *   <li>Rotate</li>
 *   <li>Edit room points</li>
 *   <li>Create walls around room</li>
 *   <li>Join walls</li>
 *   <li>Elevate furniture</li>
 * </ul>
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class AdvancedEditingPlugin extends Plugin {

  /**
   * @see Plugin#getActions()
   */
  @Override
  public PluginAction[] getActions() {
    return new PluginAction[] { new RotateAction(this), new MoveAction(this), new FlipAction(this),
        new ResizeAction(this), new EditRoomPointsAction(this),
        /*new CreateRoomWallsAction(this),*/new ConnectWallsAction(this), new ElevateAction(this) };
  }

}
