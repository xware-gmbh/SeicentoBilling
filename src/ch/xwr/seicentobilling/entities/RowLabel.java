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

import ch.xwr.seicentobilling.dal.RowLabelDAO;

/**
 * RowLabel
 */
@DAO(daoClass = RowLabelDAO.class)
@Caption("{%lblLabelShort}")
@Entity
@Table(name = "RowLabel", schema = "dbo")
public class RowLabel implements java.io.Serializable {

	private Long lblId;
	private Language language;
	private RowObject rowObject;
	private String lblLabelShort;
	private String lblLabelLong;
	private Short lblState;

	public RowLabel() {
	}

	@Caption("LblId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "lblId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getLblId() {
		return this.lblId;
	}

	public void setLblId(final Long lblId) {
		this.lblId = lblId;
	}

	@Caption("Language")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "lbllngId", nullable = false, columnDefinition = "bigint")
	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(final Language language) {
		this.language = language;
	}

	@Caption("RowObject")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "lblobjId", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObject() {
		return this.rowObject;
	}

	public void setRowObject(final RowObject rowObject) {
		this.rowObject = rowObject;
	}

	@Caption("LblLabelShort")
	@Column(name = "lblLabelShort", columnDefinition = "nvarchar")
	public String getLblLabelShort() {
		return this.lblLabelShort;
	}

	public void setLblLabelShort(final String lblLabelShort) {
		this.lblLabelShort = lblLabelShort;
	}

	@Caption("LblLabelLong")
	@Column(name = "lblLabelLong", columnDefinition = "nvarchar")
	public String getLblLabelLong() {
		return this.lblLabelLong;
	}

	public void setLblLabelLong(final String lblLabelLong) {
		this.lblLabelLong = lblLabelLong;
	}

	@Caption("LblState")
	@Column(name = "lblState", columnDefinition = "smallint")
	public Short getLblState() {
		return this.lblState;
	}

	public void setLblState(final Short lblState) {
		this.lblState = lblState;
	}

}
