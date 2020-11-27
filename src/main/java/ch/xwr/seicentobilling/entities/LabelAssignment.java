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

import ch.xwr.seicentobilling.dal.LabelAssignmentDAO;

/**
 * Label
 */
@DAO(LabelAssignmentDAO.class)
@Caption("{%claIndex}")
@Entity
@Table(name = "LabelAssignment", schema = "dbo")
public class LabelAssignment implements java.io.Serializable {

	private Long claId;
	private Long claIndex;
	private Customer customer;
	private LabelDefinition labeldefinition;

	public LabelAssignment() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "claId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getClaId() {
		return this.claId;
	}

	public void setClaId(final Long claId) {
		this.claId = claId;
	}

	@Caption("Customer")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "clacusId", columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}

	@Caption("LabelDefinition")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "clacldId", columnDefinition = "bigint")
	public LabelDefinition getLabelDefinition() {
		return this.labeldefinition;
	}

	public void setLabelDefinition(final LabelDefinition labeldefinition) {
		this.labeldefinition = labeldefinition;
	}

	@Caption("Index")
	@Column(name = "claIndex", columnDefinition = "smallint")
	public Long getClaIndex() {
		return this.claIndex;
	}

	public void setClaIndex(final Long claIndex) {
		this.claIndex = claIndex;
	}

}
