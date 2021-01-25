package ch.xwr.seicentobilling.business;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import ch.xwr.seicentobilling.business.helper.ImageResizer;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.entities.RowImage;

public class UploadReceiver implements Receiver, SucceededListener  {
	public Long rowId;

	RowImage RowImage = null;
	ByteArrayOutputStream baos = null;

	private boolean resizeImage = false;
	private int maxImageSize = 204800;

	private File fiup = null;
	private String mimeType = null;

	//Constructor
	public UploadReceiver(final Long itemId) {
		this.rowId = itemId;

		final RowImageDAO dao = new RowImageDAO();
		this.RowImage = dao.find(this.rowId);
	}

	public UploadReceiver(final RowImage rfile) {
		this.RowImage = rfile;
	}

	/**
	 * Bean
	 * @return
	 */
	public RowImage getBean() {
		return this.RowImage;
	}

	@Override
	public OutputStream receiveUpload(final String filename, String mimeType) {
	    System.out.println("________________ UPLOAD Receiver 1 " + filename);
	    if(mimeType.length() > 55) {
			mimeType = mimeType.substring(0,55);
		}
	    setMimeType(mimeType);

		this.RowImage.setRimName(filename);
		this.RowImage.setRimMimetype(mimeType);

		FileOutputStream fos = null;
		try {
			//checkFileExtension(filename);

			// Get path to servlet's temp directory
			final File directory = (File) VaadinServlet.getCurrent().getServletContext()
					.getAttribute(ServletContext.TEMPDIR);

			if (!directory.exists() && directory.canWrite()) {
				directory.createNewFile();
			}
			// Concatenate temporaryDirectory with filename and open the file
			// for writing.
	        this.fiup = new File(directory, filename);
            fos = new FileOutputStream(this.fiup);

		} catch (final java.io.FileNotFoundException e) {
            throw new RuntimeException(e);
		} catch (final IOException io) {
            throw new RuntimeException(io);
		}

		return fos;
	}

	@Override
    public void uploadSucceeded(final SucceededEvent event) {
	    System.out.println("________________ UPLOAD SUCCEEDED 3:  (Size: " + this.fiup.length() + ")");

	    if (isImage() && isResizeImage() && this.fiup.length() > getMaxImageSize()) {
	    	resizeImage(event);
	    } else {
			this.RowImage.setRimSize(this.fiup.length() + " Bytes");
			//this.RowImage.setRimImage(this.baos.toByteArray());
	    }
    }

	private boolean isImage() {
		if (this.RowImage.getRimName() == null) {
			return false;
		}

		final String fname = this.RowImage.getRimName().toLowerCase();
		if (fname.endsWith("jpg")) {
			return true;
		}
		if (fname.endsWith("jpeg")) {
			return true;
		}
		if (fname.endsWith("png")) {
			return true;
		}
		if (fname.endsWith("gif")) {
			return true;
		}
		return false;
	}

	private void resizeImage(final SucceededEvent event) {
		final ImageResizer img = new ImageResizer(this.fiup);
		img.resize(getMaxImageSize());

		try {
			this.RowImage.setRimImage(Files.readAllBytes(Paths.get(img.getResizedFile().getAbsolutePath())));
			this.RowImage.setRimSize(img.getResizedFile().length() + " Bytes");

			this.fiup = img.getResizedFile();

			this.baos = null;

		} catch (final IOException e) {
			e.printStackTrace();
		}

	    System.out.println("________________ UPLOAD RESIZE 4:  (Size: " + img.getResizedFile().length() + ")");
	}

	public boolean isResizeImage() {
		return this.resizeImage;
	}

	public void setResizeImage(final boolean resizeImage) {
		this.resizeImage = resizeImage;
	}

	public int getMaxImageSize() {
		return this.maxImageSize;
	}

	public void setMaxImageSize(final int maxImageSize) {
		this.maxImageSize = maxImageSize;
	}

	public File getFiup() {
		return this.fiup;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	public void removeUploadedFile() {
		if (getFiup() != null) {
			this.fiup.delete();
			this.fiup = null;
		}
	}
}
