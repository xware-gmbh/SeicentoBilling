
package ch.xwr.seicentobilling.ui.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.ProgressListener;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.business.crm.ZipImporter;
import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.ui.FileUploaderPopup;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


public class ImportZipPopup extends VerticalLayout implements ProgressListener
{
	
	/** Logger initialized */
	private static final Logger LOG     = LoggerFactory.getLogger(ImportZipPopup.class);
	FileUploadDto               result  = null;
	volatile double             current = 0.0;
	
	/**
	 *
	 */
	public ImportZipPopup()
	{

		super();
		this.initUI();
		
		this.lblStatus.setText("");
		this.lblFileName.setText("");
		this.lblSize.setText("");
		this.lblCount.setText("");
	}
	
	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		// win.setSizeFull();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		// win.setWidth("760");
		// win.setHeight("300");
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new ImportZipPopup());
		return win;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdProcess}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_onClick(final ClickEvent<Button> event)
	{
		Seicento.getMemory();
		
		this.horizontalLayoutFooter.setVisible(true);
		// UI.getCurrent().access(()->this.horizontalLayoutFooter.setVisible(true));
		// UI.getCurrent().setPollInterval(500);
		// startV1();
		
		try
		{
			
			// final WorkThread th = new WorkThread(this.result);
			// th.start();
			
			final ZipImporter imp = new ZipImporter();
			imp.addProgressListener(this);
			imp.readFile(this.result.getUpfile());
			
			SeicentoNotification.showInfo("Excel Datei importiert");
			this.lblStatus.setText(imp.getResultString());
			ImportZipPopup.LOG.info("Excel Datei erfolgreich importiert");
			Seicento.getMemory();
			
		}
		catch(final Exception e)
		{
			SeicentoNotification.showError(e.getMessage());
			ImportZipPopup.LOG.error("Fehler bei Import", e);
		}
		finally
		{
			// cleanup
			// this.cmdProcess.setEnabled(false);
			this.horizontalLayoutFooter.setVisible(false);
			// this.progressBar.setIndeterminate(false);
			// this.result.getUpfile().delete();
		}
	}
	
	@Override
	public void updateProgress(final long readBytes, final long contentLength)
	{
		this.lblCount.setText("" + readBytes);
		final Float pv = new Float((float)readBytes / 100);
		// this.cur2 = pv;
		// this.current = pv.doubleValue();

		this.progressBar.setValue(pv);
		UI.getCurrent().access(() -> this.progressBar.setValue(pv));

		// UI.getCurrent().access(new Runnable() {
		// @Override
		// public void run() {
		// //final float current = ImportZipPopup.this.cur2.floatValue();
		// ImportZipPopup.this.progressBar.setValue(ImportZipPopup.this.cur2);
		// if (ImportZipPopup.this.current < 1.0) {
		// ImportZipPopup.this.lblStatus.setValue("" +
		// ((int)(ImportZipPopup.this.current*100)) + "% done");
		// } else {
		// ImportZipPopup.this.lblStatus.setValue("all done");
		// }
		// }
		// });

	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpload_onClick(final ClickEvent<Button> event)
	{
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.csv");
		p1.setSubject("Import City csv Datei");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Dialog dialog = FileUploaderPopup.getPopupWindow();

		dialog.addDetachListener((final DetachEvent e) -> {
			
			ImportZipPopup.this.result = (FileUploadDto)UI.getCurrent().getSession().getAttribute("uploaddto");
			ImportZipPopup.this.cmdProcess.setEnabled(ImportZipPopup.this.result.isSuccess());
			ImportZipPopup.this.lblStatus.setText(ImportZipPopup.this.result.getMessage());
			ImportZipPopup.this.lblSize.setText("" + (ImportZipPopup.this.result.getSize() / 1000) + " KB");
			ImportZipPopup.this.lblFileName.setText(ImportZipPopup.this.result.getUpfile().getName());

		});
		dialog.open();
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdProcess}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_onFocus(final FocusEvent<Button> event)
	{
		this.horizontalLayoutFooter.setVisible(true);
		this.progressBar.setIndeterminate(true);
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout         = new VerticalLayout();
		this.horizontalLayout       = new HorizontalLayout();
		this.icon                   = new Icon(VaadinIcon.FILE_TABLE);
		this.titlelabel             = new Label();
		this.verticalLayout2        = new VerticalLayout();
		this.horizontalLayout6      = new HorizontalLayout();
		this.textArea               = new TextArea();
		this.link                   = new Anchor();
		this.horizontalLayoutUpload = new HorizontalLayout();
		this.label3                 = new Label();
		this.lblFileName            = new Label();
		this.horizontalLayout4      = new HorizontalLayout();
		this.lblSize                = new Label();
		this.horizontalLayout5      = new HorizontalLayout();
		this.label                  = new Label();
		this.lblStatus              = new Label();
		this.formLayout             = new FormLayout();
		this.horizontalLayout2      = new HorizontalLayout();
		this.cmdUpload              = new Button();
		this.cmdProcess             = new Button();
		this.horizontalLayout3      = new HorizontalLayout();
		this.cmdCancel              = new Button();
		this.horizontalLayoutFooter = new HorizontalLayout();
		this.lblCount               = new Label();
		this.progressBar            = new ProgressBar();
		this.binder                 = new BeanValidationBinder<>(Periode.class);

		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.titlelabel.setText("Ortschaften importieren (csv)");
		this.textArea.setValue(
			"Die Funktion ermöglicht den Import des offiziellen Plz Stammsatzes der Post. Eine aktuelle Datei kann unter https://opendata.swiss/de/dataset/plz_verzeichnis bezogen werden. Das Feld plz_coff darf nicht leer sein.");
		this.link.setHref("https://opendata.swiss/de/dataset/plz_verzeichnis");
		this.link.setText("Link Opendata");
		this.link.setTarget("_blank");
		this.label3.setText("Datei");
		this.lblFileName.setText("Label");
		this.lblSize.setText("Label");
		this.label.setText("Status");
		this.lblStatus.setText("Label");
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.cmdUpload.setText("Speichern");
		this.cmdUpload.setIcon(VaadinIcon.UPLOAD.create());
		this.cmdProcess.setText("Schliessen");
		this.cmdProcess.setIcon(VaadinIcon.ROCKET.create());
		this.cmdCancel.setText("Schliessen");
		this.cmdCancel.setIcon(VaadinIcon.CLOSE_SMALL.create());
		this.horizontalLayoutFooter.setSpacing(false);
		this.lblCount.setText("Label");

		this.titlelabel.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.titlelabel);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.titlelabel);
		this.textArea.setWidth("80%");
		this.textArea.setHeight(null);
		this.link.setSizeUndefined();
		this.horizontalLayout6.add(this.textArea, this.link);
		this.label3.setSizeUndefined();
		this.lblFileName.setSizeUndefined();
		this.horizontalLayout4.setWidth("250px");
		this.horizontalLayout4.setHeight("20px");
		this.lblSize.setSizeUndefined();
		this.horizontalLayoutUpload.add(this.label3, this.lblFileName, this.horizontalLayout4, this.lblSize);
		this.label.setSizeUndefined();
		this.lblStatus.setSizeUndefined();
		this.horizontalLayout5.add(this.label, this.lblStatus);
		this.horizontalLayout6.setWidthFull();
		this.horizontalLayout6.setHeight("100px");
		this.horizontalLayoutUpload.setWidthFull();
		this.horizontalLayoutUpload.setHeight("40px");
		this.horizontalLayout5.setWidthFull();
		this.horizontalLayout5.setHeight("40px");
		this.verticalLayout2.add(this.horizontalLayout6, this.horizontalLayoutUpload, this.horizontalLayout5);
		this.cmdUpload.setSizeUndefined();
		this.cmdProcess.setSizeUndefined();
		this.horizontalLayout3.setWidth("100px");
		this.horizontalLayout3.setHeight("30px");
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdUpload, this.cmdProcess, this.horizontalLayout3, this.cmdCancel);
		this.lblCount.setSizeUndefined();
		this.progressBar.setSizeUndefined();
		this.horizontalLayoutFooter.add(this.lblCount, this.progressBar);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.verticalLayout2.setWidthFull();
		this.verticalLayout2.setHeight(null);
		this.formLayout.setWidthFull();
		this.formLayout.setHeight(null);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight(null);
		this.horizontalLayoutFooter.setWidthFull();
		this.horizontalLayoutFooter.setHeight("50px");
		this.verticalLayout.add(this.horizontalLayout, this.verticalLayout2, this.formLayout, this.horizontalLayout2,
			this.horizontalLayoutFooter);
		this.verticalLayout.setWidth("90%");
		this.verticalLayout.setHeight("70%");
		this.add(this.verticalLayout);
		this.setSizeFull();

		this.cmdUpload.addClickListener(this::cmdUpload_onClick);
		this.cmdProcess.addClickListener(this::cmdProcess_onClick);
		this.cmdProcess.addFocusListener(this::cmdProcess_onFocus);
	} // </generated-code>

	// <generated-code name="variables">
	private FormLayout                    formLayout;
	private Button                        cmdUpload, cmdProcess, cmdCancel;
	private Anchor                        link;
	private TextArea                      textArea;
	private ProgressBar                   progressBar;
	private BeanValidationBinder<Periode> binder;
	private VerticalLayout                verticalLayout, verticalLayout2;
	private HorizontalLayout              horizontalLayout, horizontalLayout6, horizontalLayoutUpload,
		horizontalLayout4,
		horizontalLayout5, horizontalLayout2, horizontalLayout3, horizontalLayoutFooter;
	private Label                         titlelabel, label3, lblFileName, lblSize, label, lblStatus, lblCount;
	private Icon                          icon;
	// </generated-code>
	
}
