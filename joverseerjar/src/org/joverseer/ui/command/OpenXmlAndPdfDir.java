package org.joverseer.ui.command;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.joverseer.game.Game;
import org.joverseer.metadata.domain.Nation;
import org.joverseer.metadata.domain.NationAllegianceEnum;
import org.joverseer.support.GameHolder;
import org.joverseer.support.GamePreference;
import org.joverseer.support.TurnPostProcessor;
import org.joverseer.support.readers.newXml.TurnNewXmlReader;
import org.joverseer.support.readers.pdf.TurnPdfReader;
import org.joverseer.support.readers.xml.TurnXmlReader;
import org.joverseer.ui.JOverseerClientProgressMonitor;
import org.joverseer.ui.LifecycleEventsEnum;
import org.joverseer.ui.support.ActiveGameChecker;
import org.joverseer.ui.support.JOverseerEvent;
import org.springframework.binding.form.FormModel;
import org.springframework.context.MessageSource;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;

/**
 * Reads xml and pdf turn files from a given directory (and all subdirectories).
 * All files must be for the same turn, and this turn must either be the latest
 * turn of the game or a turn larger than the last turn of the game.
 * 
 * Xml files are read first, then pdf files
 * 
 * @author Marios Skounakis
 */
public class OpenXmlAndPdfDir extends ActionCommand implements Runnable {
	File[] files;
	JOverseerClientProgressMonitor monitor;
	GameHolder gh;
	TitledPageApplicationDialog dialog;

	public OpenXmlAndPdfDir() {
		super("openXmlAndPdfDirCommand");
		this.gh = (GameHolder) Application.instance().getApplicationContext().getBean("gameHolder");
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (Exception exc) {
			// do nothing
		}
		Game game = this.gh.getGame();
		if (game == null) {
			return;
		}
		int xmlCount = 0;
		int pdfCount = 0;
		boolean errorOccurred = false;
		for (File f : this.files) {
			if (f.getAbsolutePath().endsWith(".xml")) {
				try {
					this.monitor.subTaskStarted(String.format("Importing file '%s'.", new Object[] { f.getAbsolutePath() }));
					xmlCount++;
					final TurnXmlReader r = new TurnXmlReader(game, "file:///" + f.getCanonicalPath());
					r.setMonitor(this.monitor);
					//this may change game.metadata.NewXMLFormat!
					r.run();
					if (r.getErrorOccured()) {
						errorOccurred = true;
					}
					if (game.getMetadata().getNewXmlFormat()) {
						final TurnNewXmlReader xr = new TurnNewXmlReader(game, "file:///" + f.getCanonicalPath(), r.getTurnInfo().getNationNo());
						xr.setMonitor(this.monitor);
						xr.run();
						if (xr.getErrorOccured()) {
							errorOccurred = true;
						}
					}
				} catch (Exception exc) {
					int a = 1;
					this.monitor.subTaskStarted(exc.getMessage());
					// do nothing
					// todo fix
				}
			}

		}

		boolean warningOccurred = false;
		if (!game.getMetadata().getNewXmlFormat()) {
			for (File f : this.files) {
				if (f.getAbsolutePath().endsWith(".pdf")) {
					try {
						this.monitor.subTaskStarted(String.format("Importing file '%s'.", new Object[] { f.getAbsolutePath() }));
						pdfCount++;
						final TurnPdfReader r = new TurnPdfReader(game, f.getCanonicalPath());
						r.setMonitor(this.monitor);
						r.run();
						if (r.getErrorOccurred()) {
							warningOccurred = true;
						}
					} catch (Exception exc) {
						int a = 1;
						this.monitor.subTaskStarted(exc.getMessage());
						// do nothing
						// todo fix
					}
				}
			}
		}
		this.monitor.subTaskStarted("Read " + xmlCount + " xml files and " + pdfCount + " pdf files.");

		TurnPostProcessor turnPostProcessor = new TurnPostProcessor();
		turnPostProcessor.postProcessTurn(game.getTurn(game.getMaxTurn()));

		String globalMsg = "";
		if (errorOccurred) {
			globalMsg = "Serious errors occurred during the import. The game information may not be reliable.";
		} else if (warningOccurred) {
			globalMsg = "Some small errors occurred during the import. All vital information was imported but some secondary information from the pdf files was not parsed successfully.";
		} else {
			globalMsg = "Import was successful.";
		}
		this.monitor.setGlobalMessage(globalMsg);
		Application.instance().getApplicationContext().publishEvent(new JOverseerEvent(LifecycleEventsEnum.GameChangedEvent.toString(), this.gh.getGame(), this));

		this.monitor.done();
		this.dialog.setDescription("Processing finished.");

	}

	@Override
	protected void doExecuteCommand() {
		if (!ActiveGameChecker.checkActiveGameExists())
			return;
		MessageSource ms = (MessageSource) Application.services().getService(MessageSource.class);

		// check if allegiances have been set for all neutrals
		Game g = GameHolder.instance().getGame();
		boolean neutralNationExists = false;
		for (Nation n : g.getMetadata().getNations()) {
			if (n.getAllegiance().equals(NationAllegianceEnum.Neutral) && !n.getEliminated() && !n.getRemoved()) {
				neutralNationExists = true;
			}
		}
		if (g.containsParameter("StopAskingForAllegianceChanges") && "1".equals(g.getParameter("StopAskingForAllegianceChanges"))) {
			neutralNationExists = false;
		}
		if (neutralNationExists) {
			ConfirmationDialog dlg = new ConfirmationDialog(ms.getMessage("changeAllegiancesConfirmationDialog.title", new Object[] {}, Locale.getDefault()), ms.getMessage("changeAllegiancesConfirmationDialog.message", new Object[] {}, Locale.getDefault())) {
				@Override
				protected void onConfirm() {
					ChangeNationAllegiances cmd = new ChangeNationAllegiances();
					cmd.doExecuteCommand();
				}
			};
			dlg.setPreferredSize(new Dimension(500, 70));
			dlg.showDialog();
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		String lastDir = GamePreference.getValueForPreference("importDir", OpenGameDirTree.class);
		if (lastDir != null) {
			fileChooser.setCurrentDirectory(new File(lastDir));
		}
		if (fileChooser.showOpenDialog(Application.instance().getActiveWindow().getControl()) == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			GamePreference.setValueForPreference("importDir", file.getAbsolutePath(), OpenGameDirTree.class);
			final Runnable thisObj = this;
			class XmlAndPdfFileFilter implements FileFilter {
				@Override
				public boolean accept(File file1) {
					return !file1.isDirectory() && (file1.getName().endsWith(".pdf") || Pattern.matches("g\\d{3}n\\d{2}t\\d{3}.xml", file1.getName()));
				}
			}
			// files = file.listFiles(new XmlAndPdfFileFilter());
			this.files = getFilesRecursive(file, new XmlAndPdfFileFilter());
			FormModel formModel = FormModelHelper.createFormModel(this);
			this.monitor = new JOverseerClientProgressMonitor(formModel);
			FormBackedDialogPage page = new FormBackedDialogPage(this.monitor);
			this.dialog = new TitledPageApplicationDialog(page) {
				@Override
				protected void onAboutToShow() {
					OpenXmlAndPdfDir.this.monitor.taskStarted(String.format("Importing Directory '%s'.", new Object[] { file.getAbsolutePath() }), 100 * OpenXmlAndPdfDir.this.files.length);
					Thread t = new Thread(thisObj);
					t.start();
					// SwingUtilities.invokeLater(thisObj);
				}

				@Override
				protected boolean onFinish() {
					return true;
				}

				@Override
				protected Object[] getCommandGroupMembers() {
					return new AbstractCommand[] { getFinishCommand() };
				}
			};
			this.dialog.setTitle(ms.getMessage("importFilesDialog.title", new Object[] {}, Locale.getDefault()));
			this.dialog.showDialog();
		}
	}

	private File[] getFilesRecursive(File folder, FileFilter filter) {
		ArrayList<File> ret = new ArrayList<File>();
		File[] files1 = folder.listFiles(filter);
		if (files1.length > 0) {
			ret.addAll(Arrays.asList(files1));
			FileFilter folderFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			};
			for (File subfolder : folder.listFiles(folderFilter)) {
				ret.addAll(Arrays.asList(getFilesRecursive(subfolder, filter)));
			}
		}
		return ret.toArray(new File[] {});
	}

}
