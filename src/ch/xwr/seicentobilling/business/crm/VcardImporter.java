package ch.xwr.seicentobilling.business.crm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

public class VcardImporter {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VcardImporter.class);

	private Customer cus = null;
	private File file = null;
	private int icount;
	private boolean isValid = false;
	private List<CustomerLink> lst;
	private City cty = null;

	public VcardImporter(final File vcard) {
		this.file = vcard;
		this.isValid = false;
	}

	public Customer processVcard() {
		LOG.debug("Start creating vcard");

		try {
			readFile();
			this.isValid = true;

		} catch (final IOException e) {
			LOG.error(e);
		}
		return this.cus;
	}

	public void saveVcard() {
		if (this.isValid) {
			final CityDAO cdao = new CityDAO();
			if (this.cty.getCtyId() < 0 ) {
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
		BufferedReader vcrdr = null;
		try {
			vcrdr = new BufferedReader(new FileReader(this.file));
			loopLines(vcrdr);
			vcrdr.close();

		} catch (final FileNotFoundException e) {
			LOG.error("IO", e);
		} catch (final IOException e) {
			LOG.error("IO", e);
		}

	}

	private void loopLines(final BufferedReader vcrdr) throws IOException {
		String row = "";
		while ((row = vcrdr.readLine()) != null) {
			this.icount++;
			if (this.icount == 1) {
				if (!row.equalsIgnoreCase("BEGIN:VCARD")) {
					throw new IOException("No valid vcard format 4.0");
				}
			} else {
				if (row.equalsIgnoreCase("END:VCARD")) {
					//ignore
				}
				if (row.startsWith("VERSION:")) {
					//ignore
				}
				if (row.startsWith("ORG:")) {
					handleOrg(row);
				}
				if (row.startsWith("N:")) {
					handleN(row);
				}
				if (row.startsWith("FN:")) {
					//ignore (fullname)
				}
				if (row.startsWith("ADR;")) {
					handleAdr(row);
				}
				if (row.startsWith("TEL;")) {
					handleTel(row);
				}
				if (row.startsWith("EMAIL:")) {
					handleEmail(row);
				}
			}
		}
	}

	private void handleEmail(final String row) {
		//this.vcard.println("EMAIL:"  + customerLink.getCnkLink());
	    final String[] data = row.split(":");

	    final CustomerLink link = new CustomerLink();
	    link.setCnkType(LinkType.mail);
	    link.setCnkState(State.active);
	    link.setCnkIndex((short) 1);
	    link.setCnkLink(data[1]);

	    this.lst.add(link);
	}

	private void handleTel(final String row) {
		//this.vcard.println("TEL;TYPE=work,voice;VALUE=uri:tel:" + customerLink.getCnkLink());
	    final String[] data = row.split(":");

	    final CustomerLink link = new CustomerLink();
	    link.setCnkType(LinkType.phone);
	    link.setCnkState(State.active);
	    link.setCnkIndex((short) 1);
	    link.setCnkLink(getVal(data,2));

	    this.lst.add(link);
	}

	private void handleAdr(final String row) {
		//this.vcard.println("ADR;Type=home:;;" + this.cus.getCusAddress() + ";" + this.cus.getCity().getCtyName() + ";" + this.cus.getCity().getCtyRegion() + ";" + this.cus.getCity().getCtyZip() + ";" + this.cus.getCity().getCtyCountry());
		final City ctyl = new City();

	    final String[] parent = row.split(":");
	    final String[] data = parent[1].split(";");

	    String val = getVal(data, 2);
	    if (!val.isEmpty()) {
	    	this.cus.setCusAddress(val);
	    }

	    val = getVal(data, 3);
	    if (!val.isEmpty()) {
	    	ctyl.setCtyName(val);
	    }
	    val = getVal(data, 4);
	    if (!val.isEmpty()) {
	    	ctyl.setCtyRegion(val);
	    }
	    val = getVal(data, 5);
	    if (!val.isEmpty()) {
	    	ctyl.setCtyZip(Integer.parseInt(val));
	    }
	    val = getVal(data, 6);
	    if (!val.isEmpty()) {
	    	ctyl.setCtyCountry(val);
	    }

	    this.cty = lookupCity(ctyl);
	    this.cus.setCity(this.cty);

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

	private void handleN(final String row) {
	    final String[] parent = row.split(":");
	    final String[] data = parent[1].split(";");

	    String val = getVal(data, 0);
	    if (!val.isEmpty()) {
	    	this.cus.setCusCompany(val);
	    }
	    val = getVal(data, 1);
	    if (!val.isEmpty()) {
	    	this.cus.setCusName(val);
	    }
	    val = getVal(data, 2);
	    if (!val.isEmpty()) {
	    	this.cus.setCusFirstName(val);
	    }

	}

	private void handleOrg(final String row) {
	    final String[] data = row.split(":");

	    this.cus.setCusAccountType(AccountType.juristisch);
	    this.cus.setCusCompany(data[1]);
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


	private String getVal(final String[] data, final int idx) {
		if ((idx + 1) > data.length) {
			return "";
		}
		return data[idx];
	}
}
