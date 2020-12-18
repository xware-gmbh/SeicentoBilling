package ch.xwr.seicentobilling.ui.desktop.crm;

import org.apache.log4j.LogManager;

import com.vaadin.annotations.Push;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevLink;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevProgressBar;
import com.xdev.ui.XdevTextArea;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.crm.ZipImporter;
import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.ui.desktop.FileUploaderPopup;

@Push(PushMode.MANUAL)
public class ImportZipPopup extends XdevView implements ProgressListener {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ImportZipPopup.class);
	FileUploadDto result = null;
    volatile double current = 0.0;

	/**
	 *
	 */
	public ImportZipPopup() {
		super();
		this.initUI();

		//setupUploader();

		this.lblStatus.setValue("");
		this.lblFileName.setValue("");
		this.lblSize.setValue("");
		this.lblCount.setValue("");

	}

	public static Window getPopupWindow() {
		final Window win = new Window();

		win.setWidth("760");
		win.setHeight("300");
		win.center();
		win.setModal(true);
		win.setContent(new ImportZipPopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdProcess}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_buttonClick(final Button.ClickEvent event) {
		Seicento.getMemory();

		this.horizontalLayoutFooter.setVisible(true);
		//UI.getCurrent().access(()->this.horizontalLayoutFooter.setVisible(true));
//		UI.getCurrent().setPollInterval(500);
		//startV1();

		try {

//			final WorkThread th = new WorkThread(this.result);
//			th.start();

			final ZipImporter imp = new ZipImporter();
			imp.addProgressListener(this);
			imp.readFile(this.result.getUpfile());

			Notification.show("Excel Datei importiert", Type.TRAY_NOTIFICATION);
			this.lblStatus.setValue(imp.getResultString());
			LOG.info("Excel Datei erfolgreich importiert");
			Seicento.getMemory();

		} catch (final Exception e) {
			Notification.show("Fehler beim Importieren", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			LOG.error("Fehler bei Import", e);
		} finally {
			// cleanup
			//this.cmdProcess.setEnabled(false);
			this.horizontalLayoutFooter.setVisible(false);
			//this.progressBar.setIndeterminate(false);
			//this.result.getUpfile().delete();
		}

		//UI.getCurrent().setPollInterval(-1);

	}

	@Override
	public void updateProgress(final long readBytes, final long contentLength) {
		this.lblCount.setValue("" +readBytes);
		final Float pv = new Float((float)readBytes / 100);
//		this.cur2 = pv;
//		this.current = pv.doubleValue();

		this.progressBar.setValue(pv);
		UI.getCurrent().access(()->this.progressBar.setValue(pv));


//        UI.getCurrent().access(new Runnable() {
//            @Override
//            public void run() {
//            	//final float current = ImportZipPopup.this.cur2.floatValue();
//            	ImportZipPopup.this.progressBar.setValue(ImportZipPopup.this.cur2);
//                if (ImportZipPopup.this.current < 1.0) {
//                	ImportZipPopup.this.lblStatus.setValue("" +
//                        ((int)(ImportZipPopup.this.current*100)) + "% done");
//				} else {
//					ImportZipPopup.this.lblStatus.setValue("all done");
//				}
//            }
//        });

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdUpload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpload_buttonClick(final Button.ClickEvent event) {
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.csv");
		p1.setSubject("Import City csv Datei");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Window win = FileUploaderPopup.getPopupWindow();
		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				ImportZipPopup.this.result = (FileUploadDto) UI.getCurrent().getSession().getAttribute("uploaddto");
				ImportZipPopup.this.cmdProcess.setEnabled(ImportZipPopup.this.result.isSuccess());
				ImportZipPopup.this.lblStatus.setValue(ImportZipPopup.this.result.getMessage());
				ImportZipPopup.this.lblSize.setValue("" + (ImportZipPopup.this.result.getSize() / 1000) + " KB");
				ImportZipPopup.this.lblFileName.setValue(ImportZipPopup.this.result.getUpfile().getName());
			}
		});
		this.getUI().addWindow(win);

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
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdProcess}.
	 *
	 * @see FieldEvents.FocusListener#focus(FieldEvents.FocusEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_focus(final FieldEvents.FocusEvent event) {
		this.horizontalLayoutFooter.setVisible(true);
		this.progressBar.setIndeterminate(true);
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
		this.link = new XdevLink();
		this.horizontalLayoutUpload = new XdevHorizontalLayout();
		this.gridLayout = new XdevGridLayout();
		this.label3 = new XdevLabel();
		this.lblFileName = new XdevLabel();
		this.lblSize = new XdevLabel();
		this.label = new XdevLabel();
		this.lblStatus = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdUpload = new XdevButton();
		this.cmdProcess = new XdevButton();
		this.label2 = new XdevLabel();
		this.cmdCancel = new XdevButton();
		this.horizontalLayoutFooter = new XdevHorizontalLayout();
		this.lblCount = new XdevLabel();
		this.progressBar = new XdevProgressBar();

		this.panel.setIcon(FontAwesome.FILE_EXCEL_O);
		this.panel.setCaption("Ortschaften importieren (csv)");
		this.panel.setTabIndex(0);
		this.verticalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.horizontalLayout2.setCaption("");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.textArea.setValue(
				"Die Funktion ermöglicht den Import des offiziellen Plz Stammsatzes der Post. Eine aktuelle Datei kann unter https://opendata.swiss/de/dataset/plz_verzeichnis bezogen werden. Das Feld plz_coff darf nicht leer sein.");
		this.textArea.setReadOnly(true);
		this.textArea.setRows(3);
		this.link.setTargetName("_blank");
		this.link.setCaption("Link Opendata");
		this.link.setResource(new ExternalResource("https://opendata.swiss/de/dataset/plz_verzeichnis"));
		this.horizontalLayoutUpload.setMargin(new MarginInfo(false));
		this.gridLayout.setMargin(new MarginInfo(false));
		this.label3.setValue("Datei");
		this.lblFileName.setValue("Label");
		this.lblSize.setValue("Label");
		this.label.setValue("Status");
		this.lblStatus.setValue("Label");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdUpload.setIcon(FontAwesome.UPLOAD);
		this.cmdUpload.setCaption("Datei...");
		this.cmdProcess.setIcon(FontAwesome.ROCKET);
		this.cmdProcess.setCaption("Importieren");
		this.cmdProcess.setEnabled(false);
		this.cmdProcess.setDisableOnClick(true);
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption("Schliessen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.horizontalLayoutFooter.setMargin(new MarginInfo(false, true, true, false));
		this.horizontalLayoutFooter.setVisible(false);
		this.lblCount.setValue("Label");
		this.progressBar.setEnabled(false);

		this.textArea.setWidth(100, Unit.PERCENTAGE);
		this.textArea.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout2.addComponent(this.textArea);
		this.horizontalLayout2.setComponentAlignment(this.textArea, Alignment.MIDDLE_CENTER);
		this.horizontalLayout2.setExpandRatio(this.textArea, 80.0F);
		this.link.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.link);
		this.horizontalLayout2.setExpandRatio(this.link, 20.0F);
		this.gridLayout.setColumns(3);
		this.gridLayout.setRows(2);
		this.label3.setSizeUndefined();
		this.gridLayout.addComponent(this.label3, 0, 0);
		this.lblFileName.setSizeUndefined();
		this.gridLayout.addComponent(this.lblFileName, 1, 0);
		this.lblSize.setSizeUndefined();
		this.gridLayout.addComponent(this.lblSize, 2, 0);
		this.label.setSizeUndefined();
		this.gridLayout.addComponent(this.label, 0, 1);
		this.lblStatus.setWidth(100, Unit.PERCENTAGE);
		this.lblStatus.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.lblStatus, 1, 1, 2, 1);
		this.gridLayout.setComponentAlignment(this.lblStatus, Alignment.MIDDLE_CENTER);
		this.gridLayout.setColumnExpandRatio(1, 10.0F);
		this.gridLayout.setColumnExpandRatio(2, 10.0F);
		this.gridLayout.setRowExpandRatio(1, 10.0F);
		this.gridLayout.setSizeFull();
		this.horizontalLayoutUpload.addComponent(this.gridLayout);
		this.horizontalLayoutUpload.setComponentAlignment(this.gridLayout, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutUpload.setExpandRatio(this.gridLayout, 10.0F);
		this.cmdUpload.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdUpload);
		this.cmdProcess.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdProcess);
		this.label2.setWidth(78, Unit.PIXELS);
		this.label2.setHeight(-1, Unit.PIXELS);
		this.horizontalLayout.addComponent(this.label2);
		this.horizontalLayout.setComponentAlignment(this.label2, Alignment.MIDDLE_CENTER);
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
		this.progressBar.setWidth(100, Unit.PERCENTAGE);
		this.progressBar.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutFooter.addComponent(this.progressBar);
		this.horizontalLayoutFooter.setComponentAlignment(this.progressBar, Alignment.MIDDLE_CENTER);
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
		this.horizontalLayoutFooter.setHeight(60, Unit.PIXELS);
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
		this.cmdProcess.addFocusListener(event -> this.cmdProcess_focus(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label3, lblFileName, lblSize, label, lblStatus, label2, lblCount;
	private XdevButton cmdUpload, cmdProcess, cmdCancel;
	private XdevHorizontalLayout horizontalLayout2, horizontalLayoutUpload, horizontalLayout, horizontalLayoutFooter;
	private XdevTextArea textArea;
	private XdevProgressBar progressBar;
	private XdevPanel panel;
	private XdevLink link;
	private XdevGridLayout gridLayout;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>


}

