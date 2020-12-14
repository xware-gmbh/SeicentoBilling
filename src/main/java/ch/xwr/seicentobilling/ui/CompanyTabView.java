
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;


@Route("company")
public class CompanyTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(CompanyTabView.class);
	
	/**
	 *
	 */
	public CompanyTabView()
	{
		super();
		this.initUI();
		this.gridLayoutNbr.setVisible(false);
		this.gridLayoutJasper.setVisible(false);
		this.gridLayoutIfc.setVisible(false);

		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.tab, this.gridLayout);
		tabsToPages.put(this.tab2, this.gridLayoutNbr);
		tabsToPages.put(this.tab3, this.gridLayoutJasper);
		tabsToPages.put(this.tab4, this.gridLayoutIfc);

		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});
		
		this.grid.addComponentColumn(item -> this.createActiveButton(this.grid, item))
			.setHeader("");
	}
	
	private Button createActiveButton(final Grid<Company> grid, final Company bean)
	{
		final Button button = new Button("Aktivieren", clickEvent -> {

			SeicentoNotification.showInfo("Clicked in line, id = " + bean.getCmpId());
			if(bean.getCmpState().equals(LovState.State.active))
			{
				SeicentoNotification.showInfo("Konfiguration ist bereits aktiv");
			}

			// Delete Record
			final CompanyDAO    cmpDao = new CompanyDAO();
			final List<Company> slrs   = cmpDao.findAll();
			
			boolean activated = false;
			for(final Iterator<Company> iterator = slrs.iterator(); iterator.hasNext();)
			{
				final Company cmp = iterator.next();
				cmp.setCmpState(LovState.State.inactive);
				if(cmp.getCmpId() == bean.getCmpId())
				{
					cmp.setCmpState(LovState.State.active);
					activated = true;
				}
				cmpDao.save(cmp);
			}
			
			if(!activated)
			{
				// should not happen
				final Company fallback = slrs.get(0);
				fallback.setCmpState(LovState.State.active);
				cmpDao.save(fallback);
			}
			
			grid.setDataProvider(DataProvider.ofCollection(cmpDao.findAll()));
		});
		if(bean.getCmpState().equals(LovState.State.active))
		{
			button.setVisible(false);
		}
		else
		{
			button.setVisible(true);
		}
		return button;
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Company> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final CompanyDAO CompanyDao  = new CompanyDAO();
			final Company    CompanyBean =
				CompanyDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getCmpId());
			this.binder.setBean(CompanyBean);
		}
	}
	
	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final Company bean = this.binder.getBean();
		if(bean.getCmpId() == null || bean.getCmpId() < 1)
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
		final Company bean = new Company();
		bean.setCmpState(LovState.State.active);
		this.binder.setBean(bean);

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
		this.grid.setDataProvider(DataProvider.ofCollection(new CompanyDAO().findAll()));
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
			final Company bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog  win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getCmpId(), bean.getClass().getSimpleName()));
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
				
				final Company bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCmpId(), bean.getClass().getSimpleName());
				
				final CompanyDAO dao = new CompanyDAO();
				dao.remove(bean);
				dao.flush();
				
				this.binder.removeBean();
				CompanyTabView.this.binder.setBean(new Company());
				this.grid.setDataProvider(DataProvider.ofCollection(new CompanyDAO().findAll()));
				CompanyTabView.this.grid.getDataProvider().refreshAll();
				
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
				CompanyTabView.LOG.error("Error on delete", e);
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
		final boolean isNew = this.isNew();
		if(SeicentoCrud.doSave(this.binder, new CompanyDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getCmpId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				CompanyTabView.LOG.error("could not save ObjRoot", e);
			}
		}

		if(isNew)
		{
			this.cmdReload_onClick(null);
		}

	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		if(this.binder.getBean() != null)
		{
			final CompanyDAO dao      = new CompanyDAO();
			final Company    cityBean = dao.find(this.binder.getBean().getCmpId());
			this.binder.setBean(cityBean);
		}
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout           = new SplitLayout();
		this.verticalLayout        = new VerticalLayout();
		this.filterComponent       = new FilterComponent();
		this.horizontalLayout2     = new HorizontalLayout();
		this.cmdNew                = new Button();
		this.cmdDelete             = new Button();
		this.cmdReload             = new Button();
		this.cmdInfo               = new Button();
		this.grid                  = new Grid<>(Company.class, false);
		this.verticalLayout2       = new VerticalLayout();
		this.tabs                  = new Tabs();
		this.tab                   = new Tab();
		this.tab2                  = new Tab();
		this.tab3                  = new Tab();
		this.tab4                  = new Tab();
		this.gridLayout            = new FormLayout();
		this.formItem              = new FormItem();
		this.txtCmpName            = new TextField();
		this.formItem2             = new FormItem();
		this.txtCmpAddress         = new TextField();
		this.formItem3             = new FormItem();
		this.txtCmpZip             = new TextField();
		this.formItem4             = new FormItem();
		this.txtCmpPlace           = new TextField();
		this.formItem5             = new FormItem();
		this.txtCmpCurrency        = new TextField();
		this.formItem6             = new FormItem();
		this.txtCmpUid             = new TextField();
		this.formItem7             = new FormItem();
		this.txtCmpPhone           = new TextField();
		this.formItem8             = new FormItem();
		this.txtCmpMail            = new TextField();
		this.formItem9             = new FormItem();
		this.txtCmpComm1           = new TextField();
		this.formItem10            = new FormItem();
		this.txtCmpBusiness        = new TextField();
		this.gridLayoutNbr         = new FormLayout();
		this.formItem11            = new FormItem();
		this.txtCmpBookingYear     = new TextField();
		this.formItem12            = new FormItem();
		this.txtCmpLastOrderNbr    = new TextField();
		this.formItem13            = new FormItem();
		this.txtCmpLastItemNbr     = new TextField();
		this.formItem14            = new FormItem();
		this.txtCmpLastCustomerNbr = new TextField();
		this.gridLayoutJasper      = new FormLayout();
		this.formItem15            = new FormItem();
		this.txtCmpJasperUri       = new TextField();
		this.formItem16            = new FormItem();
		this.txtCmpReportUsr       = new TextField();
		this.formItem17            = new FormItem();
		this.passwordField         = new PasswordField();
		this.gridLayoutIfc         = new FormLayout();
		this.formItem18            = new FormItem();
		this.cboCmpAbaActive       = new Checkbox();
		this.formItem19            = new FormItem();
		this.txtCmpAbaEndpointCus  = new TextField();
		this.formItem20            = new FormItem();
		this.txtCmpEndpointDoc     = new TextField();
		this.formItem21            = new FormItem();
		this.txtCmpEndpointPay     = new TextField();
		this.formItem22            = new FormItem();
		this.txtCmpEndpointCre     = new TextField();
		this.formItem23            = new FormItem();
		this.txtCmpEndpointCreDoc  = new TextField();
		this.formItem24            = new FormItem();
		this.txtCmpAbaUser         = new TextField();
		this.formItem25            = new FormItem();
		this.txtCmpAbaMandator     = new TextField();
		this.horizontalLayout3     = new HorizontalLayout();
		this.cmdSave               = new Button();
		this.cmdReset              = new Button();
		this.binder                = new BeanValidationBinder<>(Company.class);

		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(Company::getCmpName).setKey("cmpName").setHeader("Name").setSortable(true);
		this.grid.addColumn(Company::getCmpUid).setKey("cmpUid").setHeader("Uid").setSortable(true);
		this.grid.addColumn(Company::getCmpBookingYear).setKey("cmpBookingYear").setHeader("BH Jahr").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(Company::getCmpMail).setKey("cmpMail").setHeader("Mail").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(Company::getCmpPhone).setKey("cmpPhone").setHeader("Telefon").setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new CompanyDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayout2.setMinHeight("100%");
		this.tabs.setMinHeight("50px");
		this.tab.setLabel(StringResourceUtils.optLocalizeString("{$gridLayout.caption}", this));
		this.tab2.setLabel(StringResourceUtils.optLocalizeString("{$gridLayoutNbr.caption}", this));
		this.tab3.setLabel("Jasper");
		this.tab4.setLabel("Schnittstelle");
		this.gridLayout.getStyle().set("overflow-x", "hidden");
		this.gridLayout.getStyle().set("overflow-y", "auto");
		this.gridLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.txtCmpName.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpName.value}", this));
		this.txtCmpAddress.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpAddress.value}", this));
		this.txtCmpZip.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpZip.value}", this));
		this.txtCmpPlace.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpPlace.value}", this));
		this.txtCmpCurrency.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpCurrency.value}", this));
		this.txtCmpUid.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpUid.value}", this));
		this.txtCmpPhone.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpPhone.value}", this));
		this.txtCmpMail.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpMail.value}", this));
		this.txtCmpComm1.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpComm1.value}", this));
		this.txtCmpBusiness.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpBusiness.value}", this));
		this.gridLayoutNbr.getStyle().set("overflow-x", "hidden");
		this.gridLayoutNbr.getStyle().set("overflow-y", "auto");
		this.gridLayoutNbr.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.txtCmpBookingYear.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpBookingYear.value}", this));
		this.txtCmpLastOrderNbr.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpLastOrderNbr.value}", this));
		this.txtCmpLastItemNbr.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpLastItemNbr.value}", this));
		this.txtCmpLastCustomerNbr
			.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpLastCustomerNbr.value}", this));
		this.gridLayoutJasper.getStyle().set("overflow-x", "hidden");
		this.gridLayoutJasper.getStyle().set("overflow-y", "auto");
		this.gridLayoutJasper.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.txtCmpJasperUri.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpJasperUri.value}", this));
		this.txtCmpReportUsr.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpReportUsr.value}", this));
		this.passwordField.setLabel(StringResourceUtils.optLocalizeString("{$lblCmpReportPwd.value}", this));
		this.gridLayoutIfc.getStyle().set("overflow-x", "hidden");
		this.gridLayoutIfc.getStyle().set("overflow-y", "auto");
		this.gridLayoutIfc.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.formItem18.getStyle().set("padding-top", "30px");
		this.cboCmpAbaActive.setLabel("Schnittstelle Aktiv");
		this.txtCmpAbaEndpointCus.setLabel("Endpoint Debi");
		this.txtCmpEndpointDoc.setLabel("Endpoint Beleg");
		this.txtCmpEndpointPay.setLabel("Endpoint Zahlung");
		this.txtCmpEndpointCre.setLabel("Endpoint Kredi");
		this.txtCmpEndpointCreDoc.setLabel("Endpoint Spesen");
		this.txtCmpAbaUser.setLabel("Benutzer");
		this.txtCmpAbaMandator.setLabel("Mandant");
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());

		this.binder.forField(this.txtCmpName).withNullRepresentation("").bind("cmpName");
		this.binder.forField(this.txtCmpAddress).withNullRepresentation("").bind("cmpAddress");
		this.binder.forField(this.txtCmpZip).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpZip");
		this.binder.forField(this.txtCmpPlace).withNullRepresentation("").bind("cmpPlace");
		this.binder.forField(this.txtCmpCurrency).withNullRepresentation("").bind("cmpCurrency");
		this.binder.forField(this.txtCmpUid).withNullRepresentation("").bind("cmpUid");
		this.binder.forField(this.txtCmpPhone).withNullRepresentation("").bind("cmpPhone");
		this.binder.forField(this.txtCmpMail).withNullRepresentation("").bind("cmpMail");
		this.binder.forField(this.txtCmpComm1).withNullRepresentation("").bind("cmpComm1");
		this.binder.forField(this.txtCmpBusiness).withNullRepresentation("").bind("cmpBusiness");
		this.binder.forField(this.txtCmpBookingYear).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpBookingYear");
		this.binder.forField(this.txtCmpLastOrderNbr).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpLastOrderNbr");
		this.binder.forField(this.txtCmpLastItemNbr).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpLastItemNbr");
		this.binder.forField(this.txtCmpLastCustomerNbr).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpLastCustomerNbr");
		this.binder.forField(this.txtCmpJasperUri).withNullRepresentation("").bind("cmpJasperUri");
		this.binder.forField(this.txtCmpReportUsr).withNullRepresentation("").bind("cmpReportUsr");
		this.binder.forField(this.passwordField).withNullRepresentation("").bind("cmpReportPwd");
		this.binder.forField(this.cboCmpAbaActive).withNullRepresentation(false).bind("cmpAbaActive");
		this.binder.forField(this.txtCmpAbaEndpointCus).withNullRepresentation("").bind("cmpAbaEndpointCus");
		this.binder.forField(this.txtCmpEndpointDoc).withNullRepresentation("").bind("cmpAbaEndpointDoc");
		this.binder.forField(this.txtCmpEndpointPay).withNullRepresentation("").bind("cmpAbaEndpointPay");
		this.binder.forField(this.txtCmpEndpointCre).withNullRepresentation("").bind("cmpAbaEndpointCre");
		this.binder.forField(this.txtCmpEndpointCreDoc).withNullRepresentation("").bind("cmpAbaEndpointCreDoc");
		this.binder.forField(this.txtCmpAbaUser).withNullRepresentation("").bind("cmpAbaUser");
		this.binder.forField(this.txtCmpAbaMandator).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cmpAbaMandator");

		this.filterComponent.connectWith(this.grid.getDataProvider());
		this.filterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("cmpName", "cmpPlace"), Arrays.asList("cmpPlace", "cmpState", "cmpZip")));

		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdInfo);
		this.filterComponent.setWidthFull();
		this.filterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.filterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.tabs.add(this.tab, this.tab2, this.tab3, this.tab4);
		this.txtCmpName.setWidthFull();
		this.txtCmpName.setHeight(null);
		this.formItem.add(this.txtCmpName);
		this.txtCmpAddress.setWidthFull();
		this.txtCmpAddress.setHeight(null);
		this.formItem2.add(this.txtCmpAddress);
		this.txtCmpZip.setWidthFull();
		this.txtCmpZip.setHeight(null);
		this.formItem3.add(this.txtCmpZip);
		this.txtCmpPlace.setWidthFull();
		this.txtCmpPlace.setHeight(null);
		this.formItem4.add(this.txtCmpPlace);
		this.txtCmpCurrency.setWidthFull();
		this.txtCmpCurrency.setHeight(null);
		this.formItem5.add(this.txtCmpCurrency);
		this.txtCmpUid.setWidthFull();
		this.txtCmpUid.setHeight(null);
		this.formItem6.add(this.txtCmpUid);
		this.txtCmpPhone.setWidthFull();
		this.txtCmpPhone.setHeight(null);
		this.formItem7.add(this.txtCmpPhone);
		this.txtCmpMail.setWidthFull();
		this.txtCmpMail.setHeight(null);
		this.formItem8.add(this.txtCmpMail);
		this.txtCmpComm1.setWidthFull();
		this.txtCmpComm1.setHeight(null);
		this.formItem9.add(this.txtCmpComm1);
		this.txtCmpBusiness.setWidthFull();
		this.txtCmpBusiness.setHeight(null);
		this.formItem10.add(this.txtCmpBusiness);
		this.gridLayout.add(this.formItem, this.formItem2, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6,
			this.formItem7, this.formItem8, this.formItem9, this.formItem10);
		this.txtCmpBookingYear.setWidthFull();
		this.txtCmpBookingYear.setHeight(null);
		this.formItem11.add(this.txtCmpBookingYear);
		this.txtCmpLastOrderNbr.setWidthFull();
		this.txtCmpLastOrderNbr.setHeight(null);
		this.formItem12.add(this.txtCmpLastOrderNbr);
		this.txtCmpLastItemNbr.setWidthFull();
		this.txtCmpLastItemNbr.setHeight(null);
		this.formItem13.add(this.txtCmpLastItemNbr);
		this.txtCmpLastCustomerNbr.setWidthFull();
		this.txtCmpLastCustomerNbr.setHeight(null);
		this.formItem14.add(this.txtCmpLastCustomerNbr);
		this.gridLayoutNbr.add(this.formItem11, this.formItem12, this.formItem13, this.formItem14);
		this.txtCmpJasperUri.setWidthFull();
		this.txtCmpJasperUri.setHeight(null);
		this.formItem15.add(this.txtCmpJasperUri);
		this.txtCmpReportUsr.setWidthFull();
		this.txtCmpReportUsr.setHeight(null);
		this.formItem16.add(this.txtCmpReportUsr);
		this.passwordField.setWidthFull();
		this.passwordField.setHeight(null);
		this.formItem17.add(this.passwordField);
		this.gridLayoutJasper.add(this.formItem15, this.formItem16, this.formItem17);
		this.cboCmpAbaActive.setWidthFull();
		this.cboCmpAbaActive.setHeight(null);
		this.formItem18.add(this.cboCmpAbaActive);
		this.txtCmpAbaEndpointCus.setWidthFull();
		this.txtCmpAbaEndpointCus.setHeight(null);
		this.formItem19.add(this.txtCmpAbaEndpointCus);
		this.txtCmpEndpointDoc.setWidthFull();
		this.txtCmpEndpointDoc.setHeight(null);
		this.formItem20.add(this.txtCmpEndpointDoc);
		this.txtCmpEndpointPay.setWidthFull();
		this.txtCmpEndpointPay.setHeight(null);
		this.formItem21.add(this.txtCmpEndpointPay);
		this.txtCmpEndpointCre.setWidthFull();
		this.txtCmpEndpointCre.setHeight(null);
		this.formItem22.add(this.txtCmpEndpointCre);
		this.txtCmpEndpointCreDoc.setWidthFull();
		this.txtCmpEndpointCreDoc.setHeight(null);
		this.formItem23.add(this.txtCmpEndpointCreDoc);
		this.txtCmpAbaUser.setWidthFull();
		this.txtCmpAbaUser.setHeight(null);
		this.formItem24.add(this.txtCmpAbaUser);
		this.txtCmpAbaMandator.setWidthFull();
		this.txtCmpAbaMandator.setHeight(null);
		this.formItem25.add(this.txtCmpAbaMandator);
		this.gridLayoutIfc.add(this.formItem18, this.formItem19, this.formItem20, this.formItem21, this.formItem22,
			this.formItem23, this.formItem24, this.formItem25);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.tabs.setWidthFull();
		this.tabs.setHeight("36px");
		this.gridLayout.setSizeFull();
		this.gridLayoutNbr.setSizeFull();
		this.gridLayoutJasper.setSizeFull();
		this.gridLayoutIfc.setSizeFull();
		this.horizontalLayout3.setSizeUndefined();
		this.verticalLayout2.add(this.tabs, this.gridLayout, this.gridLayoutNbr, this.gridLayoutJasper,
			this.gridLayoutIfc,
			this.horizontalLayout3);
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
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.verticalLayout2.addClickListener(this::verticalLayout2_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Tab                           tab, tab2, tab4, tab3;
	private VerticalLayout                verticalLayout, verticalLayout2;
	private HorizontalLayout              horizontalLayout2, horizontalLayout3;
	private Tabs                          tabs;
	private FilterComponent               filterComponent;
	private FormItem                      formItem, formItem2, formItem3, formItem4, formItem5, formItem6, formItem7,
		formItem8, formItem9, formItem10, formItem11, formItem12, formItem13, formItem14, formItem15, formItem16,
		formItem17, formItem18, formItem19, formItem20, formItem21, formItem22, formItem23, formItem24, formItem25;
	private FormLayout                    gridLayout, gridLayoutNbr, gridLayoutJasper, gridLayoutIfc;
	private Checkbox                      cboCmpAbaActive;
	private Button                        cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private Grid<Company>                 grid;
	private SplitLayout                   splitLayout;
	private PasswordField                 passwordField;
	private BeanValidationBinder<Company> binder;
	private TextField                     txtCmpName, txtCmpAddress, txtCmpZip, txtCmpPlace, txtCmpCurrency, txtCmpUid,
		txtCmpPhone, txtCmpMail, txtCmpComm1, txtCmpBusiness, txtCmpBookingYear, txtCmpLastOrderNbr, txtCmpLastItemNbr,
		txtCmpLastCustomerNbr, txtCmpJasperUri, txtCmpReportUsr, txtCmpAbaEndpointCus, txtCmpEndpointDoc,
		txtCmpEndpointPay,
		txtCmpEndpointCre, txtCmpEndpointCreDoc, txtCmpAbaUser, txtCmpAbaMandator;
	// </generated-code>

}
