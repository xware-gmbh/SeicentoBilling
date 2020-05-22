package ch.xwr.seicentobilling.ui.desktop.crm;

import java.util.Date;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.Address_;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;

public class AddressPopup extends XdevView {

	/**
	 *
	 */
	public AddressPopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxType.addItems((Object[]) LovCrm.AddressType.values());
		this.comboBoxSalutation.addItems((Object[]) LovCrm.Salutation.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		Address bean = null;
		Customer obj = null;

		if (beanId == null) {

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if (beanCsa == null) {
				beanCsa = new CostAccountDAO().findAll().get(0);	//Dev Mode
			}
			final CustomerDAO cusDao = new CustomerDAO();
			obj = cusDao.find(objId);


			bean = new Address();
			bean.setAdrState(LovState.State.active);
			bean.setCustomer(obj);
			//act.setCostAccount(beanCsa);
			bean.setAdrValidFrom(new Date());
			bean.setAdrType(LovCrm.AddressType.business);


		} else {
			final AddressDAO dao = new AddressDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);

	}

	private void setBeanGui(final Address bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// set RO Fields
		setROFields();

		//postLoadAccountAction(bean);
		//this.txtExpText.focus();
	}


	private void setROFields() {
//		this.dateExpBooked.setEnabled(false);
//		this.cmbPeriode.setEnabled(false);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("920");
		//win.setHeight("610");
		win.center();
		win.setModal(true);
		win.setContent(new AddressPopup());

		return win;
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
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getAdrId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevTextField}
	 * {@link #txtAdrZip}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtAdrZip_valueChange(final Property.ValueChangeEvent event) {
		//System.out.println("value change");
		final String val  = (String) event.getProperty().getValue();
		if (val != null && val.length() > 3) {
			String ctyname = null;
			if (this.txtAdrCity.getValue() != null) {
				ctyname = this.txtAdrCity.getValue();
			}
			if (ctyname == null || ctyname.length() < 1) {
				final CityDAO dao = new CityDAO();
				final List<City> ls = dao.findByZip(Integer.parseInt(val));
				if (ls != null && ls.size() > 0) {
					final City b2 = ls.get(0);
					this.txtAdrCity.setValue(b2.getCtyName());
					this.txtAdrCountry.setValue(b2.getCtyCountry());
					this.txtAdrRegion.setValue(b2.getCtyRegion());
				}
			}

		}

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.comboBoxType = new XdevComboBox<>();
		this.comboBoxState = new XdevComboBox<>();
		this.lblAdrValidFrom = new XdevLabel();
		this.dateAdrValidFrom = new XdevPopupDateField();
		this.lblAdrIndex = new XdevLabel();
		this.txtAdrIndex = new XdevTextField();
		this.lblAdrType = new XdevLabel();
		this.lblAdrSalutation = new XdevLabel();
		this.comboBoxSalutation = new XdevComboBox<>();
		this.lblName = new XdevLabel();
		this.txtAdrName = new XdevTextField();
		this.lblAddOn = new XdevLabel();
		this.txtAdrAddOn = new XdevTextField();
		this.lblAdrLine = new XdevLabel();
		this.txtAdrLine = new XdevTextField();
		this.lblAdrLine1 = new XdevLabel();
		this.txtAdrLine1 = new XdevTextField();
		this.lblAdrZip = new XdevLabel();
		this.txtAdrZip = new XdevTextField();
		this.lblAdrCity = new XdevLabel();
		this.txtAdrCity = new XdevTextField();
		this.lblAdrCountry = new XdevLabel();
		this.txtAdrCountry = new XdevTextField();
		this.lblAdrRegion = new XdevLabel();
		this.txtAdrRegion = new XdevTextField();
		this.lblAdrRemark = new XdevLabel();
		this.txtAdrRemark = new XdevTextField();
		this.lblAdrState = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Address.class);

		this.panel.setCaption("Adressen");
		this.panel.setTabIndex(0);
		this.comboBoxState.setRequired(true);
		this.lblAdrValidFrom.setValue("Gültig ab");
		this.lblAdrIndex.setValue("Index");
		this.lblAdrType.setValue("Type");
		this.lblAdrSalutation.setValue("Anrede");
		this.lblName.setValue("Name");
		this.txtAdrName.setMaxLength(50);
		this.lblAddOn.setValue("Zusatz");
		this.txtAdrAddOn.setMaxLength(50);
		this.lblAdrLine.setValue("Adresse 1");
		this.txtAdrLine.setMaxLength(50);
		this.lblAdrLine1.setValue("Adresse 2");
		this.txtAdrLine1.setMaxLength(50);
		this.lblAdrZip.setValue("Plz");
		this.txtAdrZip.setMaxLength(50);
		this.lblAdrCity.setValue("City");
		this.txtAdrCity.setMaxLength(50);
		this.lblAdrCountry.setValue("Land");
		this.txtAdrCountry.setMaxLength(50);
		this.lblAdrRegion.setValue("Region");
		this.txtAdrRegion.setMaxLength(50);
		this.lblAdrRemark.setValue("Bemerkung");
		this.txtAdrRemark.setMaxLength(50);
		this.lblAdrState.setValue("State");
		this.horizontalLayout.setMargin(new MarginInfo(true, false, true, false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.CLOSE);
		this.cmdReset.setCaption("Abbrechen");
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.dateAdrValidFrom, Address_.adrValidFrom.getName());
		this.fieldGroup.bind(this.txtAdrIndex, Address_.adrIndex.getName());
		this.fieldGroup.bind(this.comboBoxType, Address_.adrType.getName());
		this.fieldGroup.bind(this.txtAdrLine, Address_.adrLine0.getName());
		this.fieldGroup.bind(this.txtAdrLine1, Address_.adrLine1.getName());
		this.fieldGroup.bind(this.txtAdrZip, Address_.adrZip.getName());
		this.fieldGroup.bind(this.txtAdrCity, Address_.adrCity.getName());
		this.fieldGroup.bind(this.txtAdrCountry, Address_.adrCountry.getName());
		this.fieldGroup.bind(this.txtAdrRegion, Address_.adrRegion.getName());
		this.fieldGroup.bind(this.txtAdrRemark, Address_.adrRemark.getName());
		this.fieldGroup.bind(this.comboBoxState, Address_.adrState.getName());
		this.fieldGroup.bind(this.comboBoxSalutation, Address_.adrSalutation.getName());
		this.fieldGroup.bind(this.txtAdrName, Address_.adrName.getName());
		this.fieldGroup.bind(this.txtAdrAddOn, Address_.adrAddOn.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(4);
		this.form.setRows(11);
		this.comboBoxType.setSizeUndefined();
		this.form.addComponent(this.comboBoxType, 1, 1);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 8);
		this.lblAdrValidFrom.setSizeUndefined();
		this.form.addComponent(this.lblAdrValidFrom, 0, 0);
		this.dateAdrValidFrom.setSizeUndefined();
		this.form.addComponent(this.dateAdrValidFrom, 1, 0);
		this.lblAdrIndex.setSizeUndefined();
		this.form.addComponent(this.lblAdrIndex, 2, 0);
		this.txtAdrIndex.setSizeUndefined();
		this.form.addComponent(this.txtAdrIndex, 3, 0);
		this.lblAdrType.setSizeUndefined();
		this.form.addComponent(this.lblAdrType, 0, 1);
		this.lblAdrSalutation.setSizeUndefined();
		this.form.addComponent(this.lblAdrSalutation, 2, 1);
		this.comboBoxSalutation.setSizeUndefined();
		this.form.addComponent(this.comboBoxSalutation, 3, 1);
		this.lblName.setSizeUndefined();
		this.form.addComponent(this.lblName, 0, 2);
		this.txtAdrName.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrName, 1, 2);
		this.lblAddOn.setSizeUndefined();
		this.form.addComponent(this.lblAddOn, 2, 2);
		this.txtAdrAddOn.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrAddOn.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrAddOn, 3, 2);
		this.lblAdrLine.setSizeUndefined();
		this.form.addComponent(this.lblAdrLine, 0, 3);
		this.txtAdrLine.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrLine.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrLine, 1, 3);
		this.lblAdrLine1.setSizeUndefined();
		this.form.addComponent(this.lblAdrLine1, 0, 4);
		this.txtAdrLine1.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrLine1.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrLine1, 1, 4);
		this.lblAdrZip.setSizeUndefined();
		this.form.addComponent(this.lblAdrZip, 0, 5);
		this.txtAdrZip.setSizeUndefined();
		this.form.addComponent(this.txtAdrZip, 1, 5);
		this.lblAdrCity.setSizeUndefined();
		this.form.addComponent(this.lblAdrCity, 2, 5);
		this.txtAdrCity.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrCity.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrCity, 3, 5);
		this.lblAdrCountry.setSizeUndefined();
		this.form.addComponent(this.lblAdrCountry, 0, 6);
		this.txtAdrCountry.setSizeUndefined();
		this.form.addComponent(this.txtAdrCountry, 1, 6);
		this.lblAdrRegion.setSizeUndefined();
		this.form.addComponent(this.lblAdrRegion, 2, 6);
		this.txtAdrRegion.setSizeUndefined();
		this.form.addComponent(this.txtAdrRegion, 3, 6);
		this.lblAdrRemark.setSizeUndefined();
		this.form.addComponent(this.lblAdrRemark, 0, 7);
		this.txtAdrRemark.setWidth(100, Unit.PERCENTAGE);
		this.txtAdrRemark.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtAdrRemark, 1, 7);
		this.lblAdrState.setSizeUndefined();
		this.form.addComponent(this.lblAdrState, 0, 8);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 1, 9);
		this.form.setColumnExpandRatio(1, 100.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 10, 3, 10);
		this.form.setRowExpandRatio(10, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(920, Unit.PIXELS);
		this.setHeight(615, Unit.PIXELS);

		this.txtAdrZip.addValueChangeListener(event -> this.txtAdrZip_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblAdrValidFrom, lblAdrIndex, lblAdrType, lblAdrSalutation, lblName, lblAddOn, lblAdrLine,
			lblAdrLine1, lblAdrZip, lblAdrCity, lblAdrCountry, lblAdrRegion, lblAdrRemark, lblAdrState;
	private XdevButton cmdSave, cmdReset;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPopupDateField dateAdrValidFrom;
	private XdevComboBox<?> comboBoxType, comboBoxState, comboBoxSalutation;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtAdrIndex, txtAdrName, txtAdrAddOn, txtAdrLine, txtAdrLine1, txtAdrZip, txtAdrCity,
			txtAdrCountry, txtAdrRegion, txtAdrRemark;
	private XdevFieldGroup<Address> fieldGroup;
	// </generated-code>

}
