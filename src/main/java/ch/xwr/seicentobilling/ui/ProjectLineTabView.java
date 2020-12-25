
package ch.xwr.seicentobilling.ui;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.ImageIcons;
import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.format.DateFormatBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.data.renderer.DateRenderer;
import com.rapidclipse.framework.server.reports.grid.GridExportDialog;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;


@Route("projectline")
public class ProjectLineTabView extends VerticalLayout
{
	/** Logger initialized */

	private static final Logger LOG            = LoggerFactory.getLogger(ProjectLineTabView.class);
	private Periode             currentPeriode = null;
	
	private boolean                                 isAdmin = false;
	private ProjectLineOverviewItemData             pld;
	private final TreeGrid<ProjectLineOverviewItem> treeGrid, treeGrid2;

	/**
	 *
	 */
	public ProjectLineTabView()
	{
		super();
		this.initUI();

		this.sortList();
		// set RO Fields
		this.setROFields();
		
		this.setDefaultFilter();
		if(Seicento.hasRole("BillingAdmin"))
		{
			this.cmdAdmin.setEnabled(true);
			this.cmdAdmin.setVisible(true);
		}

		this.gridLayoutOverview.setVisible(false);
		
		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.tab, this.verticalLayoutReports);
		tabsToPages.put(this.tab2, this.gridLayoutOverview);

		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});
		this.tabs.setSelectedTab(this.tab);

		this.treeGrid  = new TreeGrid<>();
		this.treeGrid2 = new TreeGrid<>();
		this.treeGrid1VerticalLayout.add(this.treeGrid);
		this.treeGrid2VerticalLayout.add(this.treeGrid2);
	}

	private void setROFields()
	{
		final boolean roFlag = this.isBooked();

		this.cmdNewLine.setEnabled(!roFlag);
		this.cmdUpdateLine.setEnabled(!roFlag);
		this.cmdDeleteLine.setEnabled(!roFlag);
		// this.cmdCopySingle.setEnabled(!roFlag);
		this.cmdUpdate.setEnabled(!roFlag);
		this.cmdDelete.setEnabled(!roFlag);
		this.cmdExcel.setEnabled(!roFlag);
		this.cmdCopyLine.setEnabled(!roFlag);

	}

	private void setDefaultFilter()
	{
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if(bean == null)
		{
			bean = new CostAccountDAO().findAll().get(0); // Dev Mode
		}
		
		final Calendar cal   = Calendar.getInstance();
		final int      iyear = cal.get(Calendar.YEAR);
		
		final FilterEntry ie =
			new FilterEntry("perYear", new FilterOperator.Is().key(), new Integer[]{iyear});
		final FilterEntry ce =
			new FilterEntry("costAccount", new FilterOperator.Is().key(), new CostAccount[]{bean});
		this.containerFilterComponent.setValue(new FilterData("", new FilterEntry[]{ie, ce}));
		
	}
	
	private void sortList()
	{
		final GridSortOrder<ProjectLine> sortCol1 =
			new GridSortOrder<>(this.tableLine.getColumnByKey("prlReportDate"), SortDirection.ASCENDING);
		final GridSortOrder<ProjectLine> sortCol2 =
			new GridSortOrder<>(this.tableLine.getColumnByKey("project"), SortDirection.DESCENDING);
		this.tableLine.sort(Arrays.asList(sortCol1, sortCol2));
		//
		// if(!all)
		// {
		// return;
		// }
		final GridSortOrder<Periode> sortCol11 =
			new GridSortOrder<>(this.grid.getColumnByKey("perYear"), SortDirection.DESCENDING);
		final GridSortOrder<Periode> sortCol21 =
			new GridSortOrder<>(this.grid.getColumnByKey("perMonth"), SortDirection.ASCENDING);
		// final GridSortOrder<Periode> sortCol31 =
		// new GridSortOrder<>(this.grid.getColumnByKey("costaccount"), SortDirection.ASCENDING);
		this.grid.sort(Arrays.asList(sortCol11, sortCol21));
	}
	
	private boolean isBooked()
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return false;
		}
		if(this.isAdmin)
		{
			return false;
		}
		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(LovState.BookingType.gebucht.equals(bean.getPerBookedExpense()))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<Periode> event)
	{
		if(this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			final PeriodeDAO cityDao      = new PeriodeDAO();
			final Periode    selectedBean =
				cityDao.find(this.grid.getSelectionModel().getFirstSelectedItem().get().getPerId());
			this.currentPeriode = selectedBean;
			this.calcOverview();
		}
		else
		{
			this.grid.getSelectionModel().select(this.currentPeriode);
		}
		
		this.reloadTableLineList();
		this.setROFields();
	}

	private void calcOverview()
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		final Periode per = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(per == null)
		{
			return;
		}

		final Calendar  cal             = Calendar.getInstance();
		final YearMonth yearMonthObject = YearMonth.of(per.getPerYear(), per.getPerMonth().getValue());
		final int       daysInMonth     = yearMonthObject.lengthOfMonth();

		final double[] hours  = new double[32];
		double         totalm = 0.;

		final List<ProjectLine> ls = new ProjectLineDAO().findByPeriode(per);
		for(final ProjectLine pln : ls)
		{
			if(!pln.getPrlWorkType().equals(LovState.WorkType.expense))
			{
				// do not add expenses
				cal.setTime(pln.getPrlReportDate());
				final int iday = cal.get(Calendar.DAY_OF_MONTH);

				hours[iday] = hours[iday] + pln.getPrlHours();
				totalm      = totalm + pln.getPrlHours();
			}
		}

		this.initOverviewGrids(totalm);
		
		this.pld = new ProjectLineOverviewItemData(cal, daysInMonth, per, hours);

		// this.treeGrid.addHierarchyColumn(OrderGenTreeItem::getCbo)
		// .setHeader("");
		this.treeGrid.removeAllColumns();
		this.treeGrid2.removeAllColumns();

		this.treeGrid.setItems(this.pld.getGrid1RootItems(),
			this.pld::getChildItems);

		this.treeGrid2.setItems(this.pld.getGrid2RootItems(),
			this.pld::getChildItems);
		
		this.treeGrid
			.addComponentHierarchyColumn(
				item -> this.createLabelDatum(this.treeGrid, item))
			.setHeader("Datum").setResizable(true).setWidth("175px");
		// this.treeGrid.addColumn(ProjectLineOverviewItem::getStunden)
		// .setHeader("Stunden").setResizable(true);
		this.treeGrid
			.addComponentColumn(
				item -> this.createLabelStunden(this.treeGrid, item))
			.setHeader("Stunden").setResizable(true);

		this.treeGrid2
			.addComponentHierarchyColumn(
				item -> this.createLabelDatum(this.treeGrid2, item))
			.setHeader("Datum").setResizable(true).setWidth("175px");
		
		// this.treeGrid2.addColumn(ProjectLineOverviewItem::getStunden)
		// .setHeader("Stunden").setResizable(true);
		this.treeGrid2
			.addComponentColumn(
				item -> this.createLabelStunden(this.treeGrid2, item))
			.setHeader("Stunden").setResizable(true);
		
		this.treeGrid.getDataProvider().refreshAll();
		this.treeGrid2.getDataProvider().refreshAll();
		
	}

	public Label createLabelDatum(final TreeGrid<ProjectLineOverviewItem> grid, final ProjectLineOverviewItem bean)
	{
		final Label obj = new Label();
		obj.setText(bean.getDatum());
		if(!bean.getDatumStyle().isEmpty())
		{
			obj.getStyle().set("fontWeight", "bold");
			obj.getStyle().set("color", "#07a9ca");
		}
		return obj;
	}

	public Label createLabelStunden(final TreeGrid<ProjectLineOverviewItem> grid, final ProjectLineOverviewItem bean)
	{
		final Label obj = new Label();
		obj.setText(bean.getStunden());
		
		if(!bean.getStundenStyle().isEmpty())
		{
			obj.getStyle().set("border-radius", "10px");
			obj.getStyle().set("border-radius", "10px");
			obj.getStyle().set("padding", "1px 14px 1px 14px");
			if(bean.getStundenStyle().equals("success"))
			{
				obj.getStyle().set("border", "2px solid #2c9720");
				obj.getStyle().set("color", "#2c9720");
			}
			else
			{
				obj.getStyle().set("border", "2px solid #ed473b");
				obj.getStyle().set("color", "#ed473b");
			}
		}
		return obj;
	}
	
	private void initOverviewGrids(final double totalm)
	{
		this.lblTotalMonth.setText("");
		
		final Periode  per = this.grid.getSelectionModel().getFirstSelectedItem().get();
		final Calendar cal = Calendar.getInstance();
		cal.set(per.getPerYear(), per.getPerMonth().getValue() - 1, 1);
		final SimpleDateFormat month_date = new SimpleDateFormat("MMMM yyyy");
		final String           month_name = month_date.format(cal.getTime());
		this.lblTotalMonth.setText(" " + month_name + "    Stunden: " + totalm);
		
		/*
		 * // reset
		 * this.treeGrid.removeAllItems();
		 * this.treeGrid.removeContainerProperty("Datum");
		 * this.treeGrid.removeContainerProperty("Stunden");
		 *
		 * // rebuild
		 * this.treeGrid.addContainerProperty("Datum", XdevLabel.class, null);
		 * this.treeGrid.addContainerProperty("Stunden", XdevLabel.class, null);
		 *
		 * this.treeGrid2.removeAllItems();
		 * this.treeGrid2.removeContainerProperty("Datum");
		 * this.treeGrid2.removeContainerProperty("Stunden");
		 *
		 * // rebuild
		 * this.treeGrid2.addContainerProperty("Datum", XdevLabel.class, null);
		 * this.treeGrid2.addContainerProperty("Stunden", XdevLabel.class, null);
		 */
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onClick(final ClickEvent<Button> event)
	{
		UI.getCurrent().getSession().setAttribute("beanId", null);
		UI.getCurrent().getSession().setAttribute("reason", "new");
		UI.getCurrent().getSession().setAttribute("source", "expense");
		UI.getCurrent().getSession().setAttribute("isAdmin", false);
		this.popupPeriode();

	}

	private void popupPeriode()
	{
		final Dialog win = PeriodePopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				
				if(retval == null)
				{
					retval = "cmdCancel";
				}
				else
				{
					final Long    beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
					final Periode bean   = new PeriodeDAO().find(beanId);
					
					ProjectLineTabView.this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
					ProjectLineTabView.this.reloadMainTable();
					ProjectLineTabView.this.grid.select(bean);
					ProjectLineTabView.this.grid.getDataProvider().refreshAll();
				}

			}
		});
		win.open();
	}

	private void reloadMainTable()
	{
		// save filter
		final FilterData fd = this.containerFilterComponent.getValue();
		this.containerFilterComponent.setValue(null);
		
		// clear+reload List
		DataProvider.ofCollection(new PeriodeDAO().findAll());
		this.grid.getDataProvider().refreshAll();
		
		this.sortList();
		
		// reassign filter
		this.containerFilterComponent.setValue(fd);
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_onClick(final ClickEvent<Button> event)
	{
		this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.reloadMainTable();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNewLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewLine_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		if(this.grid.getSelectedItems() == null)
		{
			return;
		}
		final Long objId = this.grid.getSelectionModel().getFirstSelectedItem().get().getPerId();
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupProjectLine();
	}
	
	private void popupProjectLine()
	
	{
		final Dialog win = ProjectLinePopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{

				ProjectLineTabView.this.reloadTableLineList();
			}

		});
		win.open();
	}

	private void reloadTableLineList()
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(bean != null)
		{
			this.tableLine.setDataProvider(DataProvider.ofCollection(new ProjectLineDAO().findByPeriode(bean)));
			this.containerFilterComponent2.connectWith(this.tableLine.getDataProvider());
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteLine_onClick(final ClickEvent<Button> event)
	{
		if(this.grid.getSelectedItems() == null)
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}
		
		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			
			try
			{
				
				final ProjectLine bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrlId(), bean.getClass().getSimpleName());
				
				final ProjectLineDAO dao = new ProjectLineDAO();
				dao.remove(bean);
				dao.flush();
				
				this.tableLine
					.setDataProvider(
						DataProvider.ofCollection(new ProjectLineDAO().findByPeriode(this.currentPeriode)));
				ProjectLineTabView.this.tableLine.getDataProvider().refreshAll();
				
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
				ProjectLineTabView.LOG.error("Error on delete", e);
			}
		});
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfo}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */

	private void cmdInfo_onClick(final ClickEvent<Button> event)
	{

		if(this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
			final Dialog  win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getPerId(), bean.getClass().getSimpleName()));
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

				final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();

				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPerId(), bean.getClass().getSimpleName());

				final PeriodeDAO dao = new PeriodeDAO();
				dao.remove(bean);
				dao.flush();

				this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
				ProjectLineTabView.this.grid.getDataProvider().refreshAll();

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
				ProjectLineTabView.LOG.error("Error on delete", e);
			}
		});
		
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemDoubleClick(final ItemDoubleClickEvent<Periode> event)
	{
		if(!this.isBooked())
		{
			if(this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
			{
				this.cmdUpdate.click();
			}
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdate}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdate_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final Long beanId = this.grid.getSelectionModel().getFirstSelectedItem().get().getPerId();
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("reason", "update");
		UI.getCurrent().getSession().setAttribute("source", "expense");
		UI.getCurrent().getSession().setAttribute("isAdmin", false);
		
		this.popupPeriode();
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdateLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateLine_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableLine.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.tableLine.getSelectionModel().getFirstSelectedItem().get().getPrlId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupProjectLine();
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_onItemClick(final ItemClickEvent<ProjectLine> event)
	{
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_onItemDoubleClick(final ItemDoubleClickEvent<ProjectLine> event)
	{
		if(!this.isBooked())
		{
			final ProjectLine obj = event.getItem();
			this.tableLine.select(obj);
			final Long beanId = obj.getPrlId();
			final Long objId  = null;
			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);
			this.popupProjectLine();
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCopyLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopyLine_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableLine.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final ProjectLine bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();
		
		bean.setPrlId(new Long(0));
		bean.setPrlText(bean.getPrlText() + " (Kopie)");
		
		final ProjectLineDAO dao     = new ProjectLineDAO();
		final ProjectLine    newBean = dao.merge(bean);
		dao.save(newBean);
		
		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getPrlId(), newBean.getClass().getSimpleName());
		
		this.reloadTableLineList();
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdInfoExpense}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoExpense_onClick(final ClickEvent<Button> event)
	{
		if(this.tableLine.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			final ProjectLine bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();
			final Dialog      win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getPrlId(), bean.getClass().getSimpleName()));
			win.open();
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdAdmin}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdAdmin_onClick(final ClickEvent<Button> event)
	{
		this.isAdmin = !this.isAdmin;
		this.setROFields();
		
		if(this.isAdmin)
		{
			
			this.cmdAdmin.setIcon(IronIcons.SETTINGS_APPLICATIONS.create());
		}
		else
		{
			this.cmdAdmin.setIcon(IronIcons.SETTINGS.create());
		}

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReport}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReport_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			Notification.show("Es wurde keine Zeile selektiert in der Tabelle", 5000,
				Notification.Position.BOTTOM_END);
			return;
		}
		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();

		final JasperManager jsp = new JasperManager();
		jsp.addParameter("Param_Periode", "" + bean.getPerId());
		// jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());

		UI.getCurrent().getPage().open(jsp.getUri(JasperManager.ProjectLineReport1), "_blank");

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNew}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_onFocus(final FocusEvent<Button> event)
	{
		this.reloadMainTable();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdExcel}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdExcel_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			SeicentoNotification.showWarn("Excel importieren", "Keine Periode gewählt");
			return;
		}
		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();

		UI.getCurrent().getSession().setAttribute("periodebean", bean);

		this.popupExcelUpload();
	}
	
	private void popupExcelUpload()
	{
		final Dialog win = ExcelUploadPopup.getPopupWindow();
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				ProjectLineTabView.this.reloadTableLineList();
			}
		});
		win.open();

	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdRefOverview}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdRefOverview_onClick(final ClickEvent<Button> event)
	{
		this.calcOverview();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdExport}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdExport_onClick(final ClickEvent<Button> event)
	{
		GridExportDialog.open(this.tableLine);
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout               = new SplitLayout();
		this.verticalLayout            = new VerticalLayout();
		this.containerFilterComponent  = new FilterComponent();
		this.horizontalLayout2         = new HorizontalLayout();
		this.cmdNew                    = new Button();
		this.cmdDelete                 = new Button();
		this.cmdUpdate                 = new Button();
		this.cmdReload                 = new Button();
		this.cmdReport                 = new Button();
		this.cmdInfo                   = new Button();
		this.cmdAdmin                  = new Button();
		this.grid                      = new Grid<>(Periode.class, false);
		this.verticalLayout2           = new VerticalLayout();
		this.tabs                      = new Tabs();
		this.tab                       = new Tab();
		this.tab2                      = new Tab();
		this.verticalLayoutReports     = new VerticalLayout();
		this.containerFilterComponent2 = new FilterComponent();
		this.horizontalLayout3         = new HorizontalLayout();
		this.cmdNewLine                = new Button();
		this.cmdDeleteLine             = new Button();
		this.cmdUpdateLine             = new Button();
		this.cmdExcel                  = new Button();
		this.cmdCopyLine               = new Button();
		this.cmdExport                 = new Button();
		this.cmdInfoExpense            = new Button();
		this.tableLine                 = new Grid<>(ProjectLine.class, false);
		this.gridLayoutOverview        = new VerticalLayout();
		this.horizontalLayout          = new HorizontalLayout();
		this.treeGrid1VerticalLayout   = new VerticalLayout();
		this.lblTotalMonth             = new Label();
		this.treeGrid2VerticalLayout   = new VerticalLayout();
		this.cmdRefOverview            = new Button();

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdate.setIcon(ImageIcons.EDIT.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdReport.setIcon(IronIcons.PRINT.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.cmdAdmin.setIcon(IronIcons.SETTINGS.create());
		this.grid.addColumn(Periode::getPerName).setKey("perName").setHeader("Periode").setSortable(true);
		this.grid.addColumn(Periode::getPerYear).setKey("perYear").setHeader("Jahr").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerMonth)).setKey("perMonth").setHeader("Monat")
			.setSortable(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerBookedExpense)).setKey("perBookedExpense")
			.setHeader("Buchhaltung").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerBookedProject)).setKey("perBookedProject")
			.setHeader("Gebucht Projekt").setSortable(false).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerState)).setKey("perState").setHeader("Status")
			.setSortable(true).setVisible(false);
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(Periode::getCostAccount).map(CostAccount::getCsaName).orElse(null))
			.setKey("costAccount.csaName").setHeader("Kostenstelle").setSortable(true).setVisible(false);
		this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setMaxWidth("");
		this.verticalLayout2.setPadding(false);
		this.tab.setLabel("Rapportzeilen");
		this.tab2.setLabel("Übersicht");
		this.verticalLayoutReports.setSpacing(false);
		this.verticalLayoutReports.setPadding(false);
		this.cmdNewLine.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteLine.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdateLine.setIcon(VaadinIcon.PENCIL.create());
		this.cmdExcel.setIcon(IronIcons.FILE_UPLOAD.create());
		this.cmdCopyLine.setIcon(VaadinIcon.COPY.create());
		this.cmdExport.setIcon(VaadinIcon.EXTERNAL_LINK.create());
		this.cmdInfoExpense.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.tableLine
			.addColumn(
				new DateRenderer<>(ProjectLine::getPrlReportDate, DateFormatBuilder.Simple("MMM dd yyyy").build(), ""))
			.setKey("prlReportDate").setHeader("Datum").setResizable(true).setSortable(true);
		this.tableLine.addColumn(ProjectLine::getPrlTimeFrom).setKey("prlTimeFrom").setHeader("TimeFrom")
			.setResizable(true)
			.setSortable(true).setVisible(false);
		this.tableLine.addColumn(ProjectLine::getPrlTimeTo).setKey("prlTimeTo").setHeader("TimeTo").setResizable(true)
			.setSortable(true).setVisible(false);
		this.tableLine.addColumn(ProjectLine::getPrlText).setKey("prlText").setHeader("Text").setResizable(true)
			.setSortable(true);
		this.tableLine.addColumn(ProjectLine::getPrlHours).setKey("prlHours").setHeader("Stunden").setResizable(true)
			.setSortable(true);
		this.tableLine
			.addColumn(new NumberRenderer<>(ProjectLine::getPrlRate,
				NumberFormatBuilder.Currency().currency(Currency.getInstance("CHF")).build(), ""))
			.setKey("prlRate").setHeader("Ansatz").setResizable(true).setSortable(true);
		this.tableLine.addColumn(new CaptionRenderer<>(ProjectLine::getPrlWorkType)).setKey("prlWorkType")
			.setHeader("Typ")
			.setResizable(true).setSortable(true);
		this.tableLine
			.addColumn(v -> Optional.ofNullable(v).map(ProjectLine::getProject).map(Project::getProName).orElse(null))
			.setKey("project.proName").setHeader("Projektname").setResizable(true).setSortable(true);
		this.tableLine.addColumn(new CaptionRenderer<>(ProjectLine::getPrlState)).setKey("prlState").setHeader("Status")
			.setResizable(true).setSortable(true);
		this.tableLine.addColumn(new CaptionRenderer<>(ProjectLine::getProject)).setKey("project").setHeader("Projekt")
			.setResizable(true).setSortable(false).setVisible(false);
		this.tableLine.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.gridLayoutOverview.setSpacing(false);
		this.gridLayoutOverview.setPadding(false);
		this.treeGrid1VerticalLayout.setPadding(false);
		this.lblTotalMonth.setText("Label");
		this.treeGrid2VerticalLayout.setPadding(false);
		this.cmdRefOverview.setIcon(IronIcons.REFRESH.create());

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("costAccount.csaCode", "costAccount.csaName", "perName"),
			Arrays.asList("costAccount", "perBookedExpense", "perBookedProject", "perMonth", "perState", "perYear")));
		this.containerFilterComponent2.connectWith(this.tableLine.getDataProvider());
		this.containerFilterComponent2.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.tableLine,
			Arrays.asList("prlText", "project.proExtReference", "project.proName"), Arrays.asList("prlHours", "prlRate",
				"prlReportDate", "prlState", "prlText", "prlWorkType", "project", "project.proName")));

		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdUpdate.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdReport.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.cmdAdmin.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdUpdate, this.cmdReload, this.cmdReport,
			this.cmdInfo, this.cmdAdmin);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.tabs.add(this.tab, this.tab2);
		this.cmdNewLine.setSizeUndefined();
		this.cmdDeleteLine.setSizeUndefined();
		this.cmdUpdateLine.setSizeUndefined();
		this.cmdExcel.setSizeUndefined();
		this.cmdCopyLine.setSizeUndefined();
		this.cmdExport.setSizeUndefined();
		this.cmdInfoExpense.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdNewLine, this.cmdDeleteLine, this.cmdUpdateLine, this.cmdExcel,
			this.cmdCopyLine,
			this.cmdExport, this.cmdInfoExpense);
		this.containerFilterComponent2.setWidthFull();
		this.containerFilterComponent2.setHeight(null);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight(null);
		this.tableLine.setSizeFull();
		this.verticalLayoutReports.add(this.containerFilterComponent2, this.horizontalLayout3, this.tableLine);
		this.verticalLayoutReports.setFlexGrow(1.0, this.tableLine);
		this.lblTotalMonth.setSizeUndefined();
		this.treeGrid1VerticalLayout.add(this.lblTotalMonth);
		this.cmdRefOverview.setSizeUndefined();
		this.treeGrid2VerticalLayout.add(this.cmdRefOverview);
		this.treeGrid1VerticalLayout.setSizeFull();
		this.treeGrid2VerticalLayout.setSizeFull();
		this.horizontalLayout.add(this.treeGrid1VerticalLayout, this.treeGrid2VerticalLayout);
		this.horizontalLayout.setSizeFull();
		this.gridLayoutOverview.add(this.horizontalLayout);
		this.tabs.setWidthFull();
		this.tabs.setHeight(null);
		this.verticalLayoutReports.setSizeFull();
		this.gridLayoutOverview.setSizeFull();
		this.verticalLayout2.add(this.tabs, this.verticalLayoutReports, this.gridLayoutOverview);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.verticalLayout2);
		this.splitLayout.setSplitterPosition(40.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.tabs.setSelectedIndex(-1);

		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdNew.addFocusListener(this::cmdNew_onFocus);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdUpdate.addClickListener(this::cmdUpdate_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdReport.addClickListener(this::cmdReport_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.cmdAdmin.addClickListener(this::cmdAdmin_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.grid.addItemDoubleClickListener(this::grid_onItemDoubleClick);
		this.cmdNewLine.addClickListener(this::cmdNewLine_onClick);
		this.cmdDeleteLine.addClickListener(this::cmdDeleteLine_onClick);
		this.cmdUpdateLine.addClickListener(this::cmdUpdateLine_onClick);
		this.cmdExcel.addClickListener(this::cmdExcel_onClick);
		this.cmdCopyLine.addClickListener(this::cmdCopyLine_onClick);
		this.cmdExport.addClickListener(this::cmdExport_onClick);
		this.cmdInfoExpense.addClickListener(this::cmdInfoExpense_onClick);
		this.tableLine.addItemClickListener(this::tableLine_onItemClick);
		this.tableLine.addItemDoubleClickListener(this::tableLine_onItemDoubleClick);
		this.cmdRefOverview.addClickListener(this::cmdRefOverview_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Button            cmdNew, cmdDelete, cmdUpdate, cmdReload, cmdReport, cmdInfo, cmdAdmin, cmdNewLine,
		cmdDeleteLine, cmdUpdateLine, cmdExcel, cmdCopyLine, cmdExport, cmdInfoExpense, cmdRefOverview;
	private SplitLayout       splitLayout;
	private Tab               tab, tab2;
	private Grid<Periode>     grid;
	private Grid<ProjectLine> tableLine;
	private VerticalLayout    verticalLayout, verticalLayout2, verticalLayoutReports, gridLayoutOverview,
		treeGrid1VerticalLayout, treeGrid2VerticalLayout;
	private HorizontalLayout  horizontalLayout2, horizontalLayout3, horizontalLayout;
	private Label             lblTotalMonth;
	private Tabs              tabs;
	private FilterComponent   containerFilterComponent, containerFilterComponent2;
	// </generated-code>
	
}
