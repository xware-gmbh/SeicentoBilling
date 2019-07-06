package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat;

public class ExpensePopup extends XdevView {

	/**
	 *
	 */
	public ExpensePopup() {
		super();
		this.initUI();

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());

		// this.comboBoxAccount.addItems((Object[])LovState.Accounts.values());
		// loadDummyCb();

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId = (Long) UI.getCurrent().getSession().getAttribute("objId");
		Expense bean = null;
		Periode obj = null;

		if (beanId == null) {
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

		} else {
			final ExpenseDAO dao = new ExpenseDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);

	}

	private void setBeanGui(final Expense bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

		// set RO Fields
		setROFields();

		postLoadAccountAction(bean);
		this.txtExpText.focus();
	}

	private void postLoadAccountAction(final Expense bean) {
		if (bean.getExpAccount() == null) {
			return;
		}

		// final boolean exist = this.comboBoxAccount.containsId(lov);
		// funktioniert auf keine Weise....

		final Collection<?> col1 = this.comboBoxAccount.getItemIds();
		for (final Iterator<?> iterator = col1.iterator(); iterator.hasNext();) {
			final LovAccount lovBean = (LovAccount) iterator.next();
			if (lovBean.getId().equals(bean.getExpAccount())) {
				this.comboBoxAccount.select(lovBean);
				break;
			}
		}

	}

	private void setROFields() {
		this.dateExpBooked.setEnabled(false);
		this.cmbPeriode.setEnabled(false);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("720");
		win.setHeight("570");
		win.center();
		win.setModal(true);
		win.setContent(new ExpensePopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");

		try {
			preSaveAccountAction();
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getExpId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

			((Window) this.getParent()).close();
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}

	private void preSaveAccountAction() {
		final LovAccount lov = this.comboBoxAccount.getSelectedItem().getBean();
		if (lov != null) {
			this.fieldGroup.getItemDataSource().getBean().setExpAccount(lov.getId());
		}

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

	private void loadTemplate(final int iKey) {
		final Expense line = this.fieldGroup.getItemDataSource().getBean();

		final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
		final ExpenseTemplate tpl = dao.findByKeyNumber(line.getPeriode().getCostAccount(), iKey);

		if (tpl == null)
		 {
			return;	//not found
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


		this.fieldGroup.setItemDataSource(line);
		setROFields();

		postLoadAccountAction(line);
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

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.form = new XdevGridLayout();
		this.lblPeriode = new XdevLabel();
		this.cmbPeriode = new XdevComboBox<>();
		this.lblExpBooked = new XdevLabel();
		this.dateExpBooked = new XdevPopupDateField();
		this.lblExpDate = new XdevLabel();
		this.dateExpDate = new XdevPopupDateField();
		this.lblExpText = new XdevLabel();
		this.txtExpText = new XdevTextField();
		this.lblExpAmount = new XdevLabel();
		this.txtExpAmount = new XdevTextField();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblExpAccount = new XdevLabel();
		this.comboBoxAccount = new XdevComboBox<>();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblExpFlagGeneric = new XdevLabel();
		this.comboBoxGeneric = new XdevComboBox<>();
		this.lblExpFlagCostAccount = new XdevLabel();
		this.chkExpFlagCostAccount = new XdevCheckBox();
		this.lblExpUnit = new XdevLabel();
		this.comboBoxUnit = new XdevComboBox<>();
		this.lblExpQuantity = new XdevLabel();
		this.txtExpQuantity = new XdevTextField();
		this.lblExpState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
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
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Expense.class);

		this.lblPeriode.setValue(StringResourceUtils.optLocalizeString("{$lblPeriode.value}", this));
		this.cmbPeriode.setTabIndex(1);
		this.cmbPeriode.setContainerDataSource(Periode.class);
		this.cmbPeriode.setItemCaptionPropertyId(Periode_.perName.getName());
		this.lblExpBooked.setValue(StringResourceUtils.optLocalizeString("{$lblExpBooked.value}", this));
		this.dateExpBooked.setTabIndex(2);
		this.lblExpDate.setValue(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setTabIndex(3);
		this.dateExpDate.setRequired(true);
		this.lblExpText.setValue(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.txtExpText.setTabIndex(4);
		this.txtExpText.setMaxLength(128);
		this.lblExpAmount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.txtExpAmount.setTabIndex(5);
		this.txtExpAmount.setRequired(true);
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setTabIndex(6);
		this.cmbVat.setRequired(true);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setContainerDataSource(Vat.class);
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblExpAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setTabIndex(7);
		this.comboBoxAccount.setRequired(true);
		this.comboBoxAccount.setItemCaptionFromAnnotation(false);
		this.comboBoxAccount.setContainerDataSource(LovAccount.class, DAOs.get(LovAccountDAO.class).findAllMine());
		this.comboBoxAccount.setItemCaptionPropertyId("name");
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(8);
		this.cmbProject.setRequired(true);
		this.cmbProject.setContainerDataSource(Project.class);
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblExpFlagGeneric.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.comboBoxGeneric.setTabIndex(9);
		this.lblExpFlagCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.chkExpFlagCostAccount.setCaption("");
		this.chkExpFlagCostAccount.setTabIndex(10);
		this.lblExpUnit.setValue(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.comboBoxUnit.setTabIndex(11);
		this.lblExpQuantity.setValue(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.txtExpQuantity.setTabIndex(12);
		this.lblExpState.setValue(StringResourceUtils.optLocalizeString("{$lblExpState.value}", this));
		this.comboBoxState.setTabIndex(13);
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdAction01.setCaption("K1");
		this.cmdAction01.setDescription("CTRL + 1");
		this.cmdAction01.setStyleName("borderless tiny");
		this.cmdAction01.setClickShortcut(ShortcutAction.KeyCode.NUM1, ShortcutAction.ModifierKey.CTRL);
		this.cmdAction02.setIcon(null);
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
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(14);
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(15);
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.cmbPeriode, Expense_.periode.getName());
		this.fieldGroup.bind(this.dateExpBooked, Expense_.expBooked.getName());
		this.fieldGroup.bind(this.dateExpDate, Expense_.expDate.getName());
		this.fieldGroup.bind(this.txtExpText, Expense_.expText.getName());
		this.fieldGroup.bind(this.cmbProject, Expense_.project.getName());
		this.fieldGroup.bind(this.txtExpAmount, Expense_.expAmount.getName());
		this.fieldGroup.bind(this.cmbVat, Expense_.vat.getName());
		this.fieldGroup.bind(this.chkExpFlagCostAccount, Expense_.expFlagCostAccount.getName());
		this.fieldGroup.bind(this.comboBoxGeneric, Expense_.expFlagGeneric.getName());
		this.fieldGroup.bind(this.comboBoxUnit, Expense_.expUnit.getName());
		this.fieldGroup.bind(this.txtExpQuantity, Expense_.expQuantity.getName());
		this.fieldGroup.bind(this.comboBoxState, Expense_.expState.getName());

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
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_RIGHT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_RIGHT);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.form.setColumns(4);
		this.form.setRows(12);
		this.lblPeriode.setSizeUndefined();
		this.form.addComponent(this.lblPeriode, 0, 0);
		this.cmbPeriode.setWidth(100, Unit.PERCENTAGE);
		this.cmbPeriode.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbPeriode, 1, 0);
		this.lblExpBooked.setSizeUndefined();
		this.form.addComponent(this.lblExpBooked, 2, 0);
		this.dateExpBooked.setSizeUndefined();
		this.form.addComponent(this.dateExpBooked, 3, 0);
		this.lblExpDate.setSizeUndefined();
		this.form.addComponent(this.lblExpDate, 0, 1);
		this.dateExpDate.setSizeUndefined();
		this.form.addComponent(this.dateExpDate, 1, 1);
		this.lblExpText.setSizeUndefined();
		this.form.addComponent(this.lblExpText, 0, 2);
		this.txtExpText.setWidth(100, Unit.PERCENTAGE);
		this.txtExpText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpText, 1, 2, 3, 2);
		this.lblExpAmount.setSizeUndefined();
		this.form.addComponent(this.lblExpAmount, 0, 3);
		this.txtExpAmount.setSizeUndefined();
		this.form.addComponent(this.txtExpAmount, 1, 3);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 0, 4);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbVat, 1, 4);
		this.lblExpAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpAccount, 0, 5);
		this.comboBoxAccount.setSizeUndefined();
		this.form.addComponent(this.comboBoxAccount, 1, 5);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 2, 5);
		this.cmbProject.setWidth(100, Unit.PERCENTAGE);
		this.cmbProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbProject, 3, 5);
		this.lblExpFlagGeneric.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagGeneric, 0, 6);
		this.comboBoxGeneric.setSizeUndefined();
		this.form.addComponent(this.comboBoxGeneric, 1, 6);
		this.lblExpFlagCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagCostAccount, 2, 6);
		this.chkExpFlagCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.chkExpFlagCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.chkExpFlagCostAccount, 3, 6);
		this.lblExpUnit.setSizeUndefined();
		this.form.addComponent(this.lblExpUnit, 0, 7);
		this.comboBoxUnit.setSizeUndefined();
		this.form.addComponent(this.comboBoxUnit, 1, 7);
		this.lblExpQuantity.setSizeUndefined();
		this.form.addComponent(this.lblExpQuantity, 2, 7);
		this.txtExpQuantity.setWidth(100, Unit.PERCENTAGE);
		this.txtExpQuantity.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpQuantity, 3, 7);
		this.lblExpState.setSizeUndefined();
		this.form.addComponent(this.lblExpState, 0, 8);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 8);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout2, 1, 10, 3, 10);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout, 1, 9, 3, 9);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.TOP_RIGHT);
		this.form.setColumnExpandRatio(1, 70.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 11, 3, 11);
		this.form.setRowExpandRatio(11, 1.0F);
		this.form.setSizeFull();
		this.setContent(this.form);
		this.setSizeFull();

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
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblPeriode, lblExpBooked, lblExpDate, lblExpText, lblExpAmount, lblVat, lblExpAccount, lblProject,
			lblExpFlagGeneric, lblExpFlagCostAccount, lblExpUnit, lblExpQuantity, lblExpState;
	private XdevButton cmdAction01, cmdAction02, cmdAction03, cmdAction04, cmdAction05, cmdAction06, cmdAction, cmdAction2,
			cmdAction3, cmdAction4, cmdSave, cmdReset;
	private XdevFieldGroup<Expense> fieldGroup;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevComboBox<Periode> cmbPeriode;
	private XdevComboBox<Vat> cmbVat;
	private XdevHorizontalLayout horizontalLayout2, horizontalLayout;
	private XdevPopupDateField dateExpBooked, dateExpDate;
	private XdevComboBox<?> comboBoxGeneric, comboBoxUnit, comboBoxState;
	private XdevCheckBox chkExpFlagCostAccount;
	private XdevTextField txtExpText, txtExpAmount, txtExpQuantity;
	private XdevComboBox<LovAccount> comboBoxAccount;
	// </generated-code>

}
