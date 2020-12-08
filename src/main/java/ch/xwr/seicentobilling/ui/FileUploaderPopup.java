
package ch.xwr.seicentobilling.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.FailedEvent;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.ProgressUpdateEvent;
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.VaadinServlet;

import ch.xwr.seicentobilling.business.model.generic.FileUploadDto;
import ch.xwr.seicentobilling.entities.Periode;


public class FileUploaderPopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(FileUploaderPopup.class);
	
	private long          maxSize       = 0;
	private long          contentLength = 0;
	private File          file          = null;
	private final String  directory     = "";
	protected boolean     cancelled     = false;
	private FileUploadDto dto           = null;
	private String        filter        = "*";
	final MemoryBuffer    buffer        = new MemoryBuffer();
	
	/**
	 *
	 */
	public FileUploaderPopup()
	{
		super();
		this.initUI();

		this.dto = (FileUploadDto)UI.getCurrent().getSession().getAttribute("uploaddto");
		
		this.setupUploader();
		
		this.maxSize = this.dto.getMaxSize();
		this.filter  = this.dto.getFilter();
		
		this.lblSubject.setText(this.dto.getSubject());
		this.lblFileType.setText(this.dto.getFilter());
		this.lblMaxSize.setText(this.getMaxSizeInfo());
		
	}

	private void setupUploader()
	{
		// uploader
		// this.rec = new UploadReceiverExcel();
		
		// this.upload.setReceiver(this);
		// this.upload.addSucceededListener(this);
		// this.upload.addFailedListener(this);
		// this.upload.addProgressListener(this);
		// this.upload.addFinishedListener(this);

		this.upload.setReceiver(this.buffer);

		this.dto.setSuccess(false);
		this.dto.setUpfile(new File("unknown"));
	}
	
	public void setMaxSize(final long maxSize)
	{
		this.maxSize = maxSize;
	}
	
	public long getMaxSize()
	{
		return this.maxSize;
	}
	
	private String getMaxSizeInfo()
	{
		return "Max: " + (this.maxSize / 1024 / 1024) + " Mb";
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
		// win.setWidth("500");
		// win.setHeight("190");
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new FileUploaderPopup());
		return win;
	}
	
	/**
	 * Event handler delegate method for the {@link Upload} {@link #upload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void upload_onFailed(final FailedEvent event)
	{
		FileUploaderPopup.LOG.warn("upload of file failed");
		this.horizontalLayoutProgress.setVisible(false);
		if(this.contentLength > 0 && this.maxSize < this.contentLength)
		{
			SeicentoNotification.showError("File too large:" +
				"Your file " + this.contentLength / 1000 + "Kb long. Please select an file smaller than "
				+ this.maxSize / 1000 + "Kb");
			this.dto.setMessage("File too large");
		}
		else if(this.cancelled)
		{
			// Nothing to do...
		}
		else
		{
			SeicentoNotification.showError("There was a problem uploading your file. " +
				event.getReason().getLocalizedMessage());
		}

		try
		{
			this.file.delete();
		}
		catch(final Exception e)
		{
			// Silent exception. If we can't delete the file, it's not big problem. May the file did not even exist.
		}

		this.dto.setSuccess(false);

		this.afterUploadFailed(event);

	}

	/** Override me to do something more than displaying the notification */
	public void afterUploadFailed(final FailedEvent event)
	{
	}

	/**
	 * Event handler delegate method for the {@link Upload} {@link #upload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void upload_onProgressUpdate(final ProgressUpdateEvent event)
	{
		FileUploaderPopup.LOG
			.debug("updateProgress: read " + event.getReadBytes() + " length: " + event.getContentLength());
		this.contentLength = event.getContentLength();
		if(event.getReadBytes() > this.maxSize || this.contentLength > this.maxSize)
		{
			FileUploaderPopup.this.upload.interruptUpload();
			return;
		}
		final Float pval = new Float(event.getReadBytes() / (float)this.contentLength);
		FileUploaderPopup.this.horizontalLayoutProgress.setVisible(true);
		FileUploaderPopup.this.progressBar.setValue(pval);
	}
	
	/**
	 * Event handler delegate method for the {@link Upload} {@link #upload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void upload_onFinished(final FinishedEvent event)
	{
		FileUploaderPopup.LOG.debug("upload finished");
		this.horizontalLayoutProgress.setVisible(false);
		
		UI.getCurrent().getSession().setAttribute("uploaddto", this.dto);
		if(this.file != null)
		{
			((Dialog)this.getParent().get()).close();
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Upload} {@link #upload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void upload_onStarted(final StartedEvent event)
	{
		final String filename = event.getFileName();
		FileUploaderPopup.LOG.debug("receiveUpload file: " + filename + " typ: " + event.getMIMEType());
		this.progressBar.setValue(0);
		
		FileOutputStream fos = null;
		try
		{
			this.checkFileExtension(filename);
			
			// Get path to servlet's temp directory
			final File directory = (File)VaadinServlet.getCurrent().getServletContext()
				.getAttribute(ServletContext.TEMPDIR);
			
			if(!directory.exists() && directory.canWrite())
			{
				directory.createNewFile();
			}
			// Concatenate temporaryDirectory with filename and open the file
			// for writing.
			this.file = new File(directory, filename);
			fos       = new FileOutputStream(this.file);
			
		}
		catch(final java.io.FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch(final IOException io)
		{
			throw new RuntimeException(io);
		}
		
		FileUploaderPopup.LOG.debug("upload to: " + this.file.toString());
		// return fos;
	}
	
	private void checkFileExtension(final String filename) throws IOException
	{
		if(this.filter != null && this.filter.length() > 1)
		{
			final String   f2 = this.filter.replaceAll("\\*", "");
			final String[] fa = f2.split(",");

			for(int i = 0; i < fa.length; i++)
			{
				final String ext = fa[i].toLowerCase().trim();
				if(filename.toLowerCase().endsWith(ext))
				{
					return;
				}
			}
			this.dto.setMessage("invalid file extension in " + filename);
			throw new IOException(this.dto.getMessage());
		}
	}
	
	/**
	 * Event handler delegate method for the {@link Upload} {@link #upload}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void upload_onSucceeded(final SucceededEvent event)
	{
		FileUploaderPopup.LOG.debug("uploadSucceeded");
		SeicentoNotification.showInfo("Datei erfolgreich hochgeladen");
		FileUploaderPopup.LOG.info("Datei erfolgreich hochgeladen " + event.getFileName());
		
		this.dto.setUpfile(this.getFile());
		this.dto.setSize(this.contentLength);
		this.dto.setMessage("Datei erfolgreich hochgeladen!");
		this.dto.setSuccess(true);
	}
	
	public String getDirectory()
	{
		return this.directory;
	}

	public File getFile()
	{
		return this.file;
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdClose}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdClose_onClick(final ClickEvent<Button> event)
	{
		this.cancelled = true;
		this.dto.setMessage("Abgebrochen");
		this.upload.interruptUpload();
		UI.getCurrent().getSession().setAttribute("uploaddto", this.dto);
		((Dialog)this.getParent().get()).close();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout           = new VerticalLayout();
		this.horizontalLayout         = new HorizontalLayout();
		this.icon                     = new Icon(VaadinIcon.UPLOAD);
		this.label                    = new Label();
		this.horizontalLayoutLabel    = new HorizontalLayout();
		this.lblSubject               = new Label();
		this.horizontalLayout2        = new HorizontalLayout();
		this.lblFileType              = new Label();
		this.horizontalLayout3        = new HorizontalLayout();
		this.lblMaxSize               = new Label();
		this.horizontalLayout4        = new HorizontalLayout();
		this.upload                   = new Upload();
		this.horizontalLayout5        = new HorizontalLayout();
		this.cmdClose                 = new Button();
		this.horizontalLayoutProgress = new HorizontalLayout();
		this.progressBar              = new ProgressBar();
		this.binder                   = new BeanValidationBinder<>(Periode.class);

		this.label.setText("Lokale Datei hochladen");
		this.lblSubject.setText("Plz Verzeichnis");
		this.lblFileType.setText("Filter");
		this.lblMaxSize.setText("Max. Grösse");
		this.cmdClose.setText("Abbrechen");
		this.cmdClose.setIcon(IronIcons.CLOSE.create());

		this.label.setSizeUndefined();
		this.horizontalLayout.add(this.icon, this.label);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.icon);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.label);
		this.lblSubject.setSizeUndefined();
		this.horizontalLayout2.setWidth("350px");
		this.horizontalLayout2.setHeight("40px");
		this.lblFileType.setSizeUndefined();
		this.horizontalLayout3.setWidth("350px");
		this.horizontalLayout3.setHeight("40px");
		this.lblMaxSize.setSizeUndefined();
		this.horizontalLayoutLabel.add(this.lblSubject, this.horizontalLayout2, this.lblFileType,
			this.horizontalLayout3,
			this.lblMaxSize);
		this.upload.setSizeUndefined();
		this.horizontalLayout5.setWidth("400px");
		this.horizontalLayout5.setHeight("20px");
		this.cmdClose.setSizeUndefined();
		this.horizontalLayout4.add(this.upload, this.horizontalLayout5, this.cmdClose);
		this.progressBar.setSizeUndefined();
		this.horizontalLayoutProgress.add(this.progressBar);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.horizontalLayoutLabel.setWidthFull();
		this.horizontalLayoutLabel.setHeight("40px");
		this.horizontalLayout4.setWidthFull();
		this.horizontalLayout4.setHeight("90px");
		this.horizontalLayoutProgress.setWidthFull();
		this.horizontalLayoutProgress.setHeight("40px");
		this.verticalLayout.add(this.horizontalLayout, this.horizontalLayoutLabel, this.horizontalLayout4,
			this.horizontalLayoutProgress);
		this.verticalLayout.setWidth("80%");
		this.verticalLayout.setHeight("50%");
		this.add(this.verticalLayout);
		this.setSizeFull();

		this.upload.addFailedListener(this::upload_onFailed);
		this.upload.addProgressListener(this::upload_onProgressUpdate);
		this.upload.addFinishedListener(this::upload_onFinished);
		this.upload.addStartedListener(this::upload_onStarted);
		this.upload.addSucceededListener(this::upload_onSucceeded);
		this.cmdClose.addClickListener(this::cmdClose_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Button                        cmdClose;
	private Upload                        upload;
	private ProgressBar                   progressBar;
	private BeanValidationBinder<Periode> binder;
	private VerticalLayout                verticalLayout;
	private HorizontalLayout              horizontalLayout, horizontalLayoutLabel, horizontalLayout2, horizontalLayout3,
		horizontalLayout4, horizontalLayout5, horizontalLayoutProgress;
	private Label                         label, lblSubject, lblFileType, lblMaxSize;
	private Icon                          icon;
	// </generated-code>

}
