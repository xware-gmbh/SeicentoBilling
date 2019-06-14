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

import ch.xwr.seicentobilling.dal.StateCodeDAO;

/**
 * StateCode
 */
@DAO(daoClass = StateCodeDAO.class)
@Caption("{%stcFieldname}")
@Entity
@Table(name = "StateCode", schema = "dbo")
public class StateCode implements java.io.Serializable {

	private Long stcId;
	private ch.xwr.seicentobilling.entities.Entity entity;
	private String stcFieldname;
	private Integer stcCode;
	private String stcCodeName;
	private Short stcState;

	public StateCode() {
	}

	@Caption("StcId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "stcId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getStcId() {
		return this.stcId;
	}

	public void setStcId(final Long stcId) {
		this.stcId = stcId;
	}

	@Caption("Entity")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "stcentId", nullable = false, columnDefinition = "bigint")
	public ch.xwr.seicentobilling.entities.Entity getEntity() {
		return this.entity;
	}

	public void setEntity(final ch.xwr.seicentobilling.entities.Entity entity) {
		this.entity = entity;
	}

	@Caption("StcFieldname")
	@Column(name = "stcFieldname", columnDefinition = "nvarchar")
	public String getStcFieldname() {
		return this.stcFieldname;
	}

	public void setStcFieldname(final String stcFieldname) {
		this.stcFieldname = stcFieldname;
	}

	@Caption("StcCode")
	@Column(name = "stcCode", columnDefinition = "int")
	public Integer getStcCode() {
		return this.stcCode;
	}

	public void setStcCode(final Integer stcCode) {
		this.stcCode = stcCode;
	}

	@Caption("StcCodeName")
	@Column(name = "stcCodeName", columnDefinition = "nvarchar")
	public String getStcCodeName() {
		return this.stcCodeName;
	}

	public void setStcCodeName(final String stcCodeName) {
		this.stcCodeName = stcCodeName;
	}

	@Caption("StcState")
	@Column(name = "stcState", columnDefinition = "smallint")
	public Short getStcState() {
		return this.stcState;
	}

	public void setStcState(final Short stcState) {
		this.stcState = stcState;
	}

}
