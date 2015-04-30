package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Displays a dialog that lets the user create walls around a room.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class CreateRoomWallsAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of CreateRoomWallsAction.
   *
   * @param plugin the parent plugin
   */
  public CreateRoomWallsAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "CreateRoomWallsAction", CreateRoomWallsAction.class.getClassLoader(),
        Home.getRoomsSubList(plugin.getHome().getSelectedItems()).size() == 1);
    this.plugin = plugin;

    // Enable only if a room is selected
    plugin.getHome().addSelectionListener(new SelectionListener() {
      @Override
      public void selectionChanged(SelectionEvent selectionEvent) {
        setEnabled(Home.getRoomsSubList(CreateRoomWallsAction.this.plugin.getHome().getSelectedItems()).size() == 1);
      }
    });
  }

  /**
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new RoomWallsView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
