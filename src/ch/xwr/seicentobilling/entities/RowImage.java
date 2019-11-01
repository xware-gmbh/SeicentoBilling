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
import ch.xwr.seicentobilling.dal.RowImageDAO;

/**
 * RowImage
 */
@DAO(daoClass = RowImageDAO.class)
@Caption("{%rimName}")
@Entity
@Table(name = "RowImage", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"rimobjId", "rimType", "rimNumber" }))
public class RowImage implements java.io.Serializable {

	private Long rimId;
	private RowObject rowObject;
	private String rimName;
	private byte[] rimIcon;
	private byte[] rimImage;
	private LovState.State rimState;
	private String rimMimetype;
	private int rimNumber;
	private short rimType;
	private String rimSize;

	public RowImage() {
	}

	@Caption("RimId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "rimId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getRimId() {
		return this.rimId;
	}

	public void setRimId(final Long rimId) {
		this.rimId = rimId;
	}

	@Caption("RowObject")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rimobjId", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObject() {
		return this.rowObject;
	}

	public void setRowObject(final RowObject rowObject) {
		this.rowObject = rowObject;
	}

	@Caption("RimName")
	@Column(name = "rimName", columnDefinition = "nvarchar")
	public String getRimName() {
		return this.rimName;
	}

	public void setRimName(final String rimName) {
		this.rimName = rimName;
	}

	@Caption("RimIcon")
	@Column(name = "rimIcon", columnDefinition = "image")
	public byte[] getRimIcon() {
		return this.rimIcon;
	}

	public void setRimIcon(final byte[] rimIcon) {
		this.rimIcon = rimIcon;
	}

	@Caption("RimImage")
	@Column(name = "rimImage", columnDefinition = "image")
	public byte[] getRimImage() {
		return this.rimImage;
	}

	public void setRimImage(final byte[] rimImage) {
		this.rimImage = rimImage;
	}

	@Caption("RimState")
	@Column(name = "rimState", columnDefinition = "smallint")
	public LovState.State getRimState() {
		return this.rimState;
	}

	public void setRimState(final LovState.State active) {
		this.rimState = active;
	}

	@Caption("RimMimetype")
	@Column(name = "rimMimetype", columnDefinition = "nvarchar")
	public String getRimMimetype() {
		return this.rimMimetype;
	}

	public void setRimMimetype(final String rimMimetype) {
		this.rimMimetype = rimMimetype;
	}

	@Caption("RimNumber")
	@Column(name = "rimNumber", nullable = false, columnDefinition = "int")
	public int getRimNumber() {
		return this.rimNumber;
	}

	public void setRimNumber(final int rimNumber) {
		this.rimNumber = rimNumber;
	}

	@Caption("RimType")
	@Column(name = "rimType", nullable = false, columnDefinition = "smallint")
	public short getRimType() {
		return this.rimType;
	}

	public void setRimType(final short rimType) {
		this.rimType = rimType;
	}

	@Caption("RimSize")
	@Column(name = "rimSize", columnDefinition = "nvarchar")
	public String getRimSize() {
		return this.rimSize;
	}

	public void setRimSize(String rimSize) {
	    if(rimSize.length() > 9) {
			rimSize = rimSize.substring(0,9);
		}

		this.rimSize = rimSize;
	}

}
