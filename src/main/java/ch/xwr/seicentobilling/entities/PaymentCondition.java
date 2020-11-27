package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.dal.PaymentConditionDAO;

/**
 * PaymentCondition
 */
@DAO(PaymentConditionDAO.class)
@Caption("{%pacCode}")
@Entity
@Table(name = "PaymentCondition", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "pacCode"))
public class PaymentCondition implements java.io.Serializable {

	private Long pacId;
	private String pacCode;
	private String pacName;
	private int pacNbrOfDays;
	private Short pacState;
	private Set<Order> orders = new HashSet<>(0);
	private Set<Customer> customers = new HashSet<>(0);
	private String pacExtRef1;

	public PaymentCondition() {
	}

	@Caption("PacId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "pacId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getPacId() {
		return this.pacId;
	}

	public void setPacId(final Long pacId) {
		this.pacId = pacId;
	}

	@Caption("PacCode")
	@Column(name = "pacCode", unique = true, columnDefinition = "nvarchar")
	public String getPacCode() {
		return this.pacCode;
	}

	public void setPacCode(final String pacCode) {
		this.pacCode = pacCode;
	}

	@Caption("PacName")
	@Column(name = "pacName", columnDefinition = "nvarchar")
	public String getPacName() {
		return this.pacName;
	}

	public void setPacName(final String pacName) {
		this.pacName = pacName;
	}

	@Caption("PacNbrOfDays")
	@Column(name = "pacNbrOfDays", nullable = false, columnDefinition = "int")
	public int getPacNbrOfDays() {
		return this.pacNbrOfDays;
	}

	public void setPacNbrOfDays(final int pacNbrOfDays) {
		this.pacNbrOfDays = pacNbrOfDays;
	}

	@Caption("PacState")
	@Column(name = "pacState", columnDefinition = "smallint")
	public Short getPacState() {
		return this.pacState;
	}

	public void setPacState(final Short pacState) {
		this.pacState = pacState;
	}

	@Caption("Orders")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "paymentCondition")
	public Set<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(final Set<Order> orders) {
		this.orders = orders;
	}

	@Caption("Customers")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "paymentCondition")
	public Set<Customer> getCustomers() {
		return this.customers;
	}

	public void setCustomers(final Set<Customer> customers) {
		this.customers = customers;
	}

	@Caption("Ext Referenz 1")
	@Column(name = "pacExtRef1", length = 20)
	public String getPacExtRef1() {
		return this.pacExtRef1;
	}

	public void setPacExtRef1(final String noname) {
		this.pacExtRef1 = noname;
	}

}
