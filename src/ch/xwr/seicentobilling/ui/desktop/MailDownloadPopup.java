package ch.xwr.seicentobilling.ui.desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanItemContainer;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.MailManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;

public class MailDownloadPopup extends XdevView {
	private Order orderBean = null;
	private String zipname = null;

	/**
	 *
	 */
	public MailDownloadPopup() {
		super();
		this.initUI();

		// get Parameter
		this.orderBean = (Order) UI.getCurrent().getSession().getAttribute("orderbean");

		initComboBoxes();
		showInfoLabel();
	}

	private void showInfoLabel() {
		final Customer cus = this.orderBean.getCustomer();
		String info = "R#: " + this.orderBean.getOrdNumber() + " - ";
		info = info + cus.getShortname() + "  - PDF-Typ: ";

		if (cus.getCusBillingReport() != null) {
			info = info + cus.getCusBillingReport().name();
		}
		if (cus.getCusSinglepdf() != null && cus.getCusSinglepdf()) {
			info = info + " merge";
		}

		this.labelInfo.setValue(info);
	}

	private void initComboBoxes() {
        final CostAccountDAO dao = new CostAccountDAO();
		final List<CostAccount> ls1 = dao.findAllActive();
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = ls1.get(0); // Dev Mode
		}

		initCmbCostAccount(bean);
		initCmbPeriode(bean);

	}

	private void initCmbPeriode(final CostAccount selectedCst) {
		final PeriodeDAO dao = new PeriodeDAO();
		final XdevBeanItemContainer<Periode> cstList = new XdevBeanItemContainer<>(Periode.class);
		cstList.addAll(dao.findByCostAccountOpenPeriode(selectedCst));

		this.comboBoxPeriode.clear();
		this.comboBoxPeriode.setContainerDataSource(cstList);

		final Periode per = getPeriode(selectedCst);
		if (this.comboBoxPeriode.containsId(per)) {
			this.comboBoxPeriode.select(per);
		} else {
			this.comboBoxPeriode.setValue(per);
		}
	}

	private Periode getPeriode(final CostAccount cst) {
		final PeriodeDAO dao = new PeriodeDAO();
		final List<Periode> li = dao.findByCostAccount(cst);

		final Calendar now = Calendar.getInstance(); // Gets the current date
		now.setTime(this.orderBean.getOrdBillDate());
		int imonth = now.get(Calendar.MONTH);  //-1
		if (imonth == 0) {
			imonth = 12;
		}

		for (final Periode periode : li) {
			if (periode.getPerMonth().ordinal() == imonth) {
				return periode;
			}
		}

		return null;
	}

	private void initCmbCostAccount(final CostAccount loggedInCostAccount) {
		final CostAccountDAO dao = new CostAccountDAO();

		final XdevBeanItemContainer<CostAccount> dataList = new XdevBeanItemContainer<>(CostAccount.class);
		dataList.addAll(dao.findAllActive());

		this.comboBoxCst.clear();
		this.comboBoxCst.setContainerDataSource(dataList);

		if (this.comboBoxCst.containsId(loggedInCostAccount)) {
			this.comboBoxCst.select(loggedInCostAccount);
		} else {
			this.comboBoxCst.setValue(loggedInCostAccount);
		}
	}

	public static Window getPopupWindow() {
		final Window win = new Window();

		win.setWidth("530");
		win.setHeight("270");
		win.center();
		win.setModal(true);
		win.setContent(new MailDownloadPopup());

		return win;
	}

	private void initDownload() {
		this.label.setValue("starte PDF generierung....");
		final JasperManager jsp = new JasperManager();
		jsp.setSelectedPeriod(this.comboBoxPeriode.getSelectedItem().getBean());
		final String fname = jsp.getBillingZip(this.orderBean);
		this.label.setValue("Datei erstellt - bereit für Download");

		// Downloader init
		final Resource res = getInputStream(fname);
		final FileDownloader fd = new FileDownloader(res);
		fd.extend(this.cmdDownload);

		this.zipname = fname;
	}

	private Resource getInputStream(final String fname) {
		final File fin = new File(fname);

		final StreamResource.StreamSource source = new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				FileInputStream inStream = null;
				try {
					inStream = new FileInputStream(fin);
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				}
				return inStream;
			}
		};

		final Resource res = new StreamResource(source, fin.getName());

		return res;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDownload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDownload_buttonClick(final Button.ClickEvent event) {
		this.label.setValue("start download...");
		this.cmdMail.setEnabled(true);


	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdMail}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdMail_buttonClick(final Button.ClickEvent event) {
		final MailManager mail = new MailManager();
		Page.getCurrent().open(mail.getEmailUrl(this.orderBean), "_blank");

		new File(this.zipname).delete();	//is downloaded

		this.label.setValue("Mail Client geöffnet");
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdStart}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStart_buttonClick(final Button.ClickEvent event) {
		try {
			this.label.setValue("starte Erstellung - einen Moment bitte... ");
			//this.cmdStart.setEnabled(false);

			initDownload();

			this.label.setValue("erstellt");

			this.cmdDownload.setEnabled(true);
			//this.cmdDownload.click();
		} finally {
			//reset mouse icon
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #comboBoxCst}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxCst_valueChange(final Property.ValueChangeEvent event) {
		final CostAccount cst = (CostAccount) event.getProperty().getValue();

		if (cst != null) {
			initCmbPeriode(cst);
		}
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.gridLayout = new XdevGridLayout();
		this.lblCstAccount = new XdevLabel();
		this.comboBoxCst = new XdevComboBox<>();
		this.lblPeriode = new XdevLabel();
		this.comboBoxPeriode = new XdevComboBox<>();
		this.cmdStart = new XdevButton();
		this.cmdDownload = new XdevButton();
		this.cmdMail = new XdevButton();
		this.label = new XdevLabel();
		this.labelInfo = new XdevLabel();

		this.lblCstAccount.setDescription("Selektionskriterium für die Periode");
		this.lblCstAccount.setValue("Kostenstelle");
		this.comboBoxCst.setContainerDataSource(CostAccount.class, false);
		this.lblPeriode.setDescription("Definiert die Periode für den Arbeitsrapport. Default Vormonat der Rechnung.");
		this.lblPeriode.setValue("Periode Workreport");
		this.comboBoxPeriode.setContainerDataSource(Periode.class, false);
		this.cmdStart.setCaption("Erstelle Datei");
		this.cmdStart.setDescription("Startet die Erstellung der Reports auf Jasper.");
		this.cmdStart.setDisableOnClick(true);
		this.cmdDownload
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/Download1.png"));
		this.cmdDownload.setCaption("Download");
		this.cmdDownload.setEnabled(false);
		this.cmdMail.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/mail1.png"));
		this.cmdMail.setCaption("Öffne Mail");
		this.cmdMail.setEnabled(false);
		this.label.setValue("Zum Starten ersten Knopf drücken");
		this.labelInfo.setValue("Rechnung:");

		this.gridLayout.setColumns(4);
		this.gridLayout.setRows(6);
		this.lblCstAccount.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCstAccount, 0, 0);
		this.comboBoxCst.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxCst.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.comboBoxCst, 1, 0, 2, 0);
		this.lblPeriode.setSizeUndefined();
		this.gridLayout.addComponent(this.lblPeriode, 0, 1);
		this.comboBoxPeriode.setWidth(100, Unit.PERCENTAGE);
		this.comboBoxPeriode.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.comboBoxPeriode, 1, 1, 2, 1);
		this.cmdStart.setSizeUndefined();
		this.gridLayout.addComponent(this.cmdStart, 0, 2);
		this.cmdDownload.setSizeUndefined();
		this.gridLayout.addComponent(this.cmdDownload, 1, 2);
		this.cmdMail.setSizeUndefined();
		this.gridLayout.addComponent(this.cmdMail, 2, 2);
		this.label.setWidth(100, Unit.PERCENTAGE);
		this.label.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.label, 0, 3, 2, 3);
		this.labelInfo.setWidth(100, Unit.PERCENTAGE);
		this.labelInfo.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.labelInfo, 0, 4, 2, 4);
		final CustomComponent gridLayout_hSpacer = new CustomComponent();
		gridLayout_hSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_hSpacer, 3, 0, 3, 4);
		this.gridLayout.setColumnExpandRatio(3, 1.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 5, 2, 5);
		this.gridLayout.setRowExpandRatio(5, 1.0F);
		this.gridLayout.setSizeFull();
		this.setContent(this.gridLayout);
		this.setSizeFull();

		this.comboBoxCst.addValueChangeListener(event -> this.comboBoxCst_valueChange(event));
		this.cmdStart.addClickListener(event -> this.cmdStart_buttonClick(event));
		this.cmdDownload.addClickListener(event -> this.cmdDownload_buttonClick(event));
		this.cmdMail.addClickListener(event -> this.cmdMail_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblCstAccount, lblPeriode, label, labelInfo;
	private XdevButton cmdStart, cmdDownload, cmdMail;
	private XdevComboBox<CostAccount> comboBoxCst;
	private XdevGridLayout gridLayout;
	private XdevComboBox<Periode> comboBoxPeriode;
	// </generated-code>

}
