package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.OrderCalculator;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.dal.VatLineDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.OrderLine_;
import ch.xwr.seicentobilling.entities.Order_;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;

public class OrderLinePopup extends XdevView {
	OrderCalculator CALC = new OrderCalculator();

	/**
	 *
	 */
	public OrderLinePopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		OrderLine bean = null;
		Order obj = null;

		if (beanId == null) {
			// new
			final OrderDAO objDao = new OrderDAO();
			obj = objDao.find(objId);

			bean = new OrderLine();
			bean.setOrderhdr(obj);
			bean.setOdlState(LovState.State.active);
			bean.setOdlQuantity(1);
			bean.setOdlNumber(this.CALC.getNextLineNumber(obj));

		} else {
			final OrderLineDAO dao = new OrderLineDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);
		setROFields();
	}

	private void setROFields() {
		this.txtOdlAmountBrut.setEnabled(false);
		this.txtOdlAmountNet.setEnabled(false);
		this.txtOdlVatAmount.setEnabled(false);
		this.cmbOrder.setEnabled(false);

	}

	private void setBeanGui(final OrderLine bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// this.comboBoxObject.setEnabled(false);
		// this.textFieldName.setEnabled(false);
		// this.textFieldMime.setEnabled(false);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("720");
		win.setHeight("540");
		win.center();
		win.setModal(true);
		win.setContent(new OrderLinePopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		try {
			if (!this.fieldGroup.isValid()){
				final Collection<Field<?>> c2 = this.fieldGroup.getFields();
				for (final Iterator<Field<?>> iterator = c2.iterator(); iterator.hasNext();) {
					final Field<?> object = iterator.next();
					final String name = (String) this.fieldGroup.getPropertyId(object);
					try {
						object.validate();
					} catch (final InvalidValueException e) {
						Notification.show("Ungültiger Wert in Feld " + name, "Message" + e.getMessage(), Notification.Type.ERROR_MESSAGE);
						e.printStackTrace();
					} catch (final Exception e) {
						Notification.show("Fehler beim Speichern " + object.getCaption(), e.getMessage(), Notification.Type.ERROR_MESSAGE);
					}
				}

				return;
			}
			this.fieldGroup.save();

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getOdlId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbItem}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbItem_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}
		// if (event.getProperty().)
		final Item itm = (Item) event.getProperty().getValue();

		if (itm.getItmPrice1() != null) {
			this.txtOdlPrice.setValue(itm.getItmPrice1().toString());
		}
		if (itm.getVat() != null) {
			this.cmbVat.setValue(itm.getVat());
		}

		// loadDataFromEmp(emp);
	}

	/**
	 * Event handler delegate method for the {@link XdevTextField}
	 * {@link #txtOdlQuantity}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtOdlQuantity_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}

		calculateItem();
	}

	private void calculateItem() {
		this.CALC.commitFields(this.fieldGroup);
		final OrderLine newodl = this.CALC.calculateLine(this.fieldGroup.getItemDataSource().getBean());

		this.fieldGroup.setItemDataSource(newodl);
		setROFields();

	}

	/**
	 * Event handler delegate method for the {@link XdevTextField}
	 * {@link #txtOdlPrice}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtOdlPrice_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}

		calculateItem();
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbVat}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbVat_valueChange(final Property.ValueChangeEvent event) {
		if (!this.fieldGroup.isModified()) {
			return;
		}

		calculateItem();
		fetchVatRate();
	}

	private void fetchVatRate() {
		final OrderLine newodl = this.fieldGroup.getItemDataSource().getBean();

    	//TFS-240
    	final VatLineDAO dao = new VatLineDAO();
    	final List<VatLine> lst = dao.findByVatAndDate(newodl.getVat(), newodl.getOrderhdr().getOrdBillDate());
    	double rate = 0;
    	if (lst == null || lst.isEmpty()) {
    		rate = 0;
    	} else {
        	final VatLine vl = lst.get(0);
        	rate = vl.getVanRate().doubleValue();
    	}
    	this.lblVat.setValue("MwSt " + rate + "%");

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.form = new XdevGridLayout();
		this.comboBoxState = new XdevComboBox<>();
		this.lblOrder = new XdevLabel();
		this.cmbOrder = new XdevComboBox<>();
		this.lblOdlNumber = new XdevLabel();
		this.txtOdlNumber = new XdevTextField();
		this.lblItem = new XdevLabel();
		this.cmbItem = new XdevComboBox<>();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblOdlQuantity = new XdevLabel();
		this.txtOdlQuantity = new XdevTextField();
		this.lblOdlPrice = new XdevLabel();
		this.txtOdlPrice = new XdevTextField();
		this.lblOdlText = new XdevLabel();
		this.txtOdlText = new XdevTextField();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblOdlAmountBrut = new XdevLabel();
		this.txtOdlAmountBrut = new XdevTextField();
		this.lblOdlVatAmount = new XdevLabel();
		this.txtOdlVatAmount = new XdevTextField();
		this.lblOdlAmountNet = new XdevLabel();
		this.txtOdlAmountNet = new XdevTextField();
		this.lblOdlState = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(OrderLine.class);

		this.comboBoxState.setTabIndex(12);
		this.lblOrder.setValue(StringResourceUtils.optLocalizeString("{$lblOrder.value}", this));
		this.cmbOrder.setTabIndex(1);
		this.cmbOrder.setItemCaptionFromAnnotation(false);
		this.cmbOrder.setContainerDataSource(Order.class, DAOs.get(OrderDAO.class).findAll());
		this.cmbOrder.setItemCaptionPropertyId(Order_.ordNumber.getName());
		this.lblOdlNumber.setValue(StringResourceUtils.optLocalizeString("{$lblOdlNumber.value}", this));
		this.txtOdlNumber.setTabIndex(2);
		this.lblItem.setValue(StringResourceUtils.optLocalizeString("{$lblItem.value}", this));
		this.cmbItem.setTabIndex(3);
		this.cmbItem.setRequired(true);
		this.cmbItem.setItemCaptionFromAnnotation(false);
		this.cmbItem.setContainerDataSource(Item.class, DAOs.get(ItemDAO.class).findAll());
		this.cmbItem.setItemCaptionPropertyId("prpShortName");
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setTabIndex(4);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAll());
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblOdlQuantity.setValue(StringResourceUtils.optLocalizeString("{$lblOdlQuantity.value}", this));
		this.txtOdlQuantity
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(1).maximumFractionDigits(2).build());
		this.txtOdlQuantity.setTabIndex(5);
		this.lblOdlPrice.setValue(StringResourceUtils.optLocalizeString("{$lblOdlPrice.value}", this));
		this.txtOdlPrice
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.txtOdlPrice.setTabIndex(6);
		this.lblOdlText.setValue(StringResourceUtils.optLocalizeString("{$lblOdlText.value}", this));
		this.txtOdlText.setTabIndex(7);
		this.txtOdlText
				.addValidator(new StringLengthValidator("Der Text darf maximal 80 Zeichen lang sein!", null, 80, true));
		this.lblCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setTabIndex(8);
		this.cmbCostAccount.setRequired(true);
		this.cmbCostAccount.setItemCaptionFromAnnotation(false);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaName.getName());
		this.lblOdlAmountBrut.setValue(StringResourceUtils.optLocalizeString("{$lblOdlAmountBrut.value}", this));
		this.txtOdlAmountBrut.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtOdlAmountBrut.setTabIndex(9);
		this.lblOdlVatAmount.setValue(StringResourceUtils.optLocalizeString("{$lblOdlVatAmount.value}", this));
		this.txtOdlVatAmount.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtOdlVatAmount.setTabIndex(11);
		this.lblOdlAmountNet.setValue(StringResourceUtils.optLocalizeString("{$lblOdlAmountNet.value}", this));
		this.txtOdlAmountNet.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtOdlAmountNet.setTabIndex(10);
		this.lblOdlState.setValue(StringResourceUtils.optLocalizeString("{$lblOdlState.value}", this));
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(13);
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(14);
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.cmbOrder, OrderLine_.orderhdr.getName());
		this.fieldGroup.bind(this.txtOdlNumber, OrderLine_.odlNumber.getName());
		this.fieldGroup.bind(this.cmbItem, OrderLine_.item.getName());
		this.fieldGroup.bind(this.txtOdlQuantity, OrderLine_.odlQuantity.getName());
		this.fieldGroup.bind(this.cmbCostAccount, OrderLine_.costAccount.getName());
		this.fieldGroup.bind(this.txtOdlPrice, OrderLine_.odlPrice.getName());
		this.fieldGroup.bind(this.cmbVat, OrderLine_.vat.getName());
		this.fieldGroup.bind(this.txtOdlAmountBrut, OrderLine_.odlAmountBrut.getName());
		this.fieldGroup.bind(this.txtOdlAmountNet, OrderLine_.odlAmountNet.getName());
		this.fieldGroup.bind(this.txtOdlText, OrderLine_.odlText.getName());
		this.fieldGroup.bind(this.txtOdlVatAmount, OrderLine_.odlVatAmount.getName());
		this.fieldGroup.bind(this.comboBoxState, OrderLine_.odlState.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_CENTER);
		this.form.setColumns(4);
		this.form.setRows(11);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 8);
		this.lblOrder.setSizeUndefined();
		this.form.addComponent(this.lblOrder, 0, 0);
		this.cmbOrder.setWidth(100, Unit.PERCENTAGE);
		this.cmbOrder.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbOrder, 1, 0);
		this.lblOdlNumber.setSizeUndefined();
		this.form.addComponent(this.lblOdlNumber, 0, 1);
		this.txtOdlNumber.setSizeUndefined();
		this.form.addComponent(this.txtOdlNumber, 1, 1);
		this.lblItem.setSizeUndefined();
		this.form.addComponent(this.lblItem, 0, 2);
		this.cmbItem.setWidth(100, Unit.PERCENTAGE);
		this.cmbItem.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbItem, 1, 2);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 2, 2);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbVat, 3, 2);
		this.lblOdlQuantity.setSizeUndefined();
		this.form.addComponent(this.lblOdlQuantity, 0, 3);
		this.txtOdlQuantity.setSizeUndefined();
		this.form.addComponent(this.txtOdlQuantity, 1, 3);
		this.lblOdlPrice.setSizeUndefined();
		this.form.addComponent(this.lblOdlPrice, 2, 3);
		this.txtOdlPrice.setSizeUndefined();
		this.form.addComponent(this.txtOdlPrice, 3, 3);
		this.lblOdlText.setSizeUndefined();
		this.form.addComponent(this.lblOdlText, 0, 4);
		this.txtOdlText.setWidth(100, Unit.PERCENTAGE);
		this.txtOdlText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtOdlText, 1, 4, 3, 4);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 5);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 5);
		this.lblOdlAmountBrut.setSizeUndefined();
		this.form.addComponent(this.lblOdlAmountBrut, 0, 6);
		this.txtOdlAmountBrut.setSizeUndefined();
		this.form.addComponent(this.txtOdlAmountBrut, 1, 6);
		this.lblOdlVatAmount.setSizeUndefined();
		this.form.addComponent(this.lblOdlVatAmount, 2, 6);
		this.txtOdlVatAmount.setSizeUndefined();
		this.form.addComponent(this.txtOdlVatAmount, 3, 6);
		this.lblOdlAmountNet.setSizeUndefined();
		this.form.addComponent(this.lblOdlAmountNet, 0, 7);
		this.txtOdlAmountNet.setSizeUndefined();
		this.form.addComponent(this.txtOdlAmountNet, 1, 7);
		this.lblOdlState.setSizeUndefined();
		this.form.addComponent(this.lblOdlState, 0, 8);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 9, 1, 9);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 10, 3, 10);
		this.form.setRowExpandRatio(10, 1.0F);
		this.form.setSizeFull();
		this.setContent(this.form);
		this.setSizeFull();

		this.cmbItem.addValueChangeListener(event -> this.cmbItem_valueChange(event));
		this.cmbVat.addValueChangeListener(event -> this.cmbVat_valueChange(event));
		this.txtOdlQuantity.addValueChangeListener(event -> this.txtOdlQuantity_valueChange(event));
		this.txtOdlPrice.addValueChangeListener(event -> this.txtOdlPrice_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevComboBox<Item> cmbItem;
	private XdevLabel lblOrder, lblOdlNumber, lblItem, lblVat, lblOdlQuantity, lblOdlPrice, lblOdlText, lblCostAccount,
			lblOdlAmountBrut, lblOdlVatAmount, lblOdlAmountNet, lblOdlState;
	private XdevButton cmdSave, cmdReset;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevComboBox<Vat> cmbVat;
	private XdevHorizontalLayout horizontalLayout;
	private XdevComboBox<?> comboBoxState;
	private XdevGridLayout form;
	private XdevComboBox<Order> cmbOrder;
	private XdevTextField txtOdlNumber, txtOdlQuantity, txtOdlPrice, txtOdlText, txtOdlAmountBrut, txtOdlVatAmount,
			txtOdlAmountNet;
	private XdevFieldGroup<OrderLine> fieldGroup;
	// </generated-code>

}
