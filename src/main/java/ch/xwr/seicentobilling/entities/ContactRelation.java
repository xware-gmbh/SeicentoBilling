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

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.dal.ContactRelationDAO;

/**
 * Label
 */
@DAO(ContactRelationDAO.class)
@Caption("{%corRemark}")
@Entity
@Table(name = "ContactRelation", schema = "dbo")
public class ContactRelation implements java.io.Serializable {

	private Long corId;
	private LovCrm.ContactRelation corTypeOne;
	private LovCrm.ContactRelation corTypeTwo;
	private Customer customerOne;
	private Customer customerTwo;

	public ContactRelation() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "corId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCorId() {
		return this.corId;
	}

	public void setCorId(final Long corId) {
		this.corId = corId;
	}

	@Caption("RelationOne")
	@Column(name = "corTypeOne")
	public LovCrm.ContactRelation getCorTypeOne() {
		return this.corTypeOne;
	}

	public void setCorTypeOne(final LovCrm.ContactRelation noname) {
		this.corTypeOne = noname;
	}

	@Caption("RelationTwo")
	@Column(name = "corTypeTwo")
	public LovCrm.ContactRelation getCorTypeTwo() {
		return this.corTypeTwo;
	}

	public void setCorTypeTwo(final LovCrm.ContactRelation noname) {
		this.corTypeTwo = noname;
	}

	@Caption("CustomerOne")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "corcusIdTypeOne", columnDefinition = "bigint")

	public Customer getCustomerOne() {
		return this.customerOne;
	}

	public void setCustomerOne(final Customer customerOne) {
		this.customerOne = customerOne;
	}

	@Caption("CustomerTwo")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "corcusIdTypeTwo", columnDefinition = "bigint")
	public Customer getCustomerTwo() {
		return this.customerTwo;
	}

	public void setCustomerTwo(final Customer customerTwo) {
		this.customerTwo = customerTwo;
	}


}
