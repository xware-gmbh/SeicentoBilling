
package ch.xwr.seicentobilling.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;
import org.jfree.util.Log;

import com.flowingcode.vaadin.addons.ironicons.FileIcons;
import com.flowingcode.vaadin.addons.ironicons.ImageIcons;
import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.flowingcode.vaadin.addons.ironicons.MapsIcons;
import com.flowingcode.vaadin.addons.twincolgrid.TwinColGrid;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.data.renderer.RenderedComponent;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovCrm.BillReport;
import ch.xwr.seicentobilling.business.LovCrm.BillTarget;
import ch.xwr.seicentobilling.business.LovCrm.Salutation;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.AccountType;
import ch.xwr.seicentobilling.business.LovState.State;
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
import ch.xwr.seicentobilling.dal.LabelDefinitionDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.ContactRelation;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.LabelDefinition;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.PaymentCondition;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.ui.crm.ActivityPopup;
import ch.xwr.seicentobilling.ui.crm.AddressPopup;
import ch.xwr.seicentobilling.ui.crm.ContactRelationPopup;
import ch.xwr.seicentobilling.ui.crm.CustomerLinkPopup;
import ch.xwr.seicentobilling.ui.crm.FunctionAddressHyperlink;
import ch.xwr.seicentobilling.ui.crm.FunctionLinkHyperlink;
import ch.xwr.seicentobilling.ui.crm.VcardPopup;


@Route("customer")
public class CustomerTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(CustomerTabView.class);
	
	Map<Long, LabelDefinition> ldMap;
	List<LabelDefinition>      labelList;
	
	/**
	 *
	 */
	public CustomerTabView()
	{
		super();
		this.initUI();
		
		// Type
		this.cbxState.setItems(LovState.State.values());
		this.cbxAccountType.setItems(LovState.AccountType.values());
		this.cbxAccountSalutation.setItems(LovCrm.Salutation.values());
		this.cbxAccountBillingReports.setItems(LovCrm.BillReport.values());
		this.cbxAccountBillingType.setItems(LovCrm.BillTarget.values());

		this.gridLayoutAddressSplitLayout.setVisible(false);
		this.gridLayoutFlags.setVisible(false);
		this.gridLayoutListActivityVertical.setVisible(false);
		this.gridLayoutRelationVertical.setVisible(false);
		this.gridLayoutListRef.setVisible(false);
		this.labelList = new LabelDefinitionDAO().findAll();
		this.twinColSelect.addColumn(LabelDefinition::getCldText, "Label")
			.withLeftColumnCaption("available")
			.withRightColumnCaption("aktiv").withoutRemoveAllButton().withoutAddAllButton().withSizeFull()
			.selectRowOnClick();
		this.twinColSelect.setItems(new HashSet<>(this.labelList));
		
		this.ldMap = this.labelList.stream().collect(
			Collectors.toMap(LabelDefinition::getCldId, x -> x));
		
		this.binder.bind(this.twinColSelect, Customer::getLabelDefinitions, Customer::setLabelDefinitions);

		// this.verticalSplitPanelAddress.setVisible(false);
		// this.gridLayoutDesc.setVisible(false);

		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.gridLayoutContactTab, this.gridLayoutContact);
		tabsToPages.put(this.gridLayoutFlagsTab, this.gridLayoutFlags);
		tabsToPages.put(this.gridLayoutAddressTab, this.gridLayoutAddressSplitLayout);
		tabsToPages.put(this.gridLayoutListActivityTab, this.gridLayoutListActivityVertical);
		tabsToPages.put(this.gridLayoutRelationTab, this.gridLayoutRelationVertical);
		tabsToPages.put(this.gridLayoutListRefTab, this.gridLayoutListRef);

		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});

		this.setROFields();

		this.setDefaultFilter();
		if(Seicento.hasRole("BillingAdmin"))
		{
			this.cmdImport.setEnabled(true);
			this.cmdImport.setVisible(true);
			
			this.txtExtRef1.setEnabled(true);
			this.txtExtRef2.setEnabled(true);
		}
		
		// final Anchor anchorNew =
		// new Anchor("");
		// anchorNew.setTitle("Link to Google Maps");
		// final Button linkMaps = new Button();
		// linkMaps.setIcon(new Image("images/gmaps32.PNG", ""));
		// linkMaps.addClassName("pointer");
		// anchorNew.add(linkMaps);
		// this.replace(this.linkMaps, anchorNew);
		// this.linkMaps = anchorNew;
	}

	private void setROFields()
	{
		this.txtCusNumber.setEnabled(false);
		boolean hasData = true;
		if(this.binder.getBean() == null)
		{
			hasData = false;
		}
		
		this.setROComponents(hasData);
		
		// this.dateProLastBill.setEnabled(false);
		// this.txtProHoursEffective.setEnabled(false);
	}
	
	private void setROComponents(final boolean state)
	{
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		// this.cmdVcard.setEnabled(state);
		this.tabs.setEnabled(state);
		this.gridLayoutContact.setEnabled(state);

		if(Seicento.hasRole("BillingAdmin") && state)
		{
			this.txtExtRef1.setEnabled(true);
			this.txtExtRef2.setEnabled(true);
		}
		else
		{
			this.txtExtRef1.setEnabled(false);
			this.txtExtRef2.setEnabled(false);
		}

	}
	
	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final Customer bean = this.binder.getBean();
		if(bean.getCusId() == null || bean.getCusId() < 1)
		{
			return true;
		}
		return false;
	}
	
	private void setDefaultFilter()
	{
		
		final FilterEntry cs =
			new FilterEntry("cusState", new FilterOperator.Is().key(), new LovState.State[]{LovState.State.active});

		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{cs}));

	}
	
	private void checkCustomerNumber(final boolean isNew, final boolean commitNbr)
	{
		if(!isNew)
		{
			return;
		}

		Integer nbr = null;
		try
		{
			nbr = Integer.parseInt(this.txtCusNumber.getValue());
		}
		catch(final Exception e)
		{
			nbr = new Integer(0);
		}

		final NumberRangeHandler handler = new NumberRangeHandler();
		if(!commitNbr)
		{
			if(nbr > 0)
			{
				return;
			}
			this.txtCusNumber.setValue(handler.getNewCustomerNumber(false, nbr).toString());
		}
		else
		{
			handler.getNewCustomerNumber(true, nbr);
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
		
		if(this.table.getSelectedItems() != null)
		{
			final Customer bean = this.table.getSelectionModel().getFirstSelectedItem().get();
			final Dialog   win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getCusId(), bean.getClass().getSimpleName()));
			win.open();
		}
		
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
		
		this.table.setDataProvider(DataProvider.ofCollection(new CustomerDAO().findAllByNumberDesc()));

		this.containerFilterComponent.setValue(fd);
		final Customer bean = this.binder.getBean();
		if(bean != null)
		{
			this.table.getSelectionModel().select(bean);
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onClick(final ClickEvent<Button> event)
	{
		this.table.select(null);

		final PaymentConditionDAO dao  = new PaymentConditionDAO();
		final Customer            bean = new Customer();
		bean.setCusState(LovState.State.active);
		bean.setPaymentCondition(dao.find((long)1));
		bean.setCusBillingTarget(LovCrm.BillTarget.pdf);
		bean.setCusBillingReport(LovCrm.BillReport.working);
		bean.setCusAccountType(AccountType.juristisch);
		
		this.binder.setBean(bean);
		this.checkCustomerNumber(true, false);
		this.setROFields();

		this.txtZip.setValue("");

		// this.fieldGroup.setItemDataSource(bean);
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDelete}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_onClick(final ClickEvent<Button> event)
	{
		if(this.table.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final Customer bean = this.table.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCusId(), bean.getClass().getSimpleName());

				final CustomerDAO dao = new CustomerDAO();
				dao.remove(bean);
				dao.flush();

				this.binder.removeBean();
				CustomerTabView.this.binder.setBean(new Customer());
				this.table.setDataProvider(DataProvider.ofCollection(new CustomerDAO().findAllByNumberDesc()));
				CustomerTabView.this.table.getDataProvider().refreshAll();
				
				this.setROComponents(false);

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
				CustomerTabView.LOG.error("Error on delete", e);
			}
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
		if(!this.table.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle", 5000,
				Notification.Position.BOTTOM_END);
			return;
		}

		final Customer bean = this.table.getSelectionModel().getFirstSelectedItem().get();

		final JasperManager jsp = new JasperManager();
		jsp.addParameter("CustomerId", "" + bean.getCusId());

		UI.getCurrent().getPage().open(jsp.getUri(JasperManager.ContactDetails1), "_blank");
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #table}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_onItemClick(final ItemClickEvent<Customer> event)
	{
		if(this.table.getSelectedItems() != null)
		
		{
			
			final CustomerDAO CustomertDao = new CustomerDAO();

			final List<LabelDefinition> list = new ArrayList<>(this.labelList);

			final Customer bean =
				
				CustomertDao.find(this.table.getSelectionModel().getFirstSelectedItem().get().getCusId());
			for(final LabelDefinition ld : bean.getLabelDefinitions())
			{
				list.remove(this.ldMap.get(ld.getCldId()));
			}
			this.twinColSelect.setItems(list);
			this.binder.setBean(bean);
			
			// this.twinColSelect.setValue(bean.getLabelDefinitions());
			
			this.displayChildTables();
			
			this.setGoogleLink(bean);

			this.setROFields();
			
		}

	}

	private void displayChildTables()
	{
		final Customer bean = this.binder.getBean();

		// Rechnungen
		
		this.tableOrder.setItems(new OrderDAO().findByCustomer(bean));
		
		// Projekte
		this.tableProject.setItems(bean.getProjects());
		
		// Aktivitäten
		this.tableActivity.setItems(new ActivityDAO().findByCustomer(bean));
		this.containerFilterComponent2.connectWith(this.tableActivity.getDataProvider());
		
		// Adresen
		this.tableAddress.setItems(bean.getAddresses());
		
		// CustomerLinks (Email + Phone)
		this.tableLink.setItems(bean.getCustomerLinks());
		
		// Relations
		this.reloadRelationList();
		
	}
	
	private void setGoogleLink(final Customer bean)
	{
		final String uripre = "https://www.google.com/maps/search/?api=1&query=";
		String       q      = bean.getCity().getCtyName();
		if(bean.getCusAddress() != null && !bean.getCusAddress().trim().isEmpty())
		{
			q = q + ", " + bean.getCusAddress();
		}
		if(bean.getCusCompany() != null && !bean.getCusCompany().trim().isEmpty())
		{
			q = q + ", " + bean.getCusCompany().trim();
		}
		
		try
		{
			q = URLEncoder.encode(q, "UTF-8");
			this.linkMaps.setHref(uripre + q);
			this.linkMaps.setTarget("_blank");
		}
		catch(final UnsupportedEncodingException e)
		{
			Log.error(e);
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		if(!this.AreFieldsValid())
		{
			return;
		}
		final boolean isNew = this.isNew(); // assign before save. is always false after save
		if(SeicentoCrud.doSave(this.binder, new CustomerDAO()))
		{
			try
			{
				this.setROFields();
				this.checkCustomerNumber(isNew, true);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getCusId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				CustomerTabView.LOG.error("could not save ObjRoot", e);
			}
		}

		this.cmdReload_onClick(event);

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
			final CustomerDAO dao      = new CustomerDAO();
			final Customer    cityBean = dao.find(this.binder.getBean().getCusId());
			this.binder.setBean(cityBean);
		}
		this.setROFields();
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewActivity}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewActivity_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.getCurrentRecord();
		if(objId < 0)
		{
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupActivity();
	}

	private void popupActivity()
	{
		
		final Dialog win = ActivityPopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				CustomerTabView.this.reloadActivityList();
				
			}
		});
		win.open();
		
	}

	private void reloadActivityList()
	{
		Customer bean = null;
		if(this.table.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			bean = this.table.getSelectionModel().getFirstSelectedItem().get();
		}
		
		if(bean != null)
		{
			this.tableActivity.setDataProvider(DataProvider.ofCollection(new ActivityDAO().findByCustomer(bean)));
		}
		
	}
	
	private boolean AreFieldsValid()
	{
		if(this.binder.isValid())
		{
			return true;
		}
		this.binder.validate();
		
		return false;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdEditActivity}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditActivity_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableActivity.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.tableActivity.getSelectionModel().getFirstSelectedItem().get().getactId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupActivity();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteActivity}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteActivity_onClick(final ClickEvent<Button> event)
	{
		if(this.table.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final Activity bean = this.tableActivity.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getactId(), bean.getClass().getSimpleName());

				final ActivityDAO dao = new ActivityDAO();
				dao.remove(bean);
				dao.flush();
				
				this.reloadActivityList();

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
				CustomerTabView.LOG.error("Error on delete", e);
			}
		});
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReloadActivity}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadActivity_onClick(final ClickEvent<Button> event)
	{
		this.reloadActivityList();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfoActivity}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoActivity_onClick(final ClickEvent<Button> event)
	{
		if(this.tableActivity.getSelectedItems() != null)
		{
			final Activity bean = this.tableActivity.getSelectionModel().getFirstSelectedItem().get();
			final Dialog   win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getactId(), bean.getClass().getSimpleName()));
			win.open();
		}
	}
	
	private void popupAddress()
	{

		final Dialog win = AddressPopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				CustomerTabView.this.reloadAddressList();
				
			}
		});
		win.open();
		
	}

	private void reloadAddressList()
	{
		Customer bean = null;
		if(this.table.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			bean = this.table.getSelectionModel().getFirstSelectedItem().get();
		}
		
		if(bean != null)
		{
			
			this.tableAddress.setDataProvider(DataProvider.ofCollection(new AddressDAO().findByCustomer(bean)));
		}
		
	}

	private void popupCustomerLink()
	{

		final Dialog win = CustomerLinkPopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				CustomerTabView.this.reloadCustomerLinkList();
				
			}
		});
		win.open();

	}

	private void reloadCustomerLinkList()
	{
		Customer bean = null;
		if(this.table.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			bean = this.table.getSelectionModel().getFirstSelectedItem().get();
		}

		if(bean != null)

		{
			this.tableLink.setDataProvider(DataProvider.ofCollection(new CustomerLinkDAO().findByCustomer(bean)));
		}

	}
	
	private void reloadRelationList()
	{
		
		Customer bean = null;
		if(this.table.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			bean = this.table.getSelectionModel().getFirstSelectedItem().get();
		}

		if(bean != null)

		{
			this.tableRelation
				.setDataProvider(DataProvider.ofCollection(new ContactRelationDAO().findByCustomer(bean)));
		}

	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableActivity}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableActivity_onItemDoubleClick(final ItemDoubleClickEvent<Activity> event)
	{
		// Notification.show("Event Triggered ",
		// Notification.Type.TRAY_NOTIFICATION);
		final Activity obj = event.getItem();
		this.tableActivity.select(obj); // reselect after double-click
		
		final Long beanId = obj.getactId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupActivity();
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewAddress_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.getCurrentRecord();
		if(objId < 0)
		{
			return;
		}
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupAddress();
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewCustomerLink_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.getCurrentRecord();
		if(objId < 0)
		{
			return;
		}
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupCustomerLink();
	}
	
	private Long getCurrentRecord()
	{
		if(this.binder.getBean() != null)
		{
			return this.binder.getBean().getCusId();
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
	private void cmdDeleteAddress_onClick(final ClickEvent<Button> event)
	{

		if(this.tableAddress.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final Address bean = this.tableAddress.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getAdrId(), bean.getClass().getSimpleName());

				final AddressDAO dao = new AddressDAO();
				dao.remove(bean);
				dao.flush();

				this.reloadAddressList();

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
				CustomerTabView.LOG.error("Error on delete", e);
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
	private void cmdDeleteCustomerLink_onClick(final ClickEvent<Button> event)
	{

		if(this.tableLink.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final CustomerLink bean = this.tableLink.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCnkId(), bean.getClass().getSimpleName());

				final CustomerLinkDAO dao = new CustomerLinkDAO();
				dao.remove(bean);
				dao.flush();

				this.reloadCustomerLinkList();

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
				CustomerTabView.LOG.error("Error on delete", e);
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
	private void cmdEditAddress_onClick(final ClickEvent<Button> event)
	{
		if(this.tableAddress.getSelectedItems() == null)
		{
			return;
		}
		
		final Long beanId = this.tableAddress.getSelectionModel().getFirstSelectedItem().get().getAdrId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupAddress();
		
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditCustomerLink_onClick(final ClickEvent<Button> event)
	{
		if(this.tableLink.getSelectedItems() == null)
		{
			return;
		}
		
		final Long beanId = this.tableLink.getSelectionModel().getFirstSelectedItem().get().getCnkId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupCustomerLink();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadAddress_onClick(final ClickEvent<Button> event)
	{
		this.reloadAddressList();
		
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReloadCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReloadCustomerLink_onClick(final ClickEvent<Button> event)
	{
		this.reloadCustomerLinkList();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoCustomerLink}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoCustomerLink_onClick(final ClickEvent<Button> event)
	{
		
		if(this.tableLink.getSelectedItems() != null)
		{
			final CustomerLink bean = this.tableLink.getSelectionModel().getFirstSelectedItem().get();
			final Dialog       win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getCnkId(), bean.getClass().getSimpleName()));
			win.open();
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoAddress}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoAddress_onClick(final ClickEvent<Button> event)
	{
		if(this.tableAddress.getSelectedItems() != null)
		{
			final Address bean = this.tableAddress.getSelectionModel().getFirstSelectedItem().get();
			final Dialog  win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getAdrId(), bean.getClass().getSimpleName()));
			win.open();
		}

	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableAddress}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableAddress_onItemDoubleClick(final ItemDoubleClickEvent<Address> event)
	{
		// Notification.show("Event Triggered ",
		// Notification.Type.TRAY_NOTIFICATION);
		final Address obj = event.getItem();
		this.tableAddress.select(obj); // reselect after double-click
		
		final Long beanId = obj.getAdrId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupAddress();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #tableLink}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLink_onItemDoubleClick(final ItemDoubleClickEvent<CustomerLink> event)
	{

		final CustomerLink obj = event.getItem();
		this.tableLink.select(obj); // reselect after double-click
		
		final Long beanId = obj.getCnkId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupCustomerLink();
		
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteRelation}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteRelation_onClick(final ClickEvent<Button> event)
	{

		if(this.tableRelation.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				final ContactRelation bean = this.tableRelation.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCorId(), bean.getClass().getSimpleName());

				final ContactRelationDAO dao = new ContactRelationDAO();
				dao.remove(bean);
				dao.flush();
				
				this.reloadRelationList();

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
				CustomerTabView.LOG.error("Error on delete", e);
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
	private void cmdNewRelation_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.getCurrentRecord();
		if(objId < 0)
		{
			return;
		}

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupContactRelation();

	}
	
	private void popupContactRelation()
	{

		final Dialog win = ContactRelationPopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				CustomerTabView.this.reloadRelationList();
				
			}
		});
		win.open();
		
	}
	
	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdImport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdImport_onClick(final ClickEvent<Button> event)
	{
		// final Dialog win = ImportContactsPopup.getPopupWindow();
		//
		// win.addDetachListener(new ComponentEventListener<DetachEvent>()
		// {
		//
		// @Override
		// public void onComponentEvent(final DetachEvent event)
		// {
		// CustomerTabView.this.reloadCustomerLinkList();
		//
		// }
		// });
		// win.open();

	}
	
	/**
	 * Event handler delegate method for the {@link TextField} {@link #txtZip}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	@SuppressWarnings("unchecked")
	private void txtZip_valueChanged(final ComponentValueChangeEvent<TextField, String> event)
	{
		// System.out.println("value change");
		final String val = event.getValue();
		if(val != null && val.length() > 3)
		{
			City bean = null;
			if(this.cmbCity.getValue() != null)
			{
				bean = this.cmbCity.getValue();
			}
			if(bean == null || bean.getCtyZip().intValue() != Integer.parseInt(val))
			{
				final CityDAO    dao = new CityDAO();
				final List<City> ls  = dao.findByZip(Integer.parseInt(val));
				if(ls != null && ls.size() > 0)
				{
					final City b2 = ls.get(0);

					final ListDataProvider<City> dataProvider = (ListDataProvider<City>)this.cmbCity.getDataProvider();
					final Collection<City>       xx           = dataProvider.getItems();

					for(final Iterator<?> iterator = xx.iterator(); iterator.hasNext();)
					{
						final City object = (City)iterator.next();
						if(object.getCtyId().equals(b2.getCtyId()))
						{
							this.cmbCity.setValue(object);
						}
					}
				}
				else
				{
					this.cmbCity.clear();
				}
			}

		}
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbCity}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbCity_valueChanged(final ComponentValueChangeEvent<ComboBox<City>, City> event)
	{
		final City cty = event.getValue();
		if(cty != null)
		{
			final String zip = this.txtZip.getValue();
			if(zip == null || !zip.equals(cty.getCtyZip().toString()))
			{
				this.txtZip.setValue("" + cty.getCtyZip());
			}
			this.lblCountry.setText(cty.getCtyCountry());
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdVcard}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdVcard_onClick(final ClickEvent<Button> event)
	{
		if(this.binder.getBean() == null)
		{
			return;
		}

		UI.getCurrent().getSession().setAttribute("cusbeanId", this.binder.getBean().getCusId());
		this.popupVcard();
	}

	private void popupVcard()
	{
		final Dialog win = VcardPopup.getPopupWindow();
		
		win.open();
		
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout                    = new SplitLayout();
		this.verticalLayout                 = new VerticalLayout();
		this.containerFilterComponent       = new FilterComponent();
		this.proHorizontalLayout            = new HorizontalLayout();
		this.cmdNew                         = new Button();
		this.cmdDelete                      = new Button();
		this.cmdReload                      = new Button();
		this.cmdReport                      = new Button();
		this.cmdInfo                        = new Button();
		this.cmdImport                      = new Button();
		this.table                          = new Grid<>(Customer.class, false);
		this.mainLayout                     = new VerticalLayout();
		this.tabs                           = new Tabs();
		this.gridLayoutContactTab           = new Tab();
		this.gridLayoutFlagsTab             = new Tab();
		this.gridLayoutAddressTab           = new Tab();
		this.gridLayoutListActivityTab      = new Tab();
		this.gridLayoutRelationTab          = new Tab();
		this.gridLayoutListRefTab           = new Tab();
		this.gridLayoutContact              = new FormLayout();
		this.formItem2                      = new FormItem();
		this.txtCusNumber                   = new TextField();
		this.formItem3                      = new FormItem();
		this.cbxAccountType                 = new ComboBox<>();
		this.formItem41                     = new FormItem();
		this.cbxAccountSalutation           = new ComboBox<>();
		this.formItem5                      = new FormItem();
		this.txtCusCompany                  = new TextField();
		this.formItem6                      = new FormItem();
		this.txtCusName                     = new TextField();
		this.formItem7                      = new FormItem();
		this.txtCusFirstName                = new TextField();
		this.formItem101                    = new FormItem();
		this.ironIcon                       = MapsIcons.PLACE.create();
		this.linkMaps                       = new Anchor();
		this.txtCusAddress                  = new TextField();
		this.formItem11                     = new FormItem();
		this.txtZip                         = new TextField();
		this.cmbCity                        = new ComboBox<>();
		this.lblCountry                     = new Label();
		this.formItem12                     = new FormItem();
		this.datCusBirthdate                = new DatePicker();
		this.formItem13                     = new FormItem();
		this.cbxState                       = new ComboBox<>();
		this.gridLayoutFlags                = new FormLayout();
		this.formItem111                    = new FormItem();
		this.txtAccountManager              = new TextField();
		this.formItem                       = new FormItem();
		this.txtCusInfo                     = new TextField();
		this.formItem15                     = new FormItem();
		this.cmbPaymentCondition            = new ComboBox<>();
		this.formItem16                     = new FormItem();
		this.cbxAccountBillingType          = new ComboBox<>();
		this.formItem19                     = new FormItem();
		this.cbxAccountBillingReports       = new ComboBox<>();
		this.formItem8                      = new FormItem();
		this.cbxSinglePdf                   = new Checkbox();
		this.formItem31                     = new FormItem();
		this.txtExtRef1                     = new TextField();
		this.formItem4                      = new FormItem();
		this.txtExtRef2                     = new TextField();
		this.formItem21                     = new FormItem();
		this.twinColSelect                  = new TwinColGrid<>();
		this.gridLayoutAddressSplitLayout   = new SplitLayout();
		this.verticalLayoutBill             = new VerticalLayout();
		this.horizontalLayout4              = new HorizontalLayout();
		this.cmdNewAddress                  = new Button();
		this.cmdDeleteAddress               = new Button();
		this.cmdEditAddress                 = new Button();
		this.cmdReloadAddress               = new Button();
		this.cmdInfoAddress                 = new Button();
		this.horizontalLayout2              = new HorizontalLayout();
		this.labelAdressen                  = new Label();
		this.tableAddress                   = new Grid<>(Address.class, false);
		this.verticalLayoutSubProject       = new VerticalLayout();
		this.horizontalLayout5              = new HorizontalLayout();
		this.cmdNewCustomerLink             = new Button();
		this.cmdDeleteCustomerLink          = new Button();
		this.cmdEditCustomerLink            = new Button();
		this.cmdReloadCustomerLink          = new Button();
		this.cmdInfoCustomerLink            = new Button();
		this.horizontalLayout               = new HorizontalLayout();
		this.subprojectLabel                = new Label();
		this.tableLink                      = new Grid<>(CustomerLink.class, false);
		this.gridLayoutListActivityVertical = new VerticalLayout();
		this.horizontalLayoutActivityAction = new HorizontalLayout();
		this.cmdNewActivity                 = new Button();
		this.cmdDeleteActivity              = new Button();
		this.cmdEditActivity                = new Button();
		this.cmdReloadActivity              = new Button();
		this.cmdInfoActivity                = new Button();
		this.horizontalLayoutActivity2      = new HorizontalLayout();
		this.containerFilterComponent2      = new FilterComponent();
		this.horizontalLayoutActivity       = new HorizontalLayout();
		this.labelActivity                  = new Label();
		this.tableActivity                  = new Grid<>(Activity.class, false);
		this.gridLayoutRelationVertical     = new VerticalLayout();
		this.horizontalLayoutRelationAction = new HorizontalLayout();
		this.cmdNewRelation                 = new Button();
		this.cmdDeleteRelation              = new Button();
		this.horizontalLayoutRelation       = new HorizontalLayout();
		this.iconBeziehungen                = new Icon(VaadinIcon.EXCHANGE);
		this.labelRelation                  = new Label();
		this.tableRelation                  = new Grid<>(ContactRelation.class, false);
		this.gridLayoutListRef              = new SplitLayout();
		this.verticalLayoutReference        = new VerticalLayout();
		this.horizontalLayoutReference      = new HorizontalLayout();
		this.icon                           = new Icon(VaadinIcon.FILE);
		this.labelReference                 = new Label();
		this.tableOrder                     = new Grid<>(Order.class, false);
		this.verticalLayoutProject          = new VerticalLayout();
		this.horizontalLayoutProjekte       = new HorizontalLayout();
		this.projekteLabel                  = new Label();
		this.tableProject                   = new Grid<>(Project.class, false);
		this.horizontalLayout3              = new HorizontalLayout();
		this.cmdSave                        = new Button();
		this.cmdReset                       = new Button();
		this.cmdVcard                       = new Button();
		this.binder                         = new BeanValidationBinder<>(Customer.class);

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.proHorizontalLayout.setMinHeight("");
		this.proHorizontalLayout.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdReport.setIcon(IronIcons.PRINT.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.cmdImport.setIcon(FileIcons.EXCEL.create());
		this.table.addColumn(Customer::getCusNumber).setKey("cusNumber").setHeader("Nummer").setSortable(true);
		this.table.addColumn(Customer::getCusCompany).setKey("cusCompany").setHeader("Firma").setSortable(true)
			.setVisible(false);
		this.table.addColumn(Customer::getShortname).setKey("shortname").setHeader("Name").setSortable(true);
		this.table.addColumn(Customer::getCusName).setKey("cusName").setHeader("Name").setSortable(true)
			.setVisible(false);
		this.table.addColumn(Customer::getCusFirstName).setKey("cusFirstName").setHeader("Vorname").setSortable(true)
			.setVisible(false);
		this.table.addColumn(Customer::getCusAddress).setKey("cusAddress").setHeader("Adresse").setSortable(true);
		this.table.addColumn(v -> Optional.ofNullable(v).map(Customer::getCity).map(City::getCtyZip).orElse(null))
			.setKey("city.ctyZip").setHeader("PLZ").setSortable(true);
		this.table.addColumn(v -> Optional.ofNullable(v).map(Customer::getCity).map(City::getCtyName).orElse(null))
			.setKey("city.ctyName").setHeader("Ort").setSortable(true);
		this.table.addColumn(
			v -> Optional.ofNullable(v).map(Customer::getPaymentCondition).map(PaymentCondition::getPacName)
				.orElse(null))
			.setKey("paymentCondition.pacName").setHeader("Zahlungsbedingung").setSortable(true);
		this.table.addColumn(new CaptionRenderer<>(Customer::getCusState)).setKey("cusState").setHeader("Status")
			.setSortable(true);
		this.table.setDataProvider(DataProvider.ofCollection(new CustomerDAO().findAllByNumberDesc()));
		this.table.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.mainLayout.setMinHeight("100%");
		this.mainLayout.setSpacing(false);
		this.mainLayout.setPadding(false);
		this.tabs.setMinHeight("50px");
		this.gridLayoutContactTab.setLabel("Kontakt");
		this.gridLayoutFlagsTab.setLabel("Flags");
		this.gridLayoutAddressTab.setLabel("Adressen");
		this.gridLayoutListActivityTab.setLabel("Aktivitäten");
		this.gridLayoutRelationTab.setLabel("Beziehungen");
		this.gridLayoutListRefTab.setLabel("Referenzen");
		this.gridLayoutContact.getStyle().set("overflow-x", "hidden");
		this.gridLayoutContact.getStyle().set("overflow-y", "auto");
		this.gridLayoutContact.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.txtCusNumber.setLabel(StringResourceUtils.optLocalizeString("{$lblCusNumber.value}", this));
		this.cbxAccountType.setLabel("Kontakt Typ");
		this.cbxAccountType.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cbxAccountSalutation.setLabel("Anrede");
		this.cbxAccountSalutation
			.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.txtCusCompany.setLabel(StringResourceUtils.optLocalizeString("{$lblCusCompany.value}", this));
		this.txtCusName.setLabel(StringResourceUtils.optLocalizeString("{$lblCusName.value}", this));
		this.txtCusFirstName.setLabel(StringResourceUtils.optLocalizeString("{$lblCusFirstName.value}", this));
		this.formItem101.getElement().setAttribute("colspan", "2");
		this.linkMaps.setText("Adresse");
		this.txtCusAddress.setLabel("");
		this.formItem11.getElement().setAttribute("colspan", "2");
		this.txtZip.setLabel(StringResourceUtils.optLocalizeString("{$lblCity.value}", this));
		this.cmbCity.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(City::getCtyName));
		this.lblCountry.setText("CH");
		this.datCusBirthdate.setLabel("Geburtsdatum");
		this.cbxState.setLabel(StringResourceUtils.optLocalizeString("{$lblCusState.value}", this));
		this.cbxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.gridLayoutFlags.getStyle().set("overflow-x", "hidden");
		this.gridLayoutFlags.getStyle().set("overflow-y", "auto");
		this.gridLayoutFlags.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.txtAccountManager.setLabel("Account Manager");
		this.txtCusInfo.setLabel(StringResourceUtils.optLocalizeString("{$lblCusInfo.value}", this));
		this.cmbPaymentCondition.setLabel(StringResourceUtils.optLocalizeString("{$lblPaymentCondition.value}", this));
		this.cmbPaymentCondition.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbPaymentCondition::getItemLabelGenerator),
			DataProvider.ofCollection(new PaymentConditionDAO().findAllActive()));
		this.cmbPaymentCondition.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(PaymentCondition::getPacName));
		this.cbxAccountBillingType.setLabel("Rechnungstyp");
		this.cbxAccountBillingType
			.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cbxAccountBillingReports.setLabel("Rechnungsanhang");
		this.cbxAccountBillingReports
			.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cbxSinglePdf.setLabel("Single PDF");
		this.txtExtRef1.setLabel("Ext Ref 1");
		this.txtExtRef2.setLabel("Ext Ref 2");
		this.formItem21.getElement().setAttribute("colspan", "2");
		this.gridLayoutAddressSplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.verticalLayoutBill.setSpacing(false);
		this.verticalLayoutBill.setPadding(false);
		this.cmdNewAddress.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteAddress.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdEditAddress.setIcon(ImageIcons.EDIT.create());
		this.cmdReloadAddress.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfoAddress.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.horizontalLayout2.setSpacing(false);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.labelAdressen.setText("Adressen");
		this.tableAddress.addColumn(new CaptionRenderer<>(Address::getAdrType)).setKey("adrType").setHeader("Typ")
			.setSortable(true);
		this.tableAddress.addColumn(Address::getAdrName).setKey("adrName").setHeader("Name").setSortable(true);
		this.tableAddress.addColumn(Address::getAdrAddOn).setKey("adrAddOn").setHeader("Zusatz").setSortable(true)
			.setVisible(false);
		this.tableAddress.addColumn(Address::getAdrLine0).setKey("adrLine0").setHeader("Adresse").setSortable(true);
		this.tableAddress.addColumn(Address::getAdrZip).setKey("adrZip").setHeader("Plz").setSortable(true);
		this.tableAddress.addColumn(Address::getAdrCity).setKey("adrCity").setHeader("Ort").setSortable(true);
		this.tableAddress.addColumn(RenderedComponent.Renderer(FunctionAddressHyperlink::new)).setKey("renderer")
			.setHeader("Maps").setSortable(false);
		this.tableAddress.addColumn(Address::getAdrValidFrom).setKey("adrValidFrom").setHeader("Gültig ab")
			.setSortable(true);
		this.tableAddress.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayoutSubProject.setSpacing(false);
		this.verticalLayoutSubProject.setPadding(false);
		this.cmdNewCustomerLink.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteCustomerLink.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdEditCustomerLink.setIcon(ImageIcons.EDIT.create());
		this.cmdReloadCustomerLink.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfoCustomerLink.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.horizontalLayout.setSpacing(false);
		this.subprojectLabel.setText("Mail / Telefon / Links");
		this.tableLink.addColumn(new CaptionRenderer<>(CustomerLink::getCnkType)).setKey("cnkType").setHeader("Typ")
			.setSortable(true);
		this.tableLink.addColumn(CustomerLink::getCnkLink).setKey("cnkLink").setHeader("Wert").setSortable(true);
		this.tableLink.addColumn(RenderedComponent.Renderer(FunctionLinkHyperlink::new)).setKey("renderer")
			.setHeader("Link").setSortable(false);
		this.tableLink.addColumn(new CaptionRenderer<>(CustomerLink::getCnkDepartment)).setKey("cnkDepartment")
			.setHeader("Bereich").setSortable(true);
		this.tableLink.addColumn(CustomerLink::getCnkRemark).setKey("cnkRemark").setHeader("Bemerkung")
			.setSortable(true);
		this.tableLink.addColumn(CustomerLink::getCnkValidFrom).setKey("cnkValidFrom").setHeader("Gültig ab")
			.setSortable(true);
		this.tableLink.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.gridLayoutListActivityVertical.setSpacing(false);
		this.gridLayoutListActivityVertical.setPadding(false);
		this.cmdNewActivity.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteActivity.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdEditActivity.setIcon(ImageIcons.EDIT.create());
		this.cmdReloadActivity.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfoActivity.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.containerFilterComponent2.setMinWidth("");
		this.horizontalLayoutActivity.setSpacing(false);
		this.horizontalLayoutActivity.setAlignItems(FlexComponent.Alignment.CENTER);
		this.labelActivity.setText("Aktivititäten");
		this.tableActivity.addColumn(Activity::getActDate).setKey("actDate").setHeader("Datum").setSortable(true);
		this.tableActivity.addColumn(new CaptionRenderer<>(Activity::getActType)).setKey("actType").setHeader("Typ")
			.setSortable(true);
		this.tableActivity.addColumn(Activity::getActText).setKey("actText").setHeader("Bemerkung").setSortable(true);
		this.tableActivity.addColumn(RenderedComponent.Renderer(FunctionActHyperlink::new)).setKey("renderer")
			.setSortable(false);
		this.tableActivity.addColumn(new CaptionRenderer<>(Activity::getCostAccount)).setKey("costAccount")
			.setHeader("Wer")
			.setSortable(false);
		this.tableActivity.addColumn(Activity::getActFollowingUpDate).setKey("actFollowingUpDate")
			.setHeader("Folgetermin")
			.setSortable(true);
		this.tableActivity.setDataProvider(DataProvider.ofCollection(new ActivityDAO().findAll()));
		this.tableActivity.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.gridLayoutRelationVertical.setSpacing(false);
		this.gridLayoutRelationVertical.setPadding(false);
		this.cmdNewRelation.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteRelation.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.horizontalLayoutRelation.setSpacing(false);
		this.horizontalLayoutRelation.setAlignItems(FlexComponent.Alignment.CENTER);
		this.labelRelation.setText("Beziehungen");
		this.tableRelation
			.addColumn(
				v -> Optional.ofNullable(v).map(ContactRelation::getCustomerOne).map(Customer::getShortname)
					.orElse(null))
			.setKey("customerOne.shortname").setHeader("Kontakt 1").setSortable(true);
		this.tableRelation.addColumn(new CaptionRenderer<>(ContactRelation::getCorTypeOne)).setKey("corTypeOne")
			.setHeader("Typ").setSortable(true);
		this.tableRelation
			.addColumn(
				v -> Optional.ofNullable(v).map(ContactRelation::getCustomerTwo).map(Customer::getShortname)
					.orElse(null))
			.setKey("customerTwo.shortname").setHeader("Kontakt 2").setSortable(true);
		this.tableRelation.addColumn(new CaptionRenderer<>(ContactRelation::getCorTypeTwo)).setKey("corTypeTwo")
			.setHeader("Typ").setSortable(true);
		this.tableRelation.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.gridLayoutListRef.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.verticalLayoutReference.setSpacing(false);
		this.verticalLayoutReference.setPadding(false);
		this.horizontalLayoutReference.setSpacing(false);
		this.horizontalLayoutReference.setAlignItems(FlexComponent.Alignment.CENTER);
		this.labelReference.setText("Rechnungen");
		this.tableOrder.addColumn(Order::getOrdNumber).setKey("ordNumber").setHeader("Rechnungsnummer")
			.setSortable(true);
		this.tableOrder.addColumn(Order::getOrdBillDate).setKey("ordBillDate").setHeader("Rechnungsdatum")
			.setSortable(true);
		this.tableOrder.addColumn(Order::getOrdAmountNet).setKey("ordAmountNet").setHeader("Betrag Netto")
			.setSortable(true);
		this.tableOrder.addColumn(Order::getOrdPayDate).setKey("ordPayDate").setHeader("Bezahlt am").setSortable(true);
		this.tableOrder.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayoutProject.setSpacing(false);
		this.verticalLayoutProject.setPadding(false);
		this.horizontalLayoutProjekte.setSpacing(false);
		this.projekteLabel.setText("Projekte");
		this.tableProject.addColumn(Project::getProName).setKey("proName").setHeader("Projekt").setSortable(true);
		this.tableProject.addColumn(new CaptionRenderer<>(Project::getProState)).setKey("proState")
			.setHeader("Start Datum")
			.setSortable(true);
		this.tableProject.addColumn(Project::getProEndDate).setKey("proEndDate").setHeader("End Datum")
			.setSortable(true);
		this.tableProject.addColumn(Project::getProHours).setKey("proHours").setHeader("Stunden").setSortable(true);
		this.tableProject.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.cmdVcard.setText("Vcard...");

		this.binder.forField(this.txtCusNumber).asRequired().withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cusNumber");
		this.binder.forField(this.cbxAccountType).bind("cusAccountType");
		this.binder.forField(this.cbxAccountSalutation).bind("cusSalutation");
		this.binder.forField(this.txtCusCompany).withNullRepresentation("").bind("cusCompany");
		this.binder.forField(this.txtCusFirstName).asRequired().withNullRepresentation("").bind("cusFirstName");
		this.binder.forField(this.txtCusAddress).withNullRepresentation("").bind("cusAddress");
		this.binder.forField(this.cmbCity).asRequired().bind("city");
		this.binder.forField(this.datCusBirthdate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("cusBirthdate");
		this.binder.forField(this.cbxState).bind("cusState");
		this.binder.forField(this.txtCusInfo).withNullRepresentation("").bind("cusInfo");
		this.binder.forField(this.cmbPaymentCondition).asRequired().bind("paymentCondition");
		this.binder.forField(this.cbxAccountBillingType).bind("cusBillingTarget");
		this.binder.forField(this.cbxAccountBillingReports).bind("cusBillingReport");
		this.binder.forField(this.txtExtRef1).withNullRepresentation("").bind("cusExtRef1");
		this.binder.forField(this.txtExtRef2).withNullRepresentation("").bind("cusExtRef2");
		this.binder.forField(this.cbxSinglePdf).withNullRepresentation(false).bind("cusSinglepdf");
		this.binder.forField(this.txtCusName).withNullRepresentation("").bind("cusName");

		this.containerFilterComponent.connectWith(this.table.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.table,
			Arrays.asList("city.ctyName", "cusCompany", "cusFirstName", "cusName"),
			Arrays.asList("city.ctyName", "city.ctyZip", "cusAccountManager", "cusAccountType", "cusCompany",
				"cusFirstName", "cusName", "cusNumber", "cusState", "shortname")));
		this.containerFilterComponent2.connectWith(this.tableActivity.getDataProvider());
		this.containerFilterComponent2.setFilterSubject(
			GridFilterSubjectFactory.CreateFilterSubject(this.tableActivity, Arrays.asList("actText"),
				Arrays.asList()));

		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdReport.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.cmdImport.setSizeUndefined();
		this.proHorizontalLayout.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdReport, this.cmdInfo,
			this.cmdImport);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.proHorizontalLayout.setWidth("100px");
		this.proHorizontalLayout.setHeight("60px");
		this.table.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.proHorizontalLayout, this.table);
		this.verticalLayout.setFlexGrow(1.0, this.table);
		this.tabs.add(this.gridLayoutContactTab, this.gridLayoutFlagsTab, this.gridLayoutAddressTab,
			this.gridLayoutListActivityTab, this.gridLayoutRelationTab, this.gridLayoutListRefTab);
		this.txtCusNumber.setWidthFull();
		this.txtCusNumber.setHeight(null);
		this.formItem2.add(this.txtCusNumber);
		this.cbxAccountType.setWidthFull();
		this.cbxAccountType.setHeight(null);
		this.formItem3.add(this.cbxAccountType);
		this.cbxAccountSalutation.setWidthFull();
		this.cbxAccountSalutation.setHeight(null);
		this.formItem41.add(this.cbxAccountSalutation);
		this.txtCusCompany.setWidthFull();
		this.txtCusCompany.setHeight(null);
		this.formItem5.add(this.txtCusCompany);
		this.txtCusName.setWidthFull();
		this.txtCusName.setHeight(null);
		this.formItem6.add(this.txtCusName);
		this.txtCusFirstName.setWidthFull();
		this.txtCusFirstName.setHeight(null);
		this.formItem7.add(this.txtCusFirstName);
		this.linkMaps.setWidthFull();
		this.linkMaps.setHeight(null);
		this.txtCusAddress.setWidthFull();
		this.txtCusAddress.setHeight(null);
		this.formItem101.add(this.ironIcon, this.linkMaps, this.txtCusAddress);
		this.txtZip.setWidth("27%");
		this.txtZip.setHeight(null);
		this.cmbCity.setWidth("50%");
		this.cmbCity.setHeight(null);
		this.lblCountry.setWidth("140px");
		this.lblCountry.setHeightFull();
		this.formItem11.add(this.txtZip, this.cmbCity, this.lblCountry);
		this.datCusBirthdate.setWidthFull();
		this.datCusBirthdate.setHeight(null);
		this.formItem12.add(this.datCusBirthdate);
		this.cbxState.setWidthFull();
		this.cbxState.setHeight(null);
		this.formItem13.add(this.cbxState);
		this.gridLayoutContact.add(this.formItem2, this.formItem3, this.formItem41, this.formItem5, this.formItem6,
			this.formItem7, this.formItem101, this.formItem11, this.formItem12, this.formItem13);
		this.txtAccountManager.setWidthFull();
		this.txtAccountManager.setHeight(null);
		this.formItem111.add(this.txtAccountManager);
		this.txtCusInfo.setWidthFull();
		this.txtCusInfo.setHeight(null);
		this.formItem.add(this.txtCusInfo);
		this.cmbPaymentCondition.setWidthFull();
		this.cmbPaymentCondition.setHeight(null);
		this.formItem15.add(this.cmbPaymentCondition);
		this.cbxAccountBillingType.setWidthFull();
		this.cbxAccountBillingType.setHeight(null);
		this.formItem16.add(this.cbxAccountBillingType);
		this.cbxAccountBillingReports.setWidthFull();
		this.cbxAccountBillingReports.setHeight(null);
		this.formItem19.add(this.cbxAccountBillingReports);
		this.cbxSinglePdf.setWidthFull();
		this.cbxSinglePdf.setHeight(null);
		this.formItem8.add(this.cbxSinglePdf);
		this.txtExtRef1.setWidthFull();
		this.txtExtRef1.setHeight(null);
		this.formItem31.add(this.txtExtRef1);
		this.txtExtRef2.setWidthFull();
		this.txtExtRef2.setHeight(null);
		this.formItem4.add(this.txtExtRef2);
		this.twinColSelect.setWidthFull();
		this.twinColSelect.setHeight("100px");
		this.formItem21.add(this.twinColSelect);
		this.gridLayoutFlags.add(this.formItem111, this.formItem, this.formItem15, this.formItem16, this.formItem19,
			this.formItem8, this.formItem31, this.formItem4, this.formItem21);
		this.cmdNewAddress.setWidth(null);
		this.cmdNewAddress.setHeight("30px");
		this.cmdDeleteAddress.setWidth(null);
		this.cmdDeleteAddress.setHeight("33px");
		this.cmdEditAddress.setSizeUndefined();
		this.cmdReloadAddress.setWidth(null);
		this.cmdReloadAddress.setHeight("30px");
		this.cmdInfoAddress.setWidth(null);
		this.cmdInfoAddress.setHeight("30px");
		this.horizontalLayout4.add(this.cmdNewAddress, this.cmdDeleteAddress, this.cmdEditAddress,
			this.cmdReloadAddress,
			this.cmdInfoAddress);
		this.labelAdressen.setSizeUndefined();
		this.horizontalLayout2.add(this.labelAdressen);
		this.horizontalLayout4.setWidthFull();
		this.horizontalLayout4.setHeight("50px");
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("30px");
		this.tableAddress.setSizeFull();
		this.verticalLayoutBill.add(this.horizontalLayout4, this.horizontalLayout2, this.tableAddress);
		this.verticalLayoutBill.setFlexGrow(1.0, this.tableAddress);
		this.cmdNewCustomerLink.setWidth(null);
		this.cmdNewCustomerLink.setHeight("30px");
		this.cmdDeleteCustomerLink.setWidth(null);
		this.cmdDeleteCustomerLink.setHeight("33px");
		this.cmdEditCustomerLink.setSizeUndefined();
		this.cmdReloadCustomerLink.setWidth(null);
		this.cmdReloadCustomerLink.setHeight("30px");
		this.cmdInfoCustomerLink.setWidth(null);
		this.cmdInfoCustomerLink.setHeight("30px");
		this.horizontalLayout5.add(this.cmdNewCustomerLink, this.cmdDeleteCustomerLink, this.cmdEditCustomerLink,
			this.cmdReloadCustomerLink, this.cmdInfoCustomerLink);
		this.subprojectLabel.setSizeUndefined();
		this.horizontalLayout.add(this.subprojectLabel);
		this.horizontalLayout5.setWidthFull();
		this.horizontalLayout5.setHeight("50px");
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.tableLink.setSizeFull();
		this.verticalLayoutSubProject.add(this.horizontalLayout5, this.horizontalLayout, this.tableLink);
		this.verticalLayoutSubProject.setFlexGrow(1.0, this.tableLink);
		this.gridLayoutAddressSplitLayout.addToPrimary(this.verticalLayoutBill);
		this.gridLayoutAddressSplitLayout.addToSecondary(this.verticalLayoutSubProject);
		this.gridLayoutAddressSplitLayout.setSplitterPosition(50.0);
		this.cmdNewActivity.setWidth(null);
		this.cmdNewActivity.setHeight("30px");
		this.cmdDeleteActivity.setWidth(null);
		this.cmdDeleteActivity.setHeight("33px");
		this.cmdEditActivity.setSizeUndefined();
		this.cmdReloadActivity.setWidth(null);
		this.cmdReloadActivity.setHeight("30px");
		this.cmdInfoActivity.setWidth(null);
		this.cmdInfoActivity.setHeight("30px");
		this.horizontalLayoutActivity2.setWidth("300px");
		this.horizontalLayoutActivity2.setHeight("50px");
		this.containerFilterComponent2.setWidth("50%");
		this.containerFilterComponent2.setHeight(null);
		this.horizontalLayoutActivityAction.add(this.cmdNewActivity, this.cmdDeleteActivity, this.cmdEditActivity,
			this.cmdReloadActivity, this.cmdInfoActivity, this.horizontalLayoutActivity2,
			this.containerFilterComponent2);
		this.horizontalLayoutActivityAction.setFlexGrow(1.0, this.containerFilterComponent2);
		this.labelActivity.setSizeUndefined();
		this.horizontalLayoutActivity.add(this.labelActivity);
		this.horizontalLayoutActivityAction.setWidthFull();
		this.horizontalLayoutActivityAction.setHeight("50px");
		this.horizontalLayoutActivity.setWidthFull();
		this.horizontalLayoutActivity.setHeight("30px");
		this.tableActivity.setSizeFull();
		this.gridLayoutListActivityVertical.add(this.horizontalLayoutActivityAction, this.horizontalLayoutActivity,
			this.tableActivity);
		this.gridLayoutListActivityVertical.setFlexGrow(1.0, this.tableActivity);
		this.cmdNewRelation.setWidth(null);
		this.cmdNewRelation.setHeight("30px");
		this.cmdDeleteRelation.setWidth(null);
		this.cmdDeleteRelation.setHeight("33px");
		this.horizontalLayoutRelationAction.add(this.cmdNewRelation, this.cmdDeleteRelation);
		this.labelRelation.setSizeUndefined();
		this.horizontalLayoutRelation.add(this.iconBeziehungen, this.labelRelation);
		this.horizontalLayoutRelationAction.setWidthFull();
		this.horizontalLayoutRelationAction.setHeight("50px");
		this.horizontalLayoutRelation.setWidthFull();
		this.horizontalLayoutRelation.setHeight("30px");
		this.tableRelation.setSizeFull();
		this.gridLayoutRelationVertical.add(this.horizontalLayoutRelationAction, this.horizontalLayoutRelation,
			this.tableRelation);
		this.gridLayoutRelationVertical.setFlexGrow(1.0, this.tableRelation);
		this.labelReference.setSizeUndefined();
		this.horizontalLayoutReference.add(this.icon, this.labelReference);
		this.horizontalLayoutReference.setWidthFull();
		this.horizontalLayoutReference.setHeight("30px");
		this.tableOrder.setSizeFull();
		this.verticalLayoutReference.add(this.horizontalLayoutReference, this.tableOrder);
		this.verticalLayoutReference.setFlexGrow(1.0, this.tableOrder);
		this.projekteLabel.setSizeUndefined();
		this.horizontalLayoutProjekte.add(this.projekteLabel);
		this.horizontalLayoutProjekte.setWidthFull();
		this.horizontalLayoutProjekte.setHeight("30px");
		this.tableProject.setSizeFull();
		this.verticalLayoutProject.add(this.horizontalLayoutProjekte, this.tableProject);
		this.verticalLayoutProject.setFlexGrow(1.0, this.tableProject);
		this.gridLayoutListRef.addToPrimary(this.verticalLayoutReference);
		this.gridLayoutListRef.addToSecondary(this.verticalLayoutProject);
		this.gridLayoutListRef.setSplitterPosition(50.0);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.cmdVcard.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset, this.cmdVcard);
		this.tabs.setWidthFull();
		this.tabs.setHeight("36px");
		this.gridLayoutContact.setSizeFull();
		this.gridLayoutFlags.setSizeFull();
		this.gridLayoutAddressSplitLayout.setSizeFull();
		this.gridLayoutListActivityVertical.setSizeFull();
		this.gridLayoutRelationVertical.setSizeFull();
		this.gridLayoutListRef.setSizeFull();
		this.horizontalLayout3.setSizeUndefined();
		this.mainLayout.add(this.tabs, this.gridLayoutContact, this.gridLayoutFlags, this.gridLayoutAddressSplitLayout,
			this.gridLayoutListActivityVertical, this.gridLayoutRelationVertical, this.gridLayoutListRef,
			this.horizontalLayout3);
		this.mainLayout.setFlexGrow(1.0, this.gridLayoutAddressSplitLayout);
		this.mainLayout.setFlexGrow(1.0, this.gridLayoutListRef);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.mainLayout);
		this.splitLayout.setSplitterPosition(50.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.tabs.setSelectedIndex(0);

		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdReport.addClickListener(this::cmdReport_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.cmdImport.addClickListener(this::cmdImport_onClick);
		this.table.addItemClickListener(this::table_onItemClick);
		this.txtZip.addValueChangeListener(this::txtZip_valueChanged);
		this.cmbCity.addValueChangeListener(this::cmbCity_valueChanged);
		this.cmdNewAddress.addClickListener(this::cmdNewAddress_onClick);
		this.cmdDeleteAddress.addClickListener(this::cmdDeleteAddress_onClick);
		this.cmdEditAddress.addClickListener(this::cmdEditAddress_onClick);
		this.cmdReloadAddress.addClickListener(this::cmdReloadAddress_onClick);
		this.cmdInfoAddress.addClickListener(this::cmdInfoAddress_onClick);
		this.tableAddress.addItemDoubleClickListener(this::tableAddress_onItemDoubleClick);
		this.cmdNewCustomerLink.addClickListener(this::cmdNewCustomerLink_onClick);
		this.cmdDeleteCustomerLink.addClickListener(this::cmdDeleteCustomerLink_onClick);
		this.cmdEditCustomerLink.addClickListener(this::cmdEditCustomerLink_onClick);
		this.cmdReloadCustomerLink.addClickListener(this::cmdReloadCustomerLink_onClick);
		this.cmdInfoCustomerLink.addClickListener(this::cmdInfoCustomerLink_onClick);
		this.tableLink.addItemDoubleClickListener(this::tableLink_onItemDoubleClick);
		this.cmdNewActivity.addClickListener(this::cmdNewActivity_onClick);
		this.cmdDeleteActivity.addClickListener(this::cmdDeleteActivity_onClick);
		this.cmdEditActivity.addClickListener(this::cmdEditActivity_onClick);
		this.cmdReloadActivity.addClickListener(this::cmdReloadActivity_onClick);
		this.cmdInfoActivity.addClickListener(this::cmdInfoActivity_onClick);
		this.tableActivity.addItemDoubleClickListener(this::tableActivity_onItemDoubleClick);
		this.cmdNewRelation.addClickListener(this::cmdNewRelation_onClick);
		this.cmdDeleteRelation.addClickListener(this::cmdDeleteRelation_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
		this.cmdVcard.addClickListener(this::cmdVcard_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Grid<Customer>                 table;
	private BeanValidationBinder<Customer> binder;
	private ComboBox<BillReport>           cbxAccountBillingReports;
	private Tab                            gridLayoutContactTab, gridLayoutFlagsTab, gridLayoutAddressTab,
		gridLayoutListActivityTab, gridLayoutRelationTab, gridLayoutListRefTab;
	private HorizontalLayout               proHorizontalLayout, horizontalLayout4, horizontalLayout2, horizontalLayout5,
		horizontalLayout, horizontalLayoutActivityAction, horizontalLayoutActivity2, horizontalLayoutActivity,
		horizontalLayoutRelationAction, horizontalLayoutRelation, horizontalLayoutReference, horizontalLayoutProjekte,
		horizontalLayout3;
	private Label                          lblCountry, labelAdressen, subprojectLabel, labelActivity, labelRelation,
		labelReference, projekteLabel;
	private Tabs                           tabs;
	private Grid<CustomerLink>             tableLink;
	private FormItem                       formItem2, formItem3, formItem41, formItem5, formItem6, formItem7,
		formItem101,
		formItem11, formItem12, formItem13, formItem111, formItem, formItem15, formItem16, formItem19, formItem8,
		formItem31, formItem4, formItem21;
	private ComboBox<State>                cbxState;
	private SplitLayout                    splitLayout, gridLayoutAddressSplitLayout, gridLayoutListRef;
	private Grid<Activity>                 tableActivity;
	private ComboBox<AccountType>          cbxAccountType;
	private ComboBox<Salutation>           cbxAccountSalutation;
	private TextField                      txtCusNumber, txtCusCompany, txtCusName, txtCusFirstName, txtCusAddress,
		txtZip,
		txtAccountManager, txtCusInfo, txtExtRef1, txtExtRef2;
	private Icon                           iconBeziehungen, icon;
	private IronIcon                       ironIcon;
	private Anchor                         linkMaps;
	private ComboBox<City>                 cmbCity;
	private Grid<Address>                  tableAddress;
	private VerticalLayout                 verticalLayout, mainLayout, verticalLayoutBill, verticalLayoutSubProject,
		gridLayoutListActivityVertical, gridLayoutRelationVertical, verticalLayoutReference, verticalLayoutProject;
	private FilterComponent                containerFilterComponent, containerFilterComponent2;
	private Grid<Order>                    tableOrder;
	private FormLayout                     gridLayoutContact, gridLayoutFlags;
	private Checkbox                       cbxSinglePdf;
	private Button                         cmdNew, cmdDelete, cmdReload, cmdReport, cmdInfo, cmdImport, cmdNewAddress,
		cmdDeleteAddress, cmdEditAddress, cmdReloadAddress, cmdInfoAddress, cmdNewCustomerLink, cmdDeleteCustomerLink,
		cmdEditCustomerLink, cmdReloadCustomerLink, cmdInfoCustomerLink, cmdNewActivity, cmdDeleteActivity,
		cmdEditActivity,
		cmdReloadActivity, cmdInfoActivity, cmdNewRelation, cmdDeleteRelation, cmdSave, cmdReset, cmdVcard;
	private DatePicker                     datCusBirthdate;
	private ComboBox<BillTarget>           cbxAccountBillingType;
	private Grid<ContactRelation>          tableRelation;
	private TwinColGrid<LabelDefinition>   twinColSelect;
	private ComboBox<PaymentCondition>     cmbPaymentCondition;
	private Grid<Project>                  tableProject;
	// </generated-code>

}
