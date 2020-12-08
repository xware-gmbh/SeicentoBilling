
package ch.xwr.seicentobilling.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.ImageIcons;
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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.OrderCalculator;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.PaymentCondition;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.ui.crm.CustomerLookupPopup;


@Route("order")
public class OrderTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(OrderTabView.class);
	
	OrderCalculator CALC    = new OrderCalculator();
	private boolean isAdmin = false;

	/**
	 *
	 */
	public OrderTabView()
	{
		super();
		this.initUI();
		this.comboBoxState.setItems(LovState.State.values());
		this.gridLayoutDetail.setVisible(false);
		
		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.gridLayoutHdrTab, this.gridLayoutHdr);
		tabsToPages.put(this.gridLayoutDetailsTab, this.gridLayoutDetail);

		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});

		// this.sortList(true);
		
		// set RO Fields
		this.setROFields();
		this.setDefaultFilter();
		
		if(Seicento.hasRole("BillingAdmin"))
		{
			this.cmdAdmin.setEnabled(true);
			this.cmdAdmin.setVisible(true);
		}
	}
	
	private void sortList(final boolean sortAll)
	{
		final GridSortOrder<Order> sortCol1 =
			new GridSortOrder<>(this.grid.getColumnByKey("ordNumber"), SortDirection.DESCENDING);
		final GridSortOrder<Order> sortCol2 =
			new GridSortOrder<>(this.grid.getColumnByKey("ordOrderDate"), SortDirection.DESCENDING);
		this.grid.sort(Arrays.asList(sortCol1, sortCol2));
		
		if(!sortAll)
		{
			return;
		}
		
		final GridSortOrder<OrderLine> sortCol11 =
			new GridSortOrder<>(this.grid2.getColumnByKey("odlNumber"), SortDirection.ASCENDING);
		final GridSortOrder<OrderLine> sortCol21 =
			new GridSortOrder<>(this.grid2.getColumnByKey("item"), SortDirection.DESCENDING);
		this.grid2.sort(Arrays.asList(sortCol11, sortCol21));
		
	}

	private void setDefaultFilter()
	{
		final Calendar cal = Calendar.getInstance();
		// final Date today = cal.getTime();
		cal.add(Calendar.YEAR, -1); // to get previous year add -1
		final Date prevYear = cal.getTime();

		final Date[] val = new Date[]{prevYear};

		final FilterEntry pe =
			new FilterEntry("ordBillDate", new FilterOperator.Greater().key(), val);
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{pe}));

	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Order> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final OrderDAO orderDao  = new OrderDAO();
			final Order    orderBean =
				orderDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getOrdId());
			this.binder.setBean(orderBean);

			this.reloadTableLineList();

			this.prepareCustomerCombo(orderBean.getCustomer());

			this.setROFields();
		}
	}
	
	private void prepareCustomerCombo(final Customer bean)
	{
		if(bean != null)
		{
			this.binder.setReadOnly(false);

			// this.cmbCustomer.addItem(bean);
			// this.cmbCustomer.setValue(bean);
		}
	}

	private void reloadTableLineList()
	{
		if(this.binder.getBean() == null)
		{
			return;
		}
		final Order ord = this.binder.getBean();
		// this.table.select(ord); //select main table
		
		this.grid2.setDataProvider(DataProvider.ofCollection(new OrderLineDAO().findByOrder(ord)));
		
		this.grid2.getDataProvider().refreshAll();
		
	}

	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final Order bean = this.binder.getBean();
		if(bean.getOrdId() == null || bean.getOrdId() < 1)
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
		this.binder.setReadOnly(false);
		this.binder.setBean(this.getNewDaoWithDefaults());
		this.checkOrderNumber(true, false);
		this.setROFields();
		
		this.grid2.setDataProvider(DataProvider.ofCollection(new ArrayList<OrderLine>()));
		
		this.unselectOrderTable();
		
	}

	private Order getNewDaoWithDefaults()
	{
		String usr = Seicento.getUserName();
		if(usr != null && usr.length() > 20)
		{
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
	 * Event handler delegate method for the {@link Button} {@link #cmdReload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_onClick(final ClickEvent<Button> event)
	{
		this.grid.setDataProvider(DataProvider.ofCollection(new OrderDAO().findAll()));
		this.grid.getDataProvider().refreshAll();
		this.grid.sort(this.grid.getSortOrder());
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		if(this.isBooked())
		{
			return;
		}

		final boolean isNew = this.isNew(); // assign before save. is always false after save
		
		if(!this.binder.validate().isOk())
		{
			return;
		}
		
		try
		{
			
			this.checkOrderNumber(isNew, false);
			this.calculateHeader();
			this.saveHeader(true);
			Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
		}
		catch(final PersistenceException cx)
		{
			final String msg = SeicentoCrud.getPerExceptionError(cx);
			Notification.show(msg, 5000, Notification.Position.BOTTOM_END);
			cx.printStackTrace();
		}
		catch(final Exception e)
		{
			Notification.show(e.getMessage(), 5000, Notification.Position.BOTTOM_END);
			e.printStackTrace();
		}
		
		// postSave
		this.checkOrderNumber(isNew, true);
		this.refreshList(isNew);
		
	}

	private void refreshList(final boolean isNew)
	{
		if(!isNew)
		{
			return;
		}
		this.reloadTableOrderList();
	}

	private void reloadTableOrderList()
	{
		final Order ord = this.binder.getBean();
		this.cmdReload_onClick(null);
		this.grid.getDataProvider().refreshAll();
		this.grid.sort(this.grid.getSortOrder());
		
		if(ord != null && ord.getOrdId() != null)
		{
			this.grid.select(ord);
		}
		else
		{
			this.unselectOrderTable();
		}
	}

	private void unselectOrderTable()
	{
		if(this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final Order bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		this.grid.getSelectionModel().deselect(bean);
	}

	private void saveHeader(final boolean changeCount)
	{
		
		if(SeicentoCrud.doSave(this.binder, new OrderDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				this.setROFields();
				if(changeCount)
				{
					final RowObjectManager man = new RowObjectManager();
					man.updateObject(this.binder.getBean().getOrdId(),
						this.binder.getBean().getClass().getSimpleName());
				}
			}
			catch(final Exception e)
			{
				OrderTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		
	}

	private void setROFields()
	{
		if(this.isBooked())
		{
			this.cmdNewLine.setEnabled(false);
			this.cmdEditLine.setEnabled(false);
			this.cmdDeleteLine.setEnabled(false);
			
			this.cmdSave.setEnabled(false);
			this.cmdDelete.setEnabled(false);
			this.binder.setReadOnly(true);
			
		}
		else
		{
			this.cmdNewLine.setEnabled(true);
			this.cmdEditLine.setEnabled(true);
			this.cmdDeleteLine.setEnabled(true);
			
			this.cmdDelete.setEnabled(true);
			this.cmdSave.setEnabled(true);
			this.binder.setReadOnly(false);
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
		if(this.binder.getBean() == null)
		{
			hasData = false;
			this.cmdSave.setEnabled(hasData);
		}
		this.gridLayoutHdr.setEnabled(hasData);
		
		if(this.isAdmin)
		{
			this.dateOrdBookedOn.setEnabled(true);
		}
		
	}
	
	private void checkOrderNumber(final boolean isNew, final boolean commitNbr)
	{
		if(!isNew)
		{
			return;
		}

		Integer nbr = null;
		try
		{
			nbr = Integer.parseInt(this.txtOrdNumber.getValue());
		}
		catch(final Exception e)
		{
			nbr = new Integer(0);
		}

		if(!commitNbr)
		{
			this.txtOrdNumber.setEnabled(true);
			this.txtOrdNumber.setValue(this.CALC.getNewOrderNumber(false, nbr).toString());
		}
		else
		{
			this.CALC.getNewOrderNumber(true, nbr);
		}
	}
	
	private void calculateHeader()
	{
		if(this.isNew())
		{
			return; // Header wurde noch nie gespeichert
		}

		// this.CALC.commitFields(this.fieldGroup);
		final Order bean    = this.binder.getBean();
		final Order newBean = this.CALC.calculateHeader(bean);

		this.binder.setBean(newBean);
	}

	/**
	 * Order Header is booked...
	 *
	 * @return
	 */
	private boolean isBooked()
	{
		if(this.binder.getBean() == null)
		{
			return false;
		}

		final Order bean = this.binder.getBean();
		if(bean == null)
		{
			return false;
		}
		if(this.isAdmin)
		{
			return false;
		}

		if(bean.getOrdBookedOn() != null)
		{
			return true;
		}

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
		}
		else
		{
			final OrderDAO orderDao  = new OrderDAO();
			final Order    orderBean = orderDao.find(this.binder.getBean().getOrdId());
			this.binder.setBean(orderBean);
		}
		this.setROFields();
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfo}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */

	private void cmdInfo_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final Order  bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		final Dialog win  = RowObjectView.getPopupWindow();
		
		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.add(new RowObjectView(bean.getOrdId(), bean.getClass().getSimpleName()));
		win.open();
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

				final Order bean = this.grid.getSelectionModel().getFirstSelectedItem().get();

				// Delete Lines
				this.doDeleteLines(bean);
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getOrdId(), bean.getClass().getSimpleName());

				final OrderDAO dao = new OrderDAO();
				dao.remove(bean);
				dao.flush();

				this.binder.removeBean();
				OrderTabView.this.binder.setBean(new Order());
				this.grid.setDataProvider(DataProvider.ofCollection(new OrderDAO().findAll()));
				OrderTabView.this.grid.getDataProvider().refreshAll();
				try
				{
					this.grid.getSelectionModel().select(this.grid.getSelectionModel().getFirstSelectedItem().get());
				}
				catch(final Exception e)
				{
					// ignore
					OrderTabView.this.binder.setBean(new Order());
				}
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				this.reloadTableOrderList();

			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
			catch(final Exception e)
			{
				OrderTabView.LOG.error("Error on delete", e);
			}
		});
	}
	
	private void doDeleteLines(final Order bean)
	{
		final OrderLineDAO    dao  = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);
		
		final RowObjectManager man  = new RowObjectManager();
		final OrderLineDAO     daoL = new OrderLineDAO();
		for(final OrderLine orderLine : olst)
		{
			man.deleteObject(orderLine.getOdlId(), orderLine.getClass().getSimpleName());
			// Delete Record
			daoL.remove(orderLine);
		}
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbCustomer}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCustomer_valueChanged(final ComponentValueChangeEvent<ComboBox<Customer>, Customer> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}
		if(this.cmbCustomer.getValue() != null)
		{
			final Customer cus = this.cmbCustomer.getValue();
			this.cmbPaymentCondition.setValue(cus.getPaymentCondition());
		}
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
					OrderTabView.this.cmbCustomer.setValue(bean);

				}
			}
		});
		win.open();

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
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNewLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewLine_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			// Notification.show("Zeile hinzufügen", "Rechnungskopf wurde noch nich gespeichert",
			// Notification.Type.WARNING_MESSAGE);
			// return;
			this.cmdSave.click();
		}

		final Long beanId = null;
		final Long objId  = this.binder.getBean().getOrdId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupOrderLine();

	}

	private void popupOrderLine()
	{
		
		final Dialog win = OrderLinePopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if(retval == null)
				{
					retval = "cmdCancel";
				}
				if(retval.equals("cmdSave"))
				{
					OrderTabView.this.reloadTableLineList();
					OrderTabView.this.calculateHeader();
					OrderTabView.this.saveHeader(false);
				}
			}
		});
		win.open();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdEditLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditLine_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid2.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.grid2.getSelectionModel().getFirstSelectedItem().get().getOdlId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupOrderLine();
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfoLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoLine_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid2.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final OrderLine bean = this.grid2.getSelectionModel().getFirstSelectedItem().get();
		final Dialog    win  = RowObjectView.getPopupWindow();
		
		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.add(new RowObjectView(bean.getOdlId(), bean.getClass().getSimpleName()));
		win.open();

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReloadLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadLine_onClick(final ClickEvent<Button> event)
	{
		final Order ord = this.binder.getBean();
		// this.table.select(ord); //select main table
		
		this.grid2.setDataProvider(DataProvider.ofCollection(new OrderLineDAO().findByOrder(ord)));
		this.grid2.getDataProvider().refreshAll();
		this.grid2.sort(this.grid2.getSortOrder());
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteLine_onClick(final ClickEvent<Button> event)
	{
		if(this.grid2.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}
		
		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{

				final OrderLine bean = this.grid2.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getOdlId(), bean.getClass().getSimpleName());

				final OrderLineDAO dao = new OrderLineDAO();
				dao.remove(bean);
				dao.flush();

				final Order ord = this.binder.getBean();
				// this.table.select(ord); //select main table
				
				this.grid2.setDataProvider(DataProvider.ofCollection(new OrderLineDAO().findByOrder(ord)));

				OrderTabView.this.grid2.getDataProvider().refreshAll();

				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				this.reloadTableOrderList();

			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
			catch(final Exception e)
			{
				OrderTabView.LOG.error("Error on delete", e);
			}
		});
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid2}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid2_onItemClick(final ItemClickEvent<OrderLine> event)
	{
		
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid2}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid2_onItemDoubleClick(final ItemDoubleClickEvent<OrderLine> event)
	{
		if(!this.isBooked())
		{
			// Notification.show("Event Triggered ", Notification.Type.TRAY_NOTIFICATION);
			final OrderLine obj = event.getItem();
			this.grid2.select(obj); // reselect after double-click

			final Long beanId = obj.getOdlId(); // this.tableLine.getSelectedItem().getBean().getPrlId();
			final Long objId  = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			this.popupOrderLine();
		}

	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbPaymentCondition}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void
		cmbPaymentCondition_valueChanged(
			final ComponentValueChangeEvent<ComboBox<PaymentCondition>, PaymentCondition> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}
		if(event.getValue() != null)
		{
			// final PaymentCondition bean = (PaymentCondition)
			// event.getProperty().getValue();
			final PaymentCondition bean = event.getValue();
			
			final Calendar  now        = Calendar.getInstance();                     // Gets the current date and time
			final LocalDate billDate   = this.dateOrdBillDate.getValue();
			final LocalDate dateOrdDue = billDate.plusMonths(bean.getPacNbrOfDays());
			this.dateOrdDueDate.setValue(dateOrdDue);
			
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCopy}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopy_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle", 5000,
				Notification.Position.BOTTOM_END);
			return;
		}

		ConfirmDialog.show("Rechnung kopieren", "Rechnung wirklich kopieren?", okEvent -> {

			final OrderTabView gui     = OrderTabView.this;
			final Order        newBean = gui.CALC.copyOrder(gui.grid.getSelectionModel().getFirstSelectedItem().get());
			gui.binder.setBean(newBean);

			this.reloadTableOrderList();
			// TODO:gui.grid.sanitizeSelection();

			// this.table.addItem(newBean);

			Notification.show("Neuer Auftrag erstellt: " + newBean.getOrdNumber(), 5000,
				Notification.Position.BOTTOM_END);

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
		final Order bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		
		if(this.isOrderValid(bean))
		{
			final JasperManager jsp = new JasperManager();
			jsp.addParameter("OrderNummer", "" + bean.getOrdNumber());
			// jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());

			UI.getCurrent().getPage().open(jsp.getUri(JasperManager.BillReport1), "_blank");
		}
	}

	private boolean isOrderValid(final Order bean)
	{
		if(!this.CALC.isOrderValid(bean))
		{
			OrderTabView.LOG.warn("Ungültige Rechnung gefunden: " + bean.getOrdNumber() + " !!");
			if(bean.getOrdBookedOn() == null)
			{
				final Order beanC = this.CALC.calculateHeader(bean);
				new OrderDAO().save(beanC);
				OrderTabView.LOG.warn("Ungültige Rechnung  - neu berechnet");
				return true;
			}
			Notification.show("Die Rechnung hat ungültige Beträge. Bitte kontrollieren", 5000,
				Notification.Position.BOTTOM_END);
			return false;
		}
		return true;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdPdfReport}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPdfReport_onClick(final ClickEvent<Button> event)
	{
		// PDF Order
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle", 5000,
				Notification.Position.BOTTOM_END);
			return;
		}

		final Order bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(this.isOrderValid(bean))
		{
			UI.getCurrent().getSession().setAttribute("orderbean", bean);

			this.popupMailDownload();
		}
	}

	private void popupMailDownload()
	{
		final Dialog win = MailDownloadPopup.getPopupWindow();
		win.open();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdAdmin}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAdmin_onClick(final ClickEvent<Button> event)
	{
		this.isAdmin = !this.isAdmin;
		this.setROFields();

		if(this.isAdmin)
		{
			this.dateOrdPayDate.setEnabled(true);
			this.cmdAdmin.setIcon(IronIcons.SETTINGS_APPLICATIONS.create());
		}
		else
		{
			this.cmdAdmin.setIcon(IronIcons.SETTINGS.create());
		}
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout              = new SplitLayout();
		this.verticalLayout           = new VerticalLayout();
		this.containerFilterComponent = new FilterComponent();
		this.horizontalLayout2        = new HorizontalLayout();
		this.cmdNew                   = new Button();
		this.cmdDelete                = new Button();
		this.cmdReload                = new Button();
		this.cmdInfo                  = new Button();
		this.cmdCopy                  = new Button();
		this.cmdReport                = new Button();
		this.cmdPdfReport             = new Button();
		this.cmdAdmin                 = new Button();
		this.grid                     = new Grid<>(Order.class, false);
		this.verticalLayout2          = new VerticalLayout();
		this.tabs                     = new Tabs();
		this.gridLayoutHdrTab         = new Tab();
		this.gridLayoutDetailsTab     = new Tab();
		this.splitLayout2             = new SplitLayout();
		this.verticalLayout3          = new VerticalLayout();
		this.gridLayoutHdr            = new FormLayout();
		this.formItem2                = new FormItem();
		this.lblOrdNumber             = new Label();
		this.txtOrdNumber             = new TextField();
		this.formItem                 = new FormItem();
		this.lblCustomer              = new Label();
		this.cmbCustomer              = new ComboBox<>();
		this.btnSearch                = new Button();
		this.formItem3                = new FormItem();
		this.lblOrdBillDate           = new Label();
		this.dateOrdBillDate          = new DatePicker();
		this.formItem4                = new FormItem();
		this.lblOrdOrderDate          = new Label();
		this.dateOrdOrderDate         = new DatePicker();
		this.formItem5                = new FormItem();
		this.lblOrdText               = new Label();
		this.textArea                 = new TextArea();
		this.formItem6                = new FormItem();
		this.lblProject               = new Label();
		this.cmbProject               = new ComboBox<>();
		this.formItem7                = new FormItem();
		this.lblPaymentCondition      = new Label();
		this.cmbPaymentCondition      = new ComboBox<>();
		this.formItem8                = new FormItem();
		this.lblOrdAmountBrut         = new Label();
		this.txtOrdAmountBrut         = new TextField();
		this.formItem9                = new FormItem();
		this.lblOrdAmountNet          = new Label();
		this.txtOrdAmountNet          = new TextField();
		this.formItem10               = new FormItem();
		this.lblOrdAmountVat          = new Label();
		this.txtOrdAmountVat          = new TextField();
		this.gridLayoutDetail         = new FormLayout();
		this.formItem20               = new FormItem();
		this.lblOrdCreated            = new Label();
		this.dateOrdCreated           = new DatePicker();
		this.formItem21               = new FormItem();
		this.lblOrdPayDate            = new Label();
		this.dateOrdPayDate           = new DatePicker();
		this.formItem22               = new FormItem();
		this.lblOrdDueDate            = new Label();
		this.dateOrdDueDate           = new DatePicker();
		this.formItem23               = new FormItem();
		this.lblOrdBookedOn           = new Label();
		this.dateOrdBookedOn          = new DatePicker();
		this.formItem24               = new FormItem();
		this.lblOrdState              = new Label();
		this.comboBoxState            = new ComboBox<>();
		this.verticalLayout4          = new VerticalLayout();
		this.horizontalLayout         = new HorizontalLayout();
		this.cmdNewLine               = new Button();
		this.cmdDeleteLine            = new Button();
		this.cmdReloadLine            = new Button();
		this.cmdInfoLine              = new Button();
		this.cmdEditLine              = new Button();
		this.grid2                    = new Grid<>(OrderLine.class, false);
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(Order.class);
		
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.cmdCopy.setIcon(VaadinIcon.COPY.create());
		this.cmdReport.setIcon(IronIcons.PRINT.create());
		this.cmdPdfReport.setIcon(VaadinIcon.ENVELOPE.create());
		this.cmdAdmin.setIcon(IronIcons.SETTINGS.create());
		this.grid.addColumn(Order::getOrdNumber).setKey("ordNumber").setHeader("Nummer").setFrozen(true)
			.setResizable(true)
			.setSortable(true);
		this.grid.addColumn(v -> Optional.ofNullable(v).map(Order::getCustomer).map(Customer::getCusName).orElse(null))
			.setKey("customer.cusName").setHeader("K#").setFrozen(true).setResizable(true).setSortable(true);
		this.grid
			.addColumn(v -> Optional.ofNullable(v).map(Order::getCustomer).map(Customer::getShortname).orElse(null))
			.setKey("customer.shortname").setHeader("Kunde").setFrozen(true).setResizable(true).setSortable(true);
		this.grid
			.addColumn(v -> Optional.ofNullable(v).map(Order::getCustomer).map(Customer::getCity).map(City::getCtyName)
				.orElse(null))
			.setKey("customer.city.ctyName").setHeader("Ort").setFrozen(true).setResizable(true).setSortable(true);
		this.grid
			.addColumn(new NumberRenderer<>(Order::getOrdAmountBrut,
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH")).build(), ""))
			.setKey("ordAmountBrut").setHeader("Brutto").setFrozen(true).setResizable(true).setSortable(true);
		this.grid
			.addColumn(new NumberRenderer<>(Order::getOrdAmountNet,
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH")).build(), ""))
			.setKey("ordAmountNet").setHeader("Netto").setFrozen(true).setResizable(true).setSortable(true)
			.setVisible(false);
		this.grid.addColumn(Order::getOrdBillDate).setKey("ordBillDate").setHeader("R-Datum").setResizable(true)
			.setSortable(true).setAutoWidth(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Order::getProject)).setKey("project").setHeader("Projekt")
			.setSortable(false).setAutoWidth(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Order::getPaymentCondition)).setKey("paymentCondition")
			.setHeader("Frist")
			.setSortable(false).setAutoWidth(true).setVisible(false);
		this.grid.addColumn(Order::getOrdBookedOn).setKey("ordBookedOn").setHeader("Gebucht").setResizable(true)
			.setSortable(true).setAutoWidth(true).setVisible(false);
		this.grid.addColumn(Order::getOrdCreatedBy).setKey("ordCreatedBy").setHeader("Erstellt von").setResizable(true)
			.setSortable(true).setAutoWidth(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Order::getOrdState)).setKey("ordState").setHeader("Status")
			.setResizable(true).setSortable(true).setAutoWidth(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new OrderDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setPadding(false);
		this.tabs.setMinHeight("50px");
		this.gridLayoutHdrTab.setLabel(StringResourceUtils.optLocalizeString("{$gridLayoutHdr.caption}", this));
		this.gridLayoutDetailsTab.setLabel(StringResourceUtils.optLocalizeString("{$gridLayoutDetails.caption}", this));
		this.splitLayout2.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.verticalLayout3.setSpacing(false);
		this.verticalLayout3.setPadding(false);
		this.gridLayoutHdr.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblOrdNumber.setText(StringResourceUtils.optLocalizeString("{$lblOrdNumber.value}", this));
		this.lblCustomer.setText(StringResourceUtils.optLocalizeString("{$lblCustomer.value}", this));
		this.cmbCustomer.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbCustomer::getItemLabelGenerator),
			DataProvider.ofCollection(new CustomerDAO().findAll()));
		this.cmbCustomer.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Customer::getFullname));
		this.btnSearch.setIcon(IronIcons.SEARCH.create());
		this.lblOrdBillDate.setText(StringResourceUtils.optLocalizeString("{$lblOrdBillDate.value}", this));
		this.dateOrdBillDate.setLocale(new Locale("de", "CH"));
		this.lblOrdOrderDate.setText(StringResourceUtils.optLocalizeString("{$lblOrdOrderDate.value}", this));
		this.dateOrdOrderDate.setLocale(new Locale("de", "CH"));
		this.lblOrdText.setText(StringResourceUtils.optLocalizeString("{$lblOrdText.value}", this));
		this.textArea.setLabel("");
		this.lblProject.setText(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.cmbProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.lblPaymentCondition.setText(StringResourceUtils.optLocalizeString("{$lblPaymentCondition.value}", this));
		this.cmbPaymentCondition.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbPaymentCondition::getItemLabelGenerator),
			DataProvider.ofCollection(new PaymentConditionDAO().findAllActive()));
		this.cmbPaymentCondition.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(PaymentCondition::getPacName));
		this.lblOrdAmountBrut.setText(StringResourceUtils.optLocalizeString("{$lblOrdAmountBrut.value}", this));
		this.lblOrdAmountNet.setText(StringResourceUtils.optLocalizeString("{$lblOrdAmountNet.value}", this));
		this.lblOrdAmountVat.setText(StringResourceUtils.optLocalizeString("{$lblOrdAmountVat.value}", this));
		this.gridLayoutDetail.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblOrdCreated.setText(StringResourceUtils.optLocalizeString("{$lblOrdCreated.value}", this));
		this.dateOrdCreated.setLocale(new Locale("de", "CH"));
		this.lblOrdPayDate.setText(StringResourceUtils.optLocalizeString("{$lblOrdPayDate.value}", this));
		this.dateOrdPayDate.setLocale(new Locale("de", "CH"));
		this.lblOrdDueDate.setText(StringResourceUtils.optLocalizeString("{$lblOrdDueDate.value}", this));
		this.dateOrdDueDate.setLocale(new Locale("de", "CH"));
		this.lblOrdBookedOn.setText(StringResourceUtils.optLocalizeString("{$lblOrdBookedOn.value}", this));
		this.dateOrdBookedOn.setLocale(new Locale("de", "CH"));
		this.lblOrdState.setText(StringResourceUtils.optLocalizeString("{$lblOrdState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.verticalLayout4.setSpacing(false);
		this.verticalLayout4.setPadding(false);
		this.horizontalLayout.setSpacing(false);
		this.cmdNewLine.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteLine.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReloadLine.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfoLine.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.cmdEditLine.setIcon(ImageIcons.EDIT.create());
		this.grid2.addColumn(OrderLine::getOdlNumber).setKey("odlNumber").setHeader("Position").setSortable(true);
		this.grid2.addColumn(OrderLine::getOdlText).setKey("odlText").setHeader("Text").setSortable(true);
		this.grid2.addColumn(OrderLine::getOdlQuantity).setKey("odlQuantity").setHeader("Menge").setSortable(true);
		this.grid2
			.addColumn(
				new NumberRenderer<>(OrderLine::getOdlPrice,
					NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
						.currency(Currency.getInstance("CHF")).build(),
					""))
			.setKey("odlPrice").setHeader("Preis").setSortable(true);
		this.grid2
			.addColumn(
				new NumberRenderer<>(OrderLine::getOdlAmountBrut,
					NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
						.currency(Currency.getInstance("CHF")).build(),
					""))
			.setKey("odlAmountBrut").setHeader("Brutto").setSortable(true).setVisible(false);
		this.grid2
			.addColumn(
				new NumberRenderer<>(OrderLine::getOdlVatAmount,
					NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
						.currency(Currency.getInstance("CHF")).build(),
					""))
			.setKey("odlVatAmount").setHeader("Mwst").setSortable(true).setVisible(false);
		this.grid2
			.addColumn(
				new NumberRenderer<>(OrderLine::getOdlAmountNet,
					NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
						.currency(Currency.getInstance("CHF")).build(),
					""))
			.setKey("odlAmountNet").setHeader("Netto").setSortable(true);
		this.grid2.addColumn(new CaptionRenderer<>(OrderLine::getCostAccount)).setKey("costAccount")
			.setHeader("Kostenstelle").setSortable(false);
		this.grid2.addColumn(new CaptionRenderer<>(OrderLine::getVat)).setKey("vat").setHeader("Mwst Code")
			.setSortable(false);
		this.grid2.addColumn(new CaptionRenderer<>(OrderLine::getItem)).setKey("item").setHeader("Artikel")
			.setSortable(false);
		this.grid2.addColumn(new CaptionRenderer<>(OrderLine::getOdlState)).setKey("odlState").setHeader("Status")
			.setSortable(true);
		this.grid2.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.binder.setValidatorsDisabled(true);
		
		this.binder.forField(this.txtOrdNumber).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("ordNumber");
		this.binder.forField(this.cmbCustomer).bind("customer");
		this.binder.forField(this.textArea).withNullRepresentation("").bind("ordText");
		this.binder.forField(this.cmbProject).bind("project");
		this.binder.forField(this.cmbPaymentCondition).bind("paymentCondition");
		this.binder.forField(this.txtOrdAmountBrut).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("ordAmountBrut");
		this.binder.forField(this.txtOrdAmountNet).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("ordAmountNet");
		this.binder.forField(this.txtOrdAmountVat).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("ordAmountVat");
		this.binder.forField(this.comboBoxState).bind("ordState");
		this.binder.forField(this.dateOrdBillDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordBillDate");
		this.binder.forField(this.dateOrdOrderDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordOrderDate");
		this.binder.forField(this.dateOrdCreated)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordCreated");
		this.binder.forField(this.dateOrdPayDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordPayDate");
		this.binder.forField(this.dateOrdDueDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordDueDate");
		this.binder.forField(this.dateOrdBookedOn)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("ordBookedOn");
		
		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("customer.cusCompany", "customer.cusName", "ordCreatedBy", "ordText",
				"project.proExtReference",
				"project.proName"),
			Arrays.asList("customer.cusName", "customer.cusNumber", "ordAmountBrut", "ordAmountNet", "ordBillDate",
				"ordNumber", "ordOrderDate", "ordPayDate", "ordState", "paymentCondition", "project")));
		
		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.cmdCopy.setSizeUndefined();
		this.cmdReport.setSizeUndefined();
		this.cmdPdfReport.setSizeUndefined();
		this.cmdAdmin.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdInfo, this.cmdCopy,
			this.cmdReport,
			this.cmdPdfReport, this.cmdAdmin);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.tabs.add(this.gridLayoutHdrTab, this.gridLayoutDetailsTab);
		this.lblOrdNumber.setSizeUndefined();
		this.lblOrdNumber.getElement().setAttribute("slot", "label");
		this.txtOrdNumber.setWidthFull();
		this.txtOrdNumber.setHeight(null);
		this.formItem2.add(this.lblOrdNumber, this.txtOrdNumber);
		this.lblCustomer.setSizeUndefined();
		this.lblCustomer.getElement().setAttribute("slot", "label");
		this.cmbCustomer.setWidth("75%");
		this.cmbCustomer.setHeight(null);
		this.btnSearch.setSizeUndefined();
		this.formItem.add(this.lblCustomer, this.cmbCustomer, this.btnSearch);
		this.lblOrdBillDate.setSizeUndefined();
		this.lblOrdBillDate.getElement().setAttribute("slot", "label");
		this.dateOrdBillDate.setWidthFull();
		this.dateOrdBillDate.setHeight(null);
		this.formItem3.add(this.lblOrdBillDate, this.dateOrdBillDate);
		this.lblOrdOrderDate.setSizeUndefined();
		this.lblOrdOrderDate.getElement().setAttribute("slot", "label");
		this.dateOrdOrderDate.setWidthFull();
		this.dateOrdOrderDate.setHeight(null);
		this.formItem4.add(this.lblOrdOrderDate, this.dateOrdOrderDate);
		this.lblOrdText.setSizeUndefined();
		this.lblOrdText.getElement().setAttribute("slot", "label");
		this.textArea.setWidthFull();
		this.textArea.setHeight(null);
		this.formItem5.add(this.lblOrdText, this.textArea);
		this.lblProject.setSizeUndefined();
		this.lblProject.getElement().setAttribute("slot", "label");
		this.cmbProject.setWidthFull();
		this.cmbProject.setHeight(null);
		this.formItem6.add(this.lblProject, this.cmbProject);
		this.lblPaymentCondition.setSizeUndefined();
		this.lblPaymentCondition.getElement().setAttribute("slot", "label");
		this.cmbPaymentCondition.setWidthFull();
		this.cmbPaymentCondition.setHeight(null);
		this.formItem7.add(this.lblPaymentCondition, this.cmbPaymentCondition);
		this.lblOrdAmountBrut.setSizeUndefined();
		this.lblOrdAmountBrut.getElement().setAttribute("slot", "label");
		this.txtOrdAmountBrut.setWidthFull();
		this.txtOrdAmountBrut.setHeight(null);
		this.formItem8.add(this.lblOrdAmountBrut, this.txtOrdAmountBrut);
		this.lblOrdAmountNet.setSizeUndefined();
		this.lblOrdAmountNet.getElement().setAttribute("slot", "label");
		this.txtOrdAmountNet.setWidthFull();
		this.txtOrdAmountNet.setHeight(null);
		this.formItem9.add(this.lblOrdAmountNet, this.txtOrdAmountNet);
		this.lblOrdAmountVat.setSizeUndefined();
		this.lblOrdAmountVat.getElement().setAttribute("slot", "label");
		this.txtOrdAmountVat.setWidthFull();
		this.txtOrdAmountVat.setHeight(null);
		this.formItem10.add(this.lblOrdAmountVat, this.txtOrdAmountVat);
		this.gridLayoutHdr.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6, this.formItem7, this.formItem8, this.formItem9, this.formItem10);
		this.lblOrdCreated.setSizeUndefined();
		this.lblOrdCreated.getElement().setAttribute("slot", "label");
		this.dateOrdCreated.setWidthFull();
		this.dateOrdCreated.setHeight(null);
		this.formItem20.add(this.lblOrdCreated, this.dateOrdCreated);
		this.lblOrdPayDate.setSizeUndefined();
		this.lblOrdPayDate.getElement().setAttribute("slot", "label");
		this.dateOrdPayDate.setWidthFull();
		this.dateOrdPayDate.setHeight(null);
		this.formItem21.add(this.lblOrdPayDate, this.dateOrdPayDate);
		this.lblOrdDueDate.setSizeUndefined();
		this.lblOrdDueDate.getElement().setAttribute("slot", "label");
		this.dateOrdDueDate.setWidthFull();
		this.dateOrdDueDate.setHeight(null);
		this.formItem22.add(this.lblOrdDueDate, this.dateOrdDueDate);
		this.lblOrdBookedOn.setSizeUndefined();
		this.lblOrdBookedOn.getElement().setAttribute("slot", "label");
		this.dateOrdBookedOn.setWidthFull();
		this.dateOrdBookedOn.setHeight(null);
		this.formItem23.add(this.lblOrdBookedOn, this.dateOrdBookedOn);
		this.lblOrdState.setSizeUndefined();
		this.lblOrdState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem24.add(this.lblOrdState, this.comboBoxState);
		this.gridLayoutDetail.add(this.formItem20, this.formItem21, this.formItem22, this.formItem23, this.formItem24);
		this.gridLayoutHdr.setSizeFull();
		this.gridLayoutDetail.setSizeFull();
		this.verticalLayout3.add(this.gridLayoutHdr, this.gridLayoutDetail);
		this.cmdNewLine.setSizeUndefined();
		this.cmdDeleteLine.setSizeUndefined();
		this.cmdReloadLine.setSizeUndefined();
		this.cmdInfoLine.setSizeUndefined();
		this.cmdEditLine.setSizeUndefined();
		this.horizontalLayout.add(this.cmdNewLine, this.cmdDeleteLine, this.cmdReloadLine, this.cmdInfoLine,
			this.cmdEditLine);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight(null);
		this.grid2.setSizeFull();
		this.verticalLayout4.add(this.horizontalLayout, this.grid2);
		this.verticalLayout4.setFlexGrow(1.0, this.grid2);
		this.splitLayout2.addToPrimary(this.verticalLayout3);
		this.splitLayout2.addToSecondary(this.verticalLayout4);
		this.splitLayout2.setSplitterPosition(50.0);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.tabs.setWidthFull();
		this.tabs.setHeight("36px");
		this.splitLayout2.setSizeFull();
		this.horizontalLayout3.setWidth(null);
		this.horizontalLayout3.setHeight("100px");
		this.verticalLayout2.add(this.tabs, this.splitLayout2, this.horizontalLayout3);
		this.verticalLayout2.setFlexGrow(1.0, this.splitLayout2);
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
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.cmdCopy.addClickListener(this::cmdCopy_onClick);
		this.cmdReport.addClickListener(this::cmdReport_onClick);
		this.cmdPdfReport.addClickListener(this::cmdPdfReport_onClick);
		this.cmdAdmin.addClickListener(this::cmdAdmin_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.cmbCustomer.addValueChangeListener(this::cmbCustomer_valueChanged);
		this.btnSearch.addClickListener(this::btnSearch_onClick);
		this.cmbPaymentCondition.addValueChangeListener(this::cmbPaymentCondition_valueChanged);
		this.cmdNewLine.addClickListener(this::cmdNewLine_onClick);
		this.cmdDeleteLine.addClickListener(this::cmdDeleteLine_onClick);
		this.cmdReloadLine.addClickListener(this::cmdReloadLine_onClick);
		this.cmdInfoLine.addClickListener(this::cmdInfoLine_onClick);
		this.cmdEditLine.addClickListener(this::cmdEditLine_onClick);
		this.grid2.addItemClickListener(this::grid2_onItemClick);
		this.grid2.addItemDoubleClickListener(this::grid2_onItemDoubleClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Tab                         gridLayoutHdrTab, gridLayoutDetailsTab;
	private TextArea                    textArea;
	private ComboBox<Customer>          cmbCustomer;
	private VerticalLayout              verticalLayout, verticalLayout2, verticalLayout3, verticalLayout4;
	private HorizontalLayout            horizontalLayout2, horizontalLayout, horizontalLayout3;
	private Label                       lblOrdNumber, lblCustomer, lblOrdBillDate, lblOrdOrderDate, lblOrdText,
		lblProject,
		lblPaymentCondition, lblOrdAmountBrut, lblOrdAmountNet, lblOrdAmountVat, lblOrdCreated, lblOrdPayDate,
		lblOrdDueDate, lblOrdBookedOn, lblOrdState;
	private Tabs                        tabs;
	private FilterComponent             containerFilterComponent;
	private Grid<OrderLine>             grid2;
	private FormItem                    formItem2, formItem, formItem3, formItem4, formItem5, formItem6, formItem7,
		formItem8, formItem9, formItem10, formItem20, formItem21, formItem22, formItem23, formItem24;
	private Grid<Order>                 grid;
	private FormLayout                  gridLayoutHdr, gridLayoutDetail;
	private Button                      cmdNew, cmdDelete, cmdReload, cmdInfo, cmdCopy, cmdReport, cmdPdfReport,
		cmdAdmin,
		btnSearch, cmdNewLine, cmdDeleteLine, cmdReloadLine, cmdInfoLine, cmdEditLine, cmdSave, cmdReset;
	private ComboBox<State>             comboBoxState;
	private SplitLayout                 splitLayout, splitLayout2;
	private DatePicker                  dateOrdBillDate, dateOrdOrderDate, dateOrdCreated, dateOrdPayDate,
		dateOrdDueDate,
		dateOrdBookedOn;
	private ComboBox<Project>           cmbProject;
	private BeanValidationBinder<Order> binder;
	private TextField                   txtOrdNumber, txtOrdAmountBrut, txtOrdAmountNet, txtOrdAmountVat;
	private ComboBox<PaymentCondition>  cmbPaymentCondition;
	// </generated-code>
	
}
