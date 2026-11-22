package de.starrunner.sweethome3d.editing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginAction.Property;

/**
 * Preferences property listener bound to this plugin with a weak reference to
 * avoid strong link between preferences and this plugin.
 * 
 * @author Emmanuel Puybaret
 */
public class UserChangingPreferencesListener implements PropertyChangeListener {
	private WeakReference<AdvancedEditingPlugin>	plugin;
	private WeakReference<PluginAction>				pluginAction;
	private String											baseActionName;

	public UserChangingPreferencesListener(AdvancedEditingPlugin plugin, PluginAction pluginAction, String baseActionName) {
		this.plugin = new WeakReference<AdvancedEditingPlugin>(plugin);
		this.pluginAction = new WeakReference<PluginAction>(pluginAction);
		this.baseActionName = baseActionName;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// If plugin was garbage collected, remove this listener from preferences
		AdvancedEditingPlugin plugin = this.plugin.get();
		PluginAction pluginAction = this.pluginAction.get();
		UserPreferences preferences = (UserPreferences) evt.getSource();
		if (plugin == null) {
			preferences.removePropertyChangeListener(UserPreferences.Property.valueOf(evt.getPropertyName()), this);
		} else {
			ResourceBundle resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
					plugin.getPluginClassLoader());
			pluginAction.putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
			pluginAction.putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		}
	}

}
