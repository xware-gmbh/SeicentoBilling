package ch.xwr.seicentobilling.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Project;

public class BillDto {
	private int customerNbr = 0;
	private Project project;
	private Customer customer;
	private CostAccount costaccount;
	private Double totalAmount;
	public Customer getCustomer() {
		return this.customer;
	}
	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}
	public Double getTotalAmount() {
		return this.totalAmount;
	}
	public void setTotalAmount(final Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	private List<BillLine> expenseHours = new ArrayList<>();
	private List<BillLine> journeyHours = new ArrayList<>();
	private List<BillLine> projectHours = new ArrayList<>();

	public int getCustomerNbr() {
		return this.customerNbr;
	}
	public void setCustomerNbr(final int customerNbr) {
		this.customerNbr = customerNbr;
	}
	public List<BillLine> getExpenseHours() {
		return this.expenseHours;
	}
	public void setExpenseHours(final List<BillLine> expenses) {
		this.expenseHours = expenses;
	}
	public List<BillLine> getJourneyHours() {
		return this.journeyHours;
	}
	public void setJourneyHours(final List<BillLine> journey) {
		this.journeyHours = journey;
	}
	public List<BillLine> getProjectHours() {
		return this.projectHours;
	}
	public void setProjectHours(final List<BillLine> project) {
		this.projectHours = project;
	}
	public Project getProject() {
		return this.project;
	}
	public void setProject(final Project project) {
		this.project = project;
	}
	public CostAccount getCostaccount() {
		return this.costaccount;
	}
	public void setCostaccount(final CostAccount costaccount) {
		this.costaccount = costaccount;
	}
}
