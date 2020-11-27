package ch.xwr.seicentobilling.business.model.billing;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.entities.CostAccount;

public class BillLine {
	private Double hours;
	private LovState.WorkType workType;
	private Double rate;
	@SuppressWarnings("unused")
	private Double amount;
	private CostAccount costaccount;



	public Double getHours() {
		return this.hours;
	}
	public void setHours(final Double hours) {
		this.hours = hours;
	}
	public LovState.WorkType getWorkType() {
		return this.workType;
	}
	public void setWorkType(final LovState.WorkType workType) {
		this.workType = workType;
	}
	public Double getRate() {
		return this.rate;
	}
	public void setRate(final Double rate) {
		this.rate = rate;
	}
	public Double getAmount() {
		return getHours() * getRate();
	}
	public CostAccount getCostaccount() {
		return costaccount;
	}
	public void setCostaccount(CostAccount costaccount) {
		this.costaccount = costaccount;
	}
}
