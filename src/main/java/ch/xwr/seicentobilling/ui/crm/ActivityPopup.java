
package ch.xwr.seicentobilling.ui.crm;

import java.util.Date;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovCrm.ActivityType;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.ActivityDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


public class ActivityPopup extends VerticalLayout
{
	
	public ActivityPopup()
	{
		super();
		this.initUI();
		
		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxType.setItems(LovCrm.ActivityType.values());
		
		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		Activity   bean   = null;
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
			
			bean = new Activity();
			bean.setActState(LovState.State.active);
			bean.setCustomer(obj);
			bean.setCostAccount(beanCsa);
			bean.setActDate(new Date());
			bean.setActType(LovCrm.ActivityType.misc);
			bean.setActText("");
			
		}
		else
		{
			final ActivityDAO dao = new ActivityDAO();
			bean = dao.find(beanId.longValue());
		}
		
		this.setBeanGui(bean);
		
	}
	
	private void setBeanGui(final Activity bean)
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
		win.add(cancelButton, new ActivityPopup());
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
		
		if(SeicentoCrud.doSave(this.binder, new ActivityDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getactId(),
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
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout         = new VerticalLayout();
		this.horizontalLayout       = new HorizontalLayout();
		this.label                  = new Label();
		this.formLayout             = new FormLayout();
		this.formItem2              = new FormItem();
		this.lblActType             = new Label();
		this.comboBoxType           = new ComboBox<>();
		this.formItem               = new FormItem();
		this.lblActDate             = new Label();
		this.dateActDate            = new DatePicker();
		this.formItem4              = new FormItem();
		this.lblActText             = new Label();
		this.textArea               = new TextArea();
		this.formItem5              = new FormItem();
		this.lblActLink             = new Label();
		this.txtActLink             = new TextField();
		this.formItem6              = new FormItem();
		this.lblActFollowingUpDate  = new Label();
		this.dateActFollowingUpDate = new DatePicker();
		this.formItem9              = new FormItem();
		this.lblCostAccount         = new Label();
		this.cmbCostAccount         = new ComboBox<>();
		this.formItem13             = new FormItem();
		this.lblActState            = new Label();
		this.comboBoxState          = new ComboBox<>();
		this.horizontalLayout2      = new HorizontalLayout();
		this.cmdSave                = new Button();
		this.cmdReset               = new Button();
		this.binder                 = new BeanValidationBinder<>(Activity.class);

		this.label.setText("Aktivität");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblActType.setText("Type");
		this.comboBoxType.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblActDate.setText("Datum");
		this.lblActText.setText("Text");
		this.lblActLink.setText("Link");
		this.lblActFollowingUpDate.setText("Folgetermin");
		this.lblCostAccount.setText("Mitarbeiter");
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.lblActState.setText("Status");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Abbrechen");
		this.cmdReset.setIcon(IronIcons.CANCEL.create());

		this.binder.forField(this.comboBoxType).bind("actType");
		this.binder.forField(this.dateActDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("actDate");
		this.binder.forField(this.txtActLink).withNullRepresentation("").bind("actLink");
		this.binder.forField(this.dateActFollowingUpDate)
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build())
			.bind("actFollowingUpDate");
		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.comboBoxState).bind("actState");

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblActType.setSizeUndefined();
		this.lblActType.getElement().setAttribute("slot", "label");
		this.comboBoxType.setWidthFull();
		this.comboBoxType.setHeight(null);
		this.formItem2.add(this.lblActType, this.comboBoxType);
		this.lblActDate.setSizeUndefined();
		this.lblActDate.getElement().setAttribute("slot", "label");
		this.dateActDate.setWidthFull();
		this.dateActDate.setHeight(null);
		this.formItem.add(this.lblActDate, this.dateActDate);
		this.lblActText.setSizeUndefined();
		this.lblActText.getElement().setAttribute("slot", "label");
		this.textArea.setWidthFull();
		this.textArea.setHeight(null);
		this.formItem4.add(this.lblActText, this.textArea);
		this.lblActLink.setSizeUndefined();
		this.lblActLink.getElement().setAttribute("slot", "label");
		this.txtActLink.setWidthFull();
		this.txtActLink.setHeight(null);
		this.formItem5.add(this.lblActLink, this.txtActLink);
		this.lblActFollowingUpDate.setSizeUndefined();
		this.lblActFollowingUpDate.getElement().setAttribute("slot", "label");
		this.dateActFollowingUpDate.setWidthFull();
		this.dateActFollowingUpDate.setHeight(null);
		this.formItem6.add(this.lblActFollowingUpDate, this.dateActFollowingUpDate);
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setWidthFull();
		this.cmbCostAccount.setHeight(null);
		this.formItem9.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblActState.setSizeUndefined();
		this.lblActState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem13.add(this.lblActState, this.comboBoxState);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem4, this.formItem5, this.formItem6,
			this.formItem9,
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

		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private ComboBox<ActivityType>         comboBoxType;
	private TextArea                       textArea;
	private VerticalLayout                 verticalLayout;
	private HorizontalLayout               horizontalLayout, horizontalLayout2;
	private Label                          label, lblActType, lblActDate, lblActText, lblActLink, lblActFollowingUpDate,
		lblCostAccount, lblActState;
	private FormItem                       formItem2, formItem, formItem4, formItem5, formItem6, formItem9, formItem13;
	private FormLayout                     formLayout;
	private Button                         cmdSave, cmdReset;
	private ComboBox<State>                comboBoxState;
	private DatePicker                     dateActDate, dateActFollowingUpDate;
	private BeanValidationBinder<Activity> binder;
	private TextField                      txtActLink;
	private ComboBox<CostAccount>          cmbCostAccount;
	// </generated-code>
	
}
