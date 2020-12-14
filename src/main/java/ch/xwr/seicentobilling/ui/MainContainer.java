
package ch.xwr.seicentobilling.ui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.auth.SeicentoUser;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.ui.billing.OrderGenerateTabView;


/**
 *
 */
@Route("")
@HtmlImport("frontend://styles/shared-styles.html")
@HtmlImport("frontend://styles/my-menubar.html")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout implements PageConfigurator, RouterLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MainContainer.class);
	private SeicentoUser                         currentUser;

	/**
	 *
	 */
	public MainContainer()
	{
		super();
		this.setLocale();
		this.initUI();

		this.currentUser = Seicento.getSeicentoUser();

		final String                userLabel = Seicento.getUserName();
		final Map<String, String[]> mainMenu  = new LinkedHashMap<>();
		mainMenu.put("Verkauf",
			new String[]{"Rechnung", "Artikel", "Kontakte", "Rechnungen generieren..."});
		mainMenu.put("Spesen u. Rapporte", new String[]{"Spesen", "Rapporte", "Vorlagen Rapport", "Vorlagen Spesen"});
		mainMenu.put("Projekt",
			new String[]{"Projekte"});
		mainMenu.put("Hilfstabellen",
			new String[]{"Kostenstelle", "Mwst", "Ortschaft"});
		mainMenu.put("Optionen",
			new String[]{"Firma", "Objektstamm"});
		mainMenu.put(userLabel,
			new String[]{"Profil", "Info", "Logout"});
		
		@SuppressWarnings("rawtypes")
		final Map<String, Class> menuPages = new LinkedHashMap<>();
		menuPages.put("Rechnung", OrderTabView.class);
		menuPages.put("Artikel", ItemTabView.class);
		menuPages.put("Kontakte", CustomerTabView.class);
		menuPages.put("Rechnungen generieren...", OrderGenerateTabView.class);
		
		menuPages.put("Spesen", ExpenseTabView.class);
		menuPages.put("Rapporte", ProjectLineTabView.class);
		menuPages.put("Vorlagen Rapport", ProjectLineTemplateTabView.class);
		menuPages.put("Vorlagen Spesen", ExpenseTemplateTabView.class);

		menuPages.put("Projekte", ProjectTabView.class);
		
		menuPages.put("Kostenstelle", CostAccountTabView.class);
		menuPages.put("Mwst", VatTabView.class);
		menuPages.put("Ortschaft", CityTabView.class);
		
		menuPages.put("Firma", CompanyTabView.class);
		menuPages.put("Objektstamm", RowObjectTabView.class);
		
		menuPages.put("Profil", ProfileTabView.class);
		menuPages.put("Info", ApplicationSettingsTabView.class);
		menuPages.put("Logout", LogoutView.class);
		
		for(final String key : mainMenu.keySet())
		{
			final MenuItem mainMenuItem    = this.navMenuBar.addItem(key);
			final String[] subMenuItemList = mainMenu.get(key);
			for(final String subm : subMenuItemList)
			{
				final SubMenu  subMenu     = mainMenuItem.getSubMenu();
				final MenuItem subMenuItem = subMenu.addItem(subm);
				subMenuItem.addClickListener(e -> this.loadTab(menuPages.get(subm), subMenuItem.getText()));
			}
		}
		
		this.checkLocalDevEnv();
		this.setCallBackUri();
		this.loadMyData();
		this.tabs.addSelectedChangeListener(event -> {
			this.tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = this.tabsToPages.get(this.tabs.getSelectedTab());
			if(selectedPage != null)
			{
				selectedPage.setVisible(true);
			}
		});
		
	}
	
	// will be consumed in AuthView
	private void setCallBackUri()
	{
		// final AzureHelper hlp = new AzureHelper();
		// hlp.setCallBackUri(this.getPage().getLocation());

	}
	
	public SeicentoUser getUser()
	{
		return this.currentUser;
	}
	
	private void setLocale()
	{
		Locale.setDefault(new Locale("de", "CH"));
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"));

	}

	// only for local testing with preview
	private void checkLocalDevEnv()
	{
		if(this.isLocalDevEnv())
		{
			MainContainer.LOG.info("Local DEV Environment.... enable Menues, disable Gelf");
			
			// Eclipse Preview (does not have Path in Jetty)
			// enableMenu(true);
			Seicento.removeGelfAppender();
			
			// this.currentUser = new AzureUser(null);
			
			// this.menuBarRight.setVisible(false);
			// this.menuBarRight.setEnabled(false);
			
		}
	}
	
	private boolean isLocalDevEnv()
	{
		final VaadinRequest      vaadinRequest      = VaadinService.getCurrentRequest();
		final HttpServletRequest httpServletRequest = ((VaadinServletRequest)vaadinRequest).getHttpServletRequest();
		final String             path               = httpServletRequest.getRequestURL().toString();

		if(path == null || path.length() < 3)
		{
			return true; // Jetty
		}
		
		// for Tomcat use MockUser
		if(vaadinRequest.getRemotePort() == 8080 && vaadinRequest.getRemoteHost().equals("localhost"))
		{ // local tomcat
			MainContainer.LOG.info("Local Tomcat...Set Login to Mockup if needed");
			// return true;
		}
		
		return false;
	}
	
	private void loadMyData()
	{

		final CompanyDAO dao = new CompanyDAO();
		final Company    cmp = dao.getActiveConfig();
		
		// this.lblCompany.setText(cmp.getCmpName() + " " + dao.getDbNameNativeSQL());
		this.lblCompany.setText(cmp.getCmpName());
		this.lblEnvironment.setText(dao.getDbNameNativeSQL());

		MainContainer.LOG.info("Company Data loaded for " + cmp.getCmpName());
	}

	@Override
	public void configurePage(final InitialPageSettings settings)
	{
		settings.addLink("shortcut icon", "frontend/images/favicon.ico");
		settings.addFavIcon("icon", "frontend/images/favicon256.png", "256x256");
	}

	@Override
	public void showRouterLayoutContent(final HasElement content)
	{
		this.contentContainer.removeAll();
		this.contentContainer.getElement().appendChild(content.getElement());
	}

	Map<Tab, Component> tabsToPages = new HashMap<>();

	private void loadTab(final Class<?> myClass, final String desc)
	{
		if(desc.equals("Logout"))
		{
			this.tabs.removeAll();
			this.loggedIn(false, null);
			// Close the session
			VaadinSession.getCurrent().getSession().invalidate();
			UI.getCurrent().getSession().close();
			UI.getCurrent().getPage().executeJs("window.location.href=''");
			// UI.getCurrent().navigate("login");
			return;
		}
		this.tabsToPages.values().forEach(page -> page.setVisible(false));
		Component cmp         = null;
		Tab       tabToSelect = null;
		
		for(final Map.Entry<Tab, Component> entry : this.tabsToPages.entrySet())
		{
			if(entry.getKey().getLabel().equals(desc))
			{
				cmp         = entry.getValue();
				tabToSelect = entry.getKey();
				cmp.setVisible(true);
				tabToSelect.setSelected(true);
				return;
			}
		}

		Constructor<?> cons;
		try
		{
			cons = myClass.getConstructor();
			final VerticalLayout viw = (VerticalLayout)cons.newInstance();
			final Tab            tab = new Tab(desc);
			this.tabs.add(tab);
			final Button close = new Button(VaadinIcon.CLOSE_SMALL.create(),
				click -> {
					this.tabs.remove(tab);
					final Component cmpv = this.tabsToPages.get(tab);
					this.contentContainer.remove(cmpv);
					this.tabsToPages.remove(tab);
				});
			close.setWidth("0px");
			close.setHeight("30px");
			tab.addComponentAsFirst(close);
			viw.setVisible(true);
			this.tabsToPages.put(tab, viw);
			this.contentContainer.add(viw);
			this.tabs.setSelectedTab(tab);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void loggedIn(final boolean lgin, final SeicentoUser user)
	{

		if(lgin)
		{
			this.currentUser = user;
			// MainContainer.LOG.info("User logged in " + this.currentUser.name());
			// this.menuItemUser.setCaption(this.currentUser.name());
			this.setLocale();
		}
		else
		{
			// getSession().close(); //leads to Session expired
			// MainContainer.LOG.info("User logged out " + this.currentUser.name());
			// this.menuItemUser.setCaption("");
			this.currentUser = null;
		}
		
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.horizontalLayout  = new HorizontalLayout();
		this.image             = new Image();
		this.navMenuBar        = new MenuBar();
		this.profileMenubar    = new MenuBar();
		this.verticalLayout    = new VerticalLayout();
		this.lblCompany        = new Label();
		this.lblEnvironment    = new Label();
		this.horizontalLayout2 = new HorizontalLayout();
		this.contentContainer  = new VerticalLayout();
		this.tabs              = new Tabs();
		
		this.setSpacing(false);
		this.setPadding(false);
		this.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		this.image.setSrc("images/Seicento_Billing.png");
		this.navMenuBar.setMinHeight("");
		this.navMenuBar.setMaxWidth("");
		this.navMenuBar.setMinWidth("");
		this.navMenuBar.addThemeVariants(MenuBarVariant.LUMO_LARGE);
		this.navMenuBar.getStyle().set("line-height", "30px");
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.lblCompany.setText("Label");
		this.lblCompany.getStyle().set("font-size", "15px");
		this.lblCompany.getStyle().set("text-align", "right");
		this.lblEnvironment.setText("Label");
		this.lblEnvironment.getStyle().set("text-align", "right");
		this.contentContainer.setMinHeight("");
		this.contentContainer.setMaxHeight("");
		this.contentContainer.setMaxWidth("");
		this.contentContainer.setPadding(false);
		this.contentContainer.setMinWidth("100%");
		this.tabs.setMinHeight("null");
		this.tabs.setMinWidth("null");
		this.tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
		
		this.lblCompany.setWidthFull();
		this.lblCompany.setHeight(null);
		this.lblEnvironment.setWidthFull();
		this.lblEnvironment.setHeight(null);
		this.verticalLayout.add(this.lblCompany, this.lblEnvironment);
		this.image.setSizeUndefined();
		this.navMenuBar.setWidth("90%");
		this.navMenuBar.setHeight("40px");
		this.profileMenubar.setWidth("100px");
		this.profileMenubar.setHeight("40px");
		this.verticalLayout.setWidth("18%");
		this.verticalLayout.setHeight("40px");
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("20px");
		this.horizontalLayout.add(this.image, this.navMenuBar, this.profileMenubar, this.verticalLayout,
			this.horizontalLayout2);
		this.horizontalLayout.setFlexGrow(1.0, this.image);
		this.tabs.setWidthFull();
		this.tabs.setHeight("60px");
		this.contentContainer.add(this.tabs);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("40px");
		this.contentContainer.setSizeFull();
		this.add(this.horizontalLayout, this.contentContainer);
		this.setSizeFull();
		
		this.tabs.setSelectedIndex(-1);
	} // </generated-code>

	// <generated-code name="variables">
	private MenuBar          navMenuBar, profileMenubar;
	private Image            image;
	private HorizontalLayout horizontalLayout, horizontalLayout2;
	private VerticalLayout   verticalLayout, contentContainer;
	private Label            lblCompany, lblEnvironment;
	private Tabs             tabs;
	// </generated-code>

}
