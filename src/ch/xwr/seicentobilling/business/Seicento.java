package ch.xwr.seicentobilling.business;

import java.util.Iterator;
import java.util.List;

import com.vaadin.server.VaadinSession;
import com.xdev.security.authorization.Subject;
import com.xdev.server.aa.openid.auth.AzureUser;

import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.entities.CostAccount;

public class Seicento {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Seicento.class);

	public static String getUserName() {
		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);

		if (sub != null && sub instanceof AzureUser)
		{
			final AzureUser usr = (AzureUser) sub;
			return usr.name();
		}

		if (sub != null && sub instanceof MockupUser)
		{
			final MockupUser usr = (MockupUser) sub;
			return usr.name();
		}


		return "unknown";
	}

	public static boolean hasRole(final String roleName) {
		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);
		if (sub == null) {
			return true;  //Dev Mode 1
		}

		if (sub instanceof MockupUser)
		{
			return true;
		}

		if (sub instanceof AzureUser)
		{
			final AzureUser usr = (AzureUser) sub;

			if (usr != null && usr.getClaimSet().getClaims() != null) {
				@SuppressWarnings("unchecked")
				final List<String> roleNames = (List<String>) usr.getClaimSet().getClaim("roles");
				if (roleNames != null) {
					for (final String role : roleNames) {
						if (roleName.equalsIgnoreCase(role)) {
							return true;
						}
					}
				} else {
					System.out.println("*** Azure User Roles are empty: ");
				}
			}

		}

		return false;
	}

	public static CostAccount getLoggedInCostAccount() {
		final CostAccountDAO dao = new CostAccountDAO();
		CostAccount bean = null;
		try {
			bean = dao.findByName(getUserName()).get(0);

		} catch (final Exception e ) {
			//ignore
			LOG.warn("No User Found on CostAccount with Name: " + getUserName());

			//fallback DEV. Take first active account found
			final List<CostAccount> lst = dao.findAll();
			for (final Iterator<CostAccount> iterator = lst.iterator(); iterator.hasNext();) {
				final CostAccount csaObj = iterator.next();

				if (csaObj.getCsaState().equals(LovState.State.active)) {
					return csaObj;
				}
			}
		}
		return bean;
	}
}
