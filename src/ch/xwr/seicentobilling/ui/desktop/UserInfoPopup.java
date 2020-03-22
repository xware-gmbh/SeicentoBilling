package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Locale;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.xdev.security.authorization.Subject;
import com.xdev.server.aa.openid.auth.AzureUser;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.TheVersion;
import ch.xwr.seicentobilling.business.auth.SeicentoUser;

public class UserInfoPopup extends XdevView {
	private SeicentoUser currentUser;
	private Integer index;

	/**
	 *
	 */
	public UserInfoPopup() {
		super();
		this.initUI();

		final TheVersion ver = new TheVersion();
		this.labelVersion.setValue(ver.getEntryById("version"));
		this.labelArtifact.setValue(ver.getEntryById("groupId") + " " + ver.getEntryById("artifactId") );
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("1020");
		win.setHeight("700");
		win.center();
		win.setModal(true);
		win.setContent(new UserInfoPopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevGridLayout}
	 * {@link #gridLayoutUsr}.
	 *
	 * @see ClientConnector.AttachListener#attach(ClientConnector.AttachEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void gridLayoutUsr_attach(final ClientConnector.AttachEvent event) {
		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);

		if (sub != null && sub instanceof AzureUser)
		{
			this.currentUser = new SeicentoUser();
			this.currentUser.setAzureUser((AzureUser) sub);
			showInfos();
		} else if (sub != null && sub instanceof SeicentoUser){
			this.currentUser = (SeicentoUser) sub;
			showInfos();
		}

	}


	private void showInfos() {
		if (this.currentUser != null && this.currentUser.getClaimSet().getClaims() != null)
		{
			this.index = 0;
			final XdevGridLayout newGrid = new XdevGridLayout();
			this.currentUser.getClaimSet().getClaims().forEach((k,v) -> {
				newGrid.addComponent(new Label(k), 0, this.index);
				newGrid.addComponent(new Label(v.toString()), 1, this.index);
				this.index++;
			});
			this.gridLayoutUsr.addComponent(newGrid);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevGridLayout}
	 * {@link #gridLayoutApp}.
	 *
	 * @see ClientConnector.AttachListener#attach(ClientConnector.AttachEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void gridLayoutApp_attach(final ClientConnector.AttachEvent event) {
		final Locale currentLocale = Locale.getDefault();
		this.labelCountry.setValue(currentLocale.getDisplayCountry());
		this.labelLanguage.setValue(currentLocale.getDisplayLanguage());

		final Calendar cal = Calendar.getInstance();
		this.labelTimeZone.setValue(cal.getTimeZone().getID());

		this.labelUsername.setValue(Seicento.getUserName());
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #button}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void button_buttonClick(final Button.ClickEvent event) {
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
		this.tabSheet = new XdevTabSheet();
		this.gridLayoutApp = new XdevGridLayout();
		this.label = new XdevLabel();
		this.labelCountry = new XdevLabel();
		this.label2 = new XdevLabel();
		this.labelLanguage = new XdevLabel();
		this.label3 = new XdevLabel();
		this.labelTimeZone = new XdevLabel();
		this.label4 = new XdevLabel();
		this.labelVersion = new XdevLabel();
		this.labelArtifact = new XdevLabel();
		this.label5 = new XdevLabel();
		this.labelUsername = new XdevLabel();
		this.gridLayoutUsr = new XdevGridLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.button = new XdevButton();

		this.tabSheet.setStyleName("framed");
		this.gridLayoutApp.setMargin(new MarginInfo(true, true, false, true));
		this.label.setValue("Country");
		this.labelCountry.setValue("Version");
		this.label2.setValue("Language");
		this.labelLanguage.setValue("Version");
		this.label3.setValue("Time Zone");
		this.labelTimeZone.setValue("Version");
		this.label4.setValue("Version");
		this.labelVersion.setValue("0.6");
		this.labelArtifact.setValue("0.6");
		this.label5.setValue("Username");
		this.labelUsername.setValue("Version");
		this.gridLayoutUsr.setMargin(new MarginInfo(true, true, false, true));
		this.horizontalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.button.setCaption("Schliessen");

		this.gridLayoutApp.setColumns(3);
		this.gridLayoutApp.setRows(7);
		this.label.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label, 0, 0);
		this.labelCountry.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelCountry, 1, 0);
		this.label2.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label2, 0, 1);
		this.labelLanguage.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelLanguage, 1, 1);
		this.label3.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label3, 0, 2);
		this.labelTimeZone.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelTimeZone, 1, 2);
		this.label4.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label4, 0, 3);
		this.labelVersion.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelVersion, 1, 3);
		this.labelArtifact.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelArtifact, 1, 4);
		this.label5.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label5, 0, 5);
		this.labelUsername.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelUsername, 1, 5);
		final CustomComponent gridLayoutApp_hSpacer = new CustomComponent();
		gridLayoutApp_hSpacer.setSizeFull();
		this.gridLayoutApp.addComponent(gridLayoutApp_hSpacer, 2, 0, 2, 5);
		this.gridLayoutApp.setColumnExpandRatio(2, 1.0F);
		final CustomComponent gridLayoutApp_vSpacer = new CustomComponent();
		gridLayoutApp_vSpacer.setSizeFull();
		this.gridLayoutApp.addComponent(gridLayoutApp_vSpacer, 0, 6, 1, 6);
		this.gridLayoutApp.setRowExpandRatio(6, 1.0F);
		this.gridLayoutApp.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutApp, "Application", null);
		this.gridLayoutUsr.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutUsr, "UserInfo", null);
		this.tabSheet.setSelectedTab(this.gridLayoutApp);
		this.button.setSizeUndefined();
		this.horizontalLayout.addComponent(this.button);
		this.horizontalLayout.setComponentAlignment(this.button, Alignment.MIDDLE_CENTER);
		this.tabSheet.setSizeFull();
		this.verticalLayout.addComponent(this.tabSheet);
		this.verticalLayout.setComponentAlignment(this.tabSheet, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.tabSheet, 10.0F);
		this.horizontalLayout.setSizeUndefined();
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 5.0F);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.gridLayoutApp.addAttachListener(event -> this.gridLayoutApp_attach(event));
		this.gridLayoutUsr.addAttachListener(event -> this.gridLayoutUsr_attach(event));
		this.button.addClickListener(event -> this.button_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, labelCountry, label2, labelLanguage, label3, labelTimeZone, label4, labelVersion,
			labelArtifact, label5, labelUsername;
	private XdevButton button;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPanel panel;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayoutApp, gridLayoutUsr;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
