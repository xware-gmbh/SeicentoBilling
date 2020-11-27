
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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.security.authentication.CredentialsUsernamePassword;
import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.AppUserDAO;


/**
 * User
 */
@DAO(AppUserDAO.class)
@Caption("{%username}")
@Entity
@Table(name = "AppUser", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class AppUser implements java.io.Serializable, CredentialsUsernamePassword
{
	
	private Long           usrId;
	private String         username;
	private byte[]         password;
	private LovState.State usrState;
	private LovState.Theme usrThemeDesktop;
	private LovState.Theme usrThemeMobile;
	private String         usrLanguage;
	private String         usrTimeZone;
	private String         usrCountry;
	private CostAccount    costAccount;
	private Customer       customer;
	private Date           usrValidFrom;
	private Date           usrValidTo;
	private String         usrRoles;
	private String         usrFullName;
	
	public AppUser()
	{
	}
	
	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)
	
	@Column(name = "usrId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getUsrId()
	{
		return this.usrId;
	}
	
	public void setUsrId(final Long usrId)
	{
		this.usrId = usrId;
	}
	
	@Caption("Status")
	@Column(name = "usrState", columnDefinition = "smallint")
	public LovState.State getUsrState()
	{
		return this.usrState;
	}
	
	public void setUsrState(final LovState.State usrState)
	{
		this.usrState = usrState;
	}
	
	@Caption("Login Name")
	@Column(name = "username", unique = true, nullable = false, columnDefinition = "nvarchar")
	public String getUsername()
	{
		return this.username;
	}
	
	public void setUsername(final String username)
	{
		this.username = username;
	}
	
	@Caption("Password")
	@Column(name = "`password`", nullable = false, columnDefinition = "varbinary")
	public byte[] getPassword()
	{
		return this.password;
	}
	
	public void setPassword(final byte[] password)
	{
		this.password = password;
	}
	
	@Caption("Theme Desktop")
	@Column(name = "usrThemeDesktop", columnDefinition = "smallint")
	public LovState.Theme getUsrThemeDesktop()
	{
		return this.usrThemeDesktop;
	}
	
	public void setUsrThemeDesktop(final LovState.Theme noname2)
	{
		this.usrThemeDesktop = noname2;
	}
	
	@Caption("Theme Mobile")
	@Column(name = "usrThemeMobile", columnDefinition = "smallint")
	public LovState.Theme getUsrThemeMobile()
	{
		return this.usrThemeMobile;
	}
	
	public void setUsrThemeMobile(final LovState.Theme noname)
	{
		this.usrThemeMobile = noname;
	}
	
	@Caption("Language")
	@Column(name = "usrLanguage", columnDefinition = "nvarchar")
	public String getUsrLanguage()
	{
		return this.usrLanguage;
	}
	
	public void setUsrLanguage(final String noname)
	{
		this.usrLanguage = noname;
	}
	
	@Caption("TimeZone")
	@Column(name = "usrTimeZone", columnDefinition = "nvarchar")
	public String getUsrTimeZone()
	{
		return this.usrTimeZone;
	}
	
	public void setUsrTimeZone(final String noname)
	{
		this.usrTimeZone = noname;
	}
	
	@Caption("Country")
	@Column(name = "usrCountry", columnDefinition = "nvarchar")
	public String getUsrCountry()
	{
		return this.usrCountry;
	}
	
	public void setUsrCountry(final String noname)
	{
		this.usrCountry = noname;
	}
	
	@Caption("CostAccount")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "usrcsaId", columnDefinition = "bigint")
	public CostAccount getCostAccount()
	{
		return this.costAccount;
	}
	
	public void setCostAccount(final CostAccount costAccount)
	{
		this.costAccount = costAccount;
	}
	
	@Caption("Contact")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "usrcusId", columnDefinition = "bigint")
	public Customer getCustomer()
	{
		return this.customer;
	}
	
	public void setCustomer(final Customer customer)
	{
		this.customer = customer;
	}
	
	@Caption("Valid From")
	@Column(name = "usrValidFrom", columnDefinition = "datetime")
	public Date getUsrValidFrom()
	{
		return this.usrValidFrom;
	}
	
	public void setUsrValidFrom(final Date noname)
	{
		this.usrValidFrom = noname;
	}
	
	@Caption("Valid To")
	@Column(name = "usrValidTo", columnDefinition = "datetime")
	public Date getUsrValidTo()
	{
		return this.usrValidTo;
	}
	
	public void setUsrValidTo(final Date noname)
	{
		this.usrValidTo = noname;
	}
	
	@Caption("Roles")
	@Column(name = "usrRoles", columnDefinition = "nvarchar")
	public String getUsrRoles()
	{
		return this.usrRoles;
	}
	
	public void setUsrRoles(final String noname)
	{
		this.usrRoles = noname;
	}
	
	@Caption("Name")
	@Column(name = "usrFullName", columnDefinition = "nvarchar")
	public String getUsrFullName()
	{
		return this.usrFullName;
	}
	
	public void setUsrFullName(final String noname)
	{
		this.usrFullName = noname;
	}
	
	@Override
	@Column(name = "username", insertable = false, updatable = false)
	@Transient
	public String username()
	{
		return this.getUsername();
	}
	
	@Override
	@Column(name = "password", insertable = false, updatable = false)
	@Transient
	public byte[] password()
	{
		return this.getPassword();
	}
	
}
