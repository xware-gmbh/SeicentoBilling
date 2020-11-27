package ch.xwr.seicentobilling.business.model.billing;

import java.util.Date;

import ch.xwr.seicentobilling.entities.Item;

public class GuiGeneratorFields {
	String billText;
	String lineTextProject;
	String lineTextExpense;
	String lineTextJourney;
	Item itemProject;
	Item itemExpense;
	Item itemJourney;
	Date billDate;

	public String getBillText() {
		return this.billText;
	}
	public void setBillText(final String billText) {
		this.billText = billText;
	}
	public String getLineTextProject() {
		return this.lineTextProject;
	}
	public void setLineTextProject(final String lineTextProject) {
		this.lineTextProject = lineTextProject;
	}
	public String getLineTextExpense() {
		return this.lineTextExpense;
	}
	public void setLineTextExpense(final String lineTextExpense) {
		this.lineTextExpense = lineTextExpense;
	}
	public String getLineTextJourney() {
		return this.lineTextJourney;
	}
	public void setLineTextJourney(final String lineTextJourney) {
		this.lineTextJourney = lineTextJourney;
	}
	public Item getItemProject() {
		return this.itemProject;
	}
	public void setItemProject(final Item itemProject) {
		this.itemProject = itemProject;
	}
	public Item getItemExpense() {
		return this.itemExpense;
	}
	public void setItemExpense(final Item itemExpense) {
		this.itemExpense = itemExpense;
	}
	public Item getItemJourney() {
		return this.itemJourney;
	}
	public void setItemJourney(final Item itemJourney) {
		this.itemJourney = itemJourney;
	}
	public Date getBillDate() {
		return this.billDate;
	}
	public void setBillDate(final Date billDate) {
		this.billDate = billDate;
	}
	public Boolean getCopyTextFromLastBill() {
		return this.copyTextFromLastBill;
	}
	public void setCopyTextFromLastBill(final Boolean copyTextFromLastBill) {
		this.copyTextFromLastBill = copyTextFromLastBill;
	}
	Boolean copyTextFromLastBill;

}
