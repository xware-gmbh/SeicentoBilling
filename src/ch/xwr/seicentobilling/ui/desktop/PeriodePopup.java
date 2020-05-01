package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;

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
import com.xdev.dal.DAOs;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;

public class PeriodePopup extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(PeriodePopup.class);

	/**
	 *
	 */
	public PeriodePopup() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		// State
		this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxBookedExp.addItems((Object[]) LovState.BookingType.values());
		this.comboBoxBookedPro.addItems((Object[]) LovState.BookingType.values());
		this.comboBoxMonth.addItems((Object[]) LovState.Month.values());

		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());

		// get Parameter
		final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
		Periode bean = null;

		if (beanId == null) {
			bean = getNewDaoWithDefaults();
		} else {
			final PeriodeDAO dao = new PeriodeDAO();
			bean = dao.find(beanId.longValue());
		}

		setBeanGui(bean);
		setROFields();

	}

	private void setROFields() {
		this.txtPerName.setEnabled(false);
		this.comboBoxBookedExp.setEnabled(false);
		this.comboBoxBookedPro.setEnabled(false);

	}

	private void setBeanGui(final Periode bean) {
		// set Bean + Fields
		this.fieldGroup.setItemDataSource(bean);

	}

	private Periode getNewDaoWithDefaults() {
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
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0);	//Dev Mode
		}
		dao.setCostAccount(bean);

		return dao;
	}


	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("720");
		//win.setHeight("480");
		win.center();
		win.setModal(true);
		win.setContent(new PeriodePopup());

		return win;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
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

		if (SeicentoCrud.doSave(this.fieldGroup)) {
			try {
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.fieldGroup.getItemDataSource().getBean().getPerId(),
						this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

				UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
				UI.getCurrent().getSession().setAttribute("beanId",  this.fieldGroup.getItemDataSource().getBean().getPerId());

				((Window) this.getParent()).close();
				Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

			} catch (final Exception e) {
				LOG.error("could not save ObjRoot", e);
			}
		}
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblPerName = new XdevLabel();
		this.txtPerName = new XdevTextField();
		this.lblPerMonth = new XdevLabel();
		this.comboBoxMonth = new XdevComboBox<>();
		this.lblPerYear = new XdevLabel();
		this.textFieldYear = new XdevTextField();
		this.cboSignOffExpense = new XdevCheckBox();
		this.lblPerBookedExpense = new XdevLabel();
		this.comboBoxBookedExp = new XdevComboBox<>();
		this.lblPerBookedProject = new XdevLabel();
		this.comboBoxBookedPro = new XdevComboBox<>();
		this.lblPerState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdCancel = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Periode.class);

		this.setIcon(null);
		this.panel.setIcon(FontAwesome.CLOCK_O);
		this.panel.setCaption("Periode bearbeiten");
		this.panel.setTabIndex(0);
		this.form.setIcon(null);
		this.lblCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setRequired(true);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblPerName.setValue(StringResourceUtils.optLocalizeString("{$lblPerName.value}", this));
		this.lblPerMonth.setValue(StringResourceUtils.optLocalizeString("{$lblPerMonth.value}", this));
		this.comboBoxMonth.setRequired(true);
		this.lblPerYear.setValue("Jahr");
		this.textFieldYear.setConverter(ConverterBuilder.stringToInteger().groupingUsed(false).minimumIntegerDigits(4)
				.maximumIntegerDigits(4).build());
		this.textFieldYear.setRequired(true);
		this.textFieldYear.setMaxLength(4);
		this.cboSignOffExpense.setCaption("Freigabe Buchhalter");
		this.cboSignOffExpense
				.setDescription("Das Feld kann durch den Buchhalter gesetzt werden für die Freigabe an die Buchhaltung");
		this.lblPerBookedExpense.setValue(StringResourceUtils.optLocalizeString("{$lblPerBookedExpense.value}", this));
		this.lblPerBookedProject.setValue(StringResourceUtils.optLocalizeString("{$lblPerBookedProject.value}", this));
		this.lblPerState.setValue(StringResourceUtils.optLocalizeString("{$lblPerState.value}", this));
		this.horizontalLayout.setMargin(new MarginInfo(true, false, false, false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption(StringResourceUtils.optLocalizeString("{$cmdCancel.caption}", this));
		this.fieldGroup.bind(this.cmbCostAccount, Periode_.costAccount.getName());
		this.fieldGroup.bind(this.txtPerName, Periode_.perName.getName());
		this.fieldGroup.bind(this.comboBoxMonth, Periode_.perMonth.getName());
		this.fieldGroup.bind(this.comboBoxBookedExp, Periode_.perBookedExpense.getName());
		this.fieldGroup.bind(this.cboSignOffExpense, Periode_.perSignOffExpense.getName());
		this.fieldGroup.bind(this.comboBoxBookedPro, Periode_.perBookedProject.getName());
		this.fieldGroup.bind(this.comboBoxState, Periode_.perState.getName());
		this.fieldGroup.bind(this.textFieldYear, Periode_.perYear.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_LEFT);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.form.setColumns(4);
		this.form.setRows(8);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 0);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 0);
		this.lblPerName.setSizeUndefined();
		this.form.addComponent(this.lblPerName, 0, 1);
		this.txtPerName.setWidth(100, Unit.PERCENTAGE);
		this.txtPerName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPerName, 1, 1, 2, 1);
		this.lblPerMonth.setSizeUndefined();
		this.form.addComponent(this.lblPerMonth, 0, 2);
		this.comboBoxMonth.setSizeUndefined();
		this.form.addComponent(this.comboBoxMonth, 1, 2);
		this.lblPerYear.setSizeUndefined();
		this.form.addComponent(this.lblPerYear, 2, 2);
		this.textFieldYear.setSizeUndefined();
		this.form.addComponent(this.textFieldYear, 3, 2);
		this.cboSignOffExpense.setSizeUndefined();
		this.form.addComponent(this.cboSignOffExpense, 1, 3);
		this.lblPerBookedExpense.setSizeUndefined();
		this.form.addComponent(this.lblPerBookedExpense, 0, 4);
		this.comboBoxBookedExp.setSizeUndefined();
		this.form.addComponent(this.comboBoxBookedExp, 1, 4);
		this.lblPerBookedProject.setSizeUndefined();
		this.form.addComponent(this.lblPerBookedProject, 2, 4);
		this.comboBoxBookedPro.setSizeUndefined();
		this.form.addComponent(this.comboBoxBookedPro, 3, 4);
		this.lblPerState.setSizeUndefined();
		this.form.addComponent(this.lblPerState, 0, 5);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 5);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout, 0, 6, 2, 6);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_LEFT);
		this.form.setColumnExpandRatio(0, 10.0F);
		this.form.setColumnExpandRatio(1, 100.0F);
		this.form.setColumnExpandRatio(3, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 7, 3, 7);
		this.form.setRowExpandRatio(7, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(760, Unit.PIXELS);
		this.setHeight(490, Unit.PIXELS);

		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblCostAccount, lblPerName, lblPerMonth, lblPerYear, lblPerBookedExpense, lblPerBookedProject,
			lblPerState;
	private XdevButton cmdSave, cmdCancel;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevHorizontalLayout horizontalLayout;
	private XdevFieldGroup<Periode> fieldGroup;
	private XdevComboBox<?> comboBoxMonth, comboBoxBookedExp, comboBoxBookedPro, comboBoxState;
	private XdevPanel panel;
	private XdevCheckBox cboSignOffExpense;
	private XdevGridLayout form;
	private XdevTextField txtPerName, textFieldYear;
	// </generated-code>

}
