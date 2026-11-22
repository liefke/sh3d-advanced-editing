package de.starrunner.sweethome3d.editing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Modify the background image at selected level or home.
 *
 * Copyright (c) 2026 by Dorin Vatavu
 *
 * @author Dorin Vatavu
 */
public class ModifyBackgroundAction extends PluginAction {
	private final AdvancedEditingPlugin		plugin;
	private ResourceBundle						resource;
	private Level									trackedLevel;
	private final PropertyChangeListener	levelBackgroundListener;

	public ModifyBackgroundAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "ModifyBackgroundAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "PLAN_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/edit-image.png")));

		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		final Home home = this.plugin.getHome();

		this.levelBackgroundListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateEnabledState();
			}
		};

		// Listen for changes to Home's background image
		home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateEnabledState();
			}
		});

		// Listen for changes to the active level
		home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				attachLevelListener(home.getSelectedLevel());
				updateEnabledState();
			}
		});

		attachLevelListener(home.getSelectedLevel());
		updateEnabledState();
	}

	private void attachLevelListener(Level newLevel) {
		if (trackedLevel != null) { trackedLevel.removePropertyChangeListener(levelBackgroundListener); }
		trackedLevel = newLevel;
		if (trackedLevel != null) { trackedLevel.addPropertyChangeListener(levelBackgroundListener); }
	}

	private void updateEnabledState() {
		Home home = plugin.getHome();
		Level selectedLevel = home.getSelectedLevel();
		BackgroundImage image = selectedLevel != null
				? selectedLevel.getBackgroundImage()
				: home.getBackgroundImage();
		setEnabled(image != null);
	}

	@Override
	public void execute() {
		new ModifyBackgroundView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport(), plugin).displayView(null);
	}
}
