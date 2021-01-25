package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.util.NestedProperty;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.Project_;
import ch.xwr.seicentobilling.entities.Vat_;

public class ExpenseTabView extends XdevView {
	private Periode currentPeriode = null;

	//private TableFieldFactory tfFac;
	/**
	 *
	 */
	public ExpenseTabView() {
		super();
		this.initUI();

		// Sort Tables
		this.tableLine.clear();
		final Object[] properties = { "expDate", "expFlagGeneric" };
		final boolean[] ordering = { false, true };
		this.tableLine.sort(properties, ordering);

		final Object[] properties2 = { "perYear", "perMonth", "costaccount" };
		final boolean[] ordering2 = { false, false, true };
		this.table.sort(properties2, ordering2);

		// set RO Fields
		setROFields();

		setDefaultFilter();
	}

	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0);	//Dev Mode
		}

		final Calendar cal = Calendar.getInstance();
		final int iyear = cal.get(Calendar.YEAR);

		final Integer[] val = new Integer[]{iyear};
		final CostAccount[] val2 = new CostAccount[]{bean};
		final FilterData[] fd = new FilterData[]{new FilterData("perYear", new FilterOperator.Is(), val),
				new FilterData("costAccount", new FilterOperator.Is(), val2)};

		this.containerFilterComponent.setFilterData(fd);

	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute("beanId",  null);
		UI.getCurrent().getSession().setAttribute("reason",  "new");
		UI.getCurrent().getSession().setAttribute("source",  "expense");
		UI.getCurrent().getSession().setAttribute("isAdmin",  false);

		popupPeriode();
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDelete}.
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
				final Periode bean = ExpenseTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPerId(), bean.getClass().getSimpleName());

				final PeriodeDAO dao = new PeriodeDAO();
				dao.remove(bean);

				ExpenseTabView.this.table.removeItem(bean);
				ExpenseTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					ExpenseTabView.this.table.select(ExpenseTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					// ignore
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		reloadMainTable();
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();
		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getPerId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewExpense}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewExpense_buttonClick(final Button.ClickEvent event) {
		final Long beanId = null;
		if (this.table.getSelectedItem() == null) {
			return;
		}
		final Long objId = this.table.getSelectedItem().getBean().getPerId();


		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupExpense();

	}

	private void popupDialogCopy() {
		final Window win = PeriodeDialogPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent evnt) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					final Long toId = (Long) UI.getCurrent().getSession().getAttribute("toPerId");

					final Collection<?> col = ExpenseTabView.this.table.getItemIds();
					for (final Iterator<?> iterator = col.iterator(); iterator.hasNext();) {
						final Periode  per = (Periode ) iterator.next();
						if (per.getPerId().compareTo(toId) == 0) {
							ExpenseTabView.this.table.select(per);
							break;
						}
					}
				}
			}

		});
		this.getUI().addWindow(win);

	}

	private void popupExpense() {
		final Window win = ExpensePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadExpenseList();
			}

		});
		this.getUI().addWindow(win);
	}

	private void reloadMainTable() {
		//save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		//clear+reload List
		this.table.removeAllItems();
		this.table.getBeanContainerDataSource().addAll(new PeriodeDAO().findAll());

		//define sort
		final Object[] properties2 = { "perYear", "perMonth", "costaccount" };
		final boolean[] ordering2 = { false, false, true };
		this.table.sort(properties2, ordering2);

		//reassign filter
		this.containerFilterComponent.setFilterData(fd);
	}

	private void reloadExpenseList() {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Periode bean = this.table.getSelectedItem().getBean();

		final XdevBeanContainer<Expense> myCustomerList = this.tableLine.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new ExpenseDAO().findByPeriode(bean));

		if (bean != null) {
			this.tableLine.refreshRowCache();
			this.tableLine.getBeanContainerDataSource().refresh();
			this.tableLine.sort();
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteExpense}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteExpense_buttonClick(final Button.ClickEvent event) {
		final XdevTable<Expense> tab = this.tableLine;
		if (tab.getSelectedItem() == null) {
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
				final Expense bean = tab.getSelectedItem().getBean();

				Expense prev = (Expense) tab.prevItemId(bean);
				if (prev == null) {
					prev = (Expense) tab.nextItemId(bean);
				}

				// Update RowObject
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getExpId(), bean.getClass().getSimpleName());
				// Delete Record
				final ExpenseDAO dao = new ExpenseDAO();
				dao.remove(bean);

				// refresh tab
				tab.removeItem(bean);
				tab.getBeanContainerDataSource().refresh();

				if (prev != null) {
					tab.select(prev);
				}


				// if (!tab.isEmpty())
				// tab.select(tab.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdateExpense}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateExpense_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableLine.getSelectedItem().getBean().getExpId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupExpense();
	}

	private void setROFields() {
		final boolean roFlag = isBooked();

		this.cmdNewExpense.setEnabled(!roFlag);
		this.cmdUpdateExpense.setEnabled(!roFlag);
		this.cmdDeleteExpense.setEnabled(!roFlag);
		this.cmdCopySingle.setEnabled(!roFlag);
		this.cmdUpdate.setEnabled(!roFlag);
		this.cmdDelete.setEnabled(!roFlag);

		if (this.currentPeriode != null && this.currentPeriode.getPerSignOffExpense() != null && this.currentPeriode.getPerSignOffExpense()) {
			this.cmdNewExpense.setEnabled(false);
			this.cmdUpdateExpense.setEnabled(false);
			this.cmdDeleteExpense.setEnabled(false);
			this.cmdCopySingle.setEnabled(false);
		}

	}

	private boolean isBooked() {
		if (this.table.getSelectedItem() == null) {
			return false;
		}
		final Periode bean = this.table.getSelectedItem().getBean();
		if (LovState.BookingType.gebucht.equals(bean.getPerBookedExpense())) {
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
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		Periode bean = (Periode) event.getProperty().getValue();
		if (bean == null) {
			bean =  this.currentPeriode;
			this.table.select(bean);
		} else {
			this.currentPeriode = bean;
		}

//		Periode bean = null;
//		if (this.table.getSelectedItem() != null) {
//			 bean = this.table.getSelectedItem().getBean();
//		}

		reloadExpenseList();
		setROFields();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableLine}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick() && !isBooked()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final Expense obj = (Expense) event.getItemId();
			this.tableLine.select(obj); // reselect after double-click

			final Long beanId = obj.getExpId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupExpense();
		}

	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfoExpense}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoExpense_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final Expense bean = this.tableLine.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();
		win.setContent(new RowObjectView(bean.getExpId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdCopyExpenses}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopyExpenses_buttonClick(final Button.ClickEvent event) {
		Long fromId = new Long(0);
		Long toId = new Long(0);

		if (this.table.getSelectedItem() != null) {
			fromId = this.table.getSelectedItem().getBean().getPerId();
		}

		final Periode first = (Periode) this.table.firstItemId();
		if (first != null) {
			toId = first.getPerId();
		}

		UI.getCurrent().getSession().setAttribute("fromPerId", fromId);
		UI.getCurrent().getSession().setAttribute("toPerId", toId);

		//copy values from Last Periode...
		//first we get the Input Parameters
		popupDialogCopy();


	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdCopySingle}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopySingle_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final Expense bean = this.tableLine.getSelectedItem().getBean();

		bean.setExpId(new Long(0));
		bean.setExpBooked(null);
		bean.setExpText(bean.getExpText() + " (Kopie)");

		final ExpenseDAO dao = new ExpenseDAO();
		final Expense newBean = dao.merge(bean);
		dao.save(newBean);

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getExpId(), newBean.getClass().getSimpleName());

		reloadExpenseList();
		this.tableLine.select(newBean);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReport_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();

		final JasperManager jsp = new JasperManager();
		jsp.addParameter("Param_Periode", "" + bean.getPerId());
//		jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());

		Page.getCurrent().open(jsp.getUri(JasperManager.ExpenseReport1), "_blank");

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdUpdate}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdate_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.table.getSelectedItem().getBean().getPerId();
		UI.getCurrent().getSession().setAttribute("beanId",  beanId);
		UI.getCurrent().getSession().setAttribute("reason",  "update");
		UI.getCurrent().getSession().setAttribute("source",  "expense");
		UI.getCurrent().getSession().setAttribute("isAdmin",  false);

		popupPeriode();

	}

	private void popupPeriode() {
		final Window win = PeriodePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				final String reason = (String) UI.getCurrent().getSession().getAttribute("reason");

				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdSave")) {
					final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
					final Periode bean = new PeriodeDAO().find(beanId);

					if ("new".equals(reason)) {
						ExpenseTabView.this.table.addItem(bean);
					}
					reloadMainTable();
					ExpenseTabView.this.table.select(bean);
				}

			}
		});
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdToggleEdit}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdToggleEdit_buttonClick(final Button.ClickEvent event) {
		this.tableLine.setEditable(!this.tableLine.isEditable());

		final Object[] zz = this.tableLine.getVisibleColumns();
		for (int i = 0; i < zz.length; i++) {
			final Object xx = zz[i];
			final ColumnGenerator yy = this.tableLine.getColumnGenerator(xx);
			if (yy == null) {
				//
			}

		}

		final Collection<?> x = this.tableLine.getContainerPropertyIds();
		for (final Iterator<?> iterator = x.iterator(); iterator.hasNext();) {
			final Object object = iterator.next();
			final ColumnGenerator yy = this.tableLine.getColumnGenerator(object);
			if (yy == null) {
				//
			}

		}

		if (this.tableLine.isEditable()) {
			//tableLine.getContainerProperty(event, event);
		}

		//this.tableLine.getTableFieldFactory().createField(table, event, event, actionLayout);
		//this.tfFac = this.tableLine.getTableFieldFactory();

		this.tableLine.setVisibleColumns("expDate", "expAccount", "expFlagCostAccount", "expAmount", "expText");
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see FieldEvents.FocusListener#focus(FieldEvents.FocusEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_focus(final FieldEvents.FocusEvent event) {
		reloadMainTable();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		if (event.isDoubleClick() && !isBooked()) {
			if (this.table.getSelectedItem() != null) {
				this.cmdUpdate.click();
			}
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdZipReport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdZipReport_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();
		UI.getCurrent().getSession().setAttribute("perBeanId", bean.getPerId());

		popupExpenseReportPopup();
	}

	private void popupExpenseReportPopup() {
		final Window win = ExpenseReportPopup.getPopupWindow();
		this.getUI().addWindow(win);
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayoutLeft = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdUpdate = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdCopyExpenses = new XdevButton();
		this.cmdReport = new XdevButton();
		this.cmdZipReport = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.verticalLayoutRight = new XdevVerticalLayout();
		this.tabSheet = new XdevTabSheet();
		this.verticalLayoutExpense = new XdevVerticalLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdNewExpense = new XdevButton();
		this.cmdDeleteExpense = new XdevButton();
		this.cmdUpdateExpense = new XdevButton();
		this.cmdCopySingle = new XdevButton();
		this.cmdInfoExpense = new XdevButton();
		this.cmdToggleEdit = new XdevButton();
		this.tableLine = new XdevTable<>();

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(40.0F, Unit.PERCENTAGE);
		this.verticalLayoutLeft.setMargin(new MarginInfo(false));
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNew.setDescription("Neuen Datensatz anlegen");
		this.cmdDelete.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdDelete.setDescription("Datensatz löschen");
		this.cmdUpdate.setIcon(FontAwesome.PENCIL);
		this.cmdUpdate.setDescription("Periode bearbeiten...");
		this.cmdUpdate.setImmediate(true);
		this.cmdReload.setIcon(FontAwesome.REFRESH);
		this.cmdCopyExpenses.setIcon(FontAwesome.COPY);
		this.cmdCopyExpenses.setDescription("Alle Spesen einer Periode kopieren");
		this.cmdReport.setIcon(FontAwesome.PRINT);
		this.cmdReport.setDescription("Jasper Report starten");
		this.cmdZipReport.setIcon(FontAwesome.FILE_ZIP_O);
		this.cmdZipReport.setDescription("Spesen als ZIP Report");
		this.cmdInfo.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdInfo.setDescription("Objektinfo");
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Periode.class, DAOs.get(PeriodeDAO.class).findAll(),
				NestedProperty.of(Periode_.costAccount, CostAccount_.csaName));
		this.table.setVisibleColumns(Periode_.perName.getName(), Periode_.perYear.getName(), Periode_.perMonth.getName(),
				Periode_.perSignOffExpense.getName(), Periode_.perBookedExpense.getName(),
				Periode_.perBookedProject.getName(), Periode_.perState.getName(),
				NestedProperty.path(Periode_.costAccount, CostAccount_.csaName));
		this.table.setColumnHeader("perName", "Periode");
		this.table.setColumnHeader("perYear", "Jahr");
		this.table.setConverter("perYear", ConverterBuilder.stringToInteger().build());
		this.table.setColumnCollapsed("perYear", true);
		this.table.setColumnHeader("perMonth", "Monat");
		this.table.setColumnCollapsed("perMonth", true);
		this.table.setColumnHeader("perSignOffExpense", "Freigabe");
		this.table.setConverter("perSignOffExpense",
				ConverterBuilder.stringToBoolean().trueString("Ja").falseString("Nein").build());
		this.table.setColumnWidth("perSignOffExpense", 80);
		this.table.setColumnHeader("perBookedExpense", "Buchhaltung");
		this.table.setColumnHeader("perBookedProject", "Gebucht Projekt");
		this.table.setColumnCollapsed("perBookedProject", true);
		this.table.setColumnHeader("perState", "Status");
		this.table.setColumnCollapsed("perState", true);
		this.table.setColumnHeader("costAccount.csaName", "Kostenstelle");
		this.table.setColumnCollapsed("costAccount.csaName", true);
		this.verticalLayoutRight.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.tabSheet.setImmediate(false);
		this.verticalLayoutExpense.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setImmediate(true);
		this.cmdNewExpense.setIcon(FontAwesome.PLUS_CIRCLE);
		this.cmdNewExpense.setCaption(StringResourceUtils.optLocalizeString("{$cmdNewExpense.caption}", this));
		this.cmdNewExpense.setClickShortcut(ShortcutAction.KeyCode.N, ShortcutAction.ModifierKey.CTRL);
		this.cmdDeleteExpense.setIcon(FontAwesome.MINUS_CIRCLE);
		this.cmdDeleteExpense.setCaption(StringResourceUtils.optLocalizeString("{$cmdDeleteExpense.caption}", this));
		this.cmdUpdateExpense.setIcon(FontAwesome.PENCIL);
		this.cmdUpdateExpense.setCaption(StringResourceUtils.optLocalizeString("{$cmdUpdateExpense.caption}", this));
		this.cmdCopySingle.setIcon(FontAwesome.COPY);
		this.cmdCopySingle.setDescription("Markierten Datensatz kopieren");
		this.cmdInfoExpense.setIcon(FontAwesome.INFO_CIRCLE);
		this.cmdToggleEdit.setIcon(null);
		this.cmdToggleEdit.setStyleName("tiny");
		this.cmdToggleEdit.setEnabled(false);
		this.cmdToggleEdit.setVisible(false);
		this.tableLine.setColumnReorderingAllowed(true);
		this.tableLine.setColumnCollapsingAllowed(true);
		this.tableLine.setContainerDataSource(Expense.class, false, NestedProperty.of(Expense_.vat, Vat_.vatSign),
				NestedProperty.of(Expense_.vat, Vat_.vatName), NestedProperty.of(Expense_.project, Project_.proName));
		this.tableLine.addGeneratedColumn("generated", new FunctionExpenseAttachmentDownload.Generator());
		this.tableLine.setVisibleColumns(Expense_.expDate.getName(), Expense_.expAccount.getName(),
				Expense_.expFlagCostAccount.getName(), Expense_.expFlagGeneric.getName(), Expense_.expAmount.getName(),
				NestedProperty.path(Expense_.vat, Vat_.vatSign), NestedProperty.path(Expense_.vat, Vat_.vatName),
				Expense_.expText.getName(), NestedProperty.path(Expense_.project, Project_.proName),
				Expense_.expState.getName(), Expense_.expUnit.getName(), Expense_.expQuantity.getName(),
				Expense_.expBooked.getName(), "generated");
		this.tableLine.setColumnHeader("expDate", "Datum");
		this.tableLine.setConverter("expDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableLine.setColumnHeader("expAccount", "Konto");
		this.tableLine.setColumnHeader("expFlagCostAccount", "KST");
		this.tableLine.setConverter("expFlagCostAccount",
				ConverterBuilder.stringToBoolean().trueString("Ja").falseString("Nein").build());
		this.tableLine.setColumnHeader("expFlagGeneric", "Pauschal");
		this.tableLine.setColumnHeader("expAmount", "Betrag");
		this.tableLine.setColumnAlignment("expAmount", Table.Align.RIGHT);
		this.tableLine.setConverter("expAmount", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnHeader("vat.vatSign", "Mwst");
		this.tableLine.setColumnHeader("vat.vatName", "Bezeichnung");
		this.tableLine.setColumnCollapsed("vat.vatName", true);
		this.tableLine.setColumnHeader("expText", "Text");
		this.tableLine.setColumnExpandRatio("expText", 2.0F);
		this.tableLine.setColumnHeader("project.proName", "Projekt");
		this.tableLine.setColumnHeader("expState", "Status");
		this.tableLine.setColumnCollapsed("expState", true);
		this.tableLine.setColumnHeader("expUnit", "Einheit");
		this.tableLine.setColumnCollapsed("expUnit", true);
		this.tableLine.setColumnHeader("expQuantity", "Menge");
		this.tableLine.setColumnCollapsed("expQuantity", true);
		this.tableLine.setColumnHeader("expBooked", "Gebucht");
		this.tableLine.setColumnCollapsed("expBooked", true);
		this.tableLine.setColumnHeader("generated", "Beleg");

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "perYear", "perMonth",
				"costAccount", "perBookedExpense", "perBookedProject", "perState", "costAccount.csaCode",
				"costAccount.csaName", "perSignOffExpense");
		this.containerFilterComponent.setSearchableProperties("perName", "costAccount.csaName", "costAccount.csaCode");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdUpdate.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdUpdate);
		this.actionLayout.setComponentAlignment(this.cmdUpdate, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdCopyExpenses.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdCopyExpenses);
		this.actionLayout.setComponentAlignment(this.cmdCopyExpenses, Alignment.MIDDLE_CENTER);
		this.cmdReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReport);
		this.actionLayout.setComponentAlignment(this.cmdReport, Alignment.MIDDLE_CENTER);
		this.cmdZipReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdZipReport);
		this.actionLayout.setComponentAlignment(this.cmdZipReport, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutLeft.addComponent(this.containerFilterComponent);
		this.verticalLayoutLeft.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.actionLayout.setSizeUndefined();
		this.verticalLayoutLeft.addComponent(this.actionLayout);
		this.verticalLayoutLeft.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_LEFT);
		this.table.setSizeFull();
		this.verticalLayoutLeft.addComponent(this.table);
		this.verticalLayoutLeft.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayoutLeft.setExpandRatio(this.table, 100.0F);
		this.cmdNewExpense.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdNewExpense);
		this.horizontalLayout.setComponentAlignment(this.cmdNewExpense, Alignment.MIDDLE_CENTER);
		this.cmdDeleteExpense.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDeleteExpense);
		this.horizontalLayout.setComponentAlignment(this.cmdDeleteExpense, Alignment.MIDDLE_CENTER);
		this.cmdUpdateExpense.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdUpdateExpense);
		this.horizontalLayout.setComponentAlignment(this.cmdUpdateExpense, Alignment.MIDDLE_CENTER);
		this.cmdCopySingle.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCopySingle);
		this.horizontalLayout.setComponentAlignment(this.cmdCopySingle, Alignment.MIDDLE_CENTER);
		this.cmdInfoExpense.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdInfoExpense);
		this.horizontalLayout.setComponentAlignment(this.cmdInfoExpense, Alignment.MIDDLE_CENTER);
		this.cmdToggleEdit.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdToggleEdit);
		this.horizontalLayout.setComponentAlignment(this.cmdToggleEdit, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setSizeUndefined();
		this.verticalLayoutExpense.addComponent(this.horizontalLayout);
		this.verticalLayoutExpense.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_LEFT);
		this.tableLine.setSizeFull();
		this.verticalLayoutExpense.addComponent(this.tableLine);
		this.verticalLayoutExpense.setComponentAlignment(this.tableLine, Alignment.MIDDLE_CENTER);
		this.verticalLayoutExpense.setExpandRatio(this.tableLine, 100.0F);
		this.verticalLayoutExpense.setSizeFull();
		this.tabSheet.addTab(this.verticalLayoutExpense,
				StringResourceUtils.optLocalizeString("{$verticalLayoutExpense.caption}", this), null);
		this.tabSheet.setSelectedTab(this.verticalLayoutExpense);
		this.tabSheet.setSizeFull();
		this.verticalLayoutRight.addComponent(this.tabSheet);
		this.verticalLayoutRight.setExpandRatio(this.tabSheet, 100.0F);
		this.verticalLayoutLeft.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayoutLeft);
		this.verticalLayoutRight.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.verticalLayoutRight);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdNew.addFocusListener(event -> this.cmdNew_focus(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdUpdate.addClickListener(event -> this.cmdUpdate_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdCopyExpenses.addClickListener(event -> this.cmdCopyExpenses_buttonClick(event));
		this.cmdReport.addClickListener(event -> this.cmdReport_buttonClick(event));
		this.cmdZipReport.addClickListener(event -> this.cmdZipReport_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.cmdNewExpense.addClickListener(event -> this.cmdNewExpense_buttonClick(event));
		this.cmdDeleteExpense.addClickListener(event -> this.cmdDeleteExpense_buttonClick(event));
		this.cmdUpdateExpense.addClickListener(event -> this.cmdUpdateExpense_buttonClick(event));
		this.cmdCopySingle.addClickListener(event -> this.cmdCopySingle_buttonClick(event));
		this.cmdInfoExpense.addClickListener(event -> this.cmdInfoExpense_buttonClick(event));
		this.cmdToggleEdit.addClickListener(event -> this.cmdToggleEdit_buttonClick(event));
		this.tableLine.addItemClickListener(event -> this.tableLine_itemClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdUpdate, cmdReload, cmdCopyExpenses, cmdReport, cmdZipReport, cmdInfo,
			cmdNewExpense, cmdDeleteExpense, cmdUpdateExpense, cmdCopySingle, cmdInfoExpense, cmdToggleEdit;
	private XdevHorizontalLayout actionLayout, horizontalLayout;
	private XdevTable<Expense> tableLine;
	private XdevTabSheet tabSheet;
	private XdevVerticalLayout verticalLayoutLeft, verticalLayoutRight, verticalLayoutExpense;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	private XdevTable<Periode> table;
	// </generated-code>

}
