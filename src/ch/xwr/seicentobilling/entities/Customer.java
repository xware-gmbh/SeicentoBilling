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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.CustomerDAO;

/**
 * Customer
 */
@DAO(daoClass = CustomerDAO.class)
@Caption("{%cusName}")
@Entity
@Table(name = "Customer", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "cusNumber"))
public class Customer implements java.io.Serializable {

	private Long cusId;
	private City city;
	private PaymentCondition paymentCondition;
	private int cusNumber;
	private String shortname;
	private String cusName;
	private String cusFirstName;
	private String cusCompany;
	private String cusAddress;
	private String cusInfo;
	private LovState.State cusState;
	private Date cusLastBill;  //deprecated
	private Set<Order> orders = new HashSet<>(0);
	private Set<Project> projects = new HashSet<>(0);
	private String cusAccountManager;
	private LovState.AccountType cusAccountType;
	private Set<Activity> activities = new HashSet<>(0);
	//private Set<LabelAssignment> labelassignments = new HashSet<>(0);
	private Set<Address> addresses = new HashSet<>(0);
	private Set<CustomerLink> customerlinks = new HashSet<>(0);
	private LovCrm.Salutation cusSalutation;
	private Date cusBirthdate;
	private LovCrm.BillTarget cusBillingTarget;
	private LovCrm.BillReport cusBillingReport;
	private Set<LabelDefinition> labelDefinitions = new HashSet<>();
	private Set<ContactRelation> contactrelations1 = new HashSet<>();
	private Set<ContactRelation> contactrelations2 = new HashSet<>();
	private String fullname;
	private Boolean cusSinglepdf;

	public Customer() {
	}

	@Caption("CusId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "cusId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCusId() {
		return this.cusId;
	}

	public void setCusId(final Long cusId) {
		this.cusId = cusId;
	}

	@Caption("City")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cusctyId", columnDefinition = "bigint")
	public City getCity() {
		return this.city;
	}

	public void setCity(final City city) {
		this.city = city;
	}

	@Caption("PaymentCondition")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cuspacId", nullable = false, columnDefinition = "bigint")
	public PaymentCondition getPaymentCondition() {
		return this.paymentCondition;
	}

	public void setPaymentCondition(final PaymentCondition paymentCondition) {
		this.paymentCondition = paymentCondition;
	}

	@Caption("Kundennummer")
	@Column(name = "cusNumber", unique = true, nullable = false, columnDefinition = "int")
	public int getCusNumber() {
		return this.cusNumber;
	}

	public void setCusNumber(final int cusNumber) {
		this.cusNumber = cusNumber;
	}

	@Caption("Name")
	@Column(name = "cusName", nullable = false, columnDefinition = "nvarchar")
	public String getCusName() {
		return this.cusName;
	}

	public void setCusName(final String cusName) {
		this.cusName = cusName;
	}

	@Caption("Vorname")
	@Column(name = "cusFirstName", columnDefinition = "nvarchar")
	public String getCusFirstName() {
		return this.cusFirstName;
	}

	public void setCusFirstName(final String cusFirstName) {
		this.cusFirstName = cusFirstName;
	}

	@Caption("Firmenname")
	@Column(name = "cusCompany", columnDefinition = "nvarchar")
	public String getCusCompany() {
		return this.cusCompany;
	}

	public void setCusCompany(final String cusCompany) {
		this.cusCompany = cusCompany;
	}

	@Caption("CusAddress")
	@Column(name = "cusAddress", columnDefinition = "nvarchar")
	public String getCusAddress() {
		return this.cusAddress;
	}

	public void setCusAddress(final String cusAddress) {
		this.cusAddress = cusAddress;
	}

	@Caption("CusInfo")
	@Lob
	@Column(name = "cusInfo", columnDefinition = "ntext")
	public String getCusInfo() {
		return this.cusInfo;
	}

	public void setCusInfo(final String cusInfo) {
		this.cusInfo = cusInfo;
	}

	@Caption("State")
	@Column(name = "cusState", columnDefinition = "smallint")
	public LovState.State getCusState() {
		return this.cusState;
	}

	public void setCusState(final LovState.State cusState) {
		this.cusState = cusState;
	}

	@Caption("CusLastBill")
	@Temporal(TemporalType.DATE)
	@Column(name = "cusLastBill", columnDefinition = "date", length = 10)
	public Date getCusLastBill() {
		return this.cusLastBill;
	}

	public void setCusLastBill(final Date cusLastBill) {
		this.cusLastBill = cusLastBill;
	}

	@Caption("Orders")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(final Set<Order> orders) {
		this.orders = orders;
	}

	@Caption("Projects")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(final Set<Project> projects) {
		this.projects = projects;
	}

	@Caption("Activities")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<Activity> getActivities() {
		return this.activities;
	}

	public void setActivities(final Set<Activity> activities) {
		this.activities = activities;
	}

//	@Caption("LabelAssignments")
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
//	public Set<LabelAssignment> getLabelAssignments() {
//		return this.labelassignments;
//	}
//
//	public void setLabelAssignments(final Set<LabelAssignment> label) {
//		this.labelassignments = label;
//	}

	@Caption("Adresses")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<Address> getAddresses() {
		return this.addresses;
	}

	public void setAddresses(final Set<Address> address) {
		this.addresses = address;
	}

	@Caption("CustomerLink")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	public Set<CustomerLink> getCustomerLinks() {
		return this.customerlinks;
	}

	public void setCustomerLinks(final Set<CustomerLink> customerlinks) {
		this.customerlinks = customerlinks;
	}

	@Column(name = "shortname", insertable = false, updatable = false)
	@Transient
	public String getShortname() {
		final StringBuffer sb = new StringBuffer("");

		if (getCusAccountType() == LovState.AccountType.juristisch) {
			if (this.cusCompany != null && this.cusCompany.length() > 0) {
				sb.append(this.cusCompany).append(" ");
			}

		}
		sb.append(this.cusName);
		if (this.cusFirstName != null && this.cusFirstName.length() > 0) {
			sb.append(" ").append(this.cusFirstName);
		}

		this.shortname = sb.toString();
		return this.shortname;
	}

	public void setShortname(final String noname) {
		this.shortname = noname;
	}

	@Caption("AccountManager")
	@Column(name = "cusAccountManager")
	public String getCusAccountManager() {
		return this.cusAccountManager;
	}

	public void setCusAccountManager(final String noname) {
		this.cusAccountManager = noname;
	}

	@Caption("AccountType")
	@Column(name = "cusAccountType", columnDefinition = "smallint")
	public LovState.AccountType getCusAccountType() {
		return this.cusAccountType;
	}

	public void setCusAccountType(final LovState.AccountType atype) {
		this.cusAccountType = atype;
	}

	@Caption("Anrede")
	@Column(name = "cusSalutation")
	public LovCrm.Salutation getCusSalutation() {
		return this.cusSalutation;
	}

	public void setCusSalutation(final LovCrm.Salutation cusSalutation) {
		this.cusSalutation = cusSalutation;
	}

	@Caption("Birhtdate")
	@Temporal(TemporalType.DATE)
	@Column(name = "cusBirthdate", columnDefinition = "date", length = 10)
	public Date getCusBirthdate() {
		return this.cusBirthdate;
	}

	public void setCusBirthdate(final Date cusBirthdate) {
		this.cusBirthdate = cusBirthdate;
	}

	@Caption("BillTarget")
	@Column(name = "cusBillingTarget")
	public LovCrm.BillTarget getCusBillingTarget() {
		return this.cusBillingTarget;
	}

	public void setCusBillingTarget(final LovCrm.BillTarget cusBillTarget) {
		this.cusBillingTarget = cusBillTarget;
	}

	@Caption("BillReport")
	@Column(name = "cusBillingReport")
	public LovCrm.BillReport getCusBillingReport() {
		return this.cusBillingReport;
	}

	public void setCusBillingReport(final LovCrm.BillReport cusBillReport) {
		this.cusBillingReport = cusBillReport;
	}

	@Caption("Labels")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "labelassignment", schema = "dbo",
		joinColumns = @JoinColumn(name = "clacusId", referencedColumnName = "cusId", nullable = false, updatable = false, columnDefinition = "bigint identity"),
		inverseJoinColumns = @JoinColumn(name = "clacldId", referencedColumnName = "cldId", nullable = false, updatable = false, columnDefinition = "bigint identity"))
	public Set<LabelDefinition> getLabelDefinitions() {
		return this.labelDefinitions;
	}

	public void setLabelDefinitions(final Set<LabelDefinition> labelDefinitions) {
		this.labelDefinitions = labelDefinitions;
	}


	@Caption("ContactRelatonOne")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customerOne")
	public Set<ContactRelation> getContactRelations1() {
		return this.contactrelations1;
	}

	public void setContactRelations1(final Set<ContactRelation> contactrelations1) {
		this.contactrelations1 = contactrelations1;
	}

	@Caption("ContactRelatonTwo")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customerTwo")
	public Set<ContactRelation> getContactRelations2() {
		return this.contactrelations2;
	}

	public void setContactRelations2(final Set<ContactRelation> contactrelations2) {
		this.contactrelations2 = contactrelations2;
	}

	@Column(name = "fullname", insertable = false, updatable = false, unique = false)
	@Transient
	public String getFullname() {
		final StringBuffer sb = new StringBuffer("");

		if (getCusAccountType() == LovState.AccountType.juristisch) {
			if (this.cusCompany != null && this.cusCompany.length() > 0) {
				sb.append(this.cusCompany).append(" ");
			}

		}
		sb.append(this.cusName);
		if (this.cusFirstName != null && this.cusFirstName.length() > 0) {
			sb.append(" ").append(this.cusFirstName);
		}

		sb.append(" #").append(this.cusNumber).append(", ").append(this.getCity().getCtyName());
		this.fullname = sb.toString();
		return this.fullname;
	}

	public void setFullname(final String noname) {
		this.fullname = noname;
	}

	@Caption("SinglePdf")
	@Column(name = "cusSinglePdf", columnDefinition = "bit")
	public Boolean getCusSinglepdf() {
		return this.cusSinglepdf;
	}

	public void setCusSinglepdf(final Boolean singlepdf) {
		this.cusSinglepdf = singlepdf;
	}

}
