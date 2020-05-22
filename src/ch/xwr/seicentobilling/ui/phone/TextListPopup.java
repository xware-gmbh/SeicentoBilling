package ch.xwr.seicentobilling.ui.phone;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLine_;

public class TextListPopup extends XdevView {
	@NavigationParameter
	private final Project project;
	private int target = 0;

	/**
	 *
	 */
	public TextListPopup() {
		super();
		this.initUI();

		this.project = (Project) UI.getCurrent().getSession().getAttribute("project");
		//1=Rapporte, 2=Spesen
		this.target =  (int) UI.getCurrent().getSession().getAttribute("target");

		if (this.target == 1) {
			this.table.setVisible(false);
			this.table.setEnabled(false);

			this.tableExpense.setColumnHeaders("");
			this.tableExpense.setVisible(true);
			this.tableExpense.setEnabled(true);

			loadTableExpense();
		} else {
			this.tableExpense.setVisible(false);
			this.tableExpense.setEnabled(false);

			this.table.setColumnHeaders("");
			this.table.setVisible(true);
			this.table.setEnabled(true);

			loadTableProjectLine();
		}
	}

	private void loadTableProjectLine() {
		final XdevBeanContainer<ProjectLine> myList = this.table.getBeanContainerDataSource();
		myList.removeAll();
		myList.addAll(new ProjectLineDAO().findByProject(this.project));

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
	}

	private void loadTableExpense() {
		final XdevBeanContainer<Expense> myList = this.tableExpense.getBeanContainerDataSource();
		myList.removeAll();
		myList.addAll(new ExpenseDAO().findByProject(this.project));

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("320");
		win.setHeight("480");
		win.center();
		win.setModal(true);
		win.setContent(new TextListPopup());

		return win;
	}

	private void closeWin() {
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		final ProjectLine bean = (ProjectLine) event.getItemId();

		UI.getCurrent().getSession().setAttribute(String.class, "cmdDone");
		UI.getCurrent().getSession().setAttribute("textValue", bean.getPrlText());

		closeWin();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #tableExpense}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableExpense_itemClick(final ItemClickEvent event) {
		final Expense bean = (Expense) event.getItemId();

		UI.getCurrent().getSession().setAttribute(String.class, "cmdDone");
		UI.getCurrent().getSession().setAttribute("textValue", bean.getExpText());

		closeWin();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		closeWin();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.tableExpense = new XdevTable<>();
		this.table = new XdevTable<>();
		this.cmdCancel = new XdevButton();

		this.verticalLayout.setMargin(new MarginInfo(true, false, false, false));
		this.tableExpense.setCaption("Textauswahl Spesen");
		this.tableExpense.setIcon(FontAwesome.COMMENTING);
		this.tableExpense.setContainerDataSource(Expense.class, false);
		this.tableExpense.setVisibleColumns(Expense_.expText.getName());
		this.tableExpense.setColumnHeader("expText", "Spesentext");
		this.table.setCaption("Textauswahl Rapporte");
		this.table.setIcon(FontAwesome.COMMENTING);
		this.table.setContainerDataSource(ProjectLine.class, false);
		this.table.setVisibleColumns(ProjectLine_.prlText.getName());
		this.cmdCancel.setCaption("Button");
		this.cmdCancel.setVisible(false);
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.tableExpense.setSizeFull();
		this.verticalLayout.addComponent(this.tableExpense);
		this.verticalLayout.setComponentAlignment(this.tableExpense, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.tableExpense, 100.0F);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.cmdCancel.setSizeUndefined();
		this.verticalLayout.addComponent(this.cmdCancel);
		this.verticalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.tableExpense.addItemClickListener(event -> this.tableExpense_itemClick(event));
		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdCancel;
	private XdevTable<Expense> tableExpense;
	private XdevVerticalLayout verticalLayout;
	private XdevTable<ProjectLine> table;
	// </generated-code>

}
