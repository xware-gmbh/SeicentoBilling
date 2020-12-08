
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.Vat;


@Route("vat")
public class VatTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(VatTabView.class);

	/**
	 *
	 */
	public VatTabView()
	{
		super();
		this.initUI();
		// State
		this.comboBoxState.setItems(LovState.State.values());

		this.setROFields();
		this.setDefaultFilter();
	}

	private void setROFields()
	{
		if(Seicento.hasRole("BillingAdmin"))
		{
			this.txtVatExtRef.setEnabled(true);
		}
		else
		{
			this.txtVatExtRef.setEnabled(false);
		}
	}

	private void setDefaultFilter()
	{
		final FilterEntry fe =
			new FilterEntry("vatState", new FilterOperator.Is().key(), new Object[]{LovState.State.active});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{fe}));

	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Vat> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final VatDAO coustAccountDao  = new VatDAO();
			final Vat    coustAccountBean =
				coustAccountDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getVatId());
			this.binder.setBean(coustAccountBean);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onClick(final ClickEvent<Button> event)
	{
		final Vat bean = new Vat();
		bean.setVatState(LovState.State.active);
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
		
		// save filter
		final FilterData fd = this.containerFilterComponent.getValue();
		this.containerFilterComponent.setValue(null);
		
		// clear+reload List
		this.grid.setDataProvider(DataProvider.ofCollection(new VatDAO().findAllActive()));
		
		// reassign filter
		this.containerFilterComponent.setValue(fd);
		
		if(this.binder.getBean() != null)
		{
			final Vat bean = this.binder.getBean();
			if(bean != null)
			{
				this.grid.select(bean);
			}
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		
		if(SeicentoCrud.doSave(this.binder, new VatDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getVatId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				VatTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		
		this.cmdReload_onClick(null);

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
			final VatDAO coustAccountDao  = new VatDAO();
			final Vat    coustAccountBean = coustAccountDao.find(this.binder.getBean().getVatId());
			this.binder.setBean(coustAccountBean);
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
			final Vat    bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getVatId(), bean.getClass().getSimpleName()));
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
		
		ConfirmDialog.show(this.getUI().get(), "Datensatz löschen", "Wirklich löschen?");
		
		try
		{
			
			final Vat bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			
			// Delete Record
			final RowObjectManager man = new RowObjectManager();
			man.deleteObject(bean.getVatId(), bean.getClass().getSimpleName());
			
			final VatDAO dao = new VatDAO();
			dao.remove(bean);
			dao.flush();
			
			this.binder.removeBean();
			VatTabView.this.binder.setBean(new Vat());
			this.grid.setDataProvider(DataProvider.ofCollection(new VatDAO().findAll()));
			VatTabView.this.grid.getDataProvider().refreshAll();
			
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
			VatTabView.LOG.error("Error on delete", e);
		}
		
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout              = new SplitLayout();
		this.verticalLayout           = new VerticalLayout();
		this.containerFilterComponent = new FilterComponent();
		this.horizontalLayout2        = new HorizontalLayout();
		this.cmdNew                   = new Button();
		this.cmdDelete                = new Button();
		this.cmdReload                = new Button();
		this.cmdInfo                  = new Button();
		this.grid                     = new Grid<>(Vat.class, false);
		this.formLayout               = new FormLayout();
		this.formItem2                = new FormItem();
		this.lblVatName               = new Label();
		this.txtVatName               = new TextField();
		this.formItem                 = new FormItem();
		this.lblVatSign               = new Label();
		this.txtVatSign               = new TextField();
		this.formItem3                = new FormItem();
		this.lblVatInclude            = new Label();
		this.chkVatInclude            = new Checkbox();
		this.formItem4                = new FormItem();
		this.lblVatExtRef             = new Label();
		this.txtVatExtRef             = new TextField();
		this.formItem5                = new FormItem();
		this.lblVatExtRef2            = new Label();
		this.txtVatExtRef1            = new TextField();
		this.formItem6                = new FormItem();
		this.lblVatState              = new Label();
		this.comboBoxState            = new ComboBox<>();
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(Vat.class);
		
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(Vat::getVatName).setKey("vatName").setHeader("Name").setSortable(true);
		this.grid.addColumn(Vat::getVatSign).setKey("vatSign").setHeader("Sign").setSortable(true);
		this.grid.addColumn(Vat::getVatInclude).setKey("vatInclude").setHeader("Include").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Vat::getVatState)).setKey("vatState").setHeader("Status")
			.setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new VatDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.formItem2.getElement().setAttribute("colspan", "4");
		this.lblVatName.setMaxHeight("");
		this.lblVatName.setText("Name");
		this.formItem.getElement().setAttribute("colspan", "4");
		this.lblVatSign.setText("Zeichen");
		this.formItem3.getElement().setAttribute("colspan", "4");
		this.lblVatInclude.setText("Inklusiv");
		this.formItem4.getElement().setAttribute("colspan", "4");
		this.lblVatExtRef.setText("Ext Ref 1");
		this.lblVatExtRef2.setText("Ext Ref 2");
		this.formItem6.getElement().setAttribute("colspan", "4");
		this.lblVatState.setText("Status");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Verwerfen");
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.binder.setValidatorsDisabled(true);
		
		this.binder.forField(this.comboBoxState).bind("vatState");
		this.binder.forField(this.txtVatName).withNullRepresentation("").bind("vatName");
		this.binder.forField(this.chkVatInclude).withNullRepresentation(false).bind("vatInclude");
		this.binder.forField(this.txtVatExtRef1).withNullRepresentation("").bind("vatExtRef1");
		this.binder.forField(this.txtVatExtRef).withNullRepresentation("").bind("vatExtRef");
		this.binder.forField(this.txtVatSign).withNullRepresentation("").bind("vatSign");
		
		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("vatName", "vatSign"), Arrays.asList("vatInclude", "vatName", "vatState")));
		
		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdReload, this.cmdInfo);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.lblVatName.setSizeUndefined();
		this.lblVatName.getElement().setAttribute("slot", "label");
		this.txtVatName.setWidthFull();
		this.txtVatName.setHeight(null);
		this.formItem2.add(this.lblVatName, this.txtVatName);
		this.lblVatSign.setSizeUndefined();
		this.lblVatSign.getElement().setAttribute("slot", "label");
		this.txtVatSign.setWidthFull();
		this.txtVatSign.setHeight(null);
		this.formItem.add(this.lblVatSign, this.txtVatSign);
		this.lblVatInclude.setSizeUndefined();
		this.lblVatInclude.getElement().setAttribute("slot", "label");
		this.chkVatInclude.setSizeUndefined();
		this.formItem3.add(this.lblVatInclude, this.chkVatInclude);
		this.lblVatExtRef.setSizeUndefined();
		this.lblVatExtRef.getElement().setAttribute("slot", "label");
		this.txtVatExtRef.setWidthFull();
		this.txtVatExtRef.setHeight(null);
		this.formItem4.add(this.lblVatExtRef, this.txtVatExtRef);
		this.lblVatExtRef2.setSizeUndefined();
		this.lblVatExtRef2.getElement().setAttribute("slot", "label");
		this.txtVatExtRef1.setWidthFull();
		this.txtVatExtRef1.setHeight(null);
		this.formItem5.add(this.lblVatExtRef2, this.txtVatExtRef1);
		this.lblVatState.setSizeUndefined();
		this.lblVatState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblVatState, this.comboBoxState);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight(null);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem5,
			this.formItem6,
			this.horizontalLayout3);
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
	private Grid<Vat>                 grid;
	private BeanValidationBinder<Vat> binder;
	private VerticalLayout            verticalLayout;
	private HorizontalLayout          horizontalLayout2, horizontalLayout3;
	private Label                     lblVatName, lblVatSign, lblVatInclude, lblVatExtRef, lblVatExtRef2, lblVatState;
	private FilterComponent           containerFilterComponent;
	private FormItem                  formItem2, formItem, formItem3, formItem4, formItem5, formItem6;
	private FormLayout                formLayout;
	private Checkbox                  chkVatInclude;
	private Button                    cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private ComboBox<State>           comboBoxState;
	private SplitLayout               splitLayout;
	private TextField                 txtVatName, txtVatSign, txtVatExtRef, txtVatExtRef1;
	// </generated-code>
	
}
