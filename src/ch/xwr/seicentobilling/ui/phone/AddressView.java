package ch.xwr.seicentobilling.ui.phone;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.jfree.util.Log;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevLink;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.navigation.Navigation;
import com.xdev.ui.navigation.NavigationParameter;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.CustomerLink_;
import ch.xwr.seicentobilling.entities.Customer_;
import ch.xwr.seicentobilling.ui.desktop.crm.FunctionLinkHyperlink;

public class AddressView extends XdevView {

	@NavigationParameter
	private Customer customer;
	/**
	 *
	 */
	public AddressView() {
		super();
		this.initUI();

		// State
		//this.comboBoxState.addItems((Object[]) LovState.State.values());
//		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
//		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());

		this.panel.getContent().setSizeUndefined();
	}


	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.customer = Navigation.getParameter(event, "Customer", Customer.class);

		if (this.customer != null) {
			this.fieldGroup.setItemDataSource(this.customer);
			//postLoadAccountAction(this.customer);
			setGoogleLink();
			//setTelLink();
		}
	}


//	private void setTelLink() {
//
//		String tel = "";
//		if (this.customer.getCusPhone1() != null ) {
//			tel = this.customer.getCusPhone1().toString().trim();
//		}
//
//		this.linkPhone1.setCaption(tel);
//		if (!tel.isEmpty()) {
//			try {
//				this.linkPhone1.setResource(new ExternalResource(new URL("tel:" + tel)));
//			} catch (final MalformedURLException e) {
//				Log.error(e);
//			}
//		}
//
//		tel = "";
//		if (tel.isEmpty() && this.customer.getCusPhone2() != null) {
//			tel = this.customer.getCusPhone2().toString().trim();
//		}
//		this.linkPhone2.setCaption(tel);
//		if (!tel.isEmpty()) {
//			try {
//				this.linkPhone2.setResource(new ExternalResource(new URL("tel:" + tel)));
//			} catch (final MalformedURLException e) {
//				Log.error(e);
//			}
//		}
//
//	}

	private void setGoogleLink() {
		final String uripre = "https://www.google.com/maps/search/?api=1&query=";
		String q = this.customer.getCity().getCtyName();
		if (this.customer.getCusAddress() != null && !this.customer.getCusAddress().trim().isEmpty()) {
			q = q + ", " + this.customer.getCusAddress();
		}
		if (this.customer.getCusCompany() != null && !this.customer.getCusCompany().trim().isEmpty()) {
			q = q + ", " + this.customer.getCusCompany();
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

	private void goBack() {
		Navigation.to("addressSearchView").navigate();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #button}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void button_buttonClick(final Button.ClickEvent event) {
		goBack();
	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.form = new XdevGridLayout();
		this.button = new XdevButton();
		this.lblExpAmount = new XdevLabel();
		this.txtCusName = new XdevTextField();
		this.lblCompany = new XdevLabel();
		this.txtCusCompany = new XdevTextField();
		this.lblAddress = new XdevLabel();
		this.txtCusAddress = new XdevTextField();
		this.lblPlace = new XdevLabel();
		this.cmbCity = new XdevComboBox<>();
		this.linkMaps = new XdevLink();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.table = new XdevTable<>();
		this.fieldGroup = new XdevFieldGroup<>(Customer.class);

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.form.setMargin(new MarginInfo(false, false, true, true));
		this.button.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.button.setCaption("Zurück");
		this.lblExpAmount.setValue("Name");
		this.lblCompany.setValue("Firma");
		this.lblAddress.setValue("Adresse");
		this.lblPlace.setValue("Ort");
		this.cmbCity.setItemCaptionFromAnnotation(false);
		this.cmbCity.setContainerDataSource(City.class, DAOs.get(CityDAO.class).findAll());
		this.cmbCity.setItemCaptionPropertyId("fullname");
		this.linkMaps.setTargetName("_blank");
		this.linkMaps.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/gmaps32.PNG"));
		this.linkMaps.setCaption("Google Maps");
		this.linkMaps.setResource(new ExternalResource("https://www.google.com/maps/search/?api=1&query=Sursee"));
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.table.setCaption("Telefon / Mail / Web");
		this.table.setContainerDataSource(CustomerLink.class);
		this.table.addGeneratedColumn("generated", new FunctionLinkHyperlink.Generator());
		this.table.setVisibleColumns(CustomerLink_.cnkLink.getName(), "generated", CustomerLink_.cnkDepartment.getName(),
				CustomerLink_.cnkValidFrom.getName());
		this.table.setColumnHeader("cnkLink", "Wert");
		this.table.setColumnHeader("generated", "Link");
		this.table.setColumnHeader("cnkDepartment", "Bereich");
		this.table.setColumnHeader("cnkValidFrom", "Gültig ab");
		this.table.setConverter("cnkValidFrom", ConverterBuilder.stringToDate().dateOnly().build());
		this.fieldGroup.bind(this.txtCusName, "shortname");
		this.fieldGroup.bind(this.txtCusCompany, Customer_.cusCompany.getName());
		this.fieldGroup.bind(this.txtCusAddress, Customer_.cusAddress.getName());
		this.fieldGroup.bind(this.cmbCity, Customer_.city.getName());

		this.form.setColumns(2);
		this.form.setRows(8);
		this.button.setSizeUndefined();
		this.form.addComponent(this.button, 0, 0, 1, 0);
		this.lblExpAmount.setSizeUndefined();
		this.form.addComponent(this.lblExpAmount, 0, 1);
		this.txtCusName.setSizeFull();
		this.form.addComponent(this.txtCusName, 1, 1);
		this.lblCompany.setSizeUndefined();
		this.form.addComponent(this.lblCompany, 0, 2);
		this.txtCusCompany.setSizeFull();
		this.form.addComponent(this.txtCusCompany, 1, 2);
		this.lblAddress.setSizeUndefined();
		this.form.addComponent(this.lblAddress, 0, 3);
		this.txtCusAddress.setSizeFull();
		this.form.addComponent(this.txtCusAddress, 1, 3);
		this.lblPlace.setSizeUndefined();
		this.form.addComponent(this.lblPlace, 0, 4);
		this.cmbCity.setSizeFull();
		this.form.addComponent(this.cmbCity, 1, 4);
		this.linkMaps.setWidth(100, Unit.PERCENTAGE);
		this.linkMaps.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.linkMaps, 0, 5, 1, 5);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 6);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.TOP_CENTER);
		this.table.setWidth(100, Unit.PERCENTAGE);
		this.table.setHeight(300, Unit.PIXELS);
		this.form.addComponent(this.table, 0, 7, 1, 7);
		this.form.setColumnExpandRatio(1, 10.0F);
		this.form.setRowExpandRatio(7, 50.0F);
		this.form.setWidth(100, Unit.PERCENTAGE);
		this.form.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.form);
		this.verticalLayout.setComponentAlignment(this.form, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setSizeUndefined();
		this.panel.setContent(this.verticalLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.button.addClickListener(event -> this.button_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton button;
	private XdevLabel lblExpAmount, lblCompany, lblAddress, lblPlace;
	private XdevFieldGroup<Customer> fieldGroup;
	private XdevComboBox<City> cmbCity;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtCusName, txtCusCompany, txtCusAddress;
	private XdevLink linkMaps;
	private XdevVerticalLayout verticalLayout;
	private XdevTable<CustomerLink> table;
	// </generated-code>

}
