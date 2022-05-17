package org.openstreetmap.josm.plugins.areaselector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.jar.Attributes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;

/**
 * Test class for {@link AreaSelectorPlugin}
 * @author Taylor Smock
 */
@BasicPreferences
class AreaSelectorPluginTest {
    @RegisterExtension
    static JOSMTestRules josmTestRules = new JOSMTestRules().main().projection();

    /**
     * Non-regression test for JOSM #21902. This occurs when someone removes all layers, adds a new layer, and then
     * removes all layers again.
     */
    @Test
    void testNonRegression21902() throws PluginException {
        assertDoesNotThrow(() -> AreaSelectorPlugin.class.getDeclaredMethod("mapFrameInitialized", MapFrame.class, MapFrame.class), "AreaSelector used to implement mapFrameInitialized");
        final Attributes attributes = new Attributes();
        attributes.put(new Attributes.Name("Plugin-Mainversion"), Integer.toString(10_000));
        AreaSelectorPlugin plugin = new AreaSelectorPlugin(new PluginInformation(attributes, getClass().getSimpleName(), null));
        MainApplication.getMainPanel().addMapFrameListener(plugin);
        // We have to go through this twice. So let us do it a few extra times, just in case.
        for (int i = 1; i <= 5; i++) {
            final Layer testLayer = new OsmDataLayer(new DataSet(), getClass().getSimpleName(), null);
            assertDoesNotThrow(() -> MainApplication.getLayerManager().addLayer(testLayer), "Failed on cycle " + i);
            assertDoesNotThrow(() -> MainApplication.getLayerManager().removeLayer(testLayer), "Failed on cycle " + i);
        }
        assertDoesNotThrow(() -> plugin.mapFrameInitialized(null, null));
    }
}
