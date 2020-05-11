package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
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
import com.xdev.ui.masterdetail.MasterDetail;
import com.xdev.ui.util.NestedProperty;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.OrderCalculator;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.entities.City_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Customer_;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.OrderLine_;
import ch.xwr.seicentobilling.entities.Order_;
import ch.xwr.seicentobilling.entities.PaymentCondition;
import ch.xwr.seicentobilling.entities.PaymentCondition_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.ui.desktop.crm.CustomerLookupPopup;

public class OrderTabView extends XdevView {
	OrderCalculator CALC = new OrderCalculator();
	private boolean isAdmin = false;

	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrderTabView.class);


	/**
	 *
	 */
	public OrderTabView() {
		super();
		this.initUI();

		// Type
		this.comboBoxState.addItems((Object[]) LovState.State.values());
//		this.comboBoxBookedExp.addItems((Object[])LovState.BookingType.values());
//		this.comboBoxBookedPro.addItems((Object[])LovState.BookingType.values());
//		this.comboBoxMonth.addItems((Object[])LovState.Month.values());

		// Sort Tables
		final Object[] properties = { "ordNumber", "ordOrderDate" };
		final boolean[] ordering = { false, false };
		this.table.sort(properties, ordering);

		// this.tableLine.clear();
		// this.tableLine.removeAllItems();
		final Object[] properties2 = { "odlNumber", "item" };
		final boolean[] ordering2 = { true, false };
		this.tableLine.sort(properties2, ordering2);

		// set RO Fields
		setROFields();
		setDefaultFilter();

		if (Seicento.hasRole("BillingAdmin")) {
			this.cmdAdmin.setEnabled(true);
			this.cmdAdmin.setVisible(true);
		}
	}

	private void setROFields() {
		if (isBooked()) {
			this.cmdNewLine.setEnabled(false);
			this.cmdEditLine.setEnabled(false);
			this.cmdDeleteLine.setEnabled(false);

			this.cmdSave.setEnabled(false);
			this.cmdDelete.setEnabled(false);
			this.fieldGroup.setReadOnly(true);

		} else {
			this.cmdNewLine.setEnabled(true);
			this.cmdEditLine.setEnabled(true);
			this.cmdDeleteLine.setEnabled(true);

			this.cmdDelete.setEnabled(true);
			this.cmdSave.setEnabled(true);
			this.fieldGroup.setReadOnly(false);
		}

		this.txtOrdAmountBrut.setEnabled(false);
		this.txtOrdAmountNet.setEnabled(false);
		this.txtOrdAmountVat.setEnabled(false);
		this.txtOrdNumber.setEnabled(false);

		this.dateOrdBookedOn.setEnabled(false);
		this.dateOrdCreated.setEnabled(false);
		this.dateOrdPayDate.setEnabled(false);
		this.dateOrdDueDate.setEnabled(false);
		this.cmbCustomer.setEnabled(false);

		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null) {
			hasData = false;
		}
		this.cmdSave.setEnabled(hasData);
		this.tabSheet.setEnabled(hasData);

	}

	private void setDefaultFilter() {
		final Calendar cal = Calendar.getInstance();
		// final Date today = cal.getTime();
		cal.add(Calendar.YEAR, -1); // to get previous year add -1
		final Date prevYear = cal.getTime();

		final Date[] val = new Date[] { prevYear };
		final FilterData[] fd = new FilterData[] {
				new FilterData("ordBillDate", new FilterOperator.GreaterEqual(), val) };

		this.containerFilterComponent.setFilterData(fd);

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
//		final Order bean = (Order) event.getProperty().getValue();
//		System.out.println("Select 2: " + this.table.isSelected(bean));
		if (this.fieldGroup.getItemDataSource() == null) {
			return;
		}


		final Order bean2 = this.fieldGroup.getItemDataSource().getBean();
		this.table.select(bean2);
		reloadTableLineList();

		prepareCustomerCombo(this.table.getSelectedItem().getBean().getCustomer());

		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		unselectOrderTable();

		this.fieldGroup.setItemDataSource(getNewDaoWithDefaults());
		checkOrderNumber(true, false);
		setROFields();

		final XdevBeanContainer<OrderLine> myList = this.tableLine.getBeanContainerDataSource();
		myList.removeAll();

	}

	private void unselectOrderTable() {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Order bean = this.table.getSelectedItem().getBean();
		this.table.unselect(bean);
	}

	private Order getNewDaoWithDefaults() {
		String usr = Seicento.getUserName();
		if (usr != null && usr.length() > 20) {
			usr = usr.substring(0, 20);
		}

		final Order dao = new Order();

		dao.setOrdState(LovState.State.active);
		dao.setOrdBillDate(new Date());
		dao.setOrdOrderDate(new Date());
		dao.setOrdAmountBrut(new Double(0.));
		dao.setOrdAmountNet(new Double(0.));

		dao.setOrdCreated(new Date());
		dao.setOrdCreatedBy(usr);
		dao.setOrdText("");

		return dao;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNewLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewLine_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
//			Notification.show("Zeile hinzufügen", "Rechnungskopf wurde noch nich gespeichert",
//					Notification.Type.WARNING_MESSAGE);
//			return;
			this.cmdSave.click();
		}

		final Long beanId = null;
		final Long objId = this.fieldGroup.getItemDataSource().getBean().getOrdId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupOrderLine();

	}

	private void popupOrderLine() {
		final Window win = OrderLinePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdSave")) {
					reloadTableLineList();

					calculateHeader();
					saveHeader(false);
				}

			}
		});
		this.getUI().addWindow(win);
	}

	private void saveHeader(final boolean changeCount) {
		this.fieldGroup.save();
		this.setROFields();

		// Objektstamm
		if (changeCount) {
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getOrdId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
		}
	}

	private void reloadTableLineList() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return;
		}
		final Order ord = this.fieldGroup.getItemDataSource().getBean();
//		this.table.select(ord); //select main table

		final XdevBeanContainer<OrderLine> myList = this.tableLine.getBeanContainerDataSource();
		myList.removeAll();
		myList.addAll(new OrderLineDAO().findByOrder(ord));

		this.tableLine.refreshRowCache();
		this.tableLine.getBeanContainerDataSource().refresh();

	}

	private void reloadTableOrderList() {
		final Order ord = this.fieldGroup.getItemDataSource().getBean();

		// final XdevBeanContainer<Order> myList =
		// this.table.getBeanContainerDataSource();
		// myList.remove...
		// myList.addAll(new OrderDAO().findAll());

		// this.table.removeAllItems();
		// this.table.addItems(new OrderDAO().findAll());

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
		this.table.sort();

		if (ord != null) {
			this.table.select(ord);
		}
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
					doDelete(OrderTabView.this.table);
					reloadTableOrderList();
				}
			}

			private void doDelete(final XdevTable<Order> mytab) {
				final Order bean = mytab.getSelectedItem().getBean();
				// Delete Lines
				doDeleteLines(bean);
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getOrdId(), bean.getClass().getSimpleName());

				final OrderDAO dao = new OrderDAO();
				dao.remove(bean);
				mytab.getBeanContainerDataSource().refresh();

				try {
					mytab.select(mytab.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					// ignore
					OrderTabView.this.fieldGroup.setItemDataSource(new Order());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

			private void doDeleteLines(final Order bean) {
				final OrderLineDAO dao = new OrderLineDAO();
				final List<OrderLine> olst = dao.findByOrder(bean);

				final RowObjectManager man = new RowObjectManager();
				final OrderLineDAO daoL = new OrderLineDAO();
				for (final OrderLine orderLine : olst) {
					man.deleteObject(orderLine.getOdlId(), orderLine.getClass().getSimpleName());
					// Delete Record
					daoL.remove(orderLine);
				}

			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableLine.getSelectedItem().getBean().getOdlId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupOrderLine();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final OrderLine bean = this.tableLine.getSelectedItem().getBean();
		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getOdlId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Order bean = this.table.getSelectedItem().getBean();
		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getOrdId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadLine_buttonClick(final Button.ClickEvent event) {
		this.tableLine.refreshRowCache();
		this.tableLine.getBeanContainerDataSource().refresh();
		this.tableLine.sort();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
		this.table.sort();

		// reloadTableOrderList();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteLine_buttonClick(final Button.ClickEvent event) {
		final XdevTable<OrderLine> tab = this.tableLine;
		if (tab.getSelectedItem() == null) {
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
					calculateHeader();
				}
			}

			private void doDelete() {
				final OrderLine bean = tab.getSelectedItem().getBean();
				// Update RowObject
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getOdlId(), bean.getClass().getSimpleName());
				// Delete Record
				final OrderLineDAO dao = new OrderLineDAO();
				dao.remove(bean);
				// refresh tab
				tab.removeItem(bean);
				tab.getBeanContainerDataSource().refresh();

				// if (!tab.isEmpty()) tab.select(tab.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #tableLine}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick() && !isBooked()) {
			// Notification.show("Event Triggered ", Notification.Type.TRAY_NOTIFICATION);
			final OrderLine obj = (OrderLine) event.getItemId();
			this.tableLine.select(obj); // reselect after double-click

			final Long beanId = obj.getOdlId(); // this.tableLine.getSelectedItem().getBean().getPrlId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupOrderLine();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbPaymentCondition}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbPaymentCondition_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}
		if (this.cmbPaymentCondition.getSelectedItem() != null) {
			// final PaymentCondition bean = (PaymentCondition)
			// event.getProperty().getValue();
			final PaymentCondition bean = this.cmbPaymentCondition.getSelectedItem().getBean();

			final Calendar now = Calendar.getInstance(); // Gets the current date and time
			now.setTime(this.dateOrdBillDate.getValue());
			now.add(Calendar.DAY_OF_MONTH, bean.getPacNbrOfDays());
			this.dateOrdDueDate.setValue(now.getTime());

		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		if (isBooked()) {
			return;
		}
		final boolean isNew = isNew(); // assign before save. is always false after save

		if (!AreFieldsValid()) {
			return;
		}

		try {

			checkOrderNumber(isNew, false);
			calculateHeader();
			saveHeader(true);
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final PersistenceException cx) {
			final String msg = SeicentoCrud.getPerExceptionError(cx);
			Notification.show("Fehler beim Speichern", msg, Notification.Type.ERROR_MESSAGE);
			cx.printStackTrace();
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

		// postSave
		checkOrderNumber(isNew, true);
		refreshList(isNew);
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

	private void refreshList(final boolean isNew) {
		if (!isNew) {
			return;
		}
		reloadTableOrderList();
	}

	private void checkOrderNumber(final boolean isNew, final boolean commitNbr) {
		if (!isNew) {
			return;
		}

		Integer nbr = null;
		try {
			nbr = Integer.parseInt(this.txtOrdNumber.getValue());
		} catch (final Exception e) {
			nbr = new Integer(0);
		}

		if (!commitNbr) {
			this.txtOrdNumber.setValue(this.CALC.getNewOrderNumber(false, nbr).toString());
		} else {
			this.CALC.getNewOrderNumber(true, nbr);
		}
	}

	private void calculateHeader() {
		if (isNew()) {
			return; // Header wurde noch nie gespeichert
		}

		this.CALC.commitFields(this.fieldGroup);
		final Order bean = this.fieldGroup.getItemDataSource().getBean();
		final Order newBean = this.CALC.calculateHeader(bean);

		this.fieldGroup.setItemDataSource(newBean);
	}

	private boolean isNew() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return true;
		}
		final Order bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean.getOrdId() == null || bean.getOrdId() < 1) {
			return true;
		}
		return false;
	}

	/**
	 * Order Header is booked...
	 *
	 * @return
	 */
	private boolean isBooked() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return false;
		}

		final Order bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean == null) {
			return false;
		}
		if (this.isAdmin) {
			return false;
		}

		if (bean.getOrdBookedOn() != null) {
			return true;
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
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
//		final Order bean = (Order) event.getItemId(); //this.table.getSelectedItem().getBean();
//		System.out.println("Select 1: " + this.table.isSelected(bean));
//
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCopy}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopy_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

		ConfirmDialog.show(getUI(), "Rechnung kopieren", "Rechnung wirklich kopieren?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if ("cmdOk".equals(retval)) {
					final OrderTabView gui = OrderTabView.this;
					final Order newBean = gui.CALC.copyOrder(gui.table.getSelectedItem().getBean());
					gui.fieldGroup.getItemDataSource().setBean(newBean);

					reloadTableOrderList();
					gui.table.sanitizeSelection();

					// this.table.addItem(newBean);

					Notification.show("Auftrag kopieren", "Neuer Auftrag erstellt: " + newBean.getOrdNumber(),
							Notification.Type.TRAY_NOTIFICATION);

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
		final Order bean = this.table.getSelectedItem().getBean();

		if (isOrderValid(bean)) {
			final JasperManager jsp = new JasperManager();
			jsp.addParameter("OrderNummer", "" + bean.getOrdNumber());
//			jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());

			Page.getCurrent().open(jsp.getUri(JasperManager.BillReport1), "_blank");
		}

	}

	private boolean isOrderValid(final Order bean) {
		if (! this.CALC.isOrderValid(bean)) {
			LOG.warn("Ungültige Rechnung gefunden: " + bean.getOrdNumber() + " !!");
			if (bean.getOrdBookedOn() == null) {
				this.CALC.calculateHeader(bean);
				LOG.info("Ungültige Rechnung  - neu berechnet");
				return true;
			}
			Notification.show("Rechnung", "Die Rechnung hat ungültige Beträge. Bitte kontrollieren",
					Notification.Type.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdPdfReport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPdfReport_buttonClick(final Button.ClickEvent event) {
		// PDF Order
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

		final Order bean = this.table.getSelectedItem().getBean();
		if (isOrderValid(bean)) {
			UI.getCurrent().getSession().setAttribute("orderbean", bean);

			popupMailDownload();
		}
	}

	private void popupMailDownload() {
		final Window win = MailDownloadPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				// nothing
			}
		});
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdAdmin}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAdmin_buttonClick(final Button.ClickEvent event) {
		this.isAdmin = !this.isAdmin;
		setROFields();

		if (this.isAdmin) {
			this.cmdAdmin.setIcon(FontAwesome.GEARS);
		} else {
			this.cmdAdmin.setIcon(FontAwesome.GEAR);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbCustomer}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCustomer_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}
		// if (event.getProperty().)
		if (this.cmbCustomer.getSelectedItem() != null) {
			// final Customer cus = (Customer) event.getProperty().getValue();
			final Customer cus = this.cmbCustomer.getSelectedItem().getBean();
			this.cmbPaymentCondition.setValue(cus.getPaymentCondition());
		}

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
		OrderTabView.this.cmbCustomer.addItem(bean);
		OrderTabView.this.cmbCustomer.setValue(bean);
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

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayoutLeft = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.cmdCopy = new XdevButton();
		this.cmdReport = new XdevButton();
		this.cmdPdfReport = new XdevButton();
		this.cmdAdmin = new XdevButton();
		this.table = new XdevTable<>();
		this.verticalLayoutRight = new XdevVerticalLayout();
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.form = new XdevGridLayout();
		this.tabSheet = new XdevTabSheet();
		this.gridLayoutHdr = new XdevGridLayout();
		this.lblOrdNumber = new XdevLabel();
		this.txtOrdNumber = new XdevTextField();
		this.lblCustomer = new XdevLabel();
		this.horizontalLayoutCus = new XdevHorizontalLayout();
		this.cmbCustomer = new XdevComboBox<>();
		this.btnSearch = new XdevButton();
		this.lblOrdBillDate = new XdevLabel();
		this.dateOrdBillDate = new XdevPopupDateField();
		this.lblOrdOrderDate = new XdevLabel();
		this.dateOrdOrderDate = new XdevPopupDateField();
		this.lblOrdText = new XdevLabel();
		this.textArea = new XdevTextArea();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblPaymentCondition = new XdevLabel();
		this.cmbPaymentCondition = new XdevComboBox<>();
		this.lblOrdAmountBrut = new XdevLabel();
		this.txtOrdAmountBrut = new XdevTextField();
		this.lblOrdAmountNet = new XdevLabel();
		this.txtOrdAmountNet = new XdevTextField();
		this.lblOrdAmountVat = new XdevLabel();
		this.txtOrdAmountVat = new XdevTextField();
		this.gridLayoutDetails = new XdevGridLayout();
		this.lblOrdCreated = new XdevLabel();
		this.dateOrdCreated = new XdevPopupDateField();
		this.lblOrdPayDate = new XdevLabel();
		this.dateOrdPayDate = new XdevPopupDateField();
		this.lblOrdDueDate = new XdevLabel();
		this.dateOrdDueDate = new XdevPopupDateField();
		this.lblOrdBookedOn = new XdevLabel();
		this.dateOrdBookedOn = new XdevPopupDateField();
		this.lblOrdState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.fieldGroup = new XdevFieldGroup<>(Order.class);
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayoutAction = new XdevHorizontalLayout();
		this.cmdNewLine = new XdevButton();
		this.cmdDeleteLine = new XdevButton();
		this.cmdReloadLine = new XdevButton();
		this.cmdInfoLine = new XdevButton();
		this.cmdEditLine = new XdevButton();
		this.tableLine = new XdevTable<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(40.0F, Unit.PERCENTAGE);
		this.verticalLayoutLeft.setMargin(new MarginInfo(false));
		this.containerFilterComponent.setPrefixMatchOnly(false);
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdDelete.setDescription("Rechnung löschen");
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdReload.setDescription("Liste aktualsieren");
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdInfo.setDescription("Objektinfo");
		this.cmdCopy.setIcon(FontAwesome.COPY);
		this.cmdCopy.setDescription("Rechnung kopieren");
		this.cmdReport.setIcon(FontAwesome.PRINT);
		this.cmdReport.setDescription("Jasper Report starten");
		this.cmdPdfReport.setIcon(FontAwesome.ENVELOPE);
		this.cmdPdfReport.setDescription("Mail mit PDF vorbereiten...");
		this.cmdAdmin.setIcon(FontAwesome.GEAR);
		this.cmdAdmin.setDescription("Admin Modus");
		this.cmdAdmin.setEnabled(false);
		this.cmdAdmin.setVisible(false);
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Order.class, DAOs.get(OrderDAO.class).findAll(),
				NestedProperty.of(Order_.customer, Customer_.cusNumber),
				NestedProperty.of("customer.shortname", String.class),
				NestedProperty.of(Order_.customer, Customer_.city, City_.ctyName));
		this.table.setVisibleColumns(Order_.ordNumber.getName(), NestedProperty.path(Order_.customer, Customer_.cusNumber),
				"customer.shortname", NestedProperty.path(Order_.customer, Customer_.city, City_.ctyName),
				Order_.ordAmountBrut.getName(), Order_.ordAmountNet.getName(), Order_.ordBillDate.getName(),
				Order_.project.getName(), Order_.paymentCondition.getName(), Order_.ordBookedOn.getName(),
				Order_.ordCreatedBy.getName(), Order_.ordState.getName());
		this.table.setColumnHeader("ordNumber", "Nummer");
		this.table.setConverter("ordNumber", ConverterBuilder.stringToBigInteger().groupingUsed(false).build());
		this.table.setColumnHeader("customer.cusNumber", "K#");
		this.table.setConverter("customer.cusNumber", ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.table.setColumnHeader("customer.shortname", "Kunde");
		this.table.setColumnHeader("customer.city.ctyName", "Ort");
		this.table.setColumnCollapsed("customer.city.ctyName", true);
		this.table.setColumnHeader("ordAmountBrut", "Brutto");
		this.table.setColumnAlignment("ordAmountBrut", Table.Align.RIGHT);
		this.table.setConverter("ordAmountBrut", ConverterBuilder.stringToDouble().currency().build());
		this.table.setColumnHeader("ordAmountNet", "Netto");
		this.table.setColumnAlignment("ordAmountNet", Table.Align.RIGHT);
		this.table.setConverter("ordAmountNet", ConverterBuilder.stringToDouble().currency().build());
		this.table.setColumnHeader("ordBillDate", "R-Datum");
		this.table.setConverter("ordBillDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnCollapsed("ordBillDate", true);
		this.table.setColumnHeader("project", "Projekt");
		this.table.setColumnCollapsed("project", true);
		this.table.setColumnHeader("paymentCondition", "Frist");
		this.table.setColumnCollapsed("paymentCondition", true);
		this.table.setColumnHeader("ordBookedOn", "Gebucht");
		this.table.setConverter("ordBookedOn", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnCollapsed("ordBookedOn", true);
		this.table.setColumnHeader("ordCreatedBy", "Erstellt von");
		this.table.setColumnCollapsed("ordCreatedBy", true);
		this.table.setColumnHeader("ordState", "Status");
		this.verticalLayoutRight.setMargin(new MarginInfo(false));
		this.verticalSplitPanel.setStyleName("large");
		this.verticalSplitPanel.setSplitPosition(55.0F, Unit.PERCENTAGE);
		this.form.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.gridLayoutHdr.setMargin(new MarginInfo(true, true, false, true));
		this.lblOrdNumber.setValue(StringResourceUtils.optLocalizeString("{$lblOrdNumber.value}", this));
		this.txtOrdNumber.setConverter(ConverterBuilder.stringToBigInteger().groupingUsed(false).build());
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
		this.lblOrdBillDate.setValue(StringResourceUtils.optLocalizeString("{$lblOrdBillDate.value}", this));
		this.lblOrdOrderDate.setValue(StringResourceUtils.optLocalizeString("{$lblOrdOrderDate.value}", this));
		this.lblOrdText.setValue(StringResourceUtils.optLocalizeString("{$lblOrdText.value}", this));
		this.textArea.setRows(2);
		this.textArea.setMaxLength(256);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setContainerDataSource(Project.class);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblPaymentCondition.setValue(StringResourceUtils.optLocalizeString("{$lblPaymentCondition.value}", this));
		this.cmbPaymentCondition.setRequired(true);
		this.cmbPaymentCondition.setItemCaptionFromAnnotation(false);
		this.cmbPaymentCondition.setContainerDataSource(PaymentCondition.class,
				DAOs.get(PaymentConditionDAO.class).findAllActive());
		this.cmbPaymentCondition.setItemCaptionPropertyId(PaymentCondition_.pacName.getName());
		this.lblOrdAmountBrut.setValue(StringResourceUtils.optLocalizeString("{$lblOrdAmountBrut.value}", this));
		this.txtOrdAmountBrut.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.lblOrdAmountNet.setValue(StringResourceUtils.optLocalizeString("{$lblOrdAmountNet.value}", this));
		this.txtOrdAmountNet.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.lblOrdAmountVat.setValue(StringResourceUtils.optLocalizeString("{$lblOrdAmountVat.value}", this));
		this.txtOrdAmountVat.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtOrdAmountVat.setEnabled(false);
		this.gridLayoutDetails.setMargin(new MarginInfo(true, true, false, true));
		this.lblOrdCreated.setValue(StringResourceUtils.optLocalizeString("{$lblOrdCreated.value}", this));
		this.lblOrdPayDate.setValue(StringResourceUtils.optLocalizeString("{$lblOrdPayDate.value}", this));
		this.lblOrdDueDate.setValue(StringResourceUtils.optLocalizeString("{$lblOrdDueDate.value}", this));
		this.lblOrdBookedOn.setValue(StringResourceUtils.optLocalizeString("{$lblOrdBookedOn.value}", this));
		this.lblOrdState.setValue(StringResourceUtils.optLocalizeString("{$lblOrdState.value}", this));
		this.fieldGroup.bind(this.txtOrdNumber, Order_.ordNumber.getName());
		this.fieldGroup.bind(this.cmbCustomer, Order_.customer.getName());
		this.fieldGroup.bind(this.dateOrdOrderDate, Order_.ordOrderDate.getName());
		this.fieldGroup.bind(this.dateOrdCreated, Order_.ordCreated.getName());
		this.fieldGroup.bind(this.dateOrdPayDate, Order_.ordPayDate.getName());
		this.fieldGroup.bind(this.textArea, Order_.ordText.getName());
		this.fieldGroup.bind(this.cmbPaymentCondition, Order_.paymentCondition.getName());
		this.fieldGroup.bind(this.cmbProject, Order_.project.getName());
		this.fieldGroup.bind(this.comboBoxState, Order_.ordState.getName());
		this.fieldGroup.bind(this.dateOrdBillDate, Order_.ordBillDate.getName());
		this.fieldGroup.bind(this.txtOrdAmountBrut, Order_.ordAmountBrut.getName());
		this.fieldGroup.bind(this.txtOrdAmountNet, Order_.ordAmountNet.getName());
		this.fieldGroup.bind(this.dateOrdDueDate, Order_.ordDueDate.getName());
		this.fieldGroup.bind(this.dateOrdBookedOn, Order_.ordBookedOn.getName());
		this.fieldGroup.bind(this.txtOrdAmountVat, "ordAmountVat");
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayoutAction.setSpacing(false);
		this.horizontalLayoutAction.setMargin(new MarginInfo(false));
		this.cmdNewLine.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewLine.setDescription(StringResourceUtils.optLocalizeString("{$cmdNewLine.description}", this));
		this.cmdDeleteLine.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdReloadLine.setIcon(FontAwesome.REFRESH);
		this.cmdInfoLine.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdEditLine.setIcon(FontAwesome.PENCIL);
		this.tableLine.setColumnReorderingAllowed(true);
		this.tableLine.setColumnCollapsingAllowed(true);
		this.tableLine.setContainerDataSource(OrderLine.class, false);
		this.tableLine.setVisibleColumns(OrderLine_.odlNumber.getName(), OrderLine_.odlText.getName(),
				OrderLine_.odlQuantity.getName(), OrderLine_.odlPrice.getName(), OrderLine_.odlAmountBrut.getName(),
				OrderLine_.odlVatAmount.getName(), OrderLine_.odlAmountNet.getName(), OrderLine_.costAccount.getName(),
				OrderLine_.vat.getName(), OrderLine_.item.getName(), OrderLine_.odlState.getName());
		this.tableLine.setColumnHeader("odlNumber", "Position");
		this.tableLine.setColumnHeader("odlText", "Text");
		this.tableLine.setColumnHeader("odlQuantity", "Menge");
		this.tableLine.setColumnAlignment("odlQuantity", Table.Align.RIGHT);
		this.tableLine.setConverter("odlQuantity",
				ConverterBuilder.stringToDouble().minimumFractionDigits(1).maximumFractionDigits(2).build());
		this.tableLine.setColumnHeader("odlPrice", "Preis");
		this.tableLine.setColumnAlignment("odlPrice", Table.Align.RIGHT);
		this.tableLine.setConverter("odlPrice", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnHeader("odlAmountBrut", "Brutto");
		this.tableLine.setColumnAlignment("odlAmountBrut", Table.Align.RIGHT);
		this.tableLine.setConverter("odlAmountBrut", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnCollapsed("odlAmountBrut", true);
		this.tableLine.setColumnHeader("odlVatAmount", "Mwst");
		this.tableLine.setColumnAlignment("odlVatAmount", Table.Align.RIGHT);
		this.tableLine.setConverter("odlVatAmount", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnCollapsed("odlVatAmount", true);
		this.tableLine.setColumnHeader("odlAmountNet", "Netto");
		this.tableLine.setColumnAlignment("odlAmountNet", Table.Align.RIGHT);
		this.tableLine.setConverter("odlAmountNet", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnHeader("costAccount", "Kostenstelle");
		this.tableLine.setColumnHeader("vat", "Mwst Code");
		this.tableLine.setColumnHeader("item", "Artikel");
		this.tableLine.setColumnHeader("odlState", "Status");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "customer", "paymentCondition",
				"project", "ordNumber", "ordState", "ordAmountBrut", "ordOrderDate", "ordBillDate", "ordPayDate");
		this.containerFilterComponent.setSearchableProperties("ordCreatedBy", "ordText", "customer.cusCompany",
				"customer.cusName", "project.proName", "project.proExtReference");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		this.cmdCopy.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdCopy);
		this.actionLayout.setComponentAlignment(this.cmdCopy, Alignment.MIDDLE_CENTER);
		this.cmdReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReport);
		this.actionLayout.setComponentAlignment(this.cmdReport, Alignment.MIDDLE_CENTER);
		this.cmdPdfReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdPdfReport);
		this.actionLayout.setComponentAlignment(this.cmdPdfReport, Alignment.MIDDLE_CENTER);
		this.cmdAdmin.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdAdmin);
		this.actionLayout.setComponentAlignment(this.cmdAdmin, Alignment.MIDDLE_RIGHT);
		final CustomComponent actionLayout_spacer = new CustomComponent();
		actionLayout_spacer.setSizeFull();
		this.actionLayout.addComponent(actionLayout_spacer);
		this.actionLayout.setExpandRatio(actionLayout_spacer, 1.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutLeft.addComponent(this.containerFilterComponent);
		this.verticalLayoutLeft.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.actionLayout.setWidth(100, Unit.PERCENTAGE);
		this.actionLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutLeft.addComponent(this.actionLayout);
		this.verticalLayoutLeft.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayoutLeft.addComponent(this.table);
		this.verticalLayoutLeft.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayoutLeft.setExpandRatio(this.table, 100.0F);
		this.cmbCustomer.setWidth(100, Unit.PERCENTAGE);
		this.cmbCustomer.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutCus.addComponent(this.cmbCustomer);
		this.horizontalLayoutCus.setExpandRatio(this.cmbCustomer, 60.0F);
		this.btnSearch.setSizeUndefined();
		this.horizontalLayoutCus.addComponent(this.btnSearch);
		this.horizontalLayoutCus.setExpandRatio(this.btnSearch, 20.0F);
		this.gridLayoutHdr.setColumns(4);
		this.gridLayoutHdr.setRows(8);
		this.lblOrdNumber.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdNumber, 0, 0);
		this.txtOrdNumber.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.txtOrdNumber, 1, 0);
		this.lblCustomer.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblCustomer, 0, 1);
		this.horizontalLayoutCus.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutCus.setHeight(-1, Unit.PIXELS);
		this.gridLayoutHdr.addComponent(this.horizontalLayoutCus, 1, 1, 3, 1);
		this.lblOrdBillDate.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdBillDate, 0, 2);
		this.dateOrdBillDate.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.dateOrdBillDate, 1, 2);
		this.lblOrdOrderDate.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdOrderDate, 2, 2);
		this.dateOrdOrderDate.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.dateOrdOrderDate, 3, 2);
		this.lblOrdText.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdText, 0, 3);
		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.gridLayoutHdr.addComponent(this.textArea, 1, 3, 3, 3);
		this.lblProject.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblProject, 0, 4);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.gridLayoutHdr.addComponent(this.cmbProject, 1, 4);
		this.lblPaymentCondition.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblPaymentCondition, 2, 4);
		this.cmbPaymentCondition.setWidth(100, Unit.PERCENTAGE);
		this.cmbPaymentCondition.setHeight(-1, Unit.PIXELS);
		this.gridLayoutHdr.addComponent(this.cmbPaymentCondition, 3, 4);
		this.lblOrdAmountBrut.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdAmountBrut, 0, 5);
		this.txtOrdAmountBrut.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.txtOrdAmountBrut, 1, 5);
		this.lblOrdAmountNet.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdAmountNet, 2, 5);
		this.txtOrdAmountNet.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.txtOrdAmountNet, 3, 5);
		this.lblOrdAmountVat.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.lblOrdAmountVat, 0, 6);
		this.txtOrdAmountVat.setSizeUndefined();
		this.gridLayoutHdr.addComponent(this.txtOrdAmountVat, 1, 6);
		this.gridLayoutHdr.setColumnExpandRatio(1, 10.0F);
		this.gridLayoutHdr.setColumnExpandRatio(3, 10.0F);
		final CustomComponent gridLayoutHdr_vSpacer = new CustomComponent();
		gridLayoutHdr_vSpacer.setSizeFull();
		this.gridLayoutHdr.addComponent(gridLayoutHdr_vSpacer, 0, 7, 3, 7);
		this.gridLayoutHdr.setRowExpandRatio(7, 1.0F);
		this.gridLayoutDetails.setColumns(3);
		this.gridLayoutDetails.setRows(6);
		this.lblOrdCreated.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.lblOrdCreated, 0, 0);
		this.dateOrdCreated.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.dateOrdCreated, 1, 0);
		this.lblOrdPayDate.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.lblOrdPayDate, 0, 1);
		this.dateOrdPayDate.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.dateOrdPayDate, 1, 1);
		this.lblOrdDueDate.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.lblOrdDueDate, 0, 2);
		this.dateOrdDueDate.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.dateOrdDueDate, 1, 2);
		this.lblOrdBookedOn.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.lblOrdBookedOn, 0, 3);
		this.dateOrdBookedOn.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.dateOrdBookedOn, 1, 3);
		this.lblOrdState.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.lblOrdState, 0, 4);
		this.comboBoxState.setSizeUndefined();
		this.gridLayoutDetails.addComponent(this.comboBoxState, 1, 4);
		final CustomComponent gridLayoutDetails_hSpacer = new CustomComponent();
		gridLayoutDetails_hSpacer.setSizeFull();
		this.gridLayoutDetails.addComponent(gridLayoutDetails_hSpacer, 2, 0, 2, 4);
		this.gridLayoutDetails.setColumnExpandRatio(2, 1.0F);
		final CustomComponent gridLayoutDetails_vSpacer = new CustomComponent();
		gridLayoutDetails_vSpacer.setSizeFull();
		this.gridLayoutDetails.addComponent(gridLayoutDetails_vSpacer, 0, 5, 1, 5);
		this.gridLayoutDetails.setRowExpandRatio(5, 1.0F);
		this.gridLayoutHdr.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutHdr, StringResourceUtils.optLocalizeString("{$gridLayoutHdr.caption}", this),
				null);
		this.gridLayoutDetails.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutDetails,
				StringResourceUtils.optLocalizeString("{$gridLayoutDetails.caption}", this), null);
		this.tabSheet.setSelectedTab(this.gridLayoutHdr);
		this.form.setColumns(1);
		this.form.setRows(1);
		this.tabSheet.setSizeFull();
		this.form.addComponent(this.tabSheet, 0, 0);
		this.form.setColumnExpandRatio(0, 100.0F);
		this.form.setRowExpandRatio(0, 100.0F);
		this.cmdNewLine.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdNewLine);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdNewLine, Alignment.MIDDLE_CENTER);
		this.cmdDeleteLine.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdDeleteLine);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdDeleteLine, Alignment.MIDDLE_CENTER);
		this.cmdReloadLine.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdReloadLine);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdReloadLine, Alignment.MIDDLE_CENTER);
		this.cmdInfoLine.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdInfoLine);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdInfoLine, Alignment.MIDDLE_CENTER);
		this.cmdEditLine.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdEditLine);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdEditLine, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutAction.setSizeUndefined();
		this.verticalLayout.addComponent(this.horizontalLayoutAction);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutAction, Alignment.MIDDLE_LEFT);
		this.tableLine.setSizeFull();
		this.verticalLayout.addComponent(this.tableLine);
		this.verticalLayout.setComponentAlignment(this.tableLine, Alignment.BOTTOM_CENTER);
		this.verticalLayout.setExpandRatio(this.tableLine, 100.0F);
		this.form.setSizeFull();
		this.verticalSplitPanel.setFirstComponent(this.form);
		this.verticalLayout.setSizeFull();
		this.verticalSplitPanel.setSecondComponent(this.verticalLayout);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.verticalSplitPanel.setSizeFull();
		this.verticalLayoutRight.addComponent(this.verticalSplitPanel);
		this.verticalLayoutRight.setComponentAlignment(this.verticalSplitPanel, Alignment.MIDDLE_CENTER);
		this.verticalLayoutRight.setExpandRatio(this.verticalSplitPanel, 100.0F);
		this.horizontalLayout.setSizeUndefined();
		this.verticalLayoutRight.addComponent(this.horizontalLayout);
		this.verticalLayoutRight.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayoutLeft.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayoutLeft);
		this.verticalLayoutRight.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.verticalLayoutRight);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.cmdCopy.addClickListener(event -> this.cmdCopy_buttonClick(event));
		this.cmdReport.addClickListener(event -> this.cmdReport_buttonClick(event));
		this.cmdPdfReport.addClickListener(event -> this.cmdPdfReport_buttonClick(event));
		this.cmdAdmin.addClickListener(event -> this.cmdAdmin_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.cmbCustomer.addValueChangeListener(event -> this.cmbCustomer_valueChange(event));
		this.btnSearch.addClickListener(event -> this.btnSearch_buttonClick(event));
		this.cmbPaymentCondition.addValueChangeListener(event -> this.cmbPaymentCondition_valueChange(event));
		this.cmdNewLine.addClickListener(event -> this.cmdNewLine_buttonClick(event));
		this.cmdDeleteLine.addClickListener(event -> this.cmdDeleteLine_buttonClick(event));
		this.cmdReloadLine.addClickListener(event -> this.cmdReloadLine_buttonClick(event));
		this.cmdInfoLine.addClickListener(event -> this.cmdInfoLine_buttonClick(event));
		this.cmdEditLine.addClickListener(event -> this.cmdEditLine_buttonClick(event));
		this.tableLine.addItemClickListener(event -> this.tableLine_itemClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdCopy, cmdReport, cmdPdfReport, cmdAdmin, btnSearch,
			cmdNewLine, cmdDeleteLine, cmdReloadLine, cmdInfoLine, cmdEditLine, cmdSave, cmdReset;
	private XdevLabel lblOrdNumber, lblCustomer, lblOrdBillDate, lblOrdOrderDate, lblOrdText, lblProject,
			lblPaymentCondition, lblOrdAmountBrut, lblOrdAmountNet, lblOrdAmountVat, lblOrdCreated, lblOrdPayDate,
			lblOrdDueDate, lblOrdBookedOn, lblOrdState;
	private XdevComboBox<PaymentCondition> cmbPaymentCondition;
	private XdevTabSheet tabSheet;
	private XdevGridLayout form, gridLayoutHdr, gridLayoutDetails;
	private XdevComboBox<Project> cmbProject;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevHorizontalLayout actionLayout, horizontalLayoutCus, horizontalLayoutAction, horizontalLayout;
	private XdevVerticalSplitPanel verticalSplitPanel;
	private XdevPopupDateField dateOrdBillDate, dateOrdOrderDate, dateOrdCreated, dateOrdPayDate, dateOrdDueDate,
			dateOrdBookedOn;
	private XdevTextArea textArea;
	private XdevTable<OrderLine> tableLine;
	private XdevComboBox<?> comboBoxState;
	private XdevTable<Order> table;
	private XdevComboBox<Customer> cmbCustomer;
	private XdevTextField txtOrdNumber, txtOrdAmountBrut, txtOrdAmountNet, txtOrdAmountVat;
	private XdevFieldGroup<Order> fieldGroup;
	private XdevVerticalLayout verticalLayoutLeft, verticalLayoutRight, verticalLayout;
	// </generated-code>

}
