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

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.OrderLineDAO;

/**
 * OrderLine
 */
@DAO(daoClass = OrderLineDAO.class)
@Caption("{%odlText}")
@Entity
@Table(name = "OrderLine", schema = "dbo")
public class OrderLine implements java.io.Serializable {

	private Long odlId;
	private CostAccount costAccount;
	private Item item;
	private Order orderhdr;
	private Vat vat;
	private int odlNumber;
	private double odlQuantity;
	private Double odlPrice;
	private Double odlAmountBrut;
	private Double odlAmountNet;
	private String odlText;
	private Double odlVatAmount;
	private Double odlDiscount;
	private LovState.State odlState;

	public OrderLine() {
	}

	@Caption("OdlId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "odlId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getOdlId() {
		return this.odlId;
	}

	public void setOdlId(final Long odlId) {
		this.odlId = odlId;
	}

	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "odlcsaId", nullable = false, columnDefinition = "bigint")
	public CostAccount getCostAccount() {
		return this.costAccount;
	}

	public void setCostAccount(final CostAccount costAccount) {
		this.costAccount = costAccount;
	}

	@Caption("Item")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "odlitmId", nullable = false, columnDefinition = "bigint")
	public Item getItem() {
		return this.item;
	}

	public void setItem(final Item item) {
		this.item = item;
	}

	@Caption("Order")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "odlordId", columnDefinition = "bigint")
	public Order getOrderhdr() {
		return this.orderhdr;
	}

	public void setOrderhdr(final Order order) {
		this.orderhdr = order;
	}

	@Caption("Vat")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "odlvatId", nullable = false, columnDefinition = "bigint")
	public Vat getVat() {
		return this.vat;
	}

	public void setVat(final Vat vat) {
		this.vat = vat;
	}

	@Caption("OdlNumber")
	@Column(name = "odlNumber", nullable = false, columnDefinition = "int")
	public int getOdlNumber() {
		return this.odlNumber;
	}

	public void setOdlNumber(final int odlNumber) {
		this.odlNumber = odlNumber;
	}

	@Caption("OdlQuantity")
	@Column(name = "odlQuantity", nullable = false, columnDefinition = "numeric", precision = 10, scale = 4)
	public double getOdlQuantity() {
		return this.odlQuantity;
	}

	public void setOdlQuantity(final double odlQuantity) {
		this.odlQuantity = odlQuantity;
	}

	@Caption("OdlPrice")
	@Column(name = "odlPrice", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOdlPrice() {
		return this.odlPrice;
	}

	public void setOdlPrice(final Double odlPrice) {
		this.odlPrice = odlPrice;
	}

	@Caption("OdlAmountBrut")
	@Column(name = "odlAmountBrut", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOdlAmountBrut() {
		return this.odlAmountBrut;
	}

	public void setOdlAmountBrut(final Double odlAmountBrut) {
		this.odlAmountBrut = odlAmountBrut;
	}

	@Caption("OdlAmountNet")
	@Column(name = "odlAmountNet", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOdlAmountNet() {
		return this.odlAmountNet;
	}

	public void setOdlAmountNet(final Double odlAmountNet) {
		this.odlAmountNet = odlAmountNet;
	}

	@Caption("OdlText")
	@Column(name = "odlText", columnDefinition = "nvarchar")
	public String getOdlText() {
		return this.odlText;
	}

	public void setOdlText(final String odlText) {
		this.odlText = odlText;
	}

	@Caption("OdlVatAmount")
	@Column(name = "odlVatAmount", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOdlVatAmount() {
		return this.odlVatAmount;
	}

	public void setOdlVatAmount(final Double odlVatAmount) {
		this.odlVatAmount = odlVatAmount;
	}

	@Caption("OdlDiscount")
	@Column(name = "odlDiscount", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOdlDiscount() {
		return this.odlDiscount;
	}

	public void setOdlDiscount(final Double odlDiscount) {
		this.odlDiscount = odlDiscount;
	}

	@Caption("OdlState")
	@Column(name = "odlState", columnDefinition = "smallint")
	public LovState.State getOdlState() {
		return this.odlState;
	}

	public void setOdlState(final LovState.State odlState) {
		this.odlState = odlState;
	}

}
