// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector.preferences;

import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetSelector;

import java.awt.event.ActionEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

interface PresetSelectedHandler {
    void presetSelected(TaggingPreset preset);
}

/**
 * The tagging presets search dialog (F3).
 * @since 3388
 */
public final class TaggingPresetSelectionSearch extends ExtendedDialog {

    private final PresetSelectedHandler presetSelectedHandler;
    private final TaggingPresetSelector selector;

    /**
     * Returns the unique instance of {@code TaggingPresetSearchDialog}.
     * @return the unique instance of {@code TaggingPresetSearchDialog}.
     */
    public static synchronized TaggingPresetSelectionSearch show(PresetSelectedHandler presetSelectedHandler) {
        // Ideally there would only be presets in this dialog that can be applied to CLOSED_WAY, but that is not easy to fix
        TaggingPresetSelectionSearch dialog = new TaggingPresetSelectionSearch(presetSelectedHandler);
        dialog.showDialog();

        return dialog;
    }

    private TaggingPresetSelectionSearch(PresetSelectedHandler presetSelectedHandler) {
        super(MainApplication.getMainFrame(), tr("Search presets"), tr("Select"), tr("Cancel"));

        this.presetSelectedHandler = presetSelectedHandler;
        setButtonIcons("dialogs/search", "cancel");
        configureContextsensitiveHelp("/Action/TaggingPresetSearch", true /* show help button */);
        selector = new TaggingPresetSelector(false, false);
        setContent(selector, false);
        SelectionEventManager.getInstance().addSelectionListener(selector);
        selector.setDblClickListener(e -> buttonAction(0, null));

        selector.init();
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
        if (buttonIndex == 0) {
            TaggingPreset preset = selector.getSelectedPreset();
            presetSelectedHandler.presetSelected(preset);
        }
    }
}
