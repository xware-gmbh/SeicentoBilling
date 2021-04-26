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

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ProjectAllocationDAO;

/**
 * Project
 */
@DAO(daoClass = ProjectAllocationDAO.class)
@Caption("{%praId}")
@Entity
@Table(name = "ProjectAllocation", schema = "dbo")
public class ProjectAllocation implements java.io.Serializable {

	private Long praId;
	private CostAccount costAccount;
	private Project project;
	private Date praStartDate;
	private Date praEndDate;
	private int praHours;
	private int praIntensityPercent;
	private double praRate;
	private String praRemark;
	private LovState.State praState;

	public ProjectAllocation() {
	}

	@Caption("PraId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "praId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPraId() {
		return this.praId;
	}

	public void setPraId(final Long proId) {
		this.praId = proId;
	}

	@Caption("Projektstart")
	@Temporal(TemporalType.DATE)
	@Column(name = "praStartDate", nullable = false, columnDefinition = "date", length = 10)
	public Date getPraStartDate() {
		return this.praStartDate;
	}

	public void setPraStartDate(final Date praStartDate) {
		this.praStartDate = praStartDate;
	}

	@Caption("Projektende")
	@Temporal(TemporalType.DATE)
	@Column(name = "praEndDate", nullable = false, columnDefinition = "date", length = 10)
	public Date getPraEndDate() {
		return this.praEndDate;
	}

	public void setPraEndDate(final Date praEndDate) {
		this.praEndDate = praEndDate;
	}

	@Caption("Stundensoll")
	@Column(name = "praHours", columnDefinition = "int")
	public int getPraHours() {
		return this.praHours;
	}

	public void setPraHours(final int praHours) {
		this.praHours = praHours;
	}

	@Caption("Intensität")
	@Column(name = "praIntensityPercent", columnDefinition = "int")
	public int getPraIntensityPercent() {
		return this.praIntensityPercent;
	}

	public void setPraIntensityPercent(final int praIntensityPercent) {
		this.praIntensityPercent = praIntensityPercent;
	}

	@Caption("Ansatz")
	@Column(name = "praRate", nullable = false, columnDefinition = "decimal", precision = 6)
	public double getPraRate() {
		return this.praRate;
	}

	public void setPraRate(final double praRate) {
		this.praRate = praRate;
	}

	@Caption("Bemerkung")
	@Column(name = "praRemark", columnDefinition = "nvarchar")
	public String getPraRemark() {
		return this.praRemark;
	}

	public void setPraRemark(final String praRemark) {
		this.praRemark = praRemark;
	}

	@Caption("Status")
	@Column(name = "praState", columnDefinition = "smallint")
	public LovState.State getPraState() {
		return this.praState;
	}

	public void setPraState(final LovState.State praState) {
		this.praState = praState;
	}

	@Caption("Kostenstelle")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "pracsaId", columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("Projekt")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "praproId", columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

}
