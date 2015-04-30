package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Resizes the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ResizeAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of ResizeAction.
   * 
   * @param plugin the parent plugin
   */
  public ResizeAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "ResizeAction", ResizeAction.class.getClassLoader(),
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
   * Shows the resize dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new ResizeView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
