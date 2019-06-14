package ch.xwr.seicentobilling.ui.phone;

import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
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
	}

	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.projectLine = Navigation.getParameter(event, "projectLine", ProjectLine.class);
		this.periode = Navigation.getParameter(event, "periode", Periode.class);

		if (this.projectLine != null) {
			this.fieldGroup.setItemDataSource(this.projectLine);
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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDelete}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_buttonClick(final Button.ClickEvent event) {
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

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnKey02}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnKey02_buttonClick(final Button.ClickEvent event) {
		loadTemplate(2);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnKey03}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnKey03_buttonClick(final Button.ClickEvent event) {
		loadTemplate(3);
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
		this.cmdBack = new XdevButton();
		this.label = new XdevLabel();
		this.cmdDelete = new XdevButton();
		this.form = new XdevGridLayout();
		this.lblPrlReportDate = new XdevLabel();
		this.datePrlReportDate = new XdevPopupDateField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblPrlHours = new XdevLabel();
		this.txtPrlHours = new XdevTextField();
		this.lblPrlText = new XdevLabel();
		this.txtPrlText = new XdevTextField();
		this.lblPrlWorkType = new XdevLabel();
		this.comboBoxWorktype = new XdevComboBox<>();
		this.txtPrlRate = new XdevTextField();
		this.horizontalLayoutTemplate = new XdevHorizontalLayout();
		this.btnKey01 = new XdevButton();
		this.btnKey02 = new XdevButton();
		this.btnKey03 = new XdevButton();
		this.lblPrlRate = new XdevLabel();
		this.horizontalLayoutAction = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ProjectLine.class);

		this.panel.setScrollLeft(1);
		this.panel.setScrollTop(1);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setStyleName("dark");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdBack.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.cmdBack.setCaption(StringResourceUtils.optLocalizeString("{$cmdBack.caption}", this));
		this.label.setStyleName("colored h2 bold");
		this.label.setValue("Rapport");
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDelete.setCaption(StringResourceUtils.optLocalizeString("{$cmdDelete.caption}", this));
		this.form.setMargin(new MarginInfo(false, true, true, true));
		this.lblPrlReportDate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlReportDate.value}", this));
		this.datePrlReportDate.setTabIndex(1);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(2);
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblPrlHours.setValue(StringResourceUtils.optLocalizeString("{$lblPrlHours.value}", this));
		this.txtPrlHours.setTabIndex(3);
		this.lblPrlText.setValue(StringResourceUtils.optLocalizeString("{$lblPrlText.value}", this));
		this.txtPrlText.setTabIndex(4);
		this.lblPrlWorkType.setValue(StringResourceUtils.optLocalizeString("{$lblPrlWorkType.value}", this));
		this.txtPrlRate.setTabIndex(6);
		this.horizontalLayoutTemplate.setMargin(new MarginInfo(true, true, false, false));
		this.btnKey01.setCaption("Key 1");
		this.btnKey02.setCaption("Key 2");
		this.btnKey03.setCaption("Key 3");
		this.lblPrlRate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlRate.value}", this));
		this.horizontalLayoutAction.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(8);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(7);
		this.fieldGroup.bind(this.datePrlReportDate, ProjectLine_.prlReportDate.getName());
		this.fieldGroup.bind(this.cmbProject, ProjectLine_.project.getName());
		this.fieldGroup.bind(this.txtPrlHours, ProjectLine_.prlHours.getName());
		this.fieldGroup.bind(this.txtPrlText, ProjectLine_.prlText.getName());
		this.fieldGroup.bind(this.comboBoxWorktype, ProjectLine_.prlWorkType.getName());
		this.fieldGroup.bind(this.txtPrlRate, ProjectLine_.prlRate.getName());

		this.cmdBack.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdBack);
		this.horizontalLayout.setComponentAlignment(this.cmdBack, Alignment.MIDDLE_LEFT);
		this.horizontalLayout.setExpandRatio(this.cmdBack, 10.0F);
		this.label.setSizeUndefined();
		this.horizontalLayout.addComponent(this.label);
		this.horizontalLayout.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setExpandRatio(this.label, 10.0F);
		this.cmdDelete.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDelete);
		this.horizontalLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_RIGHT);
		this.horizontalLayout.setExpandRatio(this.cmdDelete, 10.0F);
		this.btnKey01.setSizeUndefined();
		this.horizontalLayoutTemplate.addComponent(this.btnKey01);
		this.horizontalLayoutTemplate.setComponentAlignment(this.btnKey01, Alignment.MIDDLE_RIGHT);
		this.btnKey02.setSizeUndefined();
		this.horizontalLayoutTemplate.addComponent(this.btnKey02);
		this.horizontalLayoutTemplate.setComponentAlignment(this.btnKey02, Alignment.MIDDLE_RIGHT);
		this.btnKey03.setSizeUndefined();
		this.horizontalLayoutTemplate.addComponent(this.btnKey03);
		this.horizontalLayoutTemplate.setComponentAlignment(this.btnKey03, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayoutTemplate_spacer = new CustomComponent();
		horizontalLayoutTemplate_spacer.setSizeFull();
		this.horizontalLayoutTemplate.addComponent(horizontalLayoutTemplate_spacer);
		this.horizontalLayoutTemplate.setExpandRatio(horizontalLayoutTemplate_spacer, 1.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdSave);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayoutAction.addComponent(this.cmdReset);
		this.horizontalLayoutAction.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(9);
		this.lblPrlReportDate.setSizeUndefined();
		this.form.addComponent(this.lblPrlReportDate, 0, 0);
		this.datePrlReportDate.setSizeUndefined();
		this.form.addComponent(this.datePrlReportDate, 1, 0);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 1);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 1, 1);
		this.lblPrlHours.setSizeUndefined();
		this.form.addComponent(this.lblPrlHours, 0, 2);
		this.txtPrlHours.setSizeUndefined();
		this.form.addComponent(this.txtPrlHours, 1, 2);
		this.lblPrlText.setSizeUndefined();
		this.form.addComponent(this.lblPrlText, 0, 3);
		this.txtPrlText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlText, 1, 3);
		this.lblPrlWorkType.setSizeUndefined();
		this.form.addComponent(this.lblPrlWorkType, 0, 4);
		this.comboBoxWorktype.setSizeUndefined();
		this.form.addComponent(this.comboBoxWorktype, 1, 4);
		this.txtPrlRate.setSizeUndefined();
		this.form.addComponent(this.txtPrlRate, 1, 5);
		this.horizontalLayoutTemplate.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutTemplate.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayoutTemplate, 1, 6);
		this.form.setComponentAlignment(this.horizontalLayoutTemplate, Alignment.MIDDLE_RIGHT);
		this.lblPrlRate.setSizeUndefined();
		this.form.addComponent(this.lblPrlRate, 0, 5);
		this.horizontalLayoutAction.setSizeUndefined();
		this.form.addComponent(this.horizontalLayoutAction, 0, 7, 1, 7);
		this.form.setComponentAlignment(this.horizontalLayoutAction, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 10.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 8, 1, 8);
		this.form.setRowExpandRatio(8, 1.0F);
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

		this.cmdBack.addClickListener(event -> this.cmdBack_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmbProject.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				ProjectLineView.this.cmbProject_valueChange(event);
			}
		});
		this.btnKey01.addClickListener(event -> this.btnKey01_buttonClick(event));
		this.btnKey02.addClickListener(event -> this.btnKey02_buttonClick(event));
		this.btnKey03.addClickListener(event -> this.btnKey03_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdBack, cmdDelete, btnKey01, btnKey02, btnKey03, cmdSave, cmdReset;
	private XdevLabel label, lblPrlReportDate, lblProject, lblPrlHours, lblPrlText, lblPrlWorkType, lblPrlRate;
	private XdevHorizontalLayout horizontalLayout, horizontalLayoutTemplate, horizontalLayoutAction;
	private XdevPopupDateField datePrlReportDate;
	private XdevComboBox<?> comboBoxWorktype;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtPrlHours, txtPrlText, txtPrlRate;
	private XdevVerticalLayout verticalLayout;
	private XdevComboBox<Project> cmbProject;
	private XdevFieldGroup<ProjectLine> fieldGroup;
	// </generated-code>

}
