
package ch.xwr.seicentobilling.ui.crm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;

import ch.xwr.seicentobilling.business.crm.CrmExcelHandler;
import ch.xwr.seicentobilling.business.crm.CustomerDto;
import ch.xwr.seicentobilling.business.crm.VcardImporter;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.ui.FileUploaderPopup;
import ch.xwr.seicentobilling.ui.SeicentoNotification;


public class ImportContactsPopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ImportContactsPopup.class);

	protected FileUploadDto result;
	
	public static enum ImportType
	{
		excel,
		vcard
	}
	
	/**
	 *
	 */
	public ImportContactsPopup()
	{
		super();
		this.initUI();
		this.labelSize.setText("");
		this.labelResult.setText("");
		this.labelType.setText("Zum Hochladen, bitte Dateityp wählen");
		this.labelHeader.setText("");
		this.labelFileName.setText("");

		this.comboBoxType.setItems(ImportType.values());
		this.comboBoxType.addValueChangeListener(this::comboBoxValueChange);

		// setupUploader();
		this.cmdStartImport.setEnabled(false);
		this.cmdSave.setEnabled(false);
		this.cmdSelect.setEnabled(false);
	}
	
	private void comboBoxValueChange(final ComponentValueChangeEvent<ComboBox<ImportType>, ImportType> event)
	{
		final ImportType type = event.getValue();
		
		this.labelType.setText("");
		if(type != null)
		{
			if(type.name().equals("excel"))
			{
				// setupUploader();
				this.setupUploaderExcel();
				this.labelType.setText("Excel importieren");
				this.labelHeader.setText(
					"CAM1;CAM2;Nachname;Vorname;Firma;x;Adresse;PLZ;Ort;;TelefonG;Mobile;TelefonP;x;E-Mail;Webseite;x;Newsletter;X-Mas;Verwendung;Notizen");
			}
			else
			{
				this.setupUploaderVcard();
				this.labelType.setText("Vcard importieren");
				this.labelHeader.setText("Version >4.0");
			}
		}
		
	}
	
	private void setupUploaderVcard()
	{
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.vcard, *.vcf");
		p1.setSubject("Import VCard");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Dialog dialog = FileUploaderPopup.getPopupWindow();
		
		dialog.addDetachListener((final DetachEvent e) -> {
			
			ImportContactsPopup.this.result = (FileUploadDto)UI.getCurrent().getSession().getAttribute("uploaddto");
			this.showUploadResult();
			
		});
		dialog.open();

	}

	private void setupUploaderExcel()
	{
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.xlsx, *.xls");
		p1.setSubject("Import Excel");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Dialog dialog = FileUploaderPopup.getPopupWindow();
		
		dialog.addDetachListener((final DetachEvent e) -> {
			
			ImportContactsPopup.this.result = (FileUploadDto)UI.getCurrent().getSession().getAttribute("uploaddto");
			this.showUploadResult();
			
		});
		dialog.open();

	}

	private void showUploadResult()
	{
		if(this.result == null)
		{
			return;
		}
		
		this.labelFileName.setText(this.result.getUpfile().getName());
		this.labelSize.setText((this.result.getSize() / 1000) + " KB");
		
		this.cmdStartImport.setEnabled(this.result.isSuccess());
		
		if(!this.result.isSuccess())
		{
			this.cmdSave.setEnabled(false);
			this.cmdSelect.setEnabled(false);
		}
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
		// win.setWidth("760");
		// win.setHeight("300");
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new ImportContactsPopup());
		return win;
	}

	private void initTreeGrid(final List<CustomerDto> lst)
	{
		this.grid.setItems(lst);
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
		final CrmExcelHandler exc     = new CrmExcelHandler();
		int                   icount  = 0;
		int                   ierror  = 0;
		int                   iselect = 0;
		
		ImportContactsPopup.LOG.info("Start saving list to Database");
		
		try
		{
			for(final CustomerDto cus : this.grid.getSelectedItems())
			{
				
				iselect++;
				// persist
				try
				{
					if(exc.saveDto(cus))
					{
						icount++;
						this.grid.getSelectionModel().deselect(cus);
					}
					else
					{
						ierror++;
					}
					
				}
				catch(final Exception e)
				{
					ImportContactsPopup.LOG.error("Could not import Contact " + cus.getCustomer().getFullname());
					ierror++;
				}
			}
			
			SeicentoNotification.showInfo("Save clicked", "Daten wurden gespeichert");
		}
		catch(final Exception e)
		{
			SeicentoNotification.showErro("Fehler beim Speichern", e.getMessage());
			e.printStackTrace();
		}
		
		this.labelResult.setText("Selektiert: " + iselect + " Importiert: " + icount + " Fehler: " + ierror);
		
		SeicentoNotification.showInfo("Liste erfolgreich importiert");
		ImportContactsPopup.LOG.info("Liste erfolgreich importiert");
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdStartImport}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStartImport_onClick(final ClickEvent<Button> event)
	{
		if(this.result != null && this.result.isSuccess())
		{
			if(this.comboBoxType.getValue().equals(ImportType.excel))
			{
				this.processUploadedFile(this.result.getUpfile());
			}
			if(this.comboBoxType.getValue().equals(ImportType.vcard))
			{
				this.processUploadedFileVcard(this.result.getUpfile());
			}
			
			this.cmdSave.setEnabled(true);
			this.cmdSelect.setEnabled(true);
		}
		
	}
	
	private void processUploadedFile(final File outFile)
	{
		final CrmExcelHandler   exc = new CrmExcelHandler();
		final List<CustomerDto> lst = exc.readContactsToList(outFile);

		if(!lst.isEmpty())
		{
			this.labelSize.setText("Zeilen: " + lst.size());
		}

		outFile.delete();
		this.initTreeGrid(lst);
	}
	
	private void processUploadedFileVcard(final File upfile)
	{
		final VcardImporter vc  = new VcardImporter(upfile);
		final CustomerDto   dto = vc.processVcard();

		try
		{
			final List<CustomerDto> lst = new ArrayList<>();
			lst.add(dto);

			if(!lst.isEmpty())
			{
				this.labelSize.setText("Zeilen: " + lst.size());
			}

			this.initTreeGrid(lst);

			// vc.saveVcard();

			final String msg = "Vcard importiert.";
			this.labelResult.setText(msg);
			this.cmdStartImport.setEnabled(false);

		}
		catch(final PersistenceException ex)
		{
			final String msg = SeicentoCrud.getPerExceptionError(ex);
			ImportContactsPopup.LOG.error(msg);
			SeicentoNotification.showErro("Fehler beim Speichern der Vcard", msg);
		}
		catch(final Exception e)
		{
			ImportContactsPopup.LOG.error(e.getMessage());
			SeicentoNotification.showInfo("Fehler beim Speichern der Vcard", e.getMessage());
		}
	}

	boolean selectAll = false;

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSelect}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSelect_onClick(final ClickEvent<Button> event)
	{
		if(!this.selectAll)
		{
			((GridMultiSelectionModel<CustomerDto>)this.grid.getSelectionModel()).selectAll();
			this.selectAll = true;
		}
		else
		{
			((GridMultiSelectionModel<CustomerDto>)this.grid.getSelectionModel()).deselectAll();
			this.selectAll = false;
		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout            = new SplitLayout();
		this.verticalLayout         = new VerticalLayout();
		this.horizontalLayout       = new HorizontalLayout();
		this.icon                   = new Icon(VaadinIcon.FILE_TABLE);
		this.titlelabel             = new Label();
		this.verticalLayout2        = new VerticalLayout();
		this.horizontalLayout6      = new HorizontalLayout();
		this.comboBoxType           = new ComboBox<>();
		this.labelType              = new Label();
		this.buttonHorizontalLayout = new HorizontalLayout();
		this.cmdStartImport         = new Button();
		this.labelFileName          = new Label();
		this.labelSize              = new Label();
		this.horizontalLayout2      = new HorizontalLayout();
		this.cmdSelect              = new Button();
		this.cmdSave                = new Button();
		this.cmdReset               = new Button();
		this.horizontalLayout3      = new HorizontalLayout();
		this.labelResult            = new Label();
		this.labelHeader            = new Label();
		this.grid                   = new Grid<>(CustomerDto.class, false);

		this.splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.titlelabel.setText("Import Datei");
		this.verticalLayout2.setPadding(false);
		this.comboBoxType.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.labelType.setText("Label");
		this.cmdStartImport.setText("Importieren");
		this.cmdStartImport.setIcon(VaadinIcon.ROCKET.create());
		this.labelFileName.setText("Label");
		this.labelSize.setText("Label");
		this.cmdSelect.setText("Select/Unselect");
		this.cmdSelect.setIcon(IronIcons.CHECK_BOX.create());
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Schliessen");
		this.cmdReset.setIcon(VaadinIcon.CLOSE_SMALL.create());
		this.labelResult.setText("Label");
		this.labelHeader.setText(
			"CAM1;CAM2;Nachname;Vorname;Firma;x;Adresse;PLZ;Ort;;TelefonG;Mobile;TelefonP;x;E-Mail;Webseite;x;Newsletter;X-Mas;Verwendung;Notizen");
		this.labelHeader.getStyle().set("font-size", "10px");
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(CustomerDto::getCustomer).map(Customer::getCusCompany).orElse(null))
			.setKey("customer.cusCompany").setHeader("Firma").setSortable(true);
		this.grid
			.addColumn(v -> Optional.ofNullable(v).map(CustomerDto::getCustomer).map(Customer::getCusName).orElse(null))
			.setKey("customer.cusName").setHeader("Name").setSortable(true);
		this.grid
			.addColumn(
				v -> Optional.ofNullable(v).map(CustomerDto::getCustomer).map(Customer::getCusAddress).orElse(null))
			.setKey("customer.cusAddress").setHeader("Adresse").setSortable(true);
		this.grid.addColumn(v -> Optional.ofNullable(v).map(CustomerDto::getCustomer).map(Customer::getCity)
			.map(City::getfullname).orElse(null)).setKey("customer.city.fullname").setHeader("Ort").setSortable(true);
		this.grid.setSelectionMode(Grid.SelectionMode.MULTI);

		this.titlelabel.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.titlelabel);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.titlelabel);
		this.comboBoxType.setSizeUndefined();
		this.labelType.setWidth("270px");
		this.labelType.setHeight("40px");
		this.horizontalLayout6.add(this.comboBoxType, this.labelType);
		this.horizontalLayout6.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.labelType);
		this.horizontalLayout6.setFlexGrow(1.0, this.labelType);
		this.cmdStartImport.setWidth("25%");
		this.cmdStartImport.setHeight(null);
		this.labelFileName.setWidth("100px");
		this.labelFileName.setHeight("40px");
		this.labelSize.setWidth("270px");
		this.labelSize.setHeight("40px");
		this.buttonHorizontalLayout.add(this.cmdStartImport, this.labelFileName, this.labelSize);
		this.buttonHorizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdStartImport);
		this.horizontalLayout6.setWidthFull();
		this.horizontalLayout6.setHeight("60px");
		this.buttonHorizontalLayout.setWidthFull();
		this.buttonHorizontalLayout.setHeight("60px");
		this.verticalLayout2.add(this.horizontalLayout6, this.buttonHorizontalLayout);
		this.cmdSelect.setWidth("30%");
		this.cmdSelect.setHeight(null);
		this.cmdSave.setWidth("25%");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("25%");
		this.cmdReset.setHeight(null);
		this.horizontalLayout3.setWidth("100px");
		this.horizontalLayout3.setHeight("30px");
		this.labelResult.setWidth("250px");
		this.labelResult.setHeightFull();
		this.horizontalLayout2.add(this.cmdSelect, this.cmdSave, this.cmdReset, this.horizontalLayout3,
			this.labelResult);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdSelect);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdSave);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.cmdReset);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.verticalLayout2.setWidthFull();
		this.verticalLayout2.setHeight(null);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("60px");
		this.labelHeader.setWidthFull();
		this.labelHeader.setHeight(null);
		this.verticalLayout.add(this.horizontalLayout, this.verticalLayout2, this.horizontalLayout2, this.labelHeader);
		this.splitLayout.addToPrimary(this.verticalLayout);
		this.splitLayout.addToSecondary(this.grid);
		this.splitLayout.setSplitterPosition(50.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();

		this.cmdStartImport.addClickListener(this::cmdStartImport_onClick);
		this.cmdSelect.addClickListener(this::cmdSelect_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private ComboBox<ImportType> comboBoxType;
	private Button               cmdStartImport, cmdSelect, cmdSave, cmdReset;
	private SplitLayout          splitLayout;
	private Grid<CustomerDto>    grid;
	private VerticalLayout       verticalLayout, verticalLayout2;
	private HorizontalLayout     horizontalLayout, horizontalLayout6, buttonHorizontalLayout, horizontalLayout2,
		horizontalLayout3;
	private Label                titlelabel, labelType, labelFileName, labelSize, labelResult, labelHeader;
	private Icon                 icon;
	// </generated-code>

}
