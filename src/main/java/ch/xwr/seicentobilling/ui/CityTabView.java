
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.FileIcons;
import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.entities.City;


@Route("city")
public class CityTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(CityTabView.class);
	
	/**
	 *
	 */
	public CityTabView()
	{
		super();
		this.initUI();
		// State
		this.comboBoxState.setItems(LovState.State.values());
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<City> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final CityDAO cityDao  = new CityDAO();
			final City    cityBean =
				cityDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getCtyId());
			this.binder.setBean(cityBean);
		}
	}
	
	private boolean isNew()
	{
		if(this.binder.getBean() == null)
		{
			return true;
		}
		final City bean = this.binder.getBean();
		if(bean.getCtyId() == null || bean.getCtyId() < 1)
		{
			return true;
		}
		return false;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onClick(final ClickEvent<Button> event)
	{
		final City bean = new City();
		bean.setCtyState(LovState.State.active);
		this.binder.setBean(bean);

		// this.fieldGroup.setItemDataSource(bean);
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_onClick(final ClickEvent<Button> event)
	{
		this.grid.setDataProvider(DataProvider.ofCollection(new CityDAO().findAll()));
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		final boolean isNew = this.isNew();
		
		if(SeicentoCrud.doSave(this.binder, new CityDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getCtyId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				CityTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		
		if(isNew)
		{
			this.cmdReload_onClick(null);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		if(this.binder.getBean() != null)
		{
			final CityDAO cityDao  = new CityDAO();
			final City    cityBean = cityDao.find(this.binder.getBean().getCtyId());
			this.binder.setBean(cityBean);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfo}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	
	private void cmdInfo_onClick(final ClickEvent<Button> event)
	{
		
		if(this.grid.getSelectedItems() != null)
		{
			final City   bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getCtyId(), bean.getClass().getSimpleName()));
			win.open();
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDelete}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_onClick(final ClickEvent<Button> event)
	{
		if(this.grid.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				
				final City bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCtyId(), bean.getClass().getSimpleName());
				
				final CityDAO dao = new CityDAO();
				dao.remove(bean);
				dao.flush();
				
				this.binder.removeBean();
				CityTabView.this.binder.setBean(new City());
				this.grid.setDataProvider(DataProvider.ofCollection(new CityDAO().findAll()));
				CityTabView.this.grid.getDataProvider().refreshAll();
				
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				
			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
			catch(final Exception e)
			{
				CityTabView.LOG.error("Error on delete", e);
			}
		});
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout          = new SplitLayout();
		this.verticalLayout       = new VerticalLayout();
		this.filterComponent      = new FilterComponent();
		this.horizontalLayout2    = new HorizontalLayout();
		this.cmdNew               = new Button();
		this.cmdDelete            = new Button();
		this.cmdReload            = new Button();
		this.cmdInfo              = new Button();
		this.cmdImport            = new Button();
		this.grid                 = new Grid<>(City.class, false);
		this.formLayout           = new FormLayout();
		this.formItem2            = new FormItem();
		this.lblCtyCountry        = new Label();
		this.txtCtyCountry        = new TextField();
		this.formItem             = new FormItem();
		this.lblCtyName           = new Label();
		this.txtCtyName           = new TextField();
		this.formItem3            = new FormItem();
		this.lblCtyRegion         = new Label();
		this.txtCtyRegion         = new TextField();
		this.formItem4            = new FormItem();
		this.lblCtyGeoCoordinates = new Label();
		this.txtCtyGeoCoordinates = new TextField();
		this.formItem5            = new FormItem();
		this.lblCtyZip            = new Label();
		this.txtCtyZip            = new NumberField();
		this.formItem6            = new FormItem();
		this.lblCtyState          = new Label();
		this.comboBoxState        = new ComboBox<>();
		this.formItem7            = new FormItem();
		this.horizontalLayout3    = new HorizontalLayout();
		this.cmdSave              = new Button();
		this.cmdReset             = new Button();
		this.binder               = new BeanValidationBinder<>(City.class);

		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.cmdImport.setIcon(FileIcons.EXCEL.create());
		this.grid.addColumn(City::getCtyName).setKey("ctyName").setHeader("Name").setSortable(true);
		this.grid.addColumn(City::getCtyCountry).setKey("ctyCountry").setHeader("Land").setSortable(true);
		this.grid.addColumn(City::getCtyZip).setKey("ctyZip").setHeader("Plz").setSortable(true);
		this.grid.addColumn(City::getCtyRegion).setKey("ctyRegion").setHeader("Region").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(City::getCtyState)).setKey("ctyState").setHeader("Status")
			.setSortable(true);
		this.grid.addColumn(City::getCtyGeoCoordinates).setKey("ctyGeoCoordinates").setHeader("Koordinaten")
			.setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new CityDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.formItem2.getElement().setAttribute("colspan", "4");
		this.lblCtyCountry.setMaxHeight("");
		this.lblCtyCountry.setText(StringResourceUtils.optLocalizeString("{$lblCtyCountry.value}", this));
		this.formItem.getElement().setAttribute("colspan", "4");
		this.lblCtyName.setText(StringResourceUtils.optLocalizeString("{$lblCtyName.value}", this));
		this.formItem3.getElement().setAttribute("colspan", "4");
		this.lblCtyRegion.setText(StringResourceUtils.optLocalizeString("{$lblCtyRegion.value}", this));
		this.formItem4.getElement().setAttribute("colspan", "4");
		this.lblCtyGeoCoordinates.setText(StringResourceUtils.optLocalizeString("{$lblCtyGeoCoordinates.value}", this));
		this.formItem5.getElement().setAttribute("colspan", "4");
		this.lblCtyZip.setText(StringResourceUtils.optLocalizeString("{$lblCtyZip.value}", this));
		this.formItem6.getElement().setAttribute("colspan", "4");
		this.lblCtyState.setText(StringResourceUtils.optLocalizeString("{$lblCtyState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.binder.setValidatorsDisabled(true);

		this.binder.forField(this.txtCtyCountry).withNullRepresentation("").bind("ctyCountry");
		this.binder.forField(this.txtCtyName).asRequired().withNullRepresentation("").bind("ctyName");
		this.binder.forField(this.txtCtyRegion).withNullRepresentation("").bind("ctyRegion");
		this.binder.forField(this.txtCtyGeoCoordinates).withNullRepresentation("").bind("ctyGeoCoordinates");
		this.binder.forField(this.txtCtyZip).asRequired().withConverter(ConverterBuilder.DoubleToInteger().build())
			.bind("ctyZip");
		this.binder.forField(this.comboBoxState).bind("ctyState");

		this.filterComponent.connectWith(this.grid.getDataProvider());
		this.filterComponent.setFilterSubject(
			GridFilterSubjectFactory.CreateFilterSubject(this.grid, Arrays.asList("ctyCountry", "ctyName", "ctyRegion"),
				Arrays.asList("ctyName", "ctyCountry", "ctyZip", "ctyRegion", "ctyState", "ctyGeoCoordinates")));

		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.cmdImport.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdInfo, this.cmdImport);
		this.filterComponent.setWidthFull();
		this.filterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.filterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.lblCtyCountry.setSizeUndefined();
		this.lblCtyCountry.getElement().setAttribute("slot", "label");
		this.txtCtyCountry.setWidthFull();
		this.txtCtyCountry.setHeight(null);
		this.formItem2.add(this.lblCtyCountry, this.txtCtyCountry);
		this.lblCtyName.setSizeUndefined();
		this.lblCtyName.getElement().setAttribute("slot", "label");
		this.txtCtyName.setWidthFull();
		this.txtCtyName.setHeight(null);
		this.formItem.add(this.lblCtyName, this.txtCtyName);
		this.lblCtyRegion.setSizeUndefined();
		this.lblCtyRegion.getElement().setAttribute("slot", "label");
		this.txtCtyRegion.setWidthFull();
		this.txtCtyRegion.setHeight(null);
		this.formItem3.add(this.lblCtyRegion, this.txtCtyRegion);
		this.lblCtyGeoCoordinates.setSizeUndefined();
		this.lblCtyGeoCoordinates.getElement().setAttribute("slot", "label");
		this.txtCtyGeoCoordinates.setWidthFull();
		this.txtCtyGeoCoordinates.setHeight(null);
		this.formItem4.add(this.lblCtyGeoCoordinates, this.txtCtyGeoCoordinates);
		this.lblCtyZip.setSizeUndefined();
		this.lblCtyZip.getElement().setAttribute("slot", "label");
		this.txtCtyZip.setWidthFull();
		this.txtCtyZip.setHeight(null);
		this.formItem5.add(this.lblCtyZip, this.txtCtyZip);
		this.lblCtyState.setSizeUndefined();
		this.lblCtyState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblCtyState, this.comboBoxState);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("100px");
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6,
			this.formItem7, this.horizontalLayout3);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.formLayout);
		this.splitLayout.setSplitterPosition(60.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Grid<City>                 grid;
	private NumberField                txtCtyZip;
	private VerticalLayout             verticalLayout;
	private HorizontalLayout           horizontalLayout2, horizontalLayout3;
	private Label                      lblCtyCountry, lblCtyName, lblCtyRegion, lblCtyGeoCoordinates, lblCtyZip,
		lblCtyState;
	private FilterComponent            filterComponent;
	private FormItem                   formItem2, formItem, formItem3, formItem4, formItem5, formItem6, formItem7;
	private FormLayout                 formLayout;
	private Button                     cmdNew, cmdDelete, cmdReload, cmdInfo, cmdImport, cmdSave, cmdReset;
	private ComboBox<State>            comboBoxState;
	private BeanValidationBinder<City> binder;
	private SplitLayout                splitLayout;
	private TextField                  txtCtyCountry, txtCtyName, txtCtyRegion, txtCtyGeoCoordinates;
	// </generated-code>

}
