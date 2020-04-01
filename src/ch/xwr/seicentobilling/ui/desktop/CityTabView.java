package ch.xwr.seicentobilling.ui.desktop;

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
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.City_;
import ch.xwr.seicentobilling.ui.desktop.crm.ImportZipPopup;

public class CityTabView extends XdevView {

	/**
	 *
	 */
	public CityTabView() {
		super();
		this.initUI();

		//State
		this.comboBoxState.addItems((Object[])LovState.State.values());

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
		final boolean isNew = isNew();

		this.fieldGroup.save();
		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);


	}

	private boolean isNew() {
		if (this.fieldGroup.getItemDataSource() == null) {
			return true;
		}
		final City bean = this.fieldGroup.getItemDataSource().getBean();
		if (bean.getCtyId() == null || bean.getCtyId() < 1) {
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
		final City bean = new City();
		bean.setCtyState(LovState.State.active);

		this.fieldGroup.setItemDataSource(bean);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final City bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCtyId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
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
				final City bean = CityTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCtyId(), bean.getClass().getSimpleName());

				final CityDAO dao = new CityDAO();
				dao.remove(bean);
				CityTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					CityTabView.this.table.select(CityTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					CityTabView.this.fieldGroup.setItemDataSource(new City());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});


	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdImport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdImport_buttonClick(final Button.ClickEvent event) {
		final Window win = ImportZipPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				cmdReload_buttonClick(null);
			}

		});
		this.getUI().addWindow(win);


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
		this.cmdImport = new XdevButton();
		this.table = new XdevTable<>();
		this.form = new XdevGridLayout();
		this.comboBoxState = new XdevComboBox<>();
		this.lblCtyName = new XdevLabel();
		this.txtCtyName = new XdevTextField();
		this.lblCtyCountry = new XdevLabel();
		this.txtCtyCountry = new XdevTextField();
		this.lblCtyRegion = new XdevLabel();
		this.txtCtyRegion = new XdevTextField();
		this.lblCtyGeoCoordinates = new XdevLabel();
		this.txtCtyGeoCoordinates = new XdevTextField();
		this.lblCtyZip = new XdevLabel();
		this.txtCtyZip = new XdevTextField();
		this.lblCtyState = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(City.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.cmdImport.setIcon(FontAwesome.FILE_EXCEL_O);
		this.cmdImport.setDescription("Import PLZ csv");
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(City.class);
		this.table.setVisibleColumns(City_.ctyName.getName(), City_.ctyCountry.getName(), City_.ctyZip.getName(),
				City_.ctyRegion.getName(), City_.ctyState.getName(), City_.ctyGeoCoordinates.getName());
		this.table.setColumnHeader("ctyName", "Name");
		this.table.setColumnHeader("ctyCountry", "Land");
		this.table.setColumnHeader("ctyZip", "Plz");
		this.table.setConverter("ctyZip", ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.table.setColumnHeader("ctyRegion", "Region");
		this.table.setColumnHeader("ctyState", "Status");
		this.table.setColumnHeader("ctyGeoCoordinates", "Koordinaten");
		this.table.setColumnCollapsed("ctyGeoCoordinates", true);
		this.lblCtyName.setValue(StringResourceUtils.optLocalizeString("{$lblCtyName.value}", this));
		this.txtCtyName.setTabIndex(1);
		this.txtCtyName.setRequired(true);
		this.lblCtyCountry.setValue(StringResourceUtils.optLocalizeString("{$lblCtyCountry.value}", this));
		this.txtCtyCountry.setTabIndex(2);
		this.lblCtyRegion.setValue(StringResourceUtils.optLocalizeString("{$lblCtyRegion.value}", this));
		this.txtCtyRegion.setTabIndex(3);
		this.lblCtyGeoCoordinates.setValue(StringResourceUtils.optLocalizeString("{$lblCtyGeoCoordinates.value}", this));
		this.txtCtyGeoCoordinates.setTabIndex(4);
		this.lblCtyZip.setValue(StringResourceUtils.optLocalizeString("{$lblCtyZip.value}", this));
		this.txtCtyZip.setConverter(ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.txtCtyZip.setTabIndex(5);
		this.txtCtyZip.setRequired(true);
		this.lblCtyState.setValue(StringResourceUtils.optLocalizeString("{$lblCtyState.value}", this));
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setTabIndex(9);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setTabIndex(8);
		this.fieldGroup.bind(this.txtCtyName, City_.ctyName.getName());
		this.fieldGroup.bind(this.txtCtyCountry, City_.ctyCountry.getName());
		this.fieldGroup.bind(this.txtCtyRegion, City_.ctyRegion.getName());
		this.fieldGroup.bind(this.txtCtyGeoCoordinates, City_.ctyGeoCoordinates.getName());
		this.fieldGroup.bind(this.txtCtyZip, City_.ctyZip.getName());
		this.fieldGroup.bind(this.comboBoxState, City_.ctyState.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "ctyName", "ctyCountry",
				"ctyRegion", "ctyState", "ctyZip");
		this.containerFilterComponent.setSearchableProperties("ctyName", "ctyRegion", "ctyCountry");

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
		this.cmdImport.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdImport);
		this.horizontalLayout.setComponentAlignment(this.cmdImport, Alignment.MIDDLE_RIGHT);
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
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 5);
		this.lblCtyName.setSizeUndefined();
		this.form.addComponent(this.lblCtyName, 0, 0);
		this.txtCtyName.setWidth(100, Unit.PERCENTAGE);
		this.txtCtyName.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCtyName, 1, 0);
		this.lblCtyCountry.setSizeUndefined();
		this.form.addComponent(this.lblCtyCountry, 0, 1);
		this.txtCtyCountry.setWidth(100, Unit.PERCENTAGE);
		this.txtCtyCountry.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCtyCountry, 1, 1);
		this.lblCtyRegion.setSizeUndefined();
		this.form.addComponent(this.lblCtyRegion, 0, 2);
		this.txtCtyRegion.setWidth(100, Unit.PERCENTAGE);
		this.txtCtyRegion.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCtyRegion, 1, 2);
		this.lblCtyGeoCoordinates.setSizeUndefined();
		this.form.addComponent(this.lblCtyGeoCoordinates, 0, 3);
		this.txtCtyGeoCoordinates.setWidth(100, Unit.PERCENTAGE);
		this.txtCtyGeoCoordinates.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCtyGeoCoordinates, 1, 3);
		this.lblCtyZip.setSizeUndefined();
		this.form.addComponent(this.lblCtyZip, 0, 4);
		this.txtCtyZip.setWidth(100, Unit.PERCENTAGE);
		this.txtCtyZip.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCtyZip, 1, 4);
		this.lblCtyState.setSizeUndefined();
		this.form.addComponent(this.lblCtyState, 0, 5);
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
		this.cmdImport.addClickListener(event -> this.cmdImport_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdImport, cmdSave, cmdReset;
	private XdevLabel lblCtyName, lblCtyCountry, lblCtyRegion, lblCtyGeoCoordinates, lblCtyZip, lblCtyState;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevComboBox<?> comboBoxState;
	private XdevFieldGroup<City> fieldGroup;
	private XdevTable<City> table;
	private XdevGridLayout form;
	private XdevTextField txtCtyName, txtCtyCountry, txtCtyRegion, txtCtyGeoCoordinates, txtCtyZip;
	private XdevVerticalLayout verticalLayout;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
