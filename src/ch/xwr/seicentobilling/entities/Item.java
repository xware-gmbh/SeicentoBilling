package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.ItemDAO;

/**
 * Item
 */
@DAO(daoClass = ItemDAO.class)
@Caption("{%itmIdent}")
@Entity
@Table(name = "Item", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "itmIdent"))
public class Item implements java.io.Serializable {

	private Long itmId;
	private ItemGroup itemGroup;
	private Vat vat;
	private String itmIdent;
	private String itmName;
	private Double itmPrice1;
	private Double itmPrice2;
	private LovState.Unit itmUnit;
	private LovState.State itmState;
	private Set<OrderLine> orderLines = new HashSet<>(0);
	@SuppressWarnings("unused")
	private String prpShortName;
	private Double itmAccount;
	private LovState.itmPriceLevel itmPriceLevel;

	public Item() {
	}

	@Caption("ItmId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "itmId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getItmId() {
		return this.itmId;
	}

	public void setItmId(final Long itmId) {
		this.itmId = itmId;
	}

	@Caption("ItemGroup")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "itmitgId", columnDefinition = "bigint")
	public ItemGroup getItemGroup() {
		return this.itemGroup;
	}

	public void setItemGroup(final ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	@Caption("Vat")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "itmvatId", columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}

	@Caption("ItmIdent")
	@Column(name = "itmIdent", unique = true, nullable = false, columnDefinition = "nvarchar")
	public String getItmIdent() {
		return this.itmIdent;
	}

	public void setItmIdent(final String itmIdent) {
		this.itmIdent = itmIdent;
	}

	@Caption("ItmName")
	@Column(name = "itmName", columnDefinition = "nvarchar")
	public String getItmName() {
		return this.itmName;
	}

	public void setItmName(final String itmName) {
		this.itmName = itmName;
	}

	@Caption("ItmPrice1")
	@Column(name = "itmPrice1", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getItmPrice1() {
		return this.itmPrice1;
	}

	public void setItmPrice1(final Double itmPrice1) {
		this.itmPrice1 = itmPrice1;
	}

	@Caption("ItmPrice2")
	@Column(name = "itmPrice2", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getItmPrice2() {
		return this.itmPrice2;
	}

	public void setItmPrice2(final Double itmPrice2) {
		this.itmPrice2 = itmPrice2;
	}

	@Caption("ItmUnit")
	@Column(name = "itmUnit", columnDefinition = "int")
	public LovState.Unit getItmUnit() {
		return this.itmUnit;
	}

	public void setItmUnit(final LovState.Unit itmUnit) {
		this.itmUnit = itmUnit;
	}

	@Caption("ItmState")
	@Column(name = "itmState", columnDefinition = "smallint")
	public LovState.State getItmState() {
		return this.itmState;
	}

	public void setItmState(final LovState.State itmState) {
		this.itmState = itmState;
	}

	@Caption("OrderLines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public Set<OrderLine> getOrderLines() {
		return this.orderLines;
	}

	public void setOrderLines(final Set<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

	@Column(name = "PRPSHORTNAME", insertable = false, updatable = false)
	@Transient
	public String getPrpShortName() {
		final StringBuffer bf = new StringBuffer("");
		bf.append(this.itmName).append(" - ").append(this.itmIdent);
		return bf.toString();
	}

	public void setPrpShortName(final String noname) {
		this.prpShortName = noname;
	}

	@Caption("Konto")
	@Column(name = "itmAccount", precision = 10, scale = 3, columnDefinition = "numeric")
	public Double getItmAccount() {
		return this.itmAccount;
	}

	public void setItmAccount(final Double noname) {
		this.itmAccount = noname;
	}

	@Caption("Preis Level")
	@Column(name = "itmPriceLevel", columnDefinition = "smallint")
	public LovState.itmPriceLevel getItmPriceLevel() {
		return this.itmPriceLevel;
	}

	public void setItmPriceLevel(final LovState.itmPriceLevel itmPriceLevel) {
		this.itmPriceLevel = itmPriceLevel;
	}

}
