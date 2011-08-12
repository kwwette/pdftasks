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

import net.sf.jabref.AbstractWorker;
import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.SidePaneComponent;
import net.sf.jabref.SidePaneManager;
import net.sf.jabref.external.ExternalFileType;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;
import net.sf.jabref.util.XMPUtil;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.encryption.BadSecurityHandlerException;

import javax.xml.transform.TransformerException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// PDFTasks side pane class
public class PDFTaskSidePane
    extends SidePaneComponent
    implements ActionListener, ChangeListener
{

    private final static String title = "PDF Tasks";

    private final JabRefFrame frame;

    private final JButton do_tasks;
    private final JCheckBox rename_pdfs_chk;
    private final JCheckBox move_to_pdf_dir_chk;
    private final JTextField pdf_dir_txt;
    private final JCheckBox write_pdf_docinfo_chk;
    private final JCheckBox erase_pdf_docinfo_chk;

    public PDFTaskSidePane(JabRefFrame frame, SidePaneManager manager) {

        // initialise SidePaneComponent
        super(manager, GUIGlobals.getIconUrl("pdfSmall"), title);

        // save JabRef frame
        this.frame = frame;

        // create check box for renaming PDFs
        rename_pdfs_chk = new JCheckBox("Rename PDFs", true);

        // create check box and text field for moving PDFs task
        move_to_pdf_dir_chk = new JCheckBox("Move PDFs to directory:", true);
        move_to_pdf_dir_chk.addChangeListener(this);
        pdf_dir_txt = new JTextField();

        // create check box for writing erasing PDF document info
        write_pdf_docinfo_chk = new JCheckBox("Write PDF document information", true);
        write_pdf_docinfo_chk.addChangeListener(this);

        // create check box for erasing previous PDF document info
        erase_pdf_docinfo_chk = new JCheckBox("Erase previous document information", false);

        // create do task button
        do_tasks = new JButton("Perform PDF Tasks");
        do_tasks.addActionListener(this);

        // create pane and layout components
        JPanel pane = new JPanel();
        {
            GroupLayout grp = new GroupLayout(pane);
            pane.setLayout(grp);
            grp.setAutoCreateGaps(true);
            grp.setAutoCreateContainerGaps(true);
            grp.setHorizontalGroup(grp
                                   .createParallelGroup(GroupLayout.Alignment.CENTER)
                                   .addGroup(grp
                                             .createParallelGroup()
                                             .addComponent(rename_pdfs_chk)
                                             .addComponent(move_to_pdf_dir_chk)
                                             .addComponent(pdf_dir_txt)
                                             .addComponent(write_pdf_docinfo_chk)
                                             .addComponent(erase_pdf_docinfo_chk)
                                             )
                                   .addComponent(do_tasks)
                                   );
            grp.setVerticalGroup(grp
                                 .createSequentialGroup()
                                 .addComponent(rename_pdfs_chk)
                                 .addPreferredGap(rename_pdfs_chk,
                                                  move_to_pdf_dir_chk,
                                                  ComponentPlacement.UNRELATED)
                                 .addComponent(move_to_pdf_dir_chk)
                                 .addPreferredGap(move_to_pdf_dir_chk,
                                                  pdf_dir_txt,
                                                  ComponentPlacement.RELATED)
                                 .addComponent(pdf_dir_txt)
                                 .addPreferredGap(pdf_dir_txt,
                                                  write_pdf_docinfo_chk,
                                                  ComponentPlacement.UNRELATED)
                                 .addComponent(write_pdf_docinfo_chk)
                                 .addPreferredGap(
                                                  write_pdf_docinfo_chk,
                                                  erase_pdf_docinfo_chk,
                                                  ComponentPlacement.RELATED)
                                 .addComponent(erase_pdf_docinfo_chk)
                                 .addPreferredGap(erase_pdf_docinfo_chk,
                                                  do_tasks,
                                                  ComponentPlacement.UNRELATED)
                                 .addComponent(do_tasks)
                                 );
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(pane);

    }

    public void componentOpening() {

        // register this class to listen for when the user changes tabs
        frame.getTabbedPane().addChangeListener(this);

        // update pane
        updatePane();

    }

    public void componentClosing() {

        // deregister this class as a listener
        frame.getTabbedPane().removeChangeListener(this);

    }

    public void stateChanged(ChangeEvent e) {

        // update pane if user changed tabs
        if (e.getSource() == frame.getTabbedPane()) {
            updatePane();
        }

        // enable pdf_dir_txt iff move_to_pdf_dir_chk is selected
        if (e.getSource() == move_to_pdf_dir_chk) {
            pdf_dir_txt.setEnabled(move_to_pdf_dir_chk.isSelected());
        }

        // enable erase_pdf_docinfo_chk iff write_pdf_docinfo_chk is selected
        if (e.getSource() == write_pdf_docinfo_chk) {
            erase_pdf_docinfo_chk.setEnabled(write_pdf_docinfo_chk.isSelected());
        }

    }

    public void actionPerformed(ActionEvent e) {

        // do PDF tasks!
        if (e.getSource() == do_tasks) {
            doTasks();
        }

    }

    private void updatePane() {

        // PDF file type representation
        ExternalFileType pdf_type = Globals.prefs.getExternalFileTypeByExt("pdf");

        // get Bibtex database associated with the current tab
        BasePanel db_panel = frame.basePanel();
        BibtexDatabase db = db_panel.database();
        File db_file = db_panel.getFile();

        // build a map of where PDF files are stored
        HashMap<String, Integer> pdf_dir_count = new HashMap<String, Integer>();
        for (BibtexEntry entry : db.getEntries()) {

            // get table of file links for this Bibtex entry
            FileListTableModel files = new FileListTableModel();
            files.setContent(entry.getField(GUIGlobals.FILE_FIELD));

            for (int i = 0; i < files.getRowCount(); ++i) {
                FileListEntry file_entry = files.getEntry(i);

                // skip if this is not a PDF file link
                if (!file_entry.getType().equals(pdf_type))
                    continue;

                // parent directory of PDF file
                String pdf_dir = new File(file_entry.getLink()).getParent();

                // add to map and increment count
                if (pdf_dir_count.containsKey(pdf_dir)) {
                    pdf_dir_count.put(pdf_dir, pdf_dir_count.get(pdf_dir) + 1);
                }
                else {
                    pdf_dir_count.put(pdf_dir, 1);
                }

            }

        }

        // find directory where the majority of PDF files are stored
        String pdf_dir_max = null;
        int pdf_dir_max_count = 0;
        for (String pdf_dir : pdf_dir_count.keySet()) {
            if (pdf_dir_count.get(pdf_dir) > pdf_dir_max_count) {
                pdf_dir_max_count = pdf_dir_count.get(pdf_dir);
                pdf_dir_max = pdf_dir;
            }
        }

        // set PDF file directory field to this directory
        if (pdf_dir_max == null) {
            pdf_dir_txt.setText("");
        }
        else {
            File pdf_dir_max_file = absoluteFile(pdf_dir_max, db_file.getParentFile());
            String rel_pdf_dir = relativePath(pdf_dir_max_file, db_file.getParentFile());
            if (rel_pdf_dir != null) {
                pdf_dir_txt.setText(rel_pdf_dir);
            }
            else {
                pdf_dir_txt.setText(pdf_dir_max);
            }
        }

    }

    private void doTasks() {

        // PDF file type representation
        final ExternalFileType pdf_type = Globals.prefs.getExternalFileTypeByExt("pdf");

        // get selected Bibtex entries from the current tab
        final BasePanel db_panel = frame.basePanel();
        final BibtexDatabase db = db_panel.database();
        final BibtexEntry[] db_entries = db_panel.getSelectedEntries();

        // get Bibtex database file for current tab
        final File db_file = db_panel.getFile();
        if (db_file == null || db_file.getParentFile() == null) {
            JOptionPane.showMessageDialog(frame,
                                          "Bibtex database must be saved before performing PDF tasks.",
                                          title, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // return if no entries are selected
        if (db_entries.length == 0) {
            JOptionPane.showMessageDialog(frame,
                                          "No entries selected for PDF tasks.",
                                          title, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // get PDF file directory
        final File pdf_dir = absoluteFile(pdf_dir_txt.getText(), db_file.getParentFile());

        // do tasks encapsulated in a worker-thread class
        AbstractWorker tasks = new AbstractWorker()
            {

                boolean cancelled = false;
                boolean confirmed = false;
                boolean erred = false;

                // get user confirmation for PDF file modifications
                private boolean getUserConfirmation() {
                    if (!confirmed) {
                        confirmed = JOptionPane.
                            showConfirmDialog(frame,
                                              "Are you sure you want to rename, move, and/or modify PDF files?\n" +
                                              "This operations cannot be undone.",
                                              title, JOptionPane.YES_NO_OPTION)
                            == JOptionPane.YES_OPTION;
                    }
                    return confirmed;
                }

                public void init() {

                    // block main window
                    frame.block();

                }

                public void run() {

                    // for debugging purposes
                    final boolean modifyDatabase = true;

                    // iterate over selected Bibtex entries
                    int entry_count = 0;
                    for (BibtexEntry entry : db_entries) {
                        ++entry_count;

                        // get Bibtex key, check is not null
                        String key = entry.getCiteKey();
                        if (key == null || key.length() == 0) {
                            JOptionPane.showMessageDialog(frame,
                                                          "BibTeX entry '" + entry.getId() + "' does not have a key!",
                                                          title, JOptionPane.ERROR_MESSAGE);
                            erred = true;
                            return;
                        }

                        // update status bar
                        frame.output(String.format(
                                                   "Processing BibTeX entry: %s (%d of %d)...",
                                                   key, entry_count, db_entries.length
                                                   ));

                        // get table of file links for this Bibtex entry
                        FileListTableModel files = new FileListTableModel();
                        files.setContent(entry.getField(GUIGlobals.FILE_FIELD));

                        for (int fileindex = 0; fileindex < files.getRowCount(); ++fileindex) {
                            FileListEntry file_entry = files.getEntry(fileindex);

                            // skip if this is not a PDF file link
                            if (!file_entry.getType().equals(pdf_type))
                                continue;

                            // get PDF file
                            File pdf_file = absoluteFile(file_entry.getLink(), db_file.getParentFile());

                            // get PDF file description
                            String pdf_desc = file_entry.getDescription();

                            // new PDF file
                            File new_pdf_file = pdf_file;

                            // rename PDF file
                            if (rename_pdfs_chk.isSelected()) {

                                // build new PDF name
                                String new_name = key;
                                if (!pdf_desc.isEmpty()) {
                                    new_name += "_" + pdf_desc.replace(" ", "_");
                                }
                                new_name += "." + pdf_type.getExtension();

                                // set new PDF file
                                new_pdf_file = absoluteFile(new_name, new_pdf_file.getParentFile());

                            }

                            // move PDF file
                            if (move_to_pdf_dir_chk.isSelected()) {
                                new_pdf_file = absoluteFile(new_pdf_file.getName(), pdf_dir);
                            }

                            // if PDF file needs to be moved
                            if (!new_pdf_file.equals(pdf_file)) {

                                // get user confirmation
                                if (!getUserConfirmation()) {
                                    cancelled = true;
                                    return;
                                }

                                // perform move/rename operations
                                if (modifyDatabase) {
                                    String errmsg = "";
                                    try {

                                        // create parent directories
                                        File new_pdf_dir = new_pdf_file.getParentFile();
                                        if (new_pdf_dir != null && !new_pdf_dir.isDirectory()) {
                                            errmsg = "Could not create directory '" + new_pdf_file.getParentFile().getPath() + "'";
                                            erred = !new_pdf_file.getParentFile().mkdirs();
                                        }
                                        if (!erred) {

                                            // check if PDF file already exists, and ask for confirmation to replace it
                                            if (new_pdf_file.isFile()) {
                                                switch (JOptionPane.
                                                        showConfirmDialog(frame,
                                                                          "PDF file '" + new_pdf_file.getPath() + "' already exists.\n" +
                                                                          "Are you sure you want to replace it with " +
                                                                          "PDF file '" + pdf_file.getPath() + "'?\n" +
                                                                          "This operation cannot be undone.",
                                                                          title,
                                                                          JOptionPane.YES_NO_CANCEL_OPTION)
                                                        ) {
                                                case JOptionPane.NO_OPTION:
                                                    continue;
                                                case JOptionPane.CANCEL_OPTION:
                                                    cancelled = true;
                                                    return;
                                                case JOptionPane.YES_OPTION:
                                                    errmsg = "Could not delete PDF file '" + new_pdf_file.getPath() + "'";
                                                    erred = !new_pdf_file.delete();
                                                }
                                            }
                                            // otherwise test that we can create the new PDF file
                                            else {
                                                errmsg = "Could not access PDF file '" + new_pdf_file.getPath() + "'";
                                                erred = !new_pdf_file.createNewFile() || !new_pdf_file.delete();
                                            }

                                            if (!erred) {

                                                // try to move/rename PDF file
                                                errmsg = "Could not rename PDF file '" + pdf_file.getPath() +
                                                    "' to '" + new_pdf_file.getPath() + "'";
                                                erred = !pdf_file.renameTo(new_pdf_file);

                                            }

                                        }
                                    }

                                    // possible exceptions
                                    catch (SecurityException e) {
                                        erred = true;
                                        errmsg += ": insufficient permissions";
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                        erred = true;
                                        errmsg += ": an I/O exception occurred";
                                    }
                                    if (erred) {
                                         JOptionPane.showMessageDialog(frame, errmsg + ".", title, JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }

                                    // everything was successful
                                    pdf_file = new_pdf_file;

                                }
                            }

                            // update file entry table and Bibtex entry
                            file_entry.setLink(pdf_file.getPath());
                            if (modifyDatabase) {
                                String new_files = files.getStringRepresentation();
                                if (!new_files.equals(entry.getField(GUIGlobals.FILE_FIELD))) {
                                    entry.setField(GUIGlobals.FILE_FIELD, new_files);
                                    db_panel.markNonUndoableBaseChanged();
                                }
                            }

                            // perform operations on PDF file contents
                            if (write_pdf_docinfo_chk.isSelected()) {
                                
                                if (erase_pdf_docinfo_chk.isSelected()) {
                                    
                                    // get user confirmation
                                    if (!getUserConfirmation()) {
                                        cancelled = true;
                                        return;
                                    }
                                        
                                    // open PDF file
                                    PDDocument document = null;
                                    try {
                                        document = PDDocument.load(pdf_file);
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                        erred = true;
                                        JOptionPane.showMessageDialog(frame,
                                                                      "Could not open PDF file '" + pdf_file.getPath() +
                                                                      "': an I/O exception occurred.",
                                                                      title, JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    
                                    // erase document information
                                    document.setDocumentInformation(new PDDocumentInformation());
                                    
                                    // erase XML metadata
                                    document.getDocumentCatalog().setMetadata(null);
                                    
                                    // save and close PDF file
                                    try {
                                        document.save(pdf_file.getPath());
                                        document.close();
                                    }
                                    catch (COSVisitorException e) {
                                        e.printStackTrace();
                                        erred = true;
                                        JOptionPane.showMessageDialog(frame,
                                                                      "Could not save PDF file '" + pdf_file.getPath() +
                                                                      "': an exception occurred.",
                                                                      title, JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                        erred = true;
                                        JOptionPane.showMessageDialog(frame,
                                                                      "Could not save/close PDF file '" + pdf_file.getPath() +
                                                                      "': an I/O exception occurred.",
                                                                      title, JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    
                                }
                                
                                // write XMP / PDF document catalog metadata
                                try {
                                    XMPUtil.writeXMP(pdf_file, entry, db);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                    erred = true;
                                    JOptionPane.showMessageDialog(frame,
                                                                  "Could not write XMP to PDF file '" + pdf_file.getPath() +
                                                                  "': an I/O exception occurred.",
                                                                  title, JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                catch (TransformerException e) {
                                    e.printStackTrace();
                                    erred = true;
                                    JOptionPane.showMessageDialog(frame,
                                                                  "Could not write XMP to PDF file '" + pdf_file.getPath() +
                                                                  "': an exception occurred.",
                                                                  title, JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                            }

                        }

                    }

                }

                public void update() {

                    // unblock main window
                    frame.unblock();

                    // print to status bar
                    if (erred) {
                        frame.output("An error occurred during PDF Tasks");
                    }
                    else if (cancelled) {
                        frame.output("Cancelled PDF Tasks");
                    }
                    else {
                        frame.output("Completed PDF Tasks");
                    }

                }

            };

        // run task thread (based on code in BasePanel.runCommand())
        try {
            tasks.init();
            tasks.getWorker().run();
            tasks.getCallBack().update();
        }
        catch (Throwable e) {
            frame.unblock();
            e.printStackTrace();
        }

    }

    // return an absolute file for 'path' relative to 'base'
    private File absoluteFile(String path, File base) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(base, path);
        }
        try {
            return file.getCanonicalFile();
        }
        catch (IOException e) {
            return null;
        }
    }

    // determine the path of 'file' relative to 'base'
    private String relativePath(File file, File base) {

        // canonicalise input files, if possible
        try {
            if (file != null) {
                file = file.getCanonicalFile();
            }
            if (base != null) {
                base = base.getCanonicalFile();
            }
        }
        catch (IOException e) {
            return null;
        }
        if (file == null) {
            return null;
        }

        // split 'file' and 'base' into path components
        LinkedList<String> descend_path = new LinkedList<String>();
        while (base != null) {
            String name = base.getName();
            if (!name.isEmpty()) {
                descend_path.addFirst(name);
            }
            base = base.getParentFile();
        }
        LinkedList<String> ascend_path = new LinkedList<String>();
        while (file != null) {
            String name = file.getName();
            if (!name.isEmpty()) {
                ascend_path.addFirst(name);
            }
            file = file.getParentFile();
        }

        // remove common path components
        boolean common = false;
        while (!descend_path.isEmpty() && !ascend_path.isEmpty() &&
               descend_path.getFirst().equals(ascend_path.getFirst())) {
            descend_path.removeFirst();
            ascend_path.removeFirst();
            common = true;
        }

        // cannot make relative path if there are no common path components
        if (!common) {
            return null;
        }

        // build relative path string
        StringBuffer rel_path = new StringBuffer();
        for (int i = 0; i < descend_path.size(); ++i) {
            if (rel_path.length() > 0) {
                rel_path.append(File.separator);
            }
            rel_path.append("..");
        }
        for (String name : ascend_path) {
            if (rel_path.length() > 0) {
                rel_path.append(File.separator);
            }
            rel_path.append(name);
        }

        return rel_path.toString();

    }

}
