
package ch.xwr.seicentobilling.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.navigation.NavigationParameter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.UploadReceiver;
import ch.xwr.seicentobilling.business.svc.SaveFileToDb;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;


public class AttachmentPopup extends VerticalLayout
{
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(AttachmentPopup.class);
	@NavigationParameter
	private final RowObject                      rowobj;
	private RowImageDAO                          dao     = null;
	private RowImage                             bean    = null;
	
	private File   fio      = null;
	private String mimeType = null;
	
	/**
	 *
	 */
	public AttachmentPopup()
	{
		super();
		this.initUI();

		this.dao    = new RowImageDAO();
		this.rowobj = (RowObject)UI.getCurrent().getSession().getAttribute("RowObject");
		this.loadTableRowImage();

		this.cmdSave.setEnabled(false);
		this.labelStatus.setText("");
		this.setupUploader(this.getRowImageBean());
	}

	private void loadTableRowImage()
	{
		
		final RowImageDAO dao = new RowImageDAO();
		this.grid.setDataProvider(DataProvider.ofCollection(dao.findByObject(this.rowobj)));
		this.grid.getDataProvider().refreshAll();
	}
	
	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		win.setWidth("60%");
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new AttachmentPopup());
		return win;
	}
	
	private RowImage getRowImageBean()
	{
		RowImage bean = null;

		if(this.rowobj != null && this.rowobj.getObjId() > 0)
		{
			final int nextNbr = this.getNextRimNumber();

			bean = new RowImage();
			bean.setRimState(LovState.State.active);
			bean.setRowObject(this.rowobj);
			bean.setRimNumber(nextNbr);
			bean.setRimType((short)2);
		}
		return bean;
	}

	private int getNextRimNumber()
	{
		final List<RowImage> lst = this.dao.findByObject(this.rowobj);
		if(lst == null || lst.isEmpty())
		{
			return 850;
		}
		
		int inbr = 0;
		for(final RowImage rowImage : lst)
		{
			if(rowImage.getRimNumber() > inbr)
			{
				inbr = rowImage.getRimNumber();
			}
		}
		if(inbr < 850)
		{
			return 850;
		}
		return inbr + 10;
	}

	private void setupUploader(final RowImage bean)
	{
		if(bean != null)
		{
			this.upload.setVisible(true);
			
			final UploadReceiver rec = new UploadReceiver(bean);
			rec.setResizeImage(true);
			this.upload.setReceiver(rec);
			
			this.upload.addSucceededListener(event ->

			{
				// This method gets called when the upload finished successfully
				System.out.println("________________ UPLOAD SUCCEEDED 2");

				rec.uploadSucceeded(event);
				AttachmentPopup.this.fio      = rec.getFiup();
				AttachmentPopup.this.mimeType = rec.getMimeType();

				if(rec.getFiup().length() > (rec.getMaxImageSize() * 2))
				{
					AttachmentPopup.this.upload.setVisible(true);
					AttachmentPopup.this.labelStatus.setText("Datei ist zu gross!");

					final int ikb = rec.getMaxImageSize() * 2 / 1024;
					SeicentoNotification.showInfo("Datei ist zu gross",
						"Max Size: " + ikb + " KB " + rec.getFiup().getName());

				}
				else
				{
					AttachmentPopup.this.cmdSave.setEnabled(true);
					AttachmentPopup.this.upload.setVisible(true);
					AttachmentPopup.this.labelStatus.setText("Upload ok. Please click SAVE.");
				}

			});
		}
		else
		{
			this.upload.setVisible(false);
		}
		// uploader
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #grid}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void grid_onItemClick(final ItemClickEvent<RowImage> event)
	{
		// final Item itm = event.getItem();
		this.gridHorizontalLayout.setVisible(false);
		this.horizontalLayout.setVisible(false);
		
		final RowImage img = event.getItem();
		if(img == null)
		{
			return;
		}
		this.bean = img;
		
		this.horizontalLayoutImage.setVisible(true);
		this.horizontalLayoutImage.setHeight("100%");
		
		if(img.getRimMimetype().toLowerCase().startsWith("image"))
		{
			this.enableImage(true);
		}
		else
		{
			this.enableBrowser(true);
		}
	}

	private void enableBrowser(final boolean enable)
	{
		if(enable)
		{
			this.image.setWidth("80%");
			final StreamResource     resource     = new StreamResource(this.bean.getRimName(),
				() -> this.createExport(this.bean.getRimImage()));
			final StreamRegistration registration =
				VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
			this.browserFrame.setSrc(registration.getResourceUri().toString());
			
			this.enableImage(false);
			
			if(this.bean.getRimMimetype().toLowerCase().startsWith("image"))
			{
				this.cmdToggleViewer.setVisible(enable);
			}
			
		}
		// all images
		this.browserFrame.setVisible(enable);
		
	}
	
	private void enableImage(final boolean enable)
	{
		if(enable)
		{
			this.image.setWidth("80%");
			final StreamResource resource = new StreamResource(this.bean.getRimName(),
				() -> this.createExport(this.bean.getRimImage()));
			this.image.setSrc(resource);
			this.enableBrowser(false);
		}
		// all images
		this.image.setVisible(enable);
		this.cmdToggleViewer.setVisible(enable);
	}
	
	private InputStream createExport(final byte[] bs)
	{
		final InputStream inStream = new ByteArrayInputStream(bs);
		return inStream;
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCloseImage}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCloseImage_onClick(final ClickEvent<Button> event)
	{
		this.closeImage();
	}
	
	private void closeImage()
	{
		this.horizontalLayoutImage.setVisible(false);
		this.gridHorizontalLayout.setVisible(true);
		this.horizontalLayout.setVisible(true);
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDelete}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_onClick(final ClickEvent<Button> event)
	{
		if(!this.grid.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final RowImage    bean = this.grid.getSelectionModel().getFirstSelectedItem().get();
		final RowImageDAO dao  = new RowImageDAO();
		dao.remove(bean);

		this.closeImage();
		this.loadTableRowImage();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdToggleViewer}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdToggleViewer_onClick(final ClickEvent<Button> event)
	{
		if(this.image.isVisible())
		{
			// switch to browser
			this.enableBrowser(true);
		}
		else
		{
			this.enableImage(true);
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		try
		{
			final SaveFileToDb upsave =
				new SaveFileToDb(this.fio.getAbsolutePath(), this.mimeType, this.rowobj.getObjId());
			upsave.importFile();

			// setBeanGui(rec.getBean());
			this.loadTableRowImage();
		}
		catch(final Exception e)
		{
			SeicentoNotification.showInfo("Fehler beim Importieren", e.getMessage());
			AttachmentPopup._logger.error(e.getLocalizedMessage());
		}
		finally
		{
			// cleanup
			this.cmdSave.setEnabled(false);
			this.labelStatus.setText("Attachment saved to DB");

		}
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdClose}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdClose_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.horizontalLayoutTitle = new HorizontalLayout();
		this.label                 = new Label();
		this.gridHorizontalLayout  = new HorizontalLayout();
		this.grid                  = new Grid<>(RowImage.class, false);
		this.horizontalLayoutImage = new HorizontalLayout();
		this.browserFrame          = new IFrame();
		this.image                 = new Image();
		this.verticalLayoutBtn     = new VerticalLayout();
		this.cmdCloseImage         = new Button();
		this.cmdDelete             = new Button();
		this.cmdToggleViewer       = new Button();
		this.horizontalLayout      = new HorizontalLayout();
		this.cmdClose              = new Button();
		this.upload                = new Upload();
		this.cmdSave               = new Button();
		this.labelStatus           = new Label();

		this.setPadding(false);
		this.label.setText("Attachments");
		this.grid.addColumn(RowImage::getRimNumber).setKey("rimNumber").setHeader("Number").setSortable(true);
		this.grid.addColumn(RowImage::getRimName).setKey("rimName").setHeader("Name").setSortable(true);
		this.grid.addColumn(RowImage::getRimSize).setKey("rimSize").setHeader("Size").setSortable(true);
		this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.horizontalLayoutImage.setVisible(false);
		this.verticalLayoutBtn.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.cmdCloseImage.setText("Zurück");
		this.cmdCloseImage.setIcon(VaadinIcon.ARROW_CIRCLE_LEFT.create());
		this.cmdDelete.setText("Löschen");
		this.cmdDelete.setIcon(VaadinIcon.CLOSE_SMALL.create());
		this.cmdToggleViewer.setText("Viewer");
		this.cmdToggleViewer.setIcon(VaadinIcon.EXPAND.create());
		this.horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.cmdClose.setText("Schliessen");
		this.cmdClose.setIcon(IronIcons.CHECK.create());
		this.cmdSave.setEnabled(false);
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.labelStatus.setText("Status");

		this.label.setSizeUndefined();
		this.horizontalLayoutTitle.add(this.label);
		this.horizontalLayoutTitle.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.grid.setSizeFull();
		this.gridHorizontalLayout.add(this.grid);
		this.gridHorizontalLayout.setFlexGrow(1.0, this.grid);
		this.cmdCloseImage.setSizeUndefined();
		this.cmdDelete.setSizeUndefined();
		this.cmdToggleViewer.setSizeUndefined();
		this.verticalLayoutBtn.add(this.cmdCloseImage, this.cmdDelete, this.cmdToggleViewer);
		this.browserFrame.setSizeFull();
		this.image.setSizeFull();
		this.verticalLayoutBtn.setWidth("250px");
		this.verticalLayoutBtn.setHeightFull();
		this.horizontalLayoutImage.add(this.browserFrame, this.image, this.verticalLayoutBtn);
		this.cmdClose.setSizeUndefined();
		this.upload.setSizeUndefined();
		this.cmdSave.setSizeUndefined();
		this.labelStatus.setSizeUndefined();
		this.horizontalLayout.add(this.cmdClose, this.upload, this.cmdSave, this.labelStatus);
		this.horizontalLayoutTitle.setWidthFull();
		this.horizontalLayoutTitle.setHeight("30px");
		this.gridHorizontalLayout.setWidthFull();
		this.gridHorizontalLayout.setHeight("327px");
		this.horizontalLayoutImage.setSizeFull();
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("150px");
		this.add(this.horizontalLayoutTitle, this.gridHorizontalLayout, this.horizontalLayoutImage,
			this.horizontalLayout);
		this.setSizeFull();

		this.grid.addItemClickListener(this::grid_onItemClick);
		this.cmdCloseImage.addClickListener(this::cmdCloseImage_onClick);
		this.cmdDelete.addClickListener(this::cmdDelete_onClick);
		this.cmdToggleViewer.addClickListener(this::cmdToggleViewer_onClick);
		this.cmdClose.addClickListener(this::cmdClose_onClick);
		this.cmdSave.addClickListener(this::cmdSave_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Button           cmdCloseImage, cmdDelete, cmdToggleViewer, cmdClose, cmdSave;
	private Image            image;
	private IFrame           browserFrame;
	private Upload           upload;
	private HorizontalLayout horizontalLayoutTitle, gridHorizontalLayout, horizontalLayoutImage, horizontalLayout;
	private VerticalLayout   verticalLayoutBtn;
	private Label            label, labelStatus;
	private Grid<RowImage>   grid;
	// </generated-code>
	
}
