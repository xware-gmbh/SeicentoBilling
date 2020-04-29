package ch.xwr.seicentobilling.ui.desktop;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.poi.ss.formula.functions.T;
import org.jfree.util.Log;

import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ExternalResource;
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
import com.xdev.ui.XdevLink;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevVerticalSplitPanel;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.listselect.XdevTwinColSelect;
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
import ch.xwr.seicentobilling.business.LovState.AccountType;
import ch.xwr.seicentobilling.business.NumberRangeHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.ActivityDAO;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.dal.ContactRelationDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.Activity_;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.Address_;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.City_;
import ch.xwr.seicentobilling.entities.ContactRelation;
import ch.xwr.seicentobilling.entities.ContactRelation_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.CustomerLink_;
import ch.xwr.seicentobilling.entities.Customer_;
import ch.xwr.seicentobilling.entities.LabelDefinition;
import ch.xwr.seicentobilling.entities.LabelDefinition_;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Order_;
import ch.xwr.seicentobilling.entities.PaymentCondition;
import ch.xwr.seicentobilling.entities.PaymentCondition_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.ui.desktop.crm.ActivityPopup;
import ch.xwr.seicentobilling.ui.desktop.crm.AddressPopup;
import ch.xwr.seicentobilling.ui.desktop.crm.ContactRelationPopup;
import ch.xwr.seicentobilling.ui.desktop.crm.CustomerLinkPopup;
import ch.xwr.seicentobilling.ui.desktop.crm.FunctionAddressHyperlink;
import ch.xwr.seicentobilling.ui.desktop.crm.FunctionLinkHyperlink;
import ch.xwr.seicentobilling.ui.desktop.crm.ImportContactsPopup;
import ch.xwr.seicentobilling.ui.desktop.crm.VcardPopup;

public class CustomerTabView extends XdevView {

	/**
	 *
	 */
	public CustomerTabView() {
		super();
		this.initUI();

		// Type
		this.cbxState.addItems((Object[]) LovState.State.values());
		this.cbxAccountType.addItems((Object[]) LovState.AccountType.values());
		this.cbxAccountSalutation.addItems((Object[]) LovCrm.Salutation.values());
		this.cbxAccountBillingReports.addItems((Object[]) LovCrm.BillReport.values());
		this.cbxAccountBillingType.addItems((Object[]) LovCrm.BillTarget.values());

		// set RO Fields
		setROFields();
		setDefaultFilter();

		if (Seicento.hasRole("BillingAdmin")) {
			this.cmdImport.setEnabled(true);
			this.cmdImport.setVisible(true);

			this.txtExtRef1.setEnabled(true);
			this.txtExtRef2.setEnabled(true);
		}
	}

	private void setROFields() {
		this.txtCusNumber.setEnabled(false);

		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null || this.fieldGroup.getItemDataSource().getBean() == null ) {
			hasData = false;
		}

		setROComponents(hasData);
	}

	private void setROComponents(final boolean state) {
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		this.cmdVcard.setEnabled(state);
		this.tabSheet.setEnabled(state);

		if (Seicento.hasRole("BillingAdmin") && state) {
			this.txtExtRef1.setEnabled(true);
			this.txtExtRef2.setEnabled(true);
		} else {
			this.txtExtRef1.setEnabled(false);
			this.txtExtRef2.setEnabled(false);
		}

	}

	private boolean isNew() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return true;
		}
		final Customer bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean.getCusId() == null || bean.getCusId().longValue() < 1) {
			return true;
		}
		return false;
	}

	private void setDefaultFilter() {
		final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final FilterData[] fd = new FilterData[] { new FilterData("cusState", new FilterOperator.Is(), valState) };

		this.containerFilterComponent.setFilterData(fd);

	}

	private void checkCustomerNumber(final boolean isNew, final boolean commitNbr) {
		if (!isNew) {
			return;
		}

		Integer nbr = null;
		try {
			nbr = Integer.parseInt(this.txtCusNumber.getValue());
		} catch (final Exception e) {
			nbr = new Integer(0);
		}

		final NumberRangeHandler handler = new NumberRangeHandler();
		if (!commitNbr) {
			if (nbr > 0) {
				return;
			}
			this.txtCusNumber.setValue(handler.getNewCustomerNumber(false, nbr).toString());
		} else {
			handler.getNewCustomerNumber(true, nbr);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		if (isNew()) {
			Notification.show("Info", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);

			return;
		}
		final Customer bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCusId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		// save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);
		final int idx = this.table.getCurrentPageFirstItemIndex();

		// clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new CustomerDAO().findAllByNumberDesc());

		// reassign filter
		this.containerFilterComponent.setFilterData(fd);

		if (this.fieldGroup.getItemDataSource() != null) {
			final Customer bean = this.fieldGroup.getItemDataSource().getBean();
			if (bean != null) {
				this.table.select(bean);

				if (idx > 0) {
					this.table.setCurrentPageFirstItemIndex(idx);
				}
				//this.table.setCurrentPageFirstItemId(bean);
			}
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		this.table.select(null);

		final PaymentConditionDAO dao = new PaymentConditionDAO();
		final Customer bean = new Customer();
		bean.setCusState(LovState.State.active);
		bean.setPaymentCondition(dao.find((long) 1));
		bean.setCusBillingTarget(LovCrm.BillTarget.pdf);
		bean.setCusBillingReport(LovCrm.BillReport.working);
		bean.setCusAccountType(AccountType.juristisch);

		this.fieldGroup.setItemDataSource(bean);
		checkCustomerNumber(true, false);
		setROFields();

		this.txtZip.setValue("");

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
					final Customer bean = CustomerTabView.this.table.getSelectedItem().getBean();

					final CustomerDAO dao = new CustomerDAO();
					dao.remove(bean);
					dao.flush();

					// Delete Record
					final RowObjectManager man = new RowObjectManager();
					man.deleteObject(bean.getCusId(), bean.getClass().getSimpleName());

					//CustomerTabView.this.table.getBeanContainerDataSource().refresh();
					CustomerTabView.this.table.removeItem(bean);
					CustomerTabView.this.table.select(null);

					CustomerTabView.this.fieldGroup.clear();
					setROComponents(false);

//					try {
//						CustomerTabView.this.table.select(CustomerTabView.this.table.getCurrentPageFirstItemId());
//					} catch (final Exception e) {
//						// ignore
//						CustomerTabView.this.fieldGroup.setItemDataSource(new Customer());
//					}
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
		final Customer bean = this.table.getSelectedItem().getBean();

		final JasperManager jsp = new JasperManager();
		//jsp.addParameter("Customer_Number", "" + bean.getCusNumber());
		jsp.addParameter("CustomerId", "" + bean.getCusId());

		Page.getCurrent().open(jsp.getUri(JasperManager.ContactDetails1), "_blank");
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		if (this.table.getSelectedItem() != null) {
			final Customer bean = this.table.getSelectedItem().getBean();
			setGoogleLink(bean);

			displayChildTables();
			setROFields();
		}
	}


	private void displayChildTables() {
		final Customer bean = this.fieldGroup.getItemDataSource().getBean();

		// Rechnungen
		this.tableOrder.clear();
		this.tableOrder.removeAllItems();
		this.tableOrder.addItems(new OrderDAO().findByCustomer(bean));

		// Projekte
		this.tableProject.clear();
		this.tableProject.removeAllItems();
		this.tableProject.addItems(bean.getProjects());

		// Aktivitäten
		this.tableActivity.clear();
		this.tableActivity.removeAllItems();
		this.tableActivity.addItems(new ActivityDAO().findByCustomer(bean));

		// Adresen
		this.tableAddress.clear();
		this.tableAddress.removeAllItems();
		this.tableAddress.addItems(bean.getAddresses());

		// CustomerLinks (Email + Phone)
		this.tableLink.clear();
		this.tableLink.removeAllItems();
		this.tableLink.addItems(bean.getCustomerLinks());

		// Relations
		this.tableRelation.clear();
		// this.tableRelation.removeAllItems();

		reloadRelationList();
//		this.tableRelation.addItems(bean.getContactRelations1());
//		this.tableRelation.addItems(bean.getContactRelations2());
	}

	private void setGoogleLink(final Customer bean) {
		final String uripre = "https://www.google.com/maps/search/?api=1&query=";
		String q = bean.getCity().getCtyName();
		if (bean.getCusAddress() != null && !bean.getCusAddress().trim().isEmpty()) {
			q = q + ", " + bean.getCusAddress();
		}
		if (bean.getCusCompany() != null && !bean.getCusCompany().trim().isEmpty()) {
			q = q + ", " + bean.getCusCompany().trim();
		}

		try {
			q = URLEncoder.encode(q, "UTF-8");
			this.linkMaps.setResource(new ExternalResource(new URL(uripre + q)));
		} catch (final MalformedURLException e) {
			Log.error(e);
		} catch (final UnsupportedEncodingException e) {
			Log.error(e);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (!AreFieldsValid()) {
			return;
		}

		final boolean isNew = isNew(); // assign before save. is always false after save
		if (SeicentoCrud.doSave(this.fieldGroup)) {
			try {
				this.setROFields();
				checkCustomerNumber(isNew, true);

				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.fieldGroup.getItemDataSource().getBean().getCusId(),
						this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			} catch (final Exception e) {
				Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
				e.printStackTrace();
			}

		}



		cmdReload_buttonClick(event);
		//this.table.sanitizeSelection();
	}

	@SuppressWarnings("unchecked")
	private boolean AreFieldsValid() {
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

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewActivity_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = getCurrentRecord();
		if (objId < 0) {
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupActivity();
	}

	private void popupActivity() {
		final Window win = ActivityPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadActivityList();
			}
		});
		this.getUI().addWindow(win);

	}

	private void reloadActivityList() {
		Customer bean = null;
		if (this.table.getSelectedItem() != null) {
			bean = this.table.getSelectedItem().getBean();
		}

		final XdevBeanContainer<Activity> myCustomerList = this.tableActivity.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new ActivityDAO().findByCustomer(bean));

		if (bean != null) {
			this.tableActivity.refreshRowCache();
			this.tableActivity.getBeanContainerDataSource().refresh();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditActivity_buttonClick(final Button.ClickEvent event) {
		if (this.tableActivity.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableActivity.getSelectedItem().getBean().getactId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupActivity();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteActivity_buttonClick(final Button.ClickEvent event) {
		if (this.tableActivity.getSelectedItem() == null) {
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
				final Activity bean = CustomerTabView.this.tableActivity.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getactId(), bean.getClass().getSimpleName());

				final ActivityDAO dao = new ActivityDAO();
				dao.remove(bean);
				CustomerTabView.this.tableActivity.getBeanContainerDataSource().refresh();

				try {
					CustomerTabView.this.tableActivity
							.select(CustomerTabView.this.tableActivity.getCurrentPageFirstItemId());
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
	 * {@link #cmdReloadActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadActivity_buttonClick(final Button.ClickEvent event) {
		this.tableActivity.refreshRowCache();
		this.tableActivity.getBeanContainerDataSource().refresh();
		// this.table.sort();

//		final Customer bean = this.fieldGroup.getItemDataSource().getBean();
//
//		this.tableActivity.clear();
//		this.tableActivity.removeAllItems();
//		this.tableActivity.addItems(bean.getActivities());
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoActivity_buttonClick(final Button.ClickEvent event) {
		final Activity bean = this.tableActivity.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getactId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	private void popupAddress() {
		final Window win = AddressPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadAddressList();
			}

		});
		this.getUI().addWindow(win);

	}

	private void reloadAddressList() {
		Customer bean = null;
		if (this.table.getSelectedItem() != null) {
			bean = this.table.getSelectedItem().getBean();
		}

		final XdevBeanContainer<Address> myCustomerList = this.tableAddress.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new AddressDAO().findByCustomer(bean));

		if (bean != null) {
			this.tableAddress.refreshRowCache();
			this.tableAddress.getBeanContainerDataSource().refresh();
		}

	}

	private void popupCustomerLink() {
		final Window win = CustomerLinkPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadCustomerLinkList();
			}

		});
		this.getUI().addWindow(win);

	}

	private void reloadCustomerLinkList() {
		Customer bean = null;
		if (this.table.getSelectedItem() != null) {
			bean = this.table.getSelectedItem().getBean();
		}

		final XdevBeanContainer<CustomerLink> myCustomerList = this.tableLink.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new CustomerLinkDAO().findByCustomer(bean));

		if (bean != null) {
			this.tableLink.refreshRowCache();
			this.tableLink.getBeanContainerDataSource().refresh();
		}

	}

	private void reloadRelationList() {
		Customer bean = null;
		if (this.table.getSelectedItem() != null) {
			bean = this.table.getSelectedItem().getBean();
		}

		final XdevBeanContainer<ContactRelation> myCustomerList = this.tableRelation.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new ContactRelationDAO().findByCustomer(bean));

		if (bean != null) {
			this.tableRelation.refreshRowCache();
			this.tableRelation.getBeanContainerDataSource().refresh();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableActivity}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableActivity_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final Activity obj = (Activity) event.getItemId();
			this.tableActivity.select(obj); // reselect after double-click

			final Long beanId = obj.getactId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupActivity();
		}

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

		popupAddress();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewCustomerLink_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = getCurrentRecord();
		if (objId < 0) {
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupCustomerLink();
	}

	private Long getCurrentRecord() {
		if (this.fieldGroup.getItemDataSource().getBean() != null) {
			return this.fieldGroup.getItemDataSource().getBean().getCusId();
		}
		return new Long(-1);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteAddress_buttonClick(final Button.ClickEvent event) {
		if (this.tableAddress.getSelectedItem() == null) {
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
				final Address bean = CustomerTabView.this.tableAddress.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getAdrId(), bean.getClass().getSimpleName());

				final AddressDAO dao = new AddressDAO();
				dao.remove(bean);
				CustomerTabView.this.tableAddress.getBeanContainerDataSource().refresh();

				try {
					CustomerTabView.this.tableAddress
							.select(CustomerTabView.this.tableAddress.getCurrentPageFirstItemId());
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
	 * {@link #cmdDeleteCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteCustomerLink_buttonClick(final Button.ClickEvent event) {
		if (this.tableLink.getSelectedItem() == null) {
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
				final CustomerLink bean = CustomerTabView.this.tableLink.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCnkId(), bean.getClass().getSimpleName());

				final CustomerLinkDAO dao = new CustomerLinkDAO();
				dao.remove(bean);
				CustomerTabView.this.tableLink.getBeanContainerDataSource().refresh();

				try {
					CustomerTabView.this.tableLink.select(CustomerTabView.this.tableLink.getCurrentPageFirstItemId());
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
	 * {@link #cmdEditAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditAddress_buttonClick(final Button.ClickEvent event) {
		if (this.tableAddress.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableAddress.getSelectedItem().getBean().getAdrId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupAddress();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditCustomerLink_buttonClick(final Button.ClickEvent event) {
		if (this.tableLink.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableLink.getSelectedItem().getBean().getCnkId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupCustomerLink();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadAddress_buttonClick(final Button.ClickEvent event) {
		this.tableAddress.refreshRowCache();
		this.tableAddress.getBeanContainerDataSource().refresh();
		this.tableAddress.sort();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadCustomerLink_buttonClick(final Button.ClickEvent event) {
		this.tableLink.refreshRowCache();
		this.tableLink.getBeanContainerDataSource().refresh();
		this.tableLink.sort();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoCustomerLink_buttonClick(final Button.ClickEvent event) {
		final CustomerLink bean = this.tableLink.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCnkId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoAddress_buttonClick(final Button.ClickEvent event) {
		final Address bean = this.tableAddress.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getAdrId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableAddress}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableAddress_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final Address obj = (Address) event.getItemId();
			this.tableAddress.select(obj); // reselect after double-click

			final Long beanId = obj.getAdrId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupAddress();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #tableLink}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLink_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final CustomerLink obj = (CustomerLink) event.getItemId();
			this.tableLink.select(obj); // reselect after double-click

			final Long beanId = obj.getCnkId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupCustomerLink();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableAddress}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableAddress_valueChange(final Property.ValueChangeEvent event) {

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteRelation}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteRelation_buttonClick(final Button.ClickEvent event) {
		if (this.tableRelation.getSelectedItem() == null) {
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
				final ContactRelation bean = CustomerTabView.this.tableRelation.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCorId(), bean.getClass().getSimpleName());

				final ContactRelationDAO dao = new ContactRelationDAO();
				dao.remove(bean);

				reloadRelationList();
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewRelation}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewRelation_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		final Long objId = getCurrentRecord();
		if (objId < 0) {
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupContactRelation();

	}

	private void popupContactRelation() {
		final Window win = ContactRelationPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadRelationList();
			}

		});
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdImport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdImport_buttonClick(final Button.ClickEvent event) {
		final Window win = ImportContactsPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadCustomerLinkList();
			}

		});
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevTextField} {@link #txtZip}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtZip_valueChange(final Property.ValueChangeEvent event) {
		//System.out.println("value change");
		final String val  = (String) event.getProperty().getValue();
		if (val != null && val.length() > 3) {
			City bean = null;
			if (this.cmbCity.getSelectedItem() != null) {
				bean = this.cmbCity.getSelectedItem().getBean();
			}
			if (bean == null || bean.getCtyZip().intValue() != Integer.parseInt(val)) {
				final CityDAO dao = new CityDAO();
				final List<City> ls = dao.findByZip(Integer.parseInt(val));
				if (ls != null && ls.size() > 0) {
					final City b2 = ls.get(0);

					final Collection<?> xx = this.cmbCity.getBeanContainerDataSource().getItemIds();
					for (final Iterator<?> iterator = xx.iterator(); iterator.hasNext();) {
						final City object = (City) iterator.next();
						if (object.getCtyId().equals(b2.getCtyId())) {
							this.cmbCity.select(object);
						}
					}
				} else {
					this.cmbCity.clear();
				}
			}

		}
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox} {@link #cmbCity}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCity_valueChange(final Property.ValueChangeEvent event) {
		final City cty = (City) event.getProperty().getValue();
		if (cty != null) {
			final String zip = this.txtZip.getValue();
			if (zip == null || !zip.equals(cty.getCtyZip().toString())) {
				this.txtZip.setValue("" + cty.getCtyZip());
			}
			this.lblCountry.setValue(cty.getCtyCountry());
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdVcard}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdVcard_buttonClick(final Button.ClickEvent event) {
		if (this.fieldGroup.getItemDataSource().getBean() == null) {
			return;
		}

		UI.getCurrent().getSession().setAttribute("cusbeanId",  this.fieldGroup.getItemDataSource().getBean().getCusId());
		popupVcard();
	}


	private void popupVcard() {
		final Window win = VcardPopup.getPopupWindow();
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
		this.cmdReport = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.cmdImport = new XdevButton();
		this.table = new XdevTable<>();
		this.form = new XdevGridLayout();
		this.tabSheet = new XdevTabSheet();
		this.gridLayoutContact = new XdevGridLayout();
		this.lblCusNumber = new XdevLabel();
		this.txtCusNumber = new XdevTextField();
		this.lblType = new XdevLabel();
		this.cbxAccountType = new XdevComboBox<>();
		this.lblAnrede = new XdevLabel();
		this.cbxAccountSalutation = new XdevComboBox<>();
		this.lblCusCompany = new XdevLabel();
		this.txtCusCompany = new XdevTextField();
		this.lblCusName = new XdevLabel();
		this.txtCusName = new XdevTextField();
		this.lblCusFirstName = new XdevLabel();
		this.txtCusFirstName = new XdevTextField();
		this.linkMaps = new XdevLink();
		this.txtCusAddress = new XdevTextField();
		this.lblCity = new XdevLabel();
		this.horizontalLayoutCity = new XdevHorizontalLayout();
		this.txtZip = new XdevTextField();
		this.cmbCity = new XdevComboBox<>();
		this.lblCountry = new XdevLabel();
		this.lblBirthdate = new XdevLabel();
		this.datCusBirthdate = new XdevPopupDateField();
		this.lblCusState = new XdevLabel();
		this.cbxState = new XdevComboBox<>();
		this.gridLayoutFlags = new XdevGridLayout();
		this.lblAccountManager = new XdevLabel();
		this.txtAccountManager = new XdevTextField();
		this.lblCusInfo = new XdevLabel();
		this.txtCusInfo = new XdevTextField();
		this.lblPaymentCondition = new XdevLabel();
		this.cmbPaymentCondition = new XdevComboBox<>();
		this.lblBillingTarget = new XdevLabel();
		this.cbxAccountBillingType = new XdevComboBox<>();
		this.cbxSinglePdf = new XdevCheckBox();
		this.lblBillingReports = new XdevLabel();
		this.cbxAccountBillingReports = new XdevComboBox<>();
		this.lblLabels = new XdevLabel();
		this.twinColSelect = new XdevTwinColSelect<>();
		this.lblExtRef1 = new XdevLabel();
		this.txtExtRef1 = new XdevTextField();
		this.lblExtRef2 = new XdevLabel();
		this.txtExtRef2 = new XdevTextField();
		this.gridLayoutAddress = new XdevGridLayout();
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.verticalLayoutAdr = new XdevVerticalLayout();
		this.horizontalLayoutAddress = new XdevHorizontalLayout();
		this.cmdNewAddress = new XdevButton();
		this.cmdDeleteAddress = new XdevButton();
		this.cmdEditAddress = new XdevButton();
		this.cmdReloadAddress = new XdevButton();
		this.cmdInfoAddress = new XdevButton();
		this.tableAddress = new XdevTable<>();
		this.verticalLayoutLink = new XdevVerticalLayout();
		this.horizontalLayoutLink = new XdevHorizontalLayout();
		this.cmdNewCustomerLink = new XdevButton();
		this.cmdDeleteCustomerLink = new XdevButton();
		this.cmdEditCustomerLink = new XdevButton();
		this.cmdReloadCustomerLink = new XdevButton();
		this.cmdInfoCustomerLink = new XdevButton();
		this.tableLink = new XdevTable<>();
		this.gridLayoutListActivity = new XdevGridLayout();
		this.verticalLayout3 = new XdevVerticalLayout();
		this.horizontalLayout3 = new XdevHorizontalLayout();
		this.cmdNewActivity = new XdevButton();
		this.cmdDeleteActivity = new XdevButton();
		this.cmdEditActivity = new XdevButton();
		this.cmdReloadActivity = new XdevButton();
		this.cmdInfoActivity = new XdevButton();
		this.tableActivity = new XdevTable<>();
		this.gridLayoutRelation = new XdevGridLayout();
		this.verticalLayout5 = new XdevVerticalLayout();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdNewRelation = new XdevButton();
		this.cmdDeleteRelation = new XdevButton();
		this.tableRelation = new XdevTable<>();
		this.gridLayoutListRef = new XdevGridLayout();
		this.verticalSplitPanel2 = new XdevVerticalSplitPanel();
		this.verticalLayout2 = new XdevVerticalLayout();
		this.tableOrder = new XdevTable<>();
		this.verticalLayout4 = new XdevVerticalLayout();
		this.tableProject = new XdevTable<>();
		this.horizontalLayoutBtn = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.label = new XdevLabel();
		this.cmdVcard = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Customer.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(45.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.containerFilterComponent.setPrefixMatchOnly(false);
		this.containerFilterComponent.setImmediate(true);
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdReport.setIcon(FontAwesome.PRINT);
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdImport.setIcon(FontAwesome.FILE_EXCEL_O);
		this.cmdImport.setDescription("Import Kontakte");
		this.cmdImport.setEnabled(false);
		this.cmdImport.setVisible(false);
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Customer.class, DAOs.get(CustomerDAO.class).findAllByNumberDesc(),
				NestedProperty.of(Customer_.city, City_.ctyZip), NestedProperty.of(Customer_.city, City_.ctyName),
				NestedProperty.of(Customer_.paymentCondition, PaymentCondition_.pacName));
		this.table.setVisibleColumns(Customer_.cusNumber.getName(), Customer_.cusAccountType.getName(),
				Customer_.cusCompany.getName(), "shortname", Customer_.cusName.getName(), Customer_.cusFirstName.getName(),
				Customer_.cusAddress.getName(), NestedProperty.path(Customer_.city, City_.ctyZip),
				NestedProperty.path(Customer_.city, City_.ctyName),
				NestedProperty.path(Customer_.paymentCondition, PaymentCondition_.pacName), Customer_.cusState.getName());
		this.table.setColumnHeader("cusNumber", "Nummer");
		this.table.setConverter("cusNumber", ConverterBuilder.stringToBigInteger().build());
		this.table.setColumnHeader("cusAccountType", "Typ");
		this.table.setColumnHeader("cusCompany", "Firma");
		this.table.setColumnCollapsed("cusCompany", true);
		this.table.setColumnHeader("shortname", "Name");
		this.table.setColumnHeader("cusName", "Name");
		this.table.setColumnCollapsed("cusName", true);
		this.table.setColumnHeader("cusFirstName", "Vorname");
		this.table.setColumnCollapsed("cusFirstName", true);
		this.table.setColumnHeader("cusAddress", "Adresse");
		this.table.setColumnHeader("city.ctyZip", "PLZ");
		this.table.setConverter("city.ctyZip", ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.table.setColumnHeader("city.ctyName", "Ort");
		this.table.setColumnHeader("paymentCondition.pacName", "Zahlungsbedingung");
		this.table.setColumnCollapsed("paymentCondition.pacName", true);
		this.table.setColumnHeader("cusState", "Status");
		this.form.setMargin(new MarginInfo(false, false, true, false));
		this.tabSheet.setStyleName("framed");
		this.lblCusNumber.setValue(StringResourceUtils.optLocalizeString("{$lblCusNumber.value}", this));
		this.txtCusNumber.setRequired(true);
		this.lblType.setValue("Kontakt Typ");
		this.lblAnrede.setValue("Anrede");
		this.lblCusCompany.setValue(StringResourceUtils.optLocalizeString("{$lblCusCompany.value}", this));
		this.txtCusCompany.setMaxLength(40);
		this.lblCusName.setValue(StringResourceUtils.optLocalizeString("{$lblCusName.value}", this));
		this.txtCusName.setRequired(true);
		this.txtCusName.setMaxLength(40);
		this.lblCusFirstName.setValue(StringResourceUtils.optLocalizeString("{$lblCusFirstName.value}", this));
		this.txtCusFirstName.setMaxLength(40);
		this.linkMaps.setTargetName("_blank");
		this.linkMaps.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/gmaps32.PNG"));
		this.linkMaps.setCaption("Adresse");
		this.txtCusAddress.setMaxLength(40);
		this.lblCity.setValue(StringResourceUtils.optLocalizeString("{$lblCity.value}", this));
		this.horizontalLayoutCity.setMargin(new MarginInfo(false));
		this.txtZip.setConverter(ConverterBuilder.stringToInteger().groupingUsed(false).maximumIntegerDigits(5).build());
		this.txtZip.setMaxLength(5);
		this.txtZip.addValidator(new IntegerRangeValidator("Plz muss numerisch sein (99999)", null, 99999));
		this.cmbCity.setRequired(true);
		this.cmbCity.setItemCaptionFromAnnotation(false);
		this.cmbCity.setContainerDataSource(City.class, DAOs.get(CityDAO.class).findAll());
		this.cmbCity.setItemCaptionPropertyId(City_.ctyName.getName());
		this.lblCountry.setValue("CH");
		this.lblBirthdate.setValue("Geburtsdatum");
		this.lblCusState.setValue(StringResourceUtils.optLocalizeString("{$lblCusState.value}", this));
		this.lblAccountManager.setValue("Account Manager");
		this.txtAccountManager.setEnabled(false);
		this.txtAccountManager.setMaxLength(40);
		this.lblCusInfo.setValue(StringResourceUtils.optLocalizeString("{$lblCusInfo.value}", this));
		this.lblPaymentCondition.setValue(StringResourceUtils.optLocalizeString("{$lblPaymentCondition.value}", this));
		this.cmbPaymentCondition.setRequired(true);
		this.cmbPaymentCondition.setItemCaptionFromAnnotation(false);
		this.cmbPaymentCondition.setContainerDataSource(PaymentCondition.class,
				DAOs.get(PaymentConditionDAO.class).findAll());
		this.cmbPaymentCondition.setItemCaptionPropertyId(PaymentCondition_.pacName.getName());
		this.lblBillingTarget.setValue("Rechnungstyp");
		this.cbxAccountBillingType.setItemCaptionFromAnnotation(false);
		this.cbxSinglePdf.setCaption("Single PDF");
		this.lblBillingReports.setValue("Rechnungsanhang");
		this.cbxAccountBillingReports.setItemCaptionFromAnnotation(false);
		this.lblLabels.setValue("Label");
		this.twinColSelect.setItemCaptionFromAnnotation(false);
		this.twinColSelect.setRightColumnCaption("aktiv");
		this.twinColSelect.setIcon(FontAwesome.BOOKMARK);
		this.twinColSelect.setContainerDataSource(LabelDefinition.class);
		this.twinColSelect.setItemCaptionPropertyId(LabelDefinition_.cldText.getName());
		this.lblExtRef1.setValue("Ext Ref 1");
		this.lblExtRef2.setValue("Ext Ref 2");
		this.gridLayoutAddress.setMargin(new MarginInfo(false));
		this.verticalSplitPanel.setStyleName("large");
		this.verticalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayoutAdr.setMargin(new MarginInfo(false));
		this.horizontalLayoutAddress.setSpacing(false);
		this.horizontalLayoutAddress.setMargin(new MarginInfo(false));
		this.cmdNewAddress.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewAddress.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDeleteAddress.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdEditAddress.setIcon(FontAwesome.PENCIL);
		this.cmdReloadAddress.setIcon(FontAwesome.REFRESH);
		this.cmdInfoAddress.setIcon(FontAwesome.INFO_CIRCLE);
		this.tableAddress.setColumnReorderingAllowed(true);
		this.tableAddress.setCaption("Adressen");
		this.tableAddress.setColumnCollapsingAllowed(true);
		this.tableAddress.setContainerDataSource(Address.class, false);
		this.tableAddress.addGeneratedColumn("generated", new FunctionAddressHyperlink.Generator());
		this.tableAddress.setVisibleColumns(Address_.adrType.getName(), Address_.adrName.getName(),
				Address_.adrAddOn.getName(), Address_.adrLine0.getName(), Address_.adrZip.getName(),
				Address_.adrCity.getName(), "generated", Address_.adrValidFrom.getName());
		this.tableAddress.setColumnHeader("adrType", "Typ");
		this.tableAddress.setColumnHeader("adrName", "Name");
		this.tableAddress.setColumnHeader("adrAddOn", "Zusatz");
		this.tableAddress.setColumnCollapsed("adrAddOn", true);
		this.tableAddress.setColumnHeader("adrLine0", "Adresse");
		this.tableAddress.setColumnHeader("adrZip", "Plz");
		this.tableAddress.setColumnHeader("adrCity", "Ort");
		this.tableAddress.setColumnHeader("generated", "Maps");
		this.tableAddress.setColumnHeader("adrValidFrom", "Gültig ab");
		this.tableAddress.setConverter("adrValidFrom", ConverterBuilder.stringToDate().dateOnly().build());
		this.verticalLayoutLink.setMargin(new MarginInfo(false));
		this.horizontalLayoutLink.setSpacing(false);
		this.horizontalLayoutLink.setMargin(new MarginInfo(false));
		this.cmdNewCustomerLink.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewCustomerLink.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDeleteCustomerLink.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdEditCustomerLink.setIcon(FontAwesome.PENCIL);
		this.cmdReloadCustomerLink.setIcon(FontAwesome.REFRESH);
		this.cmdInfoCustomerLink.setIcon(FontAwesome.INFO_CIRCLE);
		this.tableLink.setColumnReorderingAllowed(true);
		this.tableLink.setCaption("Mail / Telefon / Links");
		this.tableLink.setColumnCollapsingAllowed(true);
		this.tableLink.setContainerDataSource(CustomerLink.class, false);
		this.tableLink.addGeneratedColumn("generated", new FunctionLinkHyperlink.Generator());
		this.tableLink.setVisibleColumns(CustomerLink_.cnkType.getName(), CustomerLink_.cnkLink.getName(), "generated",
				CustomerLink_.cnkDepartment.getName(), CustomerLink_.cnkRemark.getName(),
				CustomerLink_.cnkValidFrom.getName());
		this.tableLink.setColumnHeader("cnkType", "Typ");
		this.tableLink.setColumnHeader("cnkLink", "Wert");
		this.tableLink.setColumnHeader("generated", "Link");
		this.tableLink.setColumnHeader("cnkDepartment", "Bereich");
		this.tableLink.setColumnHeader("cnkRemark", "Bemerkung");
		this.tableLink.setColumnHeader("cnkValidFrom", "Gültig ab");
		this.tableLink.setConverter("cnkValidFrom", ConverterBuilder.stringToDate().dateOnly().build());
		this.gridLayoutListActivity.setMargin(new MarginInfo(false, false, true, false));
		this.verticalLayout3.setMargin(new MarginInfo(false));
		this.horizontalLayout3.setSpacing(false);
		this.horizontalLayout3.setMargin(new MarginInfo(false));
		this.cmdNewActivity.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewActivity.setCaption("");
		this.cmdDeleteActivity.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdEditActivity.setIcon(FontAwesome.PENCIL);
		this.cmdReloadActivity.setIcon(FontAwesome.REFRESH);
		this.cmdReloadActivity.setImmediate(true);
		this.cmdInfoActivity.setIcon(FontAwesome.INFO_CIRCLE);
		this.tableActivity.setCaption("Aktivititäten");
		this.tableActivity.setIcon(FontAwesome.ARROWS_ALT);
		this.tableActivity.setSortAscending(false);
		this.tableActivity.setContainerDataSource(Activity.class, false);
		this.tableActivity.addGeneratedColumn("generated", new FunctionActHyperlink.Generator());
		this.tableActivity.setVisibleColumns(Activity_.actDate.getName(), Activity_.actType.getName(),
				Activity_.actText.getName(), "generated", Activity_.costAccount.getName(),
				Activity_.actFollowingUpDate.getName());
		this.tableActivity.setColumnHeader("actDate", "Datum");
		this.tableActivity.setColumnWidth("actDate", 180);
		this.tableActivity.setColumnHeader("actType", "Typ");
		this.tableActivity.setColumnWidth("actType", 85);
		this.tableActivity.setColumnHeader("actText", "Bemerkung");
		this.tableActivity.setColumnHeader("generated", "Link");
		this.tableActivity.setColumnHeader("costAccount", "Wer");
		this.tableActivity.setColumnHeader("actFollowingUpDate", "Folgetermin");
		this.tableActivity.setConverter("actFollowingUpDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.gridLayoutRelation.setMargin(new MarginInfo(false));
		this.verticalLayout5.setMargin(new MarginInfo(false));
		this.horizontalLayout2.setSpacing(false);
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdNewRelation.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewRelation.setCaption("");
		this.cmdDeleteRelation.setIcon(FontAwesome.MINUS_CIRCLE);
		this.tableRelation.setCaption("Beziehungen");
		this.tableRelation.setIcon(FontAwesome.EXCHANGE);
		this.tableRelation.setContainerDataSource(ContactRelation.class, false,
				NestedProperty.of("customerOne.shortname", String.class),
				NestedProperty.of("customerTwo.shortname", String.class));
		this.tableRelation.setVisibleColumns("customerOne.shortname", ContactRelation_.corTypeOne.getName(),
				"customerTwo.shortname", ContactRelation_.corTypeTwo.getName());
		this.tableRelation.setColumnHeader("customerOne.shortname", "Kontakt 1");
		this.tableRelation.setColumnHeader("corTypeOne", "Typ");
		this.tableRelation.setColumnHeader("customerTwo.shortname", "Kontakt 2");
		this.tableRelation.setColumnHeader("corTypeTwo", "Typ");
		this.gridLayoutListRef.setMargin(new MarginInfo(false, false, true, false));
		this.verticalSplitPanel2.setStyleName("large");
		this.verticalSplitPanel2.setSplitPosition(60.0F, Unit.PERCENTAGE);
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
		this.verticalLayout4.setMargin(new MarginInfo(false));
		this.tableProject.setCaption("Projekte");
		this.tableProject.setIcon(FontAwesome.PRODUCT_HUNT);
		this.tableProject.setContainerDataSource(Project.class, false);
		this.tableProject.setVisibleColumns(Project_.proName.getName(), Project_.proStartDate.getName(),
				Project_.proEndDate.getName(), Project_.proHours.getName());
		this.tableProject.setColumnHeader("proName", "Projekt");
		this.tableProject.setColumnHeader("proStartDate", "Start Datum");
		this.tableProject.setConverter("proStartDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableProject.setColumnHeader("proEndDate", "End Datum");
		this.tableProject.setConverter("proEndDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableProject.setColumnHeader("proHours", "Stunden");
		this.horizontalLayoutBtn.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdVcard.setCaption("Vcard...");
		this.fieldGroup.bind(this.cmbCity, Customer_.city.getName());
		this.fieldGroup.bind(this.cmbPaymentCondition, Customer_.paymentCondition.getName());
		this.fieldGroup.bind(this.txtCusNumber, Customer_.cusNumber.getName());
		this.fieldGroup.bind(this.txtCusName, Customer_.cusName.getName());
		this.fieldGroup.bind(this.txtCusFirstName, Customer_.cusFirstName.getName());
		this.fieldGroup.bind(this.txtCusCompany, Customer_.cusCompany.getName());
		this.fieldGroup.bind(this.txtCusAddress, Customer_.cusAddress.getName());
		this.fieldGroup.bind(this.txtCusInfo, Customer_.cusInfo.getName());
		this.fieldGroup.bind(this.cbxState, Customer_.cusState.getName());
		this.fieldGroup.bind(this.txtAccountManager, Customer_.cusAccountManager.getName());
		this.fieldGroup.bind(this.cbxAccountType, Customer_.cusAccountType.getName());
		this.fieldGroup.bind(this.twinColSelect, Customer_.labelDefinitions.getName());
		this.fieldGroup.bind(this.cbxAccountSalutation, Customer_.cusSalutation.getName());
		this.fieldGroup.bind(this.datCusBirthdate, Customer_.cusBirthdate.getName());
		this.fieldGroup.bind(this.cbxAccountBillingType, Customer_.cusBillingTarget.getName());
		this.fieldGroup.bind(this.cbxAccountBillingReports, Customer_.cusBillingReport.getName());
		this.fieldGroup.bind(this.cbxSinglePdf, Customer_.cusSinglepdf.getName());
		this.fieldGroup.bind(this.txtExtRef1, Customer_.cusExtRef1.getName());
		this.fieldGroup.bind(this.txtExtRef2, Customer_.cusExtRef2.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "shortname",
				"cusAccountManager", "cusNumber", "city.ctyName", "city.ctyZip", "cusName", "cusCompany", "cusFirstName",
				"cusAccountType", "cusState", "labelDefinitions.cldId");
		this.containerFilterComponent.setSearchableProperties("cusCompany", "cusName", "city.ctyName", "cusFirstName");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReport);
		this.actionLayout.setComponentAlignment(this.cmdReport, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		this.cmdImport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdImport);
		this.actionLayout.setComponentAlignment(this.cmdImport, Alignment.MIDDLE_RIGHT);
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
		this.txtZip.setWidth(70, Unit.PIXELS);
		this.txtZip.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutCity.addComponent(this.txtZip);
		this.horizontalLayoutCity.setComponentAlignment(this.txtZip, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutCity.setExpandRatio(this.txtZip, 20.0F);
		this.cmbCity.setWidth(220, Unit.PIXELS);
		this.cmbCity.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutCity.addComponent(this.cmbCity);
		this.horizontalLayoutCity.setComponentAlignment(this.cmbCity, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutCity.setExpandRatio(this.cmbCity, 80.0F);
		this.lblCountry.setSizeUndefined();
		this.horizontalLayoutCity.addComponent(this.lblCountry);
		this.horizontalLayoutCity.setComponentAlignment(this.lblCountry, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutCity.setExpandRatio(this.lblCountry, 10.0F);
		this.gridLayoutContact.setColumns(4);
		this.gridLayoutContact.setRows(10);
		this.lblCusNumber.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCusNumber, 0, 0);
		this.txtCusNumber.setWidth(100, Unit.PERCENTAGE);
		this.txtCusNumber.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.txtCusNumber, 1, 0);
		this.lblType.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblType, 0, 1);
		this.cbxAccountType.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.cbxAccountType, 1, 1);
		this.lblAnrede.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblAnrede, 2, 1);
		this.cbxAccountSalutation.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.cbxAccountSalutation, 3, 1);
		this.lblCusCompany.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCusCompany, 0, 2);
		this.txtCusCompany.setWidth(100, Unit.PERCENTAGE);
		this.txtCusCompany.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.txtCusCompany, 1, 2, 3, 2);
		this.lblCusName.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCusName, 0, 3);
		this.txtCusName.setWidth(100, Unit.PERCENTAGE);
		this.txtCusName.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.txtCusName, 1, 3, 3, 3);
		this.lblCusFirstName.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCusFirstName, 0, 4);
		this.txtCusFirstName.setWidth(100, Unit.PERCENTAGE);
		this.txtCusFirstName.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.txtCusFirstName, 1, 4);
		this.linkMaps.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.linkMaps, 0, 5);
		this.txtCusAddress.setWidth(100, Unit.PERCENTAGE);
		this.txtCusAddress.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.txtCusAddress, 1, 5, 3, 5);
		this.lblCity.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCity, 0, 6);
		this.horizontalLayoutCity.setWidth(360, Unit.PIXELS);
		this.horizontalLayoutCity.setHeight(-1, Unit.PIXELS);
		this.gridLayoutContact.addComponent(this.horizontalLayoutCity, 1, 6, 3, 6);
		this.lblBirthdate.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblBirthdate, 0, 7);
		this.datCusBirthdate.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.datCusBirthdate, 1, 7);
		this.lblCusState.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.lblCusState, 0, 8);
		this.cbxState.setSizeUndefined();
		this.gridLayoutContact.addComponent(this.cbxState, 1, 8);
		this.gridLayoutContact.setColumnExpandRatio(0, 20.0F);
		this.gridLayoutContact.setColumnExpandRatio(1, 10.0F);
		this.gridLayoutContact.setColumnExpandRatio(2, 20.0F);
		this.gridLayoutContact.setColumnExpandRatio(3, 100.0F);
		final CustomComponent gridLayoutContact_vSpacer = new CustomComponent();
		gridLayoutContact_vSpacer.setSizeFull();
		this.gridLayoutContact.addComponent(gridLayoutContact_vSpacer, 0, 9, 3, 9);
		this.gridLayoutContact.setRowExpandRatio(9, 1.0F);
		this.gridLayoutFlags.setColumns(3);
		this.gridLayoutFlags.setRows(9);
		this.lblAccountManager.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblAccountManager, 0, 0);
		this.txtAccountManager.setWidth(100, Unit.PERCENTAGE);
		this.txtAccountManager.setHeight(-1, Unit.PIXELS);
		this.gridLayoutFlags.addComponent(this.txtAccountManager, 1, 0);
		this.lblCusInfo.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblCusInfo, 0, 1);
		this.txtCusInfo.setWidth(100, Unit.PERCENTAGE);
		this.txtCusInfo.setHeight(-1, Unit.PIXELS);
		this.gridLayoutFlags.addComponent(this.txtCusInfo, 1, 1, 2, 1);
		this.lblPaymentCondition.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblPaymentCondition, 0, 2);
		this.cmbPaymentCondition.setWidth(100, Unit.PERCENTAGE);
		this.cmbPaymentCondition.setHeight(-1, Unit.PIXELS);
		this.gridLayoutFlags.addComponent(this.cmbPaymentCondition, 1, 2);
		this.lblBillingTarget.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblBillingTarget, 0, 3);
		this.cbxAccountBillingType.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.cbxAccountBillingType, 1, 3);
		this.cbxSinglePdf.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.cbxSinglePdf, 2, 3);
		this.lblBillingReports.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblBillingReports, 0, 4);
		this.cbxAccountBillingReports.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.cbxAccountBillingReports, 1, 4);
		this.lblLabels.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblLabels, 0, 5);
		this.twinColSelect.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.twinColSelect, 1, 5);
		this.lblExtRef1.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblExtRef1, 0, 6);
		this.txtExtRef1.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.txtExtRef1, 1, 6);
		this.lblExtRef2.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.lblExtRef2, 0, 7);
		this.txtExtRef2.setSizeUndefined();
		this.gridLayoutFlags.addComponent(this.txtExtRef2, 1, 7);
		this.gridLayoutFlags.setColumnExpandRatio(1, 100.0F);
		this.gridLayoutFlags.setColumnExpandRatio(2, 100.0F);
		final CustomComponent gridLayoutFlags_vSpacer = new CustomComponent();
		gridLayoutFlags_vSpacer.setSizeFull();
		this.gridLayoutFlags.addComponent(gridLayoutFlags_vSpacer, 0, 8, 2, 8);
		this.gridLayoutFlags.setRowExpandRatio(8, 1.0F);
		this.cmdNewAddress.setSizeUndefined();
		this.horizontalLayoutAddress.addComponent(this.cmdNewAddress);
		this.horizontalLayoutAddress.setComponentAlignment(this.cmdNewAddress, Alignment.MIDDLE_CENTER);
		this.cmdDeleteAddress.setSizeUndefined();
		this.horizontalLayoutAddress.addComponent(this.cmdDeleteAddress);
		this.horizontalLayoutAddress.setComponentAlignment(this.cmdDeleteAddress, Alignment.MIDDLE_CENTER);
		this.cmdEditAddress.setSizeUndefined();
		this.horizontalLayoutAddress.addComponent(this.cmdEditAddress);
		this.horizontalLayoutAddress.setComponentAlignment(this.cmdEditAddress, Alignment.MIDDLE_CENTER);
		this.cmdReloadAddress.setSizeUndefined();
		this.horizontalLayoutAddress.addComponent(this.cmdReloadAddress);
		this.horizontalLayoutAddress.setComponentAlignment(this.cmdReloadAddress, Alignment.MIDDLE_CENTER);
		this.cmdInfoAddress.setSizeUndefined();
		this.horizontalLayoutAddress.addComponent(this.cmdInfoAddress);
		this.horizontalLayoutAddress.setComponentAlignment(this.cmdInfoAddress, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayoutAddress_spacer = new CustomComponent();
		horizontalLayoutAddress_spacer.setSizeFull();
		this.horizontalLayoutAddress.addComponent(horizontalLayoutAddress_spacer);
		this.horizontalLayoutAddress.setExpandRatio(horizontalLayoutAddress_spacer, 1.0F);
		this.horizontalLayoutAddress.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutAddress.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutAdr.addComponent(this.horizontalLayoutAddress);
		this.verticalLayoutAdr.setComponentAlignment(this.horizontalLayoutAddress, Alignment.MIDDLE_CENTER);
		this.tableAddress.setWidth(100, Unit.PERCENTAGE);
		this.tableAddress.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutAdr.addComponent(this.tableAddress);
		final CustomComponent verticalLayoutAdr_spacer = new CustomComponent();
		verticalLayoutAdr_spacer.setSizeFull();
		this.verticalLayoutAdr.addComponent(verticalLayoutAdr_spacer);
		this.verticalLayoutAdr.setExpandRatio(verticalLayoutAdr_spacer, 1.0F);
		this.cmdNewCustomerLink.setSizeUndefined();
		this.horizontalLayoutLink.addComponent(this.cmdNewCustomerLink);
		this.horizontalLayoutLink.setComponentAlignment(this.cmdNewCustomerLink, Alignment.MIDDLE_CENTER);
		this.cmdDeleteCustomerLink.setSizeUndefined();
		this.horizontalLayoutLink.addComponent(this.cmdDeleteCustomerLink);
		this.horizontalLayoutLink.setComponentAlignment(this.cmdDeleteCustomerLink, Alignment.MIDDLE_CENTER);
		this.cmdEditCustomerLink.setSizeUndefined();
		this.horizontalLayoutLink.addComponent(this.cmdEditCustomerLink);
		this.horizontalLayoutLink.setComponentAlignment(this.cmdEditCustomerLink, Alignment.MIDDLE_CENTER);
		this.cmdReloadCustomerLink.setSizeUndefined();
		this.horizontalLayoutLink.addComponent(this.cmdReloadCustomerLink);
		this.horizontalLayoutLink.setComponentAlignment(this.cmdReloadCustomerLink, Alignment.MIDDLE_CENTER);
		this.cmdInfoCustomerLink.setSizeUndefined();
		this.horizontalLayoutLink.addComponent(this.cmdInfoCustomerLink);
		this.horizontalLayoutLink.setComponentAlignment(this.cmdInfoCustomerLink, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayoutLink_spacer = new CustomComponent();
		horizontalLayoutLink_spacer.setSizeFull();
		this.horizontalLayoutLink.addComponent(horizontalLayoutLink_spacer);
		this.horizontalLayoutLink.setExpandRatio(horizontalLayoutLink_spacer, 1.0F);
		this.horizontalLayoutLink.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutLink.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutLink.addComponent(this.horizontalLayoutLink);
		this.verticalLayoutLink.setComponentAlignment(this.horizontalLayoutLink, Alignment.MIDDLE_CENTER);
		this.tableLink.setWidth(100, Unit.PERCENTAGE);
		this.tableLink.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutLink.addComponent(this.tableLink);
		this.verticalLayoutLink.setComponentAlignment(this.tableLink, Alignment.BOTTOM_LEFT);
		final CustomComponent verticalLayoutLink_spacer = new CustomComponent();
		verticalLayoutLink_spacer.setSizeFull();
		this.verticalLayoutLink.addComponent(verticalLayoutLink_spacer);
		this.verticalLayoutLink.setExpandRatio(verticalLayoutLink_spacer, 1.0F);
		this.verticalLayoutAdr.setSizeFull();
		this.verticalSplitPanel.setFirstComponent(this.verticalLayoutAdr);
		this.verticalLayoutLink.setSizeFull();
		this.verticalSplitPanel.setSecondComponent(this.verticalLayoutLink);
		this.gridLayoutAddress.setColumns(1);
		this.gridLayoutAddress.setRows(1);
		this.verticalSplitPanel.setSizeFull();
		this.gridLayoutAddress.addComponent(this.verticalSplitPanel, 0, 0);
		this.gridLayoutAddress.setColumnExpandRatio(0, 100.0F);
		this.gridLayoutAddress.setRowExpandRatio(0, 100.0F);
		this.cmdNewActivity.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdNewActivity);
		this.horizontalLayout3.setComponentAlignment(this.cmdNewActivity, Alignment.MIDDLE_LEFT);
		this.cmdDeleteActivity.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdDeleteActivity);
		this.horizontalLayout3.setComponentAlignment(this.cmdDeleteActivity, Alignment.MIDDLE_CENTER);
		this.cmdEditActivity.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdEditActivity);
		this.horizontalLayout3.setComponentAlignment(this.cmdEditActivity, Alignment.MIDDLE_LEFT);
		this.cmdReloadActivity.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdReloadActivity);
		this.horizontalLayout3.setComponentAlignment(this.cmdReloadActivity, Alignment.MIDDLE_CENTER);
		this.cmdInfoActivity.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdInfoActivity);
		this.horizontalLayout3.setComponentAlignment(this.cmdInfoActivity, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout3_spacer = new CustomComponent();
		horizontalLayout3_spacer.setSizeFull();
		this.horizontalLayout3.addComponent(horizontalLayout3_spacer);
		this.horizontalLayout3.setExpandRatio(horizontalLayout3_spacer, 1.0F);
		this.horizontalLayout3.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout3.setHeight(-1, Unit.PIXELS);
		this.verticalLayout3.addComponent(this.horizontalLayout3);
		this.verticalLayout3.setComponentAlignment(this.horizontalLayout3, Alignment.MIDDLE_LEFT);
		this.tableActivity.setSizeFull();
		this.verticalLayout3.addComponent(this.tableActivity);
		this.verticalLayout3.setExpandRatio(this.tableActivity, 100.0F);
		this.gridLayoutListActivity.setColumns(1);
		this.gridLayoutListActivity.setRows(1);
		this.verticalLayout3.setSizeFull();
		this.gridLayoutListActivity.addComponent(this.verticalLayout3, 0, 0);
		this.gridLayoutListActivity.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutListActivity.setRowExpandRatio(0, 10.0F);
		this.cmdNewRelation.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdNewRelation);
		this.horizontalLayout2.setComponentAlignment(this.cmdNewRelation, Alignment.MIDDLE_LEFT);
		this.cmdDeleteRelation.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdDeleteRelation);
		this.horizontalLayout2.setComponentAlignment(this.cmdDeleteRelation, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout2_spacer = new CustomComponent();
		horizontalLayout2_spacer.setSizeFull();
		this.horizontalLayout2.addComponent(horizontalLayout2_spacer);
		this.horizontalLayout2.setExpandRatio(horizontalLayout2_spacer, 1.0F);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.verticalLayout5.addComponent(this.horizontalLayout2);
		this.verticalLayout5.setComponentAlignment(this.horizontalLayout2, Alignment.MIDDLE_CENTER);
		this.tableRelation.setSizeFull();
		this.verticalLayout5.addComponent(this.tableRelation);
		this.verticalLayout5.setComponentAlignment(this.tableRelation, Alignment.MIDDLE_CENTER);
		this.verticalLayout5.setExpandRatio(this.tableRelation, 100.0F);
		this.gridLayoutRelation.setColumns(1);
		this.gridLayoutRelation.setRows(1);
		this.verticalLayout5.setSizeFull();
		this.gridLayoutRelation.addComponent(this.verticalLayout5, 0, 0);
		this.gridLayoutRelation.setColumnExpandRatio(0, 10.0F);
		this.gridLayoutRelation.setRowExpandRatio(0, 10.0F);
		this.tableOrder.setSizeFull();
		this.verticalLayout2.addComponent(this.tableOrder);
		this.verticalLayout2.setComponentAlignment(this.tableOrder, Alignment.MIDDLE_CENTER);
		this.verticalLayout2.setExpandRatio(this.tableOrder, 100.0F);
		this.tableProject.setSizeFull();
		this.verticalLayout4.addComponent(this.tableProject);
		this.verticalLayout4.setExpandRatio(this.tableProject, 100.0F);
		this.verticalLayout2.setSizeFull();
		this.verticalSplitPanel2.setFirstComponent(this.verticalLayout2);
		this.verticalLayout4.setSizeFull();
		this.verticalSplitPanel2.setSecondComponent(this.verticalLayout4);
		this.gridLayoutListRef.setColumns(1);
		this.gridLayoutListRef.setRows(1);
		this.verticalSplitPanel2.setSizeFull();
		this.gridLayoutListRef.addComponent(this.verticalSplitPanel2, 0, 0);
		this.gridLayoutListRef.setColumnExpandRatio(0, 100.0F);
		this.gridLayoutListRef.setRowExpandRatio(0, 100.0F);
		this.gridLayoutContact.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutContact, "Kontakt", null);
		this.gridLayoutFlags.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutFlags, "Flags", null);
		this.gridLayoutAddress.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutAddress, "Adressen", null);
		this.gridLayoutListActivity.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutListActivity, "Aktivitäten", null);
		this.gridLayoutRelation.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutRelation, "Beziehungen", null);
		this.gridLayoutListRef.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutListRef, "Referenzen", null);
		this.tabSheet.setSelectedTab(this.gridLayoutContact);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayoutBtn.addComponent(this.cmdSave);
		this.horizontalLayoutBtn.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayoutBtn.addComponent(this.cmdReset);
		this.horizontalLayoutBtn.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.label.setWidth(38, Unit.PIXELS);
		this.label.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutBtn.addComponent(this.label);
		this.horizontalLayoutBtn.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.cmdVcard.setSizeUndefined();
		this.horizontalLayoutBtn.addComponent(this.cmdVcard);
		this.horizontalLayoutBtn.setComponentAlignment(this.cmdVcard, Alignment.MIDDLE_CENTER);
		this.form.setColumns(1);
		this.form.setRows(2);
		this.tabSheet.setSizeFull();
		this.form.addComponent(this.tabSheet, 0, 0);
		this.horizontalLayoutBtn.setSizeUndefined();
		this.form.addComponent(this.horizontalLayoutBtn, 0, 1);
		this.form.setComponentAlignment(this.horizontalLayoutBtn, Alignment.MIDDLE_CENTER);
		this.form.setColumnExpandRatio(0, 100.0F);
		this.form.setRowExpandRatio(0, 100.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.form.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.form);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdReport.addClickListener(event -> this.cmdReport_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.cmdImport.addClickListener(event -> this.cmdImport_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.txtZip.addValueChangeListener(event -> this.txtZip_valueChange(event));
		this.cmbCity.addValueChangeListener(event -> this.cmbCity_valueChange(event));
		this.cmdNewAddress.addClickListener(event -> this.cmdNewAddress_buttonClick(event));
		this.cmdDeleteAddress.addClickListener(event -> this.cmdDeleteAddress_buttonClick(event));
		this.cmdEditAddress.addClickListener(event -> this.cmdEditAddress_buttonClick(event));
		this.cmdReloadAddress.addClickListener(event -> this.cmdReloadAddress_buttonClick(event));
		this.cmdInfoAddress.addClickListener(event -> this.cmdInfoAddress_buttonClick(event));
		this.tableAddress.addItemClickListener(event -> this.tableAddress_itemClick(event));
		this.tableAddress.addValueChangeListener(event -> this.tableAddress_valueChange(event));
		this.cmdNewCustomerLink.addClickListener(event -> this.cmdNewCustomerLink_buttonClick(event));
		this.cmdDeleteCustomerLink.addClickListener(event -> this.cmdDeleteCustomerLink_buttonClick(event));
		this.cmdEditCustomerLink.addClickListener(event -> this.cmdEditCustomerLink_buttonClick(event));
		this.cmdReloadCustomerLink.addClickListener(event -> this.cmdReloadCustomerLink_buttonClick(event));
		this.cmdInfoCustomerLink.addClickListener(event -> this.cmdInfoCustomerLink_buttonClick(event));
		this.tableLink.addItemClickListener(event -> this.tableLink_itemClick(event));
		this.cmdNewActivity.addClickListener(event -> this.cmdNewActivity_buttonClick(event));
		this.cmdDeleteActivity.addClickListener(event -> this.cmdDeleteActivity_buttonClick(event));
		this.cmdEditActivity.addClickListener(event -> this.cmdEditActivity_buttonClick(event));
		this.cmdReloadActivity.addClickListener(event -> this.cmdReloadActivity_buttonClick(event));
		this.cmdInfoActivity.addClickListener(event -> this.cmdInfoActivity_buttonClick(event));
		this.tableActivity.addItemClickListener(event -> this.tableActivity_itemClick(event));
		this.cmdNewRelation.addClickListener(event -> this.cmdNewRelation_buttonClick(event));
		this.cmdDeleteRelation.addClickListener(event -> this.cmdDeleteRelation_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
		this.cmdVcard.addClickListener(event -> this.cmdVcard_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdReport, cmdInfo, cmdImport, cmdNewAddress, cmdDeleteAddress,
			cmdEditAddress, cmdReloadAddress, cmdInfoAddress, cmdNewCustomerLink, cmdDeleteCustomerLink,
			cmdEditCustomerLink, cmdReloadCustomerLink, cmdInfoCustomerLink, cmdNewActivity, cmdDeleteActivity,
			cmdEditActivity, cmdReloadActivity, cmdInfoActivity, cmdNewRelation, cmdDeleteRelation, cmdSave, cmdReset,
			cmdVcard;
	private XdevGridLayout form, gridLayoutContact, gridLayoutFlags, gridLayoutAddress, gridLayoutListActivity,
			gridLayoutRelation, gridLayoutListRef;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevTable<CustomerLink> tableLink;
	private XdevFieldGroup<Customer> fieldGroup;
	private XdevHorizontalLayout actionLayout, horizontalLayoutCity, horizontalLayoutAddress, horizontalLayoutLink,
			horizontalLayout3, horizontalLayout2, horizontalLayoutBtn;
	private XdevComboBox<City> cmbCity;
	private XdevVerticalSplitPanel verticalSplitPanel, verticalSplitPanel2;
	private XdevPopupDateField datCusBirthdate;
	private XdevComboBox<?> cbxAccountType, cbxAccountSalutation, cbxState, cbxAccountBillingType, cbxAccountBillingReports;
	private XdevTable<Project> tableProject;
	private XdevTable<Order> tableOrder;
	private XdevTextField txtCusNumber, txtCusCompany, txtCusName, txtCusFirstName, txtCusAddress, txtZip,
			txtAccountManager, txtCusInfo, txtExtRef1, txtExtRef2;
	private XdevTwinColSelect<LabelDefinition> twinColSelect;
	private XdevTable<ContactRelation> tableRelation;
	private XdevLabel lblCusNumber, lblType, lblAnrede, lblCusCompany, lblCusName, lblCusFirstName, lblCity, lblCountry,
			lblBirthdate, lblCusState, lblAccountManager, lblCusInfo, lblPaymentCondition, lblBillingTarget,
			lblBillingReports, lblLabels, lblExtRef1, lblExtRef2, label;
	private XdevTable<Activity> tableActivity;
	private XdevComboBox<PaymentCondition> cmbPaymentCondition;
	private XdevTabSheet tabSheet;
	private XdevLink linkMaps;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevTable<Customer> table;
	private XdevCheckBox cbxSinglePdf;
	private XdevVerticalLayout verticalLayout, verticalLayoutAdr, verticalLayoutLink, verticalLayout3, verticalLayout5,
			verticalLayout2, verticalLayout4;
	private XdevTable<Address> tableAddress;
	// </generated-code>

}
