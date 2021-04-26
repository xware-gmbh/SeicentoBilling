package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ProjectDAO;

/**
 * Project
 */
@DAO(daoClass = ProjectDAO.class)
@Caption("{%proName}")
@Entity
@Table(name = "Project", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "proName"))
public class Project implements java.io.Serializable {

	private Long proId;
	private CostAccount costAccount;
	private Customer customer;
	private Project project;
	private Vat vat;
	private String proName;
	private String proExtReference;
	private Date proStartDate;
	private Date proEndDate;
	private Integer proHours;
	private Integer proIntensityPercent;
	private Date proLastBill;
	private double proRate;
	private LovState.ProModel proModel;
	private LovState.State proState;
	private String proDescription;
	private String proRemark;
	private LovState.ProState proProjectState;
	private Double proHoursEffective;
	private Set<Project> projects = new HashSet<>(0);
	private Set<ResPlanning> resPlannings = new HashSet<>(0);
	private Set<ProjectLine> projectLines = new HashSet<>(0);
	private Set<Order> orders = new HashSet<>(0);
	private Set<Expense> expenses = new HashSet<>(0);
	private String proContact;
	private Address address;
	private Boolean internal;
	private LovState.ProOrderStrategy proOrdergenerationStrategy;
	private Set<ProjectAllocation> projectAllocations = new HashSet<>(0);

	public Project() {
	}

	@Caption("ProId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "proId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getProId() {
		return this.proId;
	}

	public void setProId(final Long proId) {
		this.proId = proId;
	}

	@Caption("Kostenstelle")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "procsaId", columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("Kunde")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "procusId", nullable = false, columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "proproId", columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("Mwst")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "provatId", columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}

	@Caption("Projektname")
	@Column(name = "proName", unique = true, nullable = false, columnDefinition = "nvarchar")
	public String getProName() {
		return this.proName;
	}

	public void setProName(final String proName) {
		this.proName = proName;
	}

	@Caption("ProExtReference")
	@Column(name = "proExtReference", columnDefinition = "nvarchar")
	public String getProExtReference() {
		return this.proExtReference;
	}

	public void setProExtReference(final String proExtReference) {
		this.proExtReference = proExtReference;
	}

	@Caption("Projektstart")
	@Temporal(TemporalType.DATE)
	@Column(name = "proStartDate", nullable = false, columnDefinition = "date", length = 10)
	public Date getProStartDate() {
		return this.proStartDate;
	}

	public void setProStartDate(final Date proStartDate) {
		this.proStartDate = proStartDate;
	}

	@Caption("Projektende")
	@Temporal(TemporalType.DATE)
	@Column(name = "proEndDate", columnDefinition = "date", length = 10)
	public Date getProEndDate() {
		return this.proEndDate;
	}

	public void setProEndDate(final Date proEndDate) {
		this.proEndDate = proEndDate;
	}

	@Caption("Stundensoll")
	@Column(name = "proHours", columnDefinition = "int")
	public Integer getProHours() {
		if (this.proHours == null) {
			this.proHours = new Integer(1);
		}
		return this.proHours;
	}

	public void setProHours(final Integer proHours) {
		this.proHours = proHours;
	}

	@Caption("ProIntensityPercent")
	@Column(name = "proIntensityPercent", columnDefinition = "int")
	public Integer getProIntensityPercent() {
		return this.proIntensityPercent;
	}

	public void setProIntensityPercent(final Integer proIntensityPercent) {
		this.proIntensityPercent = proIntensityPercent;
	}

	@Caption("ProLastBill")
	@Temporal(TemporalType.DATE)
	@Column(name = "proLastBill", columnDefinition = "date", length = 10)
	public Date getProLastBill() {
		return this.proLastBill;
	}

	public void setProLastBill(final Date proLastBill) {
		this.proLastBill = proLastBill;
	}

	@Caption("Ansatz")
	@Column(name = "proRate", nullable = false, columnDefinition = "decimal", precision = 6)
	public double getProRate() {
		return this.proRate;
	}

	public void setProRate(final double proRate) {
		this.proRate = proRate;
	}

	@Caption("Modell")
	@Column(name = "proModel", columnDefinition = "smallint")
	public LovState.ProModel getProModel() {
		return this.proModel;
	}

	public void setProModel(final LovState.ProModel proModel) {
		this.proModel = proModel;
	}

	@Caption("Status")
	@Column(name = "proState", columnDefinition = "smallint")
	public LovState.State getProState() {
		return this.proState;
	}

	public void setProState(final LovState.State proState) {
		this.proState = proState;
	}

	@Caption("ProDescription")
	@Lob
	@Column(name = "proDescription", columnDefinition = "ntext")
	public String getProDescription() {
		return this.proDescription;
	}

	public void setProDescription(final String proDescription) {
		this.proDescription = proDescription;
	}

	@Caption("ProRemark")
	@Lob
	@Column(name = "proRemark", columnDefinition = "ntext")
	public String getProRemark() {
		return this.proRemark;
	}

	public void setProRemark(final String proRemark) {
		this.proRemark = proRemark;
	}

	@Caption("Projektampel")
	@Column(name = "proProjectState", columnDefinition = "smallint")
	public LovState.ProState getProProjectState() {
		return this.proProjectState;
	}

	public void setProProjectState(final LovState.ProState proProjectState) {
		this.proProjectState = proProjectState;
	}

	@Caption("Stundenist")
	@Column(name = "proHoursEffective", columnDefinition = "decimal", precision = 18, updatable=false, insertable=false, nullable=false)
	public Double getProHoursEffective() {
		if (this.proHoursEffective == null) {
			this.proHoursEffective = new Double(0.);
		}
		return this.proHoursEffective;
	}

	public void setProHoursEffective(final Double proHoursEffective) {
		this.proHoursEffective = proHoursEffective;
	}

	@Caption("Projects")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(final Set<Project> projects) {
		this.projects = projects;
	}

	@Caption("ResPlannings")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<ResPlanning> getResPlannings() {
		return this.resPlannings;
	}

	public void setResPlannings(final Set<ResPlanning> resPlannings) {
		this.resPlannings = resPlannings;
	}

	@Caption("ProjectLines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<ProjectLine> getProjectLines() {
		return this.projectLines;
	}

	public void setProjectLines(final Set<ProjectLine> projectLines) {
		this.projectLines = projectLines;
	}

	@Caption("Orders")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(final Set<Order> orders) {
		this.orders = orders;
	}

	@Caption("Expenses")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<Expense> getExpenses() {
		return this.expenses;
	}

	public void setExpenses(final Set<Expense> expenses) {
		this.expenses = expenses;
	}

	@Caption("Ansprechpartner")
	@Column(name = "proContact", length = 40, columnDefinition = "nvarchar")
	public String getProContact() {
		return this.proContact;
	}

	public void setProContact(final String contact) {
		this.proContact = contact;
	}

	@Caption("Billingaddress")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "proadrId", columnDefinition = "bigint")
	public Address getAddress() {
		return this.address;
	}

	public void setAddress(final Address address) {
		this.address = address;
	}

	@Caption("Intern")
	@Column(name = "proInternal", columnDefinition = "bit")
	public Boolean getInternal() {
		if (this.internal == null) {
			return false;
		}
		return this.internal;
	}

	public void setInternal(final Boolean internal) {
		this.internal = internal;
	}

	@Caption("Rechnungsstrategie")
	@Column(name = "proOrdergenerationStrategy", columnDefinition = "smallint")
	public LovState.ProOrderStrategy getProOrdergenerationStrategy() {
		return this.proOrdergenerationStrategy;
	}

	public void setProOrdergenerationStrategy(final LovState.ProOrderStrategy proOrdergenerationStrategy) {
		this.proOrdergenerationStrategy = proOrdergenerationStrategy;
	}

	@Caption("Projektressourcen")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	public Set<ProjectAllocation> getProjectAllocations() {
		return this.projectAllocations;
	}

	public void setProjectAllocations(final Set<ProjectAllocation> projectAllocations) {
		this.projectAllocations = projectAllocations;
	}

}
