package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Edits the points of the selected room.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class EditRoomPointsAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of EditPointsAction.
   * 
   * @param plugin the parent plugin
   */
  public EditRoomPointsAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "EditRoomPointsAction", EditRoomPointsAction.class.getClassLoader(),
        true);
    this.plugin = plugin;
  }

  /**
   * Shows the dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new RoomPointsView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
