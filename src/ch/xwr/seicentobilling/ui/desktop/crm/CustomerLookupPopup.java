package ch.xwr.seicentobilling.ui.desktop.crm;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.util.NestedProperty;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.City_;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Customer_;

public class CustomerLookupPopup extends XdevView {
	private Customer cusBean = null;

	/**
	 *
	 */
	public CustomerLookupPopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		setDefaultFilter();
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("700");
		//win.setHeight("520");
		win.center();
		win.setModal(true);
		win.setContent(new CustomerLookupPopup());

		return win;
	}

	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		final String[] val1 = new String[] { bean.getCsaCode() };
		final FilterData[] fd = new FilterData[] {
				new FilterData("cusAccountManager", new FilterOperator.Contains(), val1) };

		this.containerFilterComponent.setFilterData(fd);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSelect}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSelect_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() != null) {
			final Customer beanId = this.table.getSelectedItem().getBean();
			UI.getCurrent().getSession().setAttribute("beanId", beanId.getCusId());

			((Window) this.getParent()).close();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnCancel_buttonClick(final Button.ClickEvent event) {
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		if (this.table.getSelectedItem() != null) {
			this.cusBean = this.table.getSelectedItem().getBean();
		}
		if (event.isDoubleClick() && this.cusBean != null) {
			UI.getCurrent().getSession().setAttribute("beanId", this.cusBean.getCusId());

			((Window) this.getParent()).close();
		}

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.table = new XdevTable<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.btnSelect = new XdevButton();
		this.btnCancel = new XdevButton();

		this.panel.setIcon(FontAwesome.SEARCH);
		this.panel.setCaption("Kontakte suchen");
		this.panel.setTabIndex(0);
		this.table.setColumnReorderingAllowed(true);
		this.table.setPageLength(10);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Customer.class, DAOs.get(CustomerDAO.class).findAll(),
				NestedProperty.of(Customer_.city, City_.ctyZip), NestedProperty.of(Customer_.city, City_.ctyName),
				NestedProperty.of(Customer_.city, City_.ctyCountry));
		this.table.setVisibleColumns("shortname", Customer_.cusAddress.getName(),
				NestedProperty.path(Customer_.city, City_.ctyZip), NestedProperty.path(Customer_.city, City_.ctyName),
				NestedProperty.path(Customer_.city, City_.ctyCountry), Customer_.cusNumber.getName(),
				Customer_.cusState.getName(), Customer_.cusAccountType.getName());
		this.table.setColumnHeader("shortname", "Name");
		this.table.setColumnHeader("cusAddress", "Adresse");
		this.table.setColumnHeader("city.ctyZip", "PLZ");
		this.table.setColumnHeader("city.ctyName", "Ort");
		this.table.setColumnHeader("city.ctyCountry", "Land");
		this.table.setColumnHeader("cusNumber", "Nummer");
		this.table.setColumnHeader("cusState", "Status");
		this.table.setColumnCollapsed("cusState", true);
		this.table.setColumnCollapsed("cusAccountType", true);
		this.horizontalLayout.setMargin(new MarginInfo(false, true, false, true));
		this.btnSelect.setCaption("Übernehmen");
		this.btnSelect.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.btnCancel.setCaption("Schliessen");
		this.btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "cusState", "cusAccountManager",
				"cusAccountType", "addresses.adrName", "city.ctyZip", "city.ctyCountry", "city.ctyName");
		this.containerFilterComponent.setSearchableProperties("cusCompany", "cusName");

		this.btnSelect.setWidth(-1, Unit.PIXELS);
		this.btnSelect.setHeight(100, Unit.PERCENTAGE);
		this.horizontalLayout.addComponent(this.btnSelect);
		this.horizontalLayout.setComponentAlignment(this.btnSelect, Alignment.MIDDLE_CENTER);
		this.btnCancel.setWidth(-1, Unit.PIXELS);
		this.btnCancel.setHeight(100, Unit.PERCENTAGE);
		this.horizontalLayout.addComponent(this.btnCancel);
		this.horizontalLayout.setComponentAlignment(this.btnCancel, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setExpandRatio(this.btnCancel, 10.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_LEFT);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_LEFT);
		this.verticalLayout.setExpandRatio(this.table, 10.0F);
		this.horizontalLayout.setSizeUndefined();
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(920, Unit.PIXELS);
		this.setHeight(560, Unit.PIXELS);

		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.btnSelect.addClickListener(event -> this.btnSelect_buttonClick(event));
		this.btnCancel.addClickListener(event -> this.btnCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton btnSelect, btnCancel;
	private XdevHorizontalLayout horizontalLayout;
	private XdevTable<Customer> table;
	private XdevPanel panel;
	private XdevVerticalLayout verticalLayout;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
