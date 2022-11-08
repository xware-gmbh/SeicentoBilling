package ch.xwr.seicentobilling.ui.desktop.code;

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
import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.LabelDefinitionDAO;
import ch.xwr.seicentobilling.entities.LabelDefinition;
import ch.xwr.seicentobilling.entities.LabelDefinition_;
import ch.xwr.seicentobilling.ui.desktop.RowObjectView;

public class LabelDefinitionTabView extends XdevView {

	/**
	 *
	 */
	public LabelDefinitionTabView() {
		super();
		this.initUI();

		//Type
		this.comboBoxState.addItems((Object[])LovState.State.values());
		this.comboBoxType.addItems((Object[])LovCrm.LabelType.values());


		setROFields();
		setDefaultFilter();

	}

	private void setDefaultFilter() {
		final LovState.State[] valState = new LovState.State[] { LovState.State.active };
		final FilterData[] fd = new FilterData[] { new FilterData("cldState", new FilterOperator.Is(), valState) };

		this.containerFilterComponent.setFilterData(fd);

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
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getCldId(), this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

		cmdReload_buttonClick(event);
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
			this.cmdNew.setEnabled(true);
			this.cmdDelete.setEnabled(true);
		} else {
			this.cmdNew.setEnabled(false);
			this.cmdDelete.setEnabled(false);
			this.form.setEnabled(false);
		}

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
		this.table.getBeanContainerDataSource().addAll(new LabelDefinitionDAO().findAll());

		// reassign filter
		this.containerFilterComponent.setFilterData(fd);

		if (this.fieldGroup.getItemDataSource() != null) {
			final LabelDefinition bean = this.fieldGroup.getItemDataSource().getBean();
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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.setItemDataSource(getNewDaoWithDefaults());
		setROFields();

	}

	private LabelDefinition getNewDaoWithDefaults() {
		final LabelDefinition dao = new LabelDefinition();
		dao.setCldState(LovState.State.active);
		dao.setCldType(LovCrm.LabelType.user);
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
				final LabelDefinition bean = LabelDefinitionTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCldId(), bean.getClass().getSimpleName());

				final LabelDefinitionDAO dao = new LabelDefinitionDAO();
				dao.remove(bean);
				LabelDefinitionTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					LabelDefinitionTabView.this.table.select(LabelDefinitionTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					LabelDefinitionTabView.this.fieldGroup.setItemDataSource(new LabelDefinition());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});



	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final LabelDefinition bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCldId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

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
		this.lblCldText = new XdevLabel();
		this.txtCldText = new XdevTextField();
		this.lblCldType = new XdevLabel();
		this.comboBoxType = new XdevComboBox<>();
		this.lblCldState = new XdevLabel();
		this.comboBoxState = new XdevComboBox<>();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(LabelDefinition.class);

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
		this.table.setContainerDataSource(LabelDefinition.class, DAOs.get(LabelDefinitionDAO.class).findAll());
		this.table.setVisibleColumns(LabelDefinition_.cldText.getName(), LabelDefinition_.cldType.getName(),
				LabelDefinition_.cldState.getName());
		this.table.setColumnHeader("cldText", "Text");
		this.table.setColumnHeader("cldType", "Type");
		this.table.setColumnHeader("cldState", "Status");
		this.lblCldText.setValue(StringResourceUtils.optLocalizeString("{$lblCldText.value}", this));
		this.lblCldType.setValue(StringResourceUtils.optLocalizeString("{$lblCldType.value}", this));
		this.lblCldState.setValue(StringResourceUtils.optLocalizeString("{$lblCldState.value}", this));
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(FontAwesome.UNDO);
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.fieldGroup.bind(this.txtCldText, LabelDefinition_.cldText.getName());
		this.fieldGroup.bind(this.comboBoxState, LabelDefinition_.cldState.getName());
		this.fieldGroup.bind(this.comboBoxType, LabelDefinition_.cldType.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "cldText", "cldState",
				"cldType");
		this.containerFilterComponent.setSearchableProperties("cldText");

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
		this.form.setRows(5);
		this.lblCldText.setSizeUndefined();
		this.form.addComponent(this.lblCldText, 0, 0);
		this.txtCldText.setWidth(100, Unit.PERCENTAGE);
		this.txtCldText.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.txtCldText, 1, 0);
		this.lblCldType.setSizeUndefined();
		this.form.addComponent(this.lblCldType, 0, 1);
		this.comboBoxType.setSizeUndefined();
		this.form.addComponent(this.comboBoxType, 1, 1);
		this.lblCldState.setSizeUndefined();
		this.form.addComponent(this.lblCldState, 0, 2);
		this.comboBoxState.setSizeUndefined();
		this.form.addComponent(this.comboBoxState, 1, 2);
		this.horizontalLayout2.setSizeUndefined();
		this.form.addComponent(this.horizontalLayout2, 0, 3, 1, 3);
		this.form.setComponentAlignment(this.horizontalLayout2, Alignment.TOP_RIGHT);
		this.form.setColumnExpandRatio(1, 100.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 4, 1, 4);
		this.form.setRowExpandRatio(4, 1.0F);
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
	private XdevLabel lblCldText, lblCldType, lblCldState;
	private XdevHorizontalLayout horizontalLayout, horizontalLayout2;
	private XdevFieldGroup<LabelDefinition> fieldGroup;
	private XdevTable<LabelDefinition> table;
	private XdevComboBox<?> comboBoxType, comboBoxState;
	private XdevGridLayout form;
	private XdevTextField txtCldText;
	private XdevVerticalLayout verticalLayout;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
