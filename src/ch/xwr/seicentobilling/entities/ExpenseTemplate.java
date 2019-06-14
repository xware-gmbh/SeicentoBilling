package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ExpenseTemplateDAO;

/**
 * Expense
 */
@DAO(daoClass = ExpenseTemplateDAO.class)
@Caption("{%extText}")
@Entity
@Table(name = "ExpenseTemplate", schema = "dbo")
public class ExpenseTemplate implements java.io.Serializable {

	private Long extId;
	private Project project;
	private Vat vat;
	private String extAccount;
	private Boolean extFlagCostAccount;
	private LovState.ExpType extFlagGeneric;
	private String extText;
	private LovState.ExpUnit extUnit;
	private Double extQuantity;
	private double extAmount;
	private LovState.State extState;
	private CostAccount costaccount;
	private int extKeyNumber;


	public ExpenseTemplate() {
	}

	@Caption("ExtId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "extId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getExtId() {
		return this.extId;
	}

	public void setExtId(final Long extId) {
		this.extId = extId;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "extproId", nullable = false, columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("Vat")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "extvatId", columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}

	@Caption("extAccount")
	@Column(name = "extAccount", columnDefinition = "nvarchar")
	public String getExtAccount() {
		return this.extAccount;
	}

	public void setExtAccount(final String extAccount) {
		this.extAccount = extAccount;
	}

	@Caption("extFlagCostAccount")
	@Column(name = "extFlagCostAccount", columnDefinition = "bit")
	public Boolean getExtFlagCostAccount() {
		return this.extFlagCostAccount;
	}

	public void setExtFlagCostAccount(final Boolean extFlagCostAccount) {
		this.extFlagCostAccount = extFlagCostAccount;
	}

	@Caption("extFlagGeneric")
	@Column(name = "extFlagGeneric", columnDefinition = "smallint")
	public LovState.ExpType getExtFlagGeneric() {
		return this.extFlagGeneric;
	}

	public void setExtFlagGeneric(final LovState.ExpType extFlagGeneric) {
		this.extFlagGeneric = extFlagGeneric;
	}

	@Caption("extText")
	@Column(name = "extText", columnDefinition = "nvarchar")
	public String getExtText() {
		return this.extText;
	}

	public void setExtText(final String extText) {
		this.extText = extText;
	}

	@Caption("extUnit")
	@Column(name = "extUnit", columnDefinition = "smallint")
	public LovState.ExpUnit getExtUnit() {
		return this.extUnit;
	}

	public void setExtUnit(final LovState.ExpUnit extUnit) {
		this.extUnit = extUnit;
	}

	@Caption("extQuantity")
	@Column(name = "extQuantity", columnDefinition = "decimal", precision = 6)
	public Double getExtQuantity() {
		return this.extQuantity;
	}

	public void setExtQuantity(final Double extQuantity) {
		this.extQuantity = extQuantity;
	}

	@Caption("extAmount")
	@Column(name = "extAmount", nullable = false, columnDefinition = "decimal", precision = 6)
	public double getExtAmount() {
		return this.extAmount;
	}

	public void setExtAmount(final double extAmount) {
		this.extAmount = extAmount;
	}

	@Caption("extState")
	@Column(name = "extState", columnDefinition = "smallint")
	public LovState.State getExtState() {
		return this.extState;
	}

	public void setExtState(final LovState.State extState) {
		this.extState = extState;
	}

	@Caption("extKeyNumber")
	@Column(name = "extKeyNumber", nullable = false, columnDefinition = "integer")
	public int getExtKeyNumber() {
		return this.extKeyNumber;
	}

	public void setExtKeyNumber(final int noname) {
		this.extKeyNumber = noname;
	}

	@Caption("Costaccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "extcsaId", nullable = false, columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costaccount;
	}

	public void setCostAccount(final CostAccount costaccount) {
		this.costaccount = costaccount;
	}


}
