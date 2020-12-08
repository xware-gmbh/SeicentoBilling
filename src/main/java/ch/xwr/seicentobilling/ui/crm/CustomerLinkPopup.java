
package ch.xwr.seicentobilling.ui.crm;

import java.util.Date;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
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
import ch.xwr.seicentobilling.business.LovCrm.Department;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


public class CustomerLinkPopup extends VerticalLayout
{
	
	public CustomerLinkPopup()
	{
		super();
		this.initUI();
		
		// State
		// this.comboBoxState.setItems(LovState.State.values());
		// this.comboBoxType.setItems(LovCrm.LinkType.values());
		// this.comboBoxDepartment.setItems(LovCrm.Department.values());
		
		// get Parameter
		final Long   beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long   objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		CustomerLink bean   = null;
		Customer     obj    = null;
		if(beanId == null)
		{

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if(beanCsa == null)
			{
				beanCsa = new CostAccountDAO().findAll().get(0); // Dev Mode
			}
			final CustomerDAO cusDao = new CustomerDAO();
			obj = cusDao.find(objId);

			bean = new CustomerLink();
			bean.setCnkState(LovState.State.active);
			bean.setCustomer(obj);
			// act.setCostAccount(beanCsa);
			bean.setCnkValidFrom(new Date());
			bean.setCnkType(LovCrm.LinkType.mail);
			bean.setCnkDepartment(LovCrm.Department.main);

		}
		else
		{
			final CustomerLinkDAO dao = new CustomerLinkDAO();
			bean = dao.find(beanId.longValue());
		}
		
		this.setBeanGui(bean);
		
	}
	
	private void setBeanGui(final CustomerLink bean)
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
		win.add(cancelButton, new CustomerLinkPopup());
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
		
		if(SeicentoCrud.doSave(this.binder, new CustomerLinkDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getCnkId(),
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
	 * Event handler delegate method for the {@link ComboBox} {@link #comboBox}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBox_valueChanged(final ComponentValueChangeEvent<ComboBox<Object>, Object> event)
	{
		if(!this.binder.hasChanges())
		{
			return;
		}

		String                lblText = "Link";
		final LovCrm.LinkType value   = (LovCrm.LinkType)event.getValue();

		if(value != null)
		{
			if(value.equals(LovCrm.LinkType.mail))
			{
				lblText = "Mail";
			}
			else if(value.equals(LovCrm.LinkType.phone))
			{
				lblText = "Telefon";
				this.lblCnkLink.setText("Telefon");
			}
			else if(value.equals(LovCrm.LinkType.web))
			{
				lblText = "Url";
			}
		}
		this.lblCnkLink.setText(lblText);
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout     = new VerticalLayout();
		this.horizontalLayout   = new HorizontalLayout();
		this.label              = new Label();
		this.formLayout         = new FormLayout();
		this.formItem3          = new FormItem();
		this.lblCnkIndex        = new Label();
		this.txtCnkIndex        = new TextField();
		this.formItem2          = new FormItem();
		this.lblCnkType         = new Label();
		this.comboBox           = new ComboBox<>();
		this.formItem6          = new FormItem();
		this.lblCnkDepartment   = new Label();
		this.comboBoxDepartment = new ComboBox<>();
		this.formItem           = new FormItem();
		this.lblCnkValidFrom    = new Label();
		this.dateCnkValidFrom   = new DatePicker();
		this.formItem5          = new FormItem();
		this.lblCnkLink         = new Label();
		this.txtCnkLink         = new TextField();
		this.formItem4          = new FormItem();
		this.lblCnkRemark       = new Label();
		this.txtCnkRemark       = new TextField();
		this.formItem13         = new FormItem();
		this.lblCnkState        = new Label();
		this.comboBoxState      = new ComboBox<>();
		this.horizontalLayout2  = new HorizontalLayout();
		this.cmdSave            = new Button();
		this.cmdReset           = new Button();
		this.binder             = new BeanValidationBinder<>(CustomerLink.class);

		this.label.setText("Kontaktkoordinaten");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblCnkIndex.setText("Index");
		this.lblCnkType.setText("Type");
		this.comboBox.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblCnkDepartment.setText("Bereich");
		this.comboBoxDepartment.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblCnkValidFrom.setText("ValidFrom");
		this.lblCnkLink.setText("Link");
		this.lblCnkRemark.setText("Remark");
		this.lblCnkState.setText("Status");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.CANCEL.create());

		this.binder.forField(this.txtCnkIndex).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToShort().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("cnkIndex");
		this.binder.forField(this.comboBox).bind("cnkType");
		this.binder.forField(this.comboBoxDepartment).bind("cnkDepartment");
		this.binder.forField(this.dateCnkValidFrom)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("cnkValidFrom");
		this.binder.forField(this.txtCnkLink).bind("cnkLink");
		this.binder.forField(this.txtCnkRemark).withNullRepresentation("").bind("cnkRemark");
		this.binder.forField(this.comboBoxState).bind("cnkState");

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblCnkIndex.setSizeUndefined();
		this.lblCnkIndex.getElement().setAttribute("slot", "label");
		this.txtCnkIndex.setWidthFull();
		this.txtCnkIndex.setHeight(null);
		this.formItem3.add(this.lblCnkIndex, this.txtCnkIndex);
		this.lblCnkType.setSizeUndefined();
		this.lblCnkType.getElement().setAttribute("slot", "label");
		this.comboBox.setWidthFull();
		this.comboBox.setHeight(null);
		this.formItem2.add(this.lblCnkType, this.comboBox);
		this.lblCnkDepartment.setSizeUndefined();
		this.lblCnkDepartment.getElement().setAttribute("slot", "label");
		this.comboBoxDepartment.setWidthFull();
		this.comboBoxDepartment.setHeight(null);
		this.formItem6.add(this.lblCnkDepartment, this.comboBoxDepartment);
		this.lblCnkValidFrom.setSizeUndefined();
		this.lblCnkValidFrom.getElement().setAttribute("slot", "label");
		this.dateCnkValidFrom.setWidthFull();
		this.dateCnkValidFrom.setHeight(null);
		this.formItem.add(this.lblCnkValidFrom, this.dateCnkValidFrom);
		this.lblCnkLink.setSizeUndefined();
		this.lblCnkLink.getElement().setAttribute("slot", "label");
		this.txtCnkLink.setWidthFull();
		this.txtCnkLink.setHeight(null);
		this.formItem5.add(this.lblCnkLink, this.txtCnkLink);
		this.lblCnkRemark.setSizeUndefined();
		this.lblCnkRemark.getElement().setAttribute("slot", "label");
		this.txtCnkRemark.setWidthFull();
		this.txtCnkRemark.setHeight(null);
		this.formItem4.add(this.lblCnkRemark, this.txtCnkRemark);
		this.lblCnkState.setSizeUndefined();
		this.lblCnkState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem13.add(this.lblCnkState, this.comboBoxState);
		this.formLayout.add(this.formItem3, this.formItem2, this.formItem6, this.formItem, this.formItem5,
			this.formItem4,
			this.formItem13);
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

		this.comboBox.addValueChangeListener(this::comboBox_valueChanged);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private FormLayout                         formLayout;
	private Button                             cmdSave, cmdReset;
	private ComboBox<State>                    comboBoxState;
	private DatePicker                         dateCnkValidFrom;
	private ComboBox<Department>               comboBoxDepartment;
	private VerticalLayout                     verticalLayout;
	private HorizontalLayout                   horizontalLayout, horizontalLayout2;
	private Label                              label, lblCnkIndex, lblCnkType, lblCnkDepartment, lblCnkValidFrom,
		lblCnkLink, lblCnkRemark, lblCnkState;
	private ComboBox<Object>                   comboBox;
	private TextField                          txtCnkIndex, txtCnkLink, txtCnkRemark;
	private FormItem                           formItem3, formItem2, formItem6, formItem, formItem5, formItem4,
		formItem13;
	private BeanValidationBinder<CustomerLink> binder;
	// </generated-code>
	
}
