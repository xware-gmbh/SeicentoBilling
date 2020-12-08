
package ch.xwr.seicentobilling.ui;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.OrderCalculator;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.dal.VatLineDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;


public class OrderLinePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG  = LoggerFactory.getLogger(OrderLinePopup.class);
	OrderCalculator             CALC = new OrderCalculator();

	/**
	 *
	 */
	public OrderLinePopup()
	{
		super();
		this.initUI();
		
		// State
		this.comboBoxState.setItems(LovState.State.values());
		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());
		
		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		OrderLine  bean   = null;
		Order      obj    = null;
		
		if(beanId == null)
		{
			// new
			final OrderDAO objDao = new OrderDAO();
			obj = objDao.find(objId);
			
			bean = new OrderLine();
			bean.setOrderhdr(obj);
			bean.setOdlState(LovState.State.active);
			bean.setOdlQuantity(1);
			bean.setOdlNumber(this.CALC.getNextLineNumber(obj));
			
		}
		else
		{
			final OrderLineDAO dao = new OrderLineDAO();
			bean = dao.find(beanId.longValue());
		}
		
		this.setBeanGui(bean);
		this.setROFields();

	}

	private void setBeanGui(final OrderLine bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);
		
	}

	private void setROFields()
	{
		this.txtOdlAmountBrut.setEnabled(false);
		this.txtOdlAmountNet.setEnabled(false);
		this.txtOdlVatAmount.setEnabled(false);
		this.cmbOrder.setEnabled(false);
		
	}

	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		win.setSizeFull();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new OrderLinePopup());
		return win;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCancel}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_onClick(final ClickEvent<Button> event)
	{
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
		((Dialog)this.getParent().get()).close();
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		if(SeicentoCrud.doSave(this.binder, new OrderLineDAO()))
		{
			try
			{
				if(!this.binder.isValid())
				{
					SeicentoCrud.validateField(this.binder);
					return;
				}

				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getOdlId(),
					this.binder.getBean().getClass().getSimpleName());
				
				UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
				UI.getCurrent().getSession().setAttribute("beanId",
					this.binder.getBean().getOdlId());
				
				((Dialog)this.getParent().get()).close();
				// Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				OrderLinePopup.LOG.error("could not save ObjRoot", e);
			}
		}

	}
	
	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbItem}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbItem_valueChanged(final ComponentValueChangeEvent<ComboBox<Item>, Item> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}
		// if (event.getProperty().)
		final Item itm = event.getValue();

		if(itm.getItmPrice1() != null)
		{
			this.txtOdlPrice.setValue(itm.getItmPrice1().toString());
		}
		if(itm.getVat() != null)
		{
			this.cmbVat.setValue(itm.getVat());
		}
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbVat}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbVat_valueChanged(final ComponentValueChangeEvent<ComboBox<Vat>, Vat> event)
	{
		if(event.getValue() == null)
		{
			return;
		}
		
		this.calculateItem();
		this.fetchVatRate();
	}
	
	private void fetchVatRate()
	{
		final OrderLine newodl = this.binder.getBean();

		// TFS-240
		final VatLineDAO    dao  = new VatLineDAO();
		final List<VatLine> lst  = dao.findByVatAndDate(newodl.getVat(), newodl.getOrderhdr().getOrdBillDate());
		double              rate = 0;
		if(lst == null || lst.isEmpty())
		{
			rate = 0;
		}
		else
		{
			final VatLine vl = lst.get(0);
			rate = vl.getVanRate().doubleValue();
		}
		this.lblVat.setText("MwSt " + rate + "%");

	}

	/**
	 * Event handler delegate method for the {@link TextField} {@link #txtOdlQuantity}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtOdlQuantity_valueChanged(final ComponentValueChangeEvent<TextField, String> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}
		
		this.calculateItem();
	}
	
	private void calculateItem()
	{
		final OrderLine newodl = this.CALC.calculateLine(this.binder.getBean());

		this.binder.setBean(newodl);
		this.setROFields();

	}

	/**
	 * Event handler delegate method for the {@link TextField} {@link #txtOdlPrice}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtOdlPrice_valueChanged(final ComponentValueChangeEvent<TextField, String> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}
		
		this.calculateItem();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout    = new VerticalLayout();
		this.horizontalLayout  = new HorizontalLayout();
		this.icon              = new Icon(VaadinIcon.CLOCK);
		this.label             = new Label();
		this.formLayout        = new FormLayout();
		this.formItem2         = new FormItem();
		this.lblOrder          = new Label();
		this.cmbOrder          = new ComboBox<>();
		this.formItem          = new FormItem();
		this.lblOdlNumber      = new Label();
		this.txtOdlNumber      = new TextField();
		this.formItem4         = new FormItem();
		this.lblItem           = new Label();
		this.cmbItem           = new ComboBox<>();
		this.formItem8         = new FormItem();
		this.lblVat            = new Label();
		this.cmbVat            = new ComboBox<>();
		this.formItem3         = new FormItem();
		this.lblOdlQuantity    = new Label();
		this.txtOdlQuantity    = new TextField();
		this.formItem6         = new FormItem();
		this.lblOdlPrice       = new Label();
		this.txtOdlPrice       = new TextField();
		this.formItem7         = new FormItem();
		this.lblOdlText        = new Label();
		this.txtOdlText        = new TextField();
		this.formItem9         = new FormItem();
		this.lblCostAccount    = new Label();
		this.cmbCostAccount    = new ComboBox<>();
		this.formItem11        = new FormItem();
		this.lblOdlAmountBrut  = new Label();
		this.txtOdlAmountBrut  = new TextField();
		this.formItem10        = new FormItem();
		this.lblOdlVatAmount   = new Label();
		this.txtOdlVatAmount   = new TextField();
		this.formItem12        = new FormItem();
		this.lblOdlAmountNet   = new Label();
		this.txtOdlAmountNet   = new TextField();
		this.formItem5         = new FormItem();
		this.lblOdlState       = new Label();
		this.comboBoxState     = new ComboBox<>();
		this.horizontalLayout2 = new HorizontalLayout();
		this.cmdSave           = new Button();
		this.cmdCancel         = new Button();
		this.binder            = new BeanValidationBinder<>(OrderLine.class);
		
		this.label.setText("OrderLine bearbeiten");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblOrder.setText(StringResourceUtils.optLocalizeString("{$lblOrder.value}", this));
		this.cmbOrder.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbOrder::getItemLabelGenerator),
			DataProvider.ofCollection(new OrderDAO().findAll()));
		this.cmbOrder
			.setItemLabelGenerator(
				ItemLabelGeneratorFactory.NonNull(v -> CaptionUtils.resolveCaption(v, "{%ordNumber}")));
		this.lblOdlNumber.setText(StringResourceUtils.optLocalizeString("{$lblOdlNumber.value}", this));
		this.lblItem.setText(StringResourceUtils.optLocalizeString("{$lblItem.value}", this));
		this.cmbItem.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbItem::getItemLabelGenerator),
			DataProvider.ofCollection(new ItemDAO().findAll()));
		this.cmbItem.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Item::getPrpShortName));
		this.lblVat.setText(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAllActive()));
		this.cmbVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.lblOdlQuantity.setText(StringResourceUtils.optLocalizeString("{$lblOdlQuantity.value}", this));
		this.lblOdlPrice.setText(StringResourceUtils.optLocalizeString("{$lblOdlPrice.value}", this));
		this.lblOdlText.setText(StringResourceUtils.optLocalizeString("{$lblOdlText.value}", this));
		this.lblCostAccount.setText(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaName));
		this.lblOdlAmountBrut.setText(StringResourceUtils.optLocalizeString("{$lblOdlAmountBrut.value}", this));
		this.lblOdlVatAmount.setText(StringResourceUtils.optLocalizeString("{$lblOdlVatAmount.value}", this));
		this.lblOdlAmountNet.setText("lblOdlAmountNet");
		this.lblOdlState.setText(StringResourceUtils.optLocalizeString("{$lblOdlState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdCancel.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdCancel.setIcon(IronIcons.CANCEL.create());
		
		this.binder.forField(this.cmbOrder).bind("orderhdr");
		this.binder.forField(this.txtOdlNumber).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("odlNumber");
		this.binder.forField(this.cmbItem).bind("item");
		this.binder.forField(this.cmbVat).bind("vat");
		this.binder.forField(this.txtOdlQuantity).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("odlQuantity");
		this.binder.forField(this.txtOdlPrice).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("odlPrice");
		this.binder.forField(this.txtOdlText).withNullRepresentation("").bind("odlText");
		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.txtOdlAmountBrut).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("odlAmountBrut");
		this.binder.forField(this.txtOdlVatAmount).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("odlVatAmount");
		this.binder.forField(this.txtOdlAmountNet).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(
				NumberFormatBuilder.Currency().locale(Locale.forLanguageTag("de-CH"))
					.currency(Currency.getInstance("CHF")))
				.build())
			.bind("odlAmountNet");
		this.binder.forField(this.comboBoxState).bind("odlState");
		
		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblOrder.setSizeUndefined();
		this.lblOrder.getElement().setAttribute("slot", "label");
		this.cmbOrder.setWidthFull();
		this.cmbOrder.setHeight(null);
		this.formItem2.add(this.lblOrder, this.cmbOrder);
		this.lblOdlNumber.setSizeUndefined();
		this.lblOdlNumber.getElement().setAttribute("slot", "label");
		this.txtOdlNumber.setWidthFull();
		this.txtOdlNumber.setHeight(null);
		this.formItem.add(this.lblOdlNumber, this.txtOdlNumber);
		this.lblItem.setSizeUndefined();
		this.lblItem.getElement().setAttribute("slot", "label");
		this.cmbItem.setWidthFull();
		this.cmbItem.setHeight(null);
		this.formItem4.add(this.lblItem, this.cmbItem);
		this.lblVat.setSizeUndefined();
		this.lblVat.getElement().setAttribute("slot", "label");
		this.cmbVat.setWidthFull();
		this.cmbVat.setHeight(null);
		this.formItem8.add(this.lblVat, this.cmbVat);
		this.lblOdlQuantity.setSizeUndefined();
		this.lblOdlQuantity.getElement().setAttribute("slot", "label");
		this.txtOdlQuantity.setWidthFull();
		this.txtOdlQuantity.setHeight(null);
		this.formItem3.add(this.lblOdlQuantity, this.txtOdlQuantity);
		this.lblOdlPrice.setSizeUndefined();
		this.lblOdlPrice.getElement().setAttribute("slot", "label");
		this.txtOdlPrice.setWidthFull();
		this.txtOdlPrice.setHeight(null);
		this.formItem6.add(this.lblOdlPrice, this.txtOdlPrice);
		this.lblOdlText.setSizeUndefined();
		this.lblOdlText.getElement().setAttribute("slot", "label");
		this.txtOdlText.setWidthFull();
		this.txtOdlText.setHeight(null);
		this.formItem7.add(this.lblOdlText, this.txtOdlText);
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setWidthFull();
		this.cmbCostAccount.setHeight(null);
		this.formItem9.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblOdlAmountBrut.setSizeUndefined();
		this.lblOdlAmountBrut.getElement().setAttribute("slot", "label");
		this.txtOdlAmountBrut.setWidthFull();
		this.txtOdlAmountBrut.setHeight(null);
		this.formItem11.add(this.lblOdlAmountBrut, this.txtOdlAmountBrut);
		this.lblOdlVatAmount.setSizeUndefined();
		this.lblOdlVatAmount.getElement().setAttribute("slot", "label");
		this.txtOdlVatAmount.setWidthFull();
		this.txtOdlVatAmount.setHeight(null);
		this.formItem10.add(this.lblOdlVatAmount, this.txtOdlVatAmount);
		this.lblOdlAmountNet.setSizeUndefined();
		this.lblOdlAmountNet.getElement().setAttribute("slot", "label");
		this.txtOdlAmountNet.setWidthFull();
		this.txtOdlAmountNet.setHeight(null);
		this.formItem12.add(this.lblOdlAmountNet, this.txtOdlAmountNet);
		this.lblOdlState.setSizeUndefined();
		this.lblOdlState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem5.add(this.lblOdlState, this.comboBoxState);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem4, this.formItem8, this.formItem3,
			this.formItem6,
			this.formItem7, this.formItem9, this.formItem11, this.formItem10, this.formItem12, this.formItem5);
		this.cmdSave.setSizeUndefined();
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdSave, this.cmdCancel);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();
		
		this.cmbItem.addValueChangeListener(this::cmbItem_valueChanged);
		this.cmbVat.addValueChangeListener(this::cmbVat_valueChanged);
		this.txtOdlQuantity.addValueChangeListener(this::txtOdlQuantity_valueChanged);
		this.txtOdlPrice.addValueChangeListener(this::txtOdlPrice_valueChanged);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdCancel.addClickListener(this::cmdCancel_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private BeanValidationBinder<OrderLine> binder;
	private ComboBox<Vat>                   cmbVat;
	private ComboBox<Item>                  cmbItem;
	private VerticalLayout                  verticalLayout;
	private HorizontalLayout                horizontalLayout, horizontalLayout2;
	private Label                           label, lblOrder, lblOdlNumber, lblItem, lblVat, lblOdlQuantity, lblOdlPrice,
		lblOdlText, lblCostAccount, lblOdlAmountBrut, lblOdlVatAmount, lblOdlAmountNet, lblOdlState;
	private FormItem                        formItem2, formItem, formItem4, formItem8, formItem3, formItem6, formItem7,
		formItem9, formItem11, formItem10, formItem12, formItem5;
	private FormLayout                      formLayout;
	private Button                          cmdSave, cmdCancel;
	private ComboBox<State>                 comboBoxState;
	private ComboBox<Order>                 cmbOrder;
	private Icon                            icon;
	private TextField                       txtOdlNumber, txtOdlQuantity, txtOdlPrice, txtOdlText, txtOdlAmountBrut,
		txtOdlVatAmount, txtOdlAmountNet;
	private ComboBox<CostAccount>           cmbCostAccount;
	// </generated-code>
	
}
