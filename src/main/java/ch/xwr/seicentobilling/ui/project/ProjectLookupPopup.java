
package ch.xwr.seicentobilling.ui.project;

import java.util.Arrays;

import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Project;


public class ProjectLookupPopup extends VerticalLayout
{

	private Project _proBean;
	
	/**
	 *
	 */
	public ProjectLookupPopup()
	{
		super();
		this.initUI();
		
		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));

		// State
		// this.comboBoxWorktype.addItems((Object[])LovState.WorkType.values());

		this.setDefaultFilter();
		// this.table.focus();
		// this.containerFilterComponent.getSearchTextField().focus();
	}
	
	private void setDefaultFilter()
	{
		final CostAccount bean = Seicento.getLoggedInCostAccount();
		
		final FilterEntry ce =
			new FilterEntry("costAccount", new FilterOperator.Is().key(), new CostAccount[]{bean});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{ce}));
	}

	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		win.setSizeFull();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new ProjectLookupPopup());
		return win;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #btnSelect}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnSelect_onClick(final ClickEvent<Button> event)
	{

		if(this.grid.getDataProvider().size(new Query<>()) == 1)
		{
			this.grid.select(this.grid.getSelectionModel().getFirstSelectedItem().get());
		}
		
		if(this.grid.getSelectedItems() != null)
		{
			final Project beanId = this.grid.getSelectionModel().getFirstSelectedItem().get();
			UI.getCurrent().getSession().setAttribute("beanId", beanId.getProId());
			
			((Dialog)this.getParent().get()).close();
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #btnCancel}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void btnCancel_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Project> event)
	{
		if(this.grid.getSelectedItems() != null)
		{
			this._proBean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			UI.getCurrent().getSession().setAttribute("beanId", this._proBean.getProId());
			
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemDoubleClick(final ItemDoubleClickEvent<Project> event)
	{
		UI.getCurrent().getSession().setAttribute("beanId", this._proBean.getProId());
		
		((Dialog)this.getParent().get()).close();
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout           = new VerticalLayout();
		this.horizontalLayout         = new HorizontalLayout();
		this.label                    = new Label();
		this.containerFilterComponent = new FilterComponent();
		this.grid                     = new Grid<>(Project.class, false);
		this.horizontalLayout2        = new HorizontalLayout();
		this.btnSelect                = new Button();
		this.btnCancel                = new Button();

		this.label.setText("Lookup Projekt");
		this.grid.addColumn(Project::getProName).setKey("proName").setHeader("Name").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Project::getCostAccount)).setKey("costAccount")
			.setHeader("Kostenstelle")
			.setSortable(false);
		this.grid.addColumn(Project::getProStartDate).setKey("proStartDate").setHeader("Startdatum").setSortable(true);
		this.grid.addColumn(Project::getProRate).setKey("proRate").setHeader("Ansatz").setSortable(true);
		this.grid.addColumn(Project::getProHours).setKey("proHours").setHeader("Stunden Soll").setSortable(true);
		this.grid.addColumn(Project::getProIntensityPercent).setKey("proIntensityPercent").setHeader("Auslastung")
			.setSortable(true);
		this.grid.setDataProvider(DataProvider.ofCollection(new ProjectDAO().findAllActive()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.btnSelect.setText("Übernehmen");
		this.btnCancel.setText("Schliessen");

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("proExtReference", "proName"),
			Arrays.asList("costAccount", "customer", "proName", "proState")));

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.label);
		this.btnSelect.setSizeUndefined();
		this.btnCancel.setSizeUndefined();
		this.horizontalLayout2.add(this.btnSelect, this.btnCancel);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.grid.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("10%");
		this.verticalLayout.add(this.horizontalLayout, this.containerFilterComponent, this.grid,
			this.horizontalLayout2);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();

		this.grid.addItemClickListener(this::grid_onItemClick);
		this.grid.addItemDoubleClickListener(this::grid_onItemDoubleClick);
		this.btnSelect.addClickListener(this::btnSelect_onClick);
		this.btnCancel.addClickListener(this::btnCancel_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Button           btnSelect, btnCancel;
	private VerticalLayout   verticalLayout;
	private HorizontalLayout horizontalLayout, horizontalLayout2;
	private Label            label;
	private FilterComponent  containerFilterComponent;
	private Grid<Project>    grid;
	// </generated-code>
	
}