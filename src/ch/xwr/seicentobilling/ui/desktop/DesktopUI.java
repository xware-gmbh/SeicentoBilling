
package ch.xwr.seicentobilling.ui.desktop;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.xdev.res.ApplicationResource;
import com.xdev.security.authentication.ui.XdevAuthenticationNavigator;
import com.xdev.server.aa.openid.auth.AzureUser;
import com.xdev.server.aa.openid.helper.DiscoveryHelper;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevImage;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevMenuBar;
import com.xdev.ui.XdevMenuBar.XdevMenuItem;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevUI;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.helper.AzureHelper;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.ui.desktop.billing.OrderGenerateTabView;

@Push(value = PushMode.MANUAL, transport = Transport.LONG_POLLING)
@Theme("SeicentoBilling")
public class DesktopUI extends XdevUI {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DesktopUI.class);


	private AzureUser currentUser;


	public DesktopUI() {
		super();

		//mj DiscoveryHelper for setup AAD
		try {
			DiscoveryHelper.performDiscovery(VaadinServlet.getCurrent().getServletContext());
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final VaadinRequest request) {
		setLocale();
		this.initUI();
		checkLocalDevEnv();
		setCallBackUri();
		loadMyData();

	}

	//only for local testing with preview
	private void checkLocalDevEnv() {
		if (isLocalDevEnv()) {
			LOG.info("Local DEV Environment.... enable Menues");

			//Eclipse Preview (does not have Path in Jetty)
			enableMenu(true);

			//this.currentUser = new AzureUser(null);

			this.menuBarRight.setVisible(false);
			this.menuBarRight.setEnabled(false);

		}
	}

	private boolean isLocalDevEnv() {
		final URI loc = this.getPage().getLocation();
		final String path = loc.getPath();

		if (path == null || path.length() < 3)
		 {
			return true;  //Jetty
		}
		if (loc.getPort() == 8080 && loc.getHost().equals("localhost")) {  //local tomcat
			return true;
		}

		return false;
	}

	//will be consumed in AuthView
	private void setCallBackUri() {
		final AzureHelper hlp = new AzureHelper();
		hlp.setCallBackUri(this.getPage().getLocation());

	}

	private void enableMenu(final boolean state) {
		this.menuBarLeft.setVisible(state);
		this.menuBarLeft.setEnabled(state);

		this.menuBarRight.setVisible(state);
		this.menuBarRight.setEnabled(state);
	}

	public void loggedIn(final boolean lgin, final AzureUser user) {
		this.currentUser = user;

		if (lgin) {
			LOG.info("User logged in " + user.name());
			this.menuItemUser.setCaption(this.currentUser.name());
			setLocale();
		} else {
			//getSession().close();   //leads to Session expired
			LOG.info("User logged out");
			this.menuItemUser.setCaption("");
		}

		enableMenu(lgin);
	}

	public AzureUser getUser() {
		return this.currentUser;
	}

	//auf Azure App Service ist diese auf en-US
	private void setLocale() {
		Locale.setDefault(new Locale("de", "CH"));
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"));
	}

	private void loadMyData() {

		final CompanyDAO dao = new CompanyDAO();
		final Company cmp = dao.getActiveConfig();

		this.lblEnvironment.setValue(dao.getDbNameNativeSQL());
		this.lblCompany.setValue(cmp.getCmpName());

		LOG.info("Company Data loaded for " + cmp.getCmpName());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuItem}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuItem_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ItemTabView.class, selectedItem.getText());
	}

	private void loadTab(final Class<?> myClass, final String desc){
		final Iterator<Component> itr = this.tabSheet.iterator();
		while (itr.hasNext()){
			final Component cmp = itr.next();
			if (cmp.getClass() == myClass){
				this.tabSheet.setSelectedTab(cmp);
				//break
				return;
			}
			if (cmp.getClass() == MainView.class){
				//tabSheet.removeComponent(cmp);
				cmp.setVisible(false);
			}
			if (cmp.getClass() == AuthView.class){
				//tabSheet.removeComponent(cmp);
				cmp.setVisible(false);
			}
		}

		Constructor<?> cons;
		try {
			cons = myClass.getConstructor();
			final XdevView viw = (XdevView) cons.newInstance();
			final TabSheet.Tab tab = this.tabSheet.addTab(viw);
			tab.setDescription("Mein Tab");
			tab.setCaption(desc);
			tab.setClosable(true);
			this.tabSheet.setSelectedTab(viw);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		//remove dummy tab (would be 1st position)
		final Component cmp = this.tabSheet.iterator().next();
		if (cmp.getClass() == MainView.class){
			this.tabSheet.removeComponent(cmp);
		}

//		Tab cmp = tabSheet.getTab(0);
//		if (cmp.getClass() == MainView.class){
//			tabSheet.removeTab(cmp);
//		}

//		TabSheet.Tab tab = tabSheet.addTab(viw);
//		//tab.setDescription("Mein Tab");
//		tab.setCaption(desc);
//		tab.setClosable(true);
//		tabSheet.setSelectedTab(viw);

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuCustomer}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuCustomer_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(CustomerTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuObject}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuObject_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(RowObjectTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuCompany}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuCompany_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(CompanyTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuProject2}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuProject2_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ProjectTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuExpense2}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuExpense2_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ExpenseTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuReport}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuReport_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ProjectLineTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuOrder}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuOrder_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(OrderTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuCostAccount}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuCostAccount_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(CostAccountTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuVat}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuVat_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(VatTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuCity}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuCity_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(CityTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuItemLogout}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuItemLogout_menuSelected(final MenuBar.MenuItem selectedItem) {
		this.tabSheet.removeAllComponents();
		loggedIn(false, null);
		this.navigator.navigateTo("");

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #menuItemUsrInfo}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void menuItemUsrInfo_menuSelected(final MenuBar.MenuItem selectedItem) {
		final Window win = UserInfoPopup.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new UserInfoPopup());
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuReportTemplate}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuReportTemplate_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ProjectLineTemplateTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuExpenseTemplate}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuExpenseTemplate_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTab(ExpenseTemplateTabView.class, selectedItem.getText());
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuOrderGenerate}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuOrderGenerate_menuSelected(final MenuBar.MenuItem selectedItem) {
		//loadTab(OrderTabView.class, selectedItem.getText());
		loadTab(OrderGenerateTabView.class, selectedItem.getText());
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayoutTitle = new XdevHorizontalLayout();
		this.image = new XdevImage();
		this.verticalLayout3 = new XdevVerticalLayout();
		this.lblCompany = new XdevLabel();
		this.lblEnvironment = new XdevLabel();
		this.horizontalLayoutMenu = new XdevHorizontalLayout();
		this.menuBarLeft = new XdevMenuBar();
		this.mnuOperation = this.menuBarLeft.addItem("Verkauf", null);
		this.mnuOrder = this.mnuOperation.addItem("Rechnung", null);
		this.mnuItem = this.mnuOperation.addItem("Artikel", null);
		this.mnuCustomer = this.mnuOperation.addItem("Kontakte", null);
		this.mnuSeperator = this.mnuOperation.addSeparator();
		this.mnuOrderGenerate = this.mnuOperation.addItem("Rechnungen generieren...", null);
		this.mnuExpense = this.menuBarLeft.addItem("Spesen u. Rapporte", null);
		this.mnuExpense2 = this.mnuExpense.addItem("Spesen", null);
		this.mnuReport = this.mnuExpense.addItem("Rapporte", null);
		this.mnuSeperator1 = this.mnuExpense.addSeparator();
		this.mnuReportTemplate = this.mnuExpense.addItem("Vorlagen Rapport", null);
		this.mnuExpenseTemplate = this.mnuExpense.addItem("Vorlagen Spesen", null);
		this.mnuProject = this.menuBarLeft.addItem("Projekt", null);
		this.mnuProject2 = this.mnuProject.addItem("Projekte", null);
		this.mnuAddon = this.menuBarLeft.addItem("Hilfstabellen", null);
		this.menuCostAccount = this.mnuAddon.addItem("Kostenstelle", null);
		this.menuVat = this.mnuAddon.addItem("Mwst", null);
		this.menuCity = this.mnuAddon.addItem("Ortschaft", null);
		this.mnuOption = this.menuBarLeft.addItem("Optionen", null);
		this.mnuCompany = this.mnuOption.addItem("Firma", null);
		this.mnuObject = this.mnuOption.addItem("Objektstamm", null);
		this.menuBarRight = new XdevMenuBar();
		this.menuItemUser = this.menuBarRight.addItem("Benutzer", null);
		this.menuItemUsrInfo = this.menuItemUser.addItem("Info", null);
		this.menuItemLogout = this.menuItemUser.addItem("Logout", null);
		this.layoutsTab = new XdevVerticalLayout();
		this.tabSheet = new XdevTabSheet();
		this.navigator = new XdevAuthenticationNavigator(this, this.tabSheet);

		this.verticalLayout
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/favicon.png"));
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayoutTitle.setMargin(new MarginInfo(false, true, false, false));
		this.image.setSource(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/Seicento_Billing.png"));
		this.verticalLayout3.setSpacing(false);
		this.verticalLayout3.setMargin(new MarginInfo(false, false, false, true));
		this.lblCompany.setValue("Label");
		this.lblEnvironment.setStyleName("small");
		this.lblEnvironment.setValue("Label");
		this.horizontalLayoutMenu.setSpacing(false);
		this.horizontalLayoutMenu.setMargin(new MarginInfo(false));
		this.menuBarLeft.setEnabled(false);
		this.menuBarLeft.setVisible(false);
		this.menuBarRight.setEnabled(false);
		this.menuBarRight.setVisible(false);
		this.menuItemUser
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/User_black_18.png"));
		this.layoutsTab.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.navigator.setRedirectViewName("home");
		this.navigator.addView("", AuthView.class);
		this.navigator.addView("home", MainView.class);

		this.lblCompany.setSizeUndefined();
		this.verticalLayout3.addComponent(this.lblCompany);
		this.verticalLayout3.setComponentAlignment(this.lblCompany, Alignment.MIDDLE_RIGHT);
		this.lblEnvironment.setSizeUndefined();
		this.verticalLayout3.addComponent(this.lblEnvironment);
		this.verticalLayout3.setComponentAlignment(this.lblEnvironment, Alignment.MIDDLE_RIGHT);
		this.image.setSizeUndefined();
		this.horizontalLayoutTitle.addComponent(this.image);
		this.horizontalLayoutTitle.setComponentAlignment(this.image, Alignment.MIDDLE_CENTER);
		this.verticalLayout3.setSizeUndefined();
		this.horizontalLayoutTitle.addComponent(this.verticalLayout3);
		this.horizontalLayoutTitle.setComponentAlignment(this.verticalLayout3, Alignment.MIDDLE_RIGHT);
		this.horizontalLayoutTitle.setExpandRatio(this.verticalLayout3, 10.0F);
		this.menuBarLeft.setWidth(100, Unit.PERCENTAGE);
		this.menuBarLeft.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutMenu.addComponent(this.menuBarLeft);
		this.horizontalLayoutMenu.setComponentAlignment(this.menuBarLeft, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutMenu.setExpandRatio(this.menuBarLeft, 100.0F);
		this.menuBarRight.setWidth(100, Unit.PERCENTAGE);
		this.menuBarRight.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutMenu.addComponent(this.menuBarRight);
		this.horizontalLayoutMenu.setComponentAlignment(this.menuBarRight, Alignment.MIDDLE_RIGHT);
		this.horizontalLayoutMenu.setExpandRatio(this.menuBarRight, 20.0F);
		this.tabSheet.setSizeFull();
		this.layoutsTab.addComponent(this.tabSheet);
		this.layoutsTab.setComponentAlignment(this.tabSheet, Alignment.MIDDLE_CENTER);
		this.layoutsTab.setExpandRatio(this.tabSheet, 100.0F);
		this.horizontalLayoutTitle.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutTitle.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutTitle);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutTitle, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutMenu.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutMenu.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutMenu);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutMenu, Alignment.TOP_CENTER);
		this.layoutsTab.setWidth(100, Unit.PERCENTAGE);
		this.layoutsTab.setHeight(99, Unit.PERCENTAGE);
		this.verticalLayout.addComponent(this.layoutsTab);
		this.verticalLayout.setComponentAlignment(this.layoutsTab, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.layoutsTab, 10.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.mnuOrder.setCommand(selectedItem -> this.mnuOrder_menuSelected(selectedItem));
		this.mnuItem.setCommand(selectedItem -> this.mnuItem_menuSelected(selectedItem));
		this.mnuCustomer.setCommand(selectedItem -> this.mnuCustomer_menuSelected(selectedItem));
		this.mnuOrderGenerate.setCommand(selectedItem -> this.mnuOrderGenerate_menuSelected(selectedItem));
		this.mnuExpense2.setCommand(selectedItem -> this.mnuExpense2_menuSelected(selectedItem));
		this.mnuReport.setCommand(selectedItem -> this.mnuReport_menuSelected(selectedItem));
		this.mnuReportTemplate.setCommand(selectedItem -> this.mnuReportTemplate_menuSelected(selectedItem));
		this.mnuExpenseTemplate.setCommand(selectedItem -> this.mnuExpenseTemplate_menuSelected(selectedItem));
		this.mnuProject2.setCommand(selectedItem -> this.mnuProject2_menuSelected(selectedItem));
		this.menuCostAccount.setCommand(selectedItem -> this.menuCostAccount_menuSelected(selectedItem));
		this.menuVat.setCommand(selectedItem -> this.menuVat_menuSelected(selectedItem));
		this.menuCity.setCommand(selectedItem -> this.menuCity_menuSelected(selectedItem));
		this.mnuCompany.setCommand(selectedItem -> this.mnuCompany_menuSelected(selectedItem));
		this.mnuObject.setCommand(selectedItem -> this.mnuObject_menuSelected(selectedItem));
		this.menuItemUsrInfo.setCommand(selectedItem -> this.menuItemUsrInfo_menuSelected(selectedItem));
		this.menuItemLogout.setCommand(selectedItem -> this.menuItemLogout_menuSelected(selectedItem));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblCompany, lblEnvironment;
	private XdevMenuBar menuBarLeft, menuBarRight;
	private XdevHorizontalLayout horizontalLayoutTitle, horizontalLayoutMenu;
	private XdevImage image;
	private XdevMenuItem mnuOperation, mnuOrder, mnuItem, mnuCustomer, mnuSeperator, mnuOrderGenerate, mnuExpense,
			mnuExpense2, mnuReport, mnuSeperator1, mnuReportTemplate, mnuExpenseTemplate, mnuProject, mnuProject2, mnuAddon,
			menuCostAccount, menuVat, menuCity, mnuOption, mnuCompany, mnuObject, menuItemUser, menuItemUsrInfo,
			menuItemLogout;
	private XdevTabSheet tabSheet;
	private XdevVerticalLayout verticalLayout, verticalLayout3, layoutsTab;
	private XdevAuthenticationNavigator navigator;
	// </generated-code>
}