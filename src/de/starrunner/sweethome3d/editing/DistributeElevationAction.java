package de.starrunner.sweethome3d.editing;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

public class DistributeElevationAction extends PluginAction {
	private final AdvancedEditingPlugin	plugin;
	private ResourceBundle					resource;

	/**
	 * Checks if the given list contains at least 3 furniture pieces (including
	 * grouped pieces).
	 */
	private static final boolean isValidSelection(List<?> selectedItems) {
		int count = 0;
		for (Object item : selectedItems) {
			if (item instanceof HomeFurnitureGroup) {
				count += ((HomeFurnitureGroup) item).getAllFurniture().size();
			} else if (item instanceof PieceOfFurniture) { count++; }
			if (count >= 3) { return true; }
		}
		return false;
	}

	/**
	 * Creates a new instance of DistributeElevationAction.
	 * 
	 * @param plugin the parent plugin
	 */
	public DistributeElevationAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "DistributeElevationAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "FURNITURE_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/distribute-elevation.png")));

		// Reset labels if language or unit changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		// Enable only if at least three pieces of furniture are selected
		plugin.getHome().addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(SelectionEvent selectionEvent) {
				setEnabled(isValidSelection(selectionEvent.getSelectedItems()));
			}
		});
	}

	/**
	 * @see PluginAction#execute()
	 */
	@Override
	public void execute() {
		ElevationEdit edit = new ElevationEdit(plugin.getHome(), Msg.msg("DistributeElevationAction.NAME"));
		edit.distributeElevation();
		if (plugin.getUndoableEditSupport() != null) { plugin.getUndoableEditSupport().postEdit(edit); }
	}
}
