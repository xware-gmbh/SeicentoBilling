
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
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
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.entities.CostAccount;


@Route("coustaccount")
public class CostAccountTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(CostAccountTabView.class);

	/**
	 *
	 */
	public CostAccountTabView()
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
			this.txtCsaExtRef.setEnabled(true);
		}
		else
		{
			this.txtCsaExtRef.setEnabled(false);
		}
	}

	private void setDefaultFilter()
	{
		final FilterEntry fe =
			new FilterEntry("csaState", new FilterOperator.Is().key(), new Object[]{LovState.State.active});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{fe}));
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<CostAccount> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final CostAccountDAO coustAccountDao  = new CostAccountDAO();
			final CostAccount    coustAccountBean =
				coustAccountDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getCsaId());
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
		final CostAccount bean = new CostAccount();
		bean.setCsaState(LovState.State.active);
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
		this.grid.setDataProvider(DataProvider.ofCollection(new CostAccountDAO().findAll()));
		
		// reassign filter
		this.containerFilterComponent.setValue(fd);
		
		if(this.binder.getBean() != null)
		{
			final CostAccount bean = this.binder.getBean();
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
		
		if(SeicentoCrud.doSave(this.binder, new CostAccountDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getCsaId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				CostAccountTabView.LOG.error("could not save ObjRoot", e);
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
			final CostAccountDAO coustAccountDao  = new CostAccountDAO();
			final CostAccount    coustAccountBean = coustAccountDao.find(this.binder.getBean().getCsaId());
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
			final CostAccount bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog      win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getCsaId(), bean.getClass().getSimpleName()));
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
			
			final CostAccount bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			
			// Delete Record
			final RowObjectManager man = new RowObjectManager();
			man.deleteObject(bean.getCsaId(), bean.getClass().getSimpleName());
			
			final CostAccountDAO dao = new CostAccountDAO();
			dao.remove(bean);
			dao.flush();
			
			this.binder.removeBean();
			CostAccountTabView.this.binder.setBean(new CostAccount());
			this.grid.setDataProvider(DataProvider.ofCollection(new CostAccountDAO().findAll()));
			CostAccountTabView.this.grid.getDataProvider().refreshAll();
			
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
			CostAccountTabView.LOG.error("Error on delete", e);
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
		this.grid                     = new Grid<>(CostAccount.class, false);
		this.formLayout               = new FormLayout();
		this.formItem2                = new FormItem();
		this.lblCsaCode               = new Label();
		this.txtCsaCode               = new TextField();
		this.formItem                 = new FormItem();
		this.lblCsaName               = new Label();
		this.txtCsaName               = new TextField();
		this.formItem3                = new FormItem();
		this.lblCostAccount           = new Label();
		this.cmbCostAccount           = new ComboBox<>();
		this.formItem4                = new FormItem();
		this.lblCsaExtRef             = new Label();
		this.txtCsaExtRef             = new TextField();
		this.formItem6                = new FormItem();
		this.lblCsaState              = new Label();
		this.comboBoxState            = new ComboBox<>();
		this.formItem7                = new FormItem();
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(CostAccount.class);

		this.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(CostAccount::getCsaCode).setKey("csaCode").setHeader("Code").setSortable(true);
		this.grid.addColumn(CostAccount::getCsaName).setKey("csaName").setHeader("Name").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(CostAccount::getCsaState)).setKey("csaState").setHeader("Status")
			.setSortable(true);
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(CostAccount::getCostAccount).map(CostAccount::getCsaCode).orElse(null))
			.setKey("costAccount.csaCode").setHeader("Übergeordnet").setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.formItem2.getElement().setAttribute("colspan", "4");
		this.lblCsaCode.setMaxHeight("");
		this.lblCsaCode.setText(StringResourceUtils.optLocalizeString("{$lblCsaCode.value}", this));
		this.formItem.getElement().setAttribute("colspan", "4");
		this.lblCsaName.setText(StringResourceUtils.optLocalizeString("{$lblCsaName.value}", this));
		this.formItem3.getElement().setAttribute("colspan", "4");
		this.lblCostAccount.setText(StringResourceUtils.optLocalizeString("{$lblCostAccount.value}", this));
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAllActive()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.formItem4.getElement().setAttribute("colspan", "4");
		this.lblCsaExtRef.setText("Externe Referenz");
		this.formItem6.getElement().setAttribute("colspan", "4");
		this.lblCsaState.setText(StringResourceUtils.optLocalizeString("{$lblCsaState.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.binder.setValidatorsDisabled(true);

		this.binder.forField(this.txtCsaCode).withNullRepresentation("").bind("csaCode");
		this.binder.forField(this.txtCsaName).withNullRepresentation("").bind("csaName");
		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.txtCsaExtRef).withNullRepresentation("").bind("csaExtRef");
		this.binder.forField(this.comboBoxState).bind("csaState");

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("csaCode", "csaName"), Arrays.asList("costAccount", "csaCode", "csaName", "csaState")));

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
		this.lblCsaCode.setSizeUndefined();
		this.lblCsaCode.getElement().setAttribute("slot", "label");
		this.txtCsaCode.setWidthFull();
		this.txtCsaCode.setHeight(null);
		this.formItem2.add(this.lblCsaCode, this.txtCsaCode);
		this.lblCsaName.setSizeUndefined();
		this.lblCsaName.getElement().setAttribute("slot", "label");
		this.txtCsaName.setWidthFull();
		this.txtCsaName.setHeight(null);
		this.formItem.add(this.lblCsaName, this.txtCsaName);
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setSizeUndefined();
		this.formItem3.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblCsaExtRef.setSizeUndefined();
		this.lblCsaExtRef.getElement().setAttribute("slot", "label");
		this.txtCsaExtRef.setWidthFull();
		this.txtCsaExtRef.setHeight(null);
		this.formItem4.add(this.lblCsaExtRef, this.txtCsaExtRef);
		this.lblCsaState.setSizeUndefined();
		this.lblCsaState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblCsaState, this.comboBoxState);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("100px");
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem6,
			this.formItem7,
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
	private Grid<CostAccount>                 grid;
	private VerticalLayout                    verticalLayout;
	private HorizontalLayout                  horizontalLayout2, horizontalLayout3;
	private Label                             lblCsaCode, lblCsaName, lblCostAccount, lblCsaExtRef, lblCsaState;
	private FilterComponent                   containerFilterComponent;
	private FormItem                          formItem2, formItem, formItem3, formItem4, formItem6, formItem7;
	private FormLayout                        formLayout;
	private Button                            cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private ComboBox<State>                   comboBoxState;
	private SplitLayout                       splitLayout;
	private BeanValidationBinder<CostAccount> binder;
	private TextField                         txtCsaCode, txtCsaName, txtCsaExtRef;
	private ComboBox<CostAccount>             cmbCostAccount;
	// </generated-code>
	
}
