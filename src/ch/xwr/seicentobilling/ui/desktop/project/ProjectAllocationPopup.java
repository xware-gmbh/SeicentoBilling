package ch.xwr.seicentobilling.ui.desktop.project;

import java.util.Date;

import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectAllocationDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectAllocation;
import ch.xwr.seicentobilling.entities.ProjectAllocation_;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectAllocationPopup extends XdevView {

	/**
	 *
	 */
	public ProjectAllocationPopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		//this.comboBoxType.addItems((Object[]) LovCrm.AddressType.values());
		//this.comboBoxSalutation.addItems((Object[]) LovCrm.Salutation.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		ProjectAllocation bean = null;
		Project obj = null;

		if (beanId == null) {

			CostAccount beanCsa = Seicento.getLoggedInCostAccount();
			if (beanCsa == null) {
				beanCsa = new CostAccountDAO().findAll().get(0);	//Dev Mode
			}
			final ProjectDAO proDao = new ProjectDAO();
			obj = proDao.find(objId);

			this.cmbCostAccount.clear();
			this.cmbCostAccount.addItems(new CostAccountDAO().findAllActive());


			bean = new ProjectAllocation();
			bean.setPraState(LovState.State.active);
			bean.setProject(obj);
			//act.setCostAccount(beanCsa);
			bean.setPraStartDate(obj.getProStartDate());
			bean.setPraEndDate(obj.getProEndDate());
			bean.setPraRate(obj.getProRate());


		} else {
			final ProjectAllocationDAO dao = new ProjectAllocationDAO();
			bean = dao.find(beanId.longValue());

			this.cmbCostAccount.clear();
			this.cmbCostAccount.addItems(new CostAccountDAO().findAllOrderByName());

		}

		setBeanGui(bean);


	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("920");
		//win.setHeight("610");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectAllocationPopup());

		return win;
	}

	private void setBeanGui(final ProjectAllocation bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// set RO Fields
		setROFields();

		//postLoadAccountAction(bean);
		//this.txtExpText.focus();
	}


	private void setROFields() {
		this.cmbProject.setEnabled(false);
//		this.cmbPeriode.setEnabled(false);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		try {
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPraId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePraEndDate}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePraEndDate_valueChange(final Property.ValueChangeEvent event) {
		validateDateFromTo();

	}

	private void validateDateFromTo() {
		final Date dateFrom = this.datePraStartDate.getValue();
		final Date dateTo = this.datePraEndDate.getValue();

		if (dateTo != null) {
			if (dateFrom != null && dateTo.before(dateFrom)) {
				this.datePraEndDate.setValue(dateFrom);
			}
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevPopupDateField}
	 * {@link #datePraStartDate}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void datePraStartDate_valueChange(final Property.ValueChangeEvent event) {
		validateDateFromTo();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblPraStartDate = new XdevLabel();
		this.datePraStartDate = new XdevPopupDateField();
		this.lblPraEndDate = new XdevLabel();
		this.datePraEndDate = new XdevPopupDateField();
		this.lblPraHours = new XdevLabel();
		this.txtPraHours = new XdevTextField();
		this.lblPraIntensityPercent = new XdevLabel();
		this.txtPraIntensityPercent = new XdevTextField();
		this.lblPraRate = new XdevLabel();
		this.txtPraRate = new XdevTextField();
		this.lblPraRemark = new XdevLabel();
		this.txtPraRemark = new XdevTextField();
		this.lblPraState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdCancel = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ProjectAllocation.class);

		this.panel.setCaption("Projektressourcen");
		this.panel.setTabIndex(0);
		this.lblProject.setValue("Projekt");
		this.cmbProject.setTabIndex(10);
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblCostAccount.setValue("Kostenstelle");
		this.cmbCostAccount.setTabIndex(9);
		this.cmbCostAccount.setRequired(true);
		this.cmbCostAccount.setItemCaptionFromAnnotation(false);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAllActive());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaName.getName());
		this.lblPraStartDate.setValue("Projektstart");
		this.datePraStartDate.setTabIndex(2);
		this.datePraStartDate.setRequired(true);
		this.lblPraEndDate.setValue("Projektende");
		this.datePraEndDate.setTabIndex(3);
		this.lblPraHours.setValue("Stundensoll");
		this.txtPraHours.setTabIndex(4);
		this.txtPraHours.setRequired(true);
		this.txtPraHours.addValidator(new IntegerRangeValidator("Der Wert muss grösser 0 sein!", 1, null));
		this.lblPraIntensityPercent.setValue("Intensität");
		this.txtPraIntensityPercent.setTabIndex(5);
		this.lblPraRate.setValue("Ansatz");
		this.txtPraRate.setTabIndex(6);
		this.lblPraRemark.setValue("Bemerkung");
		this.txtPraRemark.setTabIndex(7);
		this.lblPraState.setValue("Status");
		this.comboBoxState.setRequired(true);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption("Abbrechen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.datePraStartDate, ProjectAllocation_.praStartDate.getName());
		this.fieldGroup.bind(this.datePraEndDate, ProjectAllocation_.praEndDate.getName());
		this.fieldGroup.bind(this.txtPraHours, ProjectAllocation_.praHours.getName());
		this.fieldGroup.bind(this.txtPraIntensityPercent, ProjectAllocation_.praIntensityPercent.getName());
		this.fieldGroup.bind(this.txtPraRate, ProjectAllocation_.praRate.getName());
		this.fieldGroup.bind(this.txtPraRemark, ProjectAllocation_.praRemark.getName());
		this.fieldGroup.bind(this.comboBoxState, ProjectAllocation_.praState.getName());
		this.fieldGroup.bind(this.cmbCostAccount, ProjectAllocation_.costAccount.getName());
		this.fieldGroup.bind(this.cmbProject, ProjectAllocation_.project.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_LEFT);
		this.form.setColumns(4);
		this.form.setRows(9);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 0);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 1, 0);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 1);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 1);
		this.lblPraStartDate.setSizeUndefined();
		this.form.addComponent(this.lblPraStartDate, 0, 2);
		this.datePraStartDate.setSizeUndefined();
		this.form.addComponent(this.datePraStartDate, 1, 2);
		this.lblPraEndDate.setSizeUndefined();
		this.form.addComponent(this.lblPraEndDate, 2, 2);
		this.datePraEndDate.setWidth(100, Unit.PERCENTAGE);
		this.datePraEndDate.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.datePraEndDate, 3, 2);
		this.lblPraHours.setSizeUndefined();
		this.form.addComponent(this.lblPraHours, 0, 3);
		this.txtPraHours.setSizeUndefined();
		this.form.addComponent(this.txtPraHours, 1, 3);
		this.lblPraIntensityPercent.setSizeUndefined();
		this.form.addComponent(this.lblPraIntensityPercent, 2, 3);
		this.txtPraIntensityPercent.setWidth(100, Unit.PERCENTAGE);
		this.txtPraIntensityPercent.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPraIntensityPercent, 3, 3);
		this.lblPraRate.setSizeUndefined();
		this.form.addComponent(this.lblPraRate, 0, 4);
		this.txtPraRate.setSizeUndefined();
		this.form.addComponent(this.txtPraRate, 1, 4);
		this.lblPraRemark.setSizeUndefined();
		this.form.addComponent(this.lblPraRemark, 0, 5);
		this.txtPraRemark.setWidth(100, Unit.PERCENTAGE);
		this.txtPraRemark.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPraRemark, 1, 5);
		this.lblPraState.setSizeUndefined();
		this.form.addComponent(this.lblPraState, 0, 6);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 6);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 7, 1, 7);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.TOP_RIGHT);
		this.form.setColumnExpandRatio(1, 100.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 8, 3, 8);
		this.form.setRowExpandRatio(8, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(900, Unit.PIXELS);
		this.setHeight(520, Unit.PIXELS);

		this.datePraStartDate.addValueChangeListener(event -> this.datePraStartDate_valueChange(event));
		this.datePraEndDate.addValueChangeListener(event -> this.datePraEndDate_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblProject, lblCostAccount, lblPraStartDate, lblPraEndDate, lblPraHours, lblPraIntensityPercent,
			lblPraRate, lblPraRemark, lblPraState;
	private XdevButton cmdSave, cmdCancel;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPopupDateField datePraStartDate, datePraEndDate;
	private XdevComboBox<?> comboBoxState;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevTextField txtPraHours, txtPraIntensityPercent, txtPraRate, txtPraRemark;
	private XdevComboBox<Project> cmbProject;
	private XdevFieldGroup<ProjectAllocation> fieldGroup;
	// </generated-code>

}
