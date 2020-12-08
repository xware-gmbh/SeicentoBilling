
package ch.xwr.seicentobilling.ui;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import ch.xwr.seicentobilling.business.model.billing.BillLine;
import ch.xwr.seicentobilling.business.model.billing.GuiGeneratorFields;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.ItemDAO;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;


@Route("ordergenerate")
public class OrderGenerateTabView extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger _logger = LoggerFactory.getLogger(OrderGenerateTabView.class);
	
	private final GuiGeneratorFields guifld = new GuiGeneratorFields();
	private CostAccount              user   = null;

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
			final ch.xwr.seicentobilling.entities.Item itm1 = this.SearchItem(objman, "itemIdentProject");
			this.textFieldItemProject.setValue(itm1.getPrpShortName());
			//
			final ch.xwr.seicentobilling.entities.Item itm2 = this.SearchItem(objman, "itemIdentExpense");
			this.textFieldItemExpense.setValue(itm2.getPrpShortName());
			//
			final ch.xwr.seicentobilling.entities.Item itm3 = this.SearchItem(objman, "itemIdentJourney");
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
	
	private ch.xwr.seicentobilling.entities.Item SearchItem(final RowObjectAddonHandler objman, final String key)
	{
		final String value = objman.getRowParameter("billing", "generator", key);
		
		final ItemDAO                                    dao = new ItemDAO();
		final List<ch.xwr.seicentobilling.entities.Item> lst = dao.findByIdent(value);
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

	}

	private Object[] getParentLine(final BillDto billDto)
	{
		final Checkbox cbo = new Checkbox();
		cbo.setValue(true);
		if(billDto.getProject().getInternal() == null || billDto.getProject().getInternal())
		{
			cbo.setValue(false);
			cbo.setEnabled(false);
		}
		final String cusName = billDto.getCustomer().getFullname();
		final String proName = billDto.getProject().getProName();
		final String amount  = this.getAmtString(billDto.getTotalAmount(), true);
		final String ldate   = this.getLastBillDate(billDto);

		final Object[] retval = new Object[]{cbo, cusName, proName, amount, ldate, billDto};
		return retval;
	}

	private String getLastBillDate(final BillDto billDto)
	{
		final String     pattern = "dd.MM.yyyy";
		final DateFormat df      = new SimpleDateFormat(pattern);

		final OrderDAO    dao = new OrderDAO();
		final List<Order> lst = dao.findByCustomer(billDto.getCustomer());
		for(final Iterator<Order> iterator = lst.iterator(); iterator.hasNext();)
		{
			final Order order = iterator.next();
			if(order.getProject() != null)
			{
				if(order.getProject().getProId().equals(billDto.getProject().getProId()))
				{
					return df.format(order.getOrdBillDate());
				}
			}
		}

		return "";
	}

	private String getAmtString(final Double amount, final boolean currency)
	{
		final DecimalFormat decimalFormat  = new DecimalFormat("#,##0.00");
		final String        numberAsString = "            " + decimalFormat.format(amount);
		final int           ilen           = numberAsString.length();
		final String        retval         = numberAsString.substring(ilen - 11);
		if(currency)
		{
			return "CHF" + retval;
		}
		return retval;
	}

	private Object[] getDetailGridLine(final String text, final List<BillLine> list)
	{
		Double amt = new Double(0);
		if(list != null && !list.isEmpty())
		{
			for(int i = 0; i < list.size(); i++)
			{
				final BillLine tmp = list.get(i);
				amt = amt + tmp.getAmount();
			}
		}

		if(amt > 0)
		{
			final String   samt   = this.getAmtString(amt, false);
			final Object[] retval = new Object[]{null, null, text, samt, null, null};
			return retval;
		}

		return null;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSaveText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSaveText_onClick(final ClickEvent<Button> event)
	{
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
			
			this.comboBoxPeriode.setItems(new PeriodeDAO().findByCostAccount(bean));

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
		
	}
	
	/**
	 * Event handler delegate method for the {@link VerticalLayout} {@link #verticalLayout}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void verticalLayout_onClick(final ClickEvent<VerticalLayout> event)
	{
		// save text to User
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(this.user.getCsaId(), "CostAccount");

		objman.putRowParameter("billing", "generator", "headerText", this.textFieldOrderText.getValue());
		objman.putRowParameter("billing", "generator", "lineTextProject", this.textFieldProjectLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextExpense", this.textFieldExpenseLine.getValue());
		objman.putRowParameter("billing", "generator", "lineTextJourney", this.textFieldJourneyLine.getValue());

		objman.putRowParameter("billing", "generator", "cbxLastText", this.checkBoxTextLast.getValue().toString());
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
		this.treeGrid             = new TreeGrid<>();
		
		this.setSpacing(false);
		this.setPadding(false);
		this.verticalLayout.setSpacing(false);
		this.tabs.setMinHeight("50px");
		this.tab.setLabel("Main");
		this.tab2.setLabel("Texte");
		this.tab3.setLabel("Artikel");
		this.verticalLayout2.setMinHeight("100%");
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setPadding(false);
		this.gridLayout.setMinWidth("1");
		this.gridLayout.getStyle().set("overflow-x", "hidden");
		this.gridLayout.getStyle().set("overflow-y", "auto");
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
		this.gridLayoutTexte.getStyle().set("overflow-x", "hidden");
		this.gridLayoutTexte.getStyle().set("overflow-y", "auto");
		this.gridLayoutTexte.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.textFieldOrderText.setLabel("Textzeile Rechnung");
		this.textFieldProjectLine.setLabel("Textzeile Dienstleistung");
		this.textFieldExpenseLine.setLabel("Textzeile Spesen");
		this.textFieldJourneyLine.setLabel("Textzeile Reisezeit");
		this.cmdSaveText.setText("Texte speichern");
		this.gridLayoutArtikel.getStyle().set("overflow-x", "hidden");
		this.gridLayoutArtikel.getStyle().set("overflow-y", "auto");
		this.gridLayoutArtikel.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("320px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.textFieldItemProject.setLabel("Artikel Dienstleistung");
		this.textFieldItemExpense.setLabel("Artikel Spesen");
		this.textFieldItemJourney.setLabel("Artikel Reisezeit");
		this.verticalLayoutRight.setSpacing(false);
		this.verticalLayoutRight.setPadding(false);
		this.treeGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		
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
		this.verticalLayout2.setHeight("100px");
		this.verticalLayout.add(this.tabs, this.verticalLayout2);
		this.treeGrid.setSizeFull();
		this.verticalLayoutRight.add(this.treeGrid);
		this.verticalLayoutRight.setFlexGrow(1.0, this.treeGrid);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.verticalLayoutRight);
		this.splitLayout.setSplitterPosition(40.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();
		
		this.tabs.setSelectedIndex(0);
		
		this.verticalLayout.addClickListener(this::verticalLayout_onClick);
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
	private TreeGrid<?>           treeGrid;
	private SplitLayout           splitLayout;
	private DatePicker            dateBilldate;
	private ComboBox<Periode>     comboBoxPeriode;
	private Icon                  icon;
	private TextField             textFieldOrderText, textFieldProjectLine, textFieldExpenseLine, textFieldJourneyLine,
		textFieldItemProject, textFieldItemExpense, textFieldItemJourney;
	private ComboBox<CostAccount> comboBoxCostAccount;
	// </generated-code>
	
}


class Parent
{
	private String  cusName;
	private String  proName;
	private String  amount;
	private String  ldate;
	private boolean cbo;
	private Long    id;
	
	public String getCusName()
	{
		return this.cusName;
	}
	
	public void setCusName(final String cusName)
	{
		this.cusName = cusName;
	}
	
	public String getProName()
	{
		return this.proName;
	}
	
	public void setProName(final String proName)
	{
		this.proName = proName;
	}
	
	public String getAmount()
	{
		return this.amount;
	}
	
	public void setAmount(final String amount)
	{
		this.amount = amount;
	}
	
	public String getLdate()
	{
		return this.ldate;
	}
	
	public void setLdate(final String ldate)
	{
		this.ldate = ldate;
	}
	
	public boolean isCbo()
	{
		return this.cbo;
	}
	
	public void setCbo(final boolean cbo)
	{
		this.cbo = cbo;
	}
	
	public Long getId()
	{
		return this.id;
	}
	
	public void setId(final Long id)
	{
		this.id = id;
	}
	
}
