package ch.xwr.seicentobilling.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	private int maxImageSize = 100000;

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
	    System.out.println("________________ UPLOAD Receiver 1");
	    if(mimeType.length() > 55) {
			mimeType = mimeType.substring(0,55);
		}

		this.RowImage.setRimName(filename);
		this.RowImage.setRimMimetype(mimeType);

		this.baos = new ByteArrayOutputStream();
		//DataOutputStream w = new DataOutputStream(baos);

		return this.baos;
	}


	@Override
    public void uploadSucceeded(final SucceededEvent event) {
	    System.out.println("________________ UPLOAD SUCCEEDED 3:  (Size: " + this.baos.size() + ")");

	    if (isResizeImage() && this.baos.size() > getMaxImageSize()) {
	    	resizeImage(event);

	    } else {
			this.RowImage.setRimSize(this.baos.size() + " Bytes");
			this.RowImage.setRimImage(this.baos.toByteArray());
	    }
    }

	private void resizeImage(final SucceededEvent event) {
		final ImageResizer img = new ImageResizer();
		img.resize(this.baos, 640, 480);

		try {
			this.RowImage.setRimImage(Files.readAllBytes(Paths.get(img.getResizedFile().getName())));
			this.RowImage.setRimSize(img.getResizedFile().length() + " Bytes");
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

}
