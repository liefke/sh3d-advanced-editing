package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Moves the selected objects.
 *
 * Copyright (c) 2015 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class MoveAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of MoveAction.
   * 
   * @param plugin the parent plugin
   */
  public MoveAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "MoveAction", MoveAction.class.getClassLoader(),
        !plugin.getHome().getSelectedItems().isEmpty());
    this.plugin = plugin;

    // Enable only if a selection is available
    plugin.getHome().addSelectionListener(new SelectionListener() {
      @Override
      public void selectionChanged(SelectionEvent selectionEvent) {
        setEnabled(!selectionEvent.getSelectedItems().isEmpty());
      }
    });
  }

  /**
   * Shows the move dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new MoveView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
