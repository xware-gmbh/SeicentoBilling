
package ch.xwr.seicentobilling.ui;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.LovState.WorkType;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.ProjectLineHelper;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;
import ch.xwr.seicentobilling.ui.project.ProjectLookupPopup;


public class ProjectLinePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProjectLinePopup.class);
	
	/**
	
	 */
	public ProjectLinePopup()
	{
		super();
		this.initUI();

		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));

		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxWorktype.setItems(LovState.WorkType.values());
		
		// get Parameter
		final Long  beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId"); // projectline
		final Long  objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");  // Periode
		ProjectLine bean   = null;
		Periode     obj    = null;
		
		if(beanId == null)
		{
			// new
			final PeriodeDAO objDao = new PeriodeDAO();
			obj = objDao.find(objId);
			
			bean = new ProjectLine();
			bean.setPrlState(LovState.State.active);
			bean.setPrlWorkType(LovState.WorkType.project);
			bean.setPrlReportDate(new Date());
			bean.setPeriode(obj);
			
		}
		else
		{
			final ProjectLineDAO dao = new ProjectLineDAO();
			bean = dao.find(beanId.longValue());
			
			this.prepareProjectCombo(bean.getProject());
		}
		
		this.setBeanGui(bean);
		this.setTextCombo(bean.getProject());
		this.checkTemplates();

		if(beanId != null)
		{
			this.setDateToLocalTime(bean.getPrlTimeFrom(), this.datePrlReportDateFrom);
			this.setDateToLocalTime(bean.getPrlTimeTo(), this.datePrlReportDateTo);
		}
	}

	private void checkTemplates()
	{
		final ProjectLine line = this.binder.getBean();

		final ProjectLineTemplateDAO    dao = new ProjectLineTemplateDAO();
		final List<ProjectLineTemplate> lst = dao.findByCostAccount(line.getPeriode().getCostAccount());
		
		this.vorLageComboBox.setItems(lst);
	}
	
	private void setBeanGui(final ProjectLine bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);
		// this.fieldGroupProject.setItemDataSource(bean.getProject());
		// this.lookupField.setCon
		
		this.setROFields();
		
		// focus
		this.datePrlReportDate.focus();
	}
	
	private void setROFields()
	{
		// Readonly
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
		win.add(cancelButton, new ProjectLinePopup());
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
		if(!this.areFieldsValid())
		{
			return;
		}

		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
		if(SeicentoCrud.doSave(this.binder, new ProjectLineDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getPrlId(),
					this.binder.getBean().getClass().getSimpleName());

				((Dialog)this.getParent().get()).close();
				Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				ProjectLinePopup.LOG.error("could not save ObjRoot", e);
			}
		}
		
	}

	private boolean areFieldsValid()
	{
		if(this.binder.isValid())
		{
			return true;
		}
		this.binder.validate();
		
		return false;
	}

	private void setTextCombo(final Project project)
	{
		if(project == null)
		{
			final List<ProjectLine> projectList = new ArrayList<>();
			this.textComboBox.setItems(projectList);
			
		}
		else
		{
			final List<ProjectLine> projectList = new ProjectLineDAO().findByProject(project);
			
			this.textComboBox.setItems(projectList);
		}
		
	}
	
	private void loadTemplate(final int iKey)
	{
		final ProjectLine line = this.binder.getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final ProjectLineTemplate    tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if(tpl == null)
		{
			return; // not found
		}
		
		final LocalDate dar = this.datePrlReportDate.getValue();
		if(dar != null)
		{
			line.setPrlReportDate(Date.from(dar.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		}

		line.setPrlHours(tpl.getPrtHours());
		line.setPrlRate(tpl.getPrtRate());
		line.setPrlText(tpl.getprtText());
		line.setPrlWorkType(tpl.getprtWorkType());
		line.setProject(tpl.getProject());
		line.setPrlState(tpl.getPrtState());
		this.datePrlReportDateFrom.setValue(null);
		this.datePrlReportDateTo.setValue(null);

		this.prepareProjectCombo(tpl.getProject());
		this.binder.removeBean();
		this.binder.setBean(line);

		this.setROFields();
		this.vorLageComboBox.setValue(tpl);
		
	}
	
	private void prepareProjectCombo(final Project bean)
	{
		ProjectLinePopup.this.cmbProject.setValue(bean);
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
					ProjectLinePopup.this.cmbProject.setValue(bean);

				}
			}
		});
		win.open();
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
	
	private void validateTimeFromTo()
	{
		LocalDate               d1       = this.datePrlReportDate.getValue();
		final LocalTime         dateFrom = this.datePrlReportDateFrom.getValue();
		LocalTime               dateTo   = this.datePrlReportDateTo.getValue();
		final ProjectLineHelper hlp      = new ProjectLineHelper();
		
		if(d1 == null)
		{
			d1 = LocalDate.now();
		}
		
		if(dateFrom != null)
		{
			final Date df = hlp.localTimeToDate(d1, dateFrom);
			this.binder.getBean().setPrlTimeFrom(df);
		}
		if(dateTo != null)
		{
			final Date df = hlp.localTimeToDate(d1, dateFrom);
			final Date dt = hlp.localTimeToDate(d1, dateTo);
			this.binder.getBean().setPrlTimeFrom(dt);

			if(dateFrom != null && dateTo.isBefore(dateFrom))
			{
				dateTo = dateFrom;
				this.datePrlReportDateTo.setValue(dateTo);
				this.binder.getBean().setPrlTimeFrom(df);
			}
		}
	}

	public Date localDateToDate(final LocalDate ld)
	{
		return Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public LocalDate dateToLocalDate(final Date date)
	{
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	private void calcDurationFromTime()
	{
		final LocalTime fromHH = this.datePrlReportDateFrom.getValue();
		final LocalTime toHH   = this.datePrlReportDateTo.getValue();
		
		if(fromHH == null || toHH == null)
		{
			return;
		}
		
		final ProjectLineHelper hlp   = new ProjectLineHelper();
		final double            hours =
			hlp.calcDurationFromTime(fromHH, toHH);
		
		this.txtPrlHours.setValue(new DecimalFormat("####.##").format(hours));
	}
	
	private void handleStartStop()
	{
		final LocalDate d1 = this.datePrlReportDate.getValue();
		if(d1 == null)
		{
			return;
		}

		final ProjectLineHelper hlp     = new ProjectLineHelper();
		final Date              retDate = hlp.getStartStopTime(this.localDateToDate(d1));

		if(this.datePrlReportDateFrom.getValue() == null)
		{
			this.setDateToLocalTime(retDate, this.datePrlReportDateFrom);

		}
		else
		{
			if(this.datePrlReportDateTo.getValue() == null)
			{
				this.setDateToLocalTime(retDate, this.datePrlReportDateTo);
			}
		}
	}
	
	private void setDateToLocalTime(final Date retDate, final TimePicker timePicker)
	{
		if(retDate == null)
		{
			return;
		}
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(retDate);
		timePicker
			.setValue(LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdStartStop}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStartStop_onClick(final ClickEvent<Button> event)
	{
		this.handleStartStop();
	}
	
	/**
	 * Event handler delegate method for the {@link TimePicker} {@link #datePrlReportDateFrom}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateFrom_valueChanged(final ComponentValueChangeEvent<TimePicker, LocalTime> event)
	{
		this.validateTimeFromTo();
		this.calcDurationFromTime();
	}
	
	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #vorLageComboBox}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void vorLageComboBox_valueChanged(
		final ComponentValueChangeEvent<ComboBox<ProjectLineTemplate>, ProjectLineTemplate> event)
	{

		final ProjectLineTemplate tpl  = event.getValue();
		final ProjectLine         line = this.binder.getBean();
		if(tpl == null)
		{
			return; // not found
		}
		
		final LocalDate dar = this.datePrlReportDate.getValue();
		if(dar != null)
		{
			line.setPrlReportDate(Date.from(dar.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		}

		line.setPrlHours(tpl.getPrtHours());
		line.setPrlRate(tpl.getPrtRate());
		line.setPrlText(tpl.getprtText());
		line.setPrlWorkType(tpl.getprtWorkType());
		line.setProject(tpl.getProject());
		line.setPrlState(tpl.getPrtState());
		this.datePrlReportDateFrom.setValue(null);
		this.datePrlReportDateTo.setValue(null);
		
		this.prepareProjectCombo(tpl.getProject());
		this.setROFields();
		this.binder.removeBean();
		this.binder.setBean(line);
		
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #textComboBox}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void textComboBox_valueChanged(final ComponentValueChangeEvent<ComboBox<ProjectLine>, ProjectLine> event)
	{
		ProjectLinePopup.this.txtPrlText.setValue(event.getValue().getPrlText());
	}
	
	/**
	 * Event handler delegate method for the {@link TimePicker} {@link #datePrlReportDateTo}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateTo_valueChanged(final ComponentValueChangeEvent<TimePicker, LocalTime> event)
	{
		this.validateTimeFromTo();
		this.calcDurationFromTime();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout        = new VerticalLayout();
		this.horizontalLayout      = new HorizontalLayout();
		this.label                 = new Label();
		this.formLayout            = new FormLayout();
		this.formItem14            = new FormItem();
		this.vorlageLabel          = new Label();
		this.vorLageComboBox       = new ComboBox<>();
		this.formItem15            = new FormItem();
		this.label2                = new Label();
		this.textComboBox          = new ComboBox<>();
		this.formItem2             = new FormItem();
		this.lblPeriode            = new Label();
		this.cmbPeriode            = new ComboBox<>();
		this.formItem              = new FormItem();
		this.lblPrlReportDate      = new Label();
		this.datePrlReportDate     = new DatePicker();
		this.formItem3             = new FormItem();
		this.lblPrlFromTo          = new Label();
		this.datePrlReportDateFrom = new TimePicker();
		this.datePrlReportDateTo   = new TimePicker();
		this.formItem7             = new FormItem();
		this.lblProject            = new Label();
		this.cmbProject            = new ComboBox<>();
		this.btnSearch             = new Button();
		this.formItem8             = new FormItem();
		this.lblPrlHours           = new Label();
		this.txtPrlHours           = new TextField();
		this.formItem9             = new FormItem();
		this.lblPrlRate            = new Label();
		this.txtPrlRate            = new TextField();
		this.formItem11            = new FormItem();
		this.lblPrlText            = new Label();
		this.txtPrlText            = new TextField();
		this.formItem12            = new FormItem();
		this.lblPrlWorkType        = new Label();
		this.comboBoxWorktype      = new ComboBox<>();
		this.formItem13            = new FormItem();
		this.lblPrlState           = new Label();
		this.comboBoxState         = new ComboBox<>();
		this.horizontalLayout2     = new HorizontalLayout();
		this.cmdSave               = new Button();
		this.cmdCancel             = new Button();
		this.cmdStartStop          = new Button();
		this.cmdDefault1           = new Button();
		this.binder                = new BeanValidationBinder<>(ProjectLine.class);
		
		this.verticalLayout.setPadding(false);
		this.label.setText("Rapporte erfassen");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.vorlageLabel.setText("Vorlage");
		this.vorLageComboBox.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(ProjectLineTemplate::getprtText));
		this.formItem15.getElement().setAttribute("colspan", "2");
		this.label2.setText("Text");
		this.textComboBox.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(ProjectLine::getPrlText));
		this.formItem2.getElement().setAttribute("colspan", "3");
		this.lblPeriode.setText(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbPeriode::getItemLabelGenerator),
			DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.cmbPeriode.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Periode::getPerName));
		this.lblPrlReportDate.setText(StringResourceUtils.optLocalizeString("{$lblPrlReportDate.value}", this));
		this.formItem3.getElement().setAttribute("colspan", "2");
		this.lblPrlFromTo.setText("Von/Bis");
		this.formItem7.getElement().setAttribute("colspan", "3");
		this.lblProject.setText(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setEnabled(false);
		this.cmbProject.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.cmbProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.btnSearch.setIcon(IronIcons.SEARCH.create());
		this.lblPrlHours.setText(StringResourceUtils.optLocalizeString("{$lblPrlHours.value}", this));
		this.formItem9.getElement().setAttribute("colspan", "2");
		this.lblPrlRate.setText(StringResourceUtils.optLocalizeString("{$lblPrlRate.value}", this));
		this.formItem11.getElement().setAttribute("colspan", "3");
		this.lblPrlText.setText(StringResourceUtils.optLocalizeString("{$lblPrlText.value}", this));
		this.lblPrlWorkType.setText(StringResourceUtils.optLocalizeString("{$lblPrlWorkType.value}", this));
		this.comboBoxWorktype.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblPrlState.setText(StringResourceUtils.optLocalizeString("{$lblPrlState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdCancel.setText(StringResourceUtils.optLocalizeString("{$cmdCancel.caption}", this));
		this.cmdCancel.setIcon(IronIcons.CANCEL.create());
		this.cmdStartStop.setText("Start/Stop");
		this.cmdStartStop.setIcon(VaadinIcon.CLOCK.create());
		this.cmdDefault1.setText("Def 1");
		this.cmdDefault1.setIcon(VaadinIcon.BOOKMARK.create());
		
		this.binder.forField(this.cmbPeriode).bind("periode");
		this.binder.forField(this.cmbProject).asRequired().bind("project");
		this.binder.forField(this.datePrlReportDate).asRequired()
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build()).bind("prlReportDate");
		this.binder.forField(this.txtPrlHours).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("prlHours");
		this.binder.forField(this.txtPrlRate).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("prlRate");
		this.binder.forField(this.txtPrlText).withNullRepresentation("").bind("prlText");
		this.binder.forField(this.comboBoxWorktype).bind("prlWorkType");
		this.binder.forField(this.comboBoxState).bind("prlState");
		
		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.vorlageLabel.setSizeUndefined();
		this.vorlageLabel.getElement().setAttribute("slot", "label");
		this.vorLageComboBox.setWidthFull();
		this.vorLageComboBox.setHeight(null);
		this.formItem14.add(this.vorlageLabel, this.vorLageComboBox);
		this.label2.setSizeUndefined();
		this.label2.getElement().setAttribute("slot", "label");
		this.textComboBox.setWidth("40%");
		this.textComboBox.setHeight(null);
		this.formItem15.add(this.label2, this.textComboBox);
		this.lblPeriode.setSizeUndefined();
		this.lblPeriode.getElement().setAttribute("slot", "label");
		this.cmbPeriode.setWidth("30%");
		this.cmbPeriode.setHeight(null);
		this.formItem2.add(this.lblPeriode, this.cmbPeriode);
		this.lblPrlReportDate.setSizeUndefined();
		this.lblPrlReportDate.getElement().setAttribute("slot", "label");
		this.datePrlReportDate.setWidthFull();
		this.datePrlReportDate.setHeight(null);
		this.formItem.add(this.lblPrlReportDate, this.datePrlReportDate);
		this.lblPrlFromTo.setSizeUndefined();
		this.lblPrlFromTo.getElement().setAttribute("slot", "label");
		this.datePrlReportDateFrom.setSizeUndefined();
		this.datePrlReportDateTo.setSizeUndefined();
		this.formItem3.add(this.lblPrlFromTo, this.datePrlReportDateFrom, this.datePrlReportDateTo);
		this.lblProject.setSizeUndefined();
		this.lblProject.getElement().setAttribute("slot", "label");
		this.cmbProject.setWidth("40%");
		this.cmbProject.setHeight(null);
		this.btnSearch.setSizeUndefined();
		this.formItem7.add(this.lblProject, this.cmbProject, this.btnSearch);
		this.lblPrlHours.setSizeUndefined();
		this.lblPrlHours.getElement().setAttribute("slot", "label");
		this.txtPrlHours.setWidthFull();
		this.txtPrlHours.setHeight(null);
		this.formItem8.add(this.lblPrlHours, this.txtPrlHours);
		this.lblPrlRate.setSizeUndefined();
		this.lblPrlRate.getElement().setAttribute("slot", "label");
		this.txtPrlRate.setWidth("40%");
		this.txtPrlRate.setHeight(null);
		this.formItem9.add(this.lblPrlRate, this.txtPrlRate);
		this.lblPrlText.setSizeUndefined();
		this.lblPrlText.getElement().setAttribute("slot", "label");
		this.txtPrlText.setWidth("60%");
		this.txtPrlText.setHeight(null);
		this.formItem11.add(this.lblPrlText, this.txtPrlText);
		this.lblPrlWorkType.setSizeUndefined();
		this.lblPrlWorkType.getElement().setAttribute("slot", "label");
		this.comboBoxWorktype.setWidthFull();
		this.comboBoxWorktype.setHeight(null);
		this.formItem12.add(this.lblPrlWorkType, this.comboBoxWorktype);
		this.lblPrlState.setSizeUndefined();
		this.lblPrlState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem13.add(this.lblPrlState, this.comboBoxState);
		this.formLayout.add(this.formItem14, this.formItem15, this.formItem2, this.formItem, this.formItem3,
			this.formItem7,
			this.formItem8, this.formItem9, this.formItem11, this.formItem12, this.formItem13);
		this.cmdSave.setSizeUndefined();
		this.cmdCancel.setSizeUndefined();
		this.cmdStartStop.setSizeUndefined();
		this.cmdDefault1.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdSave, this.cmdCancel, this.cmdStartStop, this.cmdDefault1);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();
		
		this.vorLageComboBox.addValueChangeListener(this::vorLageComboBox_valueChanged);
		this.textComboBox.addValueChangeListener(this::textComboBox_valueChanged);
		this.datePrlReportDateFrom.addValueChangeListener(this::datePrlReportDateFrom_valueChanged);
		this.datePrlReportDateTo.addValueChangeListener(this::datePrlReportDateTo_valueChanged);
		this.btnSearch.addClickListener(this::btnSearch_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdCancel.addClickListener(this::cmdCancel_onClick);
		this.cmdStartStop.addClickListener(this::cmdStartStop_onClick);
		this.cmdDefault1.addClickListener(this::cmdDefault1_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private ComboBox<ProjectLineTemplate>     vorLageComboBox;
	private ComboBox<WorkType>                comboBoxWorktype;
	private VerticalLayout                    verticalLayout;
	private HorizontalLayout                  horizontalLayout, horizontalLayout2;
	private Label                             label, vorlageLabel, label2, lblPeriode, lblPrlReportDate, lblPrlFromTo,
		lblProject, lblPrlHours, lblPrlRate, lblPrlText, lblPrlWorkType, lblPrlState;
	private TimePicker                        datePrlReportDateFrom, datePrlReportDateTo;
	private ComboBox<ProjectLine>             textComboBox;
	private FormItem                          formItem14, formItem15, formItem2, formItem, formItem3, formItem7,
		formItem8,
		formItem9, formItem11, formItem12, formItem13;
	private FormLayout                        formLayout;
	private Button                            btnSearch, cmdSave, cmdCancel, cmdStartStop, cmdDefault1;
	private BeanValidationBinder<ProjectLine> binder;
	private ComboBox<State>                   comboBoxState;
	private DatePicker                        datePrlReportDate;
	private ComboBox<Periode>                 cmbPeriode;
	private ComboBox<Project>                 cmbProject;
	private TextField                         txtPrlHours, txtPrlRate, txtPrlText;
	// </generated-code>

}
