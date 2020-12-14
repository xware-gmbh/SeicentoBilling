
package ch.xwr.seicentobilling.ui.crm;

import java.util.Date;
import java.util.List;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovCrm.AddressType;
import ch.xwr.seicentobilling.business.LovCrm.Salutation;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


public class AddressPopup extends VerticalLayout
{

	public AddressPopup()
	{
		super();
		this.initUI();

		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxType.setItems(LovCrm.AddressType.values());
		this.comboBoxSalutation.setItems(LovCrm.Salutation.values());
		
		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		Address    bean   = null;
		Customer   obj    = null;

		if(beanId == null)
		{

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if(beanCsa == null)
			{
				beanCsa = new CostAccountDAO().findAll().get(0); // Dev Mode
			}
			final CustomerDAO cusDao = new CustomerDAO();
			obj = cusDao.find(objId);

			bean = new Address();
			bean.setAdrState(LovState.State.active);
			bean.setCustomer(obj);
			// act.setCostAccount(beanCsa);
			bean.setAdrValidFrom(new Date());
			bean.setAdrType(LovCrm.AddressType.business);

		}
		else
		{
			final AddressDAO dao = new AddressDAO();
			bean = dao.find(beanId.longValue());
		}

		this.setBeanGui(bean);

	}

	private void setBeanGui(final Address bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);

		// set RO Fields
		this.setROFields();

		// postLoadAccountAction(bean);
		// this.txtExpText.focus();
	}

	private void setROFields()
	{
		// this.dateExpBooked.setEnabled(false);
		// this.cmbPeriode.setEnabled(false);
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
		win.add(cancelButton, new AddressPopup());
		return win;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
		this.binder.removeBean();
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
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		if(SeicentoCrud.doSave(this.binder, new AddressDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getAdrId(),
					this.binder.getBean().getClass().getSimpleName());

				((Dialog)this.getParent().get()).close();
				SeicentoNotification.showInfo("Daten wurden gespeichert");
			}
			catch(final Exception e)
			{
				SeicentoNotification.showError(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Event handler delegate method for the {@link TextField} {@link #txtAdrZip}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void txtAdrZip_valueChanged(final ComponentValueChangeEvent<TextField, String> event)
	{
		// System.out.println("value change");
		final String val = event.getValue();
		if(val != null && val.length() > 3)
		{
			String ctyname = null;
			if(this.txtAdrCity.getValue() != null)
			{
				ctyname = this.txtAdrCity.getValue();
			}
			if(ctyname == null || ctyname.length() < 1)
			{
				final CityDAO    dao = new CityDAO();
				final List<City> ls  = dao.findByZip(Integer.parseInt(val));
				if(ls != null && ls.size() > 0)
				{
					final City b2 = ls.get(0);
					this.txtAdrCity.setValue(b2.getCtyName());
					this.txtAdrCountry.setValue(b2.getCtyCountry());
					this.txtAdrRegion.setValue(b2.getCtyRegion());
				}
			}

		}

	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout     = new VerticalLayout();
		this.horizontalLayout   = new HorizontalLayout();
		this.label              = new Label();
		this.formLayout         = new FormLayout();
		this.formItem           = new FormItem();
		this.lblAdrValidFrom    = new Label();
		this.dateAdrValidFrom   = new DatePicker();
		this.formItem5          = new FormItem();
		this.lblAdrIndex        = new Label();
		this.txtAdrIndex        = new TextField();
		this.formItem2          = new FormItem();
		this.lblAdrType         = new Label();
		this.comboBoxType       = new ComboBox<>();
		this.formItem3          = new FormItem();
		this.lblAdrSalutation   = new Label();
		this.comboBoxSalutation = new ComboBox<>();
		this.formItem4          = new FormItem();
		this.lblName            = new Label();
		this.txtAdrName         = new TextField();
		this.formItem6          = new FormItem();
		this.lblAddOn           = new Label();
		this.txtAdrAddOn        = new TextField();
		this.formItem7          = new FormItem();
		this.lblAdrLine         = new Label();
		this.txtAdrLine         = new TextField();
		this.formItem8          = new FormItem();
		this.lblAdrLine1        = new Label();
		this.txtAdrLine1        = new TextField();
		this.formItem9          = new FormItem();
		this.lblAdrZip          = new Label();
		this.txtAdrZip          = new TextField();
		this.formItem10         = new FormItem();
		this.lblAdrCity         = new Label();
		this.txtAdrCity         = new TextField();
		this.formItem11         = new FormItem();
		this.lblAdrCountry      = new Label();
		this.txtAdrCountry      = new TextField();
		this.formItem12         = new FormItem();
		this.lblAdrRegion       = new Label();
		this.txtAdrRegion       = new TextField();
		this.formItem13         = new FormItem();
		this.lblAdrRemark       = new Label();
		this.txtAdrRemark       = new TextField();
		this.formItem14         = new FormItem();
		this.lblAdrState        = new Label();
		this.comboBoxState      = new ComboBox<>();
		this.horizontalLayout2  = new HorizontalLayout();
		this.cmdSave            = new Button();
		this.cmdReset           = new Button();
		this.binder             = new BeanValidationBinder<>(Address.class);

		this.label.setText("Adressen");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblAdrValidFrom.setText("Gültig ab");
		this.lblAdrIndex.setText("Index");
		this.lblAdrType.setText("Type");
		this.comboBoxType.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblAdrSalutation.setText("Anrede");
		this.comboBoxSalutation.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblName.setText("Name");
		this.lblAddOn.setText("Zusatz");
		this.lblAdrLine.setText("Address 1");
		this.lblAdrLine1.setText("Address 2");
		this.lblAdrZip.setText("Plz");
		this.lblAdrCity.setText("City");
		this.lblAdrCountry.setText("Land");
		this.lblAdrRegion.setText("Region");
		this.lblAdrRemark.setText("Bemerkung");
		this.lblAdrState.setText("State");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Abbrechen");
		this.cmdReset.setIcon(IronIcons.CANCEL.create());

		this.binder.forField(this.dateAdrValidFrom)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("adrValidFrom");
		this.binder.forField(this.txtAdrIndex).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToShort().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("adrIndex");
		this.binder.forField(this.comboBoxType).bind("adrType");
		this.binder.forField(this.comboBoxSalutation).bind("adrSalutation");
		this.binder.forField(this.txtAdrName).withNullRepresentation("").bind("adrName");
		this.binder.forField(this.txtAdrAddOn).withNullRepresentation("").bind("adrAddOn");
		this.binder.forField(this.txtAdrLine).withNullRepresentation("").bind("adrLine0");
		this.binder.forField(this.txtAdrLine1).withNullRepresentation("").bind("adrLine1");
		this.binder.forField(this.txtAdrZip).withNullRepresentation("").bind("adrZip");
		this.binder.forField(this.txtAdrCity).withNullRepresentation("").bind("adrCity");
		this.binder.forField(this.txtAdrCountry).withNullRepresentation("").bind("adrCountry");
		this.binder.forField(this.txtAdrRegion).withNullRepresentation("").bind("adrRegion");
		this.binder.forField(this.txtAdrRemark).withNullRepresentation("").bind("adrRemark");
		this.binder.forField(this.comboBoxState).asRequired().bind("adrState");

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblAdrValidFrom.setSizeUndefined();
		this.lblAdrValidFrom.getElement().setAttribute("slot", "label");
		this.dateAdrValidFrom.setWidthFull();
		this.dateAdrValidFrom.setHeight(null);
		this.formItem.add(this.lblAdrValidFrom, this.dateAdrValidFrom);
		this.lblAdrIndex.setSizeUndefined();
		this.lblAdrIndex.getElement().setAttribute("slot", "label");
		this.txtAdrIndex.setWidthFull();
		this.txtAdrIndex.setHeight(null);
		this.formItem5.add(this.lblAdrIndex, this.txtAdrIndex);
		this.lblAdrType.setSizeUndefined();
		this.lblAdrType.getElement().setAttribute("slot", "label");
		this.comboBoxType.setWidthFull();
		this.comboBoxType.setHeight(null);
		this.formItem2.add(this.lblAdrType, this.comboBoxType);
		this.lblAdrSalutation.setSizeUndefined();
		this.lblAdrSalutation.getElement().setAttribute("slot", "label");
		this.comboBoxSalutation.setWidthFull();
		this.comboBoxSalutation.setHeight(null);
		this.formItem3.add(this.lblAdrSalutation, this.comboBoxSalutation);
		this.lblName.setSizeUndefined();
		this.lblName.getElement().setAttribute("slot", "label");
		this.txtAdrName.setWidthFull();
		this.txtAdrName.setHeight(null);
		this.formItem4.add(this.lblName, this.txtAdrName);
		this.lblAddOn.setSizeUndefined();
		this.lblAddOn.getElement().setAttribute("slot", "label");
		this.txtAdrAddOn.setWidthFull();
		this.txtAdrAddOn.setHeight(null);
		this.formItem6.add(this.lblAddOn, this.txtAdrAddOn);
		this.lblAdrLine.setSizeUndefined();
		this.lblAdrLine.getElement().setAttribute("slot", "label");
		this.txtAdrLine.setWidthFull();
		this.txtAdrLine.setHeight(null);
		this.formItem7.add(this.lblAdrLine, this.txtAdrLine);
		this.lblAdrLine1.setSizeUndefined();
		this.lblAdrLine1.getElement().setAttribute("slot", "label");
		this.txtAdrLine1.setWidthFull();
		this.txtAdrLine1.setHeight(null);
		this.formItem8.add(this.lblAdrLine1, this.txtAdrLine1);
		this.lblAdrZip.setSizeUndefined();
		this.lblAdrZip.getElement().setAttribute("slot", "label");
		this.txtAdrZip.setWidthFull();
		this.txtAdrZip.setHeight(null);
		this.formItem9.add(this.lblAdrZip, this.txtAdrZip);
		this.lblAdrCity.setSizeUndefined();
		this.lblAdrCity.getElement().setAttribute("slot", "label");
		this.txtAdrCity.setWidthFull();
		this.txtAdrCity.setHeight(null);
		this.formItem10.add(this.lblAdrCity, this.txtAdrCity);
		this.lblAdrCountry.setSizeUndefined();
		this.lblAdrCountry.getElement().setAttribute("slot", "label");
		this.txtAdrCountry.setWidthFull();
		this.txtAdrCountry.setHeight(null);
		this.formItem11.add(this.lblAdrCountry, this.txtAdrCountry);
		this.lblAdrRegion.setSizeUndefined();
		this.lblAdrRegion.getElement().setAttribute("slot", "label");
		this.txtAdrRegion.setWidthFull();
		this.txtAdrRegion.setHeight(null);
		this.formItem12.add(this.lblAdrRegion, this.txtAdrRegion);
		this.lblAdrRemark.setSizeUndefined();
		this.lblAdrRemark.getElement().setAttribute("slot", "label");
		this.txtAdrRemark.setWidthFull();
		this.txtAdrRemark.setHeight(null);
		this.formItem13.add(this.lblAdrRemark, this.txtAdrRemark);
		this.lblAdrState.setSizeUndefined();
		this.lblAdrState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem14.add(this.lblAdrState, this.comboBoxState);
		this.formLayout.add(this.formItem, this.formItem5, this.formItem2, this.formItem3, this.formItem4,
			this.formItem6,
			this.formItem7, this.formItem8, this.formItem9, this.formItem10, this.formItem11, this.formItem12,
			this.formItem13, this.formItem14);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();

		this.txtAdrZip.addValueChangeListener(this::txtAdrZip_valueChanged);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private FormLayout                    formLayout;
	private ComboBox<AddressType>         comboBoxType;
	private Button                        cmdSave, cmdReset;
	private ComboBox<State>               comboBoxState;
	private DatePicker                    dateAdrValidFrom;
	private BeanValidationBinder<Address> binder;
	private VerticalLayout                verticalLayout;
	private HorizontalLayout              horizontalLayout, horizontalLayout2;
	private Label                         label, lblAdrValidFrom, lblAdrIndex, lblAdrType, lblAdrSalutation, lblName,
		lblAddOn,
		lblAdrLine,
		lblAdrLine1, lblAdrZip, lblAdrCity, lblAdrCountry, lblAdrRegion, lblAdrRemark, lblAdrState;
	private ComboBox<Salutation>          comboBoxSalutation;
	private TextField                     txtAdrIndex, txtAdrName, txtAdrAddOn, txtAdrLine, txtAdrLine1, txtAdrZip,
		txtAdrCity, txtAdrCountry, txtAdrRegion, txtAdrRemark;
	private FormItem                      formItem, formItem5, formItem2, formItem3, formItem4, formItem6, formItem7,
		formItem8, formItem9, formItem10, formItem11, formItem12, formItem13, formItem14;
	// </generated-code>

}
