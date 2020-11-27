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

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.VatLineDAO;

/**
 * Vat
 */
//@EntityListeners(RowObjectListener.class)
@DAO(VatLineDAO.class)
@Caption("{%vanRate}")
@Entity
@Table(name = "VatLine", schema = "dbo")
public class VatLine implements java.io.Serializable {

	private Long vanId;
	private Date vanValidFrom;
	private Double vanRate;
	private String vanRemark;
	private LovState.State vanState;
	private Vat vat;


	public VatLine() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "vanId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getVanId() {
		return this.vanId;
	}

	public void setVanId(final Long vanId) {
		this.vanId = vanId;
	}

	@Caption("Gültig ab")
	@Column(name = "vanValidFrom", columnDefinition = "date")
	public Date getVanValidFrom() {
		return this.vanValidFrom;
	}

	public void setVanValidFrom(final Date vanValidFrom) {
		this.vanValidFrom = vanValidFrom;
	}

	@Caption("Ansatz")
	@Column(name = "vanRate", columnDefinition = "numeric", precision = 6, scale = 4)
	public Double getVanRate() {
		return this.vanRate;
	}

	public void setVanRate(final Double vanRate) {
		this.vanRate = vanRate;
	}

	@Caption("Bemerkung")
	@Column(name = "vanRemark", unique = true, columnDefinition = "nvarchar", length = 50)
	public String getVanRemark() {
		return this.vanRemark;
	}

	public void setVanRemark(final String vanRemark) {
		this.vanRemark = vanRemark;
	}

	@Caption("MwSt")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vanvatId", nullable = false, columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}


	@Caption("Status")
	@Column(name = "vanState", columnDefinition = "smallint")
	public LovState.State getVanState() {
		return this.vanState;
	}

	public void setVanState(final LovState.State vanState) {
		this.vanState = vanState;
	}



}
