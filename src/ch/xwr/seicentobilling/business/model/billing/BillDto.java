package ch.xwr.seicentobilling.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;

public class BillDto {
	private int customerNbr = 0;
	private Project project;
	private Customer customer;
	private CostAccount costaccount;
	private Double totalAmount;
	private List<BillLine> lines = new ArrayList<>();
	private Periode periode;


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
	public int getCustomerNbr() {
		return this.customerNbr;
	}
	public void setCustomerNbr(final int customerNbr) {
		this.customerNbr = customerNbr;
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
	public Periode getPeriode() {
		return this.periode;
	}
	public void setPeriode(final Periode periode) {
		this.periode = periode;
	}
	public List<BillLine> getLines() {
		return this.lines;
	}
	public void setLines(final List<BillLine> lines) {
		this.lines = lines;
	}
}
