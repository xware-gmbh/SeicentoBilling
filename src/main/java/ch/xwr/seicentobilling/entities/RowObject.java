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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.RowObjectDAO;

/**
 * RowObject
 */
@DAO(RowObjectDAO.class)
@Caption("{%objAddedBy}")
@Entity
@Table(name = "RowObject", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"objentId", "objRowId" }))
public class RowObject implements java.io.Serializable {

	private Long objId;
	private ch.xwr.seicentobilling.entities.Entity entity;
	private long objRowId;
	private Long objChngcnt;
	private LovState.State objState;
	private Date objAdded;
	private String objAddedBy;
	private Date objChanged;
	private String objChangedBy;
	private Date objDeleted;
	private String objDeletedBy;
	private Set<RowLabel> rowLabels = new HashSet<>(0);
	private Set<RowRelation> rowRelationsForRelobjIdTarget = new HashSet<>(0);
	private Set<RowImage> rowImages = new HashSet<>(0);
	private Set<RowParameter> rowParameters = new HashSet<>(0);
	private Set<RowText> rowTexts = new HashSet<>(0);
	private Set<RowRelation> rowRelationsForRelobjIdSource = new HashSet<>(0);

	public RowObject() {
	}

	@Caption("ObjId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "objId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getObjId() {
		return this.objId;
	}

	public void setObjId(final Long objId) {
		this.objId = objId;
	}

	@Caption("Entity")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "objentId", nullable = false, columnDefinition = "bigint")
	public ch.xwr.seicentobilling.entities.Entity getEntity() {
		return this.entity;
	}

	public void setEntity(final ch.xwr.seicentobilling.entities.Entity entity) {
		this.entity = entity;
	}

	@Caption("ObjRowId")
	@Column(name = "objRowId", nullable = false, columnDefinition = "bigint")
	public long getObjRowId() {
		return this.objRowId;
	}

	public void setObjRowId(final long objRowId) {
		this.objRowId = objRowId;
	}

	@Caption("ObjChngcnt")
	@Column(name = "objChngcnt", columnDefinition = "bigint")
	public Long getObjChngcnt() {
		return this.objChngcnt;
	}

	public void setObjChngcnt(final Long objChngcnt) {
		this.objChngcnt = objChngcnt;
	}

	@Caption("ObjState")
	@Column(name = "objState", columnDefinition = "smallint")
	public LovState.State getObjState() {
		return this.objState;
	}

	public void setObjState(final LovState.State objState) {
		this.objState = objState;
	}

	@Caption("ObjAdded")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "objAdded", columnDefinition = "datetime", length = 23)
	public Date getObjAdded() {
		return this.objAdded;
	}

	public void setObjAdded(final Date objAdded) {
		this.objAdded = objAdded;
	}

	@Caption("ObjAddedBy")
	@Column(name = "objAddedBy", columnDefinition = "nvarchar")
	public String getObjAddedBy() {
		return this.objAddedBy;
	}

	public void setObjAddedBy(final String objAddedBy) {
		this.objAddedBy = objAddedBy;
	}

	@Caption("ObjChanged")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "objChanged", columnDefinition = "datetime", length = 23)
	public Date getObjChanged() {
		return this.objChanged;
	}

	public void setObjChanged(final Date objChanged) {
		this.objChanged = objChanged;
	}

	@Caption("ObjChangedBy")
	@Column(name = "objChangedBy", columnDefinition = "nvarchar")
	public String getObjChangedBy() {
		return this.objChangedBy;
	}

	public void setObjChangedBy(final String objChangedBy) {
		this.objChangedBy = objChangedBy;
	}

	@Caption("ObjDeleted")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "objDeleted", columnDefinition = "datetime", length = 23)
	public Date getObjDeleted() {
		return this.objDeleted;
	}

	public void setObjDeleted(final Date objDeleted) {
		this.objDeleted = objDeleted;
	}

	@Caption("ObjDeletedBy")
	@Column(name = "objDeletedBy", columnDefinition = "nvarchar")
	public String getObjDeletedBy() {
		return this.objDeletedBy;
	}

	public void setObjDeletedBy(final String objDeletedBy) {
		this.objDeletedBy = objDeletedBy;
	}

	@Caption("RowLabels")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObject")
	public Set<RowLabel> getRowLabels() {
		return this.rowLabels;
	}

	public void setRowLabels(final Set<RowLabel> rowLabels) {
		this.rowLabels = rowLabels;
	}


	@Caption("RowRelationsForRelobjIdTarget")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObjectByRelobjIdTarget")
	public Set<RowRelation> getRowRelationsForRelobjIdTarget() {
		return this.rowRelationsForRelobjIdTarget;
	}

	public void setRowRelationsForRelobjIdTarget(final Set<RowRelation> rowRelationsForRelobjIdTarget) {
		this.rowRelationsForRelobjIdTarget = rowRelationsForRelobjIdTarget;
	}

	@Caption("RowImages")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObject")
	public Set<RowImage> getRowImages() {
		return this.rowImages;
	}

	public void setRowImages(final Set<RowImage> rowImages) {
		this.rowImages = rowImages;
	}

	@Caption("RowParameters")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObject")
	public Set<RowParameter> getRowParameters() {
		return this.rowParameters;
	}

	public void setRowParameters(final Set<RowParameter> rowParameters) {
		this.rowParameters = rowParameters;
	}

	@Caption("RowTexts")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObject")
	public Set<RowText> getRowTexts() {
		return this.rowTexts;
	}

	public void setRowTexts(final Set<RowText> rowTexts) {
		this.rowTexts = rowTexts;
	}

	@Caption("RowRelationsForRelobjIdSource")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rowObjectByRelobjIdSource")
	public Set<RowRelation> getRowRelationsForRelobjIdSource() {
		return this.rowRelationsForRelobjIdSource;
	}

	public void setRowRelationsForRelobjIdSource(final Set<RowRelation> rowRelationsForRelobjIdSource) {
		this.rowRelationsForRelobjIdSource = rowRelationsForRelobjIdSource;
	}

}
