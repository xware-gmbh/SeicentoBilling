package ch.xwr.seicentobilling.ui.phone;

import java.util.List;

import javax.persistence.EntityManager;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window;
import com.xdev.res.ResourceUtils;
import com.xdev.ui.XdevBrowserFrame;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevImage;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevUpload;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.navigation.NavigationParameter;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.UploadReceiver;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowImage_;
import ch.xwr.seicentobilling.entities.RowObject;

public class AttachmentPopup extends XdevView {
	/** Logger initialized */
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(AttachmentPopup.class);

	@NavigationParameter
	private final RowObject rowobj;
	private RowImageDAO dao = null;
	private RowImage bean = null;

	/**
	 *
	 */
	public AttachmentPopup() {
		super();
		this.initUI();

		this.dao = new RowImageDAO();
		this.rowobj = (RowObject) UI.getCurrent().getSession().getAttribute("RowObject");
		loadTableRowImage();

		setupUploader(getRowImageBean());
	}

	private void loadTableRowImage() {
		final XdevBeanContainer<RowImage> myList = this.table.getBeanContainerDataSource();
		myList.removeAll();
		final RowImageDAO dao =  new RowImageDAO();
		myList.addAll(dao.findByObject(this.rowobj));

		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		win.center();
		win.setModal(true);
		win.setContent(new AttachmentPopup());

		return win;
	}

	private RowImage getRowImageBean() {
		RowImage bean = null;

		if (this.rowobj != null && this.rowobj.getObjId() > 0) {
			final int nextNbr = getNextRimNumber();

			bean = new RowImage();
			bean.setRimState(LovState.State.active);
			bean.setRowObject(this.rowobj);
			bean.setRimNumber(nextNbr);
			bean.setRimType((short) 2);
		}
		return bean;
	}

	private int getNextRimNumber() {
	    final List<RowImage> lst = this.dao.findByObject(this.rowobj);
	    if (lst == null || lst.isEmpty()) {
	    	return 850;
	    }

	    int inbr = 0;
	    for (final RowImage rowImage : lst) {
			if (rowImage.getRimNumber() > inbr) {
				inbr = rowImage.getRimNumber();
			}
		}
	    if (inbr < 850) {
	    	return 850;
	    }
		return inbr + 10;
	}

	private void setupUploader(final RowImage bean) {
		if (bean != null) {
			this.upload.setVisible(true);
			this.upload.setEnabled(true);

			final UploadReceiver rec = new UploadReceiver(bean);
			rec.setResizeImage(true);
			this.upload.setReceiver(rec);

	        this.upload.addSucceededListener(new Upload.SucceededListener() {
	            @Override
				public void uploadSucceeded(final SucceededEvent event) {
	                // This method gets called when the upload finished successfully
	        	    System.out.println("________________ UPLOAD SUCCEEDED 2");

	        	    rec.uploadSucceeded(event);

	        		_logger.debug("Session State: " + getSession().getState().name());
	        		EntityManager em = AttachmentPopup.this.dao.getEntityManager();
	        		if (em.isOpen()) {
		        	    final RowImage bean = rec.getBean();
		        	    AttachmentPopup.this.dao.save(bean);
	        		} else {
	        			em = em.getEntityManagerFactory().createEntityManager();
		        		_logger.debug("New em State 1: " + em.isOpen());
		        		final RowImageDAO dao2 = new RowImageDAO();
		        		_logger.debug("New em State 2: " + dao2.getEntityManager().isOpen());

	        			_logger.error("DB Session (EntityManager) is closed!");
	    				Notification.show("Fehler beim speichern", "DB Session closed", Notification.Type.ERROR_MESSAGE);

	        		}


	        	    //setBeanGui(rec.getBean());
	        		loadTableRowImage();

	            }
	        });
		} else {
			this.upload.setVisible(false);
			this.upload.setEnabled(false);
		}
		//uploader
	}


	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	@SuppressWarnings("deprecation")
	private void table_itemClick(final ItemClickEvent event) {
		//final Item itm = event.getItem();
		this.table.setVisible(false);
		this.horizontalLayout.setVisible(false);

		final RowImage img = (RowImage) event.getItemId();
		if (img == null) {
			return;
		}
		this.bean = img;

		this.horizontalLayoutImage.setVisible(true);
		this.horizontalLayoutImage.setHeight(100, UNITS_PERCENTAGE);

		if (img.getRimMimetype().toLowerCase().startsWith("image")) {
			enableImage(true);
		} else {
			enableBrowser(true);
		}

	}

	private void enableBrowser(final boolean enable) {
		if (enable) {
			this.image.setWidth(80, UNITS_PERCENTAGE);
			this.browserFrame.setSource(ResourceUtils.toResource(this.bean.getRimImage(), this.bean.getRimName()));

			enableImage(false);

			if (this.bean.getRimMimetype().toLowerCase().startsWith("image")) {
				this.cmdToggleViewer.setVisible(enable);
			}

		}
		//all images
		this.browserFrame.setVisible(enable);

	}

	private void enableImage(final boolean enable) {
		if (enable) {
			this.image.setWidth(80, UNITS_PERCENTAGE);
			this.image.setSource(ResourceUtils.toResource(this.bean.getRimImage(), this.bean.getRimName()));

			enableBrowser(false);
		}
		//all images
		this.image.setVisible(enable);
		this.cmdToggleViewer.setVisible(enable);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdCloseImage}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCloseImage_buttonClick(final Button.ClickEvent event) {
		closeImage();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdClose}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdClose_buttonClick(final Button.ClickEvent event) {
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDelete}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null || this.table.getSelectedItem().getBean() == null) {
			return;
		}

		final RowImage bean = this.table.getSelectedItem().getBean();
		final RowImageDAO dao = new RowImageDAO();
		dao.remove(bean);

		closeImage();
		loadTableRowImage();
	}

	private void closeImage() {
		this.horizontalLayoutImage.setVisible(false);
		this.table.setVisible(true);
		this.horizontalLayout.setVisible(true);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdToggleViewer}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdToggleViewer_buttonClick(final Button.ClickEvent event) {
		if (this.image.isVisible()) {
			//switch to browser
			enableBrowser(true);
		} else {
			enableImage(true);
		}
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.table = new XdevTable<>();
		this.horizontalLayoutImage = new XdevHorizontalLayout();
		this.browserFrame = new XdevBrowserFrame();
		this.image = new XdevImage();
		this.verticalLayoutBtn = new XdevVerticalLayout();
		this.cmdCloseImage = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.label = new XdevLabel();
		this.cmdToggleViewer = new XdevButton();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdClose = new XdevButton();
		this.upload = new XdevUpload();

		this.verticalLayout.setMargin(new MarginInfo(false));
		this.table.setCaption("Attachments");
		this.table.setIcon(FontAwesome.FILE_PICTURE_O);
		this.table.setContainerDataSource(RowImage.class, false);
		this.table.setVisibleColumns(RowImage_.rimName.getName(), RowImage_.rimNumber.getName(),
				RowImage_.rimSize.getName());
		this.horizontalLayoutImage.setMargin(new MarginInfo(false));
		this.horizontalLayoutImage.setVisible(false);
		this.browserFrame.setVisible(false);
		this.image.setResponsive(true);
		this.verticalLayoutBtn.setMargin(new MarginInfo(true, true, true, false));
		this.cmdCloseImage.setIcon(FontAwesome.ARROW_CIRCLE_LEFT);
		this.cmdCloseImage.setCaption("Zurück");
		this.cmdCloseImage.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.cmdDelete.setIcon(FontAwesome.REMOVE);
		this.cmdDelete.setCaption("Löschen");
		this.cmdToggleViewer.setIcon(FontAwesome.EXPAND);
		this.cmdToggleViewer.setCaption("Viewer");
		this.cmdToggleViewer.setVisible(false);
		this.cmdClose.setIcon(FontAwesome.CHECK);
		this.cmdClose.setCaption("Schliessen");
		this.cmdClose.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.upload.setButtonCaption("Neu...");
		this.upload.setImmediate(true);

		this.cmdCloseImage.setSizeUndefined();
		this.verticalLayoutBtn.addComponent(this.cmdCloseImage);
		this.verticalLayoutBtn.setComponentAlignment(this.cmdCloseImage, Alignment.MIDDLE_LEFT);
		this.cmdDelete.setSizeUndefined();
		this.verticalLayoutBtn.addComponent(this.cmdDelete);
		this.verticalLayoutBtn.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_LEFT);
		this.label.setWidth(68, Unit.PIXELS);
		this.label.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutBtn.addComponent(this.label);
		this.verticalLayoutBtn.setComponentAlignment(this.label, Alignment.MIDDLE_CENTER);
		this.verticalLayoutBtn.setExpandRatio(this.label, 10.0F);
		this.cmdToggleViewer.setSizeUndefined();
		this.verticalLayoutBtn.addComponent(this.cmdToggleViewer);
		this.verticalLayoutBtn.setComponentAlignment(this.cmdToggleViewer, Alignment.MIDDLE_LEFT);
		this.browserFrame.setSizeFull();
		this.horizontalLayoutImage.addComponent(this.browserFrame);
		this.horizontalLayoutImage.setComponentAlignment(this.browserFrame, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutImage.setExpandRatio(this.browserFrame, 50.0F);
		this.image.setSizeFull();
		this.horizontalLayoutImage.addComponent(this.image);
		this.horizontalLayoutImage.setComponentAlignment(this.image, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutImage.setExpandRatio(this.image, 50.0F);
		this.verticalLayoutBtn.setWidth(100, Unit.PERCENTAGE);
		this.verticalLayoutBtn.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutImage.addComponent(this.verticalLayoutBtn);
		this.horizontalLayoutImage.setComponentAlignment(this.verticalLayoutBtn, Alignment.MIDDLE_CENTER);
		this.horizontalLayoutImage.setExpandRatio(this.verticalLayoutBtn, 15.0F);
		this.cmdClose.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdClose);
		this.horizontalLayout.setComponentAlignment(this.cmdClose, Alignment.MIDDLE_CENTER);
		this.upload.setSizeUndefined();
		this.horizontalLayout.addComponent(this.upload);
		this.horizontalLayout.setComponentAlignment(this.upload, Alignment.MIDDLE_CENTER);
		final CustomComponent horizontalLayout_spacer = new CustomComponent();
		horizontalLayout_spacer.setSizeFull();
		this.horizontalLayout.addComponent(horizontalLayout_spacer);
		this.horizontalLayout.setExpandRatio(horizontalLayout_spacer, 1.0F);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 30.0F);
		this.horizontalLayoutImage.setSizeFull();
		this.verticalLayout.addComponent(this.horizontalLayoutImage);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutImage, Alignment.TOP_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutImage, 40.0F);
		this.horizontalLayout.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayout);
		this.verticalLayout.setExpandRatio(this.horizontalLayout, 10.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setWidth(700, Unit.PIXELS);
		this.setHeight(590, Unit.PIXELS);

		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.cmdCloseImage.addClickListener(event -> this.cmdCloseImage_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdToggleViewer.addClickListener(event -> this.cmdToggleViewer_buttonClick(event));
		this.cmdClose.addClickListener(event -> this.cmdClose_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdCloseImage, cmdDelete, cmdToggleViewer, cmdClose;
	private XdevLabel label;
	private XdevUpload upload;
	private XdevHorizontalLayout horizontalLayoutImage, horizontalLayout;
	private XdevImage image;
	private XdevTable<RowImage> table;
	private XdevBrowserFrame browserFrame;
	private XdevVerticalLayout verticalLayout, verticalLayoutBtn;
	// </generated-code>

}
