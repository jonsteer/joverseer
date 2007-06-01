package org.joverseer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.support.ListListModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.layout.TableLayoutBuilder;
import org.springframework.richclient.progress.ProgressMonitor;


/**
 * A form that implements a Progress Monitor. Used for showing the progress for
 * importing xml and pdf files
 * 
 * @author Marios Skounakis
 */
public class JOverseerClientProgressMonitor extends AbstractForm implements ProgressMonitor {
    public static final String FORM_PAGE = "jOverseerClientProgressMonitor";
    JLabel taskName;
    JProgressBar taskProgress;
    JList taskSubtasks;
    ListListModel llm;
    JPanel panel;
    int progressMax = 100;
    int workBuffer;
    int lastWork = 0;
    JScrollPane scp;
    JTextArea globalMessage;
    
    public JOverseerClientProgressMonitor(FormModel formModel) {
        super(formModel, FORM_PAGE);
    }

    protected JComponent createFormControl() {
        // todo fix layout
        TableLayoutBuilder tlb = new TableLayoutBuilder();
        tlb.cell(taskName = new JLabel());
        taskName.setPreferredSize(new Dimension(350, 16));
        tlb.row();
        tlb.relatedGapRow();
        tlb.cell(taskProgress = new JProgressBar());
        taskProgress.setMaximum(progressMax);
        tlb.row();
        tlb.relatedGapRow();
        tlb.cell(new JLabel("Import steps"));
        tlb.row();
        taskSubtasks = new JList();
        taskSubtasks.setModel(llm = new ListListModel());
        taskSubtasks.setCellRenderer(new ProgressItemRenderer());
        scp = new JScrollPane(taskSubtasks);
        scp.setPreferredSize(new Dimension(350, 350));
        scp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tlb.cell(scp);
        tlb.relatedGapRow();
        tlb.row();
        globalMessage = new JTextArea();
        globalMessage.setWrapStyleWord(true);
        globalMessage.setLineWrap(true);
        JScrollPane scp = new JScrollPane(globalMessage);
        tlb.cell(scp);
        scp.setPreferredSize(new Dimension(350, 80));
        
        panel = tlb.getPanel();
        panel.setPreferredSize(new Dimension(370, 300));
        return panel;
    }

    private JButton getOkButton() {
        return this.getDefaultButton();
    }

    public void done() {
        getOkButton().setEnabled(true);
    }

    public boolean isCanceled() {
        return false;
    }

    public void setCanceled(boolean b) {
        // do nothing, not allowed
    }

    public void subTaskStarted(String string) {
        llm.add(string);
        int selIndex = llm.size() - 1;
        taskSubtasks.setSelectedIndex(selIndex);
        taskSubtasks.ensureIndexIsVisible(selIndex);
        panel.updateUI();
    }
    
    public void taskStarted(String string, int i) {
        getOkButton().setEnabled(false);
        taskProgress.setMaximum(i);
        taskName.setText(string);
        panel.updateUI();
    }
    
    public void setGlobalMessage(String msg) {
    	globalMessage.setText(msg);
    	panel.updateUI();
    }

    /**
     * Hack so that you can use multiple tasks with the same progress monitor
     * The idea is that if the task sets worked to a value smaller than the current value,
     * the new value is added
     * E.g:
     * - worked = 50, ok
     * - worked = 100, ok
     * - new process starts
     * - worked = 50 --> make progress = 100 + 50 -> 150
     * - worked = 100 --> make progress = 150 + 100 - 50 -> 200 
     */
    public void worked(int i) {
        if (lastWork > i) {
            lastWork = 0;
        }
        if (workBuffer > i) {
            workBuffer = workBuffer + i - lastWork;
            lastWork = i;
        } else {
            workBuffer = i;
        }
        taskProgress.setValue(workBuffer);
        panel.updateUI();
    }
    
    /**
     * Specialized renderer that renders items that start with the "Error" string with red font
     * @author Marios Skounakis
     */
    public class ProgressItemRenderer extends DefaultListCellRenderer implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, hasFocus());
            if (value != null && value.toString().startsWith("Error")) {
                lbl.setForeground(Color.RED);
            }
            return lbl;
        }
        
    }
}
