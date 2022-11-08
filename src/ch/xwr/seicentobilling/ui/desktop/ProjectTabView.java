package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
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
import com.xdev.ui.XdevVerticalSplitPanel;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.util.NestedProperty;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.ResourcePlanerHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.RowObjectAddonHandler;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.ProjectAllocationDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Order_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectAllocation;
import ch.xwr.seicentobilling.entities.ProjectAllocation_;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.ui.desktop.crm.CustomerLookupPopup;
import ch.xwr.seicentobilling.ui.desktop.project.ProjectAllocationPopup;
import ch.xwr.seicentobilling.ui.desktop.project.ProjectEffortDefault;

public class ProjectTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProjectTabView.class);

	private Vat vatdefault = null;

	/**
	 *
	 */
	public ProjectTabView() {
		super();
		this.initUI();

		// dummy (hight get lost)
		//this.tabSheet.setWidth(100, Unit.PERCENTAGE);
		//this.tabSheet.setHeight(-1, Unit.PIXELS);

		// Type
		this.cbxState.addItems((Object[]) LovState.State.values());
		this.cbxProModel.addItems((Object[]) LovState.ProModel.values());
		this.cbxProState.addItems((Object[]) LovState.ProState.values());
		this.cbxProOrderStrategy.addItems((Object[]) LovState.ProOrderStrategy.values());

		// sort Table
		final Object[] properties = { "proStartDate", "proEndDate" };
		final boolean[] ordering = { false, false };
		this.table.sort(properties, ordering);

		setVatDefault();
		// RO
		// set RO Fields
		setROFields();

		if (checkAutoLoad()) {

		} else {
			setDefaultFilter();
		}
	}

	private boolean checkAutoLoad() {
		final String proName = (String) UI.getCurrent().getSession().getAttribute("proName");
		Long proId = new Long(0);
		try {
			proId = Long.parseLong((String) UI.getCurrent().getSession().getAttribute("proId"));
		} catch (final Exception ignore) {
		}

		UI.getCurrent().getSession().setAttribute("proName",  "");
		UI.getCurrent().getSession().setAttribute("proId",  "");

		final ProjectDAO dao = new ProjectDAO();
		Project pro = null;
		if (proId.longValue() > 0) {
			pro = dao.find(proId);
		} else {
			if (proName != null && !proName.isEmpty()) {
				final List<Project> lst = dao.findByName(proName);
				if (lst != null && lst.size() > 0) {
					pro = lst.get(0);
				}
			}

		}

		if (pro != null) {
			this.table.removeAllItems();
			this.table.addItem(pro);

			if (this.table.containsId(pro)) {
				this.table.select(pro);
			}
			this.fieldGroup.setItemDataSource(pro);
			return true;
		}
		return false;
	}

	private void setVatDefault() {
		if (this.vatdefault == null) {
			final RowObjectAddonHandler addon = new RowObjectAddonHandler(null);  //company
			final String key = addon.getRowParameter("default", "vat", "code");

			final VatDAO dao = new VatDAO();
			List<Vat> vls = dao.findByCode(key);
			if (vls.size() > 0) {
				this.vatdefault = vls.get(0);
			} else {
				vls = dao.findAllActive();
				if (vls.size() > 0) {
					this.vatdefault = vls.get(0);
				}
			}
		}
	}

	private void setROFields() {
		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null) {
			hasData = false;
		}

		setROComponents(hasData);

		this.dateProLastBill.setEnabled(false);
		this.txtProHoursEffective.setEnabled(false);
	}

	private void setROComponents(final boolean state) {
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		this.tabSheet.setEnabled(state);

		this.cmbCustomer.setEnabled(false);
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
		final Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.YEAR, 1);

		final Project bean = new Project();
		bean.setProState(LovState.State.active);
		bean.setProStartDate(new Date());
		bean.setProEndDate(cal.getTime());
		bean.setProProjectState(LovState.ProState.grün);
		bean.setProRate(150);
		bean.setProRemark("");
		bean.setProDescription("");
		bean.setProHoursEffective(new Double(0.));
		bean.setProIntensityPercent(new Integer(80));
		bean.setProModel(LovState.ProModel.undefined);
		bean.setProOrdergenerationStrategy(LovState.ProOrderStrategy.exklusiv);

		if (this.vatdefault != null) {
			bean.setVat(this.vatdefault);
		}

		CostAccount beanCsa = Seicento.getLoggedInCostAccount();
		if (beanCsa == null) {
			beanCsa = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		bean.setCostAccount(beanCsa);

		this.fieldGroup.setItemDataSource(bean);
		setROFields();

		this.tableProjectAllocation.removeAllItems();
		this.tableOrder.removeAllItems();
		this.tableProject.removeAllItems();
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
				try {

					final Project bean = ProjectTabView.this.table.getSelectedItem().getBean();

					final ProjectDAO dao = new ProjectDAO();
					dao.remove(bean);
					dao.flush();

					// Delete Record
					final RowObjectManager man = new RowObjectManager();
					man.deleteObject(bean.getProId(), bean.getClass().getSimpleName());

					ProjectTabView.this.table.removeItem(bean);
					ProjectTabView.this.table.select(null);

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
				} catch (final PersistenceException cx) {
					final String msg = SeicentoCrud.getPerExceptionError(cx);
					Notification.show("Fehler beim Löschen", msg, Notification.Type.ERROR_MESSAGE);
					cx.printStackTrace();
				}

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
		final List<Project> lst = new ProjectDAO().findAll();
		this.table.getBeanContainerDataSource().addAll(lst);

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
		if (this.table.getSelectedItem() != null) {
			displayChildTables(this.table.getSelectedItem().getBean());
			prepareCustomerCombo(this.table.getSelectedItem().getBean().getCustomer());
		}

		setROFields();
	}

	private void displayChildTables(final Project npro) {
		Project prods = null;
		if  (this.fieldGroup.getItemDataSource() !=null) {
			prods = this.fieldGroup.getItemDataSource().getBean();
		}

		if (prods == null || (npro.getProId() != prods.getProId())) {
			this.fieldGroup.setItemDataSource(npro);

			this.tableOrder.clear();
			this.tableOrder.removeAllItems();
			this.tableOrder.addItems(new OrderDAO().findByProject(npro));

			this.tableProject.clear();
			this.tableProject.removeAllItems();
			this.tableProject.addItems(new ProjectDAO().findAllChildren(npro.getProId()));

			this.tableProjectAllocation.clear();
			this.tableProjectAllocation.removeAllItems();
			this.tableProjectAllocation.addItems(new ProjectAllocationDAO().findByProject(npro));
		}

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

		if (SeicentoCrud.doSave(this.fieldGroup)) {
			try {
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.fieldGroup.getItemDataSource().getBean().getProId(),
						this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
			} catch (final Exception e) {
				LOG.error("could not save ObjRoot", e);
			}
		}

		setROFields();
		//cmdReload_buttonClick(event);	//Slow
		//this.table.select(this.fieldGroup.getItemDataSource().getBean());
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

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSearch}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSearch_buttonClick(final Button.ClickEvent event) {
		popupCustomerLookup();

	}

	private void popupCustomerLookup() {
		final Window win = CustomerLookupPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");

				if (beanId != null && beanId > 0) {
					final Customer bean = new CustomerDAO().find(beanId);
					prepareCustomerCombo(bean);

					//nur setzene bei neuem Record
//					if (ProjectTabView.this.fieldGroup.getItemDataSource().getBean().getCusId() == null) {
//						ProjectTabView.this.txtPrlRate.setValue("" + bean.getProRate());
//					}

					//ProjectLinePopup.this.fieldGroupProject.setItemDataSource(bean);
				}
			}

		});
		this.getUI().addWindow(win);

	}

	private void prepareCustomerCombo(final Customer bean) {
		ProjectTabView.this.cmbCustomer.addItem(bean);
		ProjectTabView.this.cmbCustomer.setValue(bean);
	}


	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #dateProEndDate}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void dateProEndDate_valueChange(final Property.ValueChangeEvent event) {
		validateDateFromTo();

	}

	private void validateDateFromTo() {
		final Date dateFrom = this.dateProStartDate.getValue();
		final Date dateTo = this.dateProEndDate.getValue();

		if (dateTo != null) {
			if (dateFrom != null && dateTo.before(dateFrom)) {
				this.dateProEndDate.setValue(dateFrom);
			}
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoAddress_buttonClick(final Button.ClickEvent event) {
		final ProjectAllocation bean = this.tableProjectAllocation.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getPraId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadAddress_buttonClick(final Button.ClickEvent event) {
		this.tableProjectAllocation.refreshRowCache();
		this.tableProjectAllocation.getBeanContainerDataSource().refresh();
		this.tableProjectAllocation.sort();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteAddress_buttonClick(final Button.ClickEvent event) {
		if (this.tableProjectAllocation.getSelectedItem() == null) {
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
				final ProjectAllocation bean = ProjectTabView.this.tableProjectAllocation.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPraId(), bean.getClass().getSimpleName());

				final ProjectAllocationDAO dao = new ProjectAllocationDAO();
				dao.remove(bean);
				//ProjectTabView.this.fieldGroup.getItemDataSource().getBean().getProjectAllocations().remove(bean); //Works on Customer/Address??
				ProjectTabView.this.tableProjectAllocation.removeItem(bean);

				try {
					ProjectTabView.this.tableProjectAllocation
							.select(ProjectTabView.this.tableProjectAllocation.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					// ignore
					// CustomerTabView.this.fieldGroupActivity.setItemDataSource(new Activity());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewAddress_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = getCurrentRecord();
		if (objId < 0) {
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupProjectAllocation();

	}

	private void popupProjectAllocation() {
		final Window win = ProjectAllocationPopup.getPopupWindow(); //ProjectAllocationPopup

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadProjectAllocationList();
			}

		});
		this.getUI().addWindow(win);


	}

	private void reloadProjectAllocationList() {
		Project bean = null;
		if (this.table.getSelectedItem() != null) {
			bean = this.table.getSelectedItem().getBean();
		}

		final XdevBeanContainer<ProjectAllocation> myCustomerList = this.tableProjectAllocation.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new ProjectAllocationDAO().findByProject(bean));

		if (bean != null) {
			this.tableProjectAllocation.refreshRowCache();
			this.tableProjectAllocation.getBeanContainerDataSource().refresh();
		}

	}

	private Long getCurrentRecord() {
		if (this.fieldGroup.getItemDataSource().getBean() != null) {
			return this.fieldGroup.getItemDataSource().getBean().getProId();
		}
		return new Long(-1);
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditAddress_buttonClick(final Button.ClickEvent event) {
		if (this.tableProjectAllocation.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableProjectAllocation.getSelectedItem().getBean().getPraId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupProjectAllocation();

	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #dateProStartDate}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void dateProStartDate_valueChange(final Property.ValueChangeEvent event) {
		validateDateFromTo();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdHours}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdHours_buttonClick(final Button.ClickEvent event) {
		final Project proDao = new Project();
		proDao.setProStartDate(this.dateProStartDate.getValue());
		proDao.setProEndDate(this.dateProEndDate.getValue());
		proDao.setProIntensityPercent((Integer) this.txtProIntensityPercent.getConvertedValue());
		proDao.setProHours((Integer) this.txtProHours.getConvertedValue());

		UI.getCurrent().getSession().setAttribute("ProjectDao", proDao);

		popupProjectEffortDefault();
	}

	private void popupProjectEffortDefault() {
		final Window win = ProjectEffortDefault.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final Project proDao = (Project) UI.getCurrent().getSession().getAttribute("ProjectDao");

				ProjectTabView.this.txtProHours.setValue(proDao.getProHours().toString());
				ProjectTabView.this.dateProEndDate.setValue(proDao.getProEndDate());
				ProjectTabView.this.txtProIntensityPercent.setValue(proDao.getProIntensityPercent().toString());

			}

		});
		this.getUI().addWindow(win);

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
		this.horizontalLayoutCus = new XdevHorizontalLayout();
		this.cmbCustomer = new XdevComboBox<>();
		this.btnSearch = new XdevButton();
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
		this.lblProIntensityPercent = new XdevLabel();
		this.txtProIntensityPercent = new XdevTextField();
		this.lblProHours = new XdevLabel();
		this.txtProHours = new XdevTextField();
		this.lblProHoursEffective = new XdevLabel();
		this.txtProHoursEffective = new XdevTextField();
		this.lblProRate = new XdevLabel();
		this.txtProRate = new XdevTextField();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblBillingAddress = new XdevLabel();
		this.cmbBillingAddress = new XdevComboBox<>();
		this.lblProState = new XdevLabel();
		this.cbxState = new XdevComboBox<>();
		this.gridLayoutDesc = new XdevGridLayout();
		this.lblProModel = new XdevLabel();
		this.cbxProModel = new XdevComboBox<>();
		this.cbxInternal = new XdevCheckBox();
		this.lblProOrderStrategy = new XdevLabel();
		this.cbxProOrderStrategy = new XdevComboBox<>();
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
		this.gridLayoutRess = new XdevGridLayout();
		this.verticalLayoutRess = new XdevVerticalLayout();
		this.horizontalLayoutRess = new XdevHorizontalLayout();
		this.cmdNewAddress = new XdevButton();
		this.cmdDeleteAddress = new XdevButton();
		this.cmdEditAddress = new XdevButton();
		this.cmdReloadAddress = new XdevButton();
		this.cmdInfoAddress = new XdevButton();
		this.tableProjectAllocation = new XdevTable<>();
		this.gridLayoutRef = new XdevGridLayout();
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.verticalLayoutBill = new XdevVerticalLayout();
		this.tableOrder = new XdevTable<>();
		this.verticalLayoutSubProject = new XdevVerticalLayout();
		this.tableProject = new XdevTable<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.cmdHours = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Project.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.containerFilterComponent.setPrefixMatchOnly(false);
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdDelete.setDescription("Projekt löschen");
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdReload.setDescription("Liste neu laden");
		this.cmdPlan.setIcon(FontAwesome.AREA_CHART);
		this.cmdPlan.setDescription(StringResourceUtils.optLocalizeString("{$cmdPlan.description}", this));
		this.cmdPlan.setImmediate(true);
		this.cmdReport.setIcon(FontAwesome.PRINT);
		this.cmdReport.setDescription("Jasper Report starten");
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdInfo.setDescription("Objektstamm aufrufen");
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll(),
				NestedProperty.of("customer.shortname", String.class),
				NestedProperty.of(Project_.costAccount, CostAccount_.csaName));
		this.table.setVisibleColumns(Project_.proName.getName(), "customer.shortname", Project_.proStartDate.getName(),
				Project_.proEndDate.getName(), Project_.proExtReference.getName(),
				NestedProperty.path(Project_.costAccount, CostAccount_.csaName), Project_.costAccount.getName(),
				Project_.proRate.getName(), Project_.proProjectState.getName(), Project_.proState.getName(),
				Project_.proHoursEffective.getName(), Project_.proHours.getName());
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
		this.table.setColumnHeader("proHoursEffective", "Stunden Ist");
		this.table.setColumnAlignment("proHoursEffective", Table.Align.RIGHT);
		this.table.setConverter("proHoursEffective",
				ConverterBuilder.stringToDouble().minimumFractionDigits(1).maximumFractionDigits(1).build());
		this.table.setColumnCollapsed("proHoursEffective", true);
		this.table.setColumnHeader("proHours", "Stunden Soll");
		this.table.setColumnAlignment("proHours", Table.Align.RIGHT);
		this.table.setConverter("proHours", ConverterBuilder.stringToDouble().build());
		this.table.setColumnCollapsed("proHours", true);
		this.gridLayoutData.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.lblCustomer.setValue(StringResourceUtils.optLocalizeString("{$lblCustomer.value}", this));
		this.horizontalLayoutCus.setMargin(new MarginInfo(false));
		this.cmbCustomer.setRequired(true);
		this.cmbCustomer.setItemCaptionFromAnnotation(false);
		this.cmbCustomer.setFilteringMode(FilteringMode.CONTAINS);
		this.cmbCustomer.setEnabled(false);
		this.cmbCustomer.setContainerDataSource(Customer.class, DAOs.get(CustomerDAO.class).findAll());
		this.cmbCustomer.setItemCaptionPropertyId("fullname");
		this.btnSearch.setIcon(FontAwesome.SEARCH);
		this.btnSearch.setCaption("");
		this.btnSearch.setDescription("Suchen...");
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
		this.dateProEndDate.setRequired(true);
		this.lblProIntensityPercent
				.setValue(StringResourceUtils.optLocalizeString("{$lblProIntensityPercent.value}", this));
		this.lblProHours.setValue("Stunden Soll");
		this.txtProHours
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.txtProHours.setRequired(true);
		this.txtProHours.addValidator(new IntegerRangeValidator("Der Wert muss grösser 0 sein!", 1, null));
		this.lblProHoursEffective.setValue("Ist");
		this.txtProHoursEffective
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.lblProRate.setValue(StringResourceUtils.optLocalizeString("{$lblProRate.value}", this));
		this.txtProRate
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.txtProRate.setRequired(true);
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setRequired(true);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAllActive());
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setItemCaptionFromAnnotation(false);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAllActive());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaName.getName());
		this.lblBillingAddress.setValue("R-Adresse");
		this.cmbBillingAddress.setItemCaptionFromAnnotation(false);
		this.cmbBillingAddress.setContainerDataSource(Address.class, false);
		this.cmbBillingAddress.setItemCaptionPropertyId("shortname");
		this.lblProState.setValue(StringResourceUtils.optLocalizeString("{$lblProState.value}", this));
		this.lblProModel.setValue(StringResourceUtils.optLocalizeString("{$lblProModel.value}", this));
		this.cbxInternal.setCaption("Internes Projekt");
		this.lblProOrderStrategy.setValue("Rechnungsstrategie");
		this.cbxProOrderStrategy.setRequired(true);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAllActive());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblProDescription.setValue(StringResourceUtils.optLocalizeString("{$lblProDescription.value}", this));
		this.textArea.setRows(5);
		this.lblProRemark.setValue(StringResourceUtils.optLocalizeString("{$lblProRemark.value}", this));
		this.textAreaRem.setRows(2);
		this.lblProProjectState.setValue(StringResourceUtils.optLocalizeString("{$lblProProjectState.value}", this));
		this.lblProLastBill.setValue(StringResourceUtils.optLocalizeString("{$lblProLastBill.value}", this));
		this.gridLayoutRess.setMargin(new MarginInfo(false));
		this.verticalLayoutRess.setMargin(new MarginInfo(false));
		this.horizontalLayoutRess.setSpacing(false);
		this.horizontalLayoutRess.setMargin(new MarginInfo(false));
		this.cmdNewAddress.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewAddress.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDeleteAddress.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdEditAddress.setIcon(FontAwesome.PENCIL);
		this.cmdReloadAddress.setIcon(FontAwesome.REFRESH);
		this.cmdInfoAddress.setIcon(FontAwesome.INFO_CIRCLE);
		this.tableProjectAllocation.setColumnReorderingAllowed(true);
		this.tableProjectAllocation.setCaption("Projekt Ressourcen");
		this.tableProjectAllocation.setColumnCollapsingAllowed(true);
		this.tableProjectAllocation.setContainerDataSource(ProjectAllocation.class, false);
		this.tableProjectAllocation.setVisibleColumns(ProjectAllocation_.costAccount.getName(),
				ProjectAllocation_.praStartDate.getName(), ProjectAllocation_.praEndDate.getName(),
				ProjectAllocation_.praHours.getName(), ProjectAllocation_.praIntensityPercent.getName(),
				ProjectAllocation_.praRate.getName(), ProjectAllocation_.praRemark.getName(),
				ProjectAllocation_.praState.getName());
		this.tableProjectAllocation.setConverter("praStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableProjectAllocation.setConverter("praEndDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableProjectAllocation.setColumnAlignment("praHours", Table.Align.RIGHT);
		this.tableProjectAllocation.setConverter("praHours", ConverterBuilder.stringToDouble().build());
		this.tableProjectAllocation.setColumnAlignment("praIntensityPercent", Table.Align.RIGHT);
		this.tableProjectAllocation.setConverter("praIntensityPercent", ConverterBuilder.stringToDouble().build());
		this.tableProjectAllocation.setColumnAlignment("praRate", Table.Align.RIGHT);
		this.tableProjectAllocation.setConverter("praRate", ConverterBuilder.stringToDouble().currency().build());
		this.tableProjectAllocation.setColumnCollapsed("praState", true);
		this.gridLayoutRef.setMargin(new MarginInfo(false));
		this.verticalSplitPanel.setStyleName("large");
		this.verticalSplitPanel.setSplitPosition(60.0F, Unit.PERCENTAGE);
		this.verticalLayoutBill.setMargin(new MarginInfo(false));
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
		this.verticalLayoutSubProject.setMargin(new MarginInfo(false));
		this.tableProject.setCaption("Sub-Projekte");
		this.tableProject.setIcon(FontAwesome.CUBES);
		this.tableProject.setContainerDataSource(Project.class, false);
		this.tableProject.setVisibleColumns(Project_.proName.getName(), Project_.proHours.getName(),
				Project_.proHoursEffective.getName());
		this.tableProject.setColumnHeader("proName", "Name");
		this.tableProject.setColumnHeader("proHours", "Soll-Stunden");
		this.tableProject.setColumnHeader("proHoursEffective", "Ist-Stunden");
		this.horizontalLayout.setMargin(new MarginInfo(false, false, true, true));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdHours.setIcon(FontAwesome.CLOCK_O);
		this.cmdHours.setCaption("Stunden...");
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
		this.fieldGroup.bind(this.cbxProOrderStrategy, Project_.proOrdergenerationStrategy.getName());

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "proName", "costAccount",
				"customer", "proStartDate", "proEndDate", "vat", "proProjectState", "proModel", "proState");
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
		this.cmbCustomer.setWidth(100, Unit.PERCENTAGE);
		this.cmbCustomer.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutCus.addComponent(this.cmbCustomer);
		this.horizontalLayoutCus.setExpandRatio(this.cmbCustomer, 90.0F);
		this.btnSearch.setSizeUndefined();
		this.horizontalLayoutCus.addComponent(this.btnSearch);
		this.horizontalLayoutCus.setExpandRatio(this.btnSearch, 10.0F);
		this.gridLayout.setColumns(4);
		this.gridLayout.setRows(11);
		this.lblCustomer.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCustomer, 0, 0);
		this.horizontalLayoutCus.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutCus.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.horizontalLayoutCus, 1, 0, 3, 0);
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
		this.lblProIntensityPercent.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProIntensityPercent, 0, 4);
		this.txtProIntensityPercent.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProIntensityPercent, 1, 4);
		this.lblProHours.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProHours, 0, 5);
		this.txtProHours.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProHours, 1, 5);
		this.lblProHoursEffective.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProHoursEffective, 2, 5);
		this.txtProHoursEffective.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProHoursEffective, 3, 5);
		this.lblProRate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblProRate, 0, 6);
		this.txtProRate.setSizeUndefined();
		this.gridLayout.addComponent(this.txtProRate, 1, 6);
		this.lblVat.setSizeUndefined();
		this.gridLayout.addComponent(this.lblVat, 2, 6);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.cmbVat, 3, 6);
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
		this.gridLayout.setColumnExpandRatio(3, 100.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 10, 3, 10);
		this.gridLayout.setRowExpandRatio(10, 1.0F);
		this.gridLayoutDesc.setColumns(3);
		this.gridLayoutDesc.setRows(8);
		this.lblProModel.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProModel, 0, 0);
		this.cbxProModel.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.cbxProModel, 1, 0);
		this.cbxInternal.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.cbxInternal, 2, 0);
		this.lblProOrderStrategy.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProOrderStrategy, 0, 1);
		this.cbxProOrderStrategy.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.cbxProOrderStrategy, 1, 1);
		this.lblProject.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProject, 0, 2);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.gridLayoutDesc.addComponent(this.cmbProject, 1, 2, 2, 2);
		this.lblProDescription.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProDescription, 0, 3);
		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.gridLayoutDesc.addComponent(this.textArea, 1, 3, 2, 3);
		this.lblProRemark.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProRemark, 0, 4);
		this.textAreaRem.setWidth(100, Unit.PERCENTAGE);
		this.textAreaRem.setHeight(-1, Unit.PIXELS);
		this.gridLayoutDesc.addComponent(this.textAreaRem, 1, 4, 2, 4);
		this.lblProProjectState.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProProjectState, 0, 5);
		this.cbxProState.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.cbxProState, 1, 5);
		this.lblProLastBill.setSizeUndefined();
		this.gridLayoutDesc.addComponent(this.lblProLastBill, 0, 6);
		this.dateProLastBill.setWidth(100, Unit.PERCENTAGE);
		this.dateProLastBill.setHeight(-1, Unit.PIXELS);
		this.gridLayoutDesc.addComponent(this.dateProLastBill, 1, 6);
		this.gridLayoutDesc.setColumnExpandRatio(1, 100.0F);
		this.gridLayoutDesc.setColumnExpandRatio(2, 100.0F);
		final CustomComponent gridLayoutDesc_vSpacer = new CustomComponent();
		gridLayoutDesc_vSpacer.setSizeFull();
		this.gridLayoutDesc.addComponent(gridLayoutDesc_vSpacer, 0, 7, 2, 7);
		this.gridLayoutDesc.setRowExpandRatio(7, 1.0F);
		this.cmdNewAddress.setSizeUndefined();
		this.horizontalLayoutRess.addComponent(this.cmdNewAddress);
		this.horizontalLayoutRess.setComponentAlignment(this.cmdNewAddress, Alignment.MIDDLE_CENTER);
		this.cmdDeleteAddress.setSizeUndefined();
		this.horizontalLayoutRess.addComponent(this.cmdDeleteAddress);
		this.horizontalLayoutRess.setComponentAlignment(this.cmdDeleteAddress, Alignment.MIDDLE_CENTER);
		this.cmdEditAddress.setSizeUndefined();
		this.horizontalLayoutRess.addComponent(this.cmdEditAddress);
		this.horizontalLayoutRess.setComponentAlignment(this.cmdEditAddress, Alignment.MIDDLE_CENTER);
		this.cmdReloadAddress.setSizeUndefined();
		this.horizontalLayoutRess.addComponent(this.cmdReloadAddress);
		this.horizontalLayoutRess.setComponentAlignment(this.cmdReloadAddress, Alignment.MIDDLE_CENTER);
		this.cmdInfoAddress.setSizeUndefined();
		this.horizontalLayoutRess.addComponent(this.cmdInfoAddress);
		this.horizontalLayoutRess.setComponentAlignment(this.cmdInfoAddress, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayoutRess_spacer = new CustomComponent();
		horizontalLayoutRess_spacer.setSizeFull();
		this.horizontalLayoutRess.addComponent(horizontalLayoutRess_spacer);
		this.horizontalLayoutRess.setExpandRatio(horizontalLayoutRess_spacer, 1.0F);
		this.horizontalLayoutRess.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutRess.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutRess.addComponent(this.horizontalLayoutRess);
		this.verticalLayoutRess.setComponentAlignment(this.horizontalLayoutRess, Alignment.MIDDLE_CENTER);
		this.tableProjectAllocation.setWidth(100, Unit.PERCENTAGE);
		this.tableProjectAllocation.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutRess.addComponent(this.tableProjectAllocation);
		final CustomComponent verticalLayoutRess_spacer = new CustomComponent();
		verticalLayoutRess_spacer.setSizeFull();
		this.verticalLayoutRess.addComponent(verticalLayoutRess_spacer);
		this.verticalLayoutRess.setExpandRatio(verticalLayoutRess_spacer, 1.0F);
		this.gridLayoutRess.setColumns(1);
		this.gridLayoutRess.setRows(1);
		this.verticalLayoutRess.setSizeFull();
		this.gridLayoutRess.addComponent(this.verticalLayoutRess, 0, 0);
		this.gridLayoutRess.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutRess.setRowExpandRatio(0, 10.0F);
		this.tableOrder.setSizeFull();
		this.verticalLayoutBill.addComponent(this.tableOrder);
		this.verticalLayoutBill.setComponentAlignment(this.tableOrder, Alignment.MIDDLE_CENTER);
		this.verticalLayoutBill.setExpandRatio(this.tableOrder, 10.0F);
		this.tableProject.setSizeFull();
		this.verticalLayoutSubProject.addComponent(this.tableProject);
		this.verticalLayoutSubProject.setExpandRatio(this.tableProject, 10.0F);
		this.verticalLayoutBill.setSizeFull();
		this.verticalSplitPanel.setFirstComponent(this.verticalLayoutBill);
		this.verticalLayoutSubProject.setSizeFull();
		this.verticalSplitPanel.setSecondComponent(this.verticalLayoutSubProject);
		this.gridLayoutRef.setColumns(1);
		this.gridLayoutRef.setRows(1);
		this.verticalSplitPanel.setSizeFull();
		this.gridLayoutRef.addComponent(this.verticalSplitPanel, 0, 0);
		this.gridLayoutRef.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutRef.setRowExpandRatio(0, 10.0F);
		this.gridLayout.setSizeFull();
		this.tabSheet.addTab(this.gridLayout, StringResourceUtils.optLocalizeString("{$gridLayout.caption}", this), null);
		this.gridLayoutDesc.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutDesc, StringResourceUtils.optLocalizeString("{$gridLayout2.caption}", this),
				null);
		this.gridLayoutRess.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutRess, "Ressourcen", null);
		this.gridLayoutRef.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutRef, "Referenzen", null);
		this.tabSheet.setSelectedTab(this.gridLayout);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_CENTER);
		this.cmdHours.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdHours);
		this.horizontalLayout.setComponentAlignment(this.cmdHours, Alignment.MIDDLE_CENTER);
		this.gridLayoutData.setColumns(1);
		this.gridLayoutData.setRows(2);
		this.tabSheet.setSizeFull();
		this.gridLayoutData.addComponent(this.tabSheet, 0, 0);
		this.horizontalLayout.setSizeUndefined();
		this.gridLayoutData.addComponent(this.horizontalLayout, 0, 1);
		this.gridLayoutData.setComponentAlignment(this.horizontalLayout, Alignment.BOTTOM_CENTER);
		this.gridLayoutData.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutData.setRowExpandRatio(0, 100.0F);
		this.gridLayoutData.setRowExpandRatio(1, 10.0F);
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
		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.cmbCustomer.addValueChangeListener(event -> this.cmbCustomer_valueChange(event));
		this.btnSearch.addClickListener(event -> this.btnSearch_buttonClick(event));
		this.dateProStartDate.addValueChangeListener(event -> this.dateProStartDate_valueChange(event));
		this.dateProEndDate.addValueChangeListener(event -> this.dateProEndDate_valueChange(event));
		this.cmdNewAddress.addClickListener(event -> this.cmdNewAddress_buttonClick(event));
		this.cmdDeleteAddress.addClickListener(event -> this.cmdDeleteAddress_buttonClick(event));
		this.cmdEditAddress.addClickListener(event -> this.cmdEditAddress_buttonClick(event));
		this.cmdReloadAddress.addClickListener(event -> this.cmdReloadAddress_buttonClick(event));
		this.cmdInfoAddress.addClickListener(event -> this.cmdInfoAddress_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
		this.cmdHours.addClickListener(event -> this.cmdHours_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdPlan, cmdReport, cmdInfo, btnSearch, cmdNewAddress,
			cmdDeleteAddress, cmdEditAddress, cmdReloadAddress, cmdInfoAddress, cmdSave, cmdReset, cmdHours;
	private XdevLabel lblCustomer, lblProName, lblProExtReference, lblProContact, lblProStartDate, lblProEndDate,
			lblProIntensityPercent, lblProHours, lblProHoursEffective, lblProRate, lblVat, lblCostAccount,
			lblBillingAddress, lblProState, lblProModel, lblProOrderStrategy, lblProject, lblProDescription, lblProRemark,
			lblProProjectState, lblProLastBill;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevFieldGroup<Project> fieldGroup;
	private XdevComboBox<Address> cmbBillingAddress;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayoutData, gridLayout, gridLayoutDesc, gridLayoutRess, gridLayoutRef;
	private XdevComboBox<Project> cmbProject;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevTable<ProjectAllocation> tableProjectAllocation;
	private XdevHorizontalLayout actionLayout, horizontalLayoutCus, horizontalLayoutRess, horizontalLayout;
	private XdevComboBox<Vat> cmbVat;
	private XdevVerticalSplitPanel verticalSplitPanel;
	private XdevPopupDateField dateProStartDate, dateProEndDate, dateProLastBill;
	private XdevTextArea textArea, textAreaRem;
	private XdevTable<Project> table, tableProject;
	private XdevComboBox<?> cbxState, cbxProModel, cbxProOrderStrategy, cbxProState;
	private XdevComboBox<Customer> cmbCustomer;
	private XdevCheckBox cbxInternal;
	private XdevTable<Order> tableOrder;
	private XdevTextField txtProName, txtProExtReference, txtProContact, txtProIntensityPercent, txtProHours,
			txtProHoursEffective, txtProRate;
	private XdevVerticalLayout verticalLayout, verticalLayoutRess, verticalLayoutBill, verticalLayoutSubProject;
	// </generated-code>

}
