package ch.xwr.seicentobilling.ui.desktop;

import org.apache.log4j.LogManager;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.security.authentication.jpa.HashStrategy;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPasswordField;
import com.xdev.ui.XdevTextArea;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.AppUserDAO;
import ch.xwr.seicentobilling.entities.AppUser;

public class PasswordPopup extends XdevView {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(PasswordPopup.class);

	private final AppUser user;

	/**
	 *
	 */
	public PasswordPopup() {
		super();
		this.initUI();

		// get Parameter
		this.user = (AppUser) UI.getCurrent().getSession().getAttribute("appuserbean");

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));
	}

	public static Window getPopupWindow() {
		final Window win = new Window();

		//win.setWidth("860");
		win.setWidthUndefined();
		//win.setHeight("450");
		win.setHeightUndefined();
		win.center();
		win.setModal(true);
		win.setContent(new PasswordPopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (isPasswordValid()) {
			final String pw = this.passwordField1.getValue();
			final byte[] encryptedPassword = new HashStrategy.SHA2().hashPassword(pw.getBytes());

			this.user.setPassword(encryptedPassword);

			try {
			    new AppUserDAO().save(this.user);

				Notification.show("Passwort", "Das Passwort wurde gespeichert", Notification.Type.TRAY_NOTIFICATION);
			    LOG.debug("New Password set for User " + this.user.getUsername());

				((Window) this.getParent()).close();
			} catch (final Exception e) {
				LOG.error("Could not set password for User " + this.user.getUsername(), e);
			}
		}
	}

	private boolean isPasswordValid() {
		final String pw1 = this.passwordField1.getValue();
		final String pw2 = this.passwordField2.getValue();

		if (pw1.equals(pw2)) {
			if (pw1.length() < 6) {
				Notification.show("Fehleingabe", "Passwort muss mindestens 6 Zeichen lang sein", Notification.Type.WARNING_MESSAGE);
				return false;
			}
			return true;
		}

		Notification.show("Fehleingabe", "Passwörter sind nicht gleich", Notification.Type.WARNING_MESSAGE);
		return false;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		((Window) this.getParent()).close();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.textArea = new XdevTextArea();
		this.horizontalLayoutUpload = new XdevHorizontalLayout();
		this.gridLayout = new XdevGridLayout();
		this.label2 = new XdevLabel();
		this.passwordField1 = new XdevPasswordField();
		this.label3 = new XdevLabel();
		this.passwordField2 = new XdevPasswordField();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdCancel = new XdevButton();

		this.panel.setIcon(FontAwesome.USER);
		this.panel.setCaption("Passwort setzen");
		this.panel.setTabIndex(0);
		this.verticalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.horizontalLayout2.setCaption("");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.textArea.setValue("Erlaubt das Setzen eines neuen Passwortes für den aktuellen User.");
		this.textArea.setReadOnly(true);
		this.textArea.setRows(2);
		this.horizontalLayoutUpload.setMargin(new MarginInfo(false));
		this.gridLayout.setMargin(new MarginInfo(false, true, false, true));
		this.label2.setDescription("Die Zahl entspricht dem Monat (z.B. 9 = September)");
		this.label2.setValue("Neues Passwort");
		this.label3.setDescription("Standardwert 15. Daten bis und mit dieser Zeile in Excel werden ignoriert.");
		this.label3.setValue("Wiederholen");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption("Schliessen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout2.addComponent(this.textArea);
		this.horizontalLayout2.setComponentAlignment(this.textArea, Alignment.MIDDLE_CENTER);
		this.horizontalLayout2.setExpandRatio(this.textArea, 100.0F);
		this.gridLayout.setColumns(2);
		this.gridLayout.setRows(3);
		this.label2.setSizeUndefined();
		this.gridLayout.addComponent(this.label2, 0, 0);
		this.passwordField1.setSizeUndefined();
		this.gridLayout.addComponent(this.passwordField1, 1, 0);
		this.label3.setSizeUndefined();
		this.gridLayout.addComponent(this.label3, 0, 1);
		this.passwordField2.setSizeUndefined();
		this.gridLayout.addComponent(this.passwordField2, 1, 1);
		this.gridLayout.setColumnExpandRatio(1, 10.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 2, 1, 2);
		this.gridLayout.setRowExpandRatio(2, 1.0F);
		this.gridLayout.setSizeFull();
		this.horizontalLayoutUpload.addComponent(this.gridLayout);
		this.horizontalLayoutUpload.setComponentAlignment(this.gridLayout, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutUpload.setExpandRatio(this.gridLayout, 10.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout2);
		this.horizontalLayoutUpload.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutUpload.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutUpload);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutUpload, 20.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 10.0F);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.panel.setWidth(100, Unit.PERCENTAGE);
		this.panel.setHeight(-1, Unit.PIXELS);
		this.setContent(this.panel);
		this.setWidth(760, Unit.PIXELS);
		this.setHeight(320, Unit.PIXELS);

		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label2, label3;
	private XdevButton cmdSave, cmdCancel;
	private XdevHorizontalLayout horizontalLayout2, horizontalLayoutUpload, horizontalLayout;
	private XdevTextArea textArea;
	private XdevPasswordField passwordField1, passwordField2;
	private XdevPanel panel;
	private XdevGridLayout gridLayout;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
