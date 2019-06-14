package ch.xwr.seicentobilling.ui.phone;

import java.util.Collection;
import java.util.Iterator;

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
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.navigation.Navigation;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.Vat_;

public class ExpenseView extends XdevView {

	@NavigationParameter
	private Expense expense;
	@NavigationParameter
	private Periode periode;
	/**
	 *
	 */
	public ExpenseView() {
		super();
		this.initUI();

		// State
		//this.comboBoxState.addItems((Object[]) LovState.State.values());
		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());

		this.panel.getContent().setSizeUndefined();

	}

	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.expense = Navigation.getParameter(event, "expense", Expense.class);
		this.periode = Navigation.getParameter(event, "periode", Periode.class);

		if (this.expense != null) {
			this.fieldGroup.setItemDataSource(this.expense);
			postLoadAccountAction(this.expense);
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
			preSaveAccountAction();
			this.fieldGroup.save();
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(this.fieldGroup.getItemDataSource().getBean().getExpId(),
					this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());
			goBack();

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


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #button}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void button_buttonClick(final Button.ClickEvent event) {
		goBack();
	}

	private void goBack() {
		Navigation.to("expenseListView").parameter("periode", this.periode).navigate();
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
				final Expense bean = ExpenseView.this.fieldGroup.getItemDataSource().getBean();
				if (bean.getExpId() > 0) {
					// Update RowObject
					final RowObjectManager man = new RowObjectManager();
					man.deleteObject(bean.getExpId(), bean.getClass().getSimpleName());
					// Delete Record
					final ExpenseDAO dao = new ExpenseDAO();
					dao.remove(bean);
				}

				goBack();
			}

		});

	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.button = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.lblExpDate = new XdevLabel();
		this.dateExpDate = new XdevPopupDateField();
		this.lblExpAmount = new XdevLabel();
		this.txtExpAmount = new XdevTextField();
		this.lblExpText = new XdevLabel();
		this.txtExpText = new XdevTextField();
		this.lblProject = new XdevLabel();
		this.cmbProject = new XdevComboBox<>();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblExpAccount = new XdevLabel();
		this.comboBoxAccount = new XdevComboBox<>();
		this.lblExpFlagCostAccount = new XdevLabel();
		this.chkExpFlagCostAccount = new XdevCheckBox();
		this.lblExpFlagGeneric = new XdevLabel();
		this.comboBoxGeneric = new XdevComboBox<>();
		this.lblExpUnit = new XdevLabel();
		this.comboBoxUnit = new XdevComboBox<>();
		this.lblExpQuantity = new XdevLabel();
		this.txtExpQuantity = new XdevTextField();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Expense.class);

		this.form.setMargin(new MarginInfo(false, true, true, true));
		this.button.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.button.setCaption(StringResourceUtils.optLocalizeString("{$button.caption}", this));
		this.button.setTabIndex(1);
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDelete.setCaption(StringResourceUtils.optLocalizeString("{$cmdDelete.caption}", this));
		this.cmdDelete.setTabIndex(2);
		this.lblExpDate.setValue(StringResourceUtils.optLocalizeString("{$lblExpDate.value}", this));
		this.dateExpDate.setTabIndex(6);
		this.lblExpAmount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAmount.value}", this));
		this.txtExpAmount.setTabIndex(7);
		this.lblExpText.setValue(StringResourceUtils.optLocalizeString("{$lblExpText.value}", this));
		this.txtExpText.setTabIndex(8);
		this.lblProject.setValue(StringResourceUtils.optLocalizeString("{$lblProject.value}", this));
		this.cmbProject.setTabIndex(9);
		this.cmbProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.cmbProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setTabIndex(10);
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAll());
		this.cmbVat.setItemCaptionPropertyId(Vat_.vatName.getName());
		this.lblExpAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpAccount.value}", this));
		this.comboBoxAccount.setTabIndex(3);
		this.comboBoxAccount.setItemCaptionFromAnnotation(false);
		this.comboBoxAccount.setContainerDataSource(LovAccount.class, DAOs.get(LovAccountDAO.class).findAllMine());
		this.comboBoxAccount.setItemCaptionPropertyId("name");
		this.lblExpFlagCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagCostAccount.value}", this));
		this.chkExpFlagCostAccount.setCaption("");
		this.chkExpFlagCostAccount.setTabIndex(11);
		this.lblExpFlagGeneric.setValue(StringResourceUtils.optLocalizeString("{$lblExpFlagGeneric.value}", this));
		this.comboBoxGeneric.setTabIndex(4);
		this.lblExpUnit.setValue(StringResourceUtils.optLocalizeString("{$lblExpUnit.value}", this));
		this.comboBoxUnit.setTabIndex(5);
		this.lblExpQuantity.setValue(StringResourceUtils.optLocalizeString("{$lblExpQuantity.value}", this));
		this.txtExpQuantity.setTabIndex(12);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(14);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(13);
		this.fieldGroup.bind(this.dateExpDate, Expense_.expDate.getName());
		this.fieldGroup.bind(this.txtExpAmount, Expense_.expAmount.getName());
		this.fieldGroup.bind(this.cmbProject, Expense_.project.getName());
		this.fieldGroup.bind(this.cmbVat, Expense_.vat.getName());
		this.fieldGroup.bind(this.chkExpFlagCostAccount, Expense_.expFlagCostAccount.getName());
		this.fieldGroup.bind(this.comboBoxGeneric, Expense_.expFlagGeneric.getName());
		this.fieldGroup.bind(this.txtExpText, Expense_.expText.getName());
		this.fieldGroup.bind(this.comboBoxUnit, Expense_.expUnit.getName());
		this.fieldGroup.bind(this.txtExpQuantity, Expense_.expQuantity.getName());

		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(13);
		this.button.setSizeUndefined();
		this.form.addComponent(this.button, 0, 0);
		this.cmdDelete.setSizeUndefined();
		this.form.addComponent(this.cmdDelete, 1, 0);
		this.form.setComponentAlignment(this.cmdDelete, Alignment.TOP_RIGHT);
		this.lblExpDate.setSizeUndefined();
		this.form.addComponent(this.lblExpDate, 0, 1);
		this.dateExpDate.setSizeUndefined();
		this.form.addComponent(this.dateExpDate, 1, 1);
		this.lblExpAmount.setSizeUndefined();
		this.form.addComponent(this.lblExpAmount, 0, 2);
		this.txtExpAmount.setSizeUndefined();
		this.form.addComponent(this.txtExpAmount, 1, 2);
		this.lblExpText.setSizeUndefined();
		this.form.addComponent(this.lblExpText, 0, 3);
		this.txtExpText.setWidth(100, Unit.PERCENTAGE);
		this.txtExpText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtExpText, 1, 3);
		this.lblProject.setSizeUndefined();
		this.form.addComponent(this.lblProject, 0, 4);
		this.cmbProject.setSizeUndefined();
		this.form.addComponent(this.cmbProject, 1, 4);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 0, 5);
		this.cmbVat.setSizeUndefined();
		this.form.addComponent(this.cmbVat, 1, 5);
		this.lblExpAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpAccount, 0, 6);
		this.comboBoxAccount.setSizeUndefined();
		this.form.addComponent(this.comboBoxAccount, 1, 6);
		this.lblExpFlagCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagCostAccount, 0, 7);
		this.chkExpFlagCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.chkExpFlagCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.chkExpFlagCostAccount, 1, 7);
		this.lblExpFlagGeneric.setSizeUndefined();
		this.form.addComponent(this.lblExpFlagGeneric, 0, 8);
		this.comboBoxGeneric.setSizeUndefined();
		this.form.addComponent(this.comboBoxGeneric, 1, 8);
		this.lblExpUnit.setSizeUndefined();
		this.form.addComponent(this.lblExpUnit, 0, 9);
		this.comboBoxUnit.setSizeUndefined();
		this.form.addComponent(this.comboBoxUnit, 1, 9);
		this.lblExpQuantity.setSizeUndefined();
		this.form.addComponent(this.lblExpQuantity, 0, 10);
		this.txtExpQuantity.setSizeUndefined();
		this.form.addComponent(this.txtExpQuantity, 1, 10);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 11, 1, 11);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 12, 1, 12);
		this.form.setRowExpandRatio(12, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.button.addClickListener(event -> this.button_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton button, cmdDelete, cmdSave, cmdReset;
	private XdevLabel lblExpDate, lblExpAmount, lblProject, lblVat, lblExpAccount, lblExpFlagCostAccount, lblExpFlagGeneric,
			lblExpText, lblExpUnit, lblExpQuantity;
	private XdevFieldGroup<Expense> fieldGroup;
	private XdevPanel panel;
	private XdevGridLayout form;
	private XdevComboBox<Project> cmbProject;
	private XdevComboBox<Vat> cmbVat;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPopupDateField dateExpDate;
	private XdevComboBox<?> comboBoxGeneric, comboBoxUnit;
	private XdevCheckBox chkExpFlagCostAccount;
	private XdevTextField txtExpAmount, txtExpText, txtExpQuantity;
	private XdevComboBox<LovAccount> comboBoxAccount;
	// </generated-code>

}
