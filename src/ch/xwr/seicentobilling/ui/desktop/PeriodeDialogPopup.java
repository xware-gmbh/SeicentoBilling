package ch.xwr.seicentobilling.ui.desktop;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevImage;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTreeTable;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.ExpenseHandler;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;

public class PeriodeDialogPopup extends XdevView {

	/**
	 *
	 */
	public PeriodeDialogPopup() {
		super();
		this.initUI();

		// get Parameter
		final Long fromId = (Long) UI.getCurrent().getSession().getAttribute("fromPerId");
		final Long toId = (Long) UI.getCurrent().getSession().getAttribute("toPerId");

		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");

		final Periode fromBean = getBean(fromId);
		final Periode toBean = getBean(toId);
		fillComboBox(fromBean.getCostAccount());

		if (fromBean != null) {
			this.comboBoxFrom.select(fromBean);
		}
		if (toBean != null) {
			this.comboBoxTo.select(toBean);
		}

	}

	private void fillComboBox(final CostAccount costAccount) {
		final PeriodeDAO dao = new PeriodeDAO();

		this.comboBoxFrom.removeAllItems();
		this.comboBoxFrom.addItems(dao.findByCostAccount(costAccount));

		this.comboBoxTo.removeAllItems();
		this.comboBoxTo.addItems(dao.findByCostAccount(costAccount));
		this.comboBoxFrom.setEnabled(false);
	}

	private Periode getBean(final Long recId) {
		return new PeriodeDAO().find(recId);
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("1400");
		win.setHeight("820");
		win.center();
		win.setModal(true);
		win.setContent(new PeriodeDialogPopup());

		return win;
	}


	private Long getFromId() {
		if (this.comboBoxFrom.getSelectedItem() == null) {
			return new Long(0);
		}
		if (this.comboBoxFrom.getSelectedItem().getBean() == null) {
			return new Long(0);
		}

		return this.comboBoxFrom.getSelectedItem().getBean().getPerId();
	}

	private Long getToId() {
		if (this.comboBoxTo.getSelectedItem() == null) {
			return new Long(0);
		}
		if (this.comboBoxTo.getSelectedItem().getBean() == null) {
			return new Long(0);
		}

		return this.comboBoxTo.getSelectedItem().getBean().getPerId();
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #comboBoxFrom}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxFrom_valueChange(final Property.ValueChangeEvent event) {
		// here we read it....
		if (this.comboBoxFrom.getSelectedItem() == null) {
			Notification.show("Spesen kopieren", "Bitte Periode 'Von' wählen", Notification.Type.WARNING_MESSAGE);
			return;
		}

		final Periode per = this.comboBoxFrom.getSelectedItem().getBean();

		final ExpenseDAO dao = new ExpenseDAO();
		final List<Expense> lst = dao.findByPeriode(per);

		// publish list to grid
		InitTreeGrid(lst);
	}

	private void InitTreeGrid(final List<Expense> lst) {
		//reset
		this.treeGrid.removeAllItems();
		this.treeGrid.removeContainerProperty("");
		this.treeGrid.removeContainerProperty("Datum");
		this.treeGrid.removeContainerProperty("Konto");
		this.treeGrid.removeContainerProperty("Kst");
		this.treeGrid.removeContainerProperty("Typ");
		this.treeGrid.removeContainerProperty("Betrag");
		this.treeGrid.removeContainerProperty("Projekt");
		this.treeGrid.removeContainerProperty("Text");
		this.treeGrid.removeContainerProperty("Objekt");

		//rebuild
		this.treeGrid.addContainerProperty("", XdevCheckBox.class, true);
		this.treeGrid.addContainerProperty("Datum", XdevPopupDateField.class, null);
		this.treeGrid.addContainerProperty("Konto", XdevLabel.class, null);
		this.treeGrid.addContainerProperty("Kst", XdevCheckBox.class, null);
		this.treeGrid.addContainerProperty("Typ", XdevLabel.class, null);
		this.treeGrid.addContainerProperty("Betrag", XdevLabel.class, null);
		this.treeGrid.addContainerProperty("Projekt", XdevLabel.class, null);
		this.treeGrid.addContainerProperty("Text", XdevLabel.class, null);
		this.treeGrid.addContainerProperty("Objekt", Expense.class, null);

		int icount = 0;
		for (final Expense expObj : lst) {
			final Object[] parent = getGridLine(expObj);
			this.treeGrid.addItem(parent, icount);

			icount++;
		}

		// Collapse the tree
		for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
			this.treeGrid.setCollapsed(itemId, true);

		    // As we're at it, also disallow children from
		    // the current leaves
		    if (! this.treeGrid.hasChildren(itemId)) {
				this.treeGrid.setChildrenAllowed(itemId, true);
			}
		}

		this.treeGrid.setVisibleColumns("", "Datum", "Konto", "Kst", "Typ", "Betrag", "Projekt", "Text");
		this.treeGrid.setColumnAlignments(Align.LEFT,Align.LEFT,Align.LEFT,Align.LEFT,Align.LEFT,Align.RIGHT,Align.LEFT,Align.LEFT);
		this.treeGrid.setEditable(true);
		//this.treeGrid.setColumnCollapsingAllowed(false);
	}

	private Object[] getGridLine(final Expense expObj) {
		final XdevCheckBox cbo = new XdevCheckBox();
		cbo.setValue(true);

		final XdevPopupDateField expDate = new XdevPopupDateField();
		expDate.setValue(expObj.getExpDate());
		final XdevLabel expAcc = new XdevLabel(expObj.getExpAccount());
		final XdevLabel proName = new XdevLabel(expObj.getProject().getProName());
		final XdevLabel amount = new XdevLabel(getAmtString(expObj.getExpAmount(), true));
		final XdevCheckBox kst = new XdevCheckBox();
		kst.setValue(expObj.getExpFlagCostAccount().booleanValue());
		kst.setEnabled(false);
		final XdevLabel type = new XdevLabel(expObj.getExpFlagGeneric().name());
		final XdevLabel text = new XdevLabel(expObj.getExpText());

		final Object[] retval = new Object[] {cbo, expDate, expAcc, kst, type, amount, proName, text, expObj};
		return retval;
	}

	private String getAmtString(final Double amount, final boolean currency) {
		final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
		final String numberAsString = "            " + decimalFormat.format(amount);
		final int ilen = numberAsString.length();
		final String retval = numberAsString.substring(ilen - 11);
		if (currency ) {
			return "CHF" + retval;
		}
		return retval;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdOk}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdOk_buttonClick(final Button.ClickEvent event) {
		if (getToId() == 0) {
			Notification.show("Ungültige Zielperiode", "Ziel (0) unbekannt", Notification.Type.ERROR_MESSAGE);
			return;
		}

		if (LovState.BookingType.gebucht.equals(this.comboBoxTo.getSelectedItem().getBean().getPerBookedExpense())) {
			Notification.show("Ungültige Zielperiode", "Bereits verbucht", Notification.Type.ERROR_MESSAGE);
			return;
		}

		final boolean ret = performCopy();
		if (ret) {
			UI.getCurrent().getSession().setAttribute(String.class, "cmdOk");
			UI.getCurrent().getSession().setAttribute("fromPerId", getFromId());
			UI.getCurrent().getSession().setAttribute("toPerId", getToId());
		}

		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
		((Window) this.getParent()).close();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdToogle}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdToogle_buttonClick(final Button.ClickEvent event) {
		for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
			final Item item = this.treeGrid.getItem(itemId);
			final XdevCheckBox cbo = (XdevCheckBox) item.getItemProperty("").getValue();

			if (cbo.isEnabled()) {
				cbo.setValue(!cbo.getValue());
			}
     	}

	}

	private boolean performCopy() {
		final ExpenseHandler hdl = new ExpenseHandler();
		final Periode perT = this.comboBoxTo.getSelectedItem().getBean();
		final Periode perF = this.comboBoxFrom.getSelectedItem().getBean();
		final boolean checkit = this.checkBoxEmpty.getValue().booleanValue();
		final boolean guessDate = this.checkBoxTargetDate.getValue().booleanValue();
		int icount = 0;

		try {
			hdl.validateInput(perF, perT, checkit);
			for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
				final Item item = this.treeGrid.getItem(itemId);
				final XdevCheckBox cbo = (XdevCheckBox) item.getItemProperty("").getValue();

				if (cbo.getValue().booleanValue()) {
					final Expense exp = (Expense) item.getItemProperty("Objekt").getValue();
					final XdevPopupDateField dfield = (XdevPopupDateField) item.getItemProperty("Datum").getValue();
					final Date targetDate = dfield.getValue();

					hdl.copyExpenseRecord(exp, perF, perT, guessDate, targetDate);
					icount++;
				}
	     	}

			if (icount > 0) {
				Notification.show("Spesen kopieren", icount + " Datensätze wurden kopiert", Notification.Type.TRAY_NOTIFICATION);
			} else {
				Notification.show("Spesen kopieren", "Es wurden keine Daten kopiert!", Notification.Type.WARNING_MESSAGE);
			}

			return true;
		} catch (final Exception e) {
			Notification.show("Spesen kopieren", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}

		return false;
	}
	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.gridLayout = new XdevGridLayout();
		this.image = new XdevImage();
		this.labelTitle = new XdevLabel();
		this.label2 = new XdevLabel();
		this.comboBoxFrom = new XdevComboBox<>();
		this.label = new XdevLabel();
		this.comboBoxTo = new XdevComboBox<>();
		this.checkBoxEmpty = new XdevCheckBox();
		this.checkBoxTargetDate = new XdevCheckBox();
		this.horizontalLayoutButtons = new XdevHorizontalLayout();
		this.cmdOk = new XdevButton();
		this.cmdCancel = new XdevButton();
		this.cmdToogle = new XdevButton();
		this.verticalLayoutData = new XdevVerticalLayout();
		this.treeGrid = new XdevTreeTable();

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(33.0F, Unit.PERCENTAGE);
		this.panel.setCaption("Spesen kopieren");
		this.panel.setTabIndex(0);
		this.panel.setStyleName("bar closable dark");
		this.verticalLayout.setSpacing(false);
		this.image.setSource(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/copy1.png"));
		this.labelTitle.setStyleName("h2");
		this.labelTitle.setValue("Spesen kopieren...");
		this.label2.setValue("Von Periode");
		this.comboBoxFrom.setRequired(true);
		this.comboBoxFrom.setItemCaptionFromAnnotation(false);
		this.comboBoxFrom.setContainerDataSource(Periode.class, DAOs.get(PeriodeDAO.class).findAll());
		this.comboBoxFrom.setItemCaptionPropertyId(Periode_.perName.getName());
		this.label.setValue("Nach Periode");
		this.comboBoxTo.setRequired(true);
		this.comboBoxTo.setItemCaptionFromAnnotation(false);
		this.comboBoxTo.setContainerDataSource(Periode.class, DAOs.get(PeriodeDAO.class).findAll());
		this.comboBoxTo.setItemCaptionPropertyId(Periode_.perName.getName());
		this.checkBoxEmpty.setCaption("Zielperiode muss leer sein!");
		this.checkBoxEmpty.setValue(true);
		this.checkBoxTargetDate.setCaption("Zieldatum berechnen");
		this.checkBoxTargetDate
				.setDescription("Das Datum in der Zielperiode wird berchnet, wenn dieses nicht mutiert wird.");
		this.checkBoxTargetDate.setValue(true);
		this.horizontalLayoutButtons.setMargin(new MarginInfo(false, true, false, false));
		this.cmdOk.setCaption("Kopieren");
		this.cmdOk.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdCancel.setCaption("Abbrechen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.cmdToogle.setCaption("Select / Unselect");
		this.verticalLayoutData.setMargin(new MarginInfo(false, false, true, false));

		this.gridLayout.setColumns(2);
		this.gridLayout.setRows(6);
		this.image.setWidth(60, Unit.PIXELS);
		this.image.setHeight(60, Unit.PIXELS);
		this.gridLayout.addComponent(this.image, 0, 0);
		this.labelTitle.setSizeUndefined();
		this.gridLayout.addComponent(this.labelTitle, 1, 0);
		this.label2.setSizeUndefined();
		this.gridLayout.addComponent(this.label2, 0, 1);
		this.comboBoxFrom.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxFrom.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.comboBoxFrom, 1, 1);
		this.label.setSizeUndefined();
		this.gridLayout.addComponent(this.label, 0, 2);
		this.comboBoxTo.setSizeUndefined();
		this.gridLayout.addComponent(this.comboBoxTo, 1, 2);
		this.checkBoxEmpty.setSizeUndefined();
		this.gridLayout.addComponent(this.checkBoxEmpty, 1, 3);
		this.checkBoxTargetDate.setSizeUndefined();
		this.gridLayout.addComponent(this.checkBoxTargetDate, 1, 4);
		this.gridLayout.setColumnExpandRatio(1, 10.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 5, 1, 5);
		this.gridLayout.setRowExpandRatio(5, 1.0F);
		this.cmdOk.setSizeUndefined();
		this.horizontalLayoutButtons.addComponent(this.cmdOk);
		this.horizontalLayoutButtons.setComponentAlignment(this.cmdOk, Alignment.MIDDLE_LEFT);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayoutButtons.addComponent(this.cmdCancel);
		this.horizontalLayoutButtons.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_CENTER);
		this.cmdToogle.setSizeUndefined();
		this.horizontalLayoutButtons.addComponent(this.cmdToogle);
		this.horizontalLayoutButtons.setComponentAlignment(this.cmdToogle, Alignment.MIDDLE_RIGHT);
		this.gridLayout.setSizeUndefined();
		this.verticalLayout.addComponent(this.gridLayout);
		this.verticalLayout.setComponentAlignment(this.gridLayout, Alignment.MIDDLE_LEFT);
		this.horizontalLayoutButtons.setSizeUndefined();
		this.verticalLayout.addComponent(this.horizontalLayoutButtons);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutButtons, Alignment.MIDDLE_LEFT);
		final CustomComponent verticalLayout_spacer = new CustomComponent();
		verticalLayout_spacer.setSizeFull();
		this.verticalLayout.addComponent(verticalLayout_spacer);
		this.verticalLayout.setExpandRatio(verticalLayout_spacer, 1.0F);
		this.verticalLayout.setSizeFull();
		this.panel.setContent(this.verticalLayout);
		this.treeGrid.setSizeFull();
		this.verticalLayoutData.addComponent(this.treeGrid);
		this.verticalLayoutData.setComponentAlignment(this.treeGrid, Alignment.MIDDLE_CENTER);
		this.verticalLayoutData.setExpandRatio(this.treeGrid, 100.0F);
		this.panel.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.panel);
		this.verticalLayoutData.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.verticalLayoutData);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.comboBoxFrom.addValueChangeListener(event -> this.comboBoxFrom_valueChange(event));
		this.cmdOk.addClickListener(event -> this.cmdOk_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
		this.cmdToogle.addClickListener(event -> this.cmdToogle_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel labelTitle, label2, label;
	private XdevButton cmdOk, cmdCancel, cmdToogle;
	private XdevImage image;
	private XdevHorizontalLayout horizontalLayoutButtons;
	private XdevPanel panel;
	private XdevCheckBox checkBoxEmpty, checkBoxTargetDate;
	private XdevTreeTable treeGrid;
	private XdevGridLayout gridLayout;
	private XdevVerticalLayout verticalLayout, verticalLayoutData;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevComboBox<Periode> comboBoxFrom, comboBoxTo;
	// </generated-code>

}
