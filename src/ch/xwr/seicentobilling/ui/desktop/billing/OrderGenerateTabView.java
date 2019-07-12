package ch.xwr.seicentobilling.ui.desktop.billing;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table.Align;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevTreeTable;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanItemContainer;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.LovState.BookingType;
import ch.xwr.seicentobilling.business.OrderGenerator;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.RowObjectAddonHandler;
import ch.xwr.seicentobilling.business.model.billing.BillDto;
import ch.xwr.seicentobilling.business.model.billing.BillLine;
import ch.xwr.seicentobilling.business.model.billing.GuiGeneratorFields;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;

public class OrderGenerateTabView extends XdevView {
	private final GuiGeneratorFields guifld = new GuiGeneratorFields();
	private CostAccount user = null;

	/**
	 *
	 */
	public OrderGenerateTabView() {
		super();
		this.initUI();

		loadFields();
		loadParams();
	}


	private void loadParams() {
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(null);  //company Level
		final RowObjectAddonHandler objUsr = new RowObjectAddonHandler(this.user.getCsaId(), "CostAccount");  //userlevel

		//Order (Header)
		this.textFieldOrderText.setValue(getTextParams(objman, objUsr, "headerText"));

		//project
		this.textFieldProjectLine.setValue(getTextParams(objman, objUsr, "lineTextProject"));
		//expense
		this.textFieldExpenseLine.setValue(getTextParams(objman, objUsr, "lineTextExpense"));
		//journey
		this.textFieldJourneyLine.setValue(getTextParams(objman, objUsr, "lineTextJourney"));

		//items
		final ch.xwr.seicentobilling.entities.Item itm1 = SearchItem(objman, "itemIdentProject");
		this.textFieldItemProject.setValue(itm1.getPrpShortName());
		//
		final ch.xwr.seicentobilling.entities.Item itm2 = SearchItem(objman, "itemIdentExpense");
		this.textFieldItemExpense.setValue(itm2.getPrpShortName());
		//
		final ch.xwr.seicentobilling.entities.Item itm3 = SearchItem(objman, "itemIdentJourney");
		this.textFieldItemJourney.setValue(itm3.getPrpShortName());
		//
		final String cbxValue = objUsr.getRowParameter("billing", "generator", "cbxLastText");
		if (cbxValue.toLowerCase().equals("true")) {
			this.checkBoxTextLast.setValue(true);
		}


		this.gridLayoutArtikel.setEnabled(false);
		this.gridLayoutArtikel.setReadOnly(true);

		//init model
		this.guifld.setItemProject(itm1);
		this.guifld.setItemExpense(itm2);
		this.guifld.setItemJourney(itm3);

	}


	private String getTextParams(final RowObjectAddonHandler objman, final RowObjectAddonHandler objUsr, final String key) {
		String txtValue = objUsr.getRowParameter("billing", "generator", key);

		if (txtValue == null || txtValue.isEmpty()) {
			txtValue = objman.getRowParameter("billing", "generator", key);
		}

		return txtValue;
	}


	private ch.xwr.seicentobilling.entities.Item SearchItem(final RowObjectAddonHandler objman, final String key) {
		final String value = objman.getRowParameter("billing", "generator", key);

		final ItemDAO dao = new ItemDAO();
		final List<ch.xwr.seicentobilling.entities.Item> lst = dao.findByIdent(value);
		if (lst != null) {
			return lst.get(0);
		}
		return null;
	}


	private void loadFields() {
        final CostAccountDAO dao = new CostAccountDAO();
		final List<CostAccount> ls1 = dao.findAll();

		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = ls1.get(0); // Dev Mode
		}
		this.user = bean;

		final XdevBeanItemContainer<CostAccount> myCustomerList = new XdevBeanItemContainer<>(CostAccount.class);
		myCustomerList.addAll(ls1);
		this.comboBoxCostAccount.setContainerDataSource(myCustomerList);

		if (this.comboBoxCostAccount.containsId(bean)) {
			this.comboBoxCostAccount.select(bean);
		} else {
			this.comboBoxCostAccount.setValue(bean);
		}

		this.dateBilldate.setValue(new Date());
	}



	private void InitTreeGrid(final List<BillDto> lst) {
		//reset
		this.treeGrid.removeAllItems();
		this.treeGrid.removeContainerProperty("");
		this.treeGrid.removeContainerProperty("Kunde");
		this.treeGrid.removeContainerProperty("Projekt");
		this.treeGrid.removeContainerProperty("Betrag");
		this.treeGrid.removeContainerProperty("L-Rechnung");
		this.treeGrid.removeContainerProperty("Objekt");

		//rebuild
		this.treeGrid.addContainerProperty("", XdevCheckBox.class, true);
		this.treeGrid.addContainerProperty("Kunde", String.class, null);
		this.treeGrid.addContainerProperty("Projekt", String.class, null);
		this.treeGrid.addContainerProperty("Betrag", String.class, null);
		this.treeGrid.addContainerProperty("L-Rechnung", String.class, null);
		this.treeGrid.addContainerProperty("Objekt", BillDto.class, null);

		Object[] detail = null;
		int icount = 0;
		for (final BillDto billDto : lst) {
			final Object[] parent = getParentLine(billDto);
			this.treeGrid.addItem(parent, icount);
			final int iParent = icount;

			detail = getDetailGridLine("Spesen", billDto.getExpenseHours());
			if (detail != null) {
				icount++;
				this.treeGrid.addItem(detail, icount);
				this.treeGrid.setParent(icount, iParent);

			}
			detail = getDetailGridLine("Reisezeit", billDto.getJourneyHours());
			if (detail != null) {
				icount++;
				this.treeGrid.addItem(detail, icount);
				this.treeGrid.setParent(icount, iParent);
			}
			detail = getDetailGridLine("Dienstleistung", billDto.getProjectHours());
			if (detail != null) {
				icount++;
				this.treeGrid.addItem(detail, icount);
				this.treeGrid.setParent(icount, iParent);
			}

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

		this.treeGrid.setVisibleColumns("", "Kunde", "Projekt", "Betrag", "L-Rechnung");
		this.treeGrid.setColumnAlignments(Align.LEFT,Align.LEFT,Align.LEFT,Align.RIGHT,Align.LEFT);
	}


	private Object[] getParentLine(final BillDto billDto) {
		final XdevCheckBox cbo = new XdevCheckBox();
		cbo.setValue(true);
		if (billDto.getProject().getInternal() == null || billDto.getProject().getInternal()) {
			cbo.setValue(false);
			cbo.setEnabled(false);
		}
		final String cusName = billDto.getCustomer().getFullname();
		final String proName = billDto.getProject().getProName();
		final String amount = getAmtString(billDto.getTotalAmount(), true);
		final String ldate = getLastBillDate(billDto);

		final Object[] retval = new Object[] {cbo, cusName, proName, amount, ldate, billDto};
		return retval;
	}


	private String getLastBillDate(final BillDto billDto) {
		final String pattern = "dd.MM.yyyy";
		final DateFormat df = new SimpleDateFormat(pattern);


		final OrderDAO dao = new OrderDAO();
		final List<Order> lst = dao.findByCustomer(billDto.getCustomer());
		for (final Iterator<Order> iterator = lst.iterator(); iterator.hasNext();) {
			final Order order = iterator.next();
			if (order.getProject() != null) {
				if (order.getProject().getProId().equals(billDto.getProject().getProId())) {
					return df.format(order.getOrdBillDate());
				}
			}
		}

		return "";
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


	private Object[] getDetailGridLine(final String text, final List<BillLine> list) {
		Double amt = new Double(0);
		if (list!= null && ! list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				final BillLine tmp = list.get(i);
				amt = amt + tmp.getAmount();
			}
		}

		if (amt > 0) {
			final String samt = getAmtString(amt, false);
			final Object[] retval = new Object[]{null, null, text, samt, null, null};
			return retval;
		}

		return null;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdGenerate}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdGenerate_buttonClick(final Button.ClickEvent event) {
		if (isModelValid()) {
			int icount = 0;
			String billnbrs = "";
			final OrderGenerator gen = new OrderGenerator();
			for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
				final Item item = this.treeGrid.getItem(itemId);
				final XdevCheckBox cbo = (XdevCheckBox) item.getItemProperty("").getValue();
				final BillDto bill = (BillDto) item.getItemProperty("Objekt").getValue();

				if (cbo != null && cbo.getValue() && bill != null) {
					final Order newOrd = gen.createBill(bill, this.guifld);
					//System.out.println("Order created " + newOrd.getOrdNumber());
					icount++;

					if (icount == 1) {
						billnbrs = "" + newOrd.getOrdNumber();
					} else {
						billnbrs = billnbrs + ", " + newOrd.getOrdNumber();
					}
				}
	     	}

			Notification.show("Rechnungen generieren", "" + icount + " Rechnung(en) erstellt mit Nr: " + billnbrs + ".",
					Notification.Type.HUMANIZED_MESSAGE);

			markPeriodeAsGenerated();

		} else {
			Notification.show("Rechnungen generieren", "ungültige Parameter - Generierung kann nicht starten.",
					Notification.Type.WARNING_MESSAGE);

		}
	}

	private void markPeriodeAsGenerated() {
		final PeriodeDAO dao = new PeriodeDAO();

		final Periode per = this.comboBoxPeriode.getSelectedItem().getBean();
		per.setPerBookedProject(BookingType.gebucht);
		dao.save(per);
	}


	private boolean isModelValid() {
		this.guifld.setBillDate(this.dateBilldate.getValue());
		this.guifld.setBillText(this.textFieldOrderText.getValue());
		this.guifld.setCopyTextFromLastBill(this.checkBoxTextLast.getValue());
		this.guifld.setLineTextExpense(this.textFieldExpenseLine.getValue());
		this.guifld.setLineTextJourney(this.textFieldJourneyLine.getValue());
		this.guifld.setLineTextProject(this.textFieldProjectLine.getValue());

		if (this.guifld.getBillDate() == null) {
			return false;
		}
		if (this.guifld.getItemExpense() == null) {
			return false;
		}
		if (this.guifld.getItemJourney() == null) {
			return false;
		}
		if (this.guifld.getItemProject() == null) {
			return false;
		}

		return true;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdPropose}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPropose_buttonClick(final Button.ClickEvent event) {
		// here we read it....
		if (this.comboBoxCostAccount.getSelectedItem() == null || this.comboBoxPeriode.getSelectedItem() == null) {
			Notification.show("Rechnungen generieren", "Bitte Periode wählen", Notification.Type.WARNING_MESSAGE);
			return;
		}

		final Periode per = this.comboBoxPeriode.getSelectedItem().getBean();
		final OrderGenerator gen = new OrderGenerator();
		final List<BillDto> lst = gen.proposeDraft(per);

		if (per.getPerBookedProject().equals(BookingType.gebucht)) {
			Notification.show("Rechnungsvorschlag", "Für diese Periode wurden bereits einmal Rechnungen generiert!!", Notification.Type.TRAY_NOTIFICATION);
		}

		// publish list to grid
		InitTreeGrid(lst);
		// this.table.
		final Collection<?> lsSize = this.treeGrid.getContainerDataSource().getItemIds();
		if (lsSize.size() > 0) {
			this.cmdGenerate.setEnabled(true);
		} else {
			this.cmdGenerate.setEnabled(false);
		}

	}


	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #comboBoxCostAccount}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxCostAccount_valueChange(final Property.ValueChangeEvent event) {
		final CostAccount cst = (CostAccount) event.getProperty().getValue();
		// if (this.comboBoxCostAccount.getSelectedItem() != null) {
		if (cst != null) {
			final CostAccount bean = this.comboBoxCostAccount.getSelectedItem().getBean();

			// this.comboBoxPeriode.getContainerDataSource().removeAllItems();

			final XdevBeanItemContainer<Periode> myList = new XdevBeanItemContainer<>(Periode.class);
			myList.addAll(new PeriodeDAO().findByCostAccount(bean));
			this.comboBoxPeriode.setContainerDataSource(myList);

			final Periode beanp = myList.firstItemId();
			if (beanp != null) {
				this.comboBoxPeriode.select(beanp);
			}
		}

	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdSaveText}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSaveText_buttonClick(final Button.ClickEvent event) {
		//save text to User
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(this.user.getCsaId(), "CostAccount");

		objman.putRowParameter("billing", "generator", "headerText", this.textFieldOrderText.getValue());
		objman.putRowParameter("billing", "generator", "lineTextProject", this.textFieldProjectLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextExpense", this.textFieldExpenseLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextJourney", this.textFieldJourneyLine.getValue());

		objman.putRowParameter("billing", "generator", "cbxLastText", this.checkBoxTextLast.getValue().toString());
	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayoutLeft = new XdevVerticalLayout();
		this.panel = new XdevPanel();
		this.tabSheet = new XdevTabSheet();
		this.gridLayout = new XdevGridLayout();
		this.label = new XdevLabel();
		this.comboBoxCostAccount = new XdevComboBox<>();
		this.label2 = new XdevLabel();
		this.comboBoxPeriode = new XdevComboBox<>();
		this.label3 = new XdevLabel();
		this.dateBilldate = new XdevPopupDateField();
		this.checkBoxTextLast = new XdevCheckBox();
		this.cmdPropose = new XdevButton();
		this.cmdGenerate = new XdevButton();
		this.label4 = new XdevLabel();
		this.gridLayoutTexte = new XdevGridLayout();
		this.labelInfoToolTip = new XdevLabel();
		this.label5 = new XdevLabel();
		this.textFieldOrderText = new XdevTextField();
		this.label6 = new XdevLabel();
		this.textFieldProjectLine = new XdevTextField();
		this.label7 = new XdevLabel();
		this.textFieldExpenseLine = new XdevTextField();
		this.label8 = new XdevLabel();
		this.textFieldJourneyLine = new XdevTextField();
		this.cmdSaveText = new XdevButton();
		this.gridLayoutArtikel = new XdevGridLayout();
		this.label11 = new XdevLabel();
		this.textFieldItemProject = new XdevTextField();
		this.label12 = new XdevLabel();
		this.textFieldItemExpense = new XdevTextField();
		this.label13 = new XdevLabel();
		this.textFieldItemJourney = new XdevTextField();
		this.verticalLayoutRight = new XdevVerticalLayout();
		this.treeGrid = new XdevTreeTable();

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(35.0F, Unit.PERCENTAGE);
		this.verticalLayoutLeft.setMargin(new MarginInfo(false));
		this.panel.setCaption("Rechnungen generieren");
		this.tabSheet.setStyleName("framed");
		this.label.setValue("Kostenstelle");
		this.comboBoxCostAccount.setContainerDataSource(CostAccount.class, false);
		this.label2.setValue("Periode wählen");
		this.comboBoxPeriode.setContainerDataSource(Periode.class, false);
		this.label3.setValue("Rechnungsdatum");
		this.checkBoxTextLast.setCaption("Text von letzer Rechnung");
		this.cmdPropose.setCaption("Vorschlag generieren");
		this.cmdGenerate.setCaption("Rechnungen erstellen");
		this.cmdGenerate.setEnabled(false);
		this.label4.setIcon(FontAwesome.INFO_CIRCLE);
		this.label4.setValue("MwSt-Ansatz wird für alle Positionen vom Projekt übernommen!");
		this.labelInfoToolTip.setIcon(FontAwesome.INFO_CIRCLE);
		this.labelInfoToolTip.setDescription("Es ist ein Tooltip vorhanden auf den Labeln und dem Button.");
		this.label5.setDescription(
				"Rechnungstext - mögliche Platzhalter {proExtReference} {proName}, {proContact}, {csaName}, {perYear}, {perMonth}, {perMonthText}");
		this.label5.setValue("Textzeile Rechnung");
		this.label6.setDescription("mögliche Platzhalter: {csaName}, {csaCode}, , {perYear}, {perMonth}, {perMonthText}");
		this.label6.setValue("Textzeile Dienstleistung");
		this.label7.setDescription("siehe Dienstleistung");
		this.label7.setValue("Textzeile Spesen");
		this.label8.setDescription("siehe Dienstleistung");
		this.label8.setValue("Textzeile Reisezeit");
		this.cmdSaveText.setCaption("Texte speichern");
		this.cmdSaveText.setDescription("Speichert die Texte als persönliche Einstellungen.");
		this.label11.setValue("Artikel Dienstleistung");
		this.label12.setValue("Artikel Spesen");
		this.label13.setValue("Artikel Reisezeit");
		this.verticalLayoutRight.setMargin(new MarginInfo(false, false, true, false));
		this.treeGrid.setSelectable(true);
		this.treeGrid.setMultiSelect(true);

		this.gridLayout.setColumns(3);
		this.gridLayout.setRows(8);
		this.label.setSizeUndefined();
		this.gridLayout.addComponent(this.label, 0, 0);
		this.comboBoxCostAccount.setSizeUndefined();
		this.gridLayout.addComponent(this.comboBoxCostAccount, 1, 0);
		this.label2.setSizeUndefined();
		this.gridLayout.addComponent(this.label2, 0, 1);
		this.comboBoxPeriode.setSizeUndefined();
		this.gridLayout.addComponent(this.comboBoxPeriode, 1, 1);
		this.label3.setSizeUndefined();
		this.gridLayout.addComponent(this.label3, 0, 2);
		this.dateBilldate.setSizeUndefined();
		this.gridLayout.addComponent(this.dateBilldate, 1, 2);
		this.checkBoxTextLast.setSizeUndefined();
		this.gridLayout.addComponent(this.checkBoxTextLast, 1, 3);
		this.cmdPropose.setSizeUndefined();
		this.gridLayout.addComponent(this.cmdPropose, 1, 4);
		this.cmdGenerate.setSizeUndefined();
		this.gridLayout.addComponent(this.cmdGenerate, 1, 5);
		this.label4.setWidth(100, Unit.PERCENTAGE);
		this.label4.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.label4, 0, 6, 1, 6);
		final CustomComponent gridLayout_hSpacer = new CustomComponent();
		gridLayout_hSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_hSpacer, 2, 0, 2, 6);
		this.gridLayout.setColumnExpandRatio(2, 1.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 7, 1, 7);
		this.gridLayout.setRowExpandRatio(7, 1.0F);
		this.gridLayoutTexte.setColumns(2);
		this.gridLayoutTexte.setRows(7);
		this.labelInfoToolTip.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.labelInfoToolTip, 0, 0);
		this.label5.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.label5, 0, 1);
		this.textFieldOrderText.setWidth(100, Unit.PERCENTAGE);
		this.textFieldOrderText.setHeight(-1, Unit.PIXELS);
		this.gridLayoutTexte.addComponent(this.textFieldOrderText, 1, 1);
		this.label6.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.label6, 0, 2);
		this.textFieldProjectLine.setWidth(100, Unit.PERCENTAGE);
		this.textFieldProjectLine.setHeight(-1, Unit.PIXELS);
		this.gridLayoutTexte.addComponent(this.textFieldProjectLine, 1, 2);
		this.label7.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.label7, 0, 3);
		this.textFieldExpenseLine.setWidth(100, Unit.PERCENTAGE);
		this.textFieldExpenseLine.setHeight(-1, Unit.PIXELS);
		this.gridLayoutTexte.addComponent(this.textFieldExpenseLine, 1, 3);
		this.label8.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.label8, 0, 4);
		this.textFieldJourneyLine.setWidth(100, Unit.PERCENTAGE);
		this.textFieldJourneyLine.setHeight(-1, Unit.PIXELS);
		this.gridLayoutTexte.addComponent(this.textFieldJourneyLine, 1, 4);
		this.cmdSaveText.setSizeUndefined();
		this.gridLayoutTexte.addComponent(this.cmdSaveText, 1, 5);
		this.gridLayoutTexte.setColumnExpandRatio(1, 10.0F);
		final CustomComponent gridLayoutTexte_vSpacer = new CustomComponent();
		gridLayoutTexte_vSpacer.setSizeFull();
		this.gridLayoutTexte.addComponent(gridLayoutTexte_vSpacer, 0, 6, 1, 6);
		this.gridLayoutTexte.setRowExpandRatio(6, 1.0F);
		this.gridLayoutArtikel.setColumns(2);
		this.gridLayoutArtikel.setRows(4);
		this.label11.setSizeUndefined();
		this.gridLayoutArtikel.addComponent(this.label11, 0, 0);
		this.textFieldItemProject.setWidth(100, Unit.PERCENTAGE);
		this.textFieldItemProject.setHeight(-1, Unit.PIXELS);
		this.gridLayoutArtikel.addComponent(this.textFieldItemProject, 1, 0);
		this.label12.setSizeUndefined();
		this.gridLayoutArtikel.addComponent(this.label12, 0, 1);
		this.textFieldItemExpense.setWidth(100, Unit.PERCENTAGE);
		this.textFieldItemExpense.setHeight(-1, Unit.PIXELS);
		this.gridLayoutArtikel.addComponent(this.textFieldItemExpense, 1, 1);
		this.label13.setSizeUndefined();
		this.gridLayoutArtikel.addComponent(this.label13, 0, 2);
		this.textFieldItemJourney.setWidth(100, Unit.PERCENTAGE);
		this.textFieldItemJourney.setHeight(-1, Unit.PIXELS);
		this.gridLayoutArtikel.addComponent(this.textFieldItemJourney, 1, 2);
		this.gridLayoutArtikel.setColumnExpandRatio(1, 10.0F);
		final CustomComponent gridLayoutArtikel_vSpacer = new CustomComponent();
		gridLayoutArtikel_vSpacer.setSizeFull();
		this.gridLayoutArtikel.addComponent(gridLayoutArtikel_vSpacer, 0, 3, 1, 3);
		this.gridLayoutArtikel.setRowExpandRatio(3, 1.0F);
		this.gridLayout.setSizeFull();
		this.tabSheet.addTab(this.gridLayout, "Main", null);
		this.gridLayoutTexte.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutTexte, "Texte", null);
		this.gridLayoutArtikel.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutArtikel, "Artikel", null);
		this.tabSheet.setSelectedTab(this.gridLayout);
		this.tabSheet.setSizeFull();
		this.panel.setContent(this.tabSheet);
		this.panel.setSizeFull();
		this.verticalLayoutLeft.addComponent(this.panel);
		this.verticalLayoutLeft.setComponentAlignment(this.panel, Alignment.MIDDLE_CENTER);
		this.verticalLayoutLeft.setExpandRatio(this.panel, 10.0F);
		this.treeGrid.setSizeFull();
		this.verticalLayoutRight.addComponent(this.treeGrid);
		this.verticalLayoutRight.setComponentAlignment(this.treeGrid, Alignment.MIDDLE_CENTER);
		this.verticalLayoutRight.setExpandRatio(this.treeGrid, 100.0F);
		this.verticalLayoutLeft.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayoutLeft);
		this.verticalLayoutRight.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.verticalLayoutRight);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.comboBoxCostAccount.addValueChangeListener(event -> this.comboBoxCostAccount_valueChange(event));
		this.cmdPropose.addClickListener(event -> this.cmdPropose_buttonClick(event));
		this.cmdGenerate.addClickListener(event -> this.cmdGenerate_buttonClick(event));
		this.cmdSaveText.addClickListener(event -> this.cmdSaveText_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, label2, label3, label4, labelInfoToolTip, label5, label6, label7, label8, label11, label12,
			label13;
	private XdevButton cmdPropose, cmdGenerate, cmdSaveText;
	private XdevComboBox<CostAccount> comboBoxCostAccount;
	private XdevPanel panel;
	private XdevTabSheet tabSheet;
	private XdevGridLayout gridLayout, gridLayoutTexte, gridLayoutArtikel;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevComboBox<Periode> comboBoxPeriode;
	private XdevPopupDateField dateBilldate;
	private XdevCheckBox checkBoxTextLast;
	private XdevTreeTable treeGrid;
	private XdevTextField textFieldOrderText, textFieldProjectLine, textFieldExpenseLine, textFieldJourneyLine,
			textFieldItemProject, textFieldItemExpense, textFieldItemJourney;
	private XdevVerticalLayout verticalLayoutLeft, verticalLayoutRight;
	// </generated-code>

}
