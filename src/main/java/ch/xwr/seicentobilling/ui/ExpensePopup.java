
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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
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
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.ui.project.ProjectLookupPopup;


public class ExpensePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExpensePopup.class);
	private MenuItem            menuOption;
	private MenuItem            mnuUpload;
	private MenuItem            mnuDefaults;
	private MenuItem            mnuTemplate1;
	private MenuItem            mnuTemplate2;
	private MenuItem            mnuTemplate3;
	private MenuItem            mnuTemplate4;
	private MenuItem            mnuTemplate5;
	private MenuItem            mnuTemplate6;
	private MenuItem            mnuTemplate7;
	private MenuItem            mnuTemplate8;
	private MenuItem            mnuTemplate9;
	private MenuItem            mnuTemplate10;
	private MenuItem            mnuCancel;
	private MenuItem            menuText;
	private MenuItem            mnuSaveItem;
	
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
		
		this.createMenu();
		
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
		this.setTextList(bean.getProject());
		
		this.setBeanGui(bean);
		this.checkTemplates();
		
		if(bean.getExpId() == null || bean.getExpId().floatValue() < 1)
		{
			this.mnuUpload.setEnabled(false);
		}
	}
	
	private MenuItem getMnItem(final int icount)
	{

		switch(icount)
		{
			case 0:
				return this.mnuTemplate10;
			case 1:
				return this.mnuTemplate1;
			case 2:
				return this.mnuTemplate2;
			case 3:
				return this.mnuTemplate3;
			case 4:
				return this.mnuTemplate4;
			case 5:
				return this.mnuTemplate5;
			case 6:
				return this.mnuTemplate6;
			case 7:
				return this.mnuTemplate7;
			case 8:
				return this.mnuTemplate8;
			case 9:
				return this.mnuTemplate9;
			case 10:
				return this.mnuTemplate10;
		}

		return null;
	}
	
	private void createMenu()
	{
		this.menuOption = this.menuBar.addItem("Optionen", null);
		this.mnuUpload  =
			this.menuOption.getSubMenu().addItem("Belege verwalten...", e -> this.mnuUpload_menuSelected(e));
		this.mnuUpload.setVisible(false);
		this.mnuDefaults   = this.menuOption.getSubMenu().addItem("Vorlage", null);
		this.mnuTemplate1  = this.mnuDefaults.getSubMenu().addItem("Spesen", e -> this.loadTemplate(1));
		this.mnuTemplate2  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(2));
		this.mnuTemplate3  =
			this.mnuDefaults.getSubMenu().addItem("Vorlagen Rapport", e -> this.loadTemplate(3));
		this.mnuTemplate4  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(4));
		this.mnuTemplate5  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(5));
		this.mnuTemplate6  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(6));
		this.mnuTemplate7  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(7));
		this.mnuTemplate8  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(8));
		this.mnuTemplate9  = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(9));
		this.mnuTemplate10 = this.mnuDefaults.getSubMenu().addItem("Rapporte", e -> this.loadTemplate(10));
		this.menuText      = this.menuOption.getSubMenu().addItem("Text...", null);
		this.mnuCancel     =
			this.menuOption.getSubMenu().addItem(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this),
				e -> this.cmdReset_onClick(null));
		this.mnuSaveItem   = this.menuOption.getSubMenu().addItem("Speichern", e -> this.cmdSave_onClick(null));
		
	}
	
	private void mnuUpload_menuSelected(final ClickEvent<MenuItem> e)
	{
		final Expense bean = this.binder.getBean();

		if(bean.getExpId() == null)
		{
			return;
		}

		final RowObjectManager man = new RowObjectManager();
		final RowObject        obj = man.getRowObject(bean.getClass().getSimpleName(), bean.getExpId());
		UI.getCurrent().getSession().setAttribute("RowObject", obj);

		this.popupAttachments();

	}
	
	private void popupAttachments()
	{
		final Dialog win = AttachmentPopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{

				String retval = UI.getCurrent().getSession().getAttribute(String.class);

				if(retval == null)
				{
					retval = "cmdCancel";
				}
				if(retval.equals("cmdDone"))
				{
					// ExpenseView.this.txtExpText.setValue(reason);
				}
			}

		});
		win.open();
	}
	
	private void prepareProjectCombo(final Project bean)
	{
		// ExpensePopup.this.cmbProject.getDataProvider().refreshItem(bean);
		ExpensePopup.this.cmbProject.setValue(bean);
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
		for(int i = 1; i < 11; i++)
		{
			final MenuItem item = this.getMnItem(i);
			item.setEnabled(false);
			item.setVisible(false);
		}
		if(lst == null)
		{
			return; // not found
		}
		for(final Iterator<ExpenseTemplate> iterator = lst.iterator(); iterator.hasNext();)
		{
			final ExpenseTemplate tpl  = iterator.next();
			final int             nbr  = tpl.getExtKeyNumber();
			final MenuItem        item = this.getMnItem(nbr);

			String value = "" + nbr + ": " + tpl.getProject().getProName();
			if(tpl.getExtText() != null)
			{
				value = value + " - " + tpl.getExtText();
				if(value.length() > 35)
				{
					value = value.substring(0, 35);
				}
			}
			item.setEnabled(true);
			item.setVisible(true);
			item.setText(value);
		}

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
	
	private void setTextList(final Project project)
	{
		if(project != null)
		{
			final List<Expense> expenseList = new ExpenseDAO().findByProject(project);
			for(final Expense ex : expenseList)
			{
				this.menuText.getSubMenu().addItem(ex.getExpText(),
					e -> ExpensePopup.this.txtExpText.setValue(ex.getExpText()));
			}
		}
		
	}

	private void loadTemplate(final int iKey)
	{
		final Expense line = ExpensePopup.this.binder.getBean();
		
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
		this.binder.removeBean();
		this.binder.setBean(line);
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
					ExpensePopup.this.setTextList(bean);
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
		this.menuBar               = new MenuBar();
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

		this.setPadding(false);
		this.verticalLayout.setPadding(false);
		this.label.setText("Spesen erfassen");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblPeriode.setText(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbPeriode::getItemLabelGenerator),
			DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.cmbPeriode.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Periode::getPerName));
		this.formItem.getElement().setAttribute("colspan", "2");
		this.lblExpBooked.setText(StringResourceUtils.optLocalizeString("{$lblExpBooked.value}", this));
		this.formItem3.getElement().setAttribute("colspan", "3");
		this.lblExpDate.setText(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setLocale(new Locale("de", "CH"));
		this.formItem4.getElement().setAttribute("colspan", "3");
		this.lblExpText.setText(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.lblExpAmount.setText(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.formItem6.getElement().setAttribute("colspan", "2");
		this.lblVat.setText("MwSt Incl");
		this.cmbVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAllInclusive()));
		this.cmbVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.formItem7.getElement().setAttribute("colspan", "3");
		this.lblProject.setText(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setEnabled(false);
		this.cmbProject.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.cmbProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.btnSearch.setIcon(IronIcons.SEARCH.create());
		this.formItem8.getElement().setAttribute("colspan", "3");
		this.lblExpAccount.setText(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new LovAccountDAO().findAllMine()));
		this.comboBoxAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(LovAccount::getName));
		this.lblExpFlagGeneric.setText(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.comboBoxGeneric.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.formItem10.getElement().setAttribute("colspan", "2");
		this.lblExpFlagCostAccount.setText(" ");
		this.chkExpFlagCostAccount
			.setLabel(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.lblExpUnit.setText(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.comboBoxUnit.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblExpQuantity.setText(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.formItem13.getElement().setAttribute("colspan", "3");
		this.lblExpState.setText(StringResourceUtils.optLocalizeString("{$lblExpState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.CANCEL.create());
		this.cmdDefault1.setText("Def 1");
		this.cmdDefault1.setIcon(VaadinIcon.BOOKMARK.create());

		this.binder.forField(this.cmbPeriode).bind("periode");
		this.binder.forField(this.dateExpDate).asRequired()
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("expDate");
		this.binder.forField(this.txtExpText).withNullRepresentation("").bind("expText");
		this.binder.forField(this.txtExpAmount).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("expAmount");
		this.binder.forField(this.cmbVat).asRequired().bind("vat");
		this.binder.forField(this.cmbProject).asRequired().bind("project");
		this.binder.forField(this.comboBoxGeneric).asRequired().bind("expFlagGeneric");
		this.binder.forField(this.chkExpFlagCostAccount).withNullRepresentation(false).bind("expFlagCostAccount");
		this.binder.forField(this.comboBoxUnit).bind("expUnit");
		this.binder.forField(this.txtExpQuantity).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("expQuantity");
		this.binder.forField(this.comboBoxState).bind("expState");
		this.binder.forField(this.dateExpBooked).withNullRepresentation(LocalDate.of(2020, Month.DECEMBER, 10))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("expBooked");

		this.menuBar.setWidth("150px");
		this.menuBar.setHeightFull();
		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.menuBar, this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblPeriode.setSizeUndefined();
		this.lblPeriode.getElement().setAttribute("slot", "label");
		this.cmbPeriode.setWidthFull();
		this.cmbPeriode.setHeight(null);
		this.formItem2.add(this.lblPeriode, this.cmbPeriode);
		this.lblExpBooked.setSizeUndefined();
		this.lblExpBooked.getElement().setAttribute("slot", "label");
		this.dateExpBooked.setWidth("30%");
		this.dateExpBooked.setHeight(null);
		this.formItem.add(this.lblExpBooked, this.dateExpBooked);
		this.lblExpDate.setSizeUndefined();
		this.lblExpDate.getElement().setAttribute("slot", "label");
		this.dateExpDate.setWidth("20%");
		this.dateExpDate.setHeight(null);
		this.formItem3.add(this.lblExpDate, this.dateExpDate);
		this.lblExpText.setSizeUndefined();
		this.lblExpText.getElement().setAttribute("slot", "label");
		this.txtExpText.setWidth("50%");
		this.txtExpText.setHeight(null);
		this.formItem4.add(this.lblExpText, this.txtExpText);
		this.lblExpAmount.setSizeUndefined();
		this.lblExpAmount.getElement().setAttribute("slot", "label");
		this.txtExpAmount.setWidthFull();
		this.txtExpAmount.setHeight(null);
		this.formItem5.add(this.lblExpAmount, this.txtExpAmount);
		this.lblVat.setSizeUndefined();
		this.lblVat.getElement().setAttribute("slot", "label");
		this.cmbVat.setWidth("40%");
		this.cmbVat.setHeight(null);
		this.formItem6.add(this.lblVat, this.cmbVat);
		this.lblProject.setSizeUndefined();
		this.lblProject.getElement().setAttribute("slot", "label");
		this.cmbProject.setWidth("45%");
		this.cmbProject.setHeight(null);
		this.btnSearch.setSizeUndefined();
		this.formItem7.add(this.lblProject, this.cmbProject, this.btnSearch);
		this.lblExpAccount.setSizeUndefined();
		this.lblExpAccount.getElement().setAttribute("slot", "label");
		this.comboBoxAccount.setWidth("23%");
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
		this.comboBoxUnit.setWidth("38%");
		this.comboBoxUnit.setHeight(null);
		this.formItem11.add(this.lblExpUnit, this.comboBoxUnit);
		this.lblExpQuantity.setSizeUndefined();
		this.lblExpQuantity.getElement().setAttribute("slot", "label");
		this.txtExpQuantity.setWidthFull();
		this.txtExpQuantity.setHeight(null);
		this.formItem12.add(this.lblExpQuantity, this.txtExpQuantity);
		this.lblExpState.setSizeUndefined();
		this.lblExpState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidth("30%");
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
	private MenuBar                       menuBar;
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
