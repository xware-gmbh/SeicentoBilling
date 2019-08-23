package ch.xwr.seicentobilling.ui.desktop;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
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
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.masterdetail.MasterDetail;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.dal.VatLineDAO;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;
import ch.xwr.seicentobilling.entities.VatLine_;
import ch.xwr.seicentobilling.entities.Vat_;
import ch.xwr.seicentobilling.ui.desktop.code.VatLinePopup;

public class VatTabView extends XdevView {

	/**
	 *
	 */
	public VatTabView() {
		super();
		this.initUI();

		//Type
		this.comboBoxState.addItems((Object[])LovState.State.values());

		setDefaultFilter();
	}

	private void setDefaultFilter() {
		final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final FilterData[] fd = new FilterData[] { new FilterData("vatState", new FilterOperator.Is(), valState) };

		this.containerFilterComponent.setFilterData(fd);

	}

	private void reloadTableLineList() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return;
		}
		final Vat bean = this.fieldGroup.getItemDataSource().getBean();
		final XdevBeanContainer<VatLine> myList = this.tableVatLine.getBeanContainerDataSource();

		myList.removeAllItems();
		myList.addAll(new VatLineDAO().findByVat(bean));

		this.tableVatLine.refreshRowCache();
		this.tableVatLine.getBeanContainerDataSource().refresh();

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
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getVatId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		cmdReload_buttonClick(event);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		final Vat vat = new Vat();
		vat.setVatState(LovState.State.active);
		vat.setVatInclude(false);
		this.fieldGroup.setItemDataSource(vat);
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
				final Vat bean = VatTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getVatId(), bean.getClass().getSimpleName());

				final VatDAO dao = new VatDAO();
				dao.remove(bean);
				VatTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					VatTabView.this.table.select(VatTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					VatTabView.this.fieldGroup.setItemDataSource(new Vat());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
				cmdReload_buttonClick(event);
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
		//save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		//clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new VatDAO().findAll());

		//reassign filter
		this.containerFilterComponent.setFilterData(fd);


		final Vat bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean != null) {
			this.table.select(bean);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final Vat bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getVatId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {

		reloadTableLineList();
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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNewLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewLine_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			this.cmdSave.click();
		}

		final Long beanId = null;
		final Long objId = this.fieldGroup.getItemDataSource().getBean().getVatId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupVatLine();

	}

	private void popupVatLine() {
		final Window win = VatLinePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdSave")) {
					reloadTableLineList();
				}

			}
		});
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdEditLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEditLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableVatLine.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableVatLine.getSelectedItem().getBean().getVanId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupVatLine();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeletLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeletLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableVatLine.getSelectedItem() == null) {
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
				final VatLine bean = VatTabView.this.tableVatLine.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getVanId(), bean.getClass().getSimpleName());

				final VatLineDAO dao = new VatLineDAO();
				dao.remove(bean);
				reloadTableLineList();

				VatTabView.this.tableVatLine.select(VatTabView.this.tableVatLine.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableVatLine}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableVatLine_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final VatLine obj = (VatLine) event.getItemId();
			this.tableVatLine.select(obj); // reselect after double-click

			final Long beanId = obj.getVanId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupVatLine();
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
		this.tabSheet = new XdevTabSheet();
		this.panel = new XdevPanel();
		this.verticalLayout2 = new XdevVerticalLayout();
		this.gridLayout2 = new XdevGridLayout();
		this.lblVatName = new XdevLabel();
		this.txtVatName = new XdevTextField();
		this.lblVatSign = new XdevLabel();
		this.txtVatSign = new XdevTextField();
		this.lblVatInclude = new XdevLabel();
		this.chkVatInclude = new XdevCheckBox();
		this.lblVatState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout3 = new XdevHorizontalLayout();
		this.cmdNewLine = new XdevButton();
		this.cmdEditLine = new XdevButton();
		this.cmdDeletLine = new XdevButton();
		this.tableVatLine = new XdevTable<>();
		this.horizontalLayoutButtons = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Vat.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription("Neuen Datensatz anlegen");
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.table.setContainerDataSource(Vat.class, DAOs.get(VatDAO.class).findAll());
		this.table.setVisibleColumns(Vat_.vatName.getName(), Vat_.vatSign.getName(), Vat_.vatInclude.getName(),
				Vat_.vatState.getName());
		this.form.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.panel.setTabIndex(0);
		this.verticalLayout2.setCaption("Tab");
		this.verticalLayout2.setMargin(new MarginInfo(true, false, false, true));
		this.gridLayout2.setMargin(new MarginInfo(false));
		this.lblVatName.setValue("Name");
		this.txtVatName.setMaxLength(40);
		this.lblVatSign.setValue("Zeichen");
		this.lblVatInclude.setValue("Inklusiv");
		this.chkVatInclude.setCaption("");
		this.lblVatState.setValue("Status");
		this.horizontalLayout3.setSpacing(false);
		this.horizontalLayout3.setMargin(new MarginInfo(false, true, false, false));
		this.cmdNewLine.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewLine.setCaption("New...");
		this.cmdEditLine.setIcon(FontAwesome.EDIT);
		this.cmdEditLine.setCaption("Bearbeiten");
		this.cmdDeletLine.setIcon(FontAwesome.ERASER);
		this.cmdDeletLine.setCaption("Löschen");
		this.tableVatLine.setContainerDataSource(VatLine.class, false);
		this.tableVatLine.setVisibleColumns(VatLine_.vanValidFrom.getName(), VatLine_.vanRate.getName(),
				VatLine_.vanRemark.getName(), VatLine_.vanState.getName());
		this.tableVatLine.setColumnHeader("vanValidFrom", "Gültig ab");
		this.tableVatLine.setConverter("vanValidFrom", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableVatLine.setColumnHeader("vanRate", "Ansatz %");
		this.tableVatLine.setConverter("vanRate", ConverterBuilder.stringToDouble().groupingUsed(true)
				.minimumIntegerDigits(1).minimumFractionDigits(2).build());
		this.tableVatLine.setColumnHeader("vanRemark", "Bemerkung");
		this.tableVatLine.setColumnHeader("vanState", "Status");
		this.horizontalLayoutButtons.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption("Speichern");
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption("Verwerfen");
		this.fieldGroup.bind(this.txtVatName, Vat_.vatName.getName());
		this.fieldGroup.bind(this.txtVatSign, Vat_.vatSign.getName());
		this.fieldGroup.bind(this.chkVatInclude, Vat_.vatInclude.getName());
		this.fieldGroup.bind(this.comboBoxState, Vat_.vatState.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "vatName", "vatState",
				"vatInclude");
		this.containerFilterComponent.setSearchableProperties("vatName", "vatSign");

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
		this.gridLayout2.setColumns(2);
		this.gridLayout2.setRows(5);
		this.lblVatName.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblVatName, 0, 0);
		this.txtVatName.setWidth(100, Unit.PERCENTAGE);
		this.txtVatName.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.txtVatName, 1, 0);
		this.lblVatSign.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblVatSign, 0, 1);
		this.txtVatSign.setSizeUndefined();
		this.gridLayout2.addComponent(this.txtVatSign, 1, 1);
		this.lblVatInclude.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblVatInclude, 0, 2);
		this.chkVatInclude.setWidth(100, Unit.PERCENTAGE);
		this.chkVatInclude.setHeight(-1, Unit.PIXELS);
		this.gridLayout2.addComponent(this.chkVatInclude, 1, 2);
		this.lblVatState.setSizeUndefined();
		this.gridLayout2.addComponent(this.lblVatState, 0, 3);
		this.comboBoxState.setSizeUndefined();
		this.gridLayout2.addComponent(this.comboBoxState, 1, 3);
		this.gridLayout2.setColumnExpandRatio(1, 100.0F);
		final CustomComponent gridLayout2_vSpacer = new CustomComponent();
		gridLayout2_vSpacer.setSizeFull();
		this.gridLayout2.addComponent(gridLayout2_vSpacer, 0, 4, 1, 4);
		this.gridLayout2.setRowExpandRatio(4, 1.0F);
		this.cmdNewLine.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdNewLine);
		this.horizontalLayout3.setComponentAlignment(this.cmdNewLine, Alignment.MIDDLE_CENTER);
		this.cmdEditLine.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdEditLine);
		this.horizontalLayout3.setComponentAlignment(this.cmdEditLine, Alignment.MIDDLE_CENTER);
		this.cmdDeletLine.setSizeUndefined();
		this.horizontalLayout3.addComponent(this.cmdDeletLine);
		this.horizontalLayout3.setComponentAlignment(this.cmdDeletLine, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout3_spacer = new CustomComponent();
		horizontalLayout3_spacer.setSizeFull();
		this.horizontalLayout3.addComponent(horizontalLayout3_spacer);
		this.horizontalLayout3.setExpandRatio(horizontalLayout3_spacer, 1.0F);
		this.gridLayout2.setWidth(100, Unit.PERCENTAGE);
		this.gridLayout2.setHeight(-1, Unit.PIXELS);
		this.verticalLayout2.addComponent(this.gridLayout2);
		this.horizontalLayout3.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout3.setHeight(-1, Unit.PIXELS);
		this.verticalLayout2.addComponent(this.horizontalLayout3);
		this.verticalLayout2.setComponentAlignment(this.horizontalLayout3, Alignment.MIDDLE_CENTER);
		this.tableVatLine.setSizeFull();
		this.verticalLayout2.addComponent(this.tableVatLine);
		this.verticalLayout2.setComponentAlignment(this.tableVatLine, Alignment.MIDDLE_CENTER);
		this.verticalLayout2.setExpandRatio(this.tableVatLine, 10.0F);
		this.verticalLayout2.setSizeFull();
		this.panel.setContent(this.verticalLayout2);
		this.panel.setSizeFull();
		this.tabSheet.addTab(this.panel, "Details", null);
		this.tabSheet.setSelectedTab(this.panel);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayoutButtons.addComponent(this.cmdSave);
		this.horizontalLayoutButtons.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayoutButtons.addComponent(this.cmdReset);
		this.horizontalLayoutButtons.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(1);
		this.form.setRows(2);
		this.tabSheet.setSizeFull();
		this.form.addComponent(this.tabSheet, 0, 0);
		this.horizontalLayoutButtons.setSizeUndefined();
		this.form.addComponent(this.horizontalLayoutButtons, 0, 1);
		this.form.setComponentAlignment(this.horizontalLayoutButtons, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(0, 100.0F);
		this.form.setRowExpandRatio(0, 100.0F);
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
		this.cmdNewLine.addClickListener(event -> this.cmdNewLine_buttonClick(event));
		this.cmdEditLine.addClickListener(event -> this.cmdEditLine_buttonClick(event));
		this.cmdDeletLine.addClickListener(event -> this.cmdDeletLine_buttonClick(event));
		this.tableVatLine.addItemClickListener(event -> this.tableVatLine_itemClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdNewLine, cmdEditLine, cmdDeletLine, cmdSave, cmdReset;
	private XdevLabel lblVatName, lblVatSign, lblVatInclude, lblVatState;
	private XdevTabSheet tabSheet;
	private XdevPanel panel;
	private XdevFieldGroup<Vat> fieldGroup;
	private XdevGridLayout form, gridLayout2;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevTable<VatLine> tableVatLine;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevTable<Vat> table;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout3, horizontalLayoutButtons;
	private XdevComboBox<?> comboBoxState;
	private XdevCheckBox chkVatInclude;
	private XdevTextField txtVatName, txtVatSign;
	private XdevVerticalLayout verticalLayout, verticalLayout2;
	// </generated-code>

}
