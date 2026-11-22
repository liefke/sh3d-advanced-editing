package de.starrunner.sweethome3d.editing;

import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Edits the points of the selected {@link Room room} or {@link Polyline
 * polyline}.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class EditPointsAction extends PluginAction {
	private final AdvancedEditingPlugin	plugin;
	private ResourceBundle					resource;

	/**
	 * Creates a new instance of EditPointsAction.
	 * 
	 * @param plugin the parent plugin
	 */
	public EditPointsAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "EditPointsAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "PLAN_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(de.starrunner.sweethome3d.editing.AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/plan-edit-points.png")));
		putPropertyValue(Property.TOOL_BAR, true);

		// Reset panel if language changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		// Reset panel if unit changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		// Enable only if it is sommething selected
		plugin.getHome().addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(SelectionEvent selectionEvent) {
				setEnabled(true);
			}
		});
	}

	/**
	 * Shows the dialog.
	 * 
	 * @see PluginAction#execute()
	 */
	@Override
	public void execute() {
		new PointsView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport(), plugin).displayView(null);
	}
}
