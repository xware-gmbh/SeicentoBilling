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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.LabelDefinitionDAO;

/**
 * Label
 */
@DAO(LabelDefinitionDAO.class)
@Caption("{%cldText}")
@Entity
@Table(name = "LabelDefinition", schema = "dbo")
public class LabelDefinition implements java.io.Serializable {

	private Long cldId;
	private String cldText;
	private LovCrm.LabelType cldType;
	private LovState.State cldState;
	//private Set<LabelAssignment> labelassignments = new HashSet<>(0);
	private Set<Customer> customers = new HashSet<>();

	public LabelDefinition() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "cldId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCldId() {
		return this.cldId;
	}

	public void setCldId(final Long cldId) {
		this.cldId = cldId;
	}

	@Caption("Labeltext")
	@Column(name = "cldText", columnDefinition = "nvarchar")
	public String getCldText() {
		return this.cldText;
	}

	public void setCldText(final String cldText) {
		this.cldText = cldText;
	}


	@Caption("State")
	@Column(name = "cldState", columnDefinition = "smallint")
	public LovState.State getCldState() {
		return this.cldState;
	}

	public void setCldState(final LovState.State cldState) {
		this.cldState = cldState;
	}

	@Caption("Typ")
	@Column(name = "cldType", columnDefinition = "smallint")
	public LovCrm.LabelType getCldType() {
		return this.cldType;
	}

	public void setCldType(final LovCrm.LabelType cldType) {
		this.cldType = cldType;
	}

//	@Caption("LabelAssignements")
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "labelDefinition")
//	public Set<LabelAssignment> getLabelAssignments() {
//		return this.labelassignments;
//	}
//
//	public void setLabelAssignments(final Set<LabelAssignment> labelassignments) {
//		this.labelassignments  = labelassignments;
//	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "labelassignment", schema = "dbo", joinColumns = @JoinColumn(name = "clacldId", referencedColumnName = "cldId", nullable = false, updatable = false, columnDefinition = "bigint identity"), inverseJoinColumns = @JoinColumn(name = "clacusId", referencedColumnName = "cusId", nullable = false, updatable = false, columnDefinition = "bigint identity"))
	public Set<Customer> getCustomers() {
		return this.customers;
	}

	public void setCustomers(final Set<Customer> customers) {
		this.customers = customers;
	}

}
