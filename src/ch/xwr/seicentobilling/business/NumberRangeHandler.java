package ch.xwr.seicentobilling.business;

import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;

public class NumberRangeHandler {
	final CompanyDAO DAO = new CompanyDAO();
	private Company BEAN = null;

	public NumberRangeHandler() {
		this.BEAN = this.DAO.getActiveConfig();
	}

	public Integer getNewOrderNumber(final boolean commit, final Integer nbr) {
		final Integer value = this.BEAN.getCmpLastOrderNbr() + 1;

		if (commit) {
			this.BEAN.setCmpLastOrderNbr(value);
			saveIt(nbr, value);
		}
		return value;
	}

	public Integer getNewCustomerNumber(final boolean commit, final Integer nbr) {
		final Integer value = this.BEAN.getCmpLastCustomerNbr() + 1;

		if (commit) {
			this.BEAN.setCmpLastCustomerNbr(value);
			saveIt(nbr, value);
		}
		return value;
	}

	public Integer getNewItemNumber(final boolean commit, final Integer nbr) {
		final Integer value = this.BEAN.getCmpLastItemNbr() + 1;

		if (commit) {
			this.BEAN.setCmpLastItemNbr(value);
			saveIt(nbr, value);
		}
		return value;
	}

	private void saveIt(final Integer nbr, final Integer value) {
		this.DAO.persist(this.BEAN);

		if (nbr.intValue() != value.intValue()) {
			System.out.println("Warning: Ordernumber was increased in the meantime!!");
		}
	}
}
