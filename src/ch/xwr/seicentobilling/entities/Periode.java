package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.PeriodeDAO;

/**
 * Periode
 */
@DAO(daoClass = PeriodeDAO.class)
@Caption("{%perName}")
@Entity
@Table(name = "Periode", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"perYear", "perMonth", "percsaId" }))
public class Periode implements java.io.Serializable {

	private Long perId;
	private CostAccount costAccount;
	private String perName;
	private Integer perYear;
	private LovState.Month perMonth;
	private LovState.BookingType perBookedExpense;
	private LovState.BookingType perBookedProject;
	private LovState.State perState;
	private Set<Expense> expenses = new HashSet<>(0);
	private Set<ProjectLine> projectLines = new HashSet<>(0);
	private Boolean perSignOffExpense;

	public Periode() {
	}

	@Caption("PerId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "perId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPerId() {
		return this.perId;
	}

	public void setPerId(final Long perId) {
		this.perId = perId;
	}

	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "percsaId", nullable = false, columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("PerName")
	@Column(name = "perName", columnDefinition = "nvarchar")
	public String getPerName() {
		return this.perName;
	}

	public void setPerName(final String perName) {
		this.perName = perName;
	}

	@Caption("PerYear")
	@Column(name = "perYear", columnDefinition = "int")
	public Integer getPerYear() {
		return this.perYear;
	}

	public void setPerYear(final Integer perYear) {
		this.perYear = perYear;
	}

	@Caption("PerMonth")
	@Column(name = "perMonth", columnDefinition = "int")
	public LovState.Month  getPerMonth() {
		return this.perMonth;
	}

	public void setPerMonth(final LovState.Month perMonth) {
		this.perMonth = perMonth;
	}

	@Caption("PerBookedExpense")
	@Column(name = "perBookedExpense", columnDefinition = "smallint")
	public LovState.BookingType getPerBookedExpense() {
		return this.perBookedExpense;
	}

	public void setPerBookedExpense(final LovState.BookingType perBookedExpense) {
		this.perBookedExpense = perBookedExpense;
	}

	@Caption("PerBookedProject")
	@Column(name = "perBookedProject", columnDefinition = "smallint")
	public LovState.BookingType getPerBookedProject() {
		return this.perBookedProject;
	}

	public void setPerBookedProject(final LovState.BookingType perBookedProject) {
		this.perBookedProject = perBookedProject;
	}

	@Caption("PerState")
	@Column(name = "perState", columnDefinition = "smallint")
	public LovState.State getPerState() {
		return this.perState;
	}

	public void setPerState(final LovState.State perState) {
		this.perState = perState;
	}

	//Scheint 0 zurück zu geben, obwohl vorhanden. Query löst das Problem. Gleiches Verhalten in Quarkus.
	@Caption("Expenses")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "periode")
	public Set<Expense> getExpenses() {
		//throw new Exception("do not use! Use DAO.");
		return this.expenses;
	}

	public void setExpenses(final Set<Expense> expenses) {
		this.expenses = expenses;
	}

	@Caption("ProjectLines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "periode")
	public Set<ProjectLine> getProjectLines() {
		return this.projectLines;
	}

	public void setProjectLines(final Set<ProjectLine> projectLines) {
		this.projectLines = projectLines;
	}

    /**
     * Set my transient property at load time based on a calculation,
     * note that a native Hibernate formula mapping is better for this purpose.
     */
    @PrePersist
    @PreUpdate
    public void preSaveAction() {
    	//validate Month
    	if (getPerMonth().getValue() < 1) {
    		setPerMonth(LovState.Month.januar);
    	}
    	//calculate Name
    	final String month = String.format("%02d", getPerMonth().getValue());
    	final String name = "" + getPerYear() + "-" + month + " " + getCostAccount().getCsaName();
    	this.setPerName(name);
    }

	@Caption("Freigabe Buchhaltung")
	@Column(name = "perSignOffExpense")
	public Boolean getPerSignOffExpense() {
		return this.perSignOffExpense;
	}

	public void setPerSignOffExpense(final Boolean noname) {
		this.perSignOffExpense = noname;
	}
}
