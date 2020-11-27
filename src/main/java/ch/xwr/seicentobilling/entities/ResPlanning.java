package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.dal.ResPlanningDAO;

/**
 * ResPlanning
 */
@DAO(ResPlanningDAO.class)
@Caption("{%rspId}")
@Entity
@Table(name = "ResPlanning", schema = "dbo")
public class ResPlanning implements java.io.Serializable {

	private Long rspId;
	private CostAccount costAccount;
	private Project project;
	private Date rspPlandate;
	private short rspMode;
	private int rspPercent;
	private double rspHours;
	private short rspState;

	public ResPlanning() {
	}

	@Caption("RspId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "rspId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getRspId() {
		return this.rspId;
	}

	public void setRspId(final Long rspId) {
		this.rspId = rspId;
	}

	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rspcsaId", nullable = false, columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rspproId", columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("RspPlandate")
	@Temporal(TemporalType.DATE)
	@Column(name = "rspPlandate", nullable = false, columnDefinition = "date", length = 10)
	public Date getRspPlandate() {
		return this.rspPlandate;
	}

	public void setRspPlandate(final Date rspPlandate) {
		this.rspPlandate = rspPlandate;
	}

	@Caption("RspMode")
	@Column(name = "rspMode", nullable = false, columnDefinition = "smallint")
	public short getRspMode() {
		return this.rspMode;
	}

	public void setRspMode(final short rspMode) {
		this.rspMode = rspMode;
	}

	@Caption("RspPercent")
	@Column(name = "rspPercent", nullable = false, columnDefinition = "int")
	public int getRspPercent() {
		return this.rspPercent;
	}

	public void setRspPercent(final int rspPercent) {
		this.rspPercent = rspPercent;
	}

	@Caption("RspHours")
	@Column(name = "rspHours", nullable = false, columnDefinition = "numeric", precision = 10)
	public double getRspHours() {
		return this.rspHours;
	}

	public void setRspHours(final double rspHours) {
		this.rspHours = rspHours;
	}

	@Caption("RspState")
	@Column(name = "rspState", nullable = false, columnDefinition = "smallint")
	public short getRspState() {
		return this.rspState;
	}

	public void setRspState(final short rspState) {
		this.rspState = rspState;
	}

}
