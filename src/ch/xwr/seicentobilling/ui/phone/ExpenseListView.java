package ch.xwr.seicentobilling.ui.phone;

import java.util.Date;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.navigation.Navigation;
import com.xdev.ui.navigation.NavigationParameter;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.Periode;

public class ExpenseListView extends XdevView {
	@NavigationParameter
	private Periode periode;

	/**
	 *
	 */
	public ExpenseListView() {
		super();
		this.initUI();
	}

	@Override
	public void enter(final ViewChangeListener.ViewChangeEvent event) {
		super.enter(event);

		this.periode = Navigation.getParameter(event, "periode", Periode.class);

		loadTable();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdLeft}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdLeft_buttonClick(final Button.ClickEvent event) {
		Navigation.to("periodeView").parameter("string", "Expense").navigate();
	}

	private void loadTable() {

		final XdevBeanContainer<Expense> myList = this.table.getBeanContainerDataSource();
		myList.removeAll();
		myList.addAll(new ExpenseDAO().findByPeriode(this.periode));

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		if (isBooked()) {
			Notification.show("Daten sind bereits verbucht", "Mutation nicht möglich", Notification.Type.ERROR_MESSAGE);
		} else {
			final Expense bean = (Expense) event.getItemId();
			Navigation.to("expenseView").parameter("expense", bean).parameter("periode", this.periode).navigate();
		}
	}

	private boolean isBooked() {
		if (LovState.BookingType.gebucht.equals(this.periode.getPerBookedExpense())) {
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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		if (isBooked()) {
			Notification.show("Daten sind bereits verbucht", "Mutation nicht möglich", Notification.Type.ERROR_MESSAGE);
		} else {
			final Expense bean = getNewDefaultBean();
			Navigation.to("expenseView").parameter("expense", bean).parameter("periode", this.periode).navigate();
		}
	}

	private Expense getNewDefaultBean() {
		final Expense bean = new Expense();
		bean.setExpState(LovState.State.active);
		// bean.setPrlWorkType(LovState.WorkType.project);
		bean.setExpDate(new Date());
		bean.setExpUnit(LovState.ExpUnit.stück);
		bean.setExpQuantity(new Double(1));
		bean.setExpFlagGeneric(LovState.ExpType.standard);
		bean.setExpFlagCostAccount(true);
		bean.setPeriode(this.periode);
		return bean;
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated
	 * by the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdLeft = new XdevButton();
		this.label = new XdevLabel();
		this.cmdNew = new XdevButton();
		this.table = new XdevTable<>();

		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdLeft.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.cmdLeft.setCaption("Back");
		this.label.setStyleName("h3 h4");
		this.label.setValue("Spesenliste");
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1.png"));
		this.cmdNew.setCaption("Neu");
		this.table.setContainerDataSource(Expense.class, false);
		this.table.setVisibleColumns(Expense_.expDate.getName(), Expense_.expAmount.getName(),
				Expense_.expQuantity.getName(), Expense_.expText.getName(), Expense_.expUnit.getName());
		this.table.setColumnHeader("expDate", "Datum");
		this.table.setConverter("expDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.table.setColumnHeader("expAmount", "Betrag");
		this.table.setColumnAlignment("expAmount", Table.Align.RIGHT);
		this.table.setConverter("expAmount", ConverterBuilder.stringToDouble().currency().build());
		this.table.setColumnHeader("expQuantity", "Menge");
		this.table.setColumnHeader("expText", "Text");
		this.table.setColumnHeader("expUnit", "Einheit");

		this.cmdLeft.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdLeft);
		this.horizontalLayout.setComponentAlignment(this.cmdLeft, Alignment.MIDDLE_LEFT);
		this.horizontalLayout.setExpandRatio(this.cmdLeft, 10.0F);
		this.label.setSizeUndefined();
		this.horizontalLayout.addComponent(this.label);
		this.horizontalLayout.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.cmdNew.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdNew);
		this.horizontalLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_RIGHT);
		this.horizontalLayout.setExpandRatio(this.cmdNew, 10.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.cmdLeft.addClickListener(event -> this.cmdLeft_buttonClick(event));
		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.table.addItemClickListener(event -> this.table_itemClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdLeft, cmdNew;
	private XdevLabel label;
	private XdevHorizontalLayout horizontalLayout;
	private XdevTable<Expense> table;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
