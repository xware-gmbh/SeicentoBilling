package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.CompanyDAO;

/**
 * Company
 */
@DAO(CompanyDAO.class)
@Caption("{%cmpName}")
@Entity
@Table(name = "Company", schema = "dbo")
public class Company implements java.io.Serializable {

	private Long cmpId;
	private String cmpName;
	private String cmpAddress;
	private Integer cmpZip;
	private String cmpPlace;
	private String cmpVatcode;
	private String cmpCurrency;
	private String cmpUid;
	private String cmpPhone;
	private String cmpMail;
	private String cmpComm1;
	private String cmpBusiness;
	private byte[] cmpLogo;
	private String cmpJasperUri;
	private Integer cmpBookingYear;
	private Integer cmpLastOrderNbr;
	private Integer cmpLastItemNbr;
	private Integer cmpLastCustomerNbr;
	private String cmpReportUsr;
	private String cmpReportPwd;
	private LovState.State cmpState;
	private Boolean cmpAbaActive;
	private String cmpAbaEndpointCus;
	private String cmpAbaEndpointDoc;
	private String cmpAbaUser;
	private Integer cmpAbaMandator;
	private Integer cmpAbaMaxDays;
	private String cmpAbaEndpointCre;
	private String cmpAbaEndpointCreDoc;
	private String cmpAbaEndpointPay;

	public Company() {
	}

	@Caption("CmpId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "cmpId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getCmpId() {
		return this.cmpId;
	}

	public void setCmpId(final Long cmpId) {
		this.cmpId = cmpId;
	}

	@Caption("Name")
	@Column(name = "cmpName", columnDefinition = "nvarchar")
	public String getCmpName() {
		return this.cmpName;
	}

	public void setCmpName(final String cmpName) {
		this.cmpName = cmpName;
	}

	@Caption("Adresse")
	@Column(name = "cmpAddress", columnDefinition = "nvarchar")
	public String getCmpAddress() {
		return this.cmpAddress;
	}

	public void setCmpAddress(final String cmpAddress) {
		this.cmpAddress = cmpAddress;
	}

	@Caption("PLZ")
	@Column(name = "cmpZip", columnDefinition = "int")
	public Integer getCmpZip() {
		return this.cmpZip;
	}

	public void setCmpZip(final Integer cmpZip) {
		this.cmpZip = cmpZip;
	}

	@Caption("Ort")
	@Column(name = "cmpPlace", columnDefinition = "nvarchar")
	public String getCmpPlace() {
		return this.cmpPlace;
	}

	public void setCmpPlace(final String cmpPlace) {
		this.cmpPlace = cmpPlace;
	}

	@Caption("CmpVatcode")
	@Column(name = "cmpVatcode", columnDefinition = "nvarchar")
	public String getCmpVatcode() {
		return this.cmpVatcode;
	}

	public void setCmpVatcode(final String cmpVatcode) {
		this.cmpVatcode = cmpVatcode;
	}

	@Caption("CmpCurrency")
	@Column(name = "cmpCurrency", columnDefinition = "nvarchar")
	public String getCmpCurrency() {
		return this.cmpCurrency;
	}

	public void setCmpCurrency(final String cmpCurrency) {
		this.cmpCurrency = cmpCurrency;
	}

	@Caption("Uid Firma")
	@Column(name = "cmpUid", columnDefinition = "nvarchar")
	public String getCmpUid() {
		return this.cmpUid;
	}

	public void setCmpUid(final String cmpUid) {
		this.cmpUid = cmpUid;
	}

	@Caption("Telefon")
	@Column(name = "cmpPhone", columnDefinition = "nvarchar")
	public String getCmpPhone() {
		return this.cmpPhone;
	}

	public void setCmpPhone(final String cmpPhone) {
		this.cmpPhone = cmpPhone;
	}

	@Caption("E-Mail")
	@Column(name = "cmpMail", columnDefinition = "nvarchar")
	public String getCmpMail() {
		return this.cmpMail;
	}

	public void setCmpMail(final String cmpMail) {
		this.cmpMail = cmpMail;
	}

	@Caption("CmpComm1")
	@Column(name = "cmpComm1", columnDefinition = "nvarchar")
	public String getCmpComm1() {
		return this.cmpComm1;
	}

	public void setCmpComm1(final String cmpComm1) {
		this.cmpComm1 = cmpComm1;
	}

	@Caption("CmpBusiness")
	@Column(name = "cmpBusiness", columnDefinition = "nvarchar")
	public String getCmpBusiness() {
		return this.cmpBusiness;
	}

	public void setCmpBusiness(final String cmpBusiness) {
		this.cmpBusiness = cmpBusiness;
	}

	@Caption("Logo")
	@Column(name = "cmpLogo", columnDefinition = "image")
	public byte[] getCmpLogo() {
		return this.cmpLogo;
	}

	public void setCmpLogo(final byte[] cmpLogo) {
		this.cmpLogo = cmpLogo;
	}

	@Caption("URI Jasper")
	@Column(name = "cmpJasperUri", columnDefinition = "nchar")
	public String getCmpJasperUri() {
		return this.cmpJasperUri;
	}

	public void setCmpJasperUri(final String cmpJasperUri) {
		this.cmpJasperUri = cmpJasperUri;
	}

	@Caption("CmpBookingYear")
	@Column(name = "cmpBookingYear", columnDefinition = "int")
	public Integer getCmpBookingYear() {
		return this.cmpBookingYear;
	}

	public void setCmpBookingYear(final Integer cmpBookingYear) {
		this.cmpBookingYear = cmpBookingYear;
	}

	@Caption("CmpLastOrderNbr")
	@Column(name = "cmpLastOrderNbr", columnDefinition = "int")
	public Integer getCmpLastOrderNbr() {
		return this.cmpLastOrderNbr;
	}

	public void setCmpLastOrderNbr(final Integer cmpLastOrderNbr) {
		this.cmpLastOrderNbr = cmpLastOrderNbr;
	}

	@Caption("CmpLastItemNbr")
	@Column(name = "cmpLastItemNbr", columnDefinition = "int")
	public Integer getCmpLastItemNbr() {
		return this.cmpLastItemNbr;
	}

	public void setCmpLastItemNbr(final Integer cmpLastItemNbr) {
		this.cmpLastItemNbr = cmpLastItemNbr;
	}

	@Caption("CmpLastCustomerNbr")
	@Column(name = "cmpLastCustomerNbr", columnDefinition = "int")
	public Integer getCmpLastCustomerNbr() {
		return this.cmpLastCustomerNbr;
	}

	public void setCmpLastCustomerNbr(final Integer cmpLastCustomerNbr) {
		this.cmpLastCustomerNbr = cmpLastCustomerNbr;
	}

	@Caption("CmpReportUsr")
	@Column(name = "cmpReportUsr", columnDefinition = "nvarchar")
	public String getCmpReportUsr() {
		return this.cmpReportUsr;
	}

	public void setCmpReportUsr(final String cmpReportUsr) {
		this.cmpReportUsr = cmpReportUsr;
	}

	@Caption("CmpReportPwd")
	@Column(name = "cmpReportPwd", columnDefinition = "nvarchar")
	public String getCmpReportPwd() {
		return this.cmpReportPwd;
	}

	public void setCmpReportPwd(final String cmpReportPwd) {
		this.cmpReportPwd = cmpReportPwd;
	}

	@Caption("State")
	@Column(name = "cmpState")
	public  LovState.State getCmpState() {
		return this.cmpState;
	}

	public void setCmpState(final  LovState.State noname) {
		this.cmpState = noname;
	}

	@Caption("Schnittstelle BuHa Aktiv")
	@Column(name = "cmpAbaActive", columnDefinition = "bit")
	public Boolean getCmpAbaActive() {
		return this.cmpAbaActive;
	}

	public void setCmpAbaActive(final Boolean noname) {
		this.cmpAbaActive = noname;
	}

	@Caption("Endpoint Kunde")
	@Column(name = "cmpAbaEndpointCus", columnDefinition = "nvarchar")
	public String getCmpAbaEndpointCus() {
		return this.cmpAbaEndpointCus;
	}

	public void setCmpAbaEndpointCus(final String noname) {
		this.cmpAbaEndpointCus = noname;
	}

	@Caption("Endpoint Dokument")
	@Column(name = "cmpAbaEndpointDoc", columnDefinition = "nvarchar")
	public String getCmpAbaEndpointDoc() {
		return this.cmpAbaEndpointDoc;
	}

	public void setCmpAbaEndpointDoc(final String noname) {
		this.cmpAbaEndpointDoc = noname;
	}

	@Caption("Benutzer")
	@Column(name = "cmpAbaUser", columnDefinition = "nvarchar")
	public String getCmpAbaUser() {
		return this.cmpAbaUser;
	}

	public void setCmpAbaUser(final String noname) {
		this.cmpAbaUser = noname;
	}

	@Caption("Mandant")
	@Column(name = "cmpAbaMandator", columnDefinition = "int")
	public Integer getCmpAbaMandator() {
		return this.cmpAbaMandator;
	}

	public void setCmpAbaMandator(final Integer noname) {
		this.cmpAbaMandator = noname;
	}

	@Caption("Max. Frist Tage Rechnung")
	@Column(name = "cmpAbaMaxDays", columnDefinition = "int")
	public Integer getCmpAbaMaxDays() {
		return this.cmpAbaMaxDays;
	}

	public void setCmpAbaMaxDays(final Integer noname) {
		this.cmpAbaMaxDays = noname;
	}

	@Caption("Endpoint Kredi")
	@Column(name = "cmpAbaEndpointCre", columnDefinition = "nvarchar", length = 256)
	public String getCmpAbaEndpointCre() {
		return this.cmpAbaEndpointCre;
	}

	public void setCmpAbaEndpointCre(final String noname) {
		this.cmpAbaEndpointCre = noname;
	}

	@Caption("Endpoint DokuKredi")
	@Column(name = "cmpAbaEndpointCreDoc", columnDefinition = "nvarchar", length = 256)
	public String getCmpAbaEndpointCreDoc() {
		return this.cmpAbaEndpointCreDoc;
	}

	public void setCmpAbaEndpointCreDoc(final String noname) {
		this.cmpAbaEndpointCreDoc = noname;
	}

	@Caption("Endpoint Debi Payment")
	@Column(name = "cmpAbaEndpointPay", length = 256, columnDefinition = "nvarchar")
	public String getCmpAbaEndpointPay() {
		return this.cmpAbaEndpointPay;
	}

	public void setCmpAbaEndpointPay(final String noname) {
		this.cmpAbaEndpointPay = noname;
	}

}
