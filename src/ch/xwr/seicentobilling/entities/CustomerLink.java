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

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;

/**
 * CustomerLink
 */
@DAO(daoClass = CustomerLinkDAO.class)
@Caption("{%cnkRemark}")
@Entity
@Table(name = "CustomerLink", schema = "dbo")
public class CustomerLink implements java.io.Serializable {

	private Long cnkId;
	private Customer customer;
	private short cnkIndex;
	private LovCrm.LinkType cnkType;
	private LovCrm.Department cnkDepartment;
	private String cnkLink;
	private String cnkRemark;
	private Date cnkValidFrom;
	private LovState.State cnkState;


	public CustomerLink() {
	}

	@Caption("cnkId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "cnkId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCnkId() {
		return this.cnkId;
	}

	public void setCnkId(final Long cnkId) {
		this.cnkId = cnkId;
	}

	@Caption("ValidFrom")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cnkValidFrom", nullable = false, columnDefinition = "datetime", length = 23)
	public Date getCnkValidFrom() {
		return this.cnkValidFrom;
	}

	public void setCnkValidFrom(final Date cnkValidFrom) {
		this.cnkValidFrom = cnkValidFrom;
	}

	@Caption("Type")
	@Column(name = "cnkType", columnDefinition = "smallint")
	public LovCrm.LinkType getCnkType() {
		return this.cnkType;
	}

	public void setCnkType(final LovCrm.LinkType cnkType) {
		this.cnkType = cnkType;
	}

	@Caption("Customer")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cnkcusId", columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}


	@Caption("Link")
	@Column(name = "cnkLink", columnDefinition = "nvarchar", length = 256)
	public String getCnkLink() {
		return this.cnkLink;
	}

	public void setCnkLink(final String cnkLink) {
		this.cnkLink = cnkLink;
	}

	@Caption("Remark")
	@Column(name = "cnkRemark", columnDefinition = "nvarchar", length = 50)
	public String getCnkRemark() {
		return this.cnkRemark;
	}

	public void setCnkRemark(final String cnkRemark) {
		this.cnkRemark = cnkRemark;
	}

	@Caption("Index")
	@Column(name = "cnkIndex", columnDefinition = "smallint")
	public short getCnkIndex() {
		return this.cnkIndex;
	}

	public void setCnkIndex(final short cnkIndex) {
		this.cnkIndex = cnkIndex;
	}


	@Caption("State")
	@Column(name = "cnkState", columnDefinition = "smallint")
	public LovState.State getCnkState() {
		return this.cnkState;
	}

	public void setCnkState(final LovState.State cnkState) {
		this.cnkState = cnkState;
	}

	@Caption("Department")
	@Column(name = "cnkDepartment", columnDefinition = "smallint")
	public LovCrm.Department getCnkDepartment() {
		return this.cnkDepartment;
	}

	public void setCnkDepartment(final LovCrm.Department cnkDepartment) {
		this.cnkDepartment = cnkDepartment;
	}

}
