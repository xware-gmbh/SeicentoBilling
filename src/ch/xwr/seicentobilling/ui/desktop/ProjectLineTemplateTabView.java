package ch.xwr.seicentobilling.ui.desktop;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.masterdetail.MasterDetail;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate_;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectLineTemplateTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProjectLineTemplateTabView.class);

	/**
	 *
	 */
	public ProjectLineTemplateTabView() {
		super();
		this.initUI();

		//Type
		this.comboBoxState.addItems((Object[])LovState.State.values());
		this.comboBoxWorkType.addItems((Object[]) LovState.WorkType.values());

		//sort Table
		final Object[] properties2 = { "prtKeyNumber", "prtWorkType"};
		final boolean[] ordering2 = { true, false };
		this.table.sort(properties2, ordering2);

		setDefaultFilter();

		//RO
		//set RO Fields
		this.fieldGroup.setItemDataSource(getNewDaoWithDefaults());
		setROFields();
	}

	private void setROFields() {
		this.cmbCostAccount.setEnabled(false);
	}

	private void setDefaultFilter() {
		final CostAccount bean = Seicento.getLoggedInCostAccount();

		final LovState.State[] valState = new LovState.State[]{LovState.State.active};
		final CostAccount[] val2 = new CostAccount[]{bean};
		final FilterData[] fd = new FilterData[]{new FilterData("costAccount", new FilterOperator.Is(), val2),
				new FilterData("prtState", new FilterOperator.Is(), valState)};

		this.containerFilterComponent.setFilterData(fd);

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
		this.fieldGroup.save();

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPrtId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		LOG.debug("Record saved ProjectLineTemplate");

		reloadMainTable();
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.setItemDataSource(getNewDaoWithDefaults());
		setROFields();
	}

	private ProjectLineTemplate getNewDaoWithDefaults() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0);	//Dev Mode
		}

		final ProjectLineTemplate dao = new ProjectLineTemplate();
		dao.setPrtState(LovState.State.active);
		dao.setCostAccount(bean);

		return dao;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDelete}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

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
				final ProjectLineTemplate bean = ProjectLineTemplateTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrtId(), bean.getClass().getSimpleName());

				final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
				dao.remove(bean);
				ProjectLineTemplateTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					ProjectLineTemplateTabView.this.table.select(ProjectLineTemplateTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					ProjectLineTemplateTabView.this.fieldGroup.setItemDataSource(new ProjectLineTemplate());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});


	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		reloadMainTable();
//		this.table.refreshRowCache();
//		this.table.getBeanContainerDataSource().refresh();
//		this.table.sort();
	}

	private void reloadMainTable() {
		//save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		//clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new ProjectLineTemplateDAO().findAll());

		//define sort
		final Object[] properties2 = { "prtKeyNumber", "prtWorkType"};
		final boolean[] ordering2 = { true, false };
		this.table.sort(properties2, ordering2);

		//reassign filter
		this.containerFilterComponent.setFilterData(fd);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final ProjectLineTemplate bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getPrtId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		setROFields();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.form = new XdevGridLayout();
		this.comboBoxState = new XdevComboBox<>();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblPrtKeyNumber = new XdevLabel();
		this.txtPrtKeyNumber = new XdevTextField();
		this.lblPrtProject = new XdevLabel();
		this.comboBoxProject = new XdevComboBox<>();
		this.lblPrtText = new XdevLabel();
		this.txtPrtText = new XdevTextField();
		this.lblPrtHours = new XdevLabel();
		this.txtPrtHours = new XdevTextField();
		this.lblPrtRate = new XdevLabel();
		this.txtPrtRate = new XdevTextField();
		this.lblPrtWorktype = new XdevLabel();
		this.comboBoxWorkType = new XdevComboBox<>();
		this.lblPrtState = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ProjectLineTemplate.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(ProjectLineTemplate.class, DAOs.get(ProjectLineTemplateDAO.class).findAll());
		this.table.setVisibleColumns(ProjectLineTemplate_.prtKeyNumber.getName(), ProjectLineTemplate_.prtText.getName(),
				ProjectLineTemplate_.project.getName(), ProjectLineTemplate_.prtRate.getName(),
				ProjectLineTemplate_.prtState.getName());
		this.table.setColumnHeader("prtKeyNumber", "Nummer");
		this.table.setColumnHeader("prtText", "Text");
		this.table.setColumnHeader("project", "Projekt");
		this.table.setColumnHeader("prtRate", "Ansatz");
		this.table.setColumnHeader("prtState", "Status");
		this.comboBoxState.setTabIndex(7);
		this.lblCostAccount.setValue("Mitarbeiter");
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblPrtKeyNumber.setValue("Nummer");
		this.txtPrtKeyNumber.setDescription("Possible Values 1-6");
		this.txtPrtKeyNumber.setInputPrompt("Values 1-6");
		this.txtPrtKeyNumber.setTabIndex(1);
		this.txtPrtKeyNumber.setRequired(true);
		this.lblPrtProject.setValue("Projekt");
		this.comboBoxProject.setTabIndex(2);
		this.comboBoxProject.setItemCaptionFromAnnotation(false);
		this.comboBoxProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.comboBoxProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblPrtText.setValue("Text");
		this.txtPrtText.setTabIndex(3);
		this.lblPrtHours.setValue("Stunden");
		this.txtPrtHours.setTabIndex(4);
		this.lblPrtRate.setValue("Ansatz");
		this.txtPrtRate.setTabIndex(5);
		this.lblPrtWorktype.setValue("Typ");
		this.comboBoxWorkType.setTabIndex(6);
		this.lblPrtState.setValue("Status");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setTabIndex(8);
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption("Abbrechen");
		this.cmdReset.setTabIndex(9);
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.cmbCostAccount, ProjectLineTemplate_.costAccount.getName());
		this.fieldGroup.bind(this.txtPrtKeyNumber, ProjectLineTemplate_.prtKeyNumber.getName());
		this.fieldGroup.bind(this.comboBoxProject, ProjectLineTemplate_.project.getName());
		this.fieldGroup.bind(this.txtPrtText, ProjectLineTemplate_.prtText.getName());
		this.fieldGroup.bind(this.txtPrtHours, ProjectLineTemplate_.prtHours.getName());
		this.fieldGroup.bind(this.txtPrtRate, ProjectLineTemplate_.prtRate.getName());
		this.fieldGroup.bind(this.comboBoxWorkType, ProjectLineTemplate_.prtWorkType.getName());
		this.fieldGroup.bind(this.comboBoxState, ProjectLineTemplate_.prtState.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "costAccount", "prtState",
				"project", "prtWorkType", "prtRate");
		this.containerFilterComponent.setSearchableProperties("costAccount.csaCode", "costAccount.csaName", "prtText");

		this.cmdNew.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdNew);
		this.horizontalLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDelete);
		this.horizontalLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReload);
		this.horizontalLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdInfo);
		this.horizontalLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_LEFT);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdSave);
		this.horizontalLayout2.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdReset);
		this.horizontalLayout2.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(10);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 7);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 0);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 0);
		this.lblPrtKeyNumber.setSizeUndefined();
		this.form.addComponent(this.lblPrtKeyNumber, 0, 1);
		this.txtPrtKeyNumber.setSizeUndefined();
		this.form.addComponent(this.txtPrtKeyNumber, 1, 1);
		this.lblPrtProject.setSizeUndefined();
		this.form.addComponent(this.lblPrtProject, 0, 2);
		this.comboBoxProject.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.comboBoxProject, 1, 2);
		this.lblPrtText.setSizeUndefined();
		this.form.addComponent(this.lblPrtText, 0, 3);
		this.txtPrtText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrtText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrtText, 1, 3);
		this.lblPrtHours.setSizeUndefined();
		this.form.addComponent(this.lblPrtHours, 0, 4);
		this.txtPrtHours.setSizeUndefined();
		this.form.addComponent(this.txtPrtHours, 1, 4);
		this.lblPrtRate.setSizeUndefined();
		this.form.addComponent(this.lblPrtRate, 0, 5);
		this.txtPrtRate.setSizeUndefined();
		this.form.addComponent(this.txtPrtRate, 1, 5);
		this.lblPrtWorktype.setSizeUndefined();
		this.form.addComponent(this.lblPrtWorktype, 0, 6);
		this.comboBoxWorkType.setSizeUndefined();
		this.form.addComponent(this.comboBoxWorkType, 1, 6);
		this.lblPrtState.setSizeUndefined();
		this.form.addComponent(this.lblPrtState, 0, 7);
		this.horizontalLayout2.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout2, 0, 8, 1, 8);
		this.form.setComponentAlignment(this.horizontalLayout2, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 9, 1, 9);
		this.form.setRowExpandRatio(9, 1.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.form.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.form);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private XdevLabel lblCostAccount, lblPrtKeyNumber, lblPrtProject, lblPrtText, lblPrtHours, lblPrtRate, lblPrtWorktype,
			lblPrtState;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevFieldGroup<ProjectLineTemplate> fieldGroup;
	private XdevGridLayout form;
	private XdevComboBox<Project> comboBoxProject;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevTable<ProjectLineTemplate> table;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevComboBox<?> comboBoxState, comboBoxWorkType;
	private XdevTextField txtPrtKeyNumber, txtPrtText, txtPrtHours, txtPrtRate;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
