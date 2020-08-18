package ch.xwr.seicentobilling.ui.desktop.crm;

import java.io.File;
import java.util.List;

import javax.persistence.PersistenceException;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTreeTable;
import com.xdev.ui.XdevVerticalSplitPanel;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.combobox.XdevComboBox;

import ch.xwr.seicentobilling.business.crm.CrmExcelHandler;
import ch.xwr.seicentobilling.business.crm.CustomerDto;
import ch.xwr.seicentobilling.business.crm.VcardImporter;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.Activity_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.ui.desktop.ExcelUploadPopup;
import ch.xwr.seicentobilling.ui.desktop.FileUploaderPopup;

public class ImportContactsPopup extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExcelUploadPopup.class);

	public static enum ImportType {
		excel, vcard
	}
	protected FileUploadDto result;

	/**
	 *
	 */
	public ImportContactsPopup() {
		super();
		this.initUI();

		this.labelSize.setValue("");
		this.labelResult.setValue("");
		this.labelType.setValue("Zum Hochladen, bitte Dateityp wählen");
		this.labelHeader.setValue("");
		this.labelFileName.setValue("");

		this.comboBoxType.addItems((Object[]) ImportType.values());
		//this.comboBoxType.setValue(ImportType.excel); //trigger event

		//setupUploader();
		this.cmdStartImport.setEnabled(false);
		this.cmdSave.setEnabled(false);
		this.cmdSelect.setEnabled(false);

	}

	private void processUploadedFile(final File outFile) {
		final CrmExcelHandler exc = new CrmExcelHandler();
		final List<CustomerDto> lst = exc.readContactsToList(outFile);

		if (!lst.isEmpty()) {
			this.labelSize.setValue("Zeilen: " + lst.size());
		}

		outFile.delete();
		initTreeGrid(lst);
	}


//	private void setROFields() {
//		this.dateExpBooked.setEnabled(false);
//		this.cmbPeriode.setEnabled(false);
//	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.setWidth("1200");
		win.setHeight("860");
		win.center();
		win.setModal(true);
		win.setContent(new ImportContactsPopup());

		return win;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
		final CrmExcelHandler exc = new CrmExcelHandler();
		int icount = 0;
		int ierror = 0;
		int iselect = 0;

		LOG.info("Start saving list to Database");

		try {
			for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
				final Item item = this.treeGrid.getItem(itemId);
				final XdevCheckBox cbo = (XdevCheckBox) item.getItemProperty("").getValue();
				final CustomerDto cus = (CustomerDto) item.getItemProperty("Objekt").getValue();

				if (cbo != null && cbo.getValue() && cus != null) {
					iselect++;
					//persist
					try {
						if (exc.saveDto(cus)) {
							icount++;
							cbo.setValue(false);
							cbo.setEnabled(false);
						} else {
							ierror++;
						}

					} catch (final Exception e) {
						LOG.error("Could not import Contact " + cus.getCustomer().getFullname());
						ierror++;
					}
				}
	     	}
			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

		this.labelResult.setValue("Selektiert: " + iselect + " Importiert: " + icount + " Fehler: " + ierror);

		Notification.show("Liste erfolgreich importiert", Type.TRAY_NOTIFICATION);
		LOG.info("Liste erfolgreich importiert");
	}

	private void initTreeGrid(final List<CustomerDto> lst) {
		//reset
		this.treeGrid.removeAllItems();
		this.treeGrid.removeContainerProperty("");
		this.treeGrid.removeContainerProperty("Firma");
		this.treeGrid.removeContainerProperty("Name");
		this.treeGrid.removeContainerProperty("Adresse");
		this.treeGrid.removeContainerProperty("Ort");
		this.treeGrid.removeContainerProperty("Objekt");

		//rebuild
		this.treeGrid.addContainerProperty("", XdevCheckBox.class, true);
		this.treeGrid.addContainerProperty("Firma", String.class, null);
		this.treeGrid.addContainerProperty("Name", String.class, null);
		this.treeGrid.addContainerProperty("Adresse", String.class, null);
		this.treeGrid.addContainerProperty("Ort", String.class, null);
		this.treeGrid.addContainerProperty("Objekt", CustomerDto.class, null);

		int icount = 0;
		int iselect = 0;
		for (final CustomerDto cusDto : lst) {
			final Object[] parent = getParentLine(cusDto);
			this.treeGrid.addItem(parent, icount);
			icount++;

			//final Item item = this.treeGrid.getItem(icount);
			final XdevCheckBox cbo = (XdevCheckBox) parent[0];
			if (cbo.isEnabled()) {
				iselect++;
			}
		}
		if (!lst.isEmpty()) {
			this.labelSize.setValue("Zeilen: " + lst.size() + " gültig: " + iselect);
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

		this.treeGrid.setVisibleColumns("", "Firma", "Name", "Adresse", "Ort");
		this.treeGrid.setColumnAlignments(Align.LEFT,Align.LEFT,Align.LEFT,Align.LEFT,Align.LEFT);
	}

	private final Object[] getParentLine(final CustomerDto cusDto) {
		final XdevCheckBox cbo = new XdevCheckBox();
		cbo.setValue(true);
		if (cusDto.getCustomer().getCusId() != null || cusDto.getCustomer().getCity().getCtyZip() == 0) {
			cbo.setValue(false);
			cbo.setEnabled(false);
		}

		final String cusCompany = cusDto.getCustomer().getCusCompany();
		final String cusName = cusDto.getCustomer().getCusName() + " " + cusDto.getCustomer().getCusFirstName();
		final String cusAdr = cusDto.getCustomer().getCusAddress();
		final String cusPlace = cusDto.getCustomer().getCity().getfullname();

		final Object[] retval = new Object[] {cbo, cusCompany, cusName, cusAdr, cusPlace, cusDto};
		return retval;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSelect}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSelect_buttonClick(final Button.ClickEvent event) {
		for (final Object itemId: this.treeGrid.getContainerDataSource().getItemIds()) {
			final Item item = this.treeGrid.getItem(itemId);
			final XdevCheckBox cbo = (XdevCheckBox) item.getItemProperty("").getValue();

			if (cbo.isEnabled()) {
				cbo.setValue(!cbo.getValue());
			}
     	}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevComboBox}
	 * {@link #comboBoxType}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void comboBoxType_valueChange(final Property.ValueChangeEvent event) {
		final ImportType type = (ImportType) event.getProperty().getValue();

		this.labelType.setValue("");
		if (type != null) {
			if (type.name().equals("excel")) {
				//setupUploader();
				setupUploaderExcel();
				this.labelType.setValue("Excel importieren");
				this.labelHeader.setValue("CAM1;CAM2;Nachname;Vorname;Firma;x;Adresse;PLZ;Ort;;TelefonG;Mobile;TelefonP;x;E-Mail;Webseite;x;Newsletter;X-Mas;Verwendung;Notizen");
			} else {
				setupUploaderVcard();
				this.labelType.setValue("Vcard importieren");
				this.labelHeader.setValue("Version >4.0");
			}
		}
	}

	private void setupUploaderVcard() {
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.vcard, *.vcf");
		p1.setSubject("Import VCard");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Window win = FileUploaderPopup.getPopupWindow();
		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				ImportContactsPopup.this.result = (FileUploadDto) UI.getCurrent().getSession().getAttribute("uploaddto");
				showUploadResult();
			}
		});
		this.getUI().addWindow(win);
	}

	private void setupUploaderExcel() {
		final FileUploadDto p1 = new FileUploadDto();
		p1.setFilter("*.xlsx, *.xls");
		p1.setSubject("Import Excel");
		UI.getCurrent().getSession().setAttribute("uploaddto", p1);

		final Window win = FileUploaderPopup.getPopupWindow();
		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				ImportContactsPopup.this.result = (FileUploadDto) UI.getCurrent().getSession().getAttribute("uploaddto");
				showUploadResult();
			}
		});
		this.getUI().addWindow(win);
	}

	private void showUploadResult() {
		if (this.result ==null) {
			return;
		}

		this.labelFileName.setValue(this.result.getUpfile().getName());
		this.labelSize.setValue( (this.result.getSize() / 1000) + " KB");

		this.cmdStartImport.setEnabled(this.result.isSuccess());

		if (!this.result.isSuccess()) {
			this.cmdSave.setEnabled(false);
			this.cmdSelect.setEnabled(false);
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdStartImport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdStartImport_buttonClick(final Button.ClickEvent event) {
		if (this.result != null && this.result.isSuccess()) {
			if (this.comboBoxType.getValue().equals(ImportType.excel)) {
			    processUploadedFile(this.result.getUpfile());

				this.cmdSave.setEnabled(true);
				this.cmdSelect.setEnabled(true);
			}
			if (this.comboBoxType.getValue().equals(ImportType.vcard)) {
			    processUploadedFileVcard(this.result.getUpfile());
			}

		}

	}

	private void processUploadedFileVcard(final File upfile) {
		final VcardImporter vc = new VcardImporter(upfile);
		final Customer cus = vc.processVcard();

		try {
			vc.saveVcard();

			final String msg = "Vcard importiert. Adr# " + cus.getCusNumber();
			this.labelResult.setValue(msg);
			Notification.show(msg, Type.TRAY_NOTIFICATION);
			LOG.info(msg);

			this.cmdStartImport.setEnabled(false);
		} catch (final PersistenceException ex) {
			final String msg = SeicentoCrud.getPerExceptionError(ex);
			LOG.error(msg);
			Notification.show("Fehler beim Speichern der Vcard", msg, Notification.Type.ERROR_MESSAGE);
		} catch (final Exception e) {
			LOG.error(e.getMessage());
			Notification.show("Fehler beim Speichern der Vcard", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.comboBoxType = new XdevComboBox<>();
		this.labelType = new XdevLabel();
		this.horizontalLayout2 = new XdevHorizontalLayout();
		this.cmdStartImport = new XdevButton();
		this.labelFileName = new XdevLabel();
		this.labelSize = new XdevLabel();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSelect = new XdevButton();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.labelResult = new XdevLabel();
		this.labelHeader = new XdevLabel();
		this.fieldGroup = new XdevFieldGroup<>(Activity.class);
		this.treeGrid = new XdevTreeTable();

		this.setCaption("Aktivität");
		this.verticalSplitPanel.setStyleName("large");
		this.verticalSplitPanel.setSplitPosition(35.0F, Unit.PERCENTAGE);
		this.panel.setIcon(FontAwesome.FILE_TEXT);
		this.panel.setCaption("Import Datei");
		this.panel.setTabIndex(0);
		this.labelType.setValue("Label");
		this.horizontalLayout2.setMargin(new MarginInfo(false));
		this.cmdStartImport.setIcon(FontAwesome.ROCKET);
		this.cmdStartImport.setCaption("Importieren");
		this.labelFileName.setValue("Label");
		this.labelSize.setValue("Label");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSelect.setIcon(FontAwesome.CHECK_SQUARE);
		this.cmdSelect.setCaption("Select/Unselect");
		this.cmdSelect.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.cmdSave.setIcon(FontAwesome.SAVE);
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(FontAwesome.CLOSE);
		this.cmdReset.setCaption("Schliessen");
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.labelResult.setValue("Label");
		this.labelHeader.setStyleName("tiny");
		this.labelHeader.setValue(
				"CAM1;CAM2;Nachname;Vorname;Firma;x;Adresse;PLZ;Ort;;TelefonG;Mobile;TelefonP;x;E-Mail;Webseite;x;Newsletter;X-Mas;Verwendung;Notizen");
		this.fieldGroup.bind(this.comboBoxType, Activity_.actType.getName());

		this.cmdStartImport.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.cmdStartImport);
		this.labelFileName.setSizeUndefined();
		this.horizontalLayout2.addComponent(this.labelFileName);
		final CustomComponent horizontalLayout2_spacer = new CustomComponent();
		horizontalLayout2_spacer.setSizeFull();
		this.horizontalLayout2.addComponent(horizontalLayout2_spacer);
		this.horizontalLayout2.setExpandRatio(horizontalLayout2_spacer, 1.0F);
		this.cmdSelect.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSelect);
		this.horizontalLayout.setComponentAlignment(this.cmdSelect, Alignment.MIDDLE_LEFT);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_LEFT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_RIGHT);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.form.setColumns(2);
		this.form.setRows(5);
		this.comboBoxType.setSizeUndefined();
		this.form.addComponent(this.comboBoxType, 0, 0);
		this.labelType.setSizeUndefined();
		this.form.addComponent(this.labelType, 1, 0);
		this.horizontalLayout2.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout2.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout2, 0, 1);
		this.labelSize.setSizeUndefined();
		this.form.addComponent(this.labelSize, 1, 1);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout, 0, 2);
		this.labelResult.setSizeUndefined();
		this.form.addComponent(this.labelResult, 1, 2);
		this.labelHeader.setWidth(100, Unit.PERCENTAGE);
		this.labelHeader.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.labelHeader, 0, 3, 1, 3);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 4, 1, 4);
		this.form.setRowExpandRatio(4, 1.0F);
		this.form.setSizeFull();
		this.panel.setContent(this.form);
		this.panel.setSizeFull();
		this.verticalSplitPanel.setFirstComponent(this.panel);
		this.treeGrid.setSizeFull();
		this.verticalSplitPanel.setSecondComponent(this.treeGrid);
		this.verticalSplitPanel.setSizeFull();
		this.setContent(this.verticalSplitPanel);
		this.setSizeFull();

		this.comboBoxType.addValueChangeListener(event -> this.comboBoxType_valueChange(event));
		this.cmdStartImport.addClickListener(event -> this.cmdStartImport_buttonClick(event));
		this.cmdSelect.addClickListener(event -> this.cmdSelect_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel labelType, labelFileName, labelSize, labelResult, labelHeader;
	private XdevButton cmdStartImport, cmdSelect, cmdSave, cmdReset;
	private XdevVerticalSplitPanel verticalSplitPanel;
	private XdevHorizontalLayout horizontalLayout2, horizontalLayout;
	private XdevComboBox<?> comboBoxType;
	private XdevPanel panel;
	private XdevTreeTable treeGrid;
	private XdevGridLayout form;
	private XdevFieldGroup<Activity> fieldGroup;
	// </generated-code>

}
