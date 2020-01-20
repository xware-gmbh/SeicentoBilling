package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.VatDAO;

/**
 * Vat
 */
//@EntityListeners(RowObjectListener.class)
@DAO(daoClass = VatDAO.class)
@Caption("{%vatName}")
@Entity
@Table(name = "Vat", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "vatSign"))
public class Vat implements java.io.Serializable {

	private Long vatId;
	private String vatName;
	private Double vatRate;
	private String vatSign;
	private Boolean vatInclude;
	private LovState.State vatState;
	private Set<Item> items = new HashSet<>(0);
	private Set<Project> projects = new HashSet<>(0);
	private Set<OrderLine> orderLines = new HashSet<>(0);
	private Set<Expense> expenses = new HashSet<>(0);
	private String fullName;
	private String vatExtRef;
	private String vatExtRef1;

	public Vat() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "vatId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getVatId() {
		return this.vatId;
	}

	public void setVatId(final Long vatId) {
		this.vatId = vatId;
	}

	@Caption("Name")
	@Column(name = "vatName", columnDefinition = "nvarchar")
	public String getVatName() {
		return this.vatName;
	}

	public void setVatName(final String vatName) {
		this.vatName = vatName;
	}

	@Caption("Rate")
	@Column(name = "vatRate", columnDefinition = "numeric", precision = 6, scale = 4)
	public Double getVatRate() {
		return this.vatRate;
	}

	public void setVatRate(final Double vatRate) {
		this.vatRate = vatRate;
	}

	@Caption("Sign")
	@Column(name = "vatSign", unique = true, columnDefinition = "nvarchar")
	public String getVatSign() {
		return this.vatSign;
	}

	public void setVatSign(final String vatSign) {
		this.vatSign = vatSign;
	}

	@Caption("Include")
	@Column(name = "vatInclude", columnDefinition = "bit")
	public Boolean getVatInclude() {
		return this.vatInclude;
	}

	public void setVatInclude(final Boolean vatInclude) {
		this.vatInclude = vatInclude;
	}

	@Caption("State")
	@Column(name = "vatState", columnDefinition = "smallint")
	public LovState.State getVatState() {
		return this.vatState;
	}

	public void setVatState(final LovState.State vatState) {
		this.vatState = vatState;
	}

	@Caption("Items")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vat")
	public Set<Item> getItems() {
		return this.items;
	}

	public void setItems(final Set<Item> items) {
		this.items = items;
	}

	@Caption("Projects")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vat")
	public Set<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(final Set<Project> projects) {
		this.projects = projects;
	}

	@Caption("OrderLines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vat")
	public Set<OrderLine> getOrderLines() {
		return this.orderLines;
	}

	public void setOrderLines(final Set<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

	@Caption("Expenses")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vat")
	public Set<Expense> getExpenses() {
		return this.expenses;
	}

	public void setExpenses(final Set<Expense> expenses) {
		this.expenses = expenses;
	}

	@Column(name = "FULLNAME", insertable = false, updatable = false)
	@Transient
	public String getFullName() {
		final StringBuffer sb = new StringBuffer("");
		sb.append(this.vatSign).append(" - ").append(this.vatName).append(", ").append(this.vatState);

		this.fullName = sb.toString();
		return this.fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	@Caption("Ext Referenz 1")
	@Column(name = "vatExtRef", columnDefinition = "nvarchar", length = 20)
	public String getVatExtRef() {
		return this.vatExtRef;
	}

	public void setVatExtRef(final String noname) {
		this.vatExtRef = noname;
	}

	@Caption("Ext Referenz 2")
	@Column(name = "vatExtRef1", length = 20)
	public String getVatExtRef1() {
		return this.vatExtRef1;
	}

	public void setVatExtRef1(final String noname) {
		this.vatExtRef1 = noname;
	}


}
