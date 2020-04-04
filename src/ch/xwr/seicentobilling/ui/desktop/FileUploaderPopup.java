package ch.xwr.seicentobilling.ui.desktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevProgressBar;
import com.xdev.ui.XdevUpload;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;

import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;

public class FileUploaderPopup extends XdevView  implements Upload.SucceededListener,
															Upload.FailedListener,
															Upload.Receiver,
															Upload.ProgressListener,
															Upload.FinishedListener
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = LogManager.getLogger(FileUploaderPopup.class);

    private long maxSize = 0;
	private long contentLength = 0;
	private File file = null;
	private final String directory = "";
	protected boolean cancelled = false;
	private FileUploadDto dto = null;
	private String filter = "*";

	/**
	 *
	 */
	public FileUploaderPopup() {
		super();
		this.initUI();

		this.dto = (FileUploadDto) UI.getCurrent().getSession().getAttribute("uploaddto");

		setupUploader();

		this.lblSubject.setValue(this.dto.getSubject());
		this.lblFileType.setValue(this.dto.getFilter());
		this.lblMaxSize.setValue(getMaxSizeInfo());

		this.maxSize = this.dto.getMaxSize();
		this.filter = this.dto.getFilter();

	}

	private String getMaxSizeInfo() {
        return "Max: " + (this.maxSize / 1024 / 1024) + " Mb";
	}

	public static Window getPopupWindow() {
		final Window win = new Window();

		win.setWidth("500");
		win.setHeight("190");
		win.center();
		win.setModal(true);
		win.setContent(new FileUploaderPopup());

		return win;
	}

	private void setupUploader() {
		//uploader
		//this.rec = new UploadReceiverExcel();

		this.upload.setReceiver(this);

        this.upload.addSucceededListener(this);
        this.upload.addFailedListener(this);
        this.upload.addProgressListener(this);
        this.upload.addFinishedListener(this);

        this.progressBar.clear();

        this.dto.setSuccess(false);
        this.dto.setUpfile(new File("unknown"));
	}

	public void setMaxSize(final long maxSize) {
		this.maxSize = maxSize;
	}

	public long getMaxSize() {
		return this.maxSize;
	}

	@Override
	public void uploadFinished(final FinishedEvent event) {
		LOG.debug("upload finished");
		this.horizontalLayoutProgress.setVisible(false);

		UI.getCurrent().getSession().setAttribute("uploaddto",  this.dto);
		((Window) this.getParent()).close();
	}

	@Override
	public void updateProgress(final long readBytes, final long contentLength) {
		LOG.debug("updateProgress: read " + readBytes + " length: " + contentLength);
        this.contentLength = contentLength;
        if (readBytes > this.maxSize || contentLength > this.maxSize) {
            FileUploaderPopup.this.upload.interruptUpload();
            return;
        }
        final Float pval = new Float(readBytes / (float) contentLength);
		FileUploaderPopup.this.horizontalLayoutProgress.setVisible(true);
		FileUploaderPopup.this.progressBar.setValue(pval);
	}

	@Override
	public OutputStream receiveUpload(final String filename, final String mimeType) {
		LOG.debug("receiveUpload file: " + filename + " typ: " + mimeType);
		this.progressBar.setValue((float) 0);


		FileOutputStream fos = null;
		try {
			checkFileExtension(filename);

			// Get path to servlet's temp directory
			final File directory = (File) VaadinServlet.getCurrent().getServletContext()
					.getAttribute(ServletContext.TEMPDIR);

			if (!directory.exists() && directory.canWrite()) {
				directory.createNewFile();
			}
			// Concatenate temporaryDirectory with filename and open the file
			// for writing.
	        this.file = new File(directory, filename);
            fos = new FileOutputStream(this.file);

		} catch (final java.io.FileNotFoundException e) {
            throw new RuntimeException(e);
		} catch (final IOException io) {
            throw new RuntimeException(io);
		}

		LOG.debug("upload to: " + this.file.toString());
		return fos;
 	}

	private void checkFileExtension(final String filename) throws IOException {
		if (this.filter != null && this.filter.length() > 1) {
			final String f2 = this.filter.replaceAll("\\*", "");
			final String[] fa = f2.split(",");

			for (int i = 0; i < fa.length; i++) {
				final String ext = fa[i].toLowerCase();
				if (filename.toLowerCase().endsWith(ext)) {
					return;
				}
			}
			this.dto.setMessage("invalid file extension in " + filename);
			throw new IOException(this.dto.getMessage());
		}
	}

	@Override
	public void uploadFailed(final FailedEvent event) {
		LOG.warn("upload of file failed");
		this.horizontalLayoutProgress.setVisible(false);
        if (this.contentLength > 0 && this.maxSize < this.contentLength) {
            showNotification("File too large",
                    "Your file "+this.contentLength/1000+"Kb long. Please select an file smaller than " + this.maxSize / 1000 + "Kb");
            this.dto.setMessage("File too large");
        } else if (this.cancelled) {
            // Nothing to do...
        } else {
            showNotification("There was a problem uploading your file.",
                    "<pre>"+event.getReason().getStackTrace().toString()+"</pre>");
        }

        try{
            this.file.delete();
        } catch (final Exception e) {
            // Silent exception. If we can't delete the file, it's not big problem. May the file did not even exist.
        }


		this.dto.setSuccess(false);

        afterUploadFailed(event);
	}

    /** Override me to do something more than displaying the notification */
    public void afterUploadFailed(final FailedEvent event) {
    }

	@Override
	public void uploadSucceeded(final SucceededEvent event) {
		LOG.debug("uploadSucceeded");
		Notification.show("Datei erfolgreich hochgeladen", Type.TRAY_NOTIFICATION);
		LOG.info("Datei erfolgreich hochgeladen " + event.getFilename());

		this.dto.setUpfile(getFile());
		this.dto.setSize(this.contentLength);
		this.dto.setMessage("Datei erfolgreich hochgeladen!");
		this.dto.setSuccess(true);
	}

    protected void showNotification(final String message, final String detail) {
//        final BlackBeltAppLevelWindow mainWin = (BlackBeltAppLevelWindow)(this.getWindow().getParent());
//        NotificationUtil.showNotification(mainWin, message, detail);
		Notification.show(message, detail,Type.TRAY_NOTIFICATION);
    }

	public String getDirectory() {
		return this.directory;
	}

	public File getFile() {
		return this.file;
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdClose}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdClose_buttonClick(final Button.ClickEvent event) {
		this.cancelled = true;
		this.dto.setMessage("Abgebrochen");
		this.upload.interruptUpload();
		if (this.upload.getBytesRead() == 0) {
			UI.getCurrent().getSession().setAttribute("uploaddto",  this.dto);
			((Window) this.getParent()).close();
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
		this.horizontalLayoutLabel = new XdevHorizontalLayout();
		this.lblSubject = new XdevLabel();
		this.lblFileType = new XdevLabel();
		this.lblMaxSize = new XdevLabel();
		this.horizontalLayoutUpload = new XdevHorizontalLayout();
		this.upload = new XdevUpload();
		this.cmdClose = new XdevButton();
		this.horizontalLayoutProgress = new XdevHorizontalLayout();
		this.progressBar = new XdevProgressBar();

		this.panel.setIcon(FontAwesome.UPLOAD);
		this.panel.setCaption("Lokale Datei hochladen");
		this.panel.setTabIndex(0);
		this.verticalLayout.setIcon(FontAwesome.UPLOAD);
		this.verticalLayout.setCaption("Datei hochladen");
		this.verticalLayout.setMargin(new MarginInfo(false, true, true, true));
		this.horizontalLayoutLabel.setMargin(new MarginInfo(false, true, false, false));
		this.lblSubject.setValue("Plz Verzeichnis");
		this.lblFileType.setDescription("Die Zahl entspricht dem Monat (z.B. 9 = September)");
		this.lblFileType.setValue("Filter");
		this.lblMaxSize.setValue("Max. Grösse");
		this.horizontalLayoutUpload.setMargin(new MarginInfo(false, true, false, false));
		this.upload.setButtonCaption("Start Upload");
		this.cmdClose.setIcon(FontAwesome.CLOSE);
		this.cmdClose.setCaption("Abbrechen");
		this.cmdClose.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		this.horizontalLayoutProgress.setMargin(new MarginInfo(false, true, false, false));
		this.horizontalLayoutProgress.setVisible(false);

		this.lblSubject.setSizeUndefined();
		this.horizontalLayoutLabel.addComponent(this.lblSubject);
		this.horizontalLayoutLabel.setExpandRatio(this.lblSubject, 20.0F);
		this.lblFileType.setSizeUndefined();
		this.horizontalLayoutLabel.addComponent(this.lblFileType);
		this.horizontalLayoutLabel.setExpandRatio(this.lblFileType, 10.0F);
		this.lblMaxSize.setSizeUndefined();
		this.horizontalLayoutLabel.addComponent(this.lblMaxSize);
		this.horizontalLayoutLabel.setExpandRatio(this.lblMaxSize, 10.0F);
		this.upload.setWidth(100, Unit.PERCENTAGE);
		this.upload.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutUpload.addComponent(this.upload);
		this.horizontalLayoutUpload.setExpandRatio(this.upload, 20.0F);
		this.cmdClose.setSizeUndefined();
		this.horizontalLayoutUpload.addComponent(this.cmdClose);
		this.horizontalLayoutUpload.setComponentAlignment(this.cmdClose, Alignment.TOP_CENTER);
		this.horizontalLayoutUpload.setExpandRatio(this.cmdClose, 10.0F);
		this.progressBar.setWidth(50, Unit.PERCENTAGE);
		this.progressBar.setHeight(-1, Unit.PIXELS);
		this.horizontalLayoutProgress.addComponent(this.progressBar);
		this.horizontalLayoutProgress.setComponentAlignment(this.progressBar, Alignment.TOP_CENTER);
		this.horizontalLayoutProgress.setExpandRatio(this.progressBar, 20.0F);
		this.horizontalLayoutLabel.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutLabel.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutLabel);
		this.horizontalLayoutUpload.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutUpload.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutUpload);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutUpload, 10.0F);
		this.horizontalLayoutProgress.setWidth(100, Unit.PERCENTAGE);
		this.horizontalLayoutProgress.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.horizontalLayoutProgress);
		this.verticalLayout.setComponentAlignment(this.horizontalLayoutProgress, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.horizontalLayoutProgress, 10.0F);
		this.verticalLayout.setWidth(100, Unit.PERCENTAGE);
		this.verticalLayout.setHeight(-1, Unit.PIXELS);
		this.panel.setContent(this.verticalLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setSizeFull();

		this.cmdClose.addClickListener(event -> this.cmdClose_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblSubject, lblFileType, lblMaxSize;
	private XdevButton cmdClose;
	private XdevUpload upload;
	private XdevHorizontalLayout horizontalLayoutLabel, horizontalLayoutUpload, horizontalLayoutProgress;
	private XdevProgressBar progressBar;
	private XdevPanel panel;
	private XdevVerticalLayout verticalLayout;
	// </generated-code>

}
