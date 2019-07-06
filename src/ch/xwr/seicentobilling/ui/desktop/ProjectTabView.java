package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextArea;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.masterdetail.MasterDetail;
import com.xdev.ui.util.NestedProperty;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.ResourcePlanerHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Order_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat;

public class ProjectTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProjectTabView.class);

	/**
	 *
	 */
	public ProjectTabView() {
		super();
		this.initUI();

		// dummy (hight get lost)
		this.tabSheet.setWidth(100, Unit.PERCENTAGE);
		this.tabSheet.setHeight(-1, Unit.PIXELS);

		// Type
		this.cbxState.addItems((Object[]) LovState.State.values());
		this.cbxProModel.addItems((Object[]) LovState.ProModel.values());
		this.cbxProState.addItems((Object[]) LovState.ProState.values());

		// sort Table
		final Object[] properties = { "proStartDate", "proEndDate" };
		final boolean[] ordering = { false, false };
		this.table.sort(properties, ordering);

		// RO
		// set RO Fields
		setROFields();

		setDefaultFilter();
	}

	private void setROFields() {
		this.dateProLastBill.setEnabled(false);
		this.txtProHoursEffective.setEnabled(false);

		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null) {
			hasData = false;
		}

		setROComponents(hasData);
	}

	private void setROComponents(final boolean state) {
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		this.tabSheet.setEnabled(state);
	}


	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final CostAccount[] val2 = new CostAccount[] { bean };
		final FilterData[] fd = new FilterData[] { new FilterData("costAccount", new FilterOperator.Is(), val2),
				new FilterData("proState", new FilterOperator.Is(), valState) };

		this.containerFilterComponent.setFilterData(fd);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		final Project bean = new Project();
		bean.setProState(LovState.State.active);
		bean.setProStartDate(new Date());
		bean.setProProjectState(LovState.ProState.grün);
		bean.setProRate(150);
		bean.setProRemark("");
		bean.setProDescription("");
		bean.setProModel(LovState.ProModel.undefined);

		final VatDAO dao = new VatDAO();
		final List<Vat> list = dao.findAll();
		for (final Vat vat : list) {
			if (vat.getVatState() == LovState.State.active) {
				bean.setVat(vat);
			}
		}

		CostAccount beanCsa = Seicento.getLoggedInCostAccount();
		if (beanCsa == null) {
			beanCsa = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		bean.setCostAccount(beanCsa);

		this.fieldGroup.setItemDataSource(bean);
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDelete}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

		ConfirmDialog.show(getUI(), "Datensatz löschen", "Wirklich löschen?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					doDelete();
				}
			}

			private void doDelete() {
				final Project bean = ProjectTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getProId(), bean.getClass().getSimpleName());

				ProjectTabView.this.table.removeItem(bean);
				ProjectTabView.this.table.select(null);

				final ProjectDAO dao = new ProjectDAO();
				dao.remove(bean);

				ProjectTabView.this.fieldGroup.clear();
				setROComponents(false);

//				try {
//					ProjectTabView.this.table.select(ProjectTabView.this.table.getCurrentPageFirstItemId());
//				} catch (final Exception e) {
//					// ignore
//					ProjectTabView.this.fieldGroup.setItemDataSource(new Project());
//				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
//		this.table.refreshRowCache();
//		this.table.getBeanContainerDataSource().refresh();
//		this.table.sort();

		// save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		// clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new ProjectDAO().findAll());

		// sort Table
		final Object[] properties = { "proStartDate", "proEndDate" };
		final boolean[] ordering = { false, false };
		this.table.sort(properties, ordering);

		// reassign filter
		this.containerFilterComponent.setFilterData(fd);

		final Project bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean != null) {
			//final boolean exi = this.table.containsId(bean);
			//final com.vaadin.data.Item x = this.table.getItem(bean);
			this.table.select(bean);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final Project bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getProId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReport_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}
		final Project bean = this.table.getSelectedItem().getBean();

		final JasperManager jsp = new JasperManager();
		jsp.addParameter("Param_Project", "" + bean.getProId());
		jsp.addParameter("BILL_Print", "1");

//		jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());
//		jsp.addParameter("EmployeeId", "" + sal.getEmployee().getEmpId());

		Page.getCurrent().open(jsp.getUri(JasperManager.ProjectSummary1), "_blank");

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		setROFields();

		if (this.table.getSelectedItem() != null) {
			displayChildTables();
		}

	}

	private void displayChildTables() {
		final Project pro = this.table.getSelectedItem().getBean();

		this.tableOrder.clear();
		this.tableOrder.removeAllItems();
		this.tableOrder.addItems(pro.getOrders());

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdPlan}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPlan_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Plandaten generieren", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

		ConfirmDialog.show(getUI(), "Plandaten generieren", "Datengenerierung starten?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if ("cmdOk".equals(retval)) {
					LOG.info("Plandatengenerierung gestartet");
					final ResourcePlanerHandler rph = new ResourcePlanerHandler();
					rph.generatePlan(ProjectTabView.this.table.getSelectedItem().getBean());
					Notification.show("Plandaten generieren",
							"Plandaten erstellt für Projekt "
									+ ProjectTabView.this.table.getSelectedItem().getBean().getProName(),
							Notification.Type.ASSISTIVE_NOTIFICATION);
				}
			}
		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (!areFieldsValid()) {
			return;
		}

		try {
			this.fieldGroup.save();

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getProId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

		cmdReload_buttonClick(event);
		setROFields();
		this.table.select(this.fieldGroup.getItemDataSource().getBean());
	}

	@SuppressWarnings("unchecked")
	private boolean areFieldsValid() {
		if (this.fieldGroup.isValid()) {
			return true;
		}
		AbstractField<T> fld = null;
		try {
			final Collection<?> flds = this.fieldGroup.getFields();
			for (final Iterator<?> iterator = flds.iterator(); iterator.hasNext();) {
				fld = (AbstractField<T>) iterator.next();
				if (!fld.isValid()) {
					fld.focus();
					fld.validate();
				}
			}

		} catch (final Exception e) {
			final Object prop = this.fieldGroup.getPropertyId(fld);
			Notification.show("Feld ist ungültig", prop.toString(), Notification.Type.ERROR_MESSAGE);
		}

		return false;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		if (isNew()) {
			cmdNew_buttonClick(event);
		} else {
			this.fieldGroup.discard();
		}
		setROFields();
	}

	private boolean isNew() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return true;
		}
		final Project bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean.getProId() == null || bean.getProId() < 1) {
			return true;
		}
		return false;
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbCustomer}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCustomer_valueChange(final Property.ValueChangeEvent event) {
		this.cmbBillingAddress.clear();
		this.cmbBillingAddress.removeAllItems();
		if (this.cmbCustomer.getSelectedItem() != null) {
			// final Customer cus = (Customer) event.getProperty().getValue();
			final Customer cus = this.cmbCustomer.getSelectedItem().getBean();

			final AddressDAO dao = new AddressDAO();

			this.cmbBillingAddress.addItems(dao.findByCustomerAndType(cus, LovCrm.AddressType.business));

		}
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdPlan = new XdevButton();
		this.cmdReport = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.gridLayoutData = new XdevGridLayout();
		this.tabSheet = new XdevTabSheet();
		this.gridLayout = new XdevGridLayout();
		this.lblCustomer = new XdevLabel();
		this.cmbCustomer = new XdevComboBox<>();
		this.lblProName = new XdevLabel();
		this.txtProName = new XdevTextField();
		this.lblProExtReference = new XdevLabel();
		this.txtProExtReference = new XdevTextField();
		this.lblProContact = new XdevLabel();
		this.txtProContact = new XdevTextField();
		this.lblProStartDate = new XdevLabel();
		this.dateProStartDate = new XdevPopupDateField();
		this.lblProEndDate = new XdevLabel();
		this.dateProEndDate = new XdevPopupDateField();
		this.lblProHours = new XdevLabel();
		this.txtProHours = new XdevTextField();
		this.lblProHoursEffective = new XdevLabel();
		this.txtProHoursEffective = new XdevTextField();
		this.lblProIntensityPercent = new XdevLabel();
		this.txtProIntensityPercent = new XdevTextField();
		this.lblProRate = new XdevLabel();
		this.txtProRate = new XdevTextField();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblBillingAddress = new XdevLabel();
		this.cmbBillingAddress = new XdevComboBox<>();
		this.lblProState = new XdevLabel();
		this.cbxState = new XdevComboBox<>();
		this.gridLayout2 = new XdevGridLayout();
		this.lblProModel = new XdevLabel();
		this.cbxProModel = new XdevComboBox<>();
		this.cbxInternal = new XdevCheckBox();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblProDescription = new XdevLabel();
		this.textArea = new XdevTextArea();
		this.lblProRemark = new XdevLabel();
		this.textAreaRem = new XdevTextArea();
		this.lblProProjectState = new XdevLabel();
		this.cbxProState = new XdevComboBox<>();
		this.lblProLastBill = new XdevLabel();
		this.dateProLastBill = new XdevPopupDateField();
		this.gridLayoutRef = new XdevGridLayout();
		this.verticalLayout2 = new XdevVerticalLayout();
		this.tableOrder = new XdevTable<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Project.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDelete.setDescription("Projekt löschen");
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdReload.setDescription("Liste neu laden");
		this.cmdPlan.setIcon(FontAwesome.AREA_CHART);
		this.cmdPlan.setDescription(StringResourceUtils.optLocalizeString("{$cmdPlan.description}", this));
		this.cmdPlan.setImmediate(true);
		this.cmdReport.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/Printer_black_18.png"));
		this.cmdReport.setDescription("Jasper Report starten");
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.cmdInfo.setDescription("Objektstamm aufrufen");
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll(),
				NestedProperty.of("customer.shortname", String.class),
				NestedProperty.of(Project_.costAccount, CostAccount_.csaName));
		this.table.setVisibleColumns(Project_.proName.getName(), "customer.shortname", Project_.proStartDate.getName(),
				Project_.proEndDate.getName(), Project_.proExtReference.getName(),
				NestedProperty.path(Project_.costAccount, CostAccount_.csaName), Project_.costAccount.getName(),
				Project_.proRate.getName(), Project_.proProjectState.getName(), Project_.proState.getName());
		this.table.setColumnHeader("proName", "Name");
		this.table.setColumnHeader("customer.shortname", "Kunde");
		this.table.setColumnHeader("proStartDate", "Start");
		this.table.setConverter("proStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnHeader("proEndDate", "Ende");
		this.table.setConverter("proEndDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnCollapsed("proEndDate", true);
		this.table.setColumnHeader("proExtReference", "Ext. Referenz");
		this.table.setColumnHeader("costAccount.csaName", "KST Name");
		this.table.setColumnHeader("costAccount", "Kostenstelle");
		this.table.setColumnCollapsed("costAccount", true);
		this.table.setColumnHeader("proRate", "Ansatz");
		this.table.setColumnAlignment("proRate", Table.Align.RIGHT);
		this.table.setConverter("proRate", ConverterBuilder.stringToDouble().currency().build());
		this.table.setColumnCollapsed("proRate", true);
		this.table.setColumnHeader("proProjectState", "Projektstatus");
		this.table.setColumnCollapsed("proProjectState", true);
		this.table.setColumnHeader("proState", "Status");
		this.gridLayoutData.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.lblCustomer.setValue(StringResourceUtils.optLocalizeString("{$lblCustomer.value}", this));
		this.cmbCustomer.setRequired(true);
		this.cmbCustomer.setItemCaptionFromAnnotation(false);
		this.cmbCustomer.setContainerDataSource(Customer.class, DAOs.get(CustomerDAO.class).findAll());
		this.cmbCustomer.setItemCaptionPropertyId("fullname");
		this.lblProName.setValue(StringResourceUtils.optLocalizeString("{$lblProName.value}", this));
		this.txtProName.setRequired(true);
		this.txtProName.setMaxLength(50);
		this.lblProExtReference.setValue(StringResourceUtils.optLocalizeString("{$lblProExtReference.value}", this));
		this.txtProExtReference.setMaxLength(50);
		this.lblProContact.setValue("Kontakt");
		this.txtProContact.setMaxLength(50);
		this.lblProStartDate.setValue(StringResourceUtils.optLocalizeString("{$lblProStartDate.value}", this));
		this.dateProStartDate.setRequired(true);
		this.lblProEndDate.setValue("Ende");
		this.lblProHours.setValue("Stunden Soll");
		this.txtProHours
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.lblProHoursEffective.setValue("Ist");
		this.txtProHoursEffective
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.lblProIntensityPercent
				.setValue(StringResourceUtils.optLocalizeString("{$lblProIntensityPercent.value}", this));
		this.lblProRate.setValue(StringResourceUtils.optLocalizeString("{$lblProRate.value}", this));
		this.txtProRate
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.txtProRate.setRequired(true);
		this.lblCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setItemCaptionFromAnnotation(false);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class);
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaName.getName());
		this.lblBillingAddress.setValue("R-Adresse");
		this.cmbBillingAddress.setItemCaptionFromAnnotation(false);
		this.cmbBillingAddress.setContainerDataSource(Address.class, false);
		this.cmbBillingAddress.setItemCaptionPropertyId("shortname");
		this.lblProState.setValue(StringResourceUtils.optLocalizeString("{$lblProState.value}", this));
		this.lblProModel.setValue(StringResourceUtils.optLocalizeString("{$lblProModel.value}", this));
		this.cbxInternal.setCaption("Internes Projekt");
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setRequired(true);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setContainerDataSource(Vat.class);
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setContainerDataSource(Project.class);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblProDescription.setValue(StringResourceUtils.optLocalizeString("{$lblProDescription.value}", this));
		this.textArea.setRows(5);
		this.lblProRemark.setValue(StringResourceUtils.optLocalizeString("{$lblProRemark.value}", this));
		this.textAreaRem.setRows(2);
		this.lblProProjectState.setValue(StringResourceUtils.optLocalizeString("{$lblProProjectState.value}", this));
		this.lblProLastBill.setValue(StringResourceUtils.optLocalizeString("{$lblProLastBill.value}", this));
		this.gridLayoutRef.setMargin(new MarginInfo(false));
		this.verticalLayout2.setMargin(new MarginInfo(false));
		this.tableOrder.setCaption("Rechnungen");
		this.tableOrder.setIcon(FontAwesome.FILE);
		this.tableOrder.setContainerDataSource(Order.class, false);
		this.tableOrder.setVisibleColumns(Order_.ordNumber.getName(), Order_.ordBillDate.getName(),
				Order_.ordAmountNet.getName(), Order_.ordPayDate.getName());
		this.tableOrder.setColumnHeader("ordNumber", "Rechnungsnummer");
		this.tableOrder.setConverter("ordNumber",
				ConverterBuilder.stringToInteger().groupingUsed(false).decimalSeparatorAlwaysShown(false).build());
		this.tableOrder.setColumnHeader("ordBillDate", "Rechnungsdatum");
		this.tableOrder.setConverter("ordBillDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableOrder.setColumnHeader("ordAmountNet", "Betrag Netto");
		this.tableOrder.setColumnAlignment("ordAmountNet", Table.Align.RIGHT);
		this.tableOrder.setConverter("ordAmountNet",
				ConverterBuilder.stringToDouble().currency().minimumFractionDigits(2).build());
		this.tableOrder.setColumnHeader("ordPayDate", "Bezahlt am");
		this.tableOrder.setConverter("ordPayDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.horizontalLayout.setMargin(new MarginInfo(false, false, true, true));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.fieldGroup.bind(this.cmbCustomer, Project_.customer.getName());
		this.fieldGroup.bind(this.txtProName, Project_.proName.getName());
		this.fieldGroup.bind(this.txtProExtReference, Project_.proExtReference.getName());
		this.fieldGroup.bind(this.dateProStartDate, Project_.proStartDate.getName());
		this.fieldGroup.bind(this.dateProEndDate, Project_.proEndDate.getName());
		this.fieldGroup.bind(this.txtProHours, Project_.proHours.getName());
		this.fieldGroup.bind(this.txtProHoursEffective, Project_.proHoursEffective.getName());
		this.fieldGroup.bind(this.txtProIntensityPercent, Project_.proIntensityPercent.getName());
		this.fieldGroup.bind(this.txtProRate, Project_.proRate.getName());
		this.fieldGroup.bind(this.cmbCostAccount, Project_.costAccount.getName());
		this.fieldGroup.bind(this.cbxState, Project_.proState.getName());
		this.fieldGroup.bind(this.cbxProModel, Project_.proModel.getName());
		this.fieldGroup.bind(this.cmbVat, Project_.vat.getName());
		this.fieldGroup.bind(this.cmbProject, Project_.project.getName());
		this.fieldGroup.bind(this.textArea, Project_.proDescription.getName());
		this.fieldGroup.bind(this.textAreaRem, Project_.proRemark.getName());
		this.fieldGroup.bind(this.cbxProState, Project_.proProjectState.getName());
		this.fieldGroup.bind(this.dateProLastBill, Project_.proLastBill.getName());
		this.fieldGroup.bind(this.txtProContact, Project_.proContact.getName());
		this.fieldGroup.bind(this.cmbBillingAddress, Project_.address.getName());
		this.fieldGroup.bind(this.cbxInternal, Project_.internal.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "proStartDate", "proEndDate",
				"vat", "customer", "costAccount", "proModel", "proState", "proProjectState");
		this.containerFilterComponent.setSearchableProperties("proName", "proExtReference", "customer.cusName",
				"customer.cusCompany", "costAccount.csaCode");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdPlan.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdPlan);
		this.actionLayout.setComponentAlignment(this.cmdPlan, Alignment.MIDDLE_CENTER);
		this.cmdReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReport);
		this.actionLayout.setComponentAlignment(this.cmdReport, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		final CustomComponent actionLayout_spacer = new CustomComponent();
		actionLayout_spacer.setSizeFull();
		this.actionLayout.addComponent(actionLayout_spacer);
		this.actionLayout.setExpandRatio(actionLayout_spacer, 1.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.actionLayout.setWidth(100, Unit.PERCENTAGE);
		this.actionLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.actionLayout);
		this.verticalLayout.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.gridLayout.setColumns(4);
		this.gridLayout.setRows(11);
		this.lblCustomer.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCustomer, 0, 0);
		this.cmbCustomer.setWidth(100, Unit.PERCENTAGE);
		this.cmbCustomer.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.cmbCustomer, 1, 0, 3, 0);
		this.lblProName.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProName, 0, 1);
		this.txtProName.setWidth(100, Unit.PERCENTAGE);
		this.txtProName.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtProName, 1, 1, 3, 1);
		this.lblProExtReference.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProExtReference, 0, 2);
		this.txtProExtReference.setWidth(100, Unit.PERCENTAGE);
		this.txtProExtReference.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtProExtReference, 1, 2);
		this.lblProContact.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProContact, 2, 2);
		this.txtProContact.setWidth(100, Unit.PERCENTAGE);
		this.txtProContact.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtProContact, 3, 2);
		this.lblProStartDate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProStartDate, 0, 3);
		this.dateProStartDate.setSizeUndefined();
		this.gridLayout.addComponent(this.dateProStartDate, 1, 3);
		this.lblProEndDate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProEndDate, 2, 3);
		this.dateProEndDate.setSizeUndefined();
		this.gridLayout.addComponent(this.dateProEndDate, 3, 3);
		this.lblProHours.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProHours, 0, 4);
		this.txtProHours.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProHours, 1, 4);
		this.lblProHoursEffective.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProHoursEffective, 2, 4);
		this.txtProHoursEffective.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProHoursEffective, 3, 4);
		this.lblProIntensityPercent.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProIntensityPercent, 0, 5);
		this.txtProIntensityPercent.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProIntensityPercent, 1, 5);
		this.lblProRate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProRate, 0, 6);
		this.txtProRate.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProRate, 1, 6);
		this.lblCostAccount.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCostAccount, 0, 7);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.cmbCostAccount, 1, 7, 2, 7);
		this.lblBillingAddress.setSizeUndefined();
		this.gridLayout.addComponent(this.lblBillingAddress, 0, 8);
		this.cmbBillingAddress.setWidth(100, Unit.PERCENTAGE);
		this.cmbBillingAddress.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.cmbBillingAddress, 1, 8, 2, 8);
		this.lblProState.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProState, 0, 9);
		this.cbxState.setSizeUndefined();
		this.gridLayout.addComponent(this.cbxState, 1, 9);
		this.gridLayout.setColumnExpandRatio(1, 100.0F);
		this.gridLayout.setColumnExpandRatio(3, 100.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 10, 3, 10);
		this.gridLayout.setRowExpandRatio(10, 1.0F);
		this.gridLayout2.setColumns(3);
		this.gridLayout2.setRows(8);
		this.lblProModel.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProModel, 0, 0);
		this.cbxProModel.setSizeUndefined();
		this.gridLayout2.addComponent(this.cbxProModel, 1, 0);
		this.cbxInternal.setSizeUndefined();
		this.gridLayout2.addComponent(this.cbxInternal, 2, 0);
		this.lblVat.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblVat, 0, 1);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.cmbVat, 1, 1);
		this.lblProject.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProject, 0, 2);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.cmbProject, 1, 2, 2, 2);
		this.lblProDescription.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProDescription, 0, 3);
		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.textArea, 1, 3, 2, 3);
		this.lblProRemark.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProRemark, 0, 4);
		this.textAreaRem.setWidth(100, Unit.PERCENTAGE);
		this.textAreaRem.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.textAreaRem, 1, 4, 2, 4);
		this.lblProProjectState.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProProjectState, 0, 5);
		this.cbxProState.setSizeUndefined();
		this.gridLayout2.addComponent(this.cbxProState, 1, 5);
		this.lblProLastBill.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblProLastBill, 0, 6);
		this.dateProLastBill.setWidth(100, Unit.PERCENTAGE);
		this.dateProLastBill.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.dateProLastBill, 1, 6);
		this.gridLayout2.setColumnExpandRatio(1, 100.0F);
		this.gridLayout2.setColumnExpandRatio(2, 100.0F);
		final CustomComponent gridLayout2_vSpacer = new CustomComponent();
		gridLayout2_vSpacer.setSizeFull();
		this.gridLayout2.addComponent(gridLayout2_vSpacer, 0, 7, 2, 7);
		this.gridLayout2.setRowExpandRatio(7, 1.0F);
		this.tableOrder.setSizeFull();
		this.verticalLayout2.addComponent(this.tableOrder);
		this.verticalLayout2.setComponentAlignment(this.tableOrder, Alignment.MIDDLE_CENTER);
		this.verticalLayout2.setExpandRatio(this.tableOrder, 100.0F);
		this.gridLayoutRef.setColumns(1);
		this.gridLayoutRef.setRows(1);
		this.verticalLayout2.setSizeFull();
		this.gridLayoutRef.addComponent(this.verticalLayout2, 0, 0);
		this.gridLayoutRef.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutRef.setRowExpandRatio(0, 10.0F);
		this.gridLayout.setSizeFull();
		this.tabSheet.addTab(this.gridLayout, StringResourceUtils.optLocalizeString("{$gridLayout.caption}", this), null);
		this.gridLayout2.setSizeFull();
		this.tabSheet.addTab(this.gridLayout2, StringResourceUtils.optLocalizeString("{$gridLayout2.caption}", this), null);
		this.gridLayoutRef.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutRef, "Referenzen", null);
		this.tabSheet.setSelectedTab(this.gridLayout);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_CENTER);
		this.gridLayoutData.setColumns(1);
		this.gridLayoutData.setRows(2);
		this.tabSheet.setSizeFull();
		this.gridLayoutData.addComponent(this.tabSheet, 0, 0);
		this.horizontalLayout.setSizeUndefined();
		this.gridLayoutData.addComponent(this.horizontalLayout, 0, 1);
		this.gridLayoutData.setComponentAlignment(this.horizontalLayout, Alignment.TOP_CENTER);
		this.gridLayoutData.setColumnExpandRatio(0, 100.0F);
		this.gridLayoutData.setRowExpandRatio(0, 100.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.gridLayoutData.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.gridLayoutData);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdPlan.addClickListener(event -> this.cmdPlan_buttonClick(event));
		this.cmdReport.addClickListener(event -> this.cmdReport_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.cmbCustomer.addValueChangeListener(event -> this.cmbCustomer_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdPlan, cmdReport, cmdInfo, cmdSave, cmdReset;
	private XdevLabel lblCustomer, lblProName, lblProExtReference, lblProContact, lblProStartDate, lblProEndDate,
			lblProHours, lblProHoursEffective, lblProIntensityPercent, lblProRate, lblCostAccount, lblBillingAddress,
			lblProState, lblProModel, lblVat, lblProject, lblProDescription, lblProRemark, lblProProjectState,
			lblProLastBill;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevFieldGroup<Project> fieldGroup;
	private XdevComboBox<Address> cmbBillingAddress;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayoutData, gridLayout, gridLayout2, gridLayoutRef;
	private XdevComboBox<Project> cmbProject;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevHorizontalLayout actionLayout, horizontalLayout;
	private XdevComboBox<Vat> cmbVat;
	private XdevPopupDateField dateProStartDate, dateProEndDate, dateProLastBill;
	private XdevTextArea textArea, textAreaRem;
	private XdevTable<Project> table;
	private XdevComboBox<?> cbxState, cbxProModel, cbxProState;
	private XdevComboBox<Customer> cmbCustomer;
	private XdevCheckBox cbxInternal;
	private XdevTable<Order> tableOrder;
	private XdevTextField txtProName, txtProExtReference, txtProContact, txtProHours, txtProHoursEffective,
			txtProIntensityPercent, txtProRate;
	private XdevVerticalLayout verticalLayout, verticalLayout2;
	// </generated-code>

}
