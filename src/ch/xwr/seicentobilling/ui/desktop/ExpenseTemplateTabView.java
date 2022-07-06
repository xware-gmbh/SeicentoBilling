package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
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
import com.xdev.dal.DAOs;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
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
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.ExpenseTemplate_;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat;

public class ExpenseTemplateTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExpenseTemplateTabView.class);

	/**
	 *
	 */
	public ExpenseTemplateTabView() {
		super();
		this.initUI();

		//Type
		this.comboBoxUnit.addItems((Object[]) LovState.ExpUnit.values());
		this.comboBoxGeneric.addItems((Object[]) LovState.ExpType.values());
		this.comboBoxState.addItems((Object[])LovState.State.values());

		this.comboBoxAccount.addItems((Object[]) LovState.WorkType.values());

		//sort Table
		final Object[] properties2 = { "extKeyNumber", "extId"};
		final boolean[] ordering2 = { true, false };
		this.table.sort(properties2, ordering2);

		setDefaultFilter();

		//New Action + set RO Fields
		//this.fieldGroup.setItemDataSource(getNewDaoWithDefaults());
		setROFields();
		//postLoadAccountAction(this.fieldGroup.getItemDataSource().getBean());
	}

	private void postLoadAccountAction(final ExpenseTemplate bean) {
		if (bean.getExtAccount() == null) {
			return;
		}

		// final boolean exist = this.comboBoxAccount.containsId(lov);
		// funktioniert auf keine Weise....

		final Collection<?> col1 = this.comboBoxAccount.getItemIds();
		for (final Iterator<?> iterator = col1.iterator(); iterator.hasNext();) {
			final LovAccount lovBean = (LovAccount) iterator.next();
			if (lovBean.getId().equals(bean.getExtAccount())) {
				this.comboBoxAccount.select(lovBean);
				break;
			}
		}

	}

	private void setROFields() {
		this.cmbCostAccount.setEnabled(false);

		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null || this.fieldGroup.getItemDataSource().getBean() == null ) {

			hasData = false;
		}
		setROComponents(hasData);
	}

	private void setROComponents(final boolean state) {
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		this.form.setEnabled(state);
	}

	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}

		final LovState.State[] valState = new LovState.State[]{LovState.State.active};
		final CostAccount[] val2 = new CostAccount[]{bean};
		final FilterData[] fd = new FilterData[]{new FilterData("costAccount", new FilterOperator.Is(), val2),
				new FilterData("extState", new FilterOperator.Is(), valState)};

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
		if (!AreFieldsValid()) {
			return;
		}

		preSaveAccountAction();
		this.fieldGroup.save();

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getExtId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		LOG.debug("Record saved ExpenseTemplate");

		reloadMainTable();
		setROFields();
	}

	@SuppressWarnings("unchecked")
	private boolean AreFieldsValid() {
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

	private void preSaveAccountAction() {
		final LovAccount lov = this.comboBoxAccount.getSelectedItem().getBean();
		if (lov != null) {
			this.fieldGroup.getItemDataSource().getBean().setExtAccount(lov.getId());
		}

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
		postLoadAccountAction(this.fieldGroup.getItemDataSource().getBean());
	}

	private ExpenseTemplate getNewDaoWithDefaults() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0);	//Dev Mode
		}

		final ExpenseTemplate dao = new ExpenseTemplate();
		dao.setExtState(LovState.State.active);
		dao.setCostAccount(bean);
		dao.setExtKeyNumber(10);

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
				final ExpenseTemplate bean = ExpenseTemplateTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getExtId(), bean.getClass().getSimpleName());

				final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
				dao.remove(bean);
				ExpenseTemplateTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					ExpenseTemplateTabView.this.table.select(ExpenseTemplateTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					ExpenseTemplateTabView.this.fieldGroup.setItemDataSource(new ExpenseTemplate());
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
		this.table.getBeanContainerDataSource().addAll(new ExpenseTemplateDAO().findAll());

		//define sort
		final Object[] properties2 = { "extKeyNumber", "extId"};
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
		final ExpenseTemplate bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getExtId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		postLoadAccountAction(this.fieldGroup.getItemDataSource().getBean());
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
		this.checkBox = new XdevCheckBox();
		this.comboBoxState = new XdevComboBox<>();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblPrtKeyNumber = new XdevLabel();
		this.txtPrtKeyNumber = new XdevTextField();
		this.lblPrtText = new XdevLabel();
		this.txtPrtText = new XdevTextField();
		this.lblExtAmount = new XdevLabel();
		this.txtExtAmount = new XdevTextField();
		this.lblExtVat = new XdevLabel();
		this.comboBoxVat = new XdevComboBox<>();
		this.lblExtAccount = new XdevLabel();
		this.comboBoxAccount = new XdevComboBox<>();
		this.lblExtGeneral = new XdevLabel();
		this.comboBoxGeneric = new XdevComboBox<>();
		this.lblExtCostAccount = new XdevLabel();
		this.lblExtUnit = new XdevLabel();
		this.comboBoxUnit = new XdevComboBox<>();
		this.lblPrtProject = new XdevLabel();
		this.comboBoxProject = new XdevComboBox<>();
		this.lblExtQuantity = new XdevLabel();
		this.txtExtQuantity = new XdevTextField();
		this.lblPrtState = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(ExpenseTemplate.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription("Neuer Datensatz");
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setSortAscending(false);
		this.table.setContainerDataSource(ExpenseTemplate.class, DAOs.get(ExpenseTemplateDAO.class).findAll());
		this.table.setVisibleColumns(ExpenseTemplate_.extKeyNumber.getName(), ExpenseTemplate_.extAccount.getName(),
				ExpenseTemplate_.extText.getName(), ExpenseTemplate_.extState.getName());
		this.table.setColumnHeader("extKeyNumber", "Nummer");
		this.table.setColumnHeader("extAccount", "Konto");
		this.table.setColumnHeader("extText", "Text");
		this.table.setColumnHeader("extState", "Status");
		this.checkBox.setCaption("");
		this.checkBox.setTabIndex(7);
		this.comboBoxState.setTabIndex(11);
		this.lblCostAccount.setValue("Mitarbeiter");
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblPrtKeyNumber.setValue("Nummer");
		this.txtPrtKeyNumber.setDescription("Shortcutnumber Ctrl-+<Number>");
		this.txtPrtKeyNumber.setInputPrompt("Values 1-10");
		this.txtPrtKeyNumber.setTabIndex(1);
		this.txtPrtKeyNumber.setRequired(true);
		this.txtPrtKeyNumber.addValidator(new IntegerRangeValidator("Wert muss zwischen 1-10 sein.", 1, 10));
		this.lblPrtText.setValue("Text");
		this.txtPrtText.setTabIndex(2);
		this.lblExtAmount.setValue("Betrag");
		this.txtExtAmount.setTabIndex(3);
		this.txtExtAmount.setRequired(true);
		this.lblExtVat.setValue("MwSt");
		this.comboBoxVat.setTabIndex(4);
		this.comboBoxVat.setItemCaptionFromAnnotation(false);
		this.comboBoxVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAllInclusive());
		this.comboBoxVat.setItemCaptionPropertyId("fullName");
		this.lblExtAccount.setValue("Konto");
		this.comboBoxAccount.setTabIndex(5);
		this.comboBoxAccount.setRequired(true);
		this.comboBoxAccount.setItemCaptionFromAnnotation(false);
		this.comboBoxAccount.setContainerDataSource(LovAccount.class, DAOs.get(LovAccountDAO.class).findAllMine());
		this.comboBoxAccount.setItemCaptionPropertyId("name");
		this.lblExtGeneral.setValue("Pauschal");
		this.comboBoxGeneric.setTabIndex(6);
		this.lblExtCostAccount.setValue("Kostenstelle");
		this.lblExtUnit.setValue("Einheit");
		this.comboBoxUnit.setTabIndex(8);
		this.lblPrtProject.setValue("Projekt");
		this.comboBoxProject.setTabIndex(9);
		this.comboBoxProject.setRequired(true);
		this.comboBoxProject.setItemCaptionFromAnnotation(false);
		this.comboBoxProject.setContainerDataSource(Project.class, DAOs.get(ProjectDAO.class).findAll());
		this.comboBoxProject.setItemCaptionPropertyId(Project_.proName.getName());
		this.lblExtQuantity.setValue("Menge");
		this.txtExtQuantity.setTabIndex(10);
		this.lblPrtState.setValue("Status");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setTabIndex(12);
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption("Abbrechen");
		this.cmdReset.setTabIndex(13);
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.fieldGroup.bind(this.cmbCostAccount, ExpenseTemplate_.costAccount.getName());
		this.fieldGroup.bind(this.txtPrtKeyNumber, ExpenseTemplate_.extKeyNumber.getName());
		this.fieldGroup.bind(this.comboBoxProject, ExpenseTemplate_.project.getName());
		this.fieldGroup.bind(this.txtPrtText, ExpenseTemplate_.extText.getName());
		this.fieldGroup.bind(this.txtExtAmount, ExpenseTemplate_.extAmount.getName());
		this.fieldGroup.bind(this.comboBoxVat, ExpenseTemplate_.vat.getName());
		this.fieldGroup.bind(this.comboBoxGeneric, ExpenseTemplate_.extFlagGeneric.getName());
		this.fieldGroup.bind(this.checkBox, ExpenseTemplate_.extFlagCostAccount.getName());
		this.fieldGroup.bind(this.comboBoxUnit, ExpenseTemplate_.extUnit.getName());
		this.fieldGroup.bind(this.txtExtQuantity, ExpenseTemplate_.extQuantity.getName());
		this.fieldGroup.bind(this.comboBoxState, ExpenseTemplate_.extState.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "costAccount", "project",
				"extState");
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
		this.form.setRows(14);
		this.checkBox.setSizeUndefined();
		this.form.addComponent(this.checkBox, 1, 7);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 11);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 0);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 0);
		this.lblPrtKeyNumber.setSizeUndefined();
		this.form.addComponent(this.lblPrtKeyNumber, 0, 1);
		this.txtPrtKeyNumber.setSizeUndefined();
		this.form.addComponent(this.txtPrtKeyNumber, 1, 1);
		this.lblPrtText.setSizeUndefined();
		this.form.addComponent(this.lblPrtText, 0, 2);
		this.txtPrtText.setWidth(100, Unit.PERCENTAGE);
		this.txtPrtText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtPrtText, 1, 2);
		this.lblExtAmount.setSizeUndefined();
		this.form.addComponent(this.lblExtAmount, 0, 3);
		this.txtExtAmount.setSizeUndefined();
		this.form.addComponent(this.txtExtAmount, 1, 3);
		this.lblExtVat.setSizeUndefined();
		this.form.addComponent(this.lblExtVat, 0, 4);
		this.comboBoxVat.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxVat.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.comboBoxVat, 1, 4);
		this.lblExtAccount.setSizeUndefined();
		this.form.addComponent(this.lblExtAccount, 0, 5);
		this.comboBoxAccount.setSizeUndefined();
		this.form.addComponent(this.comboBoxAccount, 1, 5);
		this.lblExtGeneral.setSizeUndefined();
		this.form.addComponent(this.lblExtGeneral, 0, 6);
		this.comboBoxGeneric.setSizeUndefined();
		this.form.addComponent(this.comboBoxGeneric, 1, 6);
		this.lblExtCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblExtCostAccount, 0, 7);
		this.lblExtUnit.setSizeUndefined();
		this.form.addComponent(this.lblExtUnit, 0, 8);
		this.comboBoxUnit.setSizeUndefined();
		this.form.addComponent(this.comboBoxUnit, 1, 8);
		this.lblPrtProject.setSizeUndefined();
		this.form.addComponent(this.lblPrtProject, 0, 9);
		this.comboBoxProject.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxProject.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.comboBoxProject, 1, 9);
		this.lblExtQuantity.setSizeUndefined();
		this.form.addComponent(this.lblExtQuantity, 0, 10);
		this.txtExtQuantity.setSizeUndefined();
		this.form.addComponent(this.txtExtQuantity, 1, 10);
		this.lblPrtState.setSizeUndefined();
		this.form.addComponent(this.lblPrtState, 0, 11);
		this.horizontalLayout2.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout2, 0, 12, 1, 12);
		this.form.setComponentAlignment(this.horizontalLayout2, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 13, 1, 13);
		this.form.setRowExpandRatio(13, 1.0F);
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
	private XdevLabel lblCostAccount, lblPrtKeyNumber, lblPrtText, lblExtAmount, lblExtVat, lblExtAccount, lblExtGeneral,
			lblExtCostAccount, lblExtUnit, lblPrtProject, lblExtQuantity, lblPrtState;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevFieldGroup<ExpenseTemplate> fieldGroup;
	private XdevTable<ExpenseTemplate> table;
	private XdevGridLayout form;
	private XdevComboBox<Project> comboBoxProject;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevComboBox<Vat> comboBoxVat;
	private XdevComboBox<?> comboBoxState, comboBoxGeneric, comboBoxUnit;
	private XdevCheckBox checkBox;
	private XdevTextField txtPrtKeyNumber, txtPrtText, txtExtAmount, txtExtQuantity;
	private XdevVerticalLayout verticalLayout;
	private XdevComboBox<LovAccount> comboBoxAccount;
	// </generated-code>

}
