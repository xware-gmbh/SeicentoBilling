package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Locale;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.TheVersion;
import ch.xwr.seicentobilling.business.auth.SeicentoUser;

public class ApplicationSettingsTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationSettingsTabView.class);

	private SeicentoUser currentUser;
	private Integer index;

	/**
	 *
	 */
	public ApplicationSettingsTabView() {
		super();
		this.initUI();

		final TheVersion ver = new TheVersion();
		this.labelVersion.setValue(ver.getEntryById("version"));
		this.labelArtifact.setValue(ver.getEntryById("groupId") + " " + ver.getEntryById("artifactId") );

		LOG.debug("ApplicationSettingsTabView initialized");
	}

	/**
	 * Event handler delegate method for the {@link XdevGridLayout}
	 * {@link #gridLayoutUsr}.
	 *
	 * @see ClientConnector.AttachListener#attach(ClientConnector.AttachEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void gridLayoutUsr_attach(final ClientConnector.AttachEvent event) {
		final SeicentoUser su = Seicento.getSeicentoUser();
		this.currentUser = su;
		showInfos();

//		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);
//
//		if (sub != null && sub instanceof AzureUser)
//		{
//			this.currentUser = new SeicentoUser();
//			this.currentUser.setAzureUser((AzureUser) sub);
//			showInfos();
//		} else if (sub != null && sub instanceof SeicentoUser){
//			this.currentUser = (SeicentoUser) sub;
//			showInfos();
//		}
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

		this.lblMemory.setValue("" + Seicento.getMemory());
		this.lblSession.setValue(UI.getCurrent().getSession().getState().name());
		this.lblJava.setValue(System.getProperty("java.version") + " - " + System.getProperty("java.vendor"));
		this.LblOs.setValue(System.getProperty("os.name") + " - " + System.getProperty("os.version") + " - " + System.getProperty("os.arch"));
	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.tabSheet = new XdevTabSheet();
		this.gridLayoutUsr = new XdevGridLayout();
		this.gridLayoutApp = new XdevGridLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.label4 = new XdevLabel();
		this.labelVersion = new XdevLabel();
		this.labelArtifact = new XdevLabel();
		this.label5 = new XdevLabel();
		this.labelUsername = new XdevLabel();
		this.label = new XdevLabel();
		this.labelCountry = new XdevLabel();
		this.label2 = new XdevLabel();
		this.labelLanguage = new XdevLabel();
		this.label3 = new XdevLabel();
		this.labelTimeZone = new XdevLabel();
		this.label6 = new XdevLabel();
		this.lblMemory = new XdevLabel();
		this.label7 = new XdevLabel();
		this.lblSession = new XdevLabel();
		this.label8 = new XdevLabel();
		this.lblJava = new XdevLabel();
		this.label9 = new XdevLabel();
		this.LblOs = new XdevLabel();

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.gridLayoutUsr.setMargin(new MarginInfo(true, true, false, true));
		this.gridLayoutApp.setMargin(new MarginInfo(true, true, false, true));
		this.horizontalLayout.setIcon(FontAwesome.DASHBOARD);
		this.horizontalLayout.setCaption("SeicentoBilling");
		this.horizontalLayout.setStyleName("warning");
		this.horizontalLayout.setMargin(new MarginInfo(false, false, true, false));
		this.label4.setValue("Version");
		this.labelVersion.setValue("0.6");
		this.labelArtifact.setValue("0.6");
		this.label5.setValue("Username");
		this.labelUsername.setValue("unknown");
		this.label.setValue("Country");
		this.labelCountry.setValue("CH");
		this.label2.setValue("Language");
		this.labelLanguage.setValue("German");
		this.label3.setValue("Time Zone");
		this.labelTimeZone.setValue("unknown");
		this.label6.setValue("Memory used MB");
		this.lblMemory.setValue("Version");
		this.label7.setValue("Session");
		this.lblSession.setValue("Version");
		this.label8.setValue("Java");
		this.lblJava.setValue("Java");
		this.label9.setValue("Operating System");
		this.LblOs.setValue("Windows");

		this.label4.setSizeUndefined();
		this.horizontalLayout.addComponent(this.label4);
		this.labelVersion.setWidth(150, Unit.PIXELS);
		this.labelVersion.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.labelVersion);
		this.labelArtifact.setWidth(250, Unit.PIXELS);
		this.labelArtifact.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.labelArtifact);
		this.horizontalLayout.setExpandRatio(this.labelArtifact, 20.0F);
		this.gridLayoutApp.setColumns(2);
		this.gridLayoutApp.setRows(10);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(50, Unit.PIXELS);
		this.gridLayoutApp.addComponent(this.horizontalLayout, 0, 0, 1, 0);
		this.gridLayoutApp.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_LEFT);
		this.label5.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label5, 0, 1);
		this.labelUsername.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelUsername, 1, 1);
		this.label.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label, 0, 2);
		this.labelCountry.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelCountry, 1, 2);
		this.label2.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label2, 0, 3);
		this.labelLanguage.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelLanguage, 1, 3);
		this.label3.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label3, 0, 4);
		this.labelTimeZone.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.labelTimeZone, 1, 4);
		this.label6.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label6, 0, 5);
		this.lblMemory.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.lblMemory, 1, 5);
		this.label7.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label7, 0, 6);
		this.lblSession.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.lblSession, 1, 6);
		this.label8.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label8, 0, 7);
		this.lblJava.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.lblJava, 1, 7);
		this.label9.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.label9, 0, 8);
		this.LblOs.setSizeUndefined();
		this.gridLayoutApp.addComponent(this.LblOs, 1, 8);
		this.gridLayoutApp.setColumnExpandRatio(1, 40.0F);
		final CustomComponent gridLayoutApp_vSpacer = new CustomComponent();
		gridLayoutApp_vSpacer.setSizeFull();
		this.gridLayoutApp.addComponent(gridLayoutApp_vSpacer, 0, 9, 1, 9);
		this.gridLayoutApp.setRowExpandRatio(9, 1.0F);
		this.gridLayoutUsr.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutUsr, "User Claims", null);
		this.gridLayoutApp.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutApp, "Application", null);
		this.tabSheet.setSelectedTab(this.gridLayoutApp);
		this.tabSheet.setSizeFull();
		this.verticalLayout.addComponent(this.tabSheet);
		this.verticalLayout.setComponentAlignment(this.tabSheet, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.tabSheet, 10.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.gridLayoutUsr.addAttachListener(event -> this.gridLayoutUsr_attach(event));
		this.gridLayoutApp.addAttachListener(event -> this.gridLayoutApp_attach(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label4, labelVersion, labelArtifact, label5, labelUsername, label, labelCountry, label2,
			labelLanguage, label3, labelTimeZone, label6, lblMemory, label7, lblSession, label8, lblJava, label9, LblOs;
	private XdevHorizontalLayout horizontalLayout;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayoutUsr, gridLayoutApp;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
