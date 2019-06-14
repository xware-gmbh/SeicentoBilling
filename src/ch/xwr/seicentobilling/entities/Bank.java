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

import ch.xwr.seicentobilling.dal.BankDAO;

/**
 * Bank
 */
@DAO(daoClass = BankDAO.class)
@Caption("{%bnkName}")
@Entity
@Table(name = "Bank", schema = "dbo")
public class Bank implements java.io.Serializable {

	private Long bnkId;
	private City city;
	private String bnkName;
	private String bnkAddress;
	private String bnkAccount;
	private String bnkIban;
	private Short bnkState;
	private String bnkEsrTn;
	private Long bnkCustomernbr;
	private String bnkClearing;
	private String bnkSwift;
	private String bnkZip;

	public Bank() {
	}

	@Caption("BnkId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "bnkId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getBnkId() {
		return this.bnkId;
	}

	public void setBnkId(final Long bnkId) {
		this.bnkId = bnkId;
	}

	@Caption("City")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "bnkctyId", columnDefinition = "bigint")
	public City getCity() {
		return this.city;
	}

	public void setCity(final City city) {
		this.city = city;
	}

	@Caption("BnkName")
	@Column(name = "bnkName", columnDefinition = "nvarchar")
	public String getBnkName() {
		return this.bnkName;
	}

	public void setBnkName(final String bnkName) {
		this.bnkName = bnkName;
	}

	@Caption("BnkAddress")
	@Column(name = "bnkAddress", columnDefinition = "nvarchar")
	public String getBnkAddress() {
		return this.bnkAddress;
	}

	public void setBnkAddress(final String bnkAddress) {
		this.bnkAddress = bnkAddress;
	}

	@Caption("BnkAccount")
	@Column(name = "bnkAccount", columnDefinition = "nvarchar")
	public String getBnkAccount() {
		return this.bnkAccount;
	}

	public void setBnkAccount(final String bnkAccount) {
		this.bnkAccount = bnkAccount;
	}

	@Caption("BnkIban")
	@Column(name = "bnkIban", columnDefinition = "nvarchar")
	public String getBnkIban() {
		return this.bnkIban;
	}

	public void setBnkIban(final String bnkIban) {
		this.bnkIban = bnkIban;
	}

	@Caption("BnkState")
	@Column(name = "bnkState", columnDefinition = "smallint")
	public Short getBnkState() {
		return this.bnkState;
	}

	public void setBnkState(final Short bnkState) {
		this.bnkState = bnkState;
	}

	@Caption("BnkEsrTn")
	@Column(name = "bnkEsrTn", columnDefinition = "nchar")
	public String getBnkEsrTn() {
		return this.bnkEsrTn;
	}

	public void setBnkEsrTn(final String bnkEsrTn) {
		this.bnkEsrTn = bnkEsrTn;
	}

	@Caption("BnkCustomernbr")
	@Column(name = "bnkCustomernbr", columnDefinition = "bigint")
	public Long getBnkCustomernbr() {
		return this.bnkCustomernbr;
	}

	public void setBnkCustomernbr(final Long bnkCustomernbr) {
		this.bnkCustomernbr = bnkCustomernbr;
	}

	@Caption("BnkClearing")
	@Column(name = "bnkClearing", columnDefinition = "varchar", length = 50)
	public String getBnkClearing() {
		return this.bnkClearing;
	}

	public void setBnkClearing(final String bnkClearing) {
		this.bnkClearing = bnkClearing;
	}

	@Caption("BnkSwift")
	@Column(name = "bnkSwift", columnDefinition = "varchar", length = 50)
	public String getBnkSwift() {
		return this.bnkSwift;
	}

	public void setBnkSwift(final String bnkSwift) {
		this.bnkSwift = bnkSwift;
	}

	@Caption("BnkZip")
	@Column(name = "bnkZip", columnDefinition = "varchar", length = 50)
	public String getBnkZip() {
		return this.bnkZip;
	}

	public void setBnkZip(final String bnkZip) {
		this.bnkZip = bnkZip;
	}

}
