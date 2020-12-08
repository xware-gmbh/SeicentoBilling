
package ch.xwr.seicentobilling.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.List;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.MailManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;


public class MailDownloadPopup extends VerticalLayout
{
	private Order  orderBean   = null;
	private String zipname     = null;
	private URI    downloadUri = null;

	/**
	 *
	 */
	public MailDownloadPopup()
	{
		super();
		this.initUI();

		// get Parameter
		this.orderBean = (Order)UI.getCurrent().getSession().getAttribute("orderbean");

		this.initComboBoxes();
		this.showInfoLabel();

	}
	
	private void showInfoLabel()
	{
		final Customer cus  = this.orderBean.getCustomer();
		String         info = "R#: " + this.orderBean.getOrdNumber() + " - ";
		info = info + cus.getShortname() + "  - PDF-Typ: ";

		if(cus.getCusBillingReport() != null)
		{
			info = info + cus.getCusBillingReport().name();
		}
		if(cus.getCusSinglepdf() != null && cus.getCusSinglepdf())
		{
			info = info + " merge";
		}

		this.labelInfo.setText(info);
	}
	
	private void initComboBoxes()
	{
		final CostAccount bean = this.lookupCostAccount();

		this.initCmbCostAccount(bean);
		this.initCmbPeriode(bean);
	}
	
	private void initCmbPeriode(final CostAccount selectedCst)
	{
		final PeriodeDAO    dao     = new PeriodeDAO();
		final List<Periode> cstList = dao.findByCostAccountTop(selectedCst, 6);
		
		this.comboBoxPeriode.setItems(cstList);

		final Periode per = this.getPeriode(cstList);

		this.comboBoxPeriode.setValue(per);

	}

	private Periode getPeriode(final List<Periode> cstList)
	{
		// final PeriodeDAO dao = new PeriodeDAO();
		// final List<Periode> li = dao.findByCostAccount(cst);

		final Calendar now = Calendar.getInstance(); // Gets the current date
		now.setTime(this.orderBean.getOrdBillDate());
		int imonth = now.get(Calendar.MONTH); // -1
		if(imonth == 0)
		{
			imonth = 12;
		}

		for(final Periode periode : cstList)
		{
			if(periode.getPerMonth().ordinal() == imonth)
			{
				return periode;
			}
		}

		return null;
	}

	private void initCmbCostAccount(final CostAccount loggedInCostAccount)
	{
		final CostAccountDAO dao = new CostAccountDAO();

		final List<CostAccount> dataList = dao.findAllActive();

		this.comboBoxCst.setItems(dataList);

		final CostAccount bean = this.lookupInList(dataList, loggedInCostAccount);

		if(this.comboBoxCst.getDataProvider().getId(bean) != null)
		{
			this.comboBoxCst.setValue(bean);
		}
		else
		{
			this.comboBoxCst.setValue(loggedInCostAccount);
		}
	}

	private CostAccount
		lookupInList(final List<CostAccount> dataList, final CostAccount loggedInCostAccount)
	{

		for(final CostAccount costAccount : dataList)
		{
			if(costAccount.getCsaId().equals(loggedInCostAccount.getCsaId()))
			{
				return costAccount;
			}

		}

		return null;
	}

	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		win.setSizeFull();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new MailDownloadPopup());
		return win;
	}
	
	private CostAccount lookupCostAccount()
	{
		CostAccount bean = Seicento.getLoggedInCostAccount();

		if(this.orderBean.getProject() != null)
		{
			if(this.orderBean.getProject().getCostAccount() != null)
			{
				bean = this.orderBean.getProject().getCostAccount();
			}
		}

		if(bean == null)
		{
			final CostAccountDAO    dao = new CostAccountDAO();
			final List<CostAccount> ls1 = dao.findAllActive();
			bean = ls1.get(0); // Dev Mode
		}
		return bean;
	}
	
	private void initDownload()
	{
		this.label.setText("starte PDF generierung....");
		final JasperManager jsp = new JasperManager();
		jsp.setSelectedPeriod(this.comboBoxPeriode.getValue());
		final String fname = jsp.getBillingZip(this.orderBean);
		this.label.setText("Datei erstellt - bereit für Download");
		this.zipname = fname;
		// Downloader init
		final StreamResource resource = new StreamResource(fname,
			this::createExport);
		
		final Anchor downloadLink = new Anchor(resource, "Download");
		// this.horizontalLayout2.add(downloadLink);
		final StreamRegistration registration =
			VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
		this.downloadUri = registration.getResourceUri();
		
	}
	
	private InputStream createExport()
	{
		InputStream inputStream;
		try
		{
			inputStream = new FileInputStream(this.zipname);
			return inputStream;
		}
		catch(final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Event handler delegate method for the {@link ComboBox} {@link #comboBoxCst}.
	 *
	 * @see HasValue.ValueChangeListener#valueChanged(HasValue.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxCst_valueChanged(final ComponentValueChangeEvent<ComboBox<CostAccount>, CostAccount> event)
	{
		final CostAccount cst = event.getValue();
		
		if(cst != null)
		{
			this.initCmbPeriode(cst);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdStart}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStart_onClick(final ClickEvent<Button> event)
	{
		try
		{
			this.label.setText("starte Erstellung - einen Moment bitte... ");
			// this.cmdStart.setEnabled(false);
			
			this.initDownload();
			
			this.label.setText("erstellt");
			
			this.cmdDownload.setEnabled(true);
			// this.cmdDownload.click();
		}
		finally
		{
			// reset mouse icon
		}

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDownload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDownload_onClick(final ClickEvent<Button> event)
	{
		this.label.setText("start download...");
		UI.getCurrent().getPage().executeJs("window.open('" + this.downloadUri + "', \"_blank\", \"\");");
		this.cmdMail.setEnabled(true);
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdMail}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdMail_onClick(final ClickEvent<Button> event)
	{
		final MailManager mail = new MailManager();
		UI.getCurrent().getPage().open(mail.getEmailUrl(this.orderBean), "_blank");
		
		new File(this.zipname).delete(); // is downloaded
		
		this.label.setText("Mail Client geöffnet");
		((Dialog)this.getParent().get()).close();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout    = new VerticalLayout();
		this.horizontalLayout  = new HorizontalLayout();
		this.icon              = new Icon(VaadinIcon.CLOCK);
		this.label             = new Label();
		this.formLayout        = new FormLayout();
		this.formItem2         = new FormItem();
		this.lblCstAccount     = new Label();
		this.comboBoxCst       = new ComboBox<>();
		this.formItem4         = new FormItem();
		this.lblPeriode        = new Label();
		this.comboBoxPeriode   = new ComboBox<>();
		this.horizontalLayout2 = new HorizontalLayout();
		this.cmdStart          = new Button();
		this.cmdDownload       = new Button();
		this.cmdMail           = new Button();
		this.verticalLayout2   = new VerticalLayout();
		this.label2            = new Label();
		this.labelInfo         = new Label();
		
		this.verticalLayout.setPadding(false);
		this.label.setText("MailDownload bearbeiten");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 3, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblCstAccount.setText("Kostenstelle");
		this.comboBoxCst.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.comboBoxCst::getItemLabelGenerator),
			DataProvider.ofCollection(new CostAccountDAO().findAll()));
		this.comboBoxCst.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CostAccount::getCsaCode));
		this.lblPeriode.setText("Periode Workreport");
		this.comboBoxPeriode.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxPeriode::getItemLabelGenerator),
			DataProvider.ofCollection(new PeriodeDAO().findAll()));
		this.comboBoxPeriode.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Periode::getPerName));
		this.cmdStart.setText("Erstelle Datei");
		this.cmdStart.setDisableOnClick(true);
		this.cmdDownload.setEnabled(false);
		this.cmdDownload.setText("Download");
		this.cmdDownload.setIcon(VaadinIcon.DOWNLOAD.create());
		this.cmdMail.setEnabled(false);
		this.cmdMail.setText("Öffne Mail");
		this.cmdMail.setIcon(IronIcons.MAIL.create());
		this.verticalLayout2.setSpacing(false);
		this.verticalLayout2.setPadding(false);
		this.label2.setText("Zum Starten ersten Knopf drücken");
		this.labelInfo.setText("Rechnung:");
		
		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblCstAccount.setSizeUndefined();
		this.lblCstAccount.getElement().setAttribute("slot", "label");
		this.comboBoxCst.setWidthFull();
		this.comboBoxCst.setHeight(null);
		this.formItem2.add(this.lblCstAccount, this.comboBoxCst);
		this.lblPeriode.setSizeUndefined();
		this.lblPeriode.getElement().setAttribute("slot", "label");
		this.comboBoxPeriode.setWidthFull();
		this.comboBoxPeriode.setHeight(null);
		this.formItem4.add(this.lblPeriode, this.comboBoxPeriode);
		this.formLayout.add(this.formItem2, this.formItem4);
		this.cmdStart.setSizeUndefined();
		this.cmdDownload.setSizeUndefined();
		this.cmdMail.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdStart, this.cmdDownload, this.cmdMail);
		this.label2.setSizeUndefined();
		this.labelInfo.setWidthFull();
		this.labelInfo.setHeight(null);
		this.verticalLayout2.add(this.label2, this.labelInfo);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setWidthFull();
		this.formLayout.setHeight(null);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout2.setWidthFull();
		this.verticalLayout2.setHeight("100px");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2, this.verticalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();
		
		this.comboBoxCst.addValueChangeListener(this::comboBoxCst_valueChanged);
		this.cmdStart.addClickListener(this::cmdStart_onClick);
		this.cmdDownload.addClickListener(this::cmdDownload_onClick);
		this.cmdMail.addClickListener(this::cmdMail_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private FormLayout            formLayout;
	private Button                cmdStart, cmdDownload, cmdMail;
	private VerticalLayout        verticalLayout, verticalLayout2;
	private HorizontalLayout      horizontalLayout, horizontalLayout2;
	private ComboBox<Periode>     comboBoxPeriode;
	private Label                 label, lblCstAccount, lblPeriode, label2, labelInfo;
	private Icon                  icon;
	private FormItem              formItem2, formItem4;
	private ComboBox<CostAccount> comboBoxCst;
	// </generated-code>

}
