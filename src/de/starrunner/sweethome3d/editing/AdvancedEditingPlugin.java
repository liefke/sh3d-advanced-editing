package de.starrunner.sweethome3d.editing;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Adds the following actions to SweetHome 3D:
 * 
 * <ul>
 * <li>Rotate</li>
 * <li>Move</li>
 * <li>Flip</li>
 * <li>Resize</li>
 * <li>Edit points</li>
 * <li>Join walls</li>
 * <li>Elevate furniture</li>
 * <li>Modify the whole floor plan of the home</li>
 * <li>Modify the current background image</li>
 * <li>Distribute the elevation evenly</li>
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
		return new PluginAction[] {
				new RotateAction(this),
				new MoveAction(this),
				new FlipAction(this),
				new ResizeAction(this),
				new EditPointsAction(this),
				new ConnectWallsAction(this),
				new ElevateAction(this),
				new ModifyHomeAction(this),
				new ModifyBackgroundAction(this),
				new DistributeElevationAction(this)};
	}

}
