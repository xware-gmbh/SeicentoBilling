package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;
import ch.xwr.seicentobilling.entities.ProjectLine_;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.ui.desktop.project.ProjectLookupPopup;

public class ProjectLinePopup extends XdevView {

	/**
	 *
	 */
	public ProjectLinePopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxWorktype.addItems((Object[]) LovState.WorkType.values());
		this.cmbProject.addItem(new ProjectDAO().findAllActive());

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId"); // projectline
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId"); // Periode
		ProjectLine bean = null;
		Periode obj = null;

		if (beanId == null) {
			// new
			final PeriodeDAO objDao = new PeriodeDAO();
			obj = objDao.find(objId);

			bean = new ProjectLine();
			bean.setPrlState(LovState.State.active);
			bean.setPrlWorkType(LovState.WorkType.project);
			bean.setPrlReportDate(new Date());
			bean.setPeriode(obj);

		} else {
			final ProjectLineDAO dao = new ProjectLineDAO();
			bean = dao.find(beanId.longValue());

			prepareProjectCombo(bean.getProject());
		}

		setBeanGui(bean);

	}

	private void setBeanGui(final ProjectLine bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		setROFields();

		// focus
		this.datePrlReportDate.focus();
	}

	private void setROFields() {
		// Readonly
		this.cmbPeriode.setEnabled(false);

	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("670");
		win.setHeight("440");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectLinePopup());

		return win;
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
		if (!areFieldsValid()) {
			return;
		}

		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
		try {
			this.fieldGroup.save();

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPrlId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private boolean areFieldsValid() {
		if (this.fieldGroup.isValid()) {
			return true;
		}
		AbstractField<T> fld = null;
		try {
			final Collection<?> flds = this.fieldGroup.getFields();
			for (final Iterator<?> iterator = flds.iterator(); iterator.hasNext();) {
				fld = (AbstractField<T>) iterator.next();
				if (!fld.isValid()) {
					fld.focus();
					fld.validate();
				}
			}

		} catch (final Exception e) {
			final Object prop = this.fieldGroup.getPropertyId(fld);
			Notification.show("Feld ist ungültig", prop.toString(), Notification.Type.ERROR_MESSAGE);
		}

		return false;
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #cmbProject}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmbProject_valueChange(final Property.ValueChangeEvent event) {
		if (this.fieldGroup.getItemDataSource() != null) {
			if (this.fieldGroup.getItemDataSource().getBean().getPrlId() == null) {
				if (this.cmbProject.isModified()) {
					// final Project obj = (Project) event.getProperty().getValue();
					final Project obj = this.cmbProject.getSelectedItem().getBean();
					this.txtPrlRate.setValue("SFr. " + obj.getProRate());
				}
			}
		}

	}

	private void loadTemplate(final int iKey) {
		final ProjectLine line = this.fieldGroup.getItemDataSource().getBean();

		final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
		final ProjectLineTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null) {
			return; // not found
		}

		line.setPrlHours(tpl.getPrtHours());
		line.setPrlRate(tpl.getPrtRate());
		line.setPrlText(tpl.getprtText());
		line.setPrlWorkType(tpl.getprtWorkType());
		line.setProject(tpl.getProject());
		line.setPrlState(tpl.getPrtState());

		this.fieldGroup.setItemDataSource(line);
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction02}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction02_buttonClick(final Button.ClickEvent event) {
		loadTemplate(2);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction01}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction01_buttonClick(final Button.ClickEvent event) {
		loadTemplate(1);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction03}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction03_buttonClick(final Button.ClickEvent event) {
		loadTemplate(3);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction04}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction04_buttonClick(final Button.ClickEvent event) {
		loadTemplate(4);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction05}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction05_buttonClick(final Button.ClickEvent event) {
		loadTemplate(5);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdAction06}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction06_buttonClick(final Button.ClickEvent event) {
		loadTemplate(6);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdAction}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction_buttonClick(final Button.ClickEvent event) {
		loadTemplate(7);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdAction2}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction2_buttonClick(final Button.ClickEvent event) {
		loadTemplate(8);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdAction3}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction3_buttonClick(final Button.ClickEvent event) {
		loadTemplate(9);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdAction4}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAction4_buttonClick(final Button.ClickEvent event) {
		loadTemplate(0);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #btnSearch}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSearch_buttonClick(final Button.ClickEvent event) {
		popupProjectLookup();
	}

	private void popupProjectLookup() {
		final Window win = ProjectLookupPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");

				if (beanId != null && beanId > 0) {
					final Project bean = new ProjectDAO().find(beanId);
					prepareProjectCombo(bean);
				}
			}
		});
		this.getUI().addWindow(win);

	}

	private void prepareProjectCombo(final Project bean) {
		ProjectLinePopup.this.cmbProject.addItem(bean);
		ProjectLinePopup.this.cmbProject.setValue(bean);
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.form = new XdevGridLayout();
		this.lblPeriode = new XdevLabel();
		this.cmbPeriode = new XdevComboBox<>();
		this.lblPrlReportDate = new XdevLabel();
		this.datePrlReportDate = new XdevPopupDateField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.btnSearch = new XdevButton();
		this.lblPrlHours = new XdevLabel();
		this.txtPrlHours = new XdevTextField();
		this.lblPrlRate = new XdevLabel();
		this.txtPrlRate = new XdevTextField();
		this.lblPrlText = new XdevLabel();
		this.txtPrlText = new XdevTextField();
		this.lblPrlWorkType = new XdevLabel();
		this.comboBoxWorktype = new XdevComboBox<>();
		this.lblPrlState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdCancel = new XdevButton();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdAction01 = new XdevButton();
		this.cmdAction02 = new XdevButton();
		this.cmdAction03 = new XdevButton();
		this.cmdAction04 = new XdevButton();
		this.cmdAction05 = new XdevButton();
		this.cmdAction06 = new XdevButton();
		this.cmdAction = new XdevButton();
		this.cmdAction2 = new XdevButton();
		this.cmdAction3 = new XdevButton();
		this.cmdAction4 = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ProjectLine.class);

		this.lblPeriode.setValue(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setTabIndex(51);
		this.cmbPeriode.setContainerDataSource(Periode.class);
		this.cmbPeriode.setItemCaptionPropertyId(Periode_.perName.getName());
		this.lblPrlReportDate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlReportDate.value}", this));
		this.datePrlReportDate.setTabIndex(52);
		this.datePrlReportDate.setRequired(true);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(53);
		this.cmbProject.setRequired(true);
		this.cmbProject.setAutoQueryData(false);
		this.cmbProject.setImmediate(false);
		this.cmbProject.setContainerDataSource(Project.class, false);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.btnSearch.setIcon(FontAwesome.SEARCH);
		this.btnSearch.setCaption("");
		this.btnSearch.setDescription("Suchen...");
		this.btnSearch.setTabIndex(54);
		this.lblPrlHours.setValue(StringResourceUtils.optLocalizeString("{$lblPrlHours.value}", this));
		this.txtPrlHours.setConverter(ConverterBuilder.stringToDouble().build());
		this.txtPrlHours.setTabIndex(55);
		this.txtPrlHours.setRequired(true);
		this.lblPrlRate.setValue(StringResourceUtils.optLocalizeString("{$lblPrlRate.value}", this));
		this.txtPrlRate.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtPrlRate.setTabIndex(56);
		this.txtPrlRate.setRequired(true);
		this.lblPrlText.setValue(StringResourceUtils.optLocalizeString("{$lblPrlText.value}", this));
		this.txtPrlText.setTabIndex(57);
		this.txtPrlText.setMaxLength(384);
		this.lblPrlWorkType.setValue(StringResourceUtils.optLocalizeString("{$lblPrlWorkType.value}", this));
		this.comboBoxWorktype.setTabIndex(57);
		this.lblPrlState.setValue(StringResourceUtils.optLocalizeString("{$lblPrlState.value}", this));
		this.comboBoxState.setTabIndex(58);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdCancel.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdCancel.setCaption(StringResourceUtils.optLocalizeString("{$cmdCancel.caption}", this));
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdAction01.setCaption("K1");
		this.cmdAction01.setDescription("CTRL + 1");
		this.cmdAction01.setStyleName("borderless tiny");
		this.cmdAction01.setClickShortcut(ShortcutAction.KeyCode.NUM1, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction02.setCaption("K2");
		this.cmdAction02.setDescription("CTRL + 2");
		this.cmdAction02.setStyleName("borderless tiny");
		this.cmdAction02.setClickShortcut(ShortcutAction.KeyCode.NUM2, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction03.setCaption("K3");
		this.cmdAction03.setDescription("CTRL + 3");
		this.cmdAction03.setStyleName("borderless tiny");
		this.cmdAction03.setClickShortcut(ShortcutAction.KeyCode.NUM3, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction04.setCaption("K4");
		this.cmdAction04.setDescription("CTRL + 4");
		this.cmdAction04.setStyleName("borderless tiny");
		this.cmdAction04.setClickShortcut(ShortcutAction.KeyCode.NUM4, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction05.setCaption("K5");
		this.cmdAction05.setDescription("CTRL + 5");
		this.cmdAction05.setStyleName("borderless tiny");
		this.cmdAction05.setClickShortcut(ShortcutAction.KeyCode.NUM5, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction06.setCaption("K6");
		this.cmdAction06.setDescription("CTRL + 6");
		this.cmdAction06.setStyleName("borderless tiny");
		this.cmdAction06.setClickShortcut(ShortcutAction.KeyCode.NUM6, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction.setCaption("K7");
		this.cmdAction.setDescription("CTRL + 7");
		this.cmdAction.setStyleName("borderless tiny");
		this.cmdAction.setClickShortcut(ShortcutAction.KeyCode.NUM7, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction2.setCaption("K8");
		this.cmdAction2.setDescription("CTRL + 8");
		this.cmdAction2.setStyleName("borderless tiny");
		this.cmdAction2.setClickShortcut(ShortcutAction.KeyCode.NUM8, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction3.setCaption("K9");
		this.cmdAction3.setDescription("CTRL + 9");
		this.cmdAction3.setStyleName("borderless tiny");
		this.cmdAction3.setClickShortcut(ShortcutAction.KeyCode.NUM9, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction4.setCaption("K10");
		this.cmdAction4.setDescription("CTRL + 0");
		this.cmdAction4.setStyleName("borderless tiny");
		this.cmdAction4.setClickShortcut(ShortcutAction.KeyCode.NUM0, ShortcutAction.ModifierKey.CTRL);
		this.fieldGroup.bind(this.cmbPeriode, ProjectLine_.periode.getName());
		this.fieldGroup.bind(this.datePrlReportDate, ProjectLine_.prlReportDate.getName());
		this.fieldGroup.bind(this.txtPrlHours, ProjectLine_.prlHours.getName());
		this.fieldGroup.bind(this.txtPrlText, ProjectLine_.prlText.getName());
		this.fieldGroup.bind(this.txtPrlRate, ProjectLine_.prlRate.getName());
		this.fieldGroup.bind(this.cmbProject, ProjectLine_.project.getName());
		this.fieldGroup.bind(this.comboBoxWorktype, ProjectLine_.prlWorkType.getName());
		this.fieldGroup.bind(this.comboBoxState, ProjectLine_.prlState.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_LEFT);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.cmdAction01.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction01);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction01, Alignment.MIDDLE_RIGHT);
		this.cmdAction02.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction02);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction02, Alignment.MIDDLE_RIGHT);
		this.cmdAction03.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction03);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction03, Alignment.MIDDLE_RIGHT);
		this.cmdAction04.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction04);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction04, Alignment.MIDDLE_RIGHT);
		this.cmdAction05.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction05);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction05, Alignment.MIDDLE_RIGHT);
		this.cmdAction06.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction06);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction06, Alignment.MIDDLE_RIGHT);
		this.cmdAction.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction, Alignment.MIDDLE_RIGHT);
		this.cmdAction2.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction2);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction2, Alignment.MIDDLE_RIGHT);
		this.cmdAction3.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction3);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction3, Alignment.MIDDLE_RIGHT);
		this.cmdAction4.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdAction4);
		this.horizontalLayout2.setComponentAlignment(this.cmdAction4, Alignment.MIDDLE_RIGHT);
		final CustomComponent horizontalLayout2_spacer = new CustomComponent();
		horizontalLayout2_spacer.setSizeFull();
		this.horizontalLayout2.addComponent(horizontalLayout2_spacer);
		this.horizontalLayout2.setExpandRatio(horizontalLayout2_spacer, 1.0F);
		this.form.setColumns(4);
		this.form.setRows(9);
		this.lblPeriode.setSizeUndefined();
		this.form.addComponent(this.lblPeriode, 0, 0);
		this.cmbPeriode.setWidth(100, Unit.PERCENTAGE);
		this.cmbPeriode.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbPeriode, 1, 0);
		this.lblPrlReportDate.setSizeUndefined();
		this.form.addComponent(this.lblPrlReportDate, 0, 1);
		this.datePrlReportDate.setSizeUndefined();
		this.form.addComponent(this.datePrlReportDate, 1, 1);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 2);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 1, 2, 2, 2);
		this.btnSearch.setSizeUndefined();
		this.form.addComponent(this.btnSearch, 3, 2);
		this.lblPrlHours.setSizeUndefined();
		this.form.addComponent(this.lblPrlHours, 0, 3);
		this.txtPrlHours.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlHours.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlHours, 1, 3);
		this.lblPrlRate.setSizeUndefined();
		this.form.addComponent(this.lblPrlRate, 2, 3);
		this.txtPrlRate.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlRate.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlRate, 3, 3);
		this.lblPrlText.setSizeUndefined();
		this.form.addComponent(this.lblPrlText, 0, 4);
		this.txtPrlText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrlText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrlText, 1, 4, 2, 4);
		this.lblPrlWorkType.setSizeUndefined();
		this.form.addComponent(this.lblPrlWorkType, 0, 5);
		this.comboBoxWorktype.setSizeUndefined();
		this.form.addComponent(this.comboBoxWorktype, 1, 5);
		this.lblPrlState.setSizeUndefined();
		this.form.addComponent(this.lblPrlState, 2, 5);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 3, 5);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout, 0, 6, 2, 6);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout2, 0, 7, 1, 7);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 8, 3, 8);
		this.form.setRowExpandRatio(8, 1.0F);
		this.form.setSizeFull();
		this.setContent(this.form);
		this.setSizeFull();

		this.cmbProject.addValueChangeListener(event -> this.cmbProject_valueChange(event));
		this.btnSearch.addClickListener(event -> this.btnSearch_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
		this.cmdAction01.addClickListener(event -> this.cmdAction01_buttonClick(event));
		this.cmdAction02.addClickListener(event -> this.cmdAction02_buttonClick(event));
		this.cmdAction03.addClickListener(event -> this.cmdAction03_buttonClick(event));
		this.cmdAction04.addClickListener(event -> this.cmdAction04_buttonClick(event));
		this.cmdAction05.addClickListener(event -> this.cmdAction05_buttonClick(event));
		this.cmdAction06.addClickListener(event -> this.cmdAction06_buttonClick(event));
		this.cmdAction.addClickListener(event -> this.cmdAction_buttonClick(event));
		this.cmdAction2.addClickListener(event -> this.cmdAction2_buttonClick(event));
		this.cmdAction3.addClickListener(event -> this.cmdAction3_buttonClick(event));
		this.cmdAction4.addClickListener(event -> this.cmdAction4_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblPeriode, lblPrlReportDate, lblProject, lblPrlHours, lblPrlRate, lblPrlText, lblPrlWorkType,
			lblPrlState;
	private XdevButton btnSearch, cmdSave, cmdCancel, cmdAction01, cmdAction02, cmdAction03, cmdAction04, cmdAction05,
			cmdAction06, cmdAction, cmdAction2, cmdAction3, cmdAction4;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevPopupDateField datePrlReportDate;
	private XdevComboBox<?> comboBoxWorktype, comboBoxState;
	private XdevGridLayout form;
	private XdevTextField txtPrlHours, txtPrlRate, txtPrlText;
	private XdevComboBox<Project> cmbProject;
	private XdevComboBox<Periode> cmbPeriode;
	private XdevFieldGroup<ProjectLine> fieldGroup;
	// </generated-code>

}
