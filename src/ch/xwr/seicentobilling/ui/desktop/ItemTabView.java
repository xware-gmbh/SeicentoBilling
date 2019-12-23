package ch.xwr.seicentobilling.ui.desktop;

import com.vaadin.data.Property;
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
import com.xdev.res.ApplicationResource;
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
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.masterdetail.MasterDetail;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.NumberRangeHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.ItemGroupDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.ItemGroup;
import ch.xwr.seicentobilling.entities.ItemGroup_;
import ch.xwr.seicentobilling.entities.Item_;
import ch.xwr.seicentobilling.entities.Vat;

public class ItemTabView extends XdevView {

	/**
	 *
	 */
	public ItemTabView() {
		super();
		this.initUI();

		//Type
		this.cbxState.addItems((Object[])LovState.State.values());
		this.cbxUnit.addItems((Object[])LovState.Unit.values());

		//set RO Fields
		setROFields();

	}

	private void setROFields() {

		boolean hasData = true;
		if (this.fieldGroup.getItemDataSource() == null) {
			hasData = false;
		}
		this.cmdSave.setEnabled(hasData);
		//this.tabSheet.setEnabled(hasData);
		this.form.setEnabled(hasData);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		if (isNew()) {
			cmdNew_buttonClick(event);
		} else {
			this.fieldGroup.discard();
		}
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		final boolean isNew = isNew(); //assign before save. is always false after save

		this.fieldGroup.save();
		setROFields();
		checkItemNumber(isNew, true);

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getItmId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

	}

	private void checkItemNumber(final boolean isNew, final boolean commitNbr) {
		if (! isNew) {
			return;
		}

		Integer nbr = null;
		try {
			nbr = Integer.parseInt(this.txtItmIdent.getValue());
		} catch (final Exception e){
			nbr = new Integer(0);
		}

		final NumberRangeHandler handler = new NumberRangeHandler();
		if (!commitNbr) {
			if (nbr > 0) {
				return ;
			}
			this.txtItmIdent.setValue(handler.getNewItemNumber(false, nbr).toString());
		} else {
			handler.getNewItemNumber(true, nbr);
		}

	}

	private boolean isNew() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return true;
		}
		final Item bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean.getItmId() == null || bean.getItmId() < 1) {
			return true;
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
		checkItemNumber(true, false);
		setROFields();

	}

	private Item getNewDaoWithDefaults() {
		final Item dao = new Item();

		dao.setItmState(LovState.State.active);
		dao.setItmPrice1(new Double(0));
		dao.setItmUnit(LovState.Unit.hour);
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
				final Item bean = ItemTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getItmId(), bean.getClass().getSimpleName());

				final ItemDAO dao = new ItemDAO();
				dao.remove(bean);
				ItemTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					ItemTabView.this.table.select(ItemTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					ItemTabView.this.fieldGroup.setItemDataSource(new Item());
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
		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
		this.table.sort();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final Item bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getItmId(), bean.getClass().getSimpleName()));
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
			//final Item bean = this.table.getSelectedItem().getBean();
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
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.form = new XdevGridLayout();
		this.cbxUnit = new XdevComboBox<>();
		this.cbxState = new XdevComboBox<>();
		this.lblItmIdent = new XdevLabel();
		this.txtItmIdent = new XdevTextField();
		this.lblItmName = new XdevLabel();
		this.txtItmName = new XdevTextField();
		this.lblItmPrice1 = new XdevLabel();
		this.txtItmPrice1 = new XdevTextField();
		this.lblItmPrice2 = new XdevLabel();
		this.txtItmPrice2 = new XdevTextField();
		this.lblItmUnit = new XdevLabel();
		this.lblItemGroup = new XdevLabel();
		this.cmbItemGroup = new XdevComboBox<>();
		this.lblVat = new XdevLabel();
		this.cmbVat = new XdevComboBox<>();
		this.lblAccount = new XdevLabel();
		this.txtAccount = new XdevTextField();
		this.lblItmState = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Item.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription("Neuer Datensatz");
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Item.class, DAOs.get(ItemDAO.class).findAll());
		this.table.setVisibleColumns(Item_.itmIdent.getName(), Item_.itmName.getName(), Item_.itmPrice1.getName(),
				Item_.itmUnit.getName(), Item_.itemGroup.getName(), Item_.vat.getName(), Item_.itmState.getName());
		this.table.setColumnHeader("itmIdent", "Ident");
		this.table.setColumnHeader("itmName", "Name");
		this.table.setColumnHeader("itmPrice1", "Preis 1");
		this.table.setConverter("itmPrice1", ConverterBuilder.stringToDouble().currency().build());
		this.table.setColumnHeader("itmUnit", "Einheit");
		this.table.setColumnHeader("itemGroup", "Gruppe");
		this.table.setColumnHeader("vat", "Mwst");
		this.table.setColumnCollapsed("vat", true);
		this.table.setColumnHeader("itmState", "Status");
		this.lblItmIdent.setValue(StringResourceUtils.optLocalizeString("{$lblItmIdent.value}", this));
		this.txtItmIdent.setTabIndex(3);
		this.txtItmIdent.setRequired(true);
		this.txtItmIdent.setMaxLength(40);
		this.lblItmName.setValue(StringResourceUtils.optLocalizeString("{$lblItmName.value}", this));
		this.txtItmName.setTabIndex(4);
		this.txtItmName.setRequired(true);
		this.txtItmName.setMaxLength(60);
		this.lblItmPrice1.setValue(StringResourceUtils.optLocalizeString("{$lblItmPrice1.value}", this));
		this.txtItmPrice1.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtItmPrice1.setTabIndex(5);
		this.txtItmPrice1.setRequired(true);
		this.lblItmPrice2.setValue(StringResourceUtils.optLocalizeString("{$lblItmPrice2.value}", this));
		this.txtItmPrice2.setConverter(ConverterBuilder.stringToDouble().currency().build());
		this.txtItmPrice2.setTabIndex(6);
		this.lblItmUnit.setValue(StringResourceUtils.optLocalizeString("{$lblItmUnit.value}", this));
		this.lblItemGroup.setValue(StringResourceUtils.optLocalizeString("{$lblItemGroup.value}", this));
		this.cmbItemGroup.setTabIndex(1);
		this.cmbItemGroup.setContainerDataSource(ItemGroup.class, DAOs.get(ItemGroupDAO.class).findAll());
		this.cmbItemGroup.setItemCaptionPropertyId(ItemGroup_.itgName.getName());
		this.lblVat.setValue(StringResourceUtils.optLocalizeString("{$lblVat.value}", this));
		this.cmbVat.setTabIndex(2);
		this.cmbVat.setRequired(true);
		this.cmbVat.setItemCaptionFromAnnotation(false);
		this.cmbVat.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAll());
		this.cmbVat.setItemCaptionPropertyId("fullName");
		this.lblAccount.setDescription("Externe BuHa Ertrags-Konto Nummer");
		this.lblAccount.setValue("Konto#");
		this.txtAccount.setConverter(ConverterBuilder.stringToDouble().groupingUsed(false).build());
		this.lblItmState.setValue(StringResourceUtils.optLocalizeString("{$lblItmState.value}", this));
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(11);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(10);
		this.fieldGroup.bind(this.cmbItemGroup, Item_.itemGroup.getName());
		this.fieldGroup.bind(this.cmbVat, Item_.vat.getName());
		this.fieldGroup.bind(this.txtItmIdent, Item_.itmIdent.getName());
		this.fieldGroup.bind(this.txtItmName, Item_.itmName.getName());
		this.fieldGroup.bind(this.txtItmPrice1, Item_.itmPrice1.getName());
		this.fieldGroup.bind(this.txtItmPrice2, Item_.itmPrice2.getName());
		this.fieldGroup.bind(this.cbxUnit, Item_.itmUnit.getName());
		this.fieldGroup.bind(this.txtAccount, Item_.itmAccount.getName());
		this.fieldGroup.bind(this.cbxState, Item_.itmState.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "itmIdent", "itmName",
				"itmPrice1", "itmUnit", "itmState", "itemGroup", "vat");
		this.containerFilterComponent.setSearchableProperties("itmIdent", "itmName");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		final CustomComponent actionLayout_spacer = new CustomComponent();
		actionLayout_spacer.setSizeFull();
		this.actionLayout.addComponent(actionLayout_spacer);
		this.actionLayout.setExpandRatio(actionLayout_spacer, 1.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.actionLayout.setWidth(100, Unit.PERCENTAGE);
		this.actionLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.actionLayout);
		this.verticalLayout.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_CENTER);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_RIGHT);
		this.form.setColumns(2);
		this.form.setRows(11);
		this.cbxUnit.setSizeUndefined();
		this.form.addComponent(this.cbxUnit, 1, 4);
		this.cbxState.setSizeUndefined();
		this.form.addComponent(this.cbxState, 1, 8);
		this.lblItmIdent.setSizeUndefined();
		this.form.addComponent(this.lblItmIdent, 0, 0);
		this.txtItmIdent.setWidth(100, Unit.PERCENTAGE);
		this.txtItmIdent.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtItmIdent, 1, 0);
		this.lblItmName.setSizeUndefined();
		this.form.addComponent(this.lblItmName, 0, 1);
		this.txtItmName.setWidth(100, Unit.PERCENTAGE);
		this.txtItmName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtItmName, 1, 1);
		this.lblItmPrice1.setSizeUndefined();
		this.form.addComponent(this.lblItmPrice1, 0, 2);
		this.txtItmPrice1.setWidth(100, Unit.PERCENTAGE);
		this.txtItmPrice1.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtItmPrice1, 1, 2);
		this.lblItmPrice2.setSizeUndefined();
		this.form.addComponent(this.lblItmPrice2, 0, 3);
		this.txtItmPrice2.setWidth(100, Unit.PERCENTAGE);
		this.txtItmPrice2.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtItmPrice2, 1, 3);
		this.lblItmUnit.setSizeUndefined();
		this.form.addComponent(this.lblItmUnit, 0, 4);
		this.lblItemGroup.setSizeUndefined();
		this.form.addComponent(this.lblItemGroup, 0, 5);
		this.cmbItemGroup.setWidth(100, Unit.PERCENTAGE);
		this.cmbItemGroup.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbItemGroup, 1, 5);
		this.lblVat.setSizeUndefined();
		this.form.addComponent(this.lblVat, 0, 6);
		this.cmbVat.setWidth(100, Unit.PERCENTAGE);
		this.cmbVat.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.cmbVat, 1, 6);
		this.lblAccount.setSizeUndefined();
		this.form.addComponent(this.lblAccount, 0, 7);
		this.txtAccount.setSizeUndefined();
		this.form.addComponent(this.txtAccount, 1, 7);
		this.lblItmState.setSizeUndefined();
		this.form.addComponent(this.lblItmState, 0, 8);
		this.horizontalLayout.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout, 0, 9, 1, 9);
		this.form.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 10, 1, 10);
		this.form.setRowExpandRatio(10, 1.0F);
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
	private XdevLabel lblItmIdent, lblItmName, lblItmPrice1, lblItmPrice2, lblItmUnit, lblItemGroup, lblVat, lblAccount,
			lblItmState;
	private XdevFieldGroup<Item> fieldGroup;
	private XdevGridLayout form;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevComboBox<ItemGroup> cmbItemGroup;
	private XdevHorizontalLayout actionLayout, horizontalLayout;
	private XdevComboBox<Vat> cmbVat;
	private XdevComboBox<?> cbxUnit, cbxState;
	private XdevTextField txtItmIdent, txtItmName, txtItmPrice1, txtItmPrice2, txtAccount;
	private XdevVerticalLayout verticalLayout;
	private XdevTable<Item> table;
	// </generated-code>

}
