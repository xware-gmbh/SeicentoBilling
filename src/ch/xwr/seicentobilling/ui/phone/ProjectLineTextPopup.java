package ch.xwr.seicentobilling.ui.phone;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLine_;

public class ProjectLineTextPopup extends XdevView {
	@NavigationParameter
	private final Project project;

	/**
	 *
	 */
	public ProjectLineTextPopup() {
		super();
		this.initUI();

		this.project = (Project) UI.getCurrent().getSession().getAttribute("project");

		loadTable();
	}


	private void loadTable() {
		final XdevBeanContainer<ProjectLine> myList = this.table.getBeanContainerDataSource();
		myList.removeAll();
		myList.addAll(new ProjectLineDAO().findByProject(this.project));

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("320");
		win.setHeight("480");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectLineTextPopup());

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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdLeft}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdLeft_buttonClick(final Button.ClickEvent event) {
		closeWin();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdLeft = new XdevButton();
		this.label = new XdevLabel();
		this.table = new XdevTable<>();

		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdLeft.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/greenarrow_left32.png"));
		this.label.setStyleName("colored");
		this.label.setValue("Textauswahl");
		this.table.setContainerDataSource(ProjectLine.class, false);
		this.table.setVisibleColumns(ProjectLine_.prlText.getName());
		this.table.setColumnHeader("prlText", "Text");

		this.cmdLeft.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdLeft);
		this.horizontalLayout.setComponentAlignment(this.cmdLeft, Alignment.MIDDLE_LEFT);
		this.horizontalLayout.setExpandRatio(this.cmdLeft, 10.0F);
		this.label.setWidth(100, Unit.PERCENTAGE);
		this.label.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.label);
		this.horizontalLayout.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setExpandRatio(this.label, 10.0F);
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
		this.table.addItemClickListener(event -> this.table_itemClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdLeft;
	private XdevLabel label;
	private XdevHorizontalLayout horizontalLayout;
	private XdevVerticalLayout verticalLayout;
	private XdevTable<ProjectLine> table;
	// </generated-code>

}