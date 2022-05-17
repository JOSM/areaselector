// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

public class AreaSelectorPreference extends DefaultTabPreferenceSetting {

	PreferencesPanel prefPanel;

	public AreaSelectorPreference() {
		super("areaselector",
				"<html><p><b>"+tr("Area Selector - Preferences") + "</b></p></html>",
				tr("Settings for the area detection algorithm."));
	}

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		prefPanel = new PreferencesPanel();

		prefPanel.readPreferences();

		createPreferenceTabWithScrollPane(gui, prefPanel);
	}

	@Override
	public boolean ok() {
		prefPanel.savePreferences();
		return false;
	}
}
