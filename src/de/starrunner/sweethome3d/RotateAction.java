package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Rotates the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class RotateAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of RotateAction.
   * 
   * @param plugin the parent plugin
   */
  public RotateAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "RotateAction", RotateAction.class.getClassLoader(),
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
   * Shows the rotate dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new RotateView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
