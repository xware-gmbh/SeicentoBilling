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
import ch.xwr.seicentobilling.dal.AddressDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;

public class VcardHandler {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VcardHandler.class);

	private PrintWriter fout = null;
	private Customer cus = null;
	private File file = null;
	private String fextension = ".vcard";
	private VCard vcard = null;

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
			computeVcard();

			final String str = Ezvcard.write(this.vcard).version(VCardVersion.V4_0).go();
			this.fout = new PrintWriter(this.file, "UTF-8");
			this.fout.println(str);

		} catch (final FileNotFoundException e) {
			LOG.error("File not found", e);
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Invalid encoding", e);
		} finally {
			this.fout.close();
			this.fout = null;
		}
	}

	private void computeVcard() {
		this.vcard = new VCard();

		computeVcName();
		computeAddresses();
		computeLinks();

	}

	private void computeLinks() {
		final CustomerLinkDAO dao = new CustomerLinkDAO();
		final List<CustomerLink> lst = dao.findByCustomer(this.cus);
		if (lst == null || lst.isEmpty()) {
			return;
		}

		for (final CustomerLink customerLink : lst) {
			if (customerLink.getCnkType().equals(LovCrm.LinkType.mail)) {
				final Email email = new Email(customerLink.getCnkLink());
				this.vcard.addEmail(email);
			}
			if (customerLink.getCnkType().equals(LovCrm.LinkType.phone)) {
				final Telephone phone = new Telephone(customerLink.getCnkLink());
				this.vcard.addTelephoneNumber(phone);
			}
		}

	}

	private void computeAddresses() {
		final AddressDAO dao = new AddressDAO();
		final List<ch.xwr.seicentobilling.entities.Address> lst = dao.findByCustomer(this.cus);
		if (lst == null || lst.isEmpty()) {
			return;
		}


		//Address
		Address adr = new Address();
		adr.setCountry(this.cus.getCity().getCtyCountry());
		adr.setPostalCode(this.cus.getCity().getCtyZip().toString());
		adr.setLocality(this.cus.getCity().getCtyName());
		adr.setRegion(this.cus.getCity().getCtyRegion());
		adr.setStreetAddress(this.cus.getCusAddress());
		this.vcard.addAddress(adr);

		for (final ch.xwr.seicentobilling.entities.Address adrx : lst) {
			adr = new Address();
			adr.setCountry(adrx.getAdrCountry());
			adr.setPostalCode(adrx.getAdrZip());
			adr.setLocality(adrx.getAdrCity());
			adr.setRegion(adrx.getAdrRegion());
			adr.setStreetAddress(adrx.getAdrLine0());
			this.vcard.addAddress(adr);
		}
	}

	private void computeVcName() {
		final StructuredName n = new StructuredName();

		if (this.cus.getCusAccountType() == LovState.AccountType.juristisch) {
			n.setFamily(this.cus.getCusCompany());
		} else {
			n.setFamily(this.cus.getCusName());
			n.setGiven(this.cus.getCusFirstName());
		}

		this.vcard.setStructuredName(n);
		this.vcard.setFormattedName(this.cus.getShortname());
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
			LOG.error("Can not create Stream", e);
		}

		return null;
	}

	private String getTempFileName(final Customer cus) {
		final String tempDir = System.getProperty("java.io.tmpdir");

		final String prefix = cus.getShortname() + "_" + cus.getCusId();

		final String fname = tempDir + "/" + prefix + this.fextension;
		return fname;
	}

}
