package ch.xwr.seicentobilling.ui.desktop;

import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.ss.formula.functions.T;

import com.vaadin.data.Property;
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
import com.xdev.res.StringResourceUtils;
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
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;

public class CostAccountTabView extends XdevView {

	/**
	 *
	 */
	public CostAccountTabView() {
		super();
		this.initUI();

		//Type
		this.comboBoxState.addItems((Object[])LovState.State.values());


		setROFields();
		setDefaultFilter();

	}

	private void setROFields() {

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

		if (Seicento.hasRole("BillingAdmin") && state) {
			this.txtCsaExtRef.setEnabled(true);
		} else {
			this.txtCsaExtRef.setEnabled(false);
		}

	}


	private void setDefaultFilter() {
		final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final FilterData[] fd = new FilterData[] { new FilterData("csaState", new FilterOperator.Is(), valState) };

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


		this.fieldGroup.save();

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getCsaId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

		cmdReload_buttonClick(event);
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

	private CostAccount getNewDaoWithDefaults() {
		final CostAccount dao = new CostAccount();
		dao.setCsaState(LovState.State.active);
		dao.setCsaFlagCompany(false);
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
				final CostAccount bean = CostAccountTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCsaId(), bean.getClass().getSimpleName());

				final CostAccountDAO dao = new CostAccountDAO();
				dao.remove(bean);
				CostAccountTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					CostAccountTabView.this.table.select(CostAccountTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					CostAccountTabView.this.fieldGroup.setItemDataSource(new CostAccount());
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

		// save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);
		final int idx = this.table.getCurrentPageFirstItemIndex();

		// clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new CostAccountDAO().findAll());

		// reassign filter
		this.containerFilterComponent.setFilterData(fd);

		if (this.fieldGroup.getItemDataSource() != null) {
			final CostAccount bean = this.fieldGroup.getItemDataSource().getBean();
			if (bean != null) {
				this.table.select(bean);

				if (idx > 0) {
					this.table.setCurrentPageFirstItemIndex(idx);
				}
				//this.table.setCurrentPageFirstItemId(bean);
			}
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final CostAccount bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCsaId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		if (this.table.getSelectedItem() != null) {
			setROFields();
		}

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
		this.checkBoxFlagCompany = new XdevCheckBox();
		this.comboBoxState = new XdevComboBox<>();
		this.lblCsaCode = new XdevLabel();
		this.txtCsaCode = new XdevTextField();
		this.lblCsaName = new XdevLabel();
		this.txtCsaName = new XdevTextField();
		this.lblCostAccount = new XdevLabel();
		this.cmbCostAccount = new XdevComboBox<>();
		this.lblCsaExtRef = new XdevLabel();
		this.txtCsaExtRef = new XdevTextField();
		this.lblCsaCompanyFlag = new XdevLabel();
		this.lblCsaState = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(CostAccount.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.containerFilterComponent.setPrefixMatchOnly(false);
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAll());
		this.table.setVisibleColumns(CostAccount_.csaCode.getName(), CostAccount_.csaName.getName(),
				CostAccount_.csaFlagCompany.getName(), CostAccount_.csaState.getName(), CostAccount_.costAccount.getName());
		this.table.setColumnHeader("csaCode", "Code");
		this.table.setColumnHeader("csaName", "Name");
		this.table.setColumnHeader("csaFlagCompany", "Firma");
		this.table.setColumnHeader("csaState", "Status");
		this.table.setColumnHeader("costAccount", "Übergeordnet");
		this.table.setColumnCollapsed("costAccount", true);
		this.checkBoxFlagCompany.setCaption("");
		this.checkBoxFlagCompany.setTabIndex(5);
		this.comboBoxState.setTabIndex(6);
		this.lblCsaCode.setValue(StringResourceUtils.optLocalizeString("{$lblCsaCode.value}", this));
		this.txtCsaCode.setInputPrompt("");
		this.txtCsaCode.setTabIndex(1);
		this.txtCsaCode.setRequired(true);
		this.txtCsaCode.setMaxLength(5);
		this.lblCsaName.setValue(StringResourceUtils.optLocalizeString("{$lblCsaName.value}", this));
		this.txtCsaName.setTabIndex(2);
		this.txtCsaName.setRequired(true);
		this.txtCsaName.setMaxLength(50);
		this.lblCostAccount.setValue(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setTabIndex(3);
		this.cmbCostAccount.setContainerDataSource(CostAccount.class, DAOs.get(CostAccountDAO.class).findAllActive());
		this.cmbCostAccount.setItemCaptionPropertyId(CostAccount_.csaCode.getName());
		this.lblCsaExtRef.setValue("Externe Referenz");
		this.txtCsaExtRef.setTabIndex(4);
		this.txtCsaExtRef.setMaxLength(50);
		this.lblCsaCompanyFlag.setValue("Firmen Kostenstelle");
		this.lblCsaState.setValue(StringResourceUtils.optLocalizeString("{$lblCsaState.value}", this));
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(7);
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(8);
		this.fieldGroup.bind(this.txtCsaCode, CostAccount_.csaCode.getName());
		this.fieldGroup.bind(this.txtCsaName, CostAccount_.csaName.getName());
		this.fieldGroup.bind(this.cmbCostAccount, CostAccount_.costAccount.getName());
		this.fieldGroup.bind(this.txtCsaExtRef, CostAccount_.csaExtRef.getName());
		this.fieldGroup.bind(this.comboBoxState, CostAccount_.csaState.getName());
		this.fieldGroup.bind(this.checkBoxFlagCompany, CostAccount_.csaFlagCompany.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "csaCode", "csaState",
				"costAccount", "csaName", "csaFlagCompany", "csaExtRef");
		this.containerFilterComponent.setSearchableProperties("csaCode", "csaName");

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
		this.form.setRows(8);
		this.checkBoxFlagCompany.setSizeUndefined();
		this.form.addComponent(this.checkBoxFlagCompany, 1, 4);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 5);
		this.lblCsaCode.setSizeUndefined();
		this.form.addComponent(this.lblCsaCode, 0, 0);
		this.txtCsaCode.setWidth(100, Unit.PERCENTAGE);
		this.txtCsaCode.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCsaCode, 1, 0);
		this.lblCsaName.setSizeUndefined();
		this.form.addComponent(this.lblCsaName, 0, 1);
		this.txtCsaName.setWidth(100, Unit.PERCENTAGE);
		this.txtCsaName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCsaName, 1, 1);
		this.lblCostAccount.setSizeUndefined();
		this.form.addComponent(this.lblCostAccount, 0, 2);
		this.cmbCostAccount.setWidth(100, Unit.PERCENTAGE);
		this.cmbCostAccount.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbCostAccount, 1, 2);
		this.lblCsaExtRef.setSizeUndefined();
		this.form.addComponent(this.lblCsaExtRef, 0, 3);
		this.txtCsaExtRef.setWidth(100, Unit.PERCENTAGE);
		this.txtCsaExtRef.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCsaExtRef, 1, 3);
		this.lblCsaCompanyFlag.setSizeUndefined();
		this.form.addComponent(this.lblCsaCompanyFlag, 0, 4);
		this.lblCsaState.setSizeUndefined();
		this.form.addComponent(this.lblCsaState, 0, 5);
		this.horizontalLayout2.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout2, 0, 6, 1, 6);
		this.form.setComponentAlignment(this.horizontalLayout2, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 7, 1, 7);
		this.form.setRowExpandRatio(7, 1.0F);
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
	private XdevLabel lblCsaCode, lblCsaName, lblCostAccount, lblCsaExtRef, lblCsaCompanyFlag, lblCsaState;
	private XdevTable<CostAccount> table;
	private XdevComboBox<CostAccount> cmbCostAccount;
	private XdevGridLayout form;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevFieldGroup<CostAccount> fieldGroup;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevComboBox<?> comboBoxState;
	private XdevCheckBox checkBoxFlagCompany;
	private XdevTextField txtCsaCode, txtCsaName, txtCsaExtRef;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
