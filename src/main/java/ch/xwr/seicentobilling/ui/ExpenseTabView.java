
package ch.xwr.seicentobilling.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.apache.log4j.LogManager;

import com.flowingcode.vaadin.addons.ironicons.ImageIcons;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.filter.FilterComponent;
import com.rapidclipse.framework.server.ui.filter.FilterData;
import com.rapidclipse.framework.server.ui.filter.FilterEntry;
import com.rapidclipse.framework.server.ui.filter.FilterOperator;
import com.rapidclipse.framework.server.ui.filter.GridFilterSubjectFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Vat;


@Route("expense")
public class ExpenseTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG            = LogManager.getLogger(ExpenseTabView.class);
	private Periode                              currentPeriode = null;
	
	/**
	 *
	 */
	public ExpenseTabView()
	{
		super();
		this.initUI();
		this.tableLine.setItems(new ArrayList<Expense>());
		
		this.sortList(true);
		// set RO Fields
		this.setROFields();

		this.setDefaultFilter();
		this.tableLine
			.addComponentColumn(
				item -> new FunctionExpenseAttachmentDownload().createDownLoadButton(this.tableLine, item))
			.setHeader("Beleg");
		
	}

	private void setROFields()
	{
		final boolean roFlag = this.isBooked();
		
		this.cmdNewExpense.setEnabled(!roFlag);
		this.cmdUpdateExpense.setEnabled(!roFlag);
		this.cmdDeleteExpense.setEnabled(!roFlag);
		this.cmdCopySingle.setEnabled(!roFlag);
		this.cmdUpdate.setEnabled(!roFlag);
		this.cmdDelete.setEnabled(!roFlag);
		
		if(this.currentPeriode != null && this.currentPeriode.getPerSignOffExpense() != null
			&& this.currentPeriode.getPerSignOffExpense())
		{
			this.cmdNewExpense.setEnabled(false);
			this.cmdUpdateExpense.setEnabled(false);
			this.cmdDeleteExpense.setEnabled(false);
			this.cmdCopySingle.setEnabled(false);
		}
		
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

	private void sortList(final boolean all)
	{
		final GridSortOrder<Periode> sortCol1 =
			new GridSortOrder<>(this.grid.getColumnByKey("perYear"), SortDirection.DESCENDING);
		final GridSortOrder<Periode> sortCol2 =
			new GridSortOrder<>(this.grid.getColumnByKey("perMonth"), SortDirection.DESCENDING);
		// final GridSortOrder<Periode> sortCol3 =
		// new GridSortOrder<>(this.grid.getColumnByKey("costaccount"), SortDirection.ASCENDING);
		this.grid.sort(Arrays.asList(sortCol1, sortCol2));
		
		if(!all)
		{
			return;
		}
		final GridSortOrder<Expense> sortCol11 =
			new GridSortOrder<>(this.tableLine.getColumnByKey("expDate"), SortDirection.DESCENDING);
		// final GridSortOrder<Expense> sortCol21 =
		// new GridSortOrder<>(this.tableLine.getColumnByKey("expFlagGeneric"), SortDirection.ASCENDING);
		this.tableLine.sort(Arrays.asList(sortCol11));
	}

	private boolean isBooked()
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return false;
		}
		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(LovState.BookingType.gebucht.equals(bean.getPerBookedExpense()))
		{
			return true;
		}

		// if (bean != null && bean.getPerBookedExpense() != null) {
		// if (bean.getPerBookedExpense() == LovState.BookingType.gebucht) {
		// return true;
		// }
		// }
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
		}
		else
		{
			this.grid.getSelectionModel().select(this.currentPeriode);
		}

		this.reloadExpenseList();
		this.setROFields();
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
				// final String reason = (String)UI.getCurrent().getSession().getAttribute("reason");

				if(retval == null)
				{
					retval = "cmdCancel";
				}
				else
				{
					final Long    beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
					final Periode bean   = new PeriodeDAO().find(beanId);

					ExpenseTabView.this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
					ExpenseTabView.this.reloadMainTable();
					ExpenseTabView.this.grid.select(bean);
					ExpenseTabView.this.grid.getDataProvider().refreshAll();
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

		this.sortList(false);

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
	 * Event handler delegate method for the {@link Button} {@link #cmdNewExpense}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewExpense_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		if(this.grid.getSelectedItems() == null)
		{
			return;
		}
		final Long objId = this.grid.getSelectionModel().getFirstSelectedItem().get().getPerId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupExpense();
	}

	private void popupExpense()
	{
		final Dialog win = ExpensePopup.getPopupWindow();
		
		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				
				ExpenseTabView.this.reloadExpenseList();
			}
			
		});
		win.open();
	}
	
	private void reloadExpenseList()
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Periode bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		if(bean != null)
		{
			this.tableLine.setDataProvider(DataProvider.ofCollection(new ExpenseDAO().findByPeriode(bean)));
			this.tableLine.getDataProvider().refreshAll();
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteExpense}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteExpense_onClick(final ClickEvent<Button> event)
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
				
				final Expense bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getExpId(), bean.getClass().getSimpleName());
				
				final ExpenseDAO dao = new ExpenseDAO();
				dao.remove(bean);
				dao.flush();
				
				this.tableLine
					.setDataProvider(DataProvider.ofCollection(new ExpenseDAO().findByPeriode(this.currentPeriode)));
				ExpenseTabView.this.tableLine.getDataProvider().refreshAll();
				
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
				ExpenseTabView.LOG.error("Error on delete", e);
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
				ExpenseTabView.this.grid.getDataProvider().refreshAll();

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
				ExpenseTabView.LOG.error("Error on delete", e);
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
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdateExpense}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateExpense_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableLine.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final Long beanId = this.tableLine.getSelectionModel().getFirstSelectedItem().get().getExpId();
		final Long objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupExpense();
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_onItemClick(final ItemClickEvent<Expense> event)
	{
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableLine}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_onItemDoubleClick(final ItemDoubleClickEvent<Expense> event)
	{
		if(!this.isBooked())
		{
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final Expense obj = event.getItem();
			this.tableLine.select(obj); // reselect after double-click
			
			final Long beanId = obj.getExpId();
			final Long objId  = null;
			
			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);
			
			this.popupExpense();
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCopySingle}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopySingle_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableLine.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Expense bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();

		bean.setExpId(new Long(0));
		bean.setExpBooked(null);
		bean.setExpText(bean.getExpText() + " (Kopie)");

		final ExpenseDAO dao     = new ExpenseDAO();
		final Expense    newBean = dao.merge(bean);
		dao.save(newBean);

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getExpId(), newBean.getClass().getSimpleName());

		this.reloadExpenseList();
		this.tableLine.select(newBean);
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
			final Expense bean = this.tableLine.getSelectionModel().getFirstSelectedItem().get();
			final Dialog  win  = RowObjectView.getPopupWindow();
			//
			win.add(new RowObjectView(bean.getExpId(), bean.getClass().getSimpleName()));
			win.open();
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
		this.cmdUpdate                = new Button();
		this.cmdReload                = new Button();
		this.cmdInfo                  = new Button();
		this.grid                     = new Grid<>(Periode.class, false);
		this.verticalLayout2          = new VerticalLayout();
		this.verticalLayoutExpense    = new Label();
		this.horizontalLayout3        = new HorizontalLayout();
		this.cmdNewExpense            = new Button();
		this.cmdDeleteExpense         = new Button();
		this.cmdUpdateExpense         = new Button();
		this.cmdCopySingle            = new Button();
		this.cmdInfoExpense           = new Button();
		this.tableLine                = new Grid<>(Expense.class, false);

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setPadding(false);
		this.horizontalLayout2.setMinHeight("");
		this.horizontalLayout2.setMinWidth("100%");
		this.cmdNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDelete.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdate.setIcon(ImageIcons.EDIT.create());
		this.cmdReload.setIcon(VaadinIcon.REFRESH.create());
		this.cmdInfo.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.grid.addColumn(Periode::getPerName).setKey("perName").setHeader("Periode").setSortable(true);
		this.grid.addColumn(Periode::getPerYear).setKey("perYear").setHeader("Jahr").setSortable(true)
			.setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerMonth)).setKey("perMonth").setHeader("Monat")
			.setSortable(true).setVisible(false);
		this.grid.addColumn(Periode::getPerSignOffExpense).setKey("perSignOffExpense").setHeader("Freigabe")
			.setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerBookedExpense)).setKey("perBookedExpense")
			.setHeader("Buchhaltung").setSortable(true);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerBookedProject)).setKey("perBookedProject")
			.setHeader("Gebucht Projekt").setSortable(true).setVisible(false);
		this.grid.addColumn(new CaptionRenderer<>(Periode::getPerState)).setKey("perState").setHeader("Status")
			.setSortable(true).setVisible(false);
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(Periode::getCostAccount).map(CostAccount::getCsaName).orElse(null))
			.setKey("costAccount.csaName").setHeader("Kostenstelle").setSortable(true).setVisible(false);
		this.grid.setDataProvider(DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setMaxWidth("");
		this.verticalLayout2.setPadding(false);
		this.verticalLayoutExpense
			.setText(StringResourceUtils.optLocalizeString("{$verticalLayoutExpense.caption}", this));
		this.cmdNewExpense.setText(StringResourceUtils.optLocalizeString("{$cmdNewExpense.caption}", this));
		this.cmdNewExpense.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteExpense.setText(StringResourceUtils.optLocalizeString("{$cmdDeleteExpense.caption}", this));
		this.cmdDeleteExpense.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdateExpense.setText(StringResourceUtils.optLocalizeString("{$cmdUpdateExpense.caption}", this));
		this.cmdUpdateExpense.setIcon(VaadinIcon.PENCIL.create());
		this.cmdCopySingle.setIcon(VaadinIcon.COPY.create());
		this.cmdInfoExpense.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.tableLine.addColumn(Expense::getExpDate).setKey("expDate").setHeader("Datum").setResizable(true)
			.setSortable(true);
		this.tableLine.addColumn(Expense::getExpAccount).setKey("expAccount").setHeader("Konto").setResizable(true)
			.setSortable(true).setVisible(false);
		this.tableLine.addColumn(Expense::getExpFlagCostAccount).setKey("expFlagCostAccount").setHeader("KST")
			.setResizable(true).setSortable(true);
		this.tableLine.addColumn(Expense::getExpAmount).setKey("expAmount").setHeader("Betrag").setResizable(true)
			.setSortable(true);
		this.tableLine.addColumn(v -> Optional.ofNullable(v).map(Expense::getVat).map(Vat::getVatSign).orElse(null))
			.setKey("vat.vatSign").setHeader("Mwst").setResizable(true).setSortable(true);
		this.tableLine.addColumn(v -> Optional.ofNullable(v).map(Expense::getVat).map(Vat::getVatName).orElse(null))
			.setKey("vat.vatName").setHeader("Bezeichnung").setResizable(true).setSortable(true).setVisible(false);
		this.tableLine.addColumn(Expense::getExpText).setKey("expText").setHeader("Text").setResizable(true)
			.setSortable(true);
		this.tableLine
			.addColumn(v -> Optional.ofNullable(v).map(Expense::getProject).map(Project::getProName).orElse(null))
			.setKey("project.proName").setHeader("Projekt").setResizable(true).setSortable(true);
		this.tableLine.addColumn(new CaptionRenderer<>(Expense::getExpState)).setKey("expState").setHeader("Status")
			.setResizable(true).setSortable(true);
		this.tableLine.addColumn(Expense::getExpQuantity).setKey("expQuantity").setHeader("Menge").setResizable(true)
			.setSortable(true).setVisible(false);
		this.tableLine.addColumn(Expense::getExpBooked).setKey("expBooked").setHeader("Gebucht").setResizable(true)
			.setSortable(true).setVisible(false);
		this.tableLine.setSelectionMode(Grid.SelectionMode.SINGLE);

		this.containerFilterComponent.connectWith(this.grid.getDataProvider());
		this.containerFilterComponent.setFilterSubject(GridFilterSubjectFactory.CreateFilterSubject(this.grid,
			Arrays.asList("costAccount.csaCode", "costAccount.csaName", "perName"),
			Arrays.asList("costAccount", "costAccount.csaCode", "costAccount.csaName", "perBookedExpense",
				"perBookedProject", "perMonth", "perSignOffExpense", "perState", "perYear")));

		this.cmdNew.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdUpdate.setSizeUndefined();
		this.cmdReload.setSizeUndefined();
		this.cmdInfo.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNew, this.cmdDelete, this.cmdUpdate, this.cmdReload, this.cmdInfo);
		this.containerFilterComponent.setWidthFull();
		this.containerFilterComponent.setHeight(null);
		this.horizontalLayout2.setWidth("100px");
		this.horizontalLayout2.setHeight("60px");
		this.grid.setSizeFull();
		this.verticalLayout.add(this.containerFilterComponent, this.horizontalLayout2, this.grid);
		this.verticalLayout.setFlexGrow(1.0, this.grid);
		this.cmdNewExpense.setWidth("50%");
		this.cmdNewExpense.setHeight(null);
		this.cmdDeleteExpense.setWidth("50%");
		this.cmdDeleteExpense.setHeight(null);
		this.cmdUpdateExpense.setWidth("50%");
		this.cmdUpdateExpense.setHeight(null);
		this.cmdCopySingle.setSizeUndefined();
		this.cmdInfoExpense.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdNewExpense, this.cmdDeleteExpense, this.cmdUpdateExpense, this.cmdCopySingle,
			this.cmdInfoExpense);
		this.verticalLayoutExpense.setWidthFull();
		this.verticalLayoutExpense.setHeight("30px");
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("10%");
		this.tableLine.setSizeFull();
		this.verticalLayout2.add(this.verticalLayoutExpense, this.horizontalLayout3, this.tableLine);
		this.verticalLayout2.setFlexGrow(1.0, this.tableLine);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.verticalLayout2);
		this.splitLayout.setSplitterPosition(50.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.cmdNew.addClickListener(this::cmdNew_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdUpdate.addClickListener(this::cmdUpdate_onClick);
		this.cmdReload.addClickListener(this::cmdReload_onClick);
		this.cmdInfo.addClickListener(this::cmdInfo_onClick);
		this.grid.addItemClickListener(this::grid_onItemClick);
		this.grid.addItemDoubleClickListener(this::grid_onItemDoubleClick);
		this.cmdNewExpense.addClickListener(this::cmdNewExpense_onClick);
		this.cmdDeleteExpense.addClickListener(this::cmdDeleteExpense_onClick);
		this.cmdUpdateExpense.addClickListener(this::cmdUpdateExpense_onClick);
		this.cmdCopySingle.addClickListener(this::cmdCopySingle_onClick);
		this.cmdInfoExpense.addClickListener(this::cmdInfoExpense_onClick);
		this.tableLine.addItemClickListener(this::tableLine_onItemClick);
		this.tableLine.addItemDoubleClickListener(this::tableLine_onItemDoubleClick);
	} // </generated-code>

	// <generated-code name="variables">
	private Button           cmdNew, cmdDelete, cmdUpdate, cmdReload, cmdInfo, cmdNewExpense, cmdDeleteExpense,
		cmdUpdateExpense, cmdCopySingle, cmdInfoExpense;
	private Grid<Expense>    tableLine;
	private SplitLayout      splitLayout;
	private Grid<Periode>    grid;
	private VerticalLayout   verticalLayout, verticalLayout2;
	private HorizontalLayout horizontalLayout2, horizontalLayout3;
	private Label            verticalLayoutExpense;
	private FilterComponent  containerFilterComponent;
	// </generated-code>

}
