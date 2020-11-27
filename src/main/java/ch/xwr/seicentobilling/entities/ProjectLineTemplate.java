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

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ProjectLineTemplateDAO;

/**
 * ProjectLine
 */
@DAO(ProjectLineTemplateDAO.class)
@Caption("{%prtText}")
@Entity
@Table(name = "ProjectLineTemplate", schema = "dbo")
public class ProjectLineTemplate implements java.io.Serializable {

	private Long prtId;
	private CostAccount costaccount;
	private Project project;
	private Double prtHours;
	private String prtText;
	private LovState.WorkType prtWorkType;
	private Double prtRate;
	private LovState.State prtState;
	private int prtKeyNumber;

	public ProjectLineTemplate() {
	}

	@Caption("PrtId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "prtId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPrtId() {
		return this.prtId;
	}

	public void setprtId(final Long prtId) {
		this.prtId = prtId;
	}

	@Caption("Costaccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prtcsaId", nullable = false, columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costaccount;
	}

	public void setCostAccount(final CostAccount costaccount) {
		this.costaccount = costaccount;
	}

	@Caption("Project")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prtproId", nullable = false, columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}


	@Caption("prtHours")
	@Column(name = "prtHours", columnDefinition = "decimal", precision = 6)
	public Double getPrtHours() {
		return this.prtHours;
	}

	public void setPrtHours(final Double prtHours) {
		this.prtHours = prtHours;
	}

	@Caption("prtText")
	@Column(name = "prtText", columnDefinition = "nvarchar")
	public String getprtText() {
		return this.prtText;
	}

	public void setprtText(final String prtText) {
		this.prtText = prtText;
	}

	@Caption("prtWorkType")
	@Column(name = "prtWorkType", columnDefinition = "smallint")
	public LovState.WorkType getprtWorkType() {
		return this.prtWorkType;
	}

	public void setprtWorkType(final LovState.WorkType prtWorkType) {
		this.prtWorkType = prtWorkType;
	}

	@Caption("prtRate")
	@Column(name = "prtRate", columnDefinition = "decimal", precision = 6)
	public Double getPrtRate() {
		return this.prtRate;
	}

	public void setPrtRate(final Double prtRate) {
		this.prtRate = prtRate;
	}

	@Caption("prtState")
	@Column(name = "prtState", columnDefinition = "smallint")
	public LovState.State getPrtState() {
		return this.prtState;
	}

	public void setPrtState(final LovState.State prtState) {
		this.prtState = prtState;
	}

	@Caption("prtKeyNumber")
	@Column(name = "prtKeyNumber")
	public int getPrtKeyNumber() {
		return this.prtKeyNumber;
	}

	public void setPrtKeyNumber(final int noname) {
		this.prtKeyNumber = noname;
	}

}
