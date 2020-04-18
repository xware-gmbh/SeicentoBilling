package ch.xwr.seicentobilling.business.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfree.util.Log;

import com.nimbusds.jwt.JWTClaimsSet;
import com.xdev.security.authorization.Role;
import com.xdev.security.authorization.Subject;
import com.xdev.server.aa.openid.auth.AzureUser;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.AppUserDAO;
import ch.xwr.seicentobilling.entities.AppUser;
import ch.xwr.seicentobilling.entities.CostAccount;

public class SeicentoUser implements Subject, Serializable {
	private AzureUser aadUser = null;
	private AppUser dbUser = null;
	private CostAccount cst = null;
	private JWTClaimsSet claimSet = null;

	private String name = "";
	//private final String uid = "";
	//private final String token = "";
	private volatile Set<Role> roles;

	public SeicentoUser() {
		this("unknown");
    }

	public SeicentoUser(final AzureUser aad) {
		this.aadUser = aad;
		lookupDbUser();
    }

	public SeicentoUser(final String username) {
		this.name = username;

		lookupDbUser();
		if (getAzureUser() == null) {
			initLocalUser();
		}

    }

	private void lookupDbUser() {
		final AppUserDAO dao = new AppUserDAO();

		final List<AppUser> lst = dao.findByName(this.name);
		if (lst != null && lst.size()>0) {
			this.dbUser = lst.get(0);
		} else {
			if (getAzureUser() != null) {
				final AppUser bean = new AppUser();
				bean.setUsername(this.aadUser.getUniqueName());
				bean.setUsrFullName(this.aadUser.name());
				bean.setUsrRoles(getAzureRoles());
				bean.setUsrState(LovState.State.active);

				//auto create user if it does not exist
				new AppUserDAO().save(bean);
				Log.debug("New AppUser created based on Azure AD: " + bean.getUsername());
			}
		}
	}

	private String getAzureRoles() {
		String roles = "";
		int icount = 0;

		if (this.aadUser.getClaimSet().getClaims() != null) {
			@SuppressWarnings("unchecked")
			final List<String> roleNames = (List<String>) this.aadUser.getClaimSet().getClaim("roles");
			if (roleNames != null) {
				for (final String role : roleNames) {
					if (icount == 0) {
						roles = role;
					} else {
						roles = roles + ", " + role;
					}
					icount++;
				}
			}
		}
		return roles;
	}

	private void initLocalUser() {
		String role = "";
		final List<String> ls =  new ArrayList<>();
		if (this.dbUser != null) {
			role = this.dbUser.getUsrRoles();
			ls.add(role);
		}

		this.claimSet = new JWTClaimsSet.Builder()
			     .subject(this.name)
			     .issueTime(new Date())
			     .issuer("XWare GmbH")
			     .claim("roles", ls)
			     .build();

		//dummy?
		this.roles = new HashSet<>();
		this.roles.add(Role.New(role));
	}

	@Override
	public String name() {
		if (getAzureUser() != null) {
			return getAzureUser().name();
		}
		return this.name;
	}
	@Override
	public Set<Role> roles() {
		if (getAzureUser() != null) {
			return getAzureUser().roles();
		}
		return this.roles;
	}

	public JWTClaimsSet getClaimSet()
	{
		if (getAzureUser() != null) {
			return getAzureUser().getClaimSet();
		}


		return this.claimSet;
	}

	public AzureUser getAzureUser() {
		return this.aadUser;
	}

	public AppUser getDbUser() {
		return this.dbUser;
	}

	public void setAzureUser(final AzureUser currentUser) {
		this.aadUser = currentUser;
	}

	public CostAccount getAssignedAccount() {
		return this.cst;
	}

	public void setAssignedAccount(final CostAccount cst) {
		this.cst = cst;
	}

}
