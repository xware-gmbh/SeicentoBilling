
package ch.xwr.seicentobilling.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

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
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.ExpType;
import ch.xwr.seicentobilling.business.LovState.ExpUnit;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;
import ch.xwr.seicentobilling.dal.LovAccountDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.VatDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.ExpenseTemplate;
import ch.xwr.seicentobilling.entities.LovAccount;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Vat;


@Route("expensetemplate")
public class ExpenseTemplateTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ExpenseTemplateTabView.class);

	/**
	 *
	 */
	public ExpenseTemplateTabView()
	{
		super();
		this.initUI();
		// State
		this.comboBoxState.setItems(LovState.State.values());
		this.comboBoxUnit.setItems(LovState.ExpUnit.values());

		this.comboBoxGeneric.setItems(LovState.ExpType.values());

		this.sortList();
		
		this.setDefaultFilter();
		this.binder.setBean(this.getNewDaoWithDefaults());
		this.setROFields();
		this.postLoadAccountAction(this.binder.getBean());
	}

	private void postLoadAccountAction(final ExpenseTemplate bean)
	{
		if(bean.getExtAccount() == null)
		{
			return;
		}

		// final boolean exist = this.comboBoxAccount.containsId(lov);
		// funktioniert auf keine Weise....

		final Collection<LovAccount> col1 =
			this.comboBoxAccount.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
		for(final Iterator<LovAccount> iterator = col1.iterator(); iterator.hasNext();)
		{
			final LovAccount wBean = iterator.next();
			if(wBean.getId().equals(bean.getExtAccount()))
			{
				this.comboBoxAccount.setValue(wBean);
				break;
			}
		}

	}

	private ExpenseTemplate getNewDaoWithDefaults()
	{
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if(bean == null)
		{
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		
		final ExpenseTemplate dao = new ExpenseTemplate();
		dao.setExtState(LovState.State.active);
		dao.setCostAccount(bean);
		
		return dao;
	}

	private void setROFields()
	{
		this.cmbCostAccount.setEnabled(false);
		
		boolean hasData = true;
		if(this.binder.getBean() == null)
		{
			hasData = false;
		}
		this.setROComponents(hasData);
		
	}

	private void setROComponents(final boolean state)
	{
		this.cmdSave.setEnabled(state);
		this.cmdReset.setEnabled(state);
		// this.formLayout.setEnabled(state);
	}

	private void setDefaultFilter()
	{
		final CostAccount bean = Seicento.getLoggedInCostAccount();
		
		final FilterEntry pe =
			new FilterEntry("extState", new FilterOperator.Is().key(), new LovState.State[]{LovState.State.active});
		final FilterEntry ce =
			new FilterEntry("costAccount", new FilterOperator.Is().key(), new CostAccount[]{bean});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{ce, pe}));

	}

	private void sortList()
	{
		final GridSortOrder<ExpenseTemplate> sortCol1 =
			new GridSortOrder<>(this.grid.getColumnByKey("extKeyNumber"), SortDirection.ASCENDING);
		final GridSortOrder<ExpenseTemplate> sortCol2 =
			new GridSortOrder<>(this.grid.getColumnByKey("extId"), SortDirection.DESCENDING);
		this.grid.sort(Arrays.asList(sortCol1, sortCol2));
		
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<ExpenseTemplate> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			final ExpenseTemplateDAO projectLineDao  = new ExpenseTemplateDAO();
			final ExpenseTemplate    projectLineBean =
				projectLineDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getExtId());
			this.binder.setBean(projectLineBean);
			this.postLoadAccountAction(this.binder.getBean());
			this.setROFields();
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
		final ExpenseTemplate bean = new ExpenseTemplate();

		bean.setExtState(LovState.State.active);
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
		this.reloadMainTable();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		
		this.preSaveAccountAction();
		// if(!this.binder.isValid())
		// {
		// return;
		// }
		if(SeicentoCrud.doSave(this.binder, new ExpenseTemplateDAO()))
		{
			try
			{
				this.cmdReload_onClick(null);
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getExtId(),
					this.binder.getBean().getClass().getSimpleName());
				// Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
				ExpenseTemplateTabView.LOG.debug("Record saved ExpenseTemplate");
				this.reloadMainTable();
				this.setROFields();
			}
			catch(final Exception e)
			{
				ExpenseTemplateTabView.LOG.error("could not save ObjRoot", e);
			}
		}
		this.setROFields();
		
		this.cmdReload_onClick(null);

	}
	
	private void reloadMainTable()
	{
		// save filter
		final FilterData fd = this.containerFilterComponent.getValue();
		this.containerFilterComponent.setValue(null);
		
		// clear+reload List
		this.grid.setDataProvider(DataProvider.ofCollection(new ExpenseTemplateDAO().findAll()));
		this.grid.getDataProvider().refreshAll();
		
		this.sortList();
		
		// reassign filter
		this.containerFilterComponent.setValue(fd);
		
	}

	private void preSaveAccountAction()
	{
		final LovAccount lov = this.comboBoxAccount.getValue();
		if(lov != null)
		{
			this.binder.getBean().setExtAccount(lov.getId());
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
			final ExpenseTemplateDAO projectLineDao  = new ExpenseTemplateDAO();
			final ExpenseTemplate    projectLineBean = projectLineDao.find(this.binder.getBean().getExtId());
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
			final ExpenseTemplate bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog          win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getExtId(), bean.getClass().getSimpleName()));
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

				final ExpenseTemplate bean = this.grid.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getExtId(), bean.getClass().getSimpleName());

				final ExpenseTemplateDAO dao = new ExpenseTemplateDAO();
				dao.remove(bean);
				dao.flush();

				this.binder.removeBean();
				ExpenseTemplateTabView.this.binder.setBean(new ExpenseTemplate());
				this.grid.setDataProvider(DataProvider.ofCollection(new ExpenseTemplateDAO().findAll()));
				ExpenseTemplateTabView.this.grid.getDataProvider().refreshAll();

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
				ExpenseTemplateTabView.LOG.error("Error on delete", e);
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
		this.grid                     = new Grid<>(ExpenseTemplate.class, false);
		this.formLayout               = new FormLayout();
		this.formItem2                = new FormItem();
		this.lblCostAccount           = new Label();
		this.cmbCostAccount           = new ComboBox<>();
		this.formItem                 = new FormItem();
		this.lblPrtKeyNumber          = new Label();
		this.txtPrtKeyNumber          = new TextField();
		this.formItem12               = new FormItem();
		this.lblPrtText               = new Label();
		this.txtPrtText               = new TextField();
		this.formItem4                = new FormItem();
		this.lblExtAmount             = new Label();
		this.txtExtAmount             = new TextField();
		this.formItem5                = new FormItem();
		this.lblExtVat                = new Label();
		this.comboBoxVat              = new ComboBox<>();
		this.formItem7                = new FormItem();
		this.lblExtAccount            = new Label();
		this.comboBoxAccount          = new ComboBox<>();
		this.formItem11               = new FormItem();
		this.lblExtGeneral            = new Label();
		this.comboBoxGeneric          = new ComboBox<>();
		this.formItem10               = new FormItem();
		this.lblExtCostAccount        = new Label();
		this.checkbox                 = new Checkbox();
		this.formItem8                = new FormItem();
		this.lblExtUnit               = new Label();
		this.comboBoxUnit             = new ComboBox<>();
		this.formItem3                = new FormItem();
		this.lblPrtProject            = new Label();
		this.comboBoxProject          = new ComboBox<>();
		this.formItem9                = new FormItem();
		this.lblExtQuantity           = new Label();
		this.txtExtQuantity           = new TextField();
		this.formItem6                = new FormItem();
		this.lblPrtState              = new Label();
		this.comboBoxState            = new ComboBox<>();
		this.formItem13               = new FormItem();
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdSave                  = new Button();
		this.cmdReset                 = new Button();
		this.binder                   = new BeanValidationBinder<>(ExpenseTemplate.class);

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setTabIndex(1);
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setTabIndex(2);
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdReload.setTabIndex(3);
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setTabIndex(4);
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.setTabIndex(5);
		this.grid.addColumn(ExpenseTemplate::getExtKeyNumber).setKey("extKeyNumber").setHeader("Nummer")
			.setSortable(true);
		this.grid.addColumn(ExpenseTemplate::getExtAccount).setKey("extAccount").setHeader("Konto").setSortable(true);
		this.grid.addColumn(ExpenseTemplate::getExtText).setKey("extText").setHeader("Text").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(ExpenseTemplate::getExtState)).setKey("extState").setHeader("Status")
			.setSortable(true);
		this.grid.addColumn(ExpenseTemplate::getExtId).setKey("extId")
			.setHeader(CaptionUtils.resolveCaption(ExpenseTemplate.class, "extId")).setSortable(true).setVisible(false);
		this.grid.setDataProvider(DataProvider.ofCollection(new ExpenseTemplateDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.formLayout.getStyle().set("margin-left", "10px");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("25em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("32em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("40em", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.lblCostAccount.setMaxHeight("");
		this.lblCostAccount.setText("Mitarbeiter");
		this.cmbCostAccount.setTabIndex(6);
		this.cmbCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.cmbCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.cmbCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.lblPrtKeyNumber.setText("Nummer");
		this.txtPrtKeyNumber.setTabIndex(7);
		this.txtPrtKeyNumber.setPlaceholder("");
		this.lblPrtText.setText("Text");
		this.lblExtAmount.setText("Betrag");
		this.txtExtAmount.setTabIndex(8);
		this.lblExtVat.setText("MwSt");
		this.comboBoxVat.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.comboBoxVat::getItemLabelGenerator),
			DataProvider.ofCollection(new VatDAO().findAllInclusive()));
		this.comboBoxVat.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Vat::getFullName));
		this.lblExtAccount.setText("Konto");
		this.comboBoxAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new LovAccountDAO().findAllMine()));
		this.comboBoxAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(LovAccount::getName));
		this.lblExtGeneral.setText("Pauschal");
		this.comboBoxGeneric.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblExtCostAccount.setText(" ");
		this.checkbox.setTabIndex(11);
		this.checkbox.setLabel("Kostenstelle");
		this.lblExtUnit.setText("Einheit");
		this.comboBoxUnit.setTabIndex(12);
		this.comboBoxUnit.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.lblPrtProject.setText("Projekt");
		this.comboBoxProject.setTabIndex(13);
		this.comboBoxProject.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxProject::getItemLabelGenerator),
			DataProvider.ofCollection(new ProjectDAO().findAll()));
		this.comboBoxProject.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Project::getProName));
		this.lblExtQuantity.setText("Menge");
		this.txtExtQuantity.setTabIndex(14);
		this.lblPrtState.setText("Status");
		this.comboBoxState.setTabIndex(15);
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.formItem13.getElement().setAttribute("colspan", "4");
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Abbrechen");
		this.cmdReset.setIcon(IronIcons.UNDO.create());

		this.binder.forField(this.cmbCostAccount).bind("costAccount");
		this.binder.forField(this.txtPrtKeyNumber).asRequired().withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("extKeyNumber");
		this.binder.forField(this.comboBoxProject).asRequired().bind("project");
		this.binder.forField(this.txtPrtText).withNullRepresentation("").bind("extText");
		this.binder.forField(this.txtExtAmount).asRequired().withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("extAmount");
		this.binder.forField(this.comboBoxVat).bind("vat");
		this.binder.forField(this.comboBoxGeneric).asRequired().bind("extFlagGeneric");
		this.binder.forField(this.checkbox).withNullRepresentation(false).bind("extFlagCostAccount");
		this.binder.forField(this.comboBoxUnit).bind("extUnit");
		this.binder.forField(this.txtExtQuantity).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToDouble().numberFormatBuilder(NumberFormatBuilder.Decimal()).build())
			.bind("extQuantity");
		this.binder.forField(this.comboBoxState).bind("extState");

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("costAccount.csaCode", "costAccount.csaName"),
			Arrays.asList("costAccount", "extState", "project")));

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
		this.lblPrtText.setSizeUndefined();
		this.lblPrtText.getElement().setAttribute("slot", "label");
		this.txtPrtText.setWidthFull();
		this.txtPrtText.setHeight(null);
		this.formItem12.add(this.lblPrtText, this.txtPrtText);
		this.lblExtAmount.setSizeUndefined();
		this.lblExtAmount.getElement().setAttribute("slot", "label");
		this.txtExtAmount.setWidthFull();
		this.txtExtAmount.setHeight(null);
		this.formItem4.add(this.lblExtAmount, this.txtExtAmount);
		this.lblExtVat.setSizeUndefined();
		this.lblExtVat.getElement().setAttribute("slot", "label");
		this.comboBoxVat.setSizeUndefined();
		this.formItem5.add(this.lblExtVat, this.comboBoxVat);
		this.lblExtAccount.setSizeUndefined();
		this.lblExtAccount.getElement().setAttribute("slot", "label");
		this.comboBoxAccount.setSizeUndefined();
		this.formItem7.add(this.lblExtAccount, this.comboBoxAccount);
		this.lblExtGeneral.setSizeUndefined();
		this.lblExtGeneral.getElement().setAttribute("slot", "label");
		this.comboBoxGeneric.setSizeUndefined();
		this.formItem11.add(this.lblExtGeneral, this.comboBoxGeneric);
		this.lblExtCostAccount.setSizeUndefined();
		this.lblExtCostAccount.getElement().setAttribute("slot", "label");
		this.checkbox.setWidthFull();
		this.checkbox.setHeight(null);
		this.formItem10.add(this.lblExtCostAccount, this.checkbox);
		this.lblExtUnit.setSizeUndefined();
		this.lblExtUnit.getElement().setAttribute("slot", "label");
		this.comboBoxUnit.setSizeUndefined();
		this.formItem8.add(this.lblExtUnit, this.comboBoxUnit);
		this.lblPrtProject.setSizeUndefined();
		this.lblPrtProject.getElement().setAttribute("slot", "label");
		this.comboBoxProject.setSizeUndefined();
		this.formItem3.add(this.lblPrtProject, this.comboBoxProject);
		this.lblExtQuantity.setSizeUndefined();
		this.lblExtQuantity.getElement().setAttribute("slot", "label");
		this.txtExtQuantity.setWidthFull();
		this.txtExtQuantity.setHeight(null);
		this.formItem9.add(this.lblExtQuantity, this.txtExtQuantity);
		this.lblPrtState.setSizeUndefined();
		this.lblPrtState.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidthFull();
		this.comboBoxState.setHeight(null);
		this.formItem6.add(this.lblPrtState, this.comboBoxState);
		this.cmdSave.setSizeUndefined();
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight(null);
		this.formItem13.add(this.horizontalLayout3);
		this.formLayout.add(this.formItem2, this.formItem, this.formItem12, this.formItem4, this.formItem5,
			this.formItem7,
			this.formItem11, this.formItem10, this.formItem8, this.formItem3, this.formItem9, this.formItem6,
			this.formItem13);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.formLayout);
		this.splitLayout.setSplitterPosition(50.0);
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
	private ComboBox<ExpUnit>                     comboBoxUnit;
	private ComboBox<Vat>                         comboBoxVat;
	private BeanValidationBinder<ExpenseTemplate> binder;
	private VerticalLayout                        verticalLayout;
	private HorizontalLayout                      horizontalLayout2, horizontalLayout3;
	private Label                                 lblCostAccount, lblPrtKeyNumber, lblPrtText, lblExtAmount, lblExtVat,
		lblExtAccount, lblExtGeneral, lblExtCostAccount, lblExtUnit, lblPrtProject, lblExtQuantity, lblPrtState;
	private FilterComponent                       containerFilterComponent;
	private Grid<ExpenseTemplate>                 grid;
	private FormItem                              formItem2, formItem, formItem12, formItem4, formItem5, formItem7,
		formItem11, formItem10, formItem8, formItem3, formItem9, formItem6, formItem13;
	private FormLayout                            formLayout;
	private Checkbox                              checkbox;
	private Button                                cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private ComboBox<LovAccount>                  comboBoxAccount;
	private ComboBox<ExpType>                     comboBoxGeneric;
	private ComboBox<State>                       comboBoxState;
	private SplitLayout                           splitLayout;
	private ComboBox<Project>                     comboBoxProject;
	private TextField                             txtPrtKeyNumber, txtPrtText, txtExtAmount, txtExtQuantity;
	private ComboBox<CostAccount>                 cmbCostAccount;
	// </generated-code>
	
}
