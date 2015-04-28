package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Flips the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 * Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Igor A. Perminov
 */
public class FlipAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Creates a new instance of FlipAction.
   * 
   * @param plugin the parent plugin
   */
  public FlipAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "FlipAction", FlipAction.class.getClassLoader(),
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
   * Shows the flip dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new FlipView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
