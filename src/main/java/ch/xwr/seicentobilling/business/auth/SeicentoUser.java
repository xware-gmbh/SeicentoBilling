
package ch.xwr.seicentobilling.business.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nimbusds.jwt.JWTClaimsSet;
import com.rapidclipse.framework.security.authorization.Role;
import com.rapidclipse.framework.security.authorization.Subject;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.AppUserDAO;
import ch.xwr.seicentobilling.entities.AppUser;
import ch.xwr.seicentobilling.entities.CostAccount;


public class SeicentoUser implements Subject, Serializable
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SeicentoUser.class);
	
	private AzureUser    aadUser  = null;
	private AppUser      dbUser   = null;
	private CostAccount  cst      = null;
	private JWTClaimsSet claimSet = null;
	
	private String name = "";
	// private final String uid = "";
	// private final String token = "";
	private volatile Set<Role> roles;
	
	public SeicentoUser()
	{
		this("unknown");
	}
	
	public SeicentoUser(final AzureUser aad)
	{
		this.aadUser = aad;
		this.name    = aad.name();
		this.lookupDbUser(aad.getUniqueName());
	}
	
	public SeicentoUser(final String username)
	{
		this.name = username;
		
		this.lookupDbUser(username);
		if(this.getAzureUser() == null)
		{
			this.initLocalUser();
		}
		
	}
	
	private void lookupDbUser(final String uniquename)
	{
		final AppUserDAO dao = new AppUserDAO();
		
		final List<AppUser> lst = dao.findByName(uniquename);
		if(lst != null && lst.size() > 0)
		{
			this.dbUser = lst.get(0);
		}
		else
		{
			if(this.getAzureUser() != null)
			{
				final AppUser bean = new AppUser();
				bean.setUsername(this.aadUser.getUniqueName());
				bean.setUsrFullName(this.aadUser.name());
				bean.setUsrRoles(this.getAzureRoles());
				bean.setUsrState(LovState.State.active);
				
				// auto create user if it does not exist
				new AppUserDAO().save(bean);
				this.dbUser = bean;
				SeicentoUser.LOG.debug("New AppUser created based on Azure AD: " + bean.getUsername());
			}
		}
	}
	
	private String getAzureRoles()
	{
		String roles  = "";
		int    icount = 0;
		
		if(this.aadUser.getClaimSet().getClaims() != null)
		{
			@SuppressWarnings("unchecked")
			final List<String> roleNames = (List<String>)this.aadUser.getClaimSet().getClaim("roles");
			if(roleNames != null)
			{
				for(final String role : roleNames)
				{
					if(icount == 0)
					{
						roles = role;
					}
					else
					{
						roles = roles + ", " + role;
					}
					icount++;
				}
			}
		}
		return roles;
	}
	
	private void initLocalUser()
	{
		String             role = "";
		final List<String> ls   = new ArrayList<>();
		if(this.dbUser != null)
		{
			role = this.dbUser.getUsrRoles();
			ls.add(role);
		}
		
		this.claimSet = new JWTClaimsSet.Builder()
			.subject(this.name)
			.issueTime(new Date())
			.issuer("XWare GmbH")
			.claim("roles", ls)
			.build();
		
		// dummy?
		this.roles = new HashSet<>();
		this.roles.add(Role.New(role));
	}
	
	@Override
	public String name()
	{
		if(this.getAzureUser() != null)
		{
			return this.getAzureUser().name();
		}
		return this.name;
	}
	
	@Override
	public Set<Role> roles()
	{
		if(this.getAzureUser() != null)
		{
			return this.getAzureUser().roles();
		}
		return this.roles;
	}
	
	public JWTClaimsSet getClaimSet()
	{
		if(this.getAzureUser() != null)
		{
			return this.getAzureUser().getClaimSet();
		}
		
		return this.claimSet;
	}
	
	public AzureUser getAzureUser()
	{
		return this.aadUser;
	}
	
	public AppUser getDbUser()
	{
		return this.dbUser;
	}
	
	public void setAzureUser(final AzureUser currentUser)
	{
		this.aadUser = currentUser;
	}
	
	public CostAccount getAssignedAccount()
	{
		return this.cst;
	}
	
	public void setAssignedAccount(final CostAccount cst)
	{
		this.cst = cst;
	}
	
}
