package ch.xwr.seicentobilling.ui.desktop.project;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTabSheet;
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
import ch.xwr.seicentobilling.dal.ProjectAllocationDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Customer_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectAllocation;
import ch.xwr.seicentobilling.entities.ProjectAllocation_;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectLookupPopup extends XdevView {
	//private final Project _proBean = null;
	private boolean allowNullSelection = false;

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
		setDefaultFilter2();
		//this.table.focus();
		this.containerFilterComponent.getSearchTextField().focus();
	}

	private void setDefaultFilter2() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		//final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final CostAccount[] val2 = new CostAccount[] { bean };
		final FilterData[] fd = new FilterData[] { new FilterData("costAccount", new FilterOperator.Is(), val2)};

		this.containerFilterComponent2.setFilterData(fd);
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
		} else {
			if (isAllowNullSelection()) {
				UI.getCurrent().getSession().setAttribute("beanId", -1L);

				((Window) this.getParent()).close();

			}
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnCancel_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute("beanId", 0L);
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSelect2}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSelect2_buttonClick(final Button.ClickEvent event) {
		if (this.table2.getVisibleItemIds().size() == 1) {
			this.table2.select(this.table2.firstItemId());
		}

		if (this.table2.getSelectedItem() != null) {
			final Project beanId = this.table2.getSelectedItem().getBean().getProject();
			UI.getCurrent().getSession().setAttribute("beanId", beanId.getProId());

			((Window) this.getParent()).close();
		} else {
			if (isAllowNullSelection()) {
				UI.getCurrent().getSession().setAttribute("beanId", -1L);

				((Window) this.getParent()).close();

			}
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnCancel2}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnCancel2_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute("beanId", 0L);
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {

			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final Project obj = (Project) event.getItemId();
			this.table.select(obj); // reselect after double-click

			UI.getCurrent().getSession().setAttribute("beanId", obj.getProId());
			((Window) this.getParent()).close();

		}
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table2}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table2_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {

			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final ProjectAllocation obj = (ProjectAllocation) event.getItemId();
			this.table2.select(obj); // reselect after double-click


			UI.getCurrent().getSession().setAttribute("beanId", obj.getProject().getProId());
			((Window) this.getParent()).close();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevVerticalLayout}
	 * {@link #verticalLayout2}.
	 *
	 * @see ContextClickEvent.ContextClickListener#contextClick(ContextClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void verticalLayout2_contextClick(final ContextClickEvent event) {
		this.btnSelect.removeClickShortcut();
		this.btnSelect2.setClickShortcut(KeyCode.ENTER,null);
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.tabSheet = new XdevTabSheet();
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.table = new XdevTable<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.btnSelect = new XdevButton();
		this.btnCancel = new XdevButton();
		this.verticalLayout2 = new XdevVerticalLayout();
		this.containerFilterComponent2 = new XdevContainerFilterComponent();
		this.table2 = new XdevTable<>();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.btnSelect2 = new XdevButton();
		this.btnCancel2 = new XdevButton();

		this.setResponsive(true);
		this.panel.setCaption("Lookup Projekt");
		this.panel.setTabIndex(0);
		this.tabSheet.setStyleName("framed");
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
		this.table.setColumnCollapsed("customer.cusNumber", true);
		this.table.setColumnHeader("customer.shortname", "Kundenname");
		this.table.setColumnHeader("costAccount", "Kostenstelle");
		this.table.setColumnHeader("proStartDate", "Startdatum");
		this.table.setConverter("proStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnHeader("proRate", "Ansatz");
		this.table.setColumnCollapsed("proRate", true);
		this.table.setColumnHeader("proHours", "Stunden Soll");
		this.table.setColumnCollapsed("proHours", true);
		this.table.setColumnHeader("proIntensityPercent", "Auslastung");
		this.table.setColumnCollapsed("proIntensityPercent", true);
		this.horizontalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.btnSelect.setCaption("Übernehmen");
		this.btnSelect.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.btnCancel.setCaption("Schliessen");
		this.btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.containerFilterComponent2.setPrefixMatchOnly(false);
		this.table2.setColumnReorderingAllowed(true);
		this.table2.setPageLength(10);
		this.table2.setColumnCollapsingAllowed(true);
		this.table2.setContainerDataSource(ProjectAllocation.class,
				DAOs.get(ProjectAllocationDAO.class).findAllActiveProjects(),
				NestedProperty.of(ProjectAllocation_.project, Project_.proName),
				NestedProperty.of("project.customer.shortname", String.class),
				NestedProperty.of(ProjectAllocation_.costAccount, CostAccount_.csaName),
				NestedProperty.of(ProjectAllocation_.project, Project_.proExtReference));
		this.table2.setVisibleColumns(NestedProperty.path(ProjectAllocation_.project, Project_.proName),
				"project.customer.shortname", NestedProperty.path(ProjectAllocation_.costAccount, CostAccount_.csaName),
				NestedProperty.path(ProjectAllocation_.project, Project_.proExtReference),
				ProjectAllocation_.praStartDate.getName(), ProjectAllocation_.praEndDate.getName(),
				ProjectAllocation_.praHours.getName(), ProjectAllocation_.praRate.getName(),
				ProjectAllocation_.praIntensityPercent.getName(), ProjectAllocation_.praRemark.getName());
		this.table2.setConverter("praStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table2.setConverter("praEndDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table2.setConverter("praHours", ConverterBuilder.stringToDouble().build());
		this.table2.setColumnCollapsed("praHours", true);
		this.table2.setColumnCollapsed("praRate", true);
		this.table2.setColumnCollapsed("praIntensityPercent", true);
		this.table2.setColumnCollapsed("praRemark", true);
		this.horizontalLayout2.setMargin(new MarginInfo(false, true, true, true));
		this.btnSelect2.setCaption("Übernehmen");
		this.btnSelect2.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.btnCancel2.setCaption("Schliessen");
		this.btnCancel2.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "proName", "costAccount",
				"customer", "proState");
		this.containerFilterComponent.setSearchableProperties("proName", "proExtReference");
		this.containerFilterComponent2.setContainer(this.table2.getBeanContainerDataSource(), "costAccount", "praStartDate",
				"praEndDate");
		this.containerFilterComponent2.setSearchableProperties("praRemark", "costAccount.csaCode", "project.proName",
				"project.proExtReference");

		this.btnSelect.setSizeUndefined();
		this.horizontalLayout.addComponent(this.btnSelect);
		this.horizontalLayout.setComponentAlignment(this.btnSelect, Alignment.MIDDLE_CENTER);
		this.btnCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.btnCancel);
		this.horizontalLayout.setComponentAlignment(this.btnCancel, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setExpandRatio(this.btnCancel, 10.0F);
		this.containerFilterComponent.setSizeFull();
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_LEFT);
		this.verticalLayout.setExpandRatio(this.containerFilterComponent, 10.0F);
		this.table.setWidth(100, Unit.PERCENTAGE);
		this.table.setHeight(340, Unit.PIXELS);
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_LEFT);
		this.verticalLayout.setExpandRatio(this.table, 10.0F);
		this.horizontalLayout.setWidth(-1, Unit.PIXELS);
		this.horizontalLayout.setHeight(100, Unit.PERCENTAGE);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 10.0F);
		this.btnSelect2.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.btnSelect2);
		this.horizontalLayout2.setComponentAlignment(this.btnSelect2, Alignment.MIDDLE_CENTER);
		this.btnCancel2.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.btnCancel2);
		this.horizontalLayout2.setComponentAlignment(this.btnCancel2, Alignment.MIDDLE_CENTER);
		this.horizontalLayout2.setExpandRatio(this.btnCancel2, 10.0F);
		this.containerFilterComponent2.setSizeFull();
		this.verticalLayout2.addComponent(this.containerFilterComponent2);
		this.verticalLayout2.setComponentAlignment(this.containerFilterComponent2, Alignment.MIDDLE_LEFT);
		this.verticalLayout2.setExpandRatio(this.containerFilterComponent2, 10.0F);
		this.table2.setWidth(100, Unit.PERCENTAGE);
		this.table2.setHeight(340, Unit.PIXELS);
		this.verticalLayout2.addComponent(this.table2);
		this.verticalLayout2.setComponentAlignment(this.table2, Alignment.MIDDLE_LEFT);
		this.verticalLayout2.setExpandRatio(this.table2, 10.0F);
		this.horizontalLayout2.setWidth(-1, Unit.PIXELS);
		this.horizontalLayout2.setHeight(100, Unit.PERCENTAGE);
		this.verticalLayout2.addComponent(this.horizontalLayout2);
		this.verticalLayout2.setComponentAlignment(this.horizontalLayout2, Alignment.MIDDLE_CENTER);
		this.verticalLayout2.setExpandRatio(this.horizontalLayout2, 10.0F);
		this.verticalLayout.setSizeFull();
		this.tabSheet.addTab(this.verticalLayout, "Main", null);
		this.verticalLayout2.setSizeFull();
		this.tabSheet.addTab(this.verticalLayout2, "Ressourcen", null);
		this.tabSheet.setSelectedTab(this.verticalLayout);
		this.tabSheet.setWidth(100, Unit.PERCENTAGE);
		this.tabSheet.setHeight(-1, Unit.PIXELS);
		this.panel.setContent(this.tabSheet);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(900, Unit.PIXELS);
		this.setHeight(580, Unit.PIXELS);

		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.btnSelect.addClickListener(event -> this.btnSelect_buttonClick(event));
		this.btnCancel.addClickListener(event -> this.btnCancel_buttonClick(event));
		this.verticalLayout2.addContextClickListener(event -> this.verticalLayout2_contextClick(event));
		this.table2.addItemClickListener(event -> this.table2_itemClick(event));
		this.btnSelect2.addClickListener(event -> this.btnSelect2_buttonClick(event));
		this.btnCancel2.addClickListener(event -> this.btnCancel2_buttonClick(event));
	} // </generated-code>

	public boolean isAllowNullSelection() {
		return this.allowNullSelection;
	}

	public void setAllowNullSelection(final boolean allowNullSelection) {
		this.allowNullSelection = allowNullSelection;
	}

	// <generated-code name="variables">
	private XdevButton btnSelect, btnCancel, btnSelect2, btnCancel2;
	private XdevTable<ProjectAllocation> table2;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevTable<Project> table;
	private XdevPanel panel;
	private XdevTabSheet tabSheet;
	private XdevVerticalLayout verticalLayout, verticalLayout2;
	private XdevContainerFilterComponent containerFilterComponent, containerFilterComponent2;
	// </generated-code>

}
