package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Calendar;
import java.util.Date;
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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.OrderDAO;

/**
 * Order
 */
@DAO(OrderDAO.class)
@Caption("{%ordCreatedBy}")
@Entity
@Table(name = "[Order]", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "ordNumber"))
public class Order implements java.io.Serializable {

	private Long ordId;
	private Customer customer;
	private PaymentCondition paymentCondition;
	private Project project;
	private int ordNumber;
	private LovState.State ordState;
	private Date ordCreated;
	private String ordCreatedBy;
	private Date ordOrderDate;
	private Date ordBillDate;
	private Double ordAmountBrut;
	private Double ordAmountNet;
	private Date ordPayDate;
	private String ordText;
	private Date ordDueDate;
	private Date ordBookedOn;
	private Set<OrderLine> orderLines = new HashSet<>(0);
	@SuppressWarnings("unused")
	private Double ordAmountVat;  //transient

	public Order() {
	}

	@Caption("OrdId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "ordId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getOrdId() {
		return this.ordId;
	}

	public void setOrdId(final Long ordId) {
		this.ordId = ordId;
	}

	@Caption("Kunde")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ordcusId", nullable = false, columnDefinition = "bigint")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(final Customer customer) {
		this.customer = customer;
	}

	@Caption("Zahlungsbedingung")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ordpacId", nullable = false, columnDefinition = "bigint")
	public PaymentCondition getPaymentCondition() {
		return this.paymentCondition;
	}

	public void setPaymentCondition(final PaymentCondition paymentCondition) {
		this.paymentCondition = paymentCondition;
	}

	@Caption("Projekt")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ordproId", columnDefinition = "bigint")
	public Project getProject() {
		return this.project;
	}

	public void setProject(final Project project) {
		this.project = project;
	}

	@Caption("AuftragNbr")
	@Column(name = "ordNumber", unique = true, nullable = false, columnDefinition = "int")
	public int getOrdNumber() {
		return this.ordNumber;
	}

	public void setOrdNumber(final int ordNumber) {
		this.ordNumber = ordNumber;
	}

	@Caption("Status")
	@Column(name = "ordState", columnDefinition = "smallint")
	public LovState.State getOrdState() {
		return this.ordState;
	}

	public void setOrdState(final LovState.State ordState) {
		this.ordState = ordState;
	}

	@Caption("Erstellt am")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordCreated", columnDefinition = "datetime", length = 23)
	public Date getOrdCreated() {
		return this.ordCreated;
	}

	public void setOrdCreated(final Date ordCreated) {
		this.ordCreated = ordCreated;
	}

	@Caption("Erstellt von")
	@Column(name = "ordCreatedBy", columnDefinition = "nvarchar")
	public String getOrdCreatedBy() {
		return this.ordCreatedBy;
	}

	public void setOrdCreatedBy(final String ordCreatedBy) {
		this.ordCreatedBy = ordCreatedBy;
	}

	@Caption("Bestelldatum")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordOrderDate", columnDefinition = "datetime", length = 23)
	public Date getOrdOrderDate() {
		return this.ordOrderDate;
	}

	public void setOrdOrderDate(final Date ordOrderDate) {
		this.ordOrderDate = ordOrderDate;
	}

	@Caption("Rechnungsdatum")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordBillDate", columnDefinition = "datetime", length = 23)
	public Date getOrdBillDate() {
		return this.ordBillDate;
	}

	public void setOrdBillDate(final Date ordBillDate) {
		this.ordBillDate = ordBillDate;
	}

	@Caption("Betrag Brutto")
	@Column(name = "ordAmountBrut", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOrdAmountBrut() {
		return this.ordAmountBrut;
	}

	public void setOrdAmountBrut(final Double ordAmountBrut) {
		this.ordAmountBrut = ordAmountBrut;
	}

	@Caption("Betrag Netto")
	@Column(name = "ordAmountNet", columnDefinition = "numeric", precision = 10, scale = 3)
	public Double getOrdAmountNet() {
		return this.ordAmountNet;
	}

	public void setOrdAmountNet(final Double ordAmountNet) {
		this.ordAmountNet = ordAmountNet;
	}

	@Caption("Zahlungsdatum")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordPayDate", columnDefinition = "datetime", length = 23)
	public Date getOrdPayDate() {
		return this.ordPayDate;
	}

	public void setOrdPayDate(final Date ordPayDate) {
		this.ordPayDate = ordPayDate;
	}

	@Caption("Text")
	@Column(name = "ordText", columnDefinition = "nvarchar")
	public String getOrdText() {
		return this.ordText;
	}

	public void setOrdText(final String ordText) {
		this.ordText = ordText;
	}

	@Caption("Fälligkeitsdatum")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordDueDate", columnDefinition = "datetime", length = 23)
	public Date getOrdDueDate() {
		return this.ordDueDate;
	}

	public void setOrdDueDate(final Date ordDueDate) {
		this.ordDueDate = ordDueDate;
	}

	@Caption("Buchungsdatum")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ordBookedOn", columnDefinition = "datetime", length = 23)
	public Date getOrdBookedOn() {
		return this.ordBookedOn;
	}

	public void setOrdBookedOn(final Date ordBookedOn) {
		this.ordBookedOn = ordBookedOn;
	}

	@Caption("Positionen")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "orderhdr")
	public Set<OrderLine> getOrderLines() {
		return this.orderLines;
	}

	public void setOrderLines(final Set<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

// Manueller Teil

	@Caption("Betrag Mwst")
	@Transient
	@Column(name = "ORDAMOUNTVAT", insertable = false, updatable = false)
	public Double getOrdAmountVat() {
		if (this.ordAmountNet == null || this.ordAmountBrut == null) {
			return new Double(0);
		}

		return (this.ordAmountNet - this.ordAmountBrut);
	}

    /**
     * Is called within save. No DB Access allowed here!!!
     */
    @PrePersist
    @PreUpdate
    public void calculateDueDatee() {
		final Calendar now = Calendar.getInstance();   // Gets the current date and time
		now.setTime(getOrdBillDate());
		now.add(Calendar.DAY_OF_MONTH, getPaymentCondition().getPacNbrOfDays());
		setOrdDueDate(now.getTime());
    }


}
