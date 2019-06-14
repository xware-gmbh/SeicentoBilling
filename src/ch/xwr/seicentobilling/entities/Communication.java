package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.dal.CommunicationDAO;

/**
 * Communication
 */
@DAO(daoClass = CommunicationDAO.class)
@Caption("{%comValue}")
@Entity
@Table(name = "Communication", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"comNumber", "combrnId" }))
public class Communication implements java.io.Serializable {

	private Long comId;
	private Integer comNumber;
	private Short comType;
	private String comValue;
	private String comDescription;
	private long combrnId;
	private Short comState;

	public Communication() {
	}

	@Caption("ComId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "comId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getComId() {
		return this.comId;
	}

	public void setComId(final Long comId) {
		this.comId = comId;
	}

	@Caption("ComNumber")
	@Column(name = "comNumber", columnDefinition = "int")
	public Integer getComNumber() {
		return this.comNumber;
	}

	public void setComNumber(final Integer comNumber) {
		this.comNumber = comNumber;
	}

	@Caption("ComType")
	@Column(name = "comType", columnDefinition = "smallint")
	public Short getComType() {
		return this.comType;
	}

	public void setComType(final Short comType) {
		this.comType = comType;
	}

	@Caption("ComValue")
	@Column(name = "comValue", columnDefinition = "nvarchar")
	public String getComValue() {
		return this.comValue;
	}

	public void setComValue(final String comValue) {
		this.comValue = comValue;
	}

	@Caption("ComDescription")
	@Column(name = "comDescription", columnDefinition = "nvarchar")
	public String getComDescription() {
		return this.comDescription;
	}

	public void setComDescription(final String comDescription) {
		this.comDescription = comDescription;
	}

	@Caption("CombrnId")
	@Column(name = "combrnId", nullable = false, columnDefinition = "bigint")
	public long getCombrnId() {
		return this.combrnId;
	}

	public void setCombrnId(final long combrnId) {
		this.combrnId = combrnId;
	}

	@Caption("ComState")
	@Column(name = "comState", columnDefinition = "smallint")
	public Short getComState() {
		return this.comState;
	}

	public void setComState(final Short comState) {
		this.comState = comState;
	}

}
