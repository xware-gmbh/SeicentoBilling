
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
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
import com.vaadin.flow.component.grid.GridSortOrder;
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
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.LovState.WorkType;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLineTemplate;


@Route("projectlinetemplate")
public class ProjectLineTemplateTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ProjectLineTemplateTabView.class);

	/**
	 *
	 */
	public ProjectLineTemplateTabView()
	{
		super();
		this.initUI();
		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxWorkType.setItems(LovState.WorkType.values());

		this.sortList();
		
		this.setROFields();
		this.setDefaultFilter();
	}

	private void setROFields()
	{
		if(Seicento.hasRole("BillingAdmin"))
		{
			this.txtPrtText.setEnabled(true);
		}
		else
		{
			this.txtPrtText.setEnabled(false);
		}
	}

	private void setDefaultFilter()
	{
		final CostAccount bean = Seicento.getLoggedInCostAccount();

		final FilterEntry pe =
			new FilterEntry("prtState", new FilterOperator.Is().key(), new LovState.State[]{LovState.State.active});
		final FilterEntry ce =
			new FilterEntry("costAccount", new FilterOperator.Is().key(), new CostAccount[]{bean});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{ce, pe}));

	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<ProjectLineTemplate> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final ProjectLineTemplateDAO projectLineDao  = new ProjectLineTemplateDAO();
			final ProjectLineTemplate    projectLineBean =
				projectLineDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getPrtId());
			this.binder.setBean(projectLineBean);
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
		CostAccount bean1 = Seicento.getLoggedInCostAccount();
		if(bean1 == null)
		{
			bean1 = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		final ProjectLineTemplate bean = new ProjectLineTemplate();

		bean.setPrtState(LovState.State.active);
		bean.setCostAccount(bean1);
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
		this.grid.setDataProvider(DataProvider.ofCollection(new ProjectLineTemplateDAO().findAll()));
		this.grid.getDataProvider().refreshAll();
		
		this.sortList();
		
		// reassign filter
		this.containerFilterComponent.setValue(fd);

	}
	
	private void sortList()
	{
		final GridSortOrder<ProjectLineTemplate> prtKeyNumber =
			new GridSortOrder<>(this.grid.getColumnByKey("prtKeyNumber"), SortDirection.ASCENDING);
		this.grid.sort(Arrays.asList(prtKeyNumber));
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		
		if(SeicentoCrud.doSave(this.binder, new ProjectLineTemplateDAO()))
		{
			try
			{
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getPrtId(),
					this.binder.getBean().getClass().getSimpleName());
			}
			catch(final Exception e)
			{
				ProjectLineTemplateTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		this.setROFields();
		
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
			final ProjectLineTemplateDAO projectLineDao  = new ProjectLineTemplateDAO();
			final ProjectLineTemplate    projectLineBean = projectLineDao.find(this.binder.getBean().getPrtId());
			this.binder.setBean(projectLineBean);
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
			final ProjectLineTemplate bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog              win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getPrtId(), bean.getClass().getSimpleName()));
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
				
				final ProjectLineTemplate bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrtId(), bean.getClass().getSimpleName());
				
				final ProjectLineTemplateDAO dao = new ProjectLineTemplateDAO();
				dao.remove(bean);
				dao.flush();
				
				this.binder.removeBean();
				ProjectLineTemplateTabView.this.binder.setBean(new ProjectLineTemplate());
				this.grid.setDataProvider(DataProvider.ofCollection(new ProjectLineTemplateDAO().findAll()));
				ProjectLineTemplateTabView.this.grid.getDataProvider().refreshAll();
				
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
				ProjectLineTemplateTabView.LOG.error("Error on delete", e);
			}
		});
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
		this.grid                     = new Grid<>(ProjectLineTemplate.class, false);
		this.formLayout               = new FormLayout();
		this.formItem2                = new FormItem();
		this.lblCostAccount           = new Label();
		this.cmbCostAccount           = new ComboBox<>();
		this.formItem                 = new FormItem();
		this.lblPrtKeyNumber          = new Label();
		this.txtPrtKeyNumber          = new TextField();
		this.formItem3                = new FormItem();
		this.lblPrtProject            = new Label();
		this.comboBoxProject          = new ComboBox<>();
		this.formItem4                = new FormItem();
		this.lblPrtText               = new Label();
		this.txtPrtText               = new TextField();
		this.formItem5                = new FormItem();
		this.lblPrtHours              = new Label();
		this.txtPrtHours              = new TextField();
		this.formItem7                = new FormItem();
		this.lblPrtRate               = new Label();
		this.txtPrtRate               = new TextField();
		this.formItem8                = new FormItem();
		this.lblPrtWorktype           = new Label();
		this.comboBoxWorkType         = new ComboBox<>();
		this.formItem6                = new FormItem();
		this.lblPrtState              = new Label();
		this.comboBoxState            = new ComboBox<>();
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(ProjectLineTemplate.class);

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(ProjectLineTemplate::getPrtKeyNumber).setKey("prtKeyNumber").setHeader("Nummer")
			.setSortable(true);
		this.grid.addColumn(ProjectLineTemplate::getprtText).setKey("prtText").setHeader("Text").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(ProjectLineTemplate::getProject)).setKey("project")
			.setHeader("Projekt")
			.setSortable(false);
		this.grid.addColumn(ProjectLineTemplate::getPrtRate).setKey("prtRate").setHeader("Ansatz").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(ProjectLineTemplate::getPrtState)).setKey("prtState")
			.setHeader("Status")
			.setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(ProjectLineTemplate::getCostAccount)).setKey("costAccount")
			.setHeader("Account").setSortable(false).setVisible(false);
		this.grid.setDataProvider(DataProvider.ofCollection(new ProjectLineTemplateDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.formItem2.getElement().setAttribute("colspan", "4");
		this.lblCostAccount.setMaxHeight("");
		this.lblCostAccount.setText("Mitarbeiter");
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.formItem.getElement().setAttribute("colspan", "4");
		this.lblPrtKeyNumber.setText("Nummer");
		this.txtPrtKeyNumber.setPlaceholder("Values 1-10");
		this.formItem3.getElement().setAttribute("colspan", "4");
		this.lblPrtProject.setText("Projekt");
		this.comboBoxProject.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAll()));
		this.comboBoxProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.formItem4.getElement().setAttribute("colspan", "4");
		this.lblPrtText.setText("Text");
		this.formItem5.getElement().setAttribute("colspan", "4");
		this.lblPrtHours.setText("Stunden");
		this.formItem7.getElement().setAttribute("colspan", "4");
		this.lblPrtRate.setText("Ansatz");
		this.formItem8.getElement().setAttribute("colspan", "4");
		this.lblPrtWorktype.setText("Typ");
		this.comboBoxWorkType.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.formItem6.getElement().setAttribute("colspan", "4");
		this.lblPrtState.setText("Status");
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Abbrechen");
		this.cmdReset.setIcon(IronIcons.UNDO.create());
		this.binder.setValidatorsDisabled(true);

		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.txtPrtKeyNumber).asRequired().withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("prtKeyNumber");
		this.binder.forField(this.comboBoxProject).asRequired().bind("project");
		this.binder.forField(this.txtPrtText).withNullRepresentation("").bind("prtText");
		this.binder.forField(this.txtPrtHours).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("prtHours");
		this.binder.forField(this.txtPrtRate).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("prtRate");
		this.binder.forField(this.comboBoxWorkType).bind("prtWorkType");
		this.binder.forField(this.comboBoxState).bind("prtState");

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("costAccount.csaCode", "costAccount.csaName", "prtText"),
			Arrays.asList("costAccount", "project", "prtRate", "prtState", "prtWorkType")));

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
		this.lblCostAccount.setSizeUndefined();
		this.lblCostAccount.getElement().setAttribute("slot", "label");
		this.cmbCostAccount.setSizeUndefined();
		this.formItem2.add(this.lblCostAccount, this.cmbCostAccount);
		this.lblPrtKeyNumber.setSizeUndefined();
		this.lblPrtKeyNumber.getElement().setAttribute("slot", "label");
		this.txtPrtKeyNumber.setWidthFull();
		this.txtPrtKeyNumber.setHeight(null);
		this.formItem.add(this.lblPrtKeyNumber, this.txtPrtKeyNumber);
		this.lblPrtProject.setSizeUndefined();
		this.lblPrtProject.getElement().setAttribute("slot", "label");
		this.comboBoxProject.setSizeUndefined();
		this.formItem3.add(this.lblPrtProject, this.comboBoxProject);
		this.lblPrtText.setSizeUndefined();
		this.lblPrtText.getElement().setAttribute("slot", "label");
		this.txtPrtText.setWidthFull();
		this.txtPrtText.setHeight(null);
		this.formItem4.add(this.lblPrtText, this.txtPrtText);
		this.lblPrtHours.setSizeUndefined();
		this.lblPrtHours.getElement().setAttribute("slot", "label");
		this.txtPrtHours.setWidthFull();
		this.txtPrtHours.setHeight(null);
		this.formItem5.add(this.lblPrtHours, this.txtPrtHours);
		this.lblPrtRate.setSizeUndefined();
		this.lblPrtRate.getElement().setAttribute("slot", "label");
		this.txtPrtRate.setWidthFull();
		this.txtPrtRate.setHeight(null);
		this.formItem7.add(this.lblPrtRate, this.txtPrtRate);
		this.lblPrtWorktype.setSizeUndefined();
		this.lblPrtWorktype.getElement().setAttribute("slot", "label");
		this.comboBoxWorkType.setSizeUndefined();
		this.formItem8.add(this.lblPrtWorktype, this.comboBoxWorkType);
		this.lblPrtState.setSizeUndefined();
		this.lblPrtState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblPrtState, this.comboBoxState);
		this.cmdSave.setWidth("50%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("50%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight(null);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem3, this.formItem4, this.formItem5,
			this.formItem7,
			this.formItem8, this.formItem6, this.horizontalLayout3);
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
	private BeanValidationBinder<ProjectLineTemplate> binder;
	private ComboBox<WorkType>                        comboBoxWorkType;
	private VerticalLayout                            verticalLayout;
	private HorizontalLayout                          horizontalLayout2, horizontalLayout3;
	private Label                                     lblCostAccount, lblPrtKeyNumber, lblPrtProject, lblPrtText,
		lblPrtHours, lblPrtRate, lblPrtWorktype, lblPrtState;
	private FilterComponent                           containerFilterComponent;
	private FormItem                                  formItem2, formItem, formItem3, formItem4, formItem5, formItem7,
		formItem8, formItem6;
	private FormLayout                                formLayout;
	private Button                                    cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private Grid<ProjectLineTemplate>                 grid;
	private ComboBox<State>                           comboBoxState;
	private SplitLayout                               splitLayout;
	private ComboBox<Project>                         comboBoxProject;
	private TextField                                 txtPrtKeyNumber, txtPrtText, txtPrtHours, txtPrtRate;
	private ComboBox<CostAccount>                     cmbCostAccount;
	// </generated-code>
	
}
