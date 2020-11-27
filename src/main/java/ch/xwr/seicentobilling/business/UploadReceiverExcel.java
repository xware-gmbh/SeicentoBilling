
package ch.xwr.seicentobilling.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.flow.component.upload.Receiver;

//import com.vaadin.server.VaadinServlet;
//import com.vaadin.ui.Notification;
//import com.vaadin.ui.Notification.Type;
//import com.vaadin.ui.Upload.Receiver;
//import com.vaadin.ui.Upload.SucceededEvent;
//import com.vaadin.ui.Upload.SucceededListener;


public class UploadReceiverExcel implements Receiver
{
	private final File uFILE = null;

	// Constructor
	public UploadReceiverExcel()
	{
	}

	@Override
	public OutputStream receiveUpload(final String filename, final String mimeType)
	{
		// Create upload stream to write to

		final FileOutputStream fos = null;
		/*
		 * try {
		 *
		 * // Get path to servlet's temp directory
		 * final File temporaryDirectory = (File) VaadinServlet.getCurrent().getServletContext()
		 * .getAttribute(ServletContext.TEMPDIR);
		 *
		 * // Concatenate temporaryDirectory with filename and open the file
		 * // for writing.
		 * this.uFILE = new File(temporaryDirectory, filename);
		 *
		 * // Create the output stream
		 * fos = new FileOutputStream(this.uFILE);
		 *
		 * } catch (final java.io.FileNotFoundException e) {
		 * Notification.show("Konnte Datei nicht öffnen!", Type.ERROR_MESSAGE);
		 * return null;
		 * }
		 */
		return fos;
	}

	/*
	 * @Override
	 * public void uploadSucceeded(final SucceededEvent event) {
	 * // System.out.println("________________ UPLOAD SUCCEEDED x");
	 * // Notification.show("Datei erfolgreich hochgeladen", Type.ASSISTIVE_NOTIFICATION);
	 * }
	 *
	 *
	 * public File getOutFile() {
	 * return this.uFILE;
	 * }
	 */}
