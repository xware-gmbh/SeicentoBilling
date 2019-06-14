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
import ch.xwr.seicentobilling.dal.CityDAO;

/**
 * City
 */
@DAO(daoClass = CityDAO.class)
@Caption("{%ctyName}")
@Entity
@Table(name = "City", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"ctyZIP", "ctyCountry" }))
public class City implements java.io.Serializable {

	private Long ctyId;
	private String ctyName;
	private String ctyCountry;
	private String ctyRegion;
	private String ctyGeoCoordinates;
	private Integer ctyZip;
	private LovState.State ctyState;
	private Set<Bank> banks = new HashSet<>(0);
	private Set<Customer> customers = new HashSet<>(0);
	private String fullName;

	public City() {
	}

	@Caption("CtyId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "ctyId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCtyId() {
		return this.ctyId;
	}

	public void setCtyId(final Long ctyId) {
		this.ctyId = ctyId;
	}

	@Caption("Name Ort")
	@Column(name = "ctyName", nullable = false, columnDefinition = "nvarchar")
	public String getCtyName() {
		return this.ctyName;
	}

	public void setCtyName(final String ctyName) {
		this.ctyName = ctyName;
	}

	@Caption("Land")
	@Column(name = "ctyCountry", columnDefinition = "nvarchar")
	public String getCtyCountry() {
		return this.ctyCountry;
	}

	public void setCtyCountry(final String ctyCountry) {
		this.ctyCountry = ctyCountry;
	}

	@Caption("Region")
	@Column(name = "ctyRegion", columnDefinition = "nvarchar")
	public String getCtyRegion() {
		return this.ctyRegion;
	}

	public void setCtyRegion(final String ctyRegion) {
		this.ctyRegion = ctyRegion;
	}

	@Caption("GeoCoordinates")
	@Column(name = "ctyGeoCoordinates", columnDefinition = "nvarchar")
	public String getCtyGeoCoordinates() {
		return this.ctyGeoCoordinates;
	}

	public void setCtyGeoCoordinates(final String ctyGeoCoordinates) {
		this.ctyGeoCoordinates = ctyGeoCoordinates;
	}

	@Caption("Plz")
	@Column(name = "ctyZIP", columnDefinition = "int")
	public Integer getCtyZip() {
		return this.ctyZip;
	}

	public void setCtyZip(final Integer ctyZip) {
		this.ctyZip = ctyZip;
	}

	@Caption("State")
	@Column(name = "ctyState", columnDefinition = "smallint")
	public LovState.State getCtyState() {
		return this.ctyState;
	}

	public void setCtyState(final LovState.State ctyState) {
		this.ctyState = ctyState;
	}

	@Caption("Banks")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "city")
	public Set<Bank> getBanks() {
		return this.banks;
	}

	public void setBanks(final Set<Bank> banks) {
		this.banks = banks;
	}

	@Caption("Customers")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "city")
	public Set<Customer> getCustomers() {
		return this.customers;
	}

	public void setCustomers(final Set<Customer> customers) {
		this.customers = customers;
	}

	@Column(name = "FULLNAME", insertable = false, updatable = false)
	@Transient
	public String getfullname() {
		//String shortname = "";
		final StringBuffer sb = new StringBuffer("");
		sb.append(this.ctyName).append(", ");

		if (this.ctyCountry != null && this.ctyCountry.length() > 1) {
			sb.append(this.ctyCountry).append("-");
		}
		sb.append(this.ctyZip);

		this.fullName = sb.toString();
		return this.fullName;
	}

	public void setFullname(final String noname) {
		this.fullName = noname;
	}

}
