package ch.xwr.seicentobilling.business.crm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;

public class VcardHandler {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VcardHandler.class);

	private PrintWriter vcard = null;
	private Customer cus = null;
	private File file = null;
	private String fextension = ".vcard";

	public VcardHandler(final Customer cus, final String extension) {
		if (extension != null) {
			this.fextension = extension;
		}
		this.cus = cus;
		this.file = new File(getTempFileName(cus));
	}

	public void generateVcard() {
		LOG.debug("Start creating vcard");

		try {
			this.vcard = new PrintWriter(this.file, "UTF-8");

			writeHeader();
			writeName();
			writeAddress();
			writePhone();
			writeEof();

		} catch (final FileNotFoundException e) {
			LOG.error("File not found", e);
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Invalid encoding", e);
		} finally {
			this.vcard.close();
			this.vcard = null;
		}
	}

	public File getFile() {
		return this.file;
	}

	public InputStream getInputStream() {
		InputStream targetStream;
		try {
			targetStream = new FileInputStream(getFile());
			return targetStream;
		} catch (final FileNotFoundException e) {
			LOG.error("Cano not create Stream", e);
		}

		return null;
	}

	private String getTempFileName(final Customer cus) {
		final String tempDir = System.getProperty("java.io.tmpdir");

		final String prefix = cus.getShortname() + "_" + cus.getCusId();

		final String fname = tempDir + "/" + prefix + this.fextension;
		return fname;
	}

	private void writeHeader() {
		this.vcard.println("BEGIN:VCARD");
		this.vcard.println("VERSION:4.0");
	}

	private void writeName() {
		String n1 = "";
		String n2 = "";
		String n3 = "";

		if (this.cus.getCusAccountType() == LovState.AccountType.juristisch) {
			n1 = this.cus.getCusCompany();
			n2 = this.cus.getCusName();

			if (this.cus.getCusFirstName() != null) {
				n3 = this.cus.getCusFirstName();
			}

			this.vcard.println("ORG:"  + this.cus.getCusCompany());
		} else {
			n1 = this.cus.getCusName();
			n2 = this.cus.getCusFirstName();
			n3 = "";
		}

		this.vcard.println("N:"  + n1 + ";" + n2 + ";" + n3 + ";;");
		this.vcard.println("FN:"  + this.cus.getShortname());
	}

	private void writeAddress() {
		this.vcard.println("ADR;Type=home:;;" + this.cus.getCusAddress() + ";" + this.cus.getCity().getCtyName() + ";" + this.cus.getCity().getCtyRegion() + ";" + this.cus.getCity().getCtyZip() + ";" + this.cus.getCity().getCtyCountry());
	}

	private void writePhone() {
		final CustomerLinkDAO dao = new CustomerLinkDAO();
		final List<CustomerLink> lst = dao.findByCustomer(this.cus);
		if (lst == null || lst.isEmpty()) {
			return;
		}

		for (final CustomerLink customerLink : lst) {
			if (customerLink.getCnkType().equals(LovCrm.LinkType.mail)) {
				this.vcard.println("EMAIL:"  + customerLink.getCnkLink());
			}
			if (customerLink.getCnkType().equals(LovCrm.LinkType.phone)) {
				this.vcard.println("TEL;TYPE=work,voice;VALUE=uri:tel:" + customerLink.getCnkLink());
			}
		}
	}

	private void writeEof() {
		this.vcard.println("END:VCARD");
	}

}
