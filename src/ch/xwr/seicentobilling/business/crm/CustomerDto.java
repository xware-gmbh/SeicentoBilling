package ch.xwr.seicentobilling.business.crm;

import java.util.ArrayList;
import java.util.List;

import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.LabelDefinition;

public class CustomerDto {
	private final int customerNbr = 0;
	private Customer customer;
	private final List<CostAccount> costaccounts = new ArrayList<>();
	private final List<CustomerLink> clinks = new ArrayList<>();
	private final List<LabelDefinition> labels = new ArrayList<>();

	public Customer getCustomer() {
		return this.customer;
	}
	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}
	public int getCustomerNbr() {
		return this.customerNbr;
	}
	public List<CostAccount> getCostaccounts() {
		return this.costaccounts;
	}
	public List<CustomerLink> getClinks() {
		return this.clinks;
	}
	public List<LabelDefinition> getLabels() {
		return this.labels;
	}
}
