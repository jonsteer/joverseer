package org.joverseer.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.joverseer.JOApplication;
import org.joverseer.domain.Character;
import org.joverseer.domain.PlayerInfo;
import org.joverseer.game.Game;
import org.joverseer.metadata.GameTypeEnum;
import org.joverseer.orders.export.OrderFileGenerator;
import org.joverseer.orders.export.OrderTextGenerator;
import org.joverseer.preferences.PreferenceRegistry;
import org.joverseer.support.GameHolder;
import org.joverseer.support.GamePreference;
import org.joverseer.support.PropertyComparator;
import org.joverseer.tools.OrderParameterValidator;
import org.joverseer.tools.OrderValidationResult;
import org.joverseer.tools.ordercheckerIntegration.OrderResultContainer;
import org.joverseer.tools.ordercheckerIntegration.OrderResultTypeEnum;
import org.joverseer.ui.LifecycleEventsEnum;
import org.joverseer.ui.ScalableAbstractForm;
import org.joverseer.ui.command.OpenGameDirTree;
import org.joverseer.ui.command.SaveGame;
import org.joverseer.ui.command.SelectPlayedNations;
import org.joverseer.ui.support.dialogs.ErrorDialog;
import org.joverseer.ui.support.dialogs.InputDialog;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.MessageDialog;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.progress.BusyIndicator;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.JCheckBox;

/**
 * Export/Submit orders form
 *
 * @author Marios Skounakis
 */
// TODO document better
public class ExportOrdersForm extends ScalableAbstractForm implements ClipboardOwner {
	public static int ORDERS_OK = 0;
	public static int ORDERS_NOT_OK = 1;

	JButton backBtn;
	JButton fwdBtn;
	
	JComboBox nation;
	JLabel currentNation;
	int numberOfControlledNations;
	int index;
	JTextArea orders;
	JCheckBox chkDontCloseOnFinish;
	JCheckBox chkShadowOrder;
	JLabel lblVersionValue = new JLabel("");
	JLabel lblFile = new JLabel();
	JLabel lblFileValue = new JLabel("");
	JLabel lblSent = new JLabel();
	JLabel lblSentValue = new JLabel("");
	OrderFileGenerator visibleOrdersGenerator;
	boolean ordersOk = false;
	boolean cancelExport = false;
	Integer oldSelectedNation = null;
	int orderCheckResult = 0;

	boolean uncheckedOrders = false;
	boolean ordersWithErrors = false;
	boolean ordersWithWarnings = false;
	boolean missingOrders = false;
	boolean duplicateSkillOrders = false;

	// bit of a hack to let anonyous class communicate back to this class.
	private boolean cancel= false;

	private boolean lastSaveWasNotCancelled= true;
	// injected dependencies
	GameHolder gameHolder;

	public ExportOrdersForm(FormModel model,GameHolder gameHolder) {
		super(model, "ExportOrdersForm");
		this.visibleOrdersGenerator = new OrderTextGenerator();
		this.gameHolder = gameHolder;
	}

	@Override
	protected void init() {
	}

	public boolean getReadyToClose() {
		return (!this.chkDontCloseOnFinish.isSelected()) && this.lastSaveWasNotCancelled;
	}
	private ArrayList<String> getNationItems() {
		Game g = this.gameHolder.getGame();
		ArrayList<String> ret = new ArrayList<String>();
		for (PlayerInfo pi : g.getTurn().getPlayerInfo()) {
			ret.add(g.getMetadata().getNationByNum(pi.getNationNo()).getName());
		}
		return ret;
	}

	private int getSelectedNationNo() {
		String nationName = this.currentNation.getText();
		Game g = this.gameHolder.getGame();
		return g.getMetadata().getNationByName(nationName).getNumber();
	}

	/**
	 * The comment below allows Eclipse Window Builder Pro to parse the GUI for us!
	 * @wbp.parser.entryPoint
	 */
	@Override
	protected JComponent createFormControl() {
		Game g = this.gameHolder.getGame();
		
		JPanel oPanel = new JPanel();
		oPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		oPanel.add(panel, BorderLayout.CENTER);

		this.orders = new JTextArea();
		this.orders.setWrapStyleWord(false);
		this.orders.setLineWrap(false);
		this.orders.setEditable(false);
		JScrollPane scp = new JScrollPane(this.orders);
		scp.setPreferredSize(new Dimension(500, 400));
		panel.add(scp, BorderLayout.CENTER);

		JPanel topPanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // so combos are above each other
		JPanel nationPanel = new JPanel();
		nationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.currentNation = new JLabel("");
		Font newLabelFont = new Font(this.currentNation.getFont().getName(),Font.BOLD,this.currentNation.getFont().getSize());
		this.currentNation.setFont(newLabelFont);
		this.setNationLabel(0);

		panel.add(topPanel, BorderLayout.NORTH);

		nationPanel.add(this.currentNation);

		JPanel pnlPlayerInfo = new JPanel();
		nationPanel.add(pnlPlayerInfo);
		pnlPlayerInfo.setLayout(new BoxLayout(pnlPlayerInfo, BoxLayout.X_AXIS));

		JPanel pnlVersion = new JPanel();
		pnlPlayerInfo.add(pnlVersion);
		pnlVersion.setLayout(new BoxLayout(pnlVersion, BoxLayout.Y_AXIS));

		JLabel lblVersion = new JLabel(Messages.getString("playerInfo.turnVersion")); //$NON-NLS-1$
		pnlVersion.add(lblVersion);
		lblVersion.setBackground(Color.WHITE);
		this.lblVersionValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlVersion.add(this.lblVersionValue);
		this.lblVersionValue.setHorizontalAlignment(SwingConstants.CENTER);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		pnlPlayerInfo.add(horizontalStrut);

		JPanel pnlSent = new JPanel();
		pnlPlayerInfo.add(pnlSent);
		pnlSent.setLayout(new BoxLayout(pnlSent, BoxLayout.Y_AXIS));

		this.lblSent = new JLabel(Messages.getString("playerInfo.ordersSentOn")); //$NON-NLS-1$
		pnlSent.add(this.lblSent);
		this.lblSent.setBackground(Color.WHITE);
		pnlSent.add(this.lblSentValue);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		pnlPlayerInfo.add(horizontalStrut_1);

		JPanel pnlFile = new JPanel();
		pnlPlayerInfo.add(pnlFile);
		pnlFile.setLayout(new BoxLayout(pnlFile, BoxLayout.Y_AXIS));

		this.lblFile = new JLabel(Messages.getString("playerInfo.lastOrderFile")); //$NON-NLS-1$
		pnlFile.add(this.lblFile);
		this.lblFile.setBackground(Color.WHITE);
		pnlFile.add(this.lblFileValue);

		setPlayerInfoItems();
		this.setOrderText();
		topPanel.add(nationPanel);

		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// use SaveAs to show user that the name can be changed and that it's not a final click...there's a popup.
		JButton save = new JButton(Messages.getString("standardActions.SaveAs"));
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAndSendOrders(false);
			}
		});
		save.setPreferredSize(this.uiSizes.newDimension(100/20, this.uiSizes.getHeight5()));
		buttonPanel.add(save);

		JButton ctc = new JButton(Messages.getString("standardActions.CopyToClipboard"));
		final ClipboardOwner clipboardOwner = this;
		ctc.setPreferredSize(this.uiSizes.newDimension(100/11, this.uiSizes.getHeight5()));
		ctc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection stringSelection = new StringSelection(ExportOrdersForm.this.orders.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, clipboardOwner);
			}
		});

		buttonPanel.add(ctc);
		
		this.chkShadowOrder = new JCheckBox(Messages.getString("ExportOrdersForm.chkShadowOrder.text"));
		this.chkShadowOrder.setToolTipText(Messages.getString("ExportOrdersForm.chkShadowOrder.toolTipText"));
		buttonPanel.add(this.chkShadowOrder);
		
		JPanel buttonPanel2 = new JPanel();
		buttonPanel2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		oPanel.add(buttonPanel2, BorderLayout.SOUTH);
		
		JButton selectNationsBtn = new JButton(Messages.getString("ExportOrdersForm.BtnSelectNations"));
		selectNationsBtn.setToolTipText(Messages.getString("ExportOrdersForm.BtnSelectNations.toolTipText"));
		selectNationsBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectPlayedNations spn = new SelectPlayedNations(ExportOrdersForm.this.gameHolder);
				spn.execute();
				ExportOrdersForm.this.arrowAction("reset");
			}
		});
		buttonPanel2.add(selectNationsBtn);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		buttonPanel2.add(horizontalStrut_2);
		
		this.backBtn = new JButton("<");
		buttonPanel2.add(this.backBtn);
		this.backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportOrdersForm.this.arrowAction("back");
			}
		});

		this.fwdBtn = new JButton(">");
		buttonPanel2.add(this.fwdBtn);
		this.fwdBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportOrdersForm.this.arrowAction("forward");
			}
		});
		
		this.chkDontCloseOnFinish = new JCheckBox(Messages.getString("ExportOrdersForm.chckbxNewCheckBox.text")); //$NON-NLS-1$
		this.chkDontCloseOnFinish.setToolTipText(Messages.getString("ExportOrdersForm.chkSendAnother.toolTipText")); //$NON-NLS-1$
		buttonPanel2.add(this.chkDontCloseOnFinish);
		
		this.index = 0;
		this.refreshArrows();
		
		return oPanel;
	}
	
	/**
	 * Used to cycle through the selected nations, adjusting all values accordingly
	 * @param type: 3 options "back", "forward" and "reset"
	 */
	private void arrowAction(String type) {
		if(type.equals("back")) this.index = this.index - 1;
		else if(type.equals("forward")) this.index = this.index + 1;
		else if(type.equals("reset")) this.index = 0;
		else return;
		this.setNationLabel(this.index);
		this.setOrderText();
		this.setPlayerInfoItems();
		this.refreshArrows();
	}
	
	private void refreshArrows() {
		this.backBtn.setEnabled(false);
		this.fwdBtn.setEnabled(false);
		System.out.println("Index: " + this.index);
		System.out.println("Max: " + this.numberOfControlledNations);
		if(this.index > 0) this.backBtn.setEnabled(true);
		if(this.index < this.numberOfControlledNations - 1) this.fwdBtn.setEnabled(true);
	}
	
	
	private void setOrderText() {
		int nationNo = getSelectedNationNo();
		if (this.oldSelectedNation == null || this.oldSelectedNation != nationNo) {
			this.orders.setText("");
			this.ordersOk = false;
			this.oldSelectedNation = nationNo;
		}
		this.generateOrders(ExportOrdersForm.this.visibleOrdersGenerator);
		this.setPlayerInfoItems();
	}
	
	private void setNationLabel(int ind) {
		Game g = this.gameHolder.getGame();
		if (g!=null) {
			int nationNo = g.getMetadata().getNationNo();
			PlayerInfo pi = g.getTurn().getPlayerInfo(nationNo);
			if (pi == null) return;
			int[] conNations = pi.getControlledNations();
			if(conNations == null) {
				this.currentNation.setText(g.getMetadata().getNationByNum(g.getMetadata().getNationNo()).getName());
				this.numberOfControlledNations = 1;
			}
			else {
				System.out.println("Current int num: " + conNations[ind]);
				this.currentNation.setText(g.getMetadata().getNationByNum(conNations[ind]).getName());
				this.numberOfControlledNations = conNations.length;
			}
		}
	}

	private void setPlayerInfoItems() {
		Game g = this.gameHolder.getGame();
		if (g!=null) {
			int nationNo = getSelectedNationNo();
			PlayerInfo pi = g.getTurn().getPlayerInfo(nationNo);
			if (pi == null) return;
            Date d = pi.getOrdersSentOn();
            if (d == null) {
    			this.lblVersionValue.setText(String.valueOf(pi.getTurnVersion()));
            	this.lblSentValue.setText("");
            	this.lblFileValue.setText("");
        		this.lblFileValue.setVisible(false);
                this.lblSentValue.setVisible(false);
        		this.lblFile.setVisible(false);
                this.lblSent.setVisible(false);
            } else {
            	this.lblSentValue.setText(new SimpleDateFormat().format(d));
    			this.lblVersionValue.setText(String.valueOf(pi.getTurnVersion()));
    			String file = String.valueOf(pi.getLastOrderFile());
    			final int truncate=40;
    			if (file.length() > truncate) {
    				file = "..." + file.substring(file.length() -truncate -1);
    			}
    			this.lblFileValue.setText(file);
        		this.lblFileValue.setVisible(true);
                this.lblSentValue.setVisible(true);
        		this.lblFile.setVisible(true);
                this.lblSent.setVisible(true);
            }
		}
	}

	private void generateOrders(OrderFileGenerator gen) {
		Game g1 = this.gameHolder.getGame();
		try {
			this.orders.setText(gen.generateOrderFile(g1, g1.getTurn(), getSelectedNationNo()));
			this.orders.setCaretPosition(0);
			this.orderCheckResult = validateOrders();
			this.ordersOk = true;
		} catch (Exception exc) {
			this.orders.setText(this.getMessage("ExportOrdersForm.error.UnexpectedError"));
			this.ordersOk = false;
		}

	}
	private void increaseVersionNumber(PlayerInfo pi) {
		pi.setTurnVersion(pi.getTurnVersion() + 1);
		//this.nation.setSelectedIndex(this.nation.getSelectedIndex());
	}

	
	String serverResponse;
	/**
	 *
	 * @param send
	 * @return false if cancelled.
	 */
	private boolean saveAndSendOrders(boolean send) {
		boolean sendAll = false;
		boolean cont = true;
		if (this.chkDontCloseOnFinish.isSelected()) {
			this.arrowAction("reset");;
			sendAll = true;
		}
		try {
			do {
				final boolean isOK = true;
				final boolean isNotOK = false;
				cont = true;
				this.cancel = false;
				if (!this.ordersOk)
					return isOK;
				if (!checkOrderValidity())
					return isNotOK;
				Game g = this.gameHolder.getGame();
				int nationNo = getSelectedNationNo();
				PlayerInfo pi = g.getTurn().getPlayerInfo(nationNo);
				
				//Adds 'shad' onto filename if checkbox ticked
				String shadowOrd = "";
				if (this.chkShadowOrder.isSelected()) shadowOrd = "SHADOW";
				
				String fname = String.format("me%02dv%d%s.%03d", getSelectedNationNo(), pi.getTurnVersion(), shadowOrd, g.getMetadata().getGameNo());
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("Game " + Integer.toString(g.getMetadata().getGameNo()), Integer.toString(g.getMetadata().getGameNo())));
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setApproveButtonText(getMessage("standardActions.Save"));
				fileChooser.setSelectedFile(new File(fname));
		
				String orderPathPref = PreferenceRegistry.instance().getPreferenceValue("submitOrders.defaultFolder");
				String lastDir = "";
				if ("importDir".equals(orderPathPref)) {
					lastDir = GamePreference.getValueForPreference("importDir", OpenGameDirTree.class);
				} else {
					lastDir = GamePreference.getValueForPreference("orderDir", ExportOrdersForm.class);
				}
				if (lastDir == null) {
					lastDir = GamePreference.getValueForPreference("importDir", OpenGameDirTree.class);
				}
				if (lastDir != null) {
					fileChooser.setCurrentDirectory(new File(lastDir));
				}
				if (fileChooser.showSaveDialog(Application.instance().getActiveWindow().getControl()) != JFileChooser.APPROVE_OPTION) {
					return isNotOK;
				}
				File file = fileChooser.getSelectedFile();
				if ("importDir".equals(orderPathPref)) {
					GamePreference.setValueForPreference("orderDir", file.getParent(), ExportOrdersForm.class);
				}
				FileWriter f = new FileWriter(file);
				OrderFileGenerator gen = new OrderFileGenerator();
				String txt = gen.generateOrderFile(g, g.getTurn(), getSelectedNationNo());
				txt = txt.replace("\n", System.getProperty("line.separator"));
				f.write(txt);
				f.close();
				pi.setLastOrderFile(file.getAbsolutePath());
				if (!send) {
					increaseVersionNumber(pi); //strangly slow
					MessageDialog md = new MessageDialog(getMessage("ExportOrdersForm.TurnExportedDialogTitle"),
								getMessage("ExportOrdersForm.TurnExportedDialogMessage", new Object[] { fileChooser.getSelectedFile() }));
					md.showDialog();
					autoSaveGameAccordingToPref();
				} 
				if (this.index + 1 == this.numberOfControlledNations) break;
				if (sendAll)this.arrowAction("forward");
			} while (sendAll);
			if(send){
				String prefMethod = PreferenceRegistry.instance().getPreferenceValue("submitOrders.method");
				Game g = this.gameHolder.getGame();
				if (sendAll) this.arrowAction("reset");

				String email = null;
				this.serverResponse = "";
				boolean[] successes = new boolean[this.numberOfControlledNations];
				do {
					int nationNo = getSelectedNationNo();
					PlayerInfo pi = g.getTurn().getPlayerInfo(nationNo);
					File file = new File(pi.getLastOrderFile());
					String fname = file.getName();
					if (prefMethod.equals("email")) {
						// send by email
						String recipientEmail = PreferenceRegistry.instance().getPreferenceValue("submitOrders.recipientEmail");
						String cmd = "bin\\mailSender\\MailSender.exe " + recipientEmail + " " + fname + " " + file.getCanonicalPath();
						this.logger.debug("Starting mail client with command " + cmd);
						BusyIndicator.showAt(Application.instance().getActiveWindow().getControl());
						Runtime.getRuntime().exec(cmd);
						increaseVersionNumber(pi);
	
						String msg = getMessage("ExportOrdersForm.OrdersSentByMailSuccessMessage", new Object[] { recipientEmail, file });
						BusyIndicator.clearAt(Application.instance().getActiveWindow().getControl());
						MessageDialog md = new MessageDialog(getMessage("ExportOrdersForm.TurnSubmittedDialogTitle"), msg);
						md.showDialog();
						pi.setOrdersSentOn(new Date());
						autoSaveGameAccordingToPref();
						return true;
					} else {
						// submit to meturn.com
						Preferences prefs = Preferences.userNodeForPackage(ExportOrdersForm.class);
						if (email == null) {
							email = prefs.get("useremail", "");
							String emailRegex = "^(\\p{Alnum}+(\\.|\\_|\\-)?)*\\p{Alnum}@(\\p{Alnum}+(\\.|\\_|\\-)?)*\\p{Alpha}$";
							InputDialog idlg = new InputDialog("ExportOrdersForm.SendTurnInputDialogTitle");
							idlg.init(getMessage("ExportOrdersForm.SendTurnInputDialogMessage"));
							idlg.setTitlePaneTitle(getMessage("ExportOrdersForm.SendTurnInputDialogPaneTitle"));
							JTextField emailText = new JTextField();
							idlg.addComponent(getMessage("ExportOrdersForm.SendTurnInputDialog.EmailAddress"), emailText);
							idlg.setPreferredSize(new Dimension(400, 80));
							emailText.setText(email);
							do {
								idlg.showDialog();
								if (!idlg.getResult()) {
									ErrorDialog.showErrorDialog("ExportOrdersForm.SendAbortedMessage");
									send = false;
									return false;
								}
								email = emailText.getText();
							} while (!Pattern.matches(emailRegex, email));
							prefs.put("useremail", email);
						}
						String name = this.gameHolder.getGame().getTurn().getPlayerInfo(g.getMetadata().getNationNo()).getPlayerName();
						if (name == null)
							name = "null";
						String acct = this.gameHolder.getGame().getTurn().getPlayerInfo(g.getMetadata().getNationNo()).getAccountNo();
						if (acct == null)
							acct = "null";
						String url = "http://www.meturn.com/cgi-bin/HUpload.exe";
						final PostMethod filePost = new PostMethod(url);
						Part[] parts = { new StringPart("emailaddr", email), new StringPart("name", name), new StringPart("account", acct), new FilePart(file.getName(), file) };
						filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
						// final GetMethod filePost = new
						// GetMethod("http://www.meturn.com/");
						HttpClient client = new HttpClient();
						client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
						BusyIndicator.showAt(Application.instance().getActiveWindow().getControl());
						int status = client.executeMethod(filePost);
						if (status == HttpStatus.SC_OK) {

							Scanner s = new Scanner(filePost.getResponseBodyAsStream()).useDelimiter("\\A");
							String result = s.hasNext() ? s.next() : "";

							if(result.indexOf("Submitting ...Success") == -1) {
								this.cancel = true;
							}
							else successes[this.index] = true;
							
							if(this.index == 0 || successes[this.index] == false) {
								this.serverResponse = result; 
							}

							if (!this.cancel) {
								increaseVersionNumber(pi);
								filePost.releaseConnection();
								pi.setOrdersSentOn(new Date());
							}
						} else {
							this.cancel=true;
							send = false;
							filePost.releaseConnection();
							this.logger.error(String.format("Status: %d",status ));
							ErrorDialog.showErrorDialog("Unexpected Error",String.format("Status: %d",status ));
						}
					}
					if (this.index + 1 == this.numberOfControlledNations) break;
					if (sendAll) this.arrowAction("forward");
				} while(sendAll);

				String temp = "<br/>";
				this.arrowAction("reset");
				for (int i = 0; i < this.numberOfControlledNations; i++) {
					if (i != 0) this.arrowAction("forward");
					temp += g.getMetadata().getNationByNum(this.getSelectedNationNo()) + ": ";
					temp += successes[i] ? "Success" : "Not Sent";
					temp += "<br/>";
				}
				
			    String bagBegin = this.serverResponse.substring(0,this.serverResponse.lastIndexOf('/')-8);
			    String bagEnd = this.serverResponse.substring(this.serverResponse.lastIndexOf('/')-8);
			    this.serverResponse = bagBegin + temp + bagEnd;
				
				final SubmitOrdersResultsForm frm = new SubmitOrdersResultsForm(FormModelHelper.createFormModel(new Object()));
				FormBackedDialogPage page = new FormBackedDialogPage(frm);
				TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page) {

					@Override
					protected void onAboutToShow() {
						try {
							frm.getJEditorPane().setText(ExportOrdersForm.this.serverResponse);
							this.setDescription(this.getMessage("ExportOrdersForm.OrdersSentByMETURNSuccessMessage", new Object[] { new File(g.getTurn().getPlayerInfo(getSelectedNationNo()).getLastOrderFile()) }));
							
							if(frm.getJEditorPane().getText().indexOf("Submitting ...Success") != -1) System.out.println("DOne");
							BusyIndicator.clearAt(Application.instance().getActiveWindow().getControl());
						} catch (Exception exc) {
							ExportOrdersForm.this.cancel = true;
							this.logger.error(exc);
						}
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
				dialog.setTitle(Messages.getString("submitOrdersDialog.title"));
				BusyIndicator.clearAt(Application.instance().getActiveWindow().getControl());
				dialog.showDialog();
			}
		} catch (Exception exc) {
			ErrorDialog.showErrorDialog(exc);
			exc.printStackTrace();

			this.cancel = true;
		} finally {
			BusyIndicator.clearAt(Application.instance().getActiveWindow().getControl());
		}
		autoSaveGameAccordingToPref();
		return !this.cancel;
	}

	@Override
	public void commit() {
		//These 2 lines remind players to submit diplo
		PlayerInfo pi = this.gameHolder.getGame().getTurn().getPlayerInfo(getSelectedNationNo());
		if (pi.isDiploDue() && !pi.isDiploSent()) JOptionPane.showMessageDialog(null, "Remember, a Diplo is due this turn, and you have yet to send one.");
		
		this.lastSaveWasNotCancelled = saveAndSendOrders(true);
	}

	private int validateOrders() {
		Game g = this.gameHolder.getGame();
		ArrayList<Character> chars = g.getTurn().getCharacters().findAllByProperty("nationNo", getSelectedNationNo());
		Collections.sort(chars, new PropertyComparator<Character>("id"));
		ArrayList<Character> toRemove = new ArrayList<Character>();
		for (Character ch : chars) {
			if (ch.getHealth() == null || ch.getHealth() == 0 || ch.getHexNo() <= 0) {
				toRemove.add(ch);
			}
		}
		chars.removeAll(toRemove);

		this.missingOrders = false;
		this.ordersWithErrors = false;
		this.uncheckedOrders = false;
		this.duplicateSkillOrders = false;
		this.ordersWithWarnings = false;

		OrderResultContainer orc = OrderResultContainer.instance();
		OrderParameterValidator validator = new OrderParameterValidator();
		for (Character ch : chars) {
			for (int i = 0; i < ch.getNumberOfOrders(); i++) {
				if (ch.getOrders()[i].isBlank()) {
					this.missingOrders = true;
				} else {
					if (orc.getResultsForOrder(ch.getOrders()[i]).size() == 0) {
						if (!g.getMetadata().getGameType().equals(GameTypeEnum.gameKS)) {
							this.uncheckedOrders = true;
						}
					} else {
						if (orc.getResultTypeForOrder(ch.getOrders()[i]) == OrderResultTypeEnum.Error) {
							this.ordersWithErrors = true;
						} else if (orc.getResultTypeForOrder(ch.getOrders()[i]) == OrderResultTypeEnum.Warning) {
							this.ordersWithWarnings = true;
						}
					}
					OrderValidationResult ovr = validator.checkForDuplicateSkillOrder(ch.getOrders()[i]);
					if (ovr != null) {
						this.duplicateSkillOrders = true;
					}
					ovr = validator.checkOrder(ch.getOrders()[i]);
					if (ovr != null && ovr.getLevel() == OrderValidationResult.ERROR) {
						this.ordersWithErrors = true;
					}
					for (int j = 0; j <= ch.getOrders()[i].getLastParamIndex(); j++) {
						ovr = validator.checkParam(ch.getOrders()[i], j);
						if (ovr != null && ovr.getLevel() == OrderValidationResult.ERROR) {
							this.ordersWithErrors = true;
						}
					}
				}
			}
		}

		if (this.missingOrders || this.uncheckedOrders || this.ordersWithErrors || this.duplicateSkillOrders || this.ordersWithWarnings)
			return ORDERS_NOT_OK;
		return ORDERS_OK;
	}

	private boolean checkOrderValidity() {
		this.cancelExport = false;
		if (this.orderCheckResult != ORDERS_OK) {
			if (this.missingOrders) {
				return ErrorDialog.showErrorDialog("ExportOrdersForm.error.CharactersMissingOrders");
			}
			if (this.duplicateSkillOrders) {
				return ErrorDialog.showErrorDialog("ExportOrdersForm.error.CharactersIssuingDuplicateSkillOrders");
			}
			if (this.chkShadowOrder.isSelected()) {
				ConfirmationDialog dlg = new ConfirmationDialog(getMessage("standardMessages.Warning"),
						getMessage("ExportOrdersForm.warning.ShadowOrder")) {
					@Override
					protected void onCancel() {
						super.onCancel();
						ExportOrdersForm.this.cancelExport = true;
					}

					@Override
					protected void onConfirm() {
					}

				};
				dlg.setPreferredSize(new Dimension(500,60));
				dlg.showDialog();
				if (this.cancelExport)
					return false;
			}
			if (this.ordersWithErrors) {

				this.cancelExport = false;
				ConfirmationDialog dlg = new ConfirmationDialog(getMessage("standardMessages.Warning"),
						getMessage("ExportOrdersForm.warning.OrdersWithErrors")) {
					@Override
					protected void onCancel() {
						super.onCancel();
						ExportOrdersForm.this.cancelExport = true;
					}

					@Override
					protected void onConfirm() {
					}

				};
				dlg.showDialog();
				if (this.cancelExport)
					return false;
			} else if (this.ordersWithWarnings) {
				this.cancelExport = false;
				ConfirmationDialog dlg = new ConfirmationDialog(getMessage("standardMessages.Warning"),
						getMessage("ExportOrdersForm.warning.OrdersWithWarnings")) {
					@Override
					protected void onCancel() {
						super.onCancel();
						ExportOrdersForm.this.cancelExport = true;
					}

					@Override
					protected void onConfirm() {
					}

				};
				dlg.showDialog();
				if (this.cancelExport)
					return false;

			}
			if (this.uncheckedOrders) {
				ConfirmationDialog dlg = new ConfirmationDialog(getMessage("standardMessages.Warning"),
						getMessage("ExportOrdersForm.warning.OrdersNotCheckedWithOC")) {
					@Override
					protected void onCancel() {
						super.onCancel();
						ExportOrdersForm.this.cancelExport = true;
					}

					@Override
					protected void onConfirm() {
					}
				};
				dlg.showDialog();
				if (this.cancelExport)
					return false;
			}
		}
		return true;

	}

	private void autoSaveGameAccordingToPref() {
		String pval = PreferenceRegistry.instance().getPreferenceValue("submitOrders.autoSave");
		if (pval.equals("yes")) {
			new SaveGame(this.gameHolder).execute();
		}
		JOApplication.publishEvent(LifecycleEventsEnum.OrderSaveToFileEvent, this);

	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
