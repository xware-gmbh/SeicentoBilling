
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
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
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.LovState.Unit;
import ch.xwr.seicentobilling.business.NumberRangeHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.ItemGroupDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.ItemGroup;
import ch.xwr.seicentobilling.entities.Vat;


@Route("artikel")
public class ItemTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ItemTabView.class);

	/**
	 *
	 */
	public ItemTabView()
	{
		super();
		this.initUI();
		// Type
		this.comboBoxState.setItems(LovState.State.values());
		this.cbxUnit.setItems(LovState.Unit.values());
		Locale.setDefault(new Locale("de", "CH"));

		// set RO Fields
		this.setROFields();

	}
	
	private void setROFields()
	{

		boolean hasData = true;
		if(this.binder.getBean() == null)
		{
			hasData = false;
		}
		// this.cmdSave.setEnabled(hasData);
		// this.tabSheet.setEnabled(hasData);
		// this.formLayout.setEnabled(hasData);

	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Item> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final ItemDAO cityDao  = new ItemDAO();
			final Item    cityBean =
				cityDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getItmId());
			this.binder.setBean(cityBean);
			this.setROFields();
		}
	}

	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final Item bean = this.binder.getBean();
		if(bean.getItmId() == null || bean.getItmId() < 1)
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
		final Item bean = new Item();
		bean.setItmState(LovState.State.active);
		bean.setItmState(LovState.State.active);
		bean.setItmPrice1(new Double(0));
		bean.setItmUnit(LovState.Unit.hour);
		this.binder.setBean(bean);
		this.checkItemNumber(true, false);
		this.setROFields();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_onClick(final ClickEvent<Button> event)
	{
		this.grid.setDataProvider(DataProvider.ofCollection(new ItemDAO().findAll()));
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
		this.setROFields();
		if(SeicentoCrud.doSave(this.binder, new ItemDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getItmId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				ItemTabView.LOG.error("could not save ObjRoot", e);
			}
		}

		if(isNew)
		{
			this.cmdReload_onClick(null);
		}

	}

	private void checkItemNumber(final boolean isNew, final boolean commitNbr)
	{
		if(!isNew)
		{
			return;
		}
		
		Integer nbr = null;
		try
		{
			nbr = Integer.parseInt(this.txtItmIdent.getValue());
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
			this.txtItmIdent.setValue(handler.getNewItemNumber(false, nbr).toString());
		}
		else
		{
			handler.getNewItemNumber(true, nbr);
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
			final ItemDAO cityDao  = new ItemDAO();
			final Item    cityBean = cityDao.find(this.binder.getBean().getItmId());
			this.binder.setBean(cityBean);
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
		
		if(this.grid.getSelectedItems() != null)
		{
			final Item   bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getItmId(), bean.getClass().getSimpleName()));
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
		
		ConfirmDialog.show(this.getUI().get(), "Datensatz löschen", "Wirklich löschen?");
		
		try
		{
			
			final Item bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			
			// Delete Record
			final RowObjectManager man = new RowObjectManager();
			man.deleteObject(bean.getItmId(), bean.getClass().getSimpleName());
			
			final ItemDAO dao = new ItemDAO();
			dao.remove(bean);
			dao.flush();
			
			this.binder.removeBean();
			ItemTabView.this.binder.setBean(new Item());
			this.grid.setDataProvider(DataProvider.ofCollection(new ItemDAO().findAll()));
			ItemTabView.this.grid.getDataProvider().refreshAll();
			
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
			ItemTabView.LOG.error("Error on delete", e);
		}
		
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout       = new SplitLayout();
		this.verticalLayout    = new VerticalLayout();
		this.filterComponent   = new FilterComponent();
		this.horizontalLayout2 = new HorizontalLayout();
		this.cmdNew            = new Button();
		this.cmdDelete         = new Button();
		this.cmdReload         = new Button();
		this.cmdInfo           = new Button();
		this.grid              = new Grid<>(Item.class, false);
		this.formLayout        = new FormLayout();
		this.formItem2         = new FormItem();
		this.lblItmIdent       = new Label();
		this.txtItmIdent       = new TextField();
		this.formItem          = new FormItem();
		this.lblItmName        = new Label();
		this.txtItmName        = new TextField();
		this.formItem3         = new FormItem();
		this.lblItmPrice1      = new Label();
		this.txtItmPrice1      = new TextField();
		this.formItem4         = new FormItem();
		this.lblItmPrice2      = new Label();
		this.txtItmPrice2      = new TextField();
		this.formItem7         = new FormItem();
		this.lblItmUnit        = new Label();
		this.cbxUnit           = new ComboBox<>();
		this.formItem8         = new FormItem();
		this.lblItemGroup      = new Label();
		this.cmbItemGroup      = new ComboBox<>();
		this.formItem9         = new FormItem();
		this.lblVat            = new Label();
		this.cmbVat            = new ComboBox<>();
		this.formItem10        = new FormItem();
		this.lblAccount        = new Label();
		this.txtAccount        = new TextField();
		this.formItem6         = new FormItem();
		this.lblItmState       = new Label();
		this.comboBoxState     = new ComboBox<>();
		this.horizontalLayout3 = new HorizontalLayout();
		this.cmdSave           = new Button();
		this.cmdReset          = new Button();
		this.binder            = new BeanValidationBinder<>(Item.class);
		
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(Item::getItmIdent).setKey("itmIdent").setHeader("Ident").setSortable(true);
		this.grid.addColumn(Item::getItmName).setKey("itmName").setHeader("Name").setSortable(true);
		this.grid.addColumn(new NumberRenderer<>(Item::getItmPrice1, NumberFormatBuilder.Currency().build(), ""))
			.setKey("itmPrice1").setHeader("Preis 1").setSortable(true);
		this.grid.addColumn(v -> Optional.ofNullable(v).map(Item::getItemGroup).map(ItemGroup::getItgName).orElse(null))
			.setKey("itemGroup.itgName").setHeader("Gruppe").setSortable(true);
		this.grid.addColumn(v -> Optional.ofNullable(v).map(Item::getVat).map(Vat::getVatName).orElse(null))
			.setKey("vat.vatName").setHeader("Mwst").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Item::getItmState)).setKey("itmState").setHeader("Status")
			.setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new ItemDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.formItem2.getElement().setAttribute("colspan", "4");
		this.lblItmIdent.setMaxHeight("");
		this.lblItmIdent.setText(StringResourceUtils.optLocalizeString("{$lblItmIdent.value}", this));
		this.formItem.getElement().setAttribute("colspan", "4");
		this.lblItmName.setText(StringResourceUtils.optLocalizeString("{$lblItmName.value}", this));
		this.lblItmPrice1.setText(StringResourceUtils.optLocalizeString("{$lblItmPrice1.value}", this));
		this.txtItmPrice1.setPattern("");
		this.txtItmPrice1.setTitle("");
		this.lblItmPrice2.setText(StringResourceUtils.optLocalizeString("{$lblItmPrice2.value}", this));
		this.lblItmUnit.setText(StringResourceUtils.optLocalizeString("{$lblItmUnit.value}", this));
		this.cbxUnit.setPattern("");
		this.cbxUnit.setLabel("");
		this.cbxUnit.setHelperText("");
		this.cbxUnit.getStyle().set("text-transform", "capitalize");
		this.cbxUnit.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblItemGroup.setText(StringResourceUtils.optLocalizeString("{$lblItemGroup.value}", this));
		this.cmbItemGroup.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbItemGroup::getItemLabelGenerator),
			DataProvider.ofCollection(new ItemGroupDAO().findAll()));
		this.cmbItemGroup.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(ItemGroup::getItgName));
		this.lblVat.setText(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAll()));
		this.cmbVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.lblAccount.setText("Konto#");
		this.formItem6.getElement().setAttribute("colspan", "4");
		this.lblItmState.setText(StringResourceUtils.optLocalizeString("{$lblItmState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		
		this.binder.forField(this.txtItmIdent).asRequired().withNullRepresentation("").bind("itmIdent");
		this.binder.forField(this.txtItmName).asRequired().withNullRepresentation("").bind("itmName");
		this.binder.forField(this.txtItmPrice1).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble()
				.numberFormatBuilder(NumberFormatBuilder.Currency().currency(Currency.getInstance("CHF"))).build())
			.bind("itmPrice1");
		this.binder.forField(this.txtItmPrice2).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Currency()).build())
			.bind("itmPrice2");
		this.binder.forField(this.comboBoxState).bind("itmState");
		this.binder.forField(this.cbxUnit).bind("itmUnit");
		this.binder.forField(this.txtAccount).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("itmAccount");
		this.binder.forField(this.cmbItemGroup).bind("itemGroup");
		this.binder.forField(this.cmbVat).asRequired().bind("vat");
		
		this.filterComponent.connectWith(this.grid.getDataProvider());
		this.filterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("itmIdent", "itmName"),
			Arrays.asList("itemGroup.itgName", "itmIdent", "itmName", "itmPrice1", "itmState", "itmUnit",
				"vat.fullName")));
		
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
		this.lblItmIdent.setSizeUndefined();
		this.lblItmIdent.getElement().setAttribute("slot", "label");
		this.txtItmIdent.setWidthFull();
		this.txtItmIdent.setHeight(null);
		this.formItem2.add(this.lblItmIdent, this.txtItmIdent);
		this.lblItmName.setSizeUndefined();
		this.lblItmName.getElement().setAttribute("slot", "label");
		this.txtItmName.setWidthFull();
		this.txtItmName.setHeight(null);
		this.formItem.add(this.lblItmName, this.txtItmName);
		this.lblItmPrice1.setSizeUndefined();
		this.lblItmPrice1.getElement().setAttribute("slot", "label");
		this.txtItmPrice1.setWidthFull();
		this.txtItmPrice1.setHeight(null);
		this.formItem3.add(this.lblItmPrice1, this.txtItmPrice1);
		this.lblItmPrice2.setSizeUndefined();
		this.lblItmPrice2.getElement().setAttribute("slot", "label");
		this.txtItmPrice2.setWidthFull();
		this.txtItmPrice2.setHeight(null);
		this.formItem4.add(this.lblItmPrice2, this.txtItmPrice2);
		this.lblItmUnit.setSizeUndefined();
		this.lblItmUnit.getElement().setAttribute("slot", "label");
		this.cbxUnit.setWidthFull();
		this.cbxUnit.setHeight(null);
		this.formItem7.add(this.lblItmUnit, this.cbxUnit);
		this.lblItemGroup.setSizeUndefined();
		this.lblItemGroup.getElement().setAttribute("slot", "label");
		this.cmbItemGroup.setWidthFull();
		this.cmbItemGroup.setHeight(null);
		this.formItem8.add(this.lblItemGroup, this.cmbItemGroup);
		this.lblVat.setSizeUndefined();
		this.lblVat.getElement().setAttribute("slot", "label");
		this.cmbVat.setWidthFull();
		this.cmbVat.setHeight(null);
		this.formItem9.add(this.lblVat, this.cmbVat);
		this.lblAccount.setSizeUndefined();
		this.lblAccount.getElement().setAttribute("slot", "label");
		this.txtAccount.setWidthFull();
		this.txtAccount.setHeight(null);
		this.formItem10.add(this.lblAccount, this.txtAccount);
		this.lblItmState.setSizeUndefined();
		this.lblItmState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblItmState, this.comboBoxState);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("100px");
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem7,
			this.formItem8,
			this.formItem9, this.formItem10, this.formItem6, this.horizontalLayout3);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.formLayout);
		this.splitLayout.setSplitterPosition(60.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();
		
		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Grid<Item>                 grid;
	private ComboBox<Vat>              cmbVat;
	private VerticalLayout             verticalLayout;
	private HorizontalLayout           horizontalLayout2, horizontalLayout3;
	private Label                      lblItmIdent, lblItmName, lblItmPrice1, lblItmPrice2, lblItmState, lblItmUnit,
		lblItemGroup, lblVat, lblAccount;
	private FilterComponent            filterComponent;
	private FormItem                   formItem2, formItem, formItem3, formItem4, formItem6, formItem7, formItem8,
		formItem9, formItem10;
	private FormLayout                 formLayout;
	private BeanValidationBinder<Item> binder;
	private Button                     cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private ComboBox<State>            comboBoxState;
	private SplitLayout                splitLayout;
	private ComboBox<Unit>             cbxUnit;
	private ComboBox<ItemGroup>        cmbItemGroup;
	private TextField                  txtItmIdent, txtItmName, txtItmPrice1, txtItmPrice2, txtAccount;
	// </generated-code>
	
}
