package ch.xwr.seicentobilling.ui.phone;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevMenuBar;
import com.xdev.ui.XdevMenuBar.XdevMenuItem;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.navigation.Navigation;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.ProjectLineHelper;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;
import ch.xwr.seicentobilling.entities.ProjectLine_;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectLineView extends XdevView {
	@NavigationParameter
	private ProjectLine projectLine;
	@NavigationParameter
	private Periode periode;

	/**
	 *
	 */
	public ProjectLineView() {
		super();
		this.initUI();

		// State
		//this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxWorktype.addItems((Object[]) LovState.WorkType.values());

		this.panel.getContent().setSizeUndefined();
		this.mnuSeperator2.setReadOnly(true);
	}

	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.projectLine = Navigation.getParameter(event, "projectLine", ProjectLine.class);
		this.periode = Navigation.getParameter(event, "periode", Periode.class);

		if (this.projectLine != null) {
			this.fieldGroup.setItemDataSource(this.projectLine);
			checkTemplates();
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
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		try {
			this.fieldGroup.save();

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPrlId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			goBack();

		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void goBack() {
		Navigation.to("projectLineListView").parameter("periode", this.periode).navigate();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdBack}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdBack_buttonClick(final Button.ClickEvent event) {
		goBack();
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbProject}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbProject_valueChange(final Property.ValueChangeEvent event) {
		if (this.fieldGroup.getItemDataSource().getBean().getPrlId() == null) {
			if (this.cmbProject.isModified()) {
				final Project obj = (Project) event.getProperty().getValue();
				this.txtPrlRate.setValue("" + obj.getProRate());
			}
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnKey01}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnKey01_buttonClick(final Button.ClickEvent event) {
		loadTemplate(1);
	}

	private void loadTemplate(final int iKey) {
		final ProjectLine line = this.fieldGroup.getItemDataSource().getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final ProjectLineTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null)
		 {
			return;	//not found
		}

		line.setPrlHours(tpl.getPrtHours());
		line.setPrlRate(tpl.getPrtRate());
		line.setPrlText(tpl.getprtText());
		line.setPrlWorkType(tpl.getprtWorkType());
		line.setProject(tpl.getProject());
		line.setPrlState(tpl.getPrtState());

		this.fieldGroup.setItemDataSource(line);
	}

	private void checkTemplates() {
		final ProjectLine line = this.fieldGroup.getItemDataSource().getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final List<ProjectLineTemplate> lst = dao.findByCostAccount(line.getPeriode().getCostAccount());

		XdevMenuItem item = null;

		for (int i = 1; i < 11; i++) {
			item = getMnItem(i);
			item.setEnabled(false);
			item.setVisible(false);
		}

		if (lst == null)
		{
			return;	//not found
		}

		for (final Iterator<ProjectLineTemplate> iterator = lst.iterator(); iterator.hasNext();) {
			final ProjectLineTemplate tpl = iterator.next();
			final int nbr = tpl.getPrtKeyNumber();
			item = getMnItem(nbr);

			item.setEnabled(true);
			item.setVisible(true);
			item.setCaption("" + nbr + ": " + tpl.getProject().getProName());
		}

	}

	private XdevMenuItem getMnItem(final int icount) {
		switch (icount) {
			case 1: return this.mnuTemplate1;
			case 2: return this.mnuTemplate2;
			case 3: return this.mnuTemplate3;
			case 4: return this.mnuTemplate4;
			case 5: return this.mnuTemplate5;
			case 6: return this.mnuTemplate6;
			case 7: return this.mnuTemplate7;
			case 8: return this.mnuTemplate8;
			case 9: return this.mnuTemplate9;
			case 10: return this.mnuTemplate10;
		}

		return null;
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuDeleteItem}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuDeleteItem_menuSelected(final MenuBar.MenuItem selectedItem) {
		ConfirmDialog.show(getUI(), "Datensatz löschen", "Wirklich löschen?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					doDelete();
				}
			}

			private void doDelete() {
				final ProjectLine bean = ProjectLineView.this.fieldGroup.getItemDataSource().getBean();
				if (bean.getPrlId() > 0) {
					// Update RowObject
					final RowObjectManager man = new RowObjectManager();
					man.deleteObject(bean.getPrlId(), bean.getClass().getSimpleName());
					// Delete Record
					final ProjectLineDAO dao = new ProjectLineDAO();
					dao.remove(bean);
				}

				goBack();
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate1}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate1_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(1);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate2}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate2_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(2);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate3}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate3_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(3);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuStartStop}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuStartStop_menuSelected(final MenuBar.MenuItem selectedItem) {
		final Date d1 = this.datePrlReportDate.getValue();
		if (d1 == null) {
			return;
		}

		final ProjectLineHelper hlp = new ProjectLineHelper();
		final Date retDate = hlp.getStartStopTime(d1);

		if (this.datePrlReportDateFrom.getValue() == null ) {
			this.datePrlReportDateFrom.setValue(retDate);
		} else {
			if (this.datePrlReportDateTo.getValue() == null ) {
				this.datePrlReportDateTo.setValue(retDate);
			}
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate4}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate4_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(4);
	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePrlReportDateFrom}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateFrom_valueChange(final Property.ValueChangeEvent event) {
		validateTimeFromTo();
		//if (!this.datePrlReportDateFrom.isModified()) {
			calcDurationFromTime();
		//}

	}

	private void validateTimeFromTo() {
		Date d1 = this.datePrlReportDate.getValue();
		Date dateFrom = this.datePrlReportDateFrom.getValue();
		Date dateTo = this.datePrlReportDateTo.getValue();
		final ProjectLineHelper hlp = new ProjectLineHelper();

		if (d1 == null) {
			d1 = new Date();
		}

		if (dateFrom != null) {
			dateFrom = hlp.getDateCorrect(d1, dateFrom);
			this.datePrlReportDateFrom.setValue(dateFrom);
		}
		if (dateTo != null) {
			dateTo = hlp.getDateCorrect(d1, dateTo);
			if (dateFrom != null && dateTo.before(dateFrom)) {
				dateTo = dateFrom;
			}
			this.datePrlReportDateTo.setValue(dateTo);
		}
	}

	private void calcDurationFromTime() {
		final Date fromHH = this.datePrlReportDateFrom.getValue();
		final Date toHH = this.datePrlReportDateTo.getValue();

		if (fromHH == null || toHH == null) {
			return;
		}

		final ProjectLineHelper hlp = new ProjectLineHelper();
		final double hours = hlp.calcDurationFromTime(fromHH, toHH);

		this.txtPrlHours.setValue(new DecimalFormat("####.##").format(hours));
	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePrlReportDateTo}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePrlReportDateTo_valueChange(final Property.ValueChangeEvent event) {
		validateTimeFromTo();
		//if (!this.datePrlReportDateTo.isModified()) {
			calcDurationFromTime();
		//}

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate5}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate5_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(5);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate6}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate6_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(6);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate7}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate7_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(7);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate8}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate8_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(8);
	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate9}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate9_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(9);

	}

	/**
	 * Event handler delegate method for the {@link XdevMenuBar.XdevMenuItem}
	 * {@link #mnuTemplate10}.
	 *
	 * @see MenuBar.Command#menuSelected(MenuBar.MenuItem)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void mnuTemplate10_menuSelected(final MenuBar.MenuItem selectedItem) {
		loadTemplate(10);
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.menuBarLeftTop = new XdevMenuBar();
		this.mnuOperation = this.menuBarLeftTop.addItem("Optionen", null);
		this.mnuStartStop = this.mnuOperation.addItem("Start/Stop", null);
		this.mnuSeperator2 = this.mnuOperation.addSeparator();
		this.mnuDeleteItem = this.mnuOperation.addItem("Löschen", null);
		this.mnuDefaults = this.menuBarLeftTop.addItem("Template", null);
		this.mnuTemplate1 = this.mnuDefaults.addItem("Spesen", null);
		this.mnuTemplate2 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate3 = this.mnuDefaults.addItem("Vorlagen Rapport", null);
		this.mnuTemplate4 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate5 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate6 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate7 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate8 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate9 = this.mnuDefaults.addItem("Rapporte", null);
		this.mnuTemplate10 = this.mnuDefaults.addItem("Rapporte", null);
		this.menuItem = this.menuBarLeftTop.addItem("Text", null);
		this.label = new XdevLabel();
		this.cmdBack = new XdevButton();
		this.form = new XdevGridLayout();
		this.lblPrlReportDate = new XdevLabel();
		this.datePrlReportDate = new XdevPopupDateField();
		this.lblPrlReportFromTo = new XdevLabel();
		this.datePrlReportDateFrom = new XdevPopupDateField();
		this.datePrlReportDateTo = new XdevPopupDateField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblPrlHours = new XdevLabel();
		this.txtPrlHours = new XdevTextField();
		this.lblPrlRate = new XdevLabel();
		this.txtPrlRate = new XdevTextField();
		this.lblPrlText = new XdevLabel();
		this.txtPrlText = new XdevTextField();
		this.lblPrlWorkType = new XdevLabel();
		this.comboBoxWorktype = new XdevComboBox<>();
		this.horizontalLayoutAction = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.btnKey01 = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ProjectLine.class);

		this.panel.setScrollLeft(1);
		this.panel.setScrollTop(1);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setStyleName("dark");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.mnuOperation.setIcon(FontAwesome.NAVICON);
		this.mnuOperation.setStyleName("large-icons");
		this.mnuStartStop.setIcon(FontAwesome.CLOCK_O);
		this.mnuDeleteItem
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.mnuDeleteItem.setCheckable(true);
		this.menuItem.setEnabled(false);
		this.menuItem.setVisible(false);
		this.label.setStyleName("colored h2 bold");
		this.label.setValue("Rapport");
		this.cmdBack.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.cmdBack.setCaption(StringResourceUtils.optLocalizeString("{$cmdBack.caption}", this));
		this.form.setMargin(new MarginInfo(false, true, true, true));
		this.lblPrlReportDate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlReportDate.value}", this));
		this.datePrlReportDate.setTabIndex(1);
		this.lblPrlReportFromTo.setValue("Von/Bis");
		this.datePrlReportDateFrom.setDateFormat("HH:mm");
		this.datePrlReportDateFrom.setResolution(Resolution.MINUTE);
		this.datePrlReportDateTo.setDateFormat("HH:mm");
		this.datePrlReportDateTo.setResolution(Resolution.MINUTE);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(2);
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblPrlHours.setValue(StringResourceUtils.optLocalizeString("{$lblPrlHours.value}", this));
		this.txtPrlHours.setTabIndex(3);
		this.lblPrlRate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlRate.value}", this));
		this.txtPrlRate.setTabIndex(6);
		this.lblPrlText.setValue(StringResourceUtils.optLocalizeString("{$lblPrlText.value}", this));
		this.txtPrlText.setTabIndex(4);
		this.lblPrlWorkType.setValue(StringResourceUtils.optLocalizeString("{$lblPrlWorkType.value}", this));
		this.horizontalLayoutAction.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(8);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(7);
		this.btnKey01.setCaption("Key 1");
		this.fieldGroup.bind(this.datePrlReportDate, ProjectLine_.prlReportDate.getName());
		this.fieldGroup.bind(this.cmbProject, ProjectLine_.project.getName());
		this.fieldGroup.bind(this.txtPrlHours, ProjectLine_.prlHours.getName());
		this.fieldGroup.bind(this.txtPrlText, ProjectLine_.prlText.getName());
		this.fieldGroup.bind(this.comboBoxWorktype, ProjectLine_.prlWorkType.getName());
		this.fieldGroup.bind(this.txtPrlRate, ProjectLine_.prlRate.getName());
		this.fieldGroup.bind(this.datePrlReportDateFrom, ProjectLine_.prlTimeFrom.getName());
		this.fieldGroup.bind(this.datePrlReportDateTo, ProjectLine_.prlTimeTo.getName());

		this.menuBarLeftTop.setWidth(100, Unit.PERCENTAGE);
		this.menuBarLeftTop.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.menuBarLeftTop);
		this.horizontalLayout.setComponentAlignment(this.menuBarLeftTop, Alignment.MIDDLE_LEFT);
		this.horizontalLayout.setExpandRatio(this.menuBarLeftTop, 10.0F);
		this.label.setSizeUndefined();
		this.horizontalLayout.addComponent(this.label);
		this.horizontalLayout.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setExpandRatio(this.label, 10.0F);
		this.cmdBack.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdBack);
		this.horizontalLayout.setComponentAlignment(this.cmdBack, Alignment.MIDDLE_RIGHT);
		this.horizontalLayout.setExpandRatio(this.cmdBack, 10.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdSave);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdReset);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.btnKey01.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.btnKey01);
		this.horizontalLayoutAction.setComponentAlignment(this.btnKey01, Alignment.MIDDLE_RIGHT);
		this.form.setColumns(2);
		this.form.setRows(10);
		this.lblPrlReportDate.setSizeUndefined();
		this.form.addComponent(this.lblPrlReportDate, 0, 0);
		this.datePrlReportDate.setSizeUndefined();
		this.form.addComponent(this.datePrlReportDate, 1, 0);
		this.lblPrlReportFromTo.setSizeUndefined();
		this.form.addComponent(this.lblPrlReportFromTo, 0, 1);
		this.datePrlReportDateFrom.setWidth(100, Unit.PIXELS);
		this.datePrlReportDateFrom.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.datePrlReportDateFrom, 1, 1);
		this.datePrlReportDateTo.setWidth(100, Unit.PIXELS);
		this.datePrlReportDateTo.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.datePrlReportDateTo, 1, 2);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 3);
		this.cmbProject.setWidth(80, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 1, 3);
		this.lblPrlHours.setSizeUndefined();
		this.form.addComponent(this.lblPrlHours, 0, 4);
		this.txtPrlHours.setSizeUndefined();
		this.form.addComponent(this.txtPrlHours, 1, 4);
		this.lblPrlRate.setSizeUndefined();
		this.form.addComponent(this.lblPrlRate, 0, 5);
		this.txtPrlRate.setSizeUndefined();
		this.form.addComponent(this.txtPrlRate, 1, 5);
		this.lblPrlText.setSizeUndefined();
		this.form.addComponent(this.lblPrlText, 0, 6);
		this.txtPrlText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlText, 1, 6);
		this.lblPrlWorkType.setSizeUndefined();
		this.form.addComponent(this.lblPrlWorkType, 0, 7);
		this.comboBoxWorktype.setSizeUndefined();
		this.form.addComponent(this.comboBoxWorktype, 1, 7);
		this.horizontalLayoutAction.setSizeUndefined();
		this.form.addComponent(this.horizontalLayoutAction, 0, 8, 1, 8);
		this.form.setComponentAlignment(this.horizontalLayoutAction, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 10.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 9, 1, 9);
		this.form.setRowExpandRatio(9, 1.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.form.setWidth(100, Unit.PERCENTAGE);
		this.form.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.form);
		this.verticalLayout.setComponentAlignment(this.form, Alignment.MIDDLE_LEFT);
		final CustomComponent verticalLayout_spacer = new CustomComponent();
		verticalLayout_spacer.setSizeFull();
		this.verticalLayout.addComponent(verticalLayout_spacer);
		this.verticalLayout.setExpandRatio(verticalLayout_spacer, 1.0F);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.mnuStartStop.setCommand(selectedItem -> this.mnuStartStop_menuSelected(selectedItem));
		this.mnuDeleteItem.setCommand(selectedItem -> this.mnuDeleteItem_menuSelected(selectedItem));
		this.mnuTemplate1.setCommand(selectedItem -> this.mnuTemplate1_menuSelected(selectedItem));
		this.mnuTemplate2.setCommand(selectedItem -> this.mnuTemplate2_menuSelected(selectedItem));
		this.mnuTemplate3.setCommand(selectedItem -> this.mnuTemplate3_menuSelected(selectedItem));
		this.mnuTemplate4.setCommand(selectedItem -> this.mnuTemplate4_menuSelected(selectedItem));
		this.mnuTemplate5.setCommand(selectedItem -> this.mnuTemplate5_menuSelected(selectedItem));
		this.mnuTemplate6.setCommand(selectedItem -> this.mnuTemplate6_menuSelected(selectedItem));
		this.mnuTemplate7.setCommand(selectedItem -> this.mnuTemplate7_menuSelected(selectedItem));
		this.mnuTemplate8.setCommand(selectedItem -> this.mnuTemplate8_menuSelected(selectedItem));
		this.mnuTemplate9.setCommand(selectedItem -> this.mnuTemplate9_menuSelected(selectedItem));
		this.mnuTemplate10.setCommand(selectedItem -> this.mnuTemplate10_menuSelected(selectedItem));
		this.cmdBack.addClickListener(event -> this.cmdBack_buttonClick(event));
		this.datePrlReportDateFrom.addValueChangeListener(event -> this.datePrlReportDateFrom_valueChange(event));
		this.datePrlReportDateTo.addValueChangeListener(event -> this.datePrlReportDateTo_valueChange(event));
		this.cmbProject.addValueChangeListener(event -> this.cmbProject_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
		this.btnKey01.addClickListener(event -> this.btnKey01_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, lblPrlReportDate, lblPrlReportFromTo, lblProject, lblPrlHours, lblPrlRate, lblPrlText,
			lblPrlWorkType;
	private XdevButton cmdBack, cmdSave, cmdReset, btnKey01;
	private XdevMenuBar menuBarLeftTop;
	private XdevMenuItem mnuOperation, mnuStartStop, mnuSeperator2, mnuDeleteItem, mnuDefaults, mnuTemplate1, mnuTemplate2,
			mnuTemplate3, mnuTemplate4, mnuTemplate5, mnuTemplate6, mnuTemplate7, mnuTemplate8, mnuTemplate9, mnuTemplate10,
			menuItem;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevHorizontalLayout horizontalLayout, horizontalLayoutAction;
	private XdevPopupDateField datePrlReportDate, datePrlReportDateFrom, datePrlReportDateTo;
	private XdevComboBox<?> comboBoxWorktype;
	private XdevTextField txtPrlHours, txtPrlRate, txtPrlText;
	private XdevVerticalLayout verticalLayout;
	private XdevFieldGroup<ProjectLine> fieldGroup;
	// </generated-code>

}
