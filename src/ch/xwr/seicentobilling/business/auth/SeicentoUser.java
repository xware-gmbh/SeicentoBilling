package ch.xwr.seicentobilling.business.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
				bean.setUsername(this.name);
				bean.setUsrFullName(this.name);
				bean.setUsrState(LovState.State.active);

				//auto create user if it does not exist
				new AppUserDAO().save(bean);

			}
		}


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
