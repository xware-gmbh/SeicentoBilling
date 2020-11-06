package ch.xwr.seicentobilling.ui.desktop.project;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
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
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectLookupPopup extends XdevView {
	private Project _proBean = null;

	/**
	 *
	 */
	public ProjectLookupPopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		// State
		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());


		setDefaultFilter();
		//this.table.focus();
		this.containerFilterComponent.getSearchTextField().focus();
	}

	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		//final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final CostAccount[] val2 = new CostAccount[] { bean };
		final FilterData[] fd = new FilterData[] { new FilterData("costAccount", new FilterOperator.Is(), val2)};

		this.containerFilterComponent.setFilterData(fd);

	}



	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("700");
		//win.setHeight("520");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectLookupPopup());

		return win;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSelect}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSelect_buttonClick(final Button.ClickEvent event) {
		if (this.table.getVisibleItemIds().size() == 1) {
			this.table.select(this.table.firstItemId());
		}

		if (this.table.getSelectedItem() != null) {
			final Project beanId = this.table.getSelectedItem().getBean();
			UI.getCurrent().getSession().setAttribute("beanId", beanId.getProId());

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
			this._proBean = this.table.getSelectedItem().getBean();
		}
		if (event.isDoubleClick() && this._proBean != null) {
			UI.getCurrent().getSession().setAttribute("beanId", this._proBean.getProId());

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

		this.panel.setCaption("Lookup Projekt");
		this.panel.setTabIndex(0);
		this.containerFilterComponent.setPrefixMatchOnly(false);
		this.table.setColumnReorderingAllowed(true);
		this.table.setPageLength(10);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAllActive(),
				NestedProperty.of(Project_.customer, Customer_.cusNumber),
				NestedProperty.of("customer.shortname", String.class));
		this.table.setVisibleColumns(Project_.proName.getName(),
				NestedProperty.path(Project_.customer, Customer_.cusNumber), "customer.shortname",
				Project_.costAccount.getName(), Project_.proStartDate.getName(), Project_.proRate.getName(),
				Project_.proHours.getName(), Project_.proIntensityPercent.getName());
		this.table.setColumnHeader("proName", "Name");
		this.table.setColumnHeader("customer.cusNumber", "Kunde");
		this.table.setColumnHeader("customer.shortname", "Kundenname");
		this.table.setColumnCollapsed("customer.shortname", true);
		this.table.setColumnHeader("costAccount", "Kostenstelle");
		this.table.setColumnHeader("proStartDate", "Startdatum");
		this.table.setConverter("proStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnHeader("proRate", "Ansatz");
		this.table.setColumnCollapsed("proRate", true);
		this.table.setColumnHeader("proHours", "Stunden Soll");
		this.table.setColumnCollapsed("proHours", true);
		this.table.setColumnHeader("proIntensityPercent", "Auslastung");
		this.table.setColumnCollapsed("proIntensityPercent", true);
		this.horizontalLayout.setMargin(new MarginInfo(false, true, false, true));
		this.btnSelect.setCaption("Übernehmen");
		this.btnSelect.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.btnCancel.setCaption("Schliessen");
		this.btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "proName", "costAccount",
				"customer", "proState");
		this.containerFilterComponent.setSearchableProperties("proName", "proExtReference");

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
		this.table.setWidth(100, Unit.PERCENTAGE);
		this.table.setHeight(320, Unit.PIXELS);
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_LEFT);
		this.verticalLayout.setExpandRatio(this.table, 50.0F);
		this.horizontalLayout.setWidth(-1, Unit.PIXELS);
		this.horizontalLayout.setHeight(100, Unit.PERCENTAGE);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 30.0F);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.panel.setWidth(100, Unit.PERCENTAGE);
		this.panel.setHeight(-1, Unit.PIXELS);
		this.setContent(this.panel);
		this.setWidth(760, Unit.PIXELS);
		this.setHeight(520, Unit.PIXELS);

		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.btnSelect.addClickListener(event -> this.btnSelect_buttonClick(event));
		this.btnCancel.addClickListener(event -> this.btnCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton btnSelect, btnCancel;
	private XdevHorizontalLayout horizontalLayout;
	private XdevTable<Project> table;
	private XdevPanel panel;
	private XdevVerticalLayout verticalLayout;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
