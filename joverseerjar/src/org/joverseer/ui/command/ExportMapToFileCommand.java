package org.joverseer.ui.command;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import org.joverseer.game.Game;
import org.joverseer.support.GameHolder;
import org.joverseer.ui.JOverseerJIDEClient;
import org.joverseer.ui.map.MapPanel;
import org.joverseer.ui.support.ActiveGameChecker;
import org.joverseer.ui.support.Messages;
import org.joverseer.ui.support.dialogs.ErrorDialog;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.dialog.MessageDialog;
import org.w3c.dom.svg.SVGDocument;

/**
 * Exports the current map image to a file as a jpeg image
 *
 * @author Marios Skounakis
 *
 */
public class ExportMapToFileCommand  extends ActionCommand {

	//dependencies
	GameHolder gameHolder;
    public ExportMapToFileCommand(GameHolder gameHolder) {
        super("exportMapToFileCommand"); //$NON-NLS-1$
        this.gameHolder = gameHolder;
    }

    @Override
	protected void doExecuteCommand() {
    	if (!ActiveGameChecker.checkActiveGameExists()) return;
    	SVGDocument map = MapPanel.instance().getMap();
    	if (map == null) return;
    	JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setApproveButtonText(Messages.getString("ExportMapToFileCommand.Save")); //$NON-NLS-1$
        Preferences prefs = Preferences.userNodeForPackage(JOverseerJIDEClient.class);
        String saveDir = prefs.get("saveDir", null); //$NON-NLS-1$
        if (saveDir != null) {
            fileChooser.setCurrentDirectory(new File(saveDir));
        }
        Game game = this.gameHolder.getGame();
        fileChooser.setSelectedFile(new File(Messages.getString("ExportMapToFileCommand.gamefileprefix") + game.getMetadata().getGameNo() + Messages.getString("ExportMapToFileCommand.TurnAbb") + game.getCurrentTurn() + ".jpeg")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getAbsolutePath().endsWith(".svg"); //$NON-NLS-1$
			}

			@Override
			public String getDescription() {
				return Messages.getString("ExportMapToFileCommand.7"); //$NON-NLS-1$
			}

        });
        if (fileChooser.showSaveDialog(Application.instance().getActiveWindow().getControl()) == JFileChooser.APPROVE_OPTION) {
        	try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
//                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(map), 
                                     new StreamResult(new FileOutputStream(fileChooser.getSelectedFile())));

            }
        	catch (Exception exc) {
        		ErrorDialog.showErrorDialog(exc);
        	}
        }
    }

}
