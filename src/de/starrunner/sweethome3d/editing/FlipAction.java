package de.starrunner.sweethome3d.editing;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Flips the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Igor A. Perminov
 */
public class FlipAction extends PluginAction {
	private final AdvancedEditingPlugin	plugin;
	private ResourceBundle					resource;

	/**
	 * Creates a new instance of FlipAction.
	 * 
	 * @param plugin the parent plugin
	 */
	public FlipAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "FlipAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "EDIT_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(de.starrunner.sweethome3d.editing.AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/plan-flip-objects.png")));
		putPropertyValue(Property.TOOL_BAR, true);

		// Reset panel if language changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		// Reset panel if unit changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		// Enable only if a valid selection is available
		plugin.getHome().addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(SelectionEvent selectionEvent) {
				setEnabled(hasValidSelection(selectionEvent.getSelectedItems()));
			}

			private boolean hasValidSelection(List<?> selectedItems) {
				for (Object item : selectedItems) {
					if (item != null && !(item instanceof ObserverCamera) && !(item instanceof Compass)) { return true; }
				}
				return false;
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
		new FlipView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport(), plugin).displayView(null);
	}

}
