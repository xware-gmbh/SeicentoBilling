package ch.xwr.seicentobilling.ui.desktop;

import java.io.File;

import org.apache.log4j.LogManager;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevProgressBar;
import com.xdev.ui.XdevTextArea;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.ExcelHandler;
import ch.xwr.seicentobilling.business.helper.FileUploadDto;
import ch.xwr.seicentobilling.entities.Periode;

public class ExcelUploadPopup extends XdevView {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ExcelUploadPopup.class);

	private final Periode periodeBean;
	FileUploadDto result = null;

	/**
	 *
	 */
	public ExcelUploadPopup() {
		super();
		this.initUI();

		// get Parameter
		this.periodeBean = (Periode) UI.getCurrent().getSession().getAttribute("periodebean");

		setDefaultValue();

		this.labelStatus.setValue("");
		this.lblFileName.setValue("");
		this.lblSize.setValue("");
		this.lblCount.setValue("");

	}


	private void setDefaultValue() {
		int isheet = this.periodeBean.getPerMonth().getValue();

		if (isheet == 12) {
			isheet = 1;
		} else {
			isheet = isheet+1;
		}
		this.textFieldSheet.setValue("" + isheet);

	}


	public static Window getPopupWindow() {
		final Window win = new Window();

		win.setWidth("860");
		win.setHeight("450");
		win.center();
		win.setModal(true);
		win.setContent(new ExcelUploadPopup());

		return win;
	}


	private void processUploadedFile(final File outFile) {
		try {
			final int sheet = new Integer(this.textFieldSheet.getValue()).intValue();
			final ExcelHandler exc = new ExcelHandler();
			//exc.addProgressListener(this);
			exc.importReportLine(outFile, sheet, this.periodeBean);

			Notification.show("Excel Datei importiert", Type.TRAY_NOTIFICATION);
			LOG.info("Excel Datei erfolgreich importiert");

			this.labelStatus.setValue("Daten erfolgreich importiert. Bitte Rapport überprüfen.");
			this.labelStatus.setValue(exc.getResultString());

		} catch (final Exception e) {
			this.labelStatus.setValue("Importieren ist fehlgeschlagen!");
			Notification.show("Fehler beim Importieren", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			LOG.error(e.getLocalizedMessage());
		} finally {
			//cleanup
			outFile.delete();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		((Window) this.getParent()).close();
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdUpload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpload_buttonClick(final Button.ClickEvent event) {
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.xlsx");
		p1.setSubject("Import Rapporte Excel Datei");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Window win = FileUploaderPopup.getPopupWindow();
		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				ExcelUploadPopup.this.result = (FileUploadDto) UI.getCurrent().getSession().getAttribute("uploaddto");
				ExcelUploadPopup.this.cmdProcess.setEnabled(ExcelUploadPopup.this.result.isSuccess());
				ExcelUploadPopup.this.labelStatus.setValue(ExcelUploadPopup.this.result.getMessage());
				ExcelUploadPopup.this.lblSize.setValue("" + (ExcelUploadPopup.this.result.getSize() / 1000) + " KB");
				ExcelUploadPopup.this.lblFileName.setValue(ExcelUploadPopup.this.result.getUpfile().getName());
			}
		});
		this.getUI().addWindow(win);

	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdProcess}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_buttonClick(final Button.ClickEvent event) {
		try {
//			this.progressBar.setVisible(true);
//			this.progressBar.clear();
//			this.progressBar.setImmediate(true);
//			this.progressBar.setValue((float) this.result.getSize());
//			this.progressBar.setIndeterminate(true);

			processUploadedFile(this.result.getUpfile());
		} catch (final Exception e) {
			Notification.show("Fehler beim Importieren", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			LOG.error(e.getLocalizedMessage());
		} finally {
			// cleanup
			this.cmdProcess.setEnabled(false);
			//this.progressBar.setIndeterminate(false);
			this.result.getUpfile().delete();
		}

	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.textArea = new XdevTextArea();
		this.horizontalLayoutUpload = new XdevHorizontalLayout();
		this.gridLayout = new XdevGridLayout();
		this.label2 = new XdevLabel();
		this.textFieldSheet = new XdevTextField();
		this.label3 = new XdevLabel();
		this.textFieldRow = new XdevTextField();
		this.label = new XdevLabel();
		this.lblFileName = new XdevLabel();
		this.lblSize = new XdevLabel();
		this.label4 = new XdevLabel();
		this.labelStatus = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdUpload = new XdevButton();
		this.cmdProcess = new XdevButton();
		this.label7 = new XdevLabel();
		this.cmdCancel = new XdevButton();
		this.horizontalLayoutFooter = new XdevHorizontalLayout();
		this.lblCount = new XdevLabel();
		this.progressBar = new XdevProgressBar();

		this.panel.setIcon(FontAwesome.FILE_EXCEL_O);
		this.panel.setCaption("Rapporte importieren (xls)");
		this.panel.setTabIndex(0);
		this.verticalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.horizontalLayout2.setCaption("");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.textArea.setValue(
				"Die Funktion ermöglicht es Rapporte von eienm Excel File zu importieren. Das Format der Excel Datei ist definiert und darf nicht abweichen. EinTemplate ist vom Admin erhältlich. Die Reihenfolge der Sheets in Excel ist wie folgt: Stammdaten, Dezember (Vorjahr), Januar, Februar..... Mit dieser Reihenfolge ist der Wert im Feld Arbeitsblatt korrekt berechnet.");
		this.textArea.setReadOnly(true);
		this.textArea.setRows(4);
		this.horizontalLayoutUpload.setMargin(new MarginInfo(false));
		this.gridLayout.setMargin(new MarginInfo(false, true, false, true));
		this.label2.setDescription("Die Zahl entspricht dem Monat (z.B. 9 = September)");
		this.label2.setValue("Arbeitsblatt");
		this.textFieldSheet.setDescription("Welches Arbeitsblatt in Excel beginnend mit 0");
		this.textFieldSheet.setValue("2");
		this.label3.setDescription("Standardwert 15. Daten bis und mit dieser Zeile in Excel werden ignoriert.");
		this.label3.setValue("Offset Zeile");
		this.textFieldRow.setDescription("Startzeile der Rapporte beginnend bei 0");
		this.textFieldRow.setEnabled(false);
		this.textFieldRow.setValue("15");
		this.label.setDescription("Standardwert 15. Daten bis und mit dieser Zeile in Excel werden ignoriert.");
		this.label.setValue("Datei");
		this.lblFileName.setValue("Label");
		this.lblSize.setValue("Label");
		this.label4.setDescription("Standardwert 15. Daten bis und mit dieser Zeile in Excel werden ignoriert.");
		this.label4.setValue("Status");
		this.labelStatus.setValue("Label");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdUpload.setIcon(FontAwesome.UPLOAD);
		this.cmdUpload.setCaption("Datei...");
		this.cmdProcess.setIcon(FontAwesome.ROCKET);
		this.cmdProcess.setCaption("Importieren");
		this.cmdProcess.setEnabled(false);
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption("Abbrechen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.horizontalLayoutFooter.setMargin(new MarginInfo(false, true, true, false));
		this.lblCount.setValue("Label");

		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout2.addComponent(this.textArea);
		this.horizontalLayout2.setComponentAlignment(this.textArea, Alignment.MIDDLE_CENTER);
		this.horizontalLayout2.setExpandRatio(this.textArea, 100.0F);
		this.gridLayout.setColumns(3);
		this.gridLayout.setRows(5);
		this.label2.setSizeUndefined();
		this.gridLayout.addComponent(this.label2, 0, 0);
		this.textFieldSheet.setSizeUndefined();
		this.gridLayout.addComponent(this.textFieldSheet, 1, 0);
		this.label3.setSizeUndefined();
		this.gridLayout.addComponent(this.label3, 0, 1);
		this.textFieldRow.setSizeUndefined();
		this.gridLayout.addComponent(this.textFieldRow, 1, 1);
		this.label.setSizeUndefined();
		this.gridLayout.addComponent(this.label, 0, 2);
		this.lblFileName.setSizeUndefined();
		this.gridLayout.addComponent(this.lblFileName, 1, 2);
		this.lblSize.setSizeUndefined();
		this.gridLayout.addComponent(this.lblSize, 2, 2);
		this.label4.setSizeUndefined();
		this.gridLayout.addComponent(this.label4, 0, 3);
		this.labelStatus.setWidth(100, Unit.PERCENTAGE);
		this.labelStatus.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.labelStatus, 1, 3, 2, 3);
		this.gridLayout.setColumnExpandRatio(1, 20.0F);
		this.gridLayout.setColumnExpandRatio(2, 10.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 4, 2, 4);
		this.gridLayout.setRowExpandRatio(4, 1.0F);
		this.gridLayout.setSizeFull();
		this.horizontalLayoutUpload.addComponent(this.gridLayout);
		this.horizontalLayoutUpload.setComponentAlignment(this.gridLayout, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutUpload.setExpandRatio(this.gridLayout, 10.0F);
		this.cmdUpload.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdUpload);
		this.cmdProcess.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdProcess);
		this.label7.setWidth(78, Unit.PIXELS);
		this.label7.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.label7);
		this.horizontalLayout.setComponentAlignment(this.label7, Alignment.MIDDLE_CENTER);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.lblCount.setSizeUndefined();
		this.horizontalLayoutFooter.addComponent(this.lblCount);
		this.horizontalLayoutFooter.setComponentAlignment(this.lblCount, Alignment.TOP_CENTER);
		this.progressBar.setSizeUndefined();
		this.horizontalLayoutFooter.addComponent(this.progressBar);
		this.horizontalLayoutFooter.setComponentAlignment(this.progressBar, Alignment.TOP_CENTER);
		this.horizontalLayoutFooter.setExpandRatio(this.progressBar, 10.0F);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout2);
		this.horizontalLayoutUpload.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutUpload.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutUpload);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutUpload, 20.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 10.0F);
		this.horizontalLayoutFooter.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutFooter.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutFooter);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutFooter, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutFooter, 10.0F);
		this.verticalLayout.setWidth(100, Unit.PERCENTAGE);
		this.verticalLayout.setHeight(-1, Unit.PIXELS);
		this.panel.setContent(this.verticalLayout);
		this.panel.setWidth(100, Unit.PERCENTAGE);
		this.panel.setHeight(-1, Unit.PIXELS);
		this.setContent(this.panel);
		this.setSizeFull();

		this.cmdUpload.addClickListener(event -> this.cmdUpload_buttonClick(event));
		this.cmdProcess.addClickListener(event -> this.cmdProcess_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label2, label3, label, lblFileName, lblSize, label4, labelStatus, label7, lblCount;
	private XdevButton cmdUpload, cmdProcess, cmdCancel;
	private XdevHorizontalLayout horizontalLayout2, horizontalLayoutUpload, horizontalLayout, horizontalLayoutFooter;
	private XdevTextArea textArea;
	private XdevProgressBar progressBar;
	private XdevPanel panel;
	private XdevGridLayout gridLayout;
	private XdevTextField textFieldSheet, textFieldRow;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
