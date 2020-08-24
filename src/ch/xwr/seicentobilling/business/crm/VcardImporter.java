package ch.xwr.seicentobilling.business.crm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovCrm.LinkType;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.AccountType;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.NumberRangeHandler;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

public class VcardImporter {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VcardImporter.class);

	private Customer cus = null;
	private File file = null;
	private VCard vcard = null;
	private boolean isValid = false;
	private List<CustomerLink> lst;
	private City cty = null;

	public VcardImporter(final File vcard) {
		this.file = vcard;
		this.isValid = false;
	}

	public CustomerDto processVcard() {
		LOG.debug("Start creating vcard");

		CustomerDto dto = new CustomerDto();

		try {
			this.vcard = Ezvcard.parse(this.file).first();
			readFile();

			final Long id = existCustomer();
			if (id > 0) {
				this.cus.setCusId(id);
			}

			dto = getCusDto();

			this.isValid = true;

		} catch (final IOException e) {
			LOG.error(e);
		}
		return dto;
	}

	private CustomerDto getCusDto() {
		final CustomerDto dto = new CustomerDto();
		dto.setCustomer(this.cus);

		for (final CustomerLink link : this.lst) {
			dto.getClinks().add(link);
		}

//		for (final Address adrs : lst) {
//			dto.getAdrs().add(adrs);
//		}

		return dto;
	}

	private Long existCustomer() {
		final CustomerDAO dao = new CustomerDAO();
		final List<Customer> res = dao.findByCompanyAndName(this.cus.getCusCompany(), this.cus.getCusName());

		if (res != null && res.size() > 0) {
			return res.get(0).getCusId();
		}
		return (long) 0;
	}

	public void saveVcard() {
		if (this.isValid) {
			final CityDAO cdao = new CityDAO();
			if (this.cty.getCtyId() == null || this.cty.getCtyId() < 0 ) {
				this.cty = cdao.save(this.cty);
				this.cus.setCity(this.cty);
			}
			final CustomerDAO cusdao = new CustomerDAO();
			final NumberRangeHandler handler = new NumberRangeHandler();

			this.cus.setCusNumber(handler.getNewCustomerNumber(false, 0));
			this.cus = cusdao.save(this.cus);
			this.cus.setCusNumber(handler.getNewCustomerNumber(true, this.cus.getCusNumber()));

			if (this.lst.size() > 0) {
				saveCustomerLinks();
			}
		}
	}

	private void saveCustomerLinks() {
		final CustomerLinkDAO dao = new CustomerLinkDAO();
		for (final CustomerLink customerLink : this.lst) {
			customerLink.setCustomer(this.cus);
			dao.save(customerLink);

		}
	}

	private void readFile() throws IOException {
		//this.dao = new CityDAO();
		this.cus = getNewCusBean();
		this.cty = new City();
		this.lst = new ArrayList<>();

		LOG.info("Start processing file " + this.file.getName());
		handleOrg();
		handleAdr();
		handleTel();
		handleEmail();
		LOG.info("End processing file " + this.file.getName());
	}

	private void handleEmail() {
		final List<Email> lst1 = this.vcard.getEmails();

		for (final Email eml : lst1) {
		    final CustomerLink link = new CustomerLink();
		    link.setCnkType(LinkType.mail);
		    link.setCnkState(State.active);
		    link.setCnkIndex((short) 1);

		    link.setCnkLink(eml.getValue());
		    this.lst.add(link);
		}

	}


	private void handleTel() {
		final List<Telephone> lst1 = this.vcard.getTelephoneNumbers();

		for (final Telephone tel : lst1) {
		    final CustomerLink link = new CustomerLink();
		    link.setCnkType(LinkType.phone);
		    link.setCnkState(State.active);
		    link.setCnkIndex((short) 1);

		    link.setCnkLink(tel.getText());
		    if (tel.getUri() != null) {
			    link.setCnkLink(tel.getUri().getNumber());
		    }
		    this.lst.add(link);
		}
	}

	private void handleAdr() {
		int icount = 0;
		final City ctyl = new City();

		final List<Address> lsa = this.vcard.getAddresses();
		if (lsa != null && lsa.size() > 0) {
			for (final Address address : lsa) {
				if (icount == 0) {
					this.cus.setCusAddress(address.getStreetAddress());

					ctyl.setCtyName(address.getLocality());
			    	ctyl.setCtyZip(Integer.parseInt(address.getPostalCode()));
			    	ctyl.setCtyCountry(address.getCountry());
			    	ctyl.setCtyRegion(address.getRegion());

				    this.cty = lookupCity(ctyl);
				    this.cus.setCity(this.cty);
				}
				icount++;
			}
		}
	}

	private City lookupCity(final City ctyl) {
		final CityDAO dao = new CityDAO();

		if (ctyl.getCtyZip() > 0) {
			final List<City> ls = dao.findByZip(ctyl.getCtyZip());
			if (ls != null && ls.size() > 0) {
				return ls.get(0);
			}

		}

		final List<City> ls = dao.findByName(ctyl.getCtyName());
		if (ls != null && ls.size() > 0) {
			return ls.get(0);
		}

		return ctyl;
	}

	private void handleOrg() {
		if (this.vcard.getOrganization() != null || this.vcard.getStructuredName().getGiven() == null) {
		    this.cus.setCusAccountType(AccountType.juristisch);
			this.cus.setCusCompany(this.vcard.getStructuredName().getFamily());
			this.cus.setCusName(" ");
			if (this.vcard.getStructuredName().getGiven() != null) {
				this.cus.setCusName(this.vcard.getStructuredName().getGiven());
			};
			if (this.vcard.getStructuredName().getAdditionalNames() != null
					&& this.vcard.getStructuredName().getAdditionalNames().size() > 0) {
				this.cus.setCusFirstName(this.vcard.getStructuredName().getAdditionalNames().get(0));
			};
		} else {
		    this.cus.setCusAccountType(AccountType.natürlich);
			this.cus.setCusName(this.vcard.getStructuredName().getFamily());
			this.cus.setCusFirstName(this.vcard.getStructuredName().getGiven());
			if (this.vcard.getBirthday() != null) {
				this.cus.setCusBirthdate(this.vcard.getBirthday().getDate());
			}
		}
	}


	private Customer getNewCusBean() {
		final PaymentConditionDAO dao = new PaymentConditionDAO();
		final Customer bean = new Customer();
		bean.setCusState(LovState.State.active);
		bean.setPaymentCondition(dao.find((long) 1));
		bean.setCusBillingTarget(LovCrm.BillTarget.pdf);
		bean.setCusBillingReport(LovCrm.BillReport.working);
		bean.setCusAccountType(AccountType.juristisch);

		return bean;
	}
}
