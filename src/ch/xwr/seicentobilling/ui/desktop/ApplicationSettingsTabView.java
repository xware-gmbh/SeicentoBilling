package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Locale;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.TheVersion;
import ch.xwr.seicentobilling.business.auth.SeicentoUser;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.AppUserDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.AppUser;
import ch.xwr.seicentobilling.entities.AppUser_;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Customer_;

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

		//State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.cmbThemeDesktop.addItems((Object[]) LovState.Theme.values());
		this.cmbThemeMobile.addItems((Object[]) LovState.Theme.values());
	}

	private void setROFields() {
		String lm = "Authentication with Database";

		if (Seicento.getLoginMethod().equals("azure")) {
			lm = "Authentication with azure ad";

			this.txtUsername.setEnabled(false);
			this.txtUsrRoles.setEnabled(false);
			this.cmdSetPassword.setEnabled(false);
		}
		this.lblLoginMode.setValue(lm);
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
		this.lblJava.setValue(System.getProperty("java.version"));
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset2}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset2_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (SeicentoCrud.doSave(this.fieldGroup)) {
			try {
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.fieldGroup.getItemDataSource().getBean().getUsrId(),
						this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
			} catch (final Exception e) {
				LOG.error("could not save ObjRoot", e);
			}
		}

		setROFields();

	}

	/**
	 * Event handler delegate method for the {@link VerticalLayout}
	 * {@link #gridLayoutProfile}.
	 *
	 * @see ClientConnector.AttachListener#attach(ClientConnector.AttachEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void gridLayoutProfile_attach(final ClientConnector.AttachEvent event) {
		final AppUser bean = getAppUserBean();

		if (bean != null) {
			this.fieldGroup.setItemDataSource(bean);
		} else {
			this.fieldGroup.setEnabled(false);
			this.horizontalLayout2.setEnabled(false);
			LOG.error("No AppUser found in Database!" );
		}

		setROFields();
	}

	private AppUser getAppUserBean() {
		final SeicentoUser su = Seicento.getSeicentoUser();
		AppUser bean = su.getDbUser();

		if (bean != null) {
			bean = new AppUserDAO().find(bean.getUsrId());  //reread possible changes
		}
		return bean;

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSetPassword}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSetPassword_buttonClick(final Button.ClickEvent event) {
		final AppUser bean = getAppUserBean();
		UI.getCurrent().getSession().setAttribute("appuserbean", bean);

		final Window win = PasswordPopup.getPopupWindow();
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdApplyTheme}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdApplyTheme_buttonClick(final Button.ClickEvent event) {
		final LovState.Theme th = (LovState.Theme) this.cmbThemeDesktop.getValue();

		if (th.equals(LovState.Theme.dark)) {
			this.getUI().setTheme("Darksb");
		}
		if (th.equals(LovState.Theme.facebook)) {
			this.getUI().setTheme("Facebook");
		}
		if (th.equals(LovState.Theme.light)) {
			this.getUI().setTheme("SeicentoBilling");
		}

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.tabSheet = new XdevTabSheet();
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
		this.gridLayoutProfile = new VerticalLayout();
		this.form = new XdevGridLayout();
		this.lblLoginMode = new XdevLabel();
		this.label9 = new XdevLabel();
		this.lblUsername = new XdevLabel();
		this.txtUsername = new XdevTextField();
		this.cmdSetPassword = new XdevButton();
		this.lblUsrRoles = new XdevLabel();
		this.txtUsrRoles = new XdevTextField();
		this.lblUsrFullName = new XdevLabel();
		this.txtUsrFullName = new XdevTextField();
		this.lblUsrThemeDesktop = new XdevLabel();
		this.cmbThemeDesktop = new XdevComboBox<>();
		this.lblUsrThemeMobile = new XdevLabel();
		this.cmbThemeMobile = new XdevComboBox<>();
		this.lblUsrLanguage = new XdevLabel();
		this.txtUsrLanguage = new XdevTextField();
		this.lblUsrCountry = new XdevLabel();
		this.txtUsrCountry = new XdevTextField();
		this.lblUsrTimeZone = new XdevLabel();
		this.txtUsrTimeZone = new XdevTextField();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblCustomer = new XdevLabel();
		this.cmbCustomer = new XdevComboBox<>();
		this.lblUsrValidFrom = new XdevLabel();
		this.dateUsrValidFrom = new XdevPopupDateField();
		this.lblUsrValidTo = new XdevLabel();
		this.dateUsrValidTo = new XdevPopupDateField();
		this.lblUsrState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset2 = new XdevButton();
		this.label10 = new XdevLabel();
		this.cmdApplyTheme = new XdevButton();
		this.gridLayoutUsr = new XdevGridLayout();
		this.fieldGroup = new XdevFieldGroup<>(AppUser.class);

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
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
		this.lblLoginMode.setValue("Label");
		this.label9.setIcon(FontAwesome.INFO_CIRCLE);
		this.label9.setDescription("Damit Änderungen wirksam werden muss man sich neu Anmelden!");
		this.lblUsername.setValue("Login Name");
		this.cmdSetPassword.setIcon(FontAwesome.USER_SECRET);
		this.cmdSetPassword.setCaption("Passwort...");
		this.cmdSetPassword.setDescription("Passwort für lokalen Benutzer");
		this.lblUsrRoles.setValue("Roles");
		this.lblUsrFullName.setValue("Name");
		this.lblUsrThemeDesktop.setValue("Theme Desktop");
		this.lblUsrThemeMobile.setValue("Theme Mobile");
		this.lblUsrLanguage.setValue("Language");
		this.lblUsrCountry.setValue("Country");
		this.lblUsrTimeZone.setValue("TimeZone");
		this.lblCostAccount.setValue("CostAccount");
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblCustomer.setValue("Contact");
		this.cmbCustomer.setContainerDataSource(Customer.class, DAOs.get(CustomerDAO.class).findAll());
		this.cmbCustomer.setItemCaptionPropertyId(Customer_.cusName.getName());
		this.lblUsrValidFrom.setValue("Valid From");
		this.lblUsrValidTo.setValue("Valid To");
		this.lblUsrState.setValue("Status");
		this.horizontalLayout2.setMargin(new MarginInfo(false, false, false, true));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdReset2.setIcon(FontAwesome.UNDO);
		this.cmdReset2.setCaption("Verwerfen");
		this.cmdApplyTheme.setIcon(FontAwesome.DASHBOARD);
		this.cmdApplyTheme.setCaption("Theme anwenden");
		this.gridLayoutUsr.setMargin(new MarginInfo(true, true, false, true));
		this.fieldGroup.bind(this.txtUsername, AppUser_.username.getName());
		this.fieldGroup.bind(this.txtUsrRoles, AppUser_.usrRoles.getName());
		this.fieldGroup.bind(this.txtUsrFullName, AppUser_.usrFullName.getName());
		this.fieldGroup.bind(this.cmbThemeDesktop, AppUser_.usrThemeDesktop.getName());
		this.fieldGroup.bind(this.cmbThemeMobile, AppUser_.usrThemeMobile.getName());
		this.fieldGroup.bind(this.txtUsrLanguage, AppUser_.usrLanguage.getName());
		this.fieldGroup.bind(this.txtUsrCountry, AppUser_.usrCountry.getName());
		this.fieldGroup.bind(this.txtUsrTimeZone, AppUser_.usrTimeZone.getName());
		this.fieldGroup.bind(this.cmbCostAccount, AppUser_.costAccount.getName());
		this.fieldGroup.bind(this.cmbCustomer, AppUser_.customer.getName());
		this.fieldGroup.bind(this.dateUsrValidFrom, AppUser_.usrValidFrom.getName());
		this.fieldGroup.bind(this.dateUsrValidTo, AppUser_.usrValidTo.getName());
		this.fieldGroup.bind(this.comboBoxState, AppUser_.usrState.getName());

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
		this.gridLayoutApp.setRows(9);
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
		this.gridLayoutApp.setColumnExpandRatio(1, 40.0F);
		final CustomComponent gridLayoutApp_vSpacer = new CustomComponent();
		gridLayoutApp_vSpacer.setSizeFull();
		this.gridLayoutApp.addComponent(gridLayoutApp_vSpacer, 0, 8, 1, 8);
		this.gridLayoutApp.setRowExpandRatio(8, 1.0F);
		this.form.setColumns(4);
		this.form.setRows(12);
		this.lblLoginMode.setSizeUndefined();
		this.form.addComponent(this.lblLoginMode, 0, 0);
		this.label9.setSizeUndefined();
		this.form.addComponent(this.label9, 2, 0);
		this.lblUsername.setSizeUndefined();
		this.form.addComponent(this.lblUsername, 0, 1);
		this.txtUsername.setWidth(100, Unit.PERCENTAGE);
		this.txtUsername.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtUsername, 1, 1);
		this.cmdSetPassword.setSizeUndefined();
		this.form.addComponent(this.cmdSetPassword, 2, 1);
		this.form.setComponentAlignment(this.cmdSetPassword, Alignment.MIDDLE_CENTER);
		this.lblUsrRoles.setSizeUndefined();
		this.form.addComponent(this.lblUsrRoles, 0, 2);
		this.txtUsrRoles.setSizeUndefined();
		this.form.addComponent(this.txtUsrRoles, 1, 2);
		this.lblUsrFullName.setSizeUndefined();
		this.form.addComponent(this.lblUsrFullName, 0, 3);
		this.txtUsrFullName.setWidth(100, Unit.PERCENTAGE);
		this.txtUsrFullName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtUsrFullName, 1, 3);
		this.lblUsrThemeDesktop.setSizeUndefined();
		this.form.addComponent(this.lblUsrThemeDesktop, 0, 4);
		this.cmbThemeDesktop.setSizeUndefined();
		this.form.addComponent(this.cmbThemeDesktop, 1, 4);
		this.lblUsrThemeMobile.setSizeUndefined();
		this.form.addComponent(this.lblUsrThemeMobile, 2, 4);
		this.cmbThemeMobile.setSizeUndefined();
		this.form.addComponent(this.cmbThemeMobile, 3, 4);
		this.lblUsrLanguage.setSizeUndefined();
		this.form.addComponent(this.lblUsrLanguage, 0, 5);
		this.txtUsrLanguage.setWidth(100, Unit.PERCENTAGE);
		this.txtUsrLanguage.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtUsrLanguage, 1, 5);
		this.lblUsrCountry.setSizeUndefined();
		this.form.addComponent(this.lblUsrCountry, 2, 5);
		this.txtUsrCountry.setWidth(100, Unit.PERCENTAGE);
		this.txtUsrCountry.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtUsrCountry, 3, 5);
		this.lblUsrTimeZone.setSizeUndefined();
		this.form.addComponent(this.lblUsrTimeZone, 0, 6);
		this.txtUsrTimeZone.setWidth(100, Unit.PERCENTAGE);
		this.txtUsrTimeZone.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtUsrTimeZone, 1, 6);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 7);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 7);
		this.lblCustomer.setSizeUndefined();
		this.form.addComponent(this.lblCustomer, 0, 8);
		this.cmbCustomer.setWidth(100, Unit.PERCENTAGE);
		this.cmbCustomer.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCustomer, 1, 8);
		this.lblUsrValidFrom.setSizeUndefined();
		this.form.addComponent(this.lblUsrValidFrom, 0, 9);
		this.dateUsrValidFrom.setWidth(100, Unit.PERCENTAGE);
		this.dateUsrValidFrom.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.dateUsrValidFrom, 1, 9);
		this.lblUsrValidTo.setSizeUndefined();
		this.form.addComponent(this.lblUsrValidTo, 2, 9);
		this.dateUsrValidTo.setWidth(100, Unit.PERCENTAGE);
		this.dateUsrValidTo.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.dateUsrValidTo, 3, 9);
		this.lblUsrState.setSizeUndefined();
		this.form.addComponent(this.lblUsrState, 0, 10);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 10);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 11, 3, 11);
		this.form.setRowExpandRatio(11, 1.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdSave);
		this.horizontalLayout2.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset2.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdReset2);
		this.horizontalLayout2.setComponentAlignment(this.cmdReset2, Alignment.MIDDLE_LEFT);
		this.label10.setWidth(78, Unit.PIXELS);
		this.label10.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout2.addComponent(this.label10);
		this.horizontalLayout2.setComponentAlignment(this.label10, Alignment.MIDDLE_CENTER);
		this.cmdApplyTheme.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdApplyTheme);
		this.horizontalLayout2.setComponentAlignment(this.cmdApplyTheme, Alignment.MIDDLE_CENTER);
		this.form.setSizeUndefined();
		this.gridLayoutProfile.addComponent(this.form);
		this.horizontalLayout2.setSizeUndefined();
		this.gridLayoutProfile.addComponent(this.horizontalLayout2);
		final CustomComponent gridLayoutProfile_spacer = new CustomComponent();
		gridLayoutProfile_spacer.setSizeFull();
		this.gridLayoutProfile.addComponent(gridLayoutProfile_spacer);
		this.gridLayoutProfile.setExpandRatio(gridLayoutProfile_spacer, 1.0F);
		this.gridLayoutApp.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutApp, "Application", null);
		this.gridLayoutProfile.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutProfile, "Profile", null);
		this.gridLayoutUsr.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutUsr, "User Claims", null);
		this.tabSheet.setSelectedTab(this.gridLayoutApp);
		this.tabSheet.setSizeFull();
		this.verticalLayout.addComponent(this.tabSheet);
		this.verticalLayout.setComponentAlignment(this.tabSheet, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.tabSheet, 10.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.gridLayoutApp.addAttachListener(event -> this.gridLayoutApp_attach(event));
		this.gridLayoutProfile.addAttachListener(event -> this.gridLayoutProfile_attach(event));
		this.cmdSetPassword.addClickListener(event -> this.cmdSetPassword_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset2.addClickListener(event -> this.cmdReset2_buttonClick(event));
		this.cmdApplyTheme.addClickListener(event -> this.cmdApplyTheme_buttonClick(event));
		this.gridLayoutUsr.addAttachListener(event -> this.gridLayoutUsr_attach(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label5, labelUsername, label, labelCountry, label2, labelLanguage, label3, labelTimeZone, label4,
			labelVersion, labelArtifact, label6, lblMemory, label7, lblSession, label8, lblJava, lblLoginMode, label9,
			lblUsername, lblUsrRoles, lblUsrFullName, lblUsrThemeDesktop, lblUsrThemeMobile, lblUsrLanguage, lblUsrCountry,
			lblUsrTimeZone, lblCostAccount, lblCustomer, lblUsrValidFrom, lblUsrValidTo, lblUsrState, label10;
	private XdevButton cmdSetPassword, cmdSave, cmdReset2, cmdApplyTheme;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private VerticalLayout gridLayoutProfile;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayoutApp, form, gridLayoutUsr;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevPopupDateField dateUsrValidFrom, dateUsrValidTo;
	private XdevComboBox<?> cmbThemeDesktop, cmbThemeMobile, comboBoxState;
	private XdevComboBox<Customer> cmbCustomer;
	private XdevTextField txtUsername, txtUsrRoles, txtUsrFullName, txtUsrLanguage, txtUsrCountry, txtUsrTimeZone;
	private XdevVerticalLayout verticalLayout;
	private XdevFieldGroup<AppUser> fieldGroup;
	// </generated-code>

}
