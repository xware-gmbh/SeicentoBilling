package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.AddressDAO;

/**
 * Address
 */
@DAO(daoClass = AddressDAO.class)
@Caption("{%adrCity}")
@Entity
@Table(name = "Address", schema = "dbo")
public class Address implements java.io.Serializable {

	private Long adrId;
	private short adrIndex;
	private Customer customer;
	private LovCrm.AddressType adrType;
	private String adrLine0;
	private String adrLine1;
	private String adrZip;
	private String adrCity;
	private Date adrValidFrom;
	private String adrRegion;
	private String adrCountry;
	private String adrRemark;
	private LovState.State adrState;
	private List<Project> projects = new ArrayList<>();
	private String shortname;
	private String adrName;
	private String adrAddOn;
	private LovCrm.Salutation adrSalutation;


	public Address() {
	}

	@Caption("adrId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "adrId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getAdrId() {
		return this.adrId;
	}

	public void setAdrId(final Long adrId) {
		this.adrId = adrId;
	}

	@Caption("Date")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "adrValidFrom", nullable = false, columnDefinition = "datetime", length = 23)
	public Date getAdrValidFrom() {
		return this.adrValidFrom;
	}

	public void setAdrValidFrom(final Date adrValidFrom) {
		this.adrValidFrom = adrValidFrom;
	}

	@Caption("Index")
	@Column(name = "adrIndex", columnDefinition = "smallint")
	public short getAdrIndex() {
		return this.adrIndex;
	}

	public void setAdrIndex(final short adrIndex) {
		this.adrIndex = adrIndex;
	}

	@Caption("Customer")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "adrcusId", columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}


	@Caption("Type")
	@Column(name = "adrType", columnDefinition = "smallint")
	public LovCrm.AddressType getAdrType() {
		return this.adrType;
	}

	public void setAdrType(final LovCrm.AddressType adrType) {
		this.adrType = adrType;
	}

	@Caption("Line0")
	@Column(name = "adrLine0", columnDefinition = "nvarchar", length = 50)
	public String getAdrLine0() {
		return this.adrLine0;
	}

	public void setAdrLine0(final String adrLine0) {
		this.adrLine0 = adrLine0;
	}

	@Caption("Line1")
	@Column(name = "adrLine1", columnDefinition = "nvarchar", length = 50)
	public String getAdrLine1() {
		return this.adrLine1;
	}

	public void setAdrLine1(final String adrLine1) {
		this.adrLine1 = adrLine1;
	}

	@Caption("PLZ Zusatz")
	@Column(name = "adrZip", columnDefinition = "nvarchar", length = 50)
	public String getAdrZip() {
		return this.adrZip;
	}

	public void setAdrZip(final String adrZip) {
		this.adrZip = adrZip;
	}

	@Caption("Ort Zusatz")
	@Column(name = "adrCity", columnDefinition = "nvarchar", length = 50)
	public String getAdrCity() {
		return this.adrCity;
	}

	public void setAdrCity(final String adrCity) {
		this.adrCity = adrCity;
	}

	@Caption("Land Zusatz")
	@Column(name = "adrCountry", columnDefinition = "nvarchar", length = 50)
	public String getAdrCountry() {
		return this.adrCountry;
	}

	public void setAdrCountry(final String adrCountry) {
		this.adrCountry = adrCountry;
	}

	@Caption("Region")
	@Column(name = "adrRegion", columnDefinition = "nvarchar", length = 50)
	public String getAdrRegion() {
		return this.adrRegion;
	}

	public void setAdrRegion(final String adrRegion) {
		this.adrRegion = adrRegion;
	}

	@Caption("Remark")
	@Column(name = "adrRemark", columnDefinition = "nvarchar", length = 50)
	public String getAdrRemark() {
		return this.adrRemark;
	}

	public void setAdrRemark(final String adrRemark) {
		this.adrRemark = adrRemark;
	}

	@Caption("Status")
	@Column(name = "adrState", columnDefinition = "smallint")
	public LovState.State getAdrState() {
		return this.adrState;
	}

	public void setAdrState(final LovState.State adrState) {
		this.adrState = adrState;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "address")
	public List<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(final List<Project> projects) {
		this.projects = projects;
	}

	public Project addProject(final Project project) {
		getProjects().add(project);
		project.setAddress(this);
		return project;
	}

	public Project removeProject(final Project project) {
		getProjects().remove(project);
		project.setAddress(null);
		return project;
	}

	@Caption("KurzName Zusatz")
	@Column(name = "SHORTNAME", insertable = false, updatable = false)
	@Transient
	public String getShortname() {
		final StringBuffer sb = new StringBuffer("");
		if (this.adrName != null && this.adrName.length() > 0) {
			sb.append(this.adrName).append(" ");
		}
		if (this.adrAddOn != null && this.adrAddOn.length() > 0) {
			sb.append(this.adrAddOn).append(" ");
		}
		if (this.adrLine0 != null && this.adrLine0.length() > 0) {
			sb.append(this.adrLine0).append(" ");
		}
		if (this.adrLine1 != null && this.adrLine1.length() > 0) {
			sb.append(this.adrLine1).append(" ");
		}
		if (this.adrZip != null && this.adrZip.length() > 0) {
			sb.append(" ").append(this.adrZip);
		}
		if (this.adrCity != null && this.adrCity.length() > 0) {
			sb.append(" ").append(this.adrCity);
		}
		this.shortname = sb.toString();
		return this.shortname;
	}

	public void setShortname(final String noname) {
		this.shortname = noname;
	}

	@Caption("Name Zusatz")
	@Column(name = "adrName", length = 50, columnDefinition = "nvarchar")
	public String getAdrName() {
		return this.adrName;
	}

	public void setAdrName(final String noname) {
		this.adrName = noname;
	}

	@Column(name = "adrAddOn", length = 50, columnDefinition = "nvarchar")
	public String getAdrAddOn() {
		return this.adrAddOn;
	}

	public void setAdrAddOn(final String noname) {
		this.adrAddOn = noname;
	}

	@Column(name = "adrSalutation", columnDefinition = "smallint")
	public LovCrm.Salutation getAdrSalutation() {
		return this.adrSalutation;
	}

	public void setAdrSalutation(final LovCrm.Salutation noname) {
		this.adrSalutation = noname;
	}

}
