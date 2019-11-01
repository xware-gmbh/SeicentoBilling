package ch.xwr.seicentobilling.ui.desktop.crm;

import java.io.File;
import java.util.List;

import com.vaadin.data.Item;
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
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window;
import com.xdev.res.ApplicationResource;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevCheckBox;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevImage;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTreeTable;
import com.xdev.ui.XdevUpload;
import com.xdev.ui.XdevVerticalSplitPanel;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.UploadReceiverExcel;
import ch.xwr.seicentobilling.business.crm.CrmExcelHandler;
import ch.xwr.seicentobilling.business.crm.CustomerDto;
import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.ui.desktop.ExcelUploadPopup;

public class ImportContactsPopup extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExcelUploadPopup.class);


	/**
	 *
	 */
	public ImportContactsPopup() {
		super();
		this.initUI();

		this.labelSize.setValue("");
		this.labelResult.setValue("");
		setupUploader();
	}

	private void setupUploader() {
		//uploader
		final UploadReceiverExcel rec = new UploadReceiverExcel();
		this.upload.setReceiver(rec);

        this.upload.addSucceededListener(new Upload.SucceededListener() {
            @Override
			public void uploadSucceeded(final SucceededEvent event) {
                // This method gets called when the upload finished successfully
        	    //System.out.println("________________ UPLOAD SUCCEEDED y");
        	    rec.uploadSucceeded(event);
        		Notification.show("Datei erfolgreich hochgeladen", Type.TRAY_NOTIFICATION);
        		LOG.info("Excel Datei hochgeladen " + event.getFilename());

        	    processUploadedFile(rec.getOutFile());
            }

        });
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

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalSplitPanel = new XdevVerticalSplitPanel();
		this.panel = new XdevPanel();
		this.form = new XdevGridLayout();
		this.image = new XdevImage();
		this.label = new XdevLabel();
		this.upload = new XdevUpload();
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
		this.panel.setCaption("Import Datei");
		this.panel.setTabIndex(0);
		this.form.setMargin(new MarginInfo(false, true, true, true));
		this.image.setSource(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/excel24.png"));
		this.image.setStyleName("light");
		this.label.setStyleName("h3");
		this.label.setValue("Excel Datei hochladen");
		this.upload.setButtonCaption("Start Upload");
		this.labelSize.setValue("Label");
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.cmdSelect.setIcon(FontAwesome.CHECK_SQUARE);
		this.cmdSelect.setCaption("Select/Unselect");
		this.cmdSelect.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption("Speichern");
		this.cmdSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption("Schliessen");
		this.cmdReset.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.labelResult.setValue("Label");
		this.labelHeader.setStyleName("tiny");
		this.labelHeader.setValue(
				"CAM1;CAM2;Nachname;Vorname;Firma;x;Adresse;PLZ;Ort;;TelefonG;Mobile;TelefonP;x;E-Mail;Webseite;x;Newsletter;X-Mas;Verwendung;Notizen");

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
		this.form.setColumns(3);
		this.form.setRows(5);
		this.image.setWidth(100, Unit.PIXELS);
		this.image.setHeight(100, Unit.PIXELS);
		this.form.addComponent(this.image, 0, 0);
		this.form.setComponentAlignment(this.image, Alignment.MIDDLE_LEFT);
		this.label.setSizeUndefined();
		this.form.addComponent(this.label, 1, 0);
		this.upload.setWidth(100, Unit.PERCENTAGE);
		this.upload.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.upload, 0, 1, 1, 1);
		this.labelSize.setSizeUndefined();
		this.form.addComponent(this.labelSize, 2, 1);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.horizontalLayout, 0, 2, 1, 2);
		this.labelResult.setSizeUndefined();
		this.form.addComponent(this.labelResult, 2, 2);
		this.labelHeader.setWidth(100, Unit.PERCENTAGE);
		this.labelHeader.setHeight(-1, Unit.PIXELS);
		this.form.addComponent(this.labelHeader, 0, 3, 2, 3);
		this.form.setColumnExpandRatio(1, 20.0F);
		final CustomComponent form_vSpacer = new CustomComponent();
		form_vSpacer.setSizeFull();
		this.form.addComponent(form_vSpacer, 0, 4, 2, 4);
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

		this.cmdSelect.addClickListener(event -> this.cmdSelect_buttonClick(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel label, labelSize, labelResult, labelHeader;
	private XdevButton cmdSelect, cmdSave, cmdReset;
	private XdevUpload upload;
	private XdevVerticalSplitPanel verticalSplitPanel;
	private XdevImage image;
	private XdevHorizontalLayout horizontalLayout;
	private XdevPanel panel;
	private XdevTreeTable treeGrid;
	private XdevGridLayout form;
	private XdevFieldGroup<Activity> fieldGroup;
	// </generated-code>

}
