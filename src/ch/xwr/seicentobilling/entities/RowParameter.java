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
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.RowParameterDAO;

/**
 * RowParameter
 */
@DAO(daoClass = RowParameterDAO.class)
@Caption("{%prmGroup}")
@Entity
@Table(name = "RowParameter", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"prmobjId", "prmGroup", "prmSubGroup", "prmKey" }))
public class RowParameter implements java.io.Serializable {

	private Long prmId;
	private RowObject rowObject;
	private String prmGroup;
	private String prmSubGroup;
	private String prmKey;
	private String prmValue;
	private LovState.ValueType prmValueType;
	private LovState.State prmState;
	private Short prmParamType;
	private String prmLookupTable;
	private Boolean prmVisible;

	public RowParameter() {
	}

	@Caption("PrmId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "prmId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPrmId() {
		return this.prmId;
	}

	public void setPrmId(final Long prmId) {
		this.prmId = prmId;
	}

	@Caption("RowObject")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prmobjId", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObject() {
		return this.rowObject;
	}

	public void setRowObject(final RowObject rowObject) {
		this.rowObject = rowObject;
	}

	@Caption("PrmGroup")
	@Column(name = "prmGroup", columnDefinition = "nvarchar")
	public String getPrmGroup() {
		return this.prmGroup;
	}

	public void setPrmGroup(final String prmGroup) {
		this.prmGroup = prmGroup;
	}

	@Caption("PrmSubGroup")
	@Column(name = "prmSubGroup", columnDefinition = "nvarchar")
	public String getPrmSubGroup() {
		return this.prmSubGroup;
	}

	public void setPrmSubGroup(final String prmSubGroup) {
		this.prmSubGroup = prmSubGroup;
	}

	@Caption("PrmKey")
	@Column(name = "prmKey", columnDefinition = "nvarchar")
	public String getPrmKey() {
		return this.prmKey;
	}

	public void setPrmKey(final String prmKey) {
		this.prmKey = prmKey;
	}

	@Caption("PrmValue")
	@Column(name = "prmValue", columnDefinition = "nvarchar")
	public String getPrmValue() {
		return this.prmValue;
	}

	public void setPrmValue(final String prmValue) {
		this.prmValue = prmValue;
	}

	@Caption("PrmValueType")
	@Column(name = "prmValueType", columnDefinition = "smallint")
	public LovState.ValueType getPrmValueType() {
		return this.prmValueType;
	}

	public void setPrmValueType(final LovState.ValueType prmValueType) {
		this.prmValueType = prmValueType;
	}

	@Caption("PrmState")
	@Column(name = "prmState", columnDefinition = "smallint")
	public LovState.State getPrmState() {
		return this.prmState;
	}

	public void setPrmState(final LovState.State prmState) {
		this.prmState = prmState;
	}

	@Caption("PrmParamType")
	@Column(name = "prmParamType", columnDefinition = "smallint")
	public Short getPrmParamType() {
		return this.prmParamType;
	}

	public void setPrmParamType(final Short prmParamType) {
		this.prmParamType = prmParamType;
	}

	@Caption("PrmLookupTable")
	@Column(name = "prmLookupTable", columnDefinition = "nvarchar")
	public String getPrmLookupTable() {
		return this.prmLookupTable;
	}

	public void setPrmLookupTable(final String prmLookupTable) {
		this.prmLookupTable = prmLookupTable;
	}

	@Caption("PrmVisible")
	@Column(name = "prmVisible", columnDefinition = "bit")
	public Boolean getPrmVisible() {
		return this.prmVisible;
	}

	public void setPrmVisible(final Boolean prmVisible) {
		this.prmVisible = prmVisible;
	}

}
