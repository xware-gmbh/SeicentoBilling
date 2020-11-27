
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;


/**
 *
 */
@Route("")
@HtmlImport("frontend://styles/shared-styles.html")
@HtmlImport("frontend://styles/shared-styles.html")
@HtmlImport("frontend://styles/my-menubar.html")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout implements PageConfigurator, RouterLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MainContainer.class);
	
	/**
	 *
	 */
	public MainContainer()
	{
		super();
		this.setLocale();
		this.initUI();
		this.navMenuBar.setHeight("20px");

		// final MenuItem itemHome = this.navMenuBar.addItem("Verkauf");
		// itemHome.addClickListener(e -> UI.getCurrent().navigate(MainView.class));
		final Map<String, String[]> mainMenu = new LinkedHashMap<>();
		mainMenu.put("Verkauf",
			new String[]{"Rechnung", "Artikel", "Kontakte", "Rechnungen generieren..."});
		mainMenu.put("Spesen u. Rapporte", new String[]{"Spesen", "Rapporte", "Vorlagen Rapport", "Vorlagen Spesen"});
		mainMenu.put("Projekt",
			new String[]{"Projekte"});
		mainMenu.put("Hilfstabellen",
			new String[]{"Kostenstelle", "Mwst", "Ortschaft"});
		mainMenu.put("Optionen",
			new String[]{"Firma", "Objektstamm"});

		final Map<String, Class> menuPages = new LinkedHashMap<>();
		menuPages.put("Rechnung", View2.class);
		menuPages.put("Artikel", ItemTabView.class);
		menuPages.put("Kontakte", View4.class);
		menuPages.put("Rechnungen generieren...", View1.class);
		
		menuPages.put("Spesen u. Rapporte", View1.class);
		menuPages.put("Spesen", ExpenseTabView.class);
		menuPages.put("Rapporte", View3.class);
		menuPages.put("Vorlagen Rapport", ProjectLineTemplateTabView.class);
		menuPages.put("Vorlagen Spesen", ExpenseTemplateTabView.class);

		menuPages.put("Projekte", View1.class);
		menuPages.put("Projekte", View2.class);
		
		menuPages.put("Hilfstabellen", View1.class);
		menuPages.put("Kostenstelle", CostAccountTabView.class);
		menuPages.put("Mwst", VatTabView.class);
		menuPages.put("Ortschaft", CityTabView.class);
		
		menuPages.put("Optionen", View1.class);
		menuPages.put("Firma", CompanyTabView.class);
		menuPages.put("Objektstamm", View3.class);
		
		for(final String key : mainMenu.keySet())
		{
			final MenuItem mainMenuItem    = this.navMenuBar.addItem(key);
			final String[] subMenuItemList = mainMenu.get(key);
			for(final String subm : subMenuItemList)
			{
				final SubMenu  subMenu     = mainMenuItem.getSubMenu();
				final MenuItem subMenuItem = subMenu.addItem(subm);
				// subMenuItem.addClickListener(e -> UI.getCurrent().navigate(menuPages.get(subm)));
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

		// this.lblEnvironment.setValue(dao.getDbNameNativeSQL());
		// this.lblCompany.setValue(cmp.getCmpName());

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
			final Button close = new Button(VaadinIcon.CLOSE.create(),
				click -> {
					this.tabs.remove(tab);
					final Component cmpv = this.tabsToPages.get(tab);
					this.div.remove(cmpv);
					this.tabsToPages.remove(tab);
				});
			tab.addComponentAsFirst(close);
			viw.setVisible(true);
			this.tabsToPages.put(tab, viw);
			this.div.add(viw);
			this.tabs.setSelectedTab(tab);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.navMenuBar       = new MenuBar();
		this.contentContainer = new FlexLayout();
		this.verticalLayout   = new VerticalLayout();
		this.tabs             = new Tabs();
		this.div              = new Div();
		
		this.setSpacing(false);
		this.setPadding(false);
		this.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		this.navMenuBar.setClassName("my-menubar");
		this.navMenuBar.setMinHeight("60px");
		this.contentContainer.setMinHeight("");
		this.contentContainer.setMaxWidth("");
		this.verticalLayout.setMinHeight("");
		this.verticalLayout.setMaxHeight("");
		this.verticalLayout.setMaxWidth("");
		this.verticalLayout.setMinWidth("100%");
		this.tabs.setMinHeight("null");
		this.tabs.setMinWidth("100%");
		this.div.setMinHeight("");
		this.div.setMinWidth("");
		
		this.tabs.setWidthFull();
		this.tabs.setHeight("5px");
		this.div.setSizeFull();
		this.verticalLayout.add(this.tabs, this.div);
		this.verticalLayout.setFlexGrow(1.0, this.div);
		this.verticalLayout.setSizeFull();
		this.contentContainer.add(this.verticalLayout);
		this.navMenuBar.setSizeUndefined();
		this.contentContainer.setSizeFull();
		this.add(this.navMenuBar, this.contentContainer);
		this.setFlexGrow(1.0, this.contentContainer);
		this.setSizeFull();
		
		this.tabs.setSelectedIndex(-1);
	} // </generated-code>

	// <generated-code name="variables">
	private MenuBar        navMenuBar;
	private FlexLayout     contentContainer;
	private VerticalLayout verticalLayout;
	private Div            div;
	private Tabs           tabs;
	// </generated-code>

}
