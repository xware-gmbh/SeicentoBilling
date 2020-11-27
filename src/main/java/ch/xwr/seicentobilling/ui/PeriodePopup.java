
package ch.xwr.seicentobilling.ui;

import java.text.DecimalFormat;
import java.util.Calendar;

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
import com.vaadin.flow.component.checkbox.Checkbox;
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
import ch.xwr.seicentobilling.business.LovState.BookingType;
import ch.xwr.seicentobilling.business.LovState.Month;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Periode;


public class PeriodePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(PeriodePopup.class);
	
	private String  source  = "";
	private boolean isAdmin = false;
	
	/**
	 *
	 */
	public PeriodePopup()
	{
		super();
		this.initUI();

		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));
		
		// State
		this.comboBoxState.setItems(LovState.State.values());
		
		this.comboBoxBookedExp.setItems(LovState.BookingType.values());
		this.comboBoxBookedPro.setItems(LovState.BookingType.values());
		this.comboBoxMonth.setItems(LovState.Month.values());
		
		this.lblAmtExpense.setText("");
		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());
		
		// get Parameter
		this.source = (String)UI.getCurrent().getSession().getAttribute("source");
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		Periode    bean   = null;
		
		this.isAdmin = (boolean)UI.getCurrent().getSession().getAttribute("isAdmin");
		
		if(beanId == null)
		{
			bean = this.getNewDaoWithDefaults();
		}
		else
		{
			final PeriodeDAO dao = new PeriodeDAO();
			bean = dao.find(beanId.longValue());
		}
		
		this.setBeanGui(bean);
		this.setROFields();
	}
	
	private void setBeanGui(final Periode bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);

	}
	
	private void setROFields()
	{
		this.txtPerName.setEnabled(false);
		this.comboBoxBookedExp.setEnabled(false);
		this.comboBoxBookedPro.setEnabled(false);

		if("projectline".contentEquals(this.source))
		{
			this.cboSignOffExpense.setVisible(false);
		}
		else
		{
			this.cboSignOffExpense.setVisible(true);
		}

		if(this.isAdmin)
		{
			this.comboBoxBookedExp.setEnabled(true);
			this.comboBoxBookedPro.setEnabled(true);
		}
	}
	
	private Periode getNewDaoWithDefaults()
	{
		final Periode dao = new Periode();
		
		dao.setPerState(LovState.State.active);
		dao.setPerBookedExpense(LovState.BookingType.offen);
		dao.setPerBookedProject(LovState.BookingType.offen);
		dao.setPerSignOffExpense(false);
		
		// dao.setCostAccount(null);
		
		final Calendar now = Calendar.getInstance(); // Gets the current date
														// and time
		dao.setPerMonth(LovState.Month.fromId(now.get(Calendar.MONTH) + 1));
		dao.setPerYear(now.get(Calendar.YEAR));
		
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if(bean == null)
		{
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		dao.setCostAccount(bean);
		
		return dao;
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
		win.add(cancelButton, new PeriodePopup());
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
		if(SeicentoCrud.doSave(this.binder, new PeriodeDAO()))
		{
			try
			{
				
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getPerId(),
					this.binder.getBean().getClass().getSimpleName());

				UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
				UI.getCurrent().getSession().setAttribute("beanId",
					this.binder.getBean().getPerId());

				((Dialog)this.getParent().get()).close();
				// Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				PeriodePopup.LOG.error("could not save ObjRoot", e);
			}
		}
		
	}

	/**
	 * Event handler delegate method for the {@link Checkbox} {@link #cboSignOffExpense}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cboSignOffExpense_valueChanged(final ComponentValueChangeEvent<Checkbox, Boolean> event)
	{
		final Checkbox cbx = event.getSource();

		if(cbx.getValue().booleanValue())
		{
			final double total = new ExpenseDAO().sumAmount(this.binder.getBean());

			final DecimalFormat df        = new DecimalFormat("##,###.00");
			final String        formatted = df.format(total);

			final String lbl = "Total CHF: " + formatted;
			this.lblAmtExpense.setText(lbl);
		}
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout      = new VerticalLayout();
		this.horizontalLayout    = new HorizontalLayout();
		this.icon                = new Icon(VaadinIcon.CLOCK);
		this.label               = new Label();
		this.formLayout          = new FormLayout();
		this.formItem2           = new FormItem();
		this.lblCostAccount      = new Label();
		this.cmbCostAccount      = new ComboBox<>();
		this.formItem            = new FormItem();
		this.lblPerName          = new Label();
		this.txtPerName          = new TextField();
		this.formItem4           = new FormItem();
		this.lblPerMonth         = new Label();
		this.comboBoxMonth       = new ComboBox<>();
		this.formItem3           = new FormItem();
		this.lblPerYear          = new Label();
		this.textFieldYear       = new TextField();
		this.formItem6           = new FormItem();
		this.lblAmtExpense       = new Label();
		this.cboSignOffExpense   = new Checkbox();
		this.formItem8           = new FormItem();
		this.lblPerBookedExpense = new Label();
		this.comboBoxBookedExp   = new ComboBox<>();
		this.formItem9           = new FormItem();
		this.lblPerState         = new Label();
		this.comboBoxState       = new ComboBox<>();
		this.formItem5           = new FormItem();
		this.lblPerBookedProject = new Label();
		this.comboBoxBookedPro   = new ComboBox<>();
		this.horizontalLayout2   = new HorizontalLayout();
		this.cmdSave             = new Button();
		this.cmdCancel           = new Button();
		this.binder              = new BeanValidationBinder<>(Periode.class);

		this.label.setText("Periode bearbeiten");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblCostAccount.setText(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.lblPerName.setText(StringResourceUtils.optLocalizeString("{$lblPerName.value}", this));
		this.lblPerMonth.setText(StringResourceUtils.optLocalizeString("{$lblPerMonth.value}", this));
		this.comboBoxMonth.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblPerYear.setText("Jahr");
		this.lblAmtExpense.setText("Betrag");
		this.lblAmtExpense.setMinWidth("");
		this.cboSignOffExpense.setLabel("Freigabe Buchhalter");
		this.lblPerBookedExpense.setText(StringResourceUtils.optLocalizeString("{$lblPerBookedExpense.value}", this));
		this.comboBoxBookedExp.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblPerState.setText(StringResourceUtils.optLocalizeString("{$lblPerState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblPerBookedProject.setText(StringResourceUtils.optLocalizeString("{$lblPerBookedProject.value}", this));
		this.comboBoxBookedPro.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdCancel.setText(StringResourceUtils.optLocalizeString("{$cmdCancel.caption}", this));
		this.cmdCancel.setIcon(IronIcons.CANCEL.create());

		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.txtPerName).withNullRepresentation("").bind("perName");
		this.binder.forField(this.comboBoxMonth).bind("perMonth");
		this.binder.forField(this.comboBoxBookedExp).bind("perBookedExpense");
		this.binder.forField(this.cboSignOffExpense).withNullRepresentation(false).bind("perSignOffExpense");
		this.binder.forField(this.comboBoxBookedPro).bind("perBookedProject");
		this.binder.forField(this.comboBoxState).bind("perState");
		this.binder.forField(this.textFieldYear).withNullRepresentation("").withConverter(
			ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer().pattern("####"))
				.build())
			.bind("perYear");

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setWidthFull();
		this.cmbCostAccount.setHeight(null);
		this.formItem2.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblPerName.setSizeUndefined();
		this.lblPerName.getElement().setAttribute("slot", "label");
		this.txtPerName.setWidthFull();
		this.txtPerName.setHeight(null);
		this.formItem.add(this.lblPerName, this.txtPerName);
		this.lblPerMonth.setSizeUndefined();
		this.lblPerMonth.getElement().setAttribute("slot", "label");
		this.comboBoxMonth.setWidthFull();
		this.comboBoxMonth.setHeight(null);
		this.formItem4.add(this.lblPerMonth, this.comboBoxMonth);
		this.lblPerYear.setSizeUndefined();
		this.lblPerYear.getElement().setAttribute("slot", "label");
		this.textFieldYear.setWidthFull();
		this.textFieldYear.setHeight(null);
		this.formItem3.add(this.lblPerYear, this.textFieldYear);
		this.lblAmtExpense.setSizeUndefined();
		this.lblAmtExpense.getElement().setAttribute("slot", "label");
		this.cboSignOffExpense.setWidthFull();
		this.cboSignOffExpense.setHeight(null);
		this.formItem6.add(this.lblAmtExpense, this.cboSignOffExpense);
		this.lblPerBookedExpense.setSizeUndefined();
		this.lblPerBookedExpense.getElement().setAttribute("slot", "label");
		this.comboBoxBookedExp.setWidthFull();
		this.comboBoxBookedExp.setHeight(null);
		this.formItem8.add(this.lblPerBookedExpense, this.comboBoxBookedExp);
		this.lblPerState.setSizeUndefined();
		this.lblPerState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem9.add(this.lblPerState, this.comboBoxState);
		this.lblPerBookedProject.setSizeUndefined();
		this.lblPerBookedProject.getElement().setAttribute("slot", "label");
		this.comboBoxBookedPro.setWidthFull();
		this.comboBoxBookedPro.setHeight(null);
		this.formItem5.add(this.lblPerBookedProject, this.comboBoxBookedPro);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem4, this.formItem3, this.formItem6,
			this.formItem8,
			this.formItem9, this.formItem5);
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

		this.cboSignOffExpense.addValueChangeListener(this::cboSignOffExpense_valueChanged);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdCancel.addClickListener(this::cmdCancel_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private ComboBox<BookingType>         comboBoxBookedExp, comboBoxBookedPro;
	private VerticalLayout                verticalLayout;
	private HorizontalLayout              horizontalLayout, horizontalLayout2;
	private Label                         label, lblCostAccount, lblPerName, lblPerMonth, lblPerYear, lblAmtExpense,
		lblPerBookedExpense, lblPerState, lblPerBookedProject;
	private FormItem                      formItem2, formItem, formItem4, formItem3, formItem6, formItem8, formItem9,
		formItem5;
	private ComboBox<Month>               comboBoxMonth;
	private FormLayout                    formLayout;
	private Checkbox                      cboSignOffExpense;
	private Button                        cmdSave, cmdCancel;
	private ComboBox<State>               comboBoxState;
	private BeanValidationBinder<Periode> binder;
	private Icon                          icon;
	private TextField                     txtPerName, textFieldYear;
	private ComboBox<CostAccount>         cmbCostAccount;
	// </generated-code>

}
