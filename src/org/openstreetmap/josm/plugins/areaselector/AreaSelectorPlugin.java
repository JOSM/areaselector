// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.areaselector.preferences.AreaSelectorPreference;

/**
 * This is the main class for the AreaSelector plugin.
 *
 */
public class AreaSelectorPlugin extends Plugin {

	AreaSelectorAction areaSelectorAction;

	AddressDialogAction addressDialogAction;

	public AreaSelectorPlugin(PluginInformation info) {
		super(info);

		setupLogging();

		areaSelectorAction = new AreaSelectorAction(Main.map);
		MainMenu.add(Main.main.menu.toolsMenu, areaSelectorAction);

		addressDialogAction = new AddressDialogAction(Main.map);
		MainMenu.add(Main.main.menu.toolsMenu, addressDialogAction);
	}

	/**
	 * Called when the JOSM map frame is created or destroyed.
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		areaSelectorAction.updateMapFrame(oldFrame, newFrame);
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new AreaSelectorPreference(this);
	}

	/**
	 * @return the areaSelectorAction
	 */
	public AreaSelectorAction getAreaSelectorAction() {
		return areaSelectorAction;
	}

	protected LoggerContext setupLogging(){
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel(Level.ERROR);
		builder.setConfigurationName("BuilderTest");
		builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
				.addAttribute("level", Level.DEBUG));
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
				ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(builder.newLayout("PatternLayout")
				.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
				.addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);
		//		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
		//				.add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
		builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Stdout")));
		LoggerContext ctx = Configurator.initialize(builder.build());
		return ctx;
	}
}
