package de.starrunner.sweethome3d;

import java.util.Collection;

import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Elevates the selected furniture.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ElevateAction extends PluginAction {
  private final Plugin plugin;

  /**
   * Checks if the given list contains any furniture object.
   */
  private static final boolean containsFurniture(Collection<?> objects) {
    for (Object o : objects) {
      if (o instanceof PieceOfFurniture) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a new instance of ElevateAction.
   * 
   * @param plugin the parent plugin
   */
  public ElevateAction(Plugin plugin) {
    super("de.starrunner.sweethome3d.package", "ElevateAction", ElevateAction.class.getClassLoader(),
        containsFurniture(plugin.getHome().getSelectedItems()));
    this.plugin = plugin;

    // Enable only if a furniture is selected
    plugin.getHome().addSelectionListener(new SelectionListener() {
      @Override
      public void selectionChanged(SelectionEvent selectionEvent) {
        setEnabled(containsFurniture(selectionEvent.getSelectedItems()));
      }
    });
  }

  /**
   * Shows the elevate dialog.
   * 
   * @see PluginAction#execute()
   */
  @Override
  public void execute() {
    new ElevationView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport()).displayView(null);
  }

}
