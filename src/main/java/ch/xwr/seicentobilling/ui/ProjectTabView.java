
package ch.xwr.seicentobilling.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.ProModel;
import ch.xwr.seicentobilling.business.LovState.ProState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.ResourcePlanerHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.RowObjectAddonHandler;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.ui.crm.CustomerLookupPopup;


@Route("project")
public class ProjectTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ProjectTabView.class);

	private Vat vatdefault = null;
	
	/**
	 *
	 */
	public ProjectTabView()
	{
		super();
		this.initUI();

		// Type
		this.cbxState.setItems(LovState.State.values());
		this.cbxProModel.setItems(LovState.ProModel.values());
		this.cbxProState.setItems(LovState.ProState.values());
		
		this.sortList();

		this.verticalSplitPanel.setVisible(false);
		this.gridLayoutDesc.setVisible(false);
		
		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.tab, this.gridLayout);
		tabsToPages.put(this.tab2, this.gridLayoutDesc);
		tabsToPages.put(this.tab3, this.verticalSplitPanel);
		
		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});

		this.setVatDefault();
		
		this.setROFields();
		
		this.setDefaultFilter();
	}

	private void setVatDefault()
	{
		if(this.vatdefault == null)
		{
			final RowObjectAddonHandler addon = new RowObjectAddonHandler(null);                // company
			final String                key   = addon.getRowParameter("default", "vat", "code");
			
			final VatDAO dao = new VatDAO();
			List<Vat>    vls = dao.findByCode(key);
			if(vls.size() > 0)
			{
				this.vatdefault = vls.get(0);
			}
			else
			{
				vls = dao.findAllActive();
				if(vls.size() > 0)
				{
					this.vatdefault = vls.get(0);
				}
			}
		}
	}

	private void sortList()
	{
		final GridSortOrder<Project> sortCol1 =
			new GridSortOrder<>(this.grid.getColumnByKey("proStartDate"), SortDirection.DESCENDING);
		final GridSortOrder<Project> sortCol2 =
			new GridSortOrder<>(this.grid.getColumnByKey("proEndDate"), SortDirection.DESCENDING);
		this.grid.sort(Arrays.asList(sortCol1, sortCol2));

	}
	
	private void setROFields()
	{
		boolean hasData = true;
		if(this.binder.getBean() == null)
		{
			hasData = false;
		}

		this.setROComponents(hasData);

		this.dateProLastBill.setEnabled(false);
		this.txtProHoursEffective.setEnabled(false);
	}

	private void setROComponents(final boolean state)
	{
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		this.tabs.setEnabled(state);
		this.gridLayout.setEnabled(state);

		this.cmbCustomer.setEnabled(false);
	}

	private void setDefaultFilter()
	{
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if(bean == null)
		{
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		final FilterEntry pe =
			new FilterEntry("proState", new FilterOperator.Is().key(), new LovState.State[]{LovState.State.active});
		final FilterEntry ce =
			new FilterEntry("costAccount", new FilterOperator.Is().key(), new CostAccount[]{bean});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{ce, pe}));
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Project> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final ProjectDAO ProjectDao  = new ProjectDAO();
			final Project    ProjectBean =
				ProjectDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getProId());

			this.displayChildTables(ProjectBean);
			this.prepareCustomerCombo(ProjectBean.getCustomer());
			this.binder.setBean(ProjectBean);

			this.setDateToDatePicker(ProjectBean);

		}
		this.setROFields();
	}

	private void setDateToDatePicker(final Project projectBean)
	{
		this.setDatePicker(projectBean.getProStartDate(), this.dateProStartDate);
		this.setDatePicker(projectBean.getProEndDate(), this.dateProEndDate);
		this.setDatePicker(projectBean.getProLastBill(), this.dateProLastBill);
		
	}

	private void setDatePicker(final Date dateValue, final DatePicker datePicker)
	{
		if(dateValue != null)
		{
			datePicker.setValue(this.convertDateToLocalDate(dateValue));
		}

	}

	public LocalDate convertDateToLocalDate(final Date dateValue)
	{
		
		if(dateValue == null)
		{
			return null;
		}
		try
		{
			
			return dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			
		}
		catch(final UnsupportedOperationException e)
		{
			
		}
		
		// do this first:
		final Date safeDate = new Date(dateValue.getTime());
		
		return safeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
	}
	
	private void prepareCustomerCombo(final Customer bean)
	{
		// final List<Customer> combo = new ArrayList<>();
		// combo.add(bean);
		// ProjectTabView.this.cmbCustomer.setItems(combo);
		ProjectTabView.this.cmbCustomer.setValue(bean);
	}
	
	private void displayChildTables(final Project npro)
	{
		Project prods = null;
		if(this.binder.getBean() != null)
		{
			prods = this.binder.getBean();
		}

		if(prods == null || (npro.getProId() != prods.getProId()))
		{
			this.binder.setBean(npro);

			this.tableOrder.setItems(new OrderDAO().findByProject(npro));

			this.tableProject.setItems(new ProjectDAO().findAllChildren(npro.getProId()));
			
			this.tableOrder.getDataProvider().refreshAll();
			this.tableProject.getDataProvider().refreshAll();

		}
		
	}

	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final Project bean = this.binder.getBean();
		if(bean.getProId() == null || bean.getProId() < 1)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onClick(final ClickEvent<Button> event)
	{
		final Project bean = new Project();
		bean.setProState(LovState.State.active);
		bean.setProStartDate(new Date());
		bean.setProProjectState(LovState.ProState.grün);
		bean.setProRate(150);
		bean.setProRemark("");
		bean.setProDescription("");
		bean.setProHoursEffective(new Double(0.));
		bean.setProModel(LovState.ProModel.undefined);
		
		if(this.vatdefault != null)
		{
			bean.setVat(this.vatdefault);
		}
		
		CostAccount beanCsa = Seicento.getLoggedInCostAccount();
		if(beanCsa == null)
		{
			beanCsa = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		bean.setCostAccount(beanCsa);
		this.binder.setBean(bean);
		
		this.setROFields();
		
		// this.fieldGroup.setItemDataSource(bean);
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_onClick(final ClickEvent<Button> event)
	{
		final FilterData fd = this.containerFilterComponent.getValue();
		this.containerFilterComponent.setValue(null);

		this.grid.setDataProvider(DataProvider.ofCollection(new ProjectDAO().findAll()));

		this.sortList();
		
		this.containerFilterComponent.setValue(fd);
		final Project bean = this.binder.getBean();
		if(bean != null)
		{
			this.grid.getSelectionModel().select(bean);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfo}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */

	private void cmdInfo_onClick(final ClickEvent<Button> event)
	{

		if(this.grid.getSelectedItems() != null)
		{
			final Project bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog  win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getProId(), bean.getClass().getSimpleName()));
			win.open();
		}

	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDelete}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_onClick(final ClickEvent<Button> event)
	{
		if(this.grid.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}
		
		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final Project bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getProId(), bean.getClass().getSimpleName());
				
				final ProjectDAO dao = new ProjectDAO();
				dao.remove(bean);
				dao.flush();
				
				this.binder.removeBean();
				ProjectTabView.this.binder.setBean(new Project());
				this.grid.setDataProvider(DataProvider.ofCollection(new ProjectDAO().findAll()));
				ProjectTabView.this.grid.getDataProvider().refreshAll();
				
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				
			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
			catch(final Exception e)
			{
				ProjectTabView.LOG.error("Error on delete", e);
			}
		});
	}

	/**
	 * Event handler delegate method for the {@link VerticalLayout} {@link #verticalLayout2}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void verticalLayout2_onClick(final ClickEvent<VerticalLayout> event)
	{
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		this.setDatePickerToDate();
		if(!this.areFieldsValid())
		{
			return;
		}
		
		if(SeicentoCrud.doSave(this.binder, new ProjectDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getProId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				ProjectTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		
		this.setROFields();
		
	}
	
	private void setDatePickerToDate()
	{
		this.binder.getBean().setProStartDate(this.getDate(this.dateProStartDate));
		this.binder.getBean().setProEndDate(this.getDate(this.dateProEndDate));
		this.binder.getBean().setProLastBill(this.getDate(this.dateProLastBill));
	}

	private Date getDate(final DatePicker datePicker)
	{
		if(datePicker.getValue() != null)
		{
			final Date date =
				Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
			return date;
		}
		return null;

	}
	
	private boolean areFieldsValid()
	{
		if(this.binder.isValid())
		{
			return true;
		}
		this.binder.validate();

		return false;
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		if(this.isNew())
		{
			this.cmdNew_onClick(event);
			return;
		}
		if(this.binder.getBean() != null)
		{
			final ProjectDAO dao      = new ProjectDAO();
			final Project    cityBean = dao.find(this.binder.getBean().getProId());
			this.binder.setBean(cityBean);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdPlan}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPlan_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				5000, Notification.Position.BOTTOM_END);
			return;
		}
		
		ConfirmDialog.show("Plandaten generieren", "Datengenerierung starten?", okEvent -> {
			ProjectTabView.LOG.info("Plandatengenerierung gestartet");
			final ResourcePlanerHandler rph = new ResourcePlanerHandler();
			rph.generatePlan(ProjectTabView.this.grid.getSelectionModel().getFirstSelectedItem().get());
			Notification.show(
				"Plandaten erstellt für Projekt "
					+ ProjectTabView.this.grid.getSelectionModel().getFirstSelectedItem().get().getProName(),
				5000, Notification.Position.BOTTOM_END);
		});
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReport}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReport_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle", 5000,
				Notification.Position.BOTTOM_END);
			return;
		}
		
		final Project bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		
		final JasperManager jsp =
			new JasperManager();
		jsp.addParameter("Param_Project", "" + bean.getProId());
		jsp.addParameter("BILL_Print", "1");
		
		// jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());
		// jsp.addParameter("EmployeeId", "" + sal.getEmployee().getEmpId());
		
		UI.getCurrent().getPage().open(jsp.getUri(JasperManager.ProjectSummary1), "_blank");
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbCustomer}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCustomer_valueChanged(final ComponentValueChangeEvent<ComboBox<Customer>, Customer> event)
	{
		this.cmbBillingAddress.setItems(new ArrayList<Address>());
		if(this.cmbCustomer.getValue() != null)
		{
			// final Customer cus = (Customer) event.getProperty().getValue();
			final Customer cus = this.cmbCustomer.getValue();

			final AddressDAO dao = new AddressDAO();

			this.cmbBillingAddress.setItems(dao.findByCustomerAndType(cus, LovCrm.AddressType.business));

		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #btnSearch}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSearch_onClick(final ClickEvent<Button> event)
	{
		this.popupCustomerLookup();
	}
	
	private void popupCustomerLookup()
	{
		final Dialog win = CustomerLookupPopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");

				if(beanId != null && beanId > 0)
				{
					
					final Customer bean = new CustomerDAO().find(beanId);
					ProjectTabView.this.cmbCustomer.setValue(bean);

				}
			}
		});
		win.open();
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout              = new SplitLayout();
		this.verticalLayout           = new VerticalLayout();
		this.containerFilterComponent = new FilterComponent();
		this.proHorizontalLayout      = new HorizontalLayout();
		this.cmdNew                   = new Button();
		this.cmdDelete                = new Button();
		this.cmdReload                = new Button();
		this.cmdPlan                  = new Button();
		this.cmdReport                = new Button();
		this.cmdInfo                  = new Button();
		this.grid                     = new Grid<>(Project.class, false);
		this.verticalLayout2          = new VerticalLayout();
		this.tabs                     = new Tabs();
		this.tab                      = new Tab();
		this.tab2                     = new Tab();
		this.tab3                     = new Tab();
		this.gridLayout               = new FormLayout();
		this.formItem                 = new FormItem();
		this.cmbCustomer              = new ComboBox<>();
		this.btnSearch                = new Button();
		this.formItem2                = new FormItem();
		this.txtProName               = new TextField();
		this.formItem3                = new FormItem();
		this.txtProExtReference       = new TextField();
		this.formItem4                = new FormItem();
		this.txtProContact            = new TextField();
		this.formItem5                = new FormItem();
		this.dateProStartDate         = new DatePicker();
		this.formItem6                = new FormItem();
		this.dateProEndDate           = new DatePicker();
		this.formItem7                = new FormItem();
		this.txtProHours              = new TextField();
		this.formItem8                = new FormItem();
		this.txtProHoursEffective     = new TextField();
		this.formItem9                = new FormItem();
		this.txtProRate               = new TextField();
		this.formItem101              = new FormItem();
		this.txtProIntensityPercent   = new TextField();
		this.formItem11               = new FormItem();
		this.cmbVat                   = new ComboBox<>();
		this.formItem12               = new FormItem();
		this.cmbCostAccount           = new ComboBox<>();
		this.formItem13               = new FormItem();
		this.cmbBillingAddress        = new ComboBox<>();
		this.formItem14               = new FormItem();
		this.cbxState                 = new ComboBox<>();
		this.gridLayoutDesc           = new FormLayout();
		this.formItem15               = new FormItem();
		this.cbxProModel              = new ComboBox<>();
		this.formItem10               = new FormItem();
		this.lblCbxInternal           = new Label();
		this.cbxInternal              = new Checkbox();
		this.formItem16               = new FormItem();
		this.cmbProject               = new ComboBox<>();
		this.formItem17               = new FormItem();
		this.textArea                 = new TextArea();
		this.formItem18               = new FormItem();
		this.textAreaRem              = new TextArea();
		this.formItem19               = new FormItem();
		this.cbxProState              = new ComboBox<>();
		this.formItem20               = new FormItem();
		this.dateProLastBill          = new DatePicker();
		this.verticalSplitPanel       = new SplitLayout();
		this.verticalLayoutBill       = new VerticalLayout();
		this.horizontalLayout2        = new HorizontalLayout();
		this.icon2                    = new Icon(VaadinIcon.FILE);
		this.labelRechnungen          = new Label();
		this.tableOrder               = new Grid<>(Order.class, false);
		this.verticalLayoutSubProject = new VerticalLayout();
		this.horizontalLayout         = new HorizontalLayout();
		this.icon                     = new Icon(VaadinIcon.CUBES);
		this.subprojectLabel          = new Label();
		this.tableProject             = new Grid<>(Project.class, false);
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(Project.class);
		
		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.proHorizontalLayout.setMinHeight("");
		this.proHorizontalLayout.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdPlan.setIcon(VaadinIcon.CHART.create());
		this.cmdReport.setIcon(IronIcons.PRINT.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(Project::getProName).setKey("proName").setHeader("Name").setSortable(true);
		this.grid
			.addColumn(v -> Optional.ofNullable(v).map(Project::getCustomer).map(Customer::getShortname).orElse(null))
			.setKey("customer.shortname").setHeader("Kunde").setSortable(true);
		this.grid.addColumn(Project::getProStartDate).setKey("proStartDate").setHeader("Start").setSortable(true);
		this.grid.addColumn(Project::getProEndDate).setKey("proEndDate").setHeader("Ende").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(Project::getProExtReference).setKey("proExtReference").setHeader("Ext. Referenz")
			.setSortable(true);
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(Project::getCostAccount).map(CostAccount::getCsaName).orElse(null))
			.setKey("costAccount.csaName").setHeader("KST Name").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Project::getCostAccount)).setKey("costAccount")
			.setHeader("Kostenstelle")
			.setSortable(false).setVisible(false);
		this.grid.addColumn(Project::getProRate).setKey("proRate").setHeader("Ansatz").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Project::getProProjectState)).setKey("proProjectState")
			.setHeader("Projektstatus").setSortable(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Project::getProState)).setKey("proState").setHeader("Status")
			.setSortable(true);
		this.grid.addColumn(Project::getProHoursEffective).setKey("proHoursEffective").setHeader("Stunden Ist")
			.setSortable(true).setVisible(false);
		this.grid.addColumn(Project::getProHours).setKey("proHours").setHeader("Stunden Soll").setSortable(true)
			.setVisible(false);
		this.grid.setDataProvider(DataProvider.ofCollection(new ProjectDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setPadding(false);
		this.tabs.setMinHeight("50px");
		this.tab.setLabel(StringResourceUtils.optLocalizeString("{$gridLayout.caption}", this));
		this.tab2.setLabel(StringResourceUtils.optLocalizeString("{$gridLayout2.caption}", this));
		this.tab3.setLabel("Referenzen");
		this.gridLayout.getStyle().set("overflow-x", "hidden");
		this.gridLayout.getStyle().set("overflow-y", "auto");
		this.gridLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.cmbCustomer.setLabel(StringResourceUtils.optLocalizeString("{$lblCustomer.value}", this));
		this.cmbCustomer.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbCustomer::getItemLabelGenerator),
			DataProvider.ofCollection(new CustomerDAO().findAll()));
		this.cmbCustomer.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Customer::getFullname));
		this.btnSearch.setIcon(IronIcons.SEARCH.create());
		this.txtProName.setLabel(StringResourceUtils.optLocalizeString("{$lblProName.value}", this));
		this.txtProExtReference.setLabel(StringResourceUtils.optLocalizeString("{$lblProExtReference.value}", this));
		this.txtProContact.setLabel("Kontakt");
		this.dateProStartDate.setLocale(new Locale("de", "CH"));
		this.dateProStartDate.setLabel(StringResourceUtils.optLocalizeString("{$lblProStartDate.value}", this));
		this.dateProEndDate.setLocale(new Locale("de", "CH"));
		this.dateProEndDate.setLabel("Ende");
		this.txtProHours.setLabel("Stunden Soll");
		this.txtProHoursEffective.setLabel("Ist");
		this.txtProRate.setLabel(StringResourceUtils.optLocalizeString("{$lblProRate.value}", this));
		this.txtProIntensityPercent
			.setLabel(StringResourceUtils.optLocalizeString("{$lblProIntensityPercent.value}", this));
		this.cmbVat.setLabel(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAllInclusive()));
		this.cmbVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.cmbCostAccount.setLabel(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAllActive()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaName));
		this.cmbBillingAddress.setLabel("R-Adresse");
		this.cmbBillingAddress.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbBillingAddress::getItemLabelGenerator),
			DataProvider.ofCollection(new AddressDAO().findAll()));
		this.cmbBillingAddress.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Address::getShortname));
		this.cbxState.setLabel(StringResourceUtils.optLocalizeString("{$lblProState.value}", this));
		this.cbxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.gridLayoutDesc.getStyle().set("overflow-x", "hidden");
		this.gridLayoutDesc.getStyle().set("overflow-y", "auto");
		this.gridLayoutDesc.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.cbxProModel.setLabel(StringResourceUtils.optLocalizeString("{$lblProModel.value}", this));
		this.cbxProModel.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblCbxInternal.setText("  ");
		this.cbxInternal.setLabel("Internes Projekt");
		this.cbxInternal.getStyle().set("margin-top", "30px");
		this.cmbProject.setLabel(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.cmbProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.textArea.setLabel(StringResourceUtils.optLocalizeString("{$lblProDescription.value}", this));
		this.textAreaRem.setLabel(StringResourceUtils.optLocalizeString("{$lblProRemark.value}", this));
		this.cbxProState.setLabel(StringResourceUtils.optLocalizeString("{$lblProProjectState.value}", this));
		this.cbxProState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.dateProLastBill.setLocale(new Locale("de", "CH"));
		this.dateProLastBill.setLabel(StringResourceUtils.optLocalizeString("{$lblProLastBill.value}", this));
		this.verticalSplitPanel.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.verticalLayoutBill.setSpacing(false);
		this.verticalLayoutBill.setPadding(false);
		this.horizontalLayout2.setSpacing(false);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.labelRechnungen.setText("Rechnungen");
		this.tableOrder.addColumn(Order::getOrdNumber).setKey("ordNumber").setHeader("Rechnungsnummer")
			.setSortable(true);
		this.tableOrder.addColumn(Order::getOrdBillDate).setKey("ordBillDate").setHeader("Rechnungsdatum")
			.setSortable(true);
		this.tableOrder.addColumn(Order::getOrdAmountNet).setKey("ordAmountNet").setHeader("Betrag Netto")
			.setSortable(true)
			.setTextAlign(ColumnTextAlign.END);
		this.tableOrder.addColumn(Order::getOrdPayDate).setKey("ordPayDate").setHeader("Bezahlt am").setSortable(true);
		this.tableOrder.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayoutSubProject.setSpacing(false);
		this.verticalLayoutSubProject.setPadding(false);
		this.horizontalLayout.setSpacing(false);
		this.subprojectLabel.setText("Sub-Projekte");
		this.tableProject.addColumn(Project::getProName).setKey("proName").setHeader("Name").setSortable(true);
		this.tableProject.addColumn(Project::getProHours).setKey("proHours").setHeader("Soll-Stunden")
			.setSortable(true);
		this.tableProject.addColumn(Project::getProHoursEffective).setKey("proHoursEffective").setHeader("Ist-Stunden")
			.setSortable(true);
		this.tableProject.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.horizontalLayout3.setSpacing(false);
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		
		this.binder.forField(this.txtProName).asRequired().withNullRepresentation("").bind("proName");
		this.binder.forField(this.cmbCustomer).asRequired().bind("customer");
		this.binder.forField(this.txtProExtReference).withNullRepresentation("").bind("proExtReference");
		this.binder.forField(this.txtProContact).withNullRepresentation("").bind("proContact");
		this.binder.forField(this.txtProHoursEffective).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("proHoursEffective");
		this.binder.forField(this.txtProRate).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("proRate");
		this.binder.forField(this.txtProIntensityPercent).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("proIntensityPercent");
		this.binder.forField(this.cmbVat).bind("vat");
		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.cmbBillingAddress).bind("address");
		this.binder.forField(this.cbxProModel).bind("proModel");
		this.binder.forField(this.cbxInternal).withNullRepresentation(false).bind("internal");
		this.binder.forField(this.cmbProject).bind("project");
		this.binder.forField(this.textArea).withNullRepresentation("").bind("proDescription");
		this.binder.forField(this.textAreaRem).withNullRepresentation("").bind("proRemark");
		this.binder.forField(this.cbxState).bind("proState");
		this.binder.forField(this.cbxProState).bind("proProjectState");
		this.binder.forField(this.txtProHours).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("proHours");
		this.binder.forField(this.dateProStartDate).asRequired()
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("proStartDate");
		this.binder.forField(this.dateProEndDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("proEndDate");
		this.binder.forField(this.dateProLastBill)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("proLastBill");
		
		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("costAccount.csaCode", "customer.cusCompany", "customer.cusName", "proExtReference",
				"proName"),
			Arrays.asList("costAccount", "customer", "proEndDate", "proModel", "proName", "proProjectState",
				"proStartDate",
				"proState", "vat")));
		
		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdPlan.setSizeUndefined();
		this.cmdReport.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.proHorizontalLayout.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdPlan, this.cmdReport,
			this.cmdInfo);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.proHorizontalLayout.setWidth("100px");
		this.proHorizontalLayout.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.proHorizontalLayout, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.tabs.add(this.tab, this.tab2, this.tab3);
		this.cmbCustomer.setWidth("75%");
		this.cmbCustomer.setHeight(null);
		this.btnSearch.setSizeUndefined();
		this.formItem.add(this.cmbCustomer, this.btnSearch);
		this.txtProName.setWidthFull();
		this.txtProName.setHeight(null);
		this.formItem2.add(this.txtProName);
		this.txtProExtReference.setWidthFull();
		this.txtProExtReference.setHeight(null);
		this.formItem3.add(this.txtProExtReference);
		this.txtProContact.setWidthFull();
		this.txtProContact.setHeight(null);
		this.formItem4.add(this.txtProContact);
		this.dateProStartDate.setWidthFull();
		this.dateProStartDate.setHeight(null);
		this.formItem5.add(this.dateProStartDate);
		this.dateProEndDate.setWidthFull();
		this.dateProEndDate.setHeight(null);
		this.formItem6.add(this.dateProEndDate);
		this.txtProHours.setWidthFull();
		this.txtProHours.setHeight(null);
		this.formItem7.add(this.txtProHours);
		this.txtProHoursEffective.setWidthFull();
		this.txtProHoursEffective.setHeight(null);
		this.formItem8.add(this.txtProHoursEffective);
		this.txtProRate.setWidthFull();
		this.txtProRate.setHeight(null);
		this.formItem9.add(this.txtProRate);
		this.txtProIntensityPercent.setWidthFull();
		this.txtProIntensityPercent.setHeight(null);
		this.formItem101.add(this.txtProIntensityPercent);
		this.cmbVat.setWidthFull();
		this.cmbVat.setHeight(null);
		this.formItem11.add(this.cmbVat);
		this.cmbCostAccount.setWidthFull();
		this.cmbCostAccount.setHeight(null);
		this.formItem12.add(this.cmbCostAccount);
		this.cmbBillingAddress.setWidthFull();
		this.cmbBillingAddress.setHeight(null);
		this.formItem13.add(this.cmbBillingAddress);
		this.cbxState.setWidthFull();
		this.cbxState.setHeight(null);
		this.formItem14.add(this.cbxState);
		this.gridLayout.add(this.formItem, this.formItem2, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6,
			this.formItem7, this.formItem8, this.formItem9, this.formItem101, this.formItem11, this.formItem12,
			this.formItem13, this.formItem14);
		this.cbxProModel.setWidthFull();
		this.cbxProModel.setHeight(null);
		this.formItem15.add(this.cbxProModel);
		this.lblCbxInternal.setSizeUndefined();
		this.lblCbxInternal.getElement().setAttribute("slot", "label");
		this.cbxInternal.setWidthFull();
		this.cbxInternal.setHeight(null);
		this.formItem10.add(this.lblCbxInternal, this.cbxInternal);
		this.cmbProject.setWidthFull();
		this.cmbProject.setHeight(null);
		this.formItem16.add(this.cmbProject);
		this.textArea.setWidthFull();
		this.textArea.setHeight(null);
		this.formItem17.add(this.textArea);
		this.textAreaRem.setWidthFull();
		this.textAreaRem.setHeight(null);
		this.formItem18.add(this.textAreaRem);
		this.cbxProState.setWidthFull();
		this.cbxProState.setHeight(null);
		this.formItem19.add(this.cbxProState);
		this.dateProLastBill.setWidthFull();
		this.dateProLastBill.setHeight(null);
		this.formItem20.add(this.dateProLastBill);
		this.gridLayoutDesc.add(this.formItem15, this.formItem10, this.formItem16, this.formItem17, this.formItem18,
			this.formItem19, this.formItem20);
		this.labelRechnungen.setSizeUndefined();
		this.horizontalLayout2.add(this.icon2, this.labelRechnungen);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("30px");
		this.tableOrder.setSizeFull();
		this.verticalLayoutBill.add(this.horizontalLayout2, this.tableOrder);
		this.verticalLayoutBill.setFlexGrow(1.0, this.tableOrder);
		this.subprojectLabel.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.subprojectLabel);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.tableProject.setSizeFull();
		this.verticalLayoutSubProject.add(this.horizontalLayout, this.tableProject);
		this.verticalLayoutSubProject.setFlexGrow(1.0, this.tableProject);
		this.verticalSplitPanel.addToPrimary(this.verticalLayoutBill);
		this.verticalSplitPanel.addToSecondary(this.verticalLayoutSubProject);
		this.verticalSplitPanel.setSplitterPosition(50.0);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.tabs.setWidthFull();
		this.tabs.setHeight("36px");
		this.gridLayout.setSizeFull();
		this.gridLayoutDesc.setSizeFull();
		this.verticalSplitPanel.setSizeFull();
		this.horizontalLayout3.setSizeUndefined();
		this.verticalLayout2.add(this.tabs, this.gridLayout, this.gridLayoutDesc, this.verticalSplitPanel,
			this.horizontalLayout3);
		this.verticalLayout2.setFlexGrow(1.0, this.verticalSplitPanel);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.verticalLayout2);
		this.splitLayout.setSplitterPosition(60.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();
		
		this.tabs.setSelectedIndex(0);
		
		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdPlan.addClickListener(this::cmdPlan_onClick);
		this.cmdReport.addClickListener(this::cmdReport_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.verticalLayout2.addClickListener(this::verticalLayout2_onClick);
		this.cmbCustomer.addValueChangeListener(this::cmbCustomer_valueChanged);
		this.btnSearch.addClickListener(this::btnSearch_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Tab                           tab, tab2, tab3;
	private TextArea                      textArea, textAreaRem;
	private ComboBox<Customer>            cmbCustomer;
	private HorizontalLayout              proHorizontalLayout, horizontalLayout2, horizontalLayout, horizontalLayout3;
	private Label                         lblCbxInternal, labelRechnungen, subprojectLabel;
	private Tabs                          tabs;
	private ComboBox<ProState>            cbxProState;
	private FormItem                      formItem, formItem2, formItem3, formItem4, formItem5, formItem6, formItem7,
		formItem8, formItem9, formItem101, formItem11, formItem12, formItem13, formItem14, formItem15, formItem10,
		formItem16, formItem17, formItem18, formItem19, formItem20;
	private ComboBox<State>               cbxState;
	private SplitLayout                   splitLayout, verticalSplitPanel;
	private TextField                     txtProName, txtProExtReference, txtProContact, txtProHours,
		txtProHoursEffective,
		txtProRate, txtProIntensityPercent;
	private Icon                          icon2, icon;
	private ComboBox<Vat>                 cmbVat;
	private ComboBox<ProModel>            cbxProModel;
	private VerticalLayout                verticalLayout, verticalLayout2, verticalLayoutBill, verticalLayoutSubProject;
	private ComboBox<Address>             cmbBillingAddress;
	private FilterComponent               containerFilterComponent;
	private Grid<Order>                   tableOrder;
	private FormLayout                    gridLayout, gridLayoutDesc;
	private Checkbox                      cbxInternal;
	private Button                        cmdNew, cmdDelete, cmdReload, cmdPlan, cmdReport, cmdInfo, btnSearch, cmdSave,
		cmdReset;
	private DatePicker                    dateProStartDate, dateProEndDate, dateProLastBill;
	private ComboBox<Project>             cmbProject;
	private BeanValidationBinder<Project> binder;
	private Grid<Project>                 grid, tableProject;
	private ComboBox<CostAccount>         cmbCostAccount;
	// </generated-code>
	
}
