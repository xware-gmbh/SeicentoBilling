package ch.xwr.seicentobilling.ui.desktop;

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
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
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
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.Vat_;

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
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getVatId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		cmdReload_buttonClick(event);
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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
//		this.table.refreshRowCache();
//		this.table.getBeanContainerDataSource().refresh();
//		this.table.sort();

		//save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		//clear+reload List
		this.table.removeAllItems();

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().addAll(new VatDAO().findAll());

		//sort Table
//		final Object [] properties={"proStartDate","proEndDate"};
//		final boolean [] ordering={false, false};
//		this.table.sort(properties, ordering);

		//reassign filter
		this.containerFilterComponent.setFilterData(fd);


		final Vat bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean != null) {
			this.table.select(bean);
		}

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

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.gridLayout = new XdevGridLayout();
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
		this.lblVatName = new XdevLabel();
		this.txtVatName = new XdevTextField();
		this.lblVatRate = new XdevLabel();
		this.txtVatRate = new XdevTextField();
		this.lblVatSign = new XdevLabel();
		this.txtVatSign = new XdevTextField();
		this.lblVatInclude = new XdevLabel();
		this.chkVatInclude = new XdevCheckBox();
		this.lblVatState = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Vat.class);

		this.gridLayout.setMargin(new MarginInfo(false));
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
		this.table.setVisibleColumns(Vat_.vatName.getName(), Vat_.vatRate.getName(), Vat_.vatSign.getName(),
				Vat_.vatInclude.getName(), Vat_.vatState.getName());
		this.table.setConverter("vatRate",
				ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.lblVatName.setValue("Name");
		this.txtVatName.setTabIndex(1);
		this.lblVatRate.setValue("Rate");
		this.txtVatRate
				.setConverter(ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.txtVatRate.setTabIndex(2);
		this.lblVatSign.setValue("Sign");
		this.txtVatSign.setTabIndex(3);
		this.lblVatInclude.setValue("Include");
		this.chkVatInclude.setCaption("");
		this.chkVatInclude.setTabIndex(4);
		this.lblVatState.setValue("State");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption("Save");
		this.cmdSave.setTabIndex(8);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption("Reset");
		this.cmdReset.setTabIndex(7);
		this.fieldGroup.bind(this.txtVatName, Vat_.vatName.getName());
		this.fieldGroup.bind(this.txtVatRate, Vat_.vatRate.getName());
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
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdSave);
		this.horizontalLayout2.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdReset);
		this.horizontalLayout2.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.form.setColumns(2);
		this.form.setRows(7);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 4);
		this.lblVatName.setSizeUndefined();
		this.form.addComponent(this.lblVatName, 0, 0);
		this.txtVatName.setWidth(100, Unit.PERCENTAGE);
		this.txtVatName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtVatName, 1, 0);
		this.lblVatRate.setSizeUndefined();
		this.form.addComponent(this.lblVatRate, 0, 1);
		this.txtVatRate.setWidth(100, Unit.PERCENTAGE);
		this.txtVatRate.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtVatRate, 1, 1);
		this.lblVatSign.setSizeUndefined();
		this.form.addComponent(this.lblVatSign, 0, 2);
		this.txtVatSign.setWidth(100, Unit.PERCENTAGE);
		this.txtVatSign.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtVatSign, 1, 2);
		this.lblVatInclude.setSizeUndefined();
		this.form.addComponent(this.lblVatInclude, 0, 3);
		this.chkVatInclude.setWidth(100, Unit.PERCENTAGE);
		this.chkVatInclude.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.chkVatInclude, 1, 3);
		this.lblVatState.setSizeUndefined();
		this.form.addComponent(this.lblVatState, 0, 4);
		this.horizontalLayout2.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout2, 0, 5, 1, 5);
		this.form.setComponentAlignment(this.horizontalLayout2, Alignment.TOP_CENTER);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 6, 1, 6);
		this.form.setRowExpandRatio(6, 1.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.form.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.form);
		this.gridLayout.setColumns(1);
		this.gridLayout.setRows(1);
		this.horizontalSplitPanel.setSizeFull();
		this.gridLayout.addComponent(this.horizontalSplitPanel, 0, 0);
		this.gridLayout.setColumnExpandRatio(0, 100.0F);
		this.gridLayout.setRowExpandRatio(0, 100.0F);
		this.gridLayout.setSizeFull();
		this.setContent(this.gridLayout);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private XdevLabel lblVatName, lblVatRate, lblVatSign, lblVatInclude, lblVatState;
	private XdevTable<Vat> table;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevComboBox<?> comboBoxState;
	private XdevCheckBox chkVatInclude;
	private XdevFieldGroup<Vat> fieldGroup;
	private XdevGridLayout gridLayout, form;
	private XdevTextField txtVatName, txtVatRate, txtVatSign;
	private XdevVerticalLayout verticalLayout;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
