/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dev.thource.runelite.dudewheresmystuff;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

class DudeWheresMyStuffPanel extends PluginPanel {
    private final ItemManager itemManager;
    private final DudeWheresMyStuffConfig config;

    /* This is the panel the tabs' respective panels will be displayed on. */
    private final JPanel display = new JPanel();
    private final Map<Tab, MaterialTab> uiTabs = new HashMap<>();
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);

    private boolean active;

    @Nullable
    private TabContentPanel activeTabPanel = null;

    @Inject
    DudeWheresMyStuffPanel(ItemManager itemManager, DudeWheresMyStuffConfig config, ConfigManager configManager,
                           CoinsManager coinsManager, CarryableManager carryableManager, MinigamesManager minigamesManager,
                           @Named("developerMode") boolean developerMode) {
        super(false);

        this.itemManager = itemManager;
        this.config = config;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        display.setBorder(new EmptyBorder(6, 6, 6, 6));

        tabGroup.setLayout(new GridLayout(0, 6, 7, 7));
        tabGroup.setBorder(new EmptyBorder(6, 6, 0, 6));

        add(tabGroup, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);

        addTab(Tab.OVERVIEW, new OverviewTabPanel(itemManager, config, this, coinsManager, carryableManager));

        addTab(Tab.COINS, new CoinsTabPanel(itemManager, config, this, coinsManager));
        addTab(Tab.CARRYABLE_STORAGE, new CarryableTabPanel(itemManager, config, this, carryableManager));
        addTab(Tab.MINIGAMES, new MinigamesTabPanel(itemManager, config, this, minigamesManager));

//        for (Tab tab : Tab.TABS) {
//            addTab(tab, new OverviewTabPanel(itemManager, config, this, coinsManager));
//        }
    }

    private void addTab(Tab tab, TabContentPanel tabContentPanel) {
        JPanel wrapped = new JPanel(new BorderLayout());
        wrapped.add(tabContentPanel, BorderLayout.NORTH);
        wrapped.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JScrollPane scroller = new JScrollPane(wrapped);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        scroller.getVerticalScrollBar().setBorder(new EmptyBorder(0, 9, 0, 0));
        scroller.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Use a placeholder icon until the async image gets loaded
        MaterialTab materialTab = new MaterialTab(new ImageIcon(), tabGroup, scroller);
        materialTab.setPreferredSize(new Dimension(30, 27));
        materialTab.setName(tab.getName());
        materialTab.setToolTipText(tab.getName());

        AsyncBufferedImage icon = itemManager.getImage(tab.getItemID(), tab.getItemQuantity(), false);
        Runnable resize = () ->
        {
            BufferedImage subIcon = icon.getSubimage(0, 0, 32, 32);
            materialTab.setIcon(new ImageIcon(subIcon.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        };
        icon.onLoaded(resize);
        resize.run();

        materialTab.setOnSelectEvent(() ->
        {
            activeTabPanel = tabContentPanel;

            tabContentPanel.update();
            return true;
        });

        uiTabs.put(tab, materialTab);
        tabGroup.addTab(materialTab);

        if (tab == Tab.OVERVIEW) {
            tabGroup.select(materialTab);
        }
    }

    void switchTab(Tab tab) {
        tabGroup.select(uiTabs.get(tab));
    }

    /**
     * Updates the active tab panel, if this plugin panel is displayed.
     */
    void update() {
        if (!active || activeTabPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(activeTabPanel::update);
    }

    @Override
    public void onActivate() {
        active = true;
        update();
    }

    @Override
    public void onDeactivate() {
        active = false;
    }

    public void softUpdate() {
        if (activeTabPanel == null || !(activeTabPanel instanceof StorageTabPanel)) return;

        ((StorageTabPanel<?, ?, ?>) activeTabPanel).softUpdate();
    }
}
