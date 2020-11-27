package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;

import ch.xwr.seicentobilling.dal.EntityDAO;

/**
 * Entity
 */
@DAO(EntityDAO.class)
@javax.persistence.Entity
@Table(name = "Entity", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "entName"))
public class Entity implements java.io.Serializable {

	private Long entId;
	private String entName;
	private String entAbbreviation;
	private String entDataclass;
	private Boolean entHasrowobject;
	private Boolean entReadonly;
	private Boolean entExport2sdf;
	private Short entSdfOrdinal;
	private Short entAuditHistory;
	private Integer entType;
	private Short entState;
	private Set<StateCode> stateCodes = new HashSet<>(0);
	private Set<RowObject> rowObjects = new HashSet<>(0);

	public Entity() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "entId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getEntId() {
		return this.entId;
	}

	public void setEntId(final Long entId) {
		this.entId = entId;
	}

	@Column(name = "entName", unique = true, columnDefinition = "nvarchar")
	public String getEntName() {
		return this.entName;
	}

	public void setEntName(final String entName) {
		this.entName = entName;
	}

	@Column(name = "entAbbreviation", columnDefinition = "nvarchar")
	public String getEntAbbreviation() {
		return this.entAbbreviation;
	}

	public void setEntAbbreviation(final String entAbbreviation) {
		this.entAbbreviation = entAbbreviation;
	}

	@Column(name = "entDataclass", columnDefinition = "nvarchar")
	public String getEntDataclass() {
		return this.entDataclass;
	}

	public void setEntDataclass(final String entDataclass) {
		this.entDataclass = entDataclass;
	}

	@Column(name = "entHasrowobject", columnDefinition = "bit")
	public Boolean getEntHasrowobject() {
		return this.entHasrowobject;
	}

	public void setEntHasrowobject(final Boolean entHasrowobject) {
		this.entHasrowobject = entHasrowobject;
	}

	@Column(name = "entReadonly", columnDefinition = "bit")
	public Boolean getEntReadonly() {
		return this.entReadonly;
	}

	public void setEntReadonly(final Boolean entReadonly) {
		this.entReadonly = entReadonly;
	}

	@Column(name = "entExport2sdf", columnDefinition = "bit")
	public Boolean getEntExport2sdf() {
		return this.entExport2sdf;
	}

	public void setEntExport2sdf(final Boolean entExport2sdf) {
		this.entExport2sdf = entExport2sdf;
	}

	@Column(name = "entSdfOrdinal", columnDefinition = "smallint")
	public Short getEntSdfOrdinal() {
		return this.entSdfOrdinal;
	}

	public void setEntSdfOrdinal(final Short entSdfOrdinal) {
		this.entSdfOrdinal = entSdfOrdinal;
	}

	@Column(name = "entAuditHistory", columnDefinition = "smallint")
	public Short getEntAuditHistory() {
		return this.entAuditHistory;
	}

	public void setEntAuditHistory(final Short entAuditHistory) {
		this.entAuditHistory = entAuditHistory;
	}

	@Column(name = "entType", columnDefinition = "int")
	public Integer getEntType() {
		return this.entType;
	}

	public void setEntType(final Integer entType) {
		this.entType = entType;
	}

	@Column(name = "entState", columnDefinition = "smallint")
	public Short getEntState() {
		return this.entState;
	}

	public void setEntState(final Short entState) {
		this.entState = entState;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "entity")
	public Set<StateCode> getStateCodes() {
		return this.stateCodes;
	}

	public void setStateCodes(final Set<StateCode> stateCodes) {
		this.stateCodes = stateCodes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "entity")
	public Set<RowObject> getRowObjects() {
		return this.rowObjects;
	}

	public void setRowObjects(final Set<RowObject> rowObjects) {
		this.rowObjects = rowObjects;
	}

}
