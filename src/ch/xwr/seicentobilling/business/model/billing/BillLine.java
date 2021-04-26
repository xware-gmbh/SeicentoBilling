package ch.xwr.seicentobilling.business.model.billing;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Item;

public class BillLine {
	private Double hours;
	private LovState.WorkType workType;
	private Double rate;
	@SuppressWarnings("unused")
	private Double amount;
	private CostAccount costaccount;
	private Item item;
	private String text;


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
		return this.costaccount;
	}
	public void setCostaccount(final CostAccount costaccount) {
		this.costaccount = costaccount;
	}
	public Item getItem() {
		return this.item;
	}
	public void setItem(final Item item) {
		this.item = item;
	}
	public String getText() {
		return this.text;
	}
	public void setText(final String text) {
		this.text = text;
	}
}
