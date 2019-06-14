package ch.xwr.seicentobilling.business;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.entities.RowImage;

public class UploadReceiver implements Receiver, SucceededListener  {
	public Long rowId;

	RowImage RowImage = null;
	ByteArrayOutputStream baos = null;


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
	    System.out.println("________________ UPLOAD Receiver x");
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
	    System.out.println("________________ UPLOAD SUCCEEDED x");

		this.RowImage.setRimSize(this.baos.size() + " Bytes");
		this.RowImage.setRimImage(this.baos.toByteArray());
    }

}
