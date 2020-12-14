
package ch.xwr.seicentobilling.ui.billing;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import ch.xwr.seicentobilling.business.LovState.BookingType;
import ch.xwr.seicentobilling.business.OrderGenerator;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.helper.RowObjectAddonHandler;
import ch.xwr.seicentobilling.business.model.billing.BillDto;
import ch.xwr.seicentobilling.business.model.billing.GuiGeneratorFields;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.ui.OrderGenTreeItem;
import ch.xwr.seicentobilling.ui.OrderGenTreeItemData;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


@Route("ordergenerate")
public class OrderGenerateTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger _logger = LoggerFactory.getLogger(OrderGenerateTabView.class);
	
	private final GuiGeneratorFields guifld = new GuiGeneratorFields();
	private CostAccount              user   = null;
	TreeGrid<OrderGenTreeItem>       treeGrid;
	OrderGenTreeItemData             orderData;
	
	public OrderGenerateTabView()
	{

		super();
		this.initUI();
		this.loadFields();
		if(!this.loadParams())
		{
			this.cmdPropose.setEnabled(false);
			SeicentoNotification.showError("Parameter konnten nicht geladen werden. Bitte Artikel + Texte überprüfen.");
		}
		
		this.gridLayoutTexte.setVisible(false);
		this.gridLayoutArtikel.setVisible(false);
		this.treeGrid = new TreeGrid<>();

		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.tab, this.gridLayout);
		tabsToPages.put(this.tab2, this.gridLayoutTexte);
		tabsToPages.put(this.tab3, this.gridLayoutArtikel);

		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});
		this.verticalLayoutRight.add(this.treeGrid);
	}

	private boolean loadParams()
	{
		try
		{
			final RowObjectAddonHandler objman = new RowObjectAddonHandler(null);                                // company
																													// Level
			final RowObjectAddonHandler objUsr = new RowObjectAddonHandler(this.user.getCsaId(), "CostAccount"); // userlevel
			
			// Order (Header)
			this.textFieldOrderText.setValue(this.getTextParams(objman, objUsr, "headerText"));
			
			// project
			this.textFieldProjectLine.setValue(this.getTextParams(objman, objUsr, "lineTextProject"));
			// expense
			this.textFieldExpenseLine.setValue(this.getTextParams(objman, objUsr, "lineTextExpense"));
			// journey
			this.textFieldJourneyLine.setValue(this.getTextParams(objman, objUsr, "lineTextJourney"));
			
			// items
			final Item itm1 = this.SearchItem(objman, "itemIdentProject");
			this.textFieldItemProject.setValue(itm1.getPrpShortName());
			//
			final Item itm2 = this.SearchItem(objman, "itemIdentExpense");
			this.textFieldItemExpense.setValue(itm2.getPrpShortName());
			//
			final Item itm3 = this.SearchItem(objman, "itemIdentJourney");
			this.textFieldItemJourney.setValue(itm3.getPrpShortName());
			//
			final String cbxValue = objUsr.getRowParameter("billing", "generator", "cbxLastText");
			if(cbxValue.toLowerCase().equals("true"))
			{
				this.checkBoxTextLast.setValue(true);
			}
			
			this.gridLayoutArtikel.setEnabled(false);
			
			// init model
			this.guifld.setItemProject(itm1);
			this.guifld.setItemExpense(itm2);
			this.guifld.setItemJourney(itm3);
			
			return true;
			
		}
		catch(final Exception e)
		{
			OrderGenerateTabView._logger.error("Could not load Params for Ordergeneration");
			OrderGenerateTabView._logger.error(e.getStackTrace().toString());
		}
		
		return false;
	}

	private String
		getTextParams(final RowObjectAddonHandler objman, final RowObjectAddonHandler objUsr, final String key)
	{
		String txtValue = objUsr.getRowParameter("billing", "generator", key);
		
		if(txtValue == null || txtValue.isEmpty())
		{
			txtValue = objman.getRowParameter("billing", "generator", key);
		}
		
		return txtValue;
	}
	
	private Item SearchItem(final RowObjectAddonHandler objman, final String key)
	{
		final String value = objman.getRowParameter("billing", "generator", key);
		
		final ItemDAO    dao = new ItemDAO();
		final List<Item> lst = dao.findByIdent(value);
		if(lst != null && lst.size() > 0)
		{
			return lst.get(0);
		}
		return null;
	}
	
	private void loadFields()
	{
		final CostAccountDAO    dao = new CostAccountDAO();
		final List<CostAccount> ls1 = dao.findAll();
		
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if(bean == null)
		{
			bean = ls1.get(0); // Dev Mode
		}
		this.user = bean;
		
		this.comboBoxCostAccount.setItems(ls1);
		
		this.comboBoxCostAccount.setValue(bean);
		
		this.dateBilldate.setValue(LocalDate.now());
	}
	
	private void InitTreeGrid(final List<BillDto> lst)
	{

		this.orderData = new OrderGenTreeItemData(lst);
		this.treeGrid.removeAllColumns();
		
		this.treeGrid.setItems(this.orderData.getRootOrderItems(),
			this.orderData::getChildOrderItems);
		// this.treeGrid.addHierarchyColumn(OrderGenTreeItem::getCbo)
		// .setHeader("");
		this.treeGrid
			.addComponentHierarchyColumn(
				item -> this.createCheckBox(this.treeGrid, item))
			.setHeader("").setResizable(true);
		this.treeGrid.addColumn(OrderGenTreeItem::getCusName)
			.setHeader("Kunde").setResizable(true);
		this.treeGrid.addColumn(OrderGenTreeItem::getProName)
			.setHeader("Projekt").setResizable(true);
		this.treeGrid.addColumn(OrderGenTreeItem::getTotalAmount).setHeader("Betrag").setResizable(true)
			.setTextAlign(ColumnTextAlign.END);
		this.treeGrid.addColumn(OrderGenTreeItem::getLdate)
			.setHeader("L-Rechnung").setResizable(true);
		
		this.treeGrid.setSizeFull();

	}
	
	public Checkbox createCheckBox(final TreeGrid<OrderGenTreeItem> grid, final OrderGenTreeItem bean)
	{
		final Checkbox chkBox = new Checkbox();
		chkBox.setValue(bean.getCbo());
		if(bean.getChildItems().size() == 0)
		{
			chkBox.setVisible(false);
		}
		chkBox.addClickListener(ee -> {
			bean.setCbo(ee.getSource().getValue());
		});
		return chkBox;
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSaveText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSaveText_onClick(final ClickEvent<Button> event)
	{
		// save text to User
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(this.user.getCsaId(), "CostAccount");
		
		objman.putRowParameter("billing", "generator", "headerText", this.textFieldOrderText.getValue());
		objman.putRowParameter("billing", "generator", "lineTextProject", this.textFieldProjectLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextExpense", this.textFieldExpenseLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextJourney", this.textFieldJourneyLine.getValue());
		
		objman.putRowParameter("billing", "generator", "cbxLastText", this.checkBoxTextLast.getValue().toString());
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdPropose}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPropose_onClick(final ClickEvent<Button> event)
	{
		// here we read it....
		if(this.comboBoxCostAccount.getValue() == null || this.comboBoxPeriode.getValue() == null)
		{
			SeicentoNotification.showWarn("Bitte Periode wählen");
			return;
		}
		
		final Periode        per = this.comboBoxPeriode.getValue();
		final OrderGenerator gen = new OrderGenerator();
		final List<BillDto>  lst = gen.proposeDraft(per);
		
		if(per.getPerBookedProject().equals(BookingType.gebucht))
		{
			SeicentoNotification.showWarn("Für diese Periode wurden bereits Rechnungen generiert!!");
		}
		
		// publish list to grid
		this.InitTreeGrid(lst);
		// this.table.
		final Collection<?> lsSize = this.treeGrid.getTreeData().getRootItems();
		if(lsSize.size() > 0)
		{
			this.cmdGenerate.setEnabled(true);
		}
		else
		{
			this.cmdGenerate.setEnabled(false);
		}
		
	}

	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #comboBoxCostAccount}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void
		comboBoxCostAccount_valueChanged(final ComponentValueChangeEvent<ComboBox<CostAccount>, CostAccount> event)
	{
		final CostAccount cst = event.getValue();
		// if (this.comboBoxCostAccount.getSelectedItem() != null) {
		if(cst != null)
		{
			final CostAccount bean = this.comboBoxCostAccount.getValue();

			// this.comboBoxPeriode.getContainerDataSource().removeAllItems();
			final List<Periode> periodList = new PeriodeDAO().findByCostAccount(bean);
			this.comboBoxPeriode.setItems(periodList);
			if(periodList.size() > 0)
			{
				final Periode beanp = periodList.get(0);
				this.comboBoxPeriode.setValue(beanp);
			}
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdGenerate}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdGenerate_onClick(final ClickEvent<Button> event)
	{
		if(this.isModelValid())
		{
			int                  icount   = 0;
			String               billnbrs = "";
			final OrderGenerator gen      = new OrderGenerator();
			
			for(final OrderGenTreeItem item : this.treeGrid.getTreeData().getRootItems())
			{

				final BillDto bill = item.getObject();
				if(item.getCbo() && bill != null)
				{
					final Order newOrd = gen.createBill(bill, this.guifld);
					// System.out.println("Order created " + newOrd.getOrdNumber());
					icount++;

					if(icount == 1)
					{
						billnbrs = "" + newOrd.getOrdNumber();
					}
					else
					{
						billnbrs = billnbrs + ", " + newOrd.getOrdNumber();
					}
					item.setCbo(false);
					item.setLdate("Neu: " + newOrd.getOrdNumber());
				}
			}

			SeicentoNotification.showInfo("Rechnungen erstellen ausgeführt");
			if(icount > 0)
			{
				this.treeGrid.getDataProvider().refreshAll();
				SeicentoNotification.showInfo("Rechnungen generieren",
					"" + icount + " Rechnung(en) erstellt mit Nr: " + billnbrs + ".");
			}

			this.markPeriodeAsGenerated();

		}
		else
		{
			SeicentoNotification.showWarn("Rechnungen generieren",
				"ungültige Parameter - Generierung kann nicht starten.");

		}

	}
	
	private void markPeriodeAsGenerated()
	{
		final PeriodeDAO dao = new PeriodeDAO();
		
		final Periode per = this.comboBoxPeriode.getValue();
		per.setPerBookedProject(BookingType.gebucht);
		dao.save(per);
	}
	
	private boolean isModelValid()
	{
		final Date billingDate =
			Date.from(this.dateBilldate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
		this.guifld.setBillDate(billingDate);
		this.guifld.setBillText(this.textFieldOrderText.getValue());
		this.guifld.setCopyTextFromLastBill(this.checkBoxTextLast.getValue());
		this.guifld.setLineTextExpense(this.textFieldExpenseLine.getValue());
		this.guifld.setLineTextJourney(this.textFieldJourneyLine.getValue());
		this.guifld.setLineTextProject(this.textFieldProjectLine.getValue());
		
		if(this.guifld.getBillDate() == null)
		{
			return false;
		}
		if(this.guifld.getItemExpense() == null)
		{
			return false;
		}
		if(this.guifld.getItemJourney() == null)
		{
			return false;
		}
		if(this.guifld.getItemProject() == null)
		{
			return false;
		}
		
		return true;
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout          = new SplitLayout();
		this.verticalLayout       = new VerticalLayout();
		this.tabs                 = new Tabs();
		this.tab                  = new Tab();
		this.tab2                 = new Tab();
		this.tab3                 = new Tab();
		this.verticalLayout2      = new VerticalLayout();
		this.gridLayout           = new FormLayout();
		this.formItem11           = new FormItem();
		this.comboBoxCostAccount  = new ComboBox<>();
		this.formItem12           = new FormItem();
		this.comboBoxPeriode      = new ComboBox<>();
		this.formItem13           = new FormItem();
		this.dateBilldate         = new DatePicker();
		this.formItem14           = new FormItem();
		this.checkBoxTextLast     = new Checkbox();
		this.formItem3            = new FormItem();
		this.formItem             = new FormItem();
		this.cmdPropose           = new Button();
		this.cmdGenerate          = new Button();
		this.formItem2            = new FormItem();
		this.icon                 = new Icon(VaadinIcon.INFO_CIRCLE);
		this.label4               = new Label();
		this.gridLayoutTexte      = new FormLayout();
		this.formItem15           = new FormItem();
		this.textFieldOrderText   = new TextField();
		this.formItem16           = new FormItem();
		this.textFieldProjectLine = new TextField();
		this.formItem17           = new FormItem();
		this.textFieldExpenseLine = new TextField();
		this.formItem18           = new FormItem();
		this.textFieldJourneyLine = new TextField();
		this.formItem19           = new FormItem();
		this.cmdSaveText          = new Button();
		this.gridLayoutArtikel    = new FormLayout();
		this.formItem20           = new FormItem();
		this.textFieldItemProject = new TextField();
		this.formItem21           = new FormItem();
		this.textFieldItemExpense = new TextField();
		this.formItem22           = new FormItem();
		this.textFieldItemJourney = new TextField();
		this.verticalLayoutRight  = new VerticalLayout();

		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setAlignItems(FlexComponent.Alignment.START);
		this.verticalLayout.getStyle().set("overflow", "inherit");
		this.tabs.setMinHeight("null");
		this.tab.setLabel("Main");
		this.tab2.setLabel("Texte");
		this.tab3.setLabel("Artikel");
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setPadding(false);
		this.gridLayout.setMinWidth("1");
		this.gridLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.comboBoxCostAccount.setLabel("Kostenstelle");
		this.comboBoxCostAccount.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxCostAccount::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.comboBoxCostAccount.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.comboBoxPeriode.setLabel("Periode wählen");
		this.comboBoxPeriode.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Periode::getPerName));
		this.dateBilldate.setLabel("Rechnungsdatum");
		this.checkBoxTextLast.setLabel("Text von letzer Rechnung");
		this.cmdPropose.setText("Vorschlag generieren");
		this.cmdGenerate.setEnabled(false);
		this.cmdGenerate.setText("Rechnungen erstellen");
		this.formItem2.getElement().setAttribute("colspan", "2");
		this.label4.setText("       MwSt-Ansatz wird für alle Positionen vom Projekt übernommen!");
		this.gridLayoutTexte.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.textFieldOrderText.setLabel("Textzeile Rechnung");
		this.textFieldProjectLine.setLabel("Textzeile Dienstleistung");
		this.textFieldExpenseLine.setLabel("Textzeile Spesen");
		this.textFieldJourneyLine.setLabel("Textzeile Reisezeit");
		this.cmdSaveText.setText("Texte speichern");
		this.gridLayoutArtikel.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.textFieldItemProject.setLabel("Artikel Dienstleistung");
		this.textFieldItemExpense.setLabel("Artikel Spesen");
		this.textFieldItemJourney.setLabel("Artikel Reisezeit");
		this.verticalLayoutRight.setSpacing(false);
		this.verticalLayoutRight.setPadding(false);

		this.tabs.add(this.tab, this.tab2, this.tab3);
		this.comboBoxCostAccount.setWidthFull();
		this.comboBoxCostAccount.setHeight(null);
		this.formItem11.add(this.comboBoxCostAccount);
		this.comboBoxPeriode.setWidthFull();
		this.comboBoxPeriode.setHeight(null);
		this.formItem12.add(this.comboBoxPeriode);
		this.dateBilldate.setWidthFull();
		this.dateBilldate.setHeight(null);
		this.formItem13.add(this.dateBilldate);
		this.checkBoxTextLast.setWidthFull();
		this.checkBoxTextLast.setHeight(null);
		this.formItem14.add(this.checkBoxTextLast);
		this.cmdPropose.setWidthFull();
		this.cmdPropose.setHeight(null);
		this.cmdGenerate.setWidthFull();
		this.cmdGenerate.setHeight(null);
		this.formItem.add(this.cmdPropose, this.cmdGenerate);
		this.label4.setWidthFull();
		this.label4.setHeight(null);
		this.formItem2.add(this.icon, this.label4);
		this.gridLayout.add(this.formItem11, this.formItem12, this.formItem13, this.formItem14, this.formItem3,
			this.formItem, this.formItem2);
		this.textFieldOrderText.setWidthFull();
		this.textFieldOrderText.setHeight(null);
		this.formItem15.add(this.textFieldOrderText);
		this.textFieldProjectLine.setWidthFull();
		this.textFieldProjectLine.setHeight(null);
		this.formItem16.add(this.textFieldProjectLine);
		this.textFieldExpenseLine.setWidthFull();
		this.textFieldExpenseLine.setHeight(null);
		this.formItem17.add(this.textFieldExpenseLine);
		this.textFieldJourneyLine.setWidthFull();
		this.textFieldJourneyLine.setHeight(null);
		this.formItem18.add(this.textFieldJourneyLine);
		this.cmdSaveText.setSizeUndefined();
		this.formItem19.add(this.cmdSaveText);
		this.gridLayoutTexte.add(this.formItem15, this.formItem16, this.formItem17, this.formItem18, this.formItem19);
		this.textFieldItemProject.setWidthFull();
		this.textFieldItemProject.setHeight(null);
		this.formItem20.add(this.textFieldItemProject);
		this.textFieldItemExpense.setWidthFull();
		this.textFieldItemExpense.setHeight(null);
		this.formItem21.add(this.textFieldItemExpense);
		this.textFieldItemJourney.setWidthFull();
		this.textFieldItemJourney.setHeight(null);
		this.formItem22.add(this.textFieldItemJourney);
		this.gridLayoutArtikel.add(this.formItem20, this.formItem21, this.formItem22);
		this.gridLayout.setSizeFull();
		this.gridLayoutTexte.setSizeFull();
		this.gridLayoutArtikel.setSizeFull();
		this.verticalLayout2.add(this.gridLayout, this.gridLayoutTexte, this.gridLayoutArtikel);
		this.tabs.setWidthFull();
		this.tabs.setHeight("36px");
		this.verticalLayout2.setWidthFull();
		this.verticalLayout2.setHeight(null);
		this.verticalLayout.add(this.tabs, this.verticalLayout2);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.verticalLayoutRight);
		this.splitLayout.setSplitterPosition(35.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.tabs.setSelectedIndex(0);

		this.comboBoxCostAccount.addValueChangeListener(this::comboBoxCostAccount_valueChanged);
		this.cmdPropose.addClickListener(this::cmdPropose_onClick);
		this.cmdGenerate.addClickListener(this::cmdGenerate_onClick);
		this.cmdSaveText.addClickListener(this::cmdSaveText_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Tab                   tab, tab2, tab3;
	private VerticalLayout        verticalLayout, verticalLayout2, verticalLayoutRight;
	private Label                 label4;
	private Tabs                  tabs;
	private FormItem              formItem11, formItem12, formItem13, formItem14, formItem3, formItem, formItem2,
		formItem15, formItem16, formItem17, formItem18, formItem19, formItem20, formItem21, formItem22;
	private FormLayout            gridLayout, gridLayoutTexte, gridLayoutArtikel;
	private Checkbox              checkBoxTextLast;
	private Button                cmdPropose, cmdGenerate, cmdSaveText;
	private SplitLayout           splitLayout;
	private DatePicker            dateBilldate;
	private ComboBox<Periode>     comboBoxPeriode;
	private Icon                  icon;
	private TextField             textFieldOrderText, textFieldProjectLine, textFieldExpenseLine, textFieldJourneyLine,
		textFieldItemProject, textFieldItemExpense, textFieldItemJourney;
	private ComboBox<CostAccount> comboBoxCostAccount;
	// </generated-code>
	
}
