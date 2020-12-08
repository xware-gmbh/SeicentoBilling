
package ch.xwr.seicentobilling.ui;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.ExpType;
import ch.xwr.seicentobilling.business.LovState.ExpUnit;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.ui.project.ProjectLookupPopup;


public class ExpensePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExpensePopup.class);

	/**

	 */
	public ExpensePopup()
	{
		super();
		this.initUI();
		
		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));

		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxUnit.setItems(LovState.ExpUnit.values());
		this.comboBoxGeneric.setItems(LovState.ExpType.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		Expense    bean   = null;
		Periode    obj    = null;

		if(beanId == null)
		{
			// new
			final PeriodeDAO objDao = new PeriodeDAO();
			obj = objDao.find(objId);

			bean = new Expense();
			bean.setExpState(LovState.State.active);
			// bean.setPrlWorkType(LovState.WorkType.project);
			bean.setExpDate(new Date());
			bean.setExpUnit(LovState.ExpUnit.stück);
			bean.setExpQuantity(new Double(1));
			bean.setExpFlagGeneric(LovState.ExpType.standard);
			bean.setExpFlagCostAccount(true);
			bean.setPeriode(obj);
			bean.setExpDate(new Date());
			bean.setExpBooked(null);
			this.dateExpBooked.setValue(null);
		}
		else
		{
			final ExpenseDAO dao = new ExpenseDAO();
			bean = dao.find(beanId.longValue());

			if(bean.getExpBooked() != null)
			{
				this.dateExpBooked.setValue(bean.getExpBooked().toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate());
			}

			// this.prepareProjectCombo(bean.getProject());
		}

		this.setBeanGui(bean);
		this.checkTemplates();

		if(bean.getExpId() == null || bean.getExpId().floatValue() < 1)
		{
			// this.mnuUpload.setEnabled(false);
		}
	}

	private void prepareProjectCombo(final Project bean)
	{
		// ExpensePopup.this.cmbProject.getDataProvider().refreshItem(bean);
		// ExpensePopup.this.cmbProject.setValue(bean);
	}

	private void setBeanGui(final Expense bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);

		this.setROFields();
		
		this.postLoadAccountAction(bean);
		this.txtExpText.focus();
		
	}

	private void postLoadAccountAction(final Expense bean)
	{
		if(bean.getExpAccount() == null)
		{
			return;
		}
		
		// final boolean exist = this.comboBoxAccount.containsId(lov);
		// funktioniert auf keine Weise....
		
		final Collection<?> col1 =
			this.comboBoxAccount.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
		for(final Iterator<?> iterator = col1.iterator(); iterator.hasNext();)
		{
			final LovAccount lovBean = (LovAccount)iterator.next();
			if(lovBean.getId().equals(bean.getExpAccount()))
			{
				this.comboBoxAccount.setValue(lovBean);
				break;
			}
		}
		
	}
	
	private void checkTemplates()
	{
		final Expense line = this.binder.getBean();

		final ExpenseTemplateDAO    dao = new ExpenseTemplateDAO();
		final List<ExpenseTemplate> lst = dao.findByCostAccount(line.getPeriode().getCostAccount());

		// XdevMenuItem item = null;
		//
		// for(int i = 1; i < 11; i++)
		// {
		// item = getMnItem(i);
		// item.setEnabled(false);
		// item.setVisible(false);
		// }
		//
		// if(lst == null)
		// {
		// return; // not found
		// }
		//
		// for(final Iterator<ExpenseTemplate> iterator = lst.iterator(); iterator.hasNext();)
		// {
		// final ExpenseTemplate tpl = iterator.next();
		// final int nbr = tpl.getExtKeyNumber();
		// item = getMnItem(nbr);
		//
		// String value = "" + nbr + ": " + tpl.getProject().getProName();
		// if(tpl.getExtText() != null)
		// {
		// value = value + " - " + tpl.getExtText();
		// if(value.length() > 35)
		// {
		// value = value.substring(0, 35);
		// }
		// }
		// item.setEnabled(true);
		// item.setVisible(true);
		// item.setCaption(value);
		// }

	}

	private void setROFields()
	{
		this.dateExpBooked.setEnabled(false);
		this.cmbPeriode.setEnabled(false);
		this.cmbProject.setEnabled(false);
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
		win.add(cancelButton, new ExpensePopup());
		return win;
	}

	private void preSaveAccountAction()
	{
		if(this.binder.getBean().getExpDate() == null)
		{
			this.binder.getBean().setExpDate(new Date());
		}
		if(this.dateExpBooked.getValue() != null)
		{
			final Date date = Date.from(this.dateExpBooked.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
			this.binder.getBean().setExpBooked(date);
		}
		
		final LovAccount lov = this.comboBoxAccount.getValue();
		if(lov != null)
		{
			this.binder.getBean().setExpAccount(lov.getId());
		}
		
	}
	
	private void loadTemplate(final int iKey)
	{
		final Expense line = this.binder.getBean();

		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final ExpenseTemplate    tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if(tpl == null)
		{
			return; // not found
		}

		line.setExpAccount(tpl.getExtAccount());
		line.setExpAmount(tpl.getExtAmount());
		line.setExpFlagCostAccount(tpl.getExtFlagCostAccount());
		line.setExpFlagGeneric(tpl.getExtFlagGeneric());
		line.setExpQuantity(tpl.getExtQuantity());
		line.setExpState(tpl.getExtState());
		line.setExpText(tpl.getExtText());
		line.setExpUnit(tpl.getExtUnit());
		line.setProject(tpl.getProject());
		line.setVat(tpl.getVat());

		this.prepareProjectCombo(tpl.getProject());
		this.setROFields();

		this.postLoadAccountAction(line);
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
		this.preSaveAccountAction();
		if(SeicentoCrud.doSave(this.binder, new ExpenseDAO()))
		{
			try
			{

				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getExpId(),
					this.binder.getBean().getClass().getSimpleName());
				
				((Dialog)this.getParent().get()).close();
				Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				ExpensePopup.LOG.error("could not save ObjRoot", e);
			}
		}

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #btnSearch}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSearch_onClick(final ClickEvent<Button> event)
	{
		this.popupProjectLookup();
	}

	private void popupProjectLookup()
	{
		final Dialog win = ProjectLookupPopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
				
				if(beanId != null && beanId > 0)
				{
					final Project bean = new ProjectDAO().find(beanId);
					ExpensePopup.this.cmbProject.setValue(bean);
					
				}
			}
		});
		win.open();
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #cmbPeriode}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbPeriode_valueChanged(final ComponentValueChangeEvent<ComboBox<Periode>, Periode> event)
	{
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDefault1}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDefault1_onClick(final ClickEvent<Button> event)
	{
		this.loadTemplate(1);
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout        = new VerticalLayout();
		this.horizontalLayout      = new HorizontalLayout();
		this.label                 = new Label();
		this.formLayout            = new FormLayout();
		this.formItem2             = new FormItem();
		this.lblPeriode            = new Label();
		this.cmbPeriode            = new ComboBox<>();
		this.formItem              = new FormItem();
		this.lblExpBooked          = new Label();
		this.dateExpBooked         = new DatePicker();
		this.formItem3             = new FormItem();
		this.lblExpDate            = new Label();
		this.dateExpDate           = new DatePicker();
		this.formItem4             = new FormItem();
		this.lblExpText            = new Label();
		this.txtExpText            = new TextField();
		this.formItem5             = new FormItem();
		this.lblExpAmount          = new Label();
		this.txtExpAmount          = new TextField();
		this.formItem6             = new FormItem();
		this.lblVat                = new Label();
		this.cmbVat                = new ComboBox<>();
		this.formItem7             = new FormItem();
		this.lblProject            = new Label();
		this.cmbProject            = new ComboBox<>();
		this.btnSearch             = new Button();
		this.formItem8             = new FormItem();
		this.lblExpAccount         = new Label();
		this.comboBoxAccount       = new ComboBox<>();
		this.formItem9             = new FormItem();
		this.lblExpFlagGeneric     = new Label();
		this.comboBoxGeneric       = new ComboBox<>();
		this.formItem10            = new FormItem();
		this.lblExpFlagCostAccount = new Label();
		this.chkExpFlagCostAccount = new Checkbox();
		this.formItem11            = new FormItem();
		this.lblExpUnit            = new Label();
		this.comboBoxUnit          = new ComboBox<>();
		this.formItem12            = new FormItem();
		this.lblExpQuantity        = new Label();
		this.txtExpQuantity        = new TextField();
		this.formItem13            = new FormItem();
		this.lblExpState           = new Label();
		this.comboBoxState         = new ComboBox<>();
		this.horizontalLayout2     = new HorizontalLayout();
		this.cmdSave               = new Button();
		this.cmdReset              = new Button();
		this.cmdDefault1           = new Button();
		this.binder                = new BeanValidationBinder<>(Expense.class);
		
		this.label.setText("Spesen erfassen");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblPeriode.setText(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbPeriode::getItemLabelGenerator),
			DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.cmbPeriode.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Periode::getPerName));
		this.lblExpBooked.setText(StringResourceUtils.optLocalizeString("{$lblExpBooked.value}", this));
		this.lblExpDate.setText(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setLocale(new Locale("de", "CH"));
		this.dateExpDate.setValue(LocalDate.of(2020, Month.NOVEMBER, 2));
		this.lblExpText.setText(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.lblExpAmount.setText(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.lblVat.setText("MwSt Incl");
		this.cmbVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAllInclusive()));
		this.cmbVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.lblProject.setText(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setEnabled(false);
		this.cmbProject.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.cmbProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.btnSearch.setIcon(IronIcons.SEARCH.create());
		this.lblExpAccount.setText(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new LovAccountDAO().findAllMine()));
		this.comboBoxAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(LovAccount::getName));
		this.lblExpFlagGeneric.setText(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.comboBoxGeneric.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblExpFlagCostAccount.setText(" ");
		this.chkExpFlagCostAccount
			.setLabel(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.lblExpUnit.setText(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.comboBoxUnit.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblExpQuantity.setText(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.lblExpState.setText(StringResourceUtils.optLocalizeString("{$lblExpState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.CANCEL.create());
		this.cmdDefault1.setText("Def 1");
		this.cmdDefault1.setIcon(VaadinIcon.BOOKMARK.create());
		
		this.binder.forField(this.cmbPeriode).bind("periode");
		this.binder.forField(this.dateExpDate).withNullRepresentation(LocalDate.of(2020, Month.NOVEMBER, 26))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("expDate");
		this.binder.forField(this.txtExpText).withNullRepresentation("").bind("expText");
		this.binder.forField(this.txtExpAmount).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("expAmount");
		this.binder.forField(this.cmbVat).bind("vat");
		this.binder.forField(this.cmbProject).bind("project");
		this.binder.forField(this.comboBoxGeneric).bind("expFlagGeneric");
		this.binder.forField(this.chkExpFlagCostAccount).withNullRepresentation(false).bind("expFlagCostAccount");
		this.binder.forField(this.comboBoxUnit).bind("expUnit");
		this.binder.forField(this.txtExpQuantity).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("expQuantity");
		this.binder.forField(this.comboBoxState).bind("expState");
		
		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblPeriode.setSizeUndefined();
		this.lblPeriode.getElement().setAttribute("slot", "label");
		this.cmbPeriode.setWidthFull();
		this.cmbPeriode.setHeight(null);
		this.formItem2.add(this.lblPeriode, this.cmbPeriode);
		this.lblExpBooked.setSizeUndefined();
		this.lblExpBooked.getElement().setAttribute("slot", "label");
		this.dateExpBooked.setWidthFull();
		this.dateExpBooked.setHeight(null);
		this.formItem.add(this.lblExpBooked, this.dateExpBooked);
		this.lblExpDate.setSizeUndefined();
		this.lblExpDate.getElement().setAttribute("slot", "label");
		this.dateExpDate.setWidthFull();
		this.dateExpDate.setHeight(null);
		this.formItem3.add(this.lblExpDate, this.dateExpDate);
		this.lblExpText.setSizeUndefined();
		this.lblExpText.getElement().setAttribute("slot", "label");
		this.txtExpText.setWidthFull();
		this.txtExpText.setHeight(null);
		this.formItem4.add(this.lblExpText, this.txtExpText);
		this.lblExpAmount.setSizeUndefined();
		this.lblExpAmount.getElement().setAttribute("slot", "label");
		this.txtExpAmount.setWidthFull();
		this.txtExpAmount.setHeight(null);
		this.formItem5.add(this.lblExpAmount, this.txtExpAmount);
		this.lblVat.setSizeUndefined();
		this.lblVat.getElement().setAttribute("slot", "label");
		this.cmbVat.setWidthFull();
		this.cmbVat.setHeight(null);
		this.formItem6.add(this.lblVat, this.cmbVat);
		this.lblProject.setSizeUndefined();
		this.lblProject.getElement().setAttribute("slot", "label");
		this.cmbProject.setWidth("75%");
		this.cmbProject.setHeight(null);
		this.btnSearch.setSizeUndefined();
		this.formItem7.add(this.lblProject, this.cmbProject, this.btnSearch);
		this.lblExpAccount.setSizeUndefined();
		this.lblExpAccount.getElement().setAttribute("slot", "label");
		this.comboBoxAccount.setWidthFull();
		this.comboBoxAccount.setHeight(null);
		this.formItem8.add(this.lblExpAccount, this.comboBoxAccount);
		this.lblExpFlagGeneric.setSizeUndefined();
		this.lblExpFlagGeneric.getElement().setAttribute("slot", "label");
		this.comboBoxGeneric.setWidthFull();
		this.comboBoxGeneric.setHeight(null);
		this.formItem9.add(this.lblExpFlagGeneric, this.comboBoxGeneric);
		this.lblExpFlagCostAccount.setSizeUndefined();
		this.lblExpFlagCostAccount.getElement().setAttribute("slot", "label");
		this.chkExpFlagCostAccount.setWidthFull();
		this.chkExpFlagCostAccount.setHeight(null);
		this.formItem10.add(this.lblExpFlagCostAccount, this.chkExpFlagCostAccount);
		this.lblExpUnit.setSizeUndefined();
		this.lblExpUnit.getElement().setAttribute("slot", "label");
		this.comboBoxUnit.setWidthFull();
		this.comboBoxUnit.setHeight(null);
		this.formItem11.add(this.lblExpUnit, this.comboBoxUnit);
		this.lblExpQuantity.setSizeUndefined();
		this.lblExpQuantity.getElement().setAttribute("slot", "label");
		this.txtExpQuantity.setWidthFull();
		this.txtExpQuantity.setHeight(null);
		this.formItem12.add(this.lblExpQuantity, this.txtExpQuantity);
		this.lblExpState.setSizeUndefined();
		this.lblExpState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem13.add(this.lblExpState, this.comboBoxState);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6,
			this.formItem7, this.formItem8, this.formItem9, this.formItem10, this.formItem11, this.formItem12,
			this.formItem13);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.cmdDefault1.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdSave, this.cmdReset, this.cmdDefault1);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();
		
		this.cmbPeriode.addValueChangeListener(this::cmbPeriode_valueChanged);
		this.btnSearch.addClickListener(this::btnSearch_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
		this.cmdDefault1.addClickListener(this::cmdDefault1_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private ComboBox<ExpUnit>             comboBoxUnit;
	private ComboBox<Vat>                 cmbVat;
	private VerticalLayout                verticalLayout;
	private HorizontalLayout              horizontalLayout, horizontalLayout2;
	private Label                         label, lblPeriode, lblExpBooked, lblExpDate, lblExpText, lblExpAmount, lblVat,
		lblProject, lblExpAccount, lblExpFlagGeneric, lblExpFlagCostAccount, lblExpUnit, lblExpQuantity, lblExpState;
	private BeanValidationBinder<Expense> binder;
	private FormItem                      formItem2, formItem, formItem3, formItem4, formItem5, formItem6, formItem7,
		formItem8, formItem9, formItem10, formItem11, formItem12, formItem13;
	private FormLayout                    formLayout;
	private Checkbox                      chkExpFlagCostAccount;
	private Button                        btnSearch, cmdSave, cmdReset, cmdDefault1;
	private ComboBox<LovAccount>          comboBoxAccount;
	private ComboBox<ExpType>             comboBoxGeneric;
	private ComboBox<State>               comboBoxState;
	private DatePicker                    dateExpBooked, dateExpDate;
	private ComboBox<Periode>             cmbPeriode;
	private ComboBox<Project>             cmbProject;
	private TextField                     txtExpText, txtExpAmount, txtExpQuantity;
	// </generated-code>
	
}
