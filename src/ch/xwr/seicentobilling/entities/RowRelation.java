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

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.dal.RowRelationDAO;

/**
 * RowRelation
 */
@DAO(daoClass = RowRelationDAO.class)
@Caption("{%relName}")
@Entity
@Table(name = "RowRelation", schema = "dbo")
public class RowRelation implements java.io.Serializable {

	private Long relId;
	private RowObject rowObjectByRelobjIdTarget;
	private RowObject rowObjectByRelobjIdSource;
	private String relName;
	private Integer relOrder;
	private String relDescription;
	private Short relState;

	public RowRelation() {
	}

	@Caption("RelId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "relId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getRelId() {
		return this.relId;
	}

	public void setRelId(final Long relId) {
		this.relId = relId;
	}

	@Caption("RowObjectByRelobjIdTarget")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relobjId_Target", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObjectByRelobjIdTarget() {
		return this.rowObjectByRelobjIdTarget;
	}

	public void setRowObjectByRelobjIdTarget(final RowObject rowObjectByRelobjIdTarget) {
		this.rowObjectByRelobjIdTarget = rowObjectByRelobjIdTarget;
	}

	@Caption("RowObjectByRelobjIdSource")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relobjId_Source", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObjectByRelobjIdSource() {
		return this.rowObjectByRelobjIdSource;
	}

	public void setRowObjectByRelobjIdSource(final RowObject rowObjectByRelobjIdSource) {
		this.rowObjectByRelobjIdSource = rowObjectByRelobjIdSource;
	}

	@Caption("RelName")
	@Column(name = "relName", nullable = false, columnDefinition = "nvarchar")
	public String getRelName() {
		return this.relName;
	}

	public void setRelName(final String relName) {
		this.relName = relName;
	}

	@Caption("RelOrder")
	@Column(name = "relOrder", columnDefinition = "int")
	public Integer getRelOrder() {
		return this.relOrder;
	}

	public void setRelOrder(final Integer relOrder) {
		this.relOrder = relOrder;
	}

	@Caption("RelDescription")
	@Column(name = "relDescription", columnDefinition = "nvarchar")
	public String getRelDescription() {
		return this.relDescription;
	}

	public void setRelDescription(final String relDescription) {
		this.relDescription = relDescription;
	}

	@Caption("RelState")
	@Column(name = "relState", columnDefinition = "smallint")
	public Short getRelState() {
		return this.relState;
	}

	public void setRelState(final Short relState) {
		this.relState = relState;
	}

}
