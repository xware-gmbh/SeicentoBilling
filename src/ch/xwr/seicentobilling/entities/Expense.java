package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ExpenseDAO;

/**
 * Expense
 */
@DAO(daoClass = ExpenseDAO.class)
@Caption("{%expAccount}")
@Entity
@Table(name = "Expense", schema = "dbo")
public class Expense implements java.io.Serializable {

	private Long expId;
	private Periode periode;
	private Project project;
	private Vat vat;
	private String expAccount;
	private Boolean expFlagCostAccount;
	private LovState.ExpType expFlagGeneric;
	private Date expDate;
	private String expText;
	private LovState.ExpUnit expUnit;
	private Double expQuantity;
	private double expAmount;
	private Date expBooked;
	private LovState.State expState;
	private double expAmountWOTax;

	public Expense() {
	}

	@Caption("ExpId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "expId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getExpId() {
		return this.expId;
	}

	public void setExpId(final Long expId) {
		this.expId = expId;
	}

	@Caption("Periode")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "expperId", nullable = false, columnDefinition = "bigint")
	public Periode getPeriode() {
		return this.periode;
	}

	public void setPeriode(final Periode periode) {
		this.periode = periode;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "expproId", nullable = false, columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("Vat")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "expvatId", columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}

	@Caption("ExpAccount")
	@Column(name = "expAccount", columnDefinition = "nvarchar")
	public String getExpAccount() {
		return this.expAccount;
	}

	public void setExpAccount(final String expAccount) {
		this.expAccount = expAccount;
	}

	@Caption("ExpFlagCostAccount")
	@Column(name = "expFlagCostAccount", columnDefinition = "bit")
	public Boolean getExpFlagCostAccount() {
		return this.expFlagCostAccount;
	}

	public void setExpFlagCostAccount(final Boolean expFlagCostAccount) {
		this.expFlagCostAccount = expFlagCostAccount;
	}

	@Caption("ExpFlagGeneric")
	@Column(name = "expFlagGeneric", columnDefinition = "smallint")
	public LovState.ExpType getExpFlagGeneric() {
		return this.expFlagGeneric;
	}

	public void setExpFlagGeneric(final LovState.ExpType expFlagGeneric) {
		this.expFlagGeneric = expFlagGeneric;
	}

	@Caption("ExpDate")
	@Temporal(TemporalType.DATE)
	@Column(name = "expDate", nullable = false, columnDefinition = "date", length = 10)
	public Date getExpDate() {
		return this.expDate;
	}

	public void setExpDate(final Date expDate) {
		this.expDate = expDate;
	}

	@Caption("ExpText")
	@Column(name = "expText", columnDefinition = "nvarchar")
	public String getExpText() {
		return this.expText;
	}

	public void setExpText(final String expText) {
		this.expText = expText;
	}

	@Caption("ExpUnit")
	@Column(name = "expUnit", columnDefinition = "smallint")
	public LovState.ExpUnit getExpUnit() {
		return this.expUnit;
	}

	public void setExpUnit(final LovState.ExpUnit expUnit) {
		this.expUnit = expUnit;
	}

	@Caption("ExpQuantity")
	@Column(name = "expQuantity", columnDefinition = "decimal", precision = 6)
	public Double getExpQuantity() {
		return this.expQuantity;
	}

	public void setExpQuantity(final Double expQuantity) {
		this.expQuantity = expQuantity;
	}

	@Caption("ExpAmount")
	@Column(name = "expAmount", nullable = false, columnDefinition = "decimal", precision = 6)
	public double getExpAmount() {
		return this.expAmount;
	}

	public void setExpAmount(final double expAmount) {
		this.expAmount = expAmount;
	}

	@Caption("Betrag exkl.")
	@Column(name = "expAmountWOTax", nullable = false, columnDefinition = "decimal", precision = 6)
	public double getExpAmountWOTax() {
		return this.expAmountWOTax;
	}

	public void setExpAmountWOTax(final double expAmountWOTax) {
		this.expAmountWOTax = expAmountWOTax;
	}

	@Caption("ExpBooked")
	@Temporal(TemporalType.DATE)
	@Column(name = "expBooked", columnDefinition = "date", length = 10)
	public Date getExpBooked() {
		return this.expBooked;
	}

	public void setExpBooked(final Date expBooked) {
		this.expBooked = expBooked;
	}

	@Caption("ExpState")
	@Column(name = "expState", columnDefinition = "smallint")
	public LovState.State getExpState() {
		return this.expState;
	}

	public void setExpState(final LovState.State expState) {
		this.expState = expState;
	}

}
