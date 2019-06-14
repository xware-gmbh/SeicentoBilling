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

import ch.xwr.seicentobilling.business.LovCrm;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ActivityDAO;

/**
 * Activity
 */
@DAO(daoClass = ActivityDAO.class)
@Caption("{%actName}")
@Entity
@Table(name = "Activity", schema = "dbo")
public class Activity implements java.io.Serializable {

	private Long actId;
	private Date actDate;
	private LovCrm.ActivityType actType;
	private Customer customer;
	private String actText;
	private Date actFollowingUpDate;
	private CostAccount costaccount;
	private String actLink;
	private LovState.State actState;


	public Activity() {
	}

	@Caption("actId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "actId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getactId() {
		return this.actId;
	}

	public void setactId(final Long actId) {
		this.actId = actId;
	}

	@Caption("Date")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actDate", nullable = false, columnDefinition = "datetime", length = 23)
	public Date getActDate() {
		return this.actDate;
	}

	public void setActDate(final Date actDate) {
		this.actDate = actDate;
	}

	@Caption("Type")
	@Column(name = "actType", columnDefinition = "smallint")
	public LovCrm.ActivityType getActType() {
		return this.actType;
	}

	public void setActType(final LovCrm.ActivityType actType) {
		this.actType = actType;
	}

	@Caption("Customer")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "actcusId", columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}

	@Caption("Text")
	@Column(name = "actText", columnDefinition = "ntext")
	public String getActText() {
		return this.actText;
	}

	public void setActText(final String actText) {
		this.actText = actText;
	}

	@Caption("Link")
	@Column(name = "actLink", columnDefinition = "nvarchar", length = 256)
	public String getActLink() {
		return this.actLink;
	}

	public void setActLink(final String actLink) {
		this.actLink = actLink;
	}

	@Caption("FollowingUpDate")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actFollowingUpDate", nullable = false, columnDefinition = "datetime", length = 23)
	public Date getActFollowingUpDate() {
		return this.actFollowingUpDate;
	}

	public void setActFollowingUpDate(final Date actFollowingUpDate) {
		this.actFollowingUpDate = actFollowingUpDate;
	}

	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "actcsaId", columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costaccount;
	}

	public void setCostAccount(final CostAccount costaccount) {
		this.costaccount = costaccount;
	}

	@Caption("State")
	@Column(name = "actState", columnDefinition = "smallint")
	public LovState.State getActState() {
		return this.actState;
	}

	public void setActState(final LovState.State actState) {
		this.actState = actState;
	}

}
