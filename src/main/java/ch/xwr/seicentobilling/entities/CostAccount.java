package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.CostAccountDAO;

/**
 * CostAccount
 */
//@EntityListeners(RowObjectListener.class)
@DAO(CostAccountDAO.class)
@Caption("{%csaCode}")
@Entity
@Table(name = "CostAccount", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "csaCode"))
public class CostAccount implements java.io.Serializable {

	private Long csaId;
	private CostAccount costAccount;
	private String csaCode;
	private String csaName;
	private LovState.State csaState;
	private Set<Periode> periodes = new HashSet<>(0);
	private Set<Project> projects = new HashSet<>(0);
	private Set<ResPlanning> resPlannings = new HashSet<>(0);
	private Set<CostAccount> costAccounts = new HashSet<>(0);
	private Set<OrderLine> orderLines = new HashSet<>(0);
	private String csaExtRef;
	private List<AppUser> users = new ArrayList<>();

	public CostAccount() {
	}

	@Caption("CsaId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "csaId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCsaId() {
		return this.csaId;
	}

	public void setCsaId(final Long csaId) {
		this.csaId = csaId;
	}

	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "csacsaId", columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("CsaCode")
	@Column(name = "csaCode", unique = true, columnDefinition = "nvarchar")
	public String getCsaCode() {
		return this.csaCode;
	}

	public void setCsaCode(final String csaCode) {
		this.csaCode = csaCode;
	}

	@Caption("Name")
	@Column(name = "csaName", columnDefinition = "nvarchar")
	public String getCsaName() {
		return this.csaName;
	}

	public void setCsaName(final String csaName) {
		this.csaName = csaName;
	}

	@Caption("Status")
	@Column(name = "csaState", columnDefinition = "smallint")
	public LovState.State getCsaState() {
		return this.csaState;
	}

	public void setCsaState(final LovState.State csaState) {
		this.csaState = csaState;
	}

	@Caption("Periodes")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public Set<Periode> getPeriodes() {
		return this.periodes;
	}

	public void setPeriodes(final Set<Periode> periodes) {
		this.periodes = periodes;
	}

	@Caption("Projects")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public Set<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(final Set<Project> projects) {
		this.projects = projects;
	}

	@Caption("ResPlannings")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public Set<ResPlanning> getResPlannings() {
		return this.resPlannings;
	}

	public void setResPlannings(final Set<ResPlanning> resPlannings) {
		this.resPlannings = resPlannings;
	}

	@Caption("CostAccounts")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public Set<CostAccount> getCostAccounts() {
		return this.costAccounts;
	}

	public void setCostAccounts(final Set<CostAccount> costAccounts) {
		this.costAccounts = costAccounts;
	}

	@Caption("OrderLines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public Set<OrderLine> getOrderLines() {
		return this.orderLines;
	}

	public void setOrderLines(final Set<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

	@Caption("Referenz extern")
	@Column(name = "csaExtRef", columnDefinition = "nvarchar")
	public String getCsaExtRef() {
		return this.csaExtRef;
	}

	public void setCsaExtRef(final String noname) {
		this.csaExtRef = noname;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "costAccount")
	public List<AppUser> getUsers() {
		return this.users;
	}

	public void setUsers(final List<AppUser> users) {
		this.users = users;
	}

	public AppUser addUser(final AppUser user) {
		getUsers().add(user);
		user.setCostAccount(this);
		return user;
	}

	public AppUser removeUser(final AppUser user) {
		getUsers().remove(user);
		user.setCostAccount(null);
		return user;
	}


}
