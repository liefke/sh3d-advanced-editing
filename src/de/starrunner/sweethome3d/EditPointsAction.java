package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Edits the points of the selected {@link Room room} or {@link Polyline polyline}.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class EditPointsAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of EditPointsAction.
   * 
   * @param plugin the parent plugin
   */
  public EditPointsAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "EditPointsAction", EditPointsAction.class.getClassLoader(), true);
    this.plugin = plugin;
  }

  /**
   * Shows the dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new PointsView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
