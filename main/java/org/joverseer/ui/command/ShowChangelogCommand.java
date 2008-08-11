package org.joverseer.ui.command;

import java.util.Locale;

import org.joverseer.ui.views.ChangelogForm;
import org.joverseer.ui.views.CreditsForm;
import org.springframework.binding.form.FormModel;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;

/**
 * Shows the program changelog using the Changelog form
 * 
 * @author Marios Skounakis
 */
public class ShowChangelogCommand extends ActionCommand {
    public ShowChangelogCommand() {
        super("ShowChangelogCommand");
    }

    protected void doExecuteCommand() {
        Resource res = Application.instance().getApplicationContext().getResource("classpath:changelog.txt");
        FormModel formModel = FormModelHelper.createFormModel(res);
        final ChangelogForm form = new ChangelogForm(formModel);
        FormBackedDialogPage page = new FormBackedDialogPage(form);

        TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page) {
            protected void onAboutToShow() {
            }

            protected boolean onFinish() {
                return true;
            }

            protected Object[] getCommandGroupMembers() {
                return new AbstractCommand[] {
                        getFinishCommand()
                };
            }
        };
        MessageSource ms = (MessageSource)Application.services().getService(MessageSource.class);
        dialog.setTitle(ms.getMessage("changelogDialog.title", new Object[]{}, Locale.getDefault()));
        dialog.showDialog();
    }
}