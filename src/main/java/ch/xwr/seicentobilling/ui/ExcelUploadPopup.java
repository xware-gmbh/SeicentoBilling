
package ch.xwr.seicentobilling.ui;

import java.io.File;

import org.apache.log4j.LogManager;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import ch.xwr.seicentobilling.business.ExcelHandler;
import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.entities.Periode;


public class ExcelUploadPopup extends VerticalLayout
{

	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(ExcelUploadPopup.class);

	private final Periode periodeBean;
	FileUploadDto         result = null;

	/**
	 *
	 */
	public ExcelUploadPopup()
	{

		super();
		this.initUI();

		// get Parameter
		this.periodeBean = (Periode)UI.getCurrent().getSession().getAttribute("periodebean");
		
		this.setDefaultValue();
		
		this.labelStatus.setText("");
		this.lblFileName.setText("");
		this.lblSize.setText("");
		this.lblCount.setText("");
	}

	private void setDefaultValue()
	{
		int isheet = this.periodeBean.getPerMonth().getValue();

		if(isheet == 12)
		{
			isheet = 1;
		}
		else
		{
			isheet = isheet + 1;
		}
		this.textFieldSheet.setValue("" + isheet);

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
		// win.setWidth("860");
		// win.setHeight("450");
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new ExcelUploadPopup());
		return win;
	}

	private void processUploadedFile(final File outFile)
	{
		try
		{
			final int          sheet = new Integer(this.textFieldSheet.getValue()).intValue();
			final ExcelHandler exc   = new ExcelHandler();
			// exc.addProgressListener(this);
			exc.importReportLine(outFile, sheet, this.periodeBean);
			
			SeicentoNotification.showInfo("Excel Datei importiert");
			ExcelUploadPopup.LOG.info("Excel Datei erfolgreich importiert");
			
			this.labelStatus.setText("Daten erfolgreich importiert. Bitte Rapport überprüfen.");
			this.labelStatus.setText(exc.getResultString());
			
		}
		catch(final Exception e)
		{
			this.labelStatus.setText("Importieren ist fehlgeschlagen!");
			SeicentoNotification.showErro("Fehler beim Importieren", e.getMessage());
			ExcelUploadPopup.LOG.error(e.getLocalizedMessage());
		}
		finally
		{
			// cleanup
			outFile.delete();
		}
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCancel}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
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
		p1.setFilter("*.xlsx, *.xlsm");
		p1.setSubject("Import Rapporte Excel Datei");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);
		
		final Dialog dialog = FileUploaderPopup.getPopupWindow();

		dialog.addDetachListener((final DetachEvent e) -> {
			
			ExcelUploadPopup.this.result = (FileUploadDto)UI.getCurrent().getSession().getAttribute("uploaddto");
			ExcelUploadPopup.this.cmdProcess.setEnabled(ExcelUploadPopup.this.result.isSuccess());
			ExcelUploadPopup.this.labelStatus.setText(ExcelUploadPopup.this.result.getMessage());
			ExcelUploadPopup.this.lblSize.setText("" + (ExcelUploadPopup.this.result.getSize() / 1000) + " KB");
			ExcelUploadPopup.this.lblFileName.setText(ExcelUploadPopup.this.result.getUpfile().getName());

		});
		dialog.open();
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdProcess}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdProcess_onClick(final ClickEvent<Button> event)
	{
		try
		{
			// this.progressBar.setVisible(true);
			// this.progressBar.clear();
			// this.progressBar.setImmediate(true);
			// this.progressBar.setValue((float) this.result.getSize());
			// this.progressBar.setIndeterminate(true);
			
			this.processUploadedFile(this.result.getUpfile());
		}
		catch(final Exception e)
		{
			SeicentoNotification.showErro("Fehler beim Importieren", e.getMessage());
			ExcelUploadPopup.LOG.error(e.getLocalizedMessage());
		}
		finally
		{
			// cleanup
			this.horizontalLayoutFooter.setVisible(false);
			this.result.getUpfile().delete();
		}
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
		this.horizontalLayoutUpload = new HorizontalLayout();
		this.formLayout2            = new FormLayout();
		this.formItem               = new FormItem();
		this.label                  = new Label();
		this.textFieldSheet         = new TextField();
		this.formItem2              = new FormItem();
		this.label2                 = new Label();
		this.textFieldRow           = new TextField();
		this.formItem3              = new FormItem();
		this.label3                 = new Label();
		this.lblFileName            = new Label();
		this.lblSize                = new Label();
		this.formItem4              = new FormItem();
		this.label4                 = new Label();
		this.labelStatus            = new Label();
		this.horizontalLayout2      = new HorizontalLayout();
		this.cmdUpload              = new Button();
		this.cmdProcess             = new Button();
		this.horizontalLayout3      = new HorizontalLayout();
		this.cmdCancel              = new Button();
		this.horizontalLayoutFooter = new HorizontalLayout();
		this.lblCount               = new Label();
		this.horizontalLayout4      = new HorizontalLayout();
		this.progressBar            = new ProgressBar();

		this.verticalLayout.setPadding(false);
		this.titlelabel.setText("Rapporte importieren (xls)");
		this.verticalLayout2.setPadding(false);
		this.textArea.setValue(
			"Die Funktion ermöglicht es Rapporte von einem Excel File zu importieren. Das Format der Excel Datei ist definiert und darf nicht abweichen. EinTemplate ist vom Admin erhältlich. Die Reihenfolge der Sheets in Excel ist wie folgt: Stammdaten (0), Dezember Vorjahr (1), Januar (2), Februar.(3).... Mit dieser Reihenfolge ist der Wert im Feld Arbeitsblatt korrekt berechnet.");
		this.formLayout2.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 2, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.label.setText("Arbeitsblatt");
		this.formItem2.getElement().setAttribute("colspan", "2");
		this.label2.setText("Offset Zeile");
		this.label3.setText("Datei");
		this.lblFileName.setText("Label");
		this.lblSize.setText("Label");
		this.formItem4.getElement().setAttribute("colspan", "2");
		this.label4.setText("Status");
		this.labelStatus.setText("Label");
		this.cmdUpload.setText("Datei...");
		this.cmdUpload.setIcon(VaadinIcon.UPLOAD.create());
		this.cmdProcess.setText("Importieren");
		this.cmdProcess.setIcon(VaadinIcon.ROCKET.create());
		this.cmdCancel.setText("Schliessen");
		this.cmdCancel.setIcon(VaadinIcon.CLOSE_SMALL.create());
		this.horizontalLayoutFooter.setSpacing(false);
		this.lblCount.setText("Label");

		this.titlelabel.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.titlelabel);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.titlelabel);
		this.textArea.setWidthFull();
		this.textArea.setHeight(null);
		this.horizontalLayout6.add(this.textArea);
		this.horizontalLayout6.setWidthFull();
		this.horizontalLayout6.setHeight("100px");
		this.verticalLayout2.add(this.horizontalLayout6);
		this.label2.setSizeUndefined();
		this.label2.getElement().setAttribute("slot", "label");
		this.textFieldRow.setWidth("30%");
		this.textFieldRow.setHeight(null);
		this.formItem2.add(this.label2, this.textFieldRow);
		this.label3.setSizeUndefined();
		this.label3.getElement().setAttribute("slot", "label");
		this.lblFileName.setWidth("367px");
		this.lblFileName.setHeight(null);
		this.formItem3.add(this.label3, this.lblFileName);
		this.label4.setSizeUndefined();
		this.label4.getElement().setAttribute("slot", "label");
		this.labelStatus.setSizeFull();
		this.formItem4.add(this.label4, this.labelStatus);
		this.lblSize.setSizeFull();
		this.formLayout2.add(this.formItem, this.formItem2, this.formItem3, this.lblSize, this.formItem4);
		this.formLayout2.setSizeFull();
		this.horizontalLayoutUpload.add(this.formLayout2);
		this.cmdUpload.setSizeUndefined();
		this.cmdProcess.setSizeUndefined();
		this.horizontalLayout3.setWidth("100px");
		this.horizontalLayout3.setHeight("30px");
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdUpload, this.cmdProcess, this.horizontalLayout3, this.cmdCancel);
		this.lblCount.setWidth("200px");
		this.lblCount.setHeightFull();
		this.horizontalLayout4.setWidth("200px");
		this.horizontalLayout4.setHeight("40px");
		this.progressBar.setWidth("20%");
		this.progressBar.setHeight(null);
		this.horizontalLayoutFooter.add(this.lblCount, this.horizontalLayout4, this.progressBar);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.verticalLayout2.setWidthFull();
		this.verticalLayout2.setHeight(null);
		this.horizontalLayoutUpload.setWidthFull();
		this.horizontalLayoutUpload.setHeight(null);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("50px");
		this.horizontalLayoutFooter.setWidthFull();
		this.horizontalLayoutFooter.setHeight("50px");
		this.verticalLayout.add(this.horizontalLayout, this.verticalLayout2, this.horizontalLayoutUpload,
			this.horizontalLayout2, this.horizontalLayoutFooter);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setSizeFull();

		this.cmdUpload.addClickListener(this::cmdUpload_onClick);
		this.cmdProcess.addClickListener(this::cmdProcess_onClick);
		this.cmdProcess.addFocusListener(this::cmdProcess_onFocus);
		this.cmdCancel.addClickListener(this::cmdCancel_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private FormLayout       formLayout2;
	private Button           cmdUpload, cmdProcess, cmdCancel;
	private TextArea         textArea;
	private ProgressBar      progressBar;
	private VerticalLayout   verticalLayout, verticalLayout2;
	private HorizontalLayout horizontalLayout, horizontalLayout6, horizontalLayoutUpload, horizontalLayout2,
		horizontalLayout3, horizontalLayoutFooter, horizontalLayout4;
	private Label            titlelabel, label, label2, label3, lblFileName, lblSize, label4, labelStatus, lblCount;
	private Icon             icon;
	private TextField        textFieldSheet, textFieldRow;
	private FormItem         formItem, formItem2, formItem3, formItem4;
	// </generated-code>
	
}
