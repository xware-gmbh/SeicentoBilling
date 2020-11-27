package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.dal.ConversionDAO;

/**
 * Conversion
 */
@DAO(ConversionDAO.class)
@Caption("{%cnvRemark}")
@Entity
@Table(name = "Conversion", schema = "dbo")
public class Conversion implements java.io.Serializable {

	private Long cnvId;
	private String cnvGroup;
	private String cnvSubGroup;
	private Short cnvState;
	private String cnvValueIn;
	private String cnvValueOut;
	private String cnvRemark;
	private Short cnvDataType;

	public Conversion() {
	}

	@Caption("cnvId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "cnvId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getcnvId() {
		return this.cnvId;
	}

	public void setcnvId(final Long cnvId) {
		this.cnvId = cnvId;
	}


	@Caption("Gruppe")
	@Column(name = "cnvGroup", columnDefinition = "nvarchar", length = 40)
	public String getCnvGroup() {
		return this.cnvGroup;
	}

	public void setcnvGroup(final String cnvGroup) {
		this.cnvGroup = cnvGroup;
	}

	@Caption("Untergruppe")
	@Column(name = "cnvSubGroup", columnDefinition = "nvarchar", length = 40)
	public String getCnvSubGroup() {
		return this.cnvSubGroup;
	}

	public void setCnvSubGroup(final String cnvSubGroup) {
		this.cnvSubGroup = cnvSubGroup;
	}


	@Caption("Status")
	@Column(name = "cnvState", columnDefinition = "smallint")
	public Short getcnvState() {
		return this.cnvState;
	}

	public void setcnvState(final Short cnvState) {
		this.cnvState = cnvState;
	}

	@Caption("Startwert")
	@Column(name = "cnvValueIn", columnDefinition = "nvarchar", length = 80)
	public String getCnvValueIn() {
		return this.cnvValueIn;
	}

	public void setCnvValueIn(final String noname) {
		this.cnvValueIn = noname;
	}

	@Caption("Zielwert")
	@Column(name = "cnvValueOut", columnDefinition = "nvarchar", length = 80)
	public String getCnvValueOut() {
		return this.cnvValueOut;
	}

	public void setCnvValueOut(final String noname) {
		this.cnvValueOut = noname;
	}

	@Caption("Bemerkung")
	@Column(name = "cnvRemark", columnDefinition = "nvarchar", length = 80)
	public String getCnvRemark() {
		return this.cnvRemark;
	}

	public void setCnvRemark(final String noname) {
		this.cnvRemark = noname;
	}

	@Caption("Datentyp")
	@Column(name = "cnvDataType", columnDefinition = "smallint")
	public Short getCnvDataType() {
		return this.cnvDataType;
	}

	public void setCnvDataType(final Short noname) {
		this.cnvDataType = noname;
	}


}
