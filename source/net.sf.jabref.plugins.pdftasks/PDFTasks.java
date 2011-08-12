//
// JabRef PDFTasks Plugin
// Copyright (C) 2011  Karl Wette
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package net.sf.jabref.plugins.pdftasks;

import net.sf.jabref.GUIGlobals;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.SidePaneComponent;
import net.sf.jabref.SidePaneManager;
import net.sf.jabref.plugin.SidePanePlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

// PDFTasks plugin interface class
public class PDFTasks
    implements SidePanePlugin, ActionListener
{

    private SidePaneManager manager;
    private SidePaneComponent side_pane;
    private JMenuItem menu_item;

    public void init(JabRefFrame frame, SidePaneManager manager){

        // save side pane manager
        this.manager = manager;

        // create new side pane and register it with manager
        side_pane = new PDFTaskSidePane(frame, manager);
        manager.register(PDFTaskSidePane.class.getName(), side_pane);

        // create new menu item, add this class as a listener
        menu_item = new JMenuItem("PDF Tasks", new ImageIcon(GUIGlobals.getIconUrl("pdfSmall")));
        menu_item.addActionListener(this);

    }

    public SidePaneComponent getSidePaneComponent() {
        return side_pane;
    }

    public JMenuItem getMenuItem() {
        return menu_item;
    }

    // this method seems not to be used anywhere
    public String getShortcutKey() {
        return null;
    }

    public void actionPerformed(ActionEvent e) {

        // show side pane if plugin menu item is selected
        if (e.getSource() == menu_item) {
            manager.show(PDFTaskSidePane.class.getName());
        }

    }

}
