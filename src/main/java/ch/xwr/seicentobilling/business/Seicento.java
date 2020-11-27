
package ch.xwr.seicentobilling.business;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;

import com.rapidclipse.framework.security.authorization.Subject;
import com.vaadin.flow.server.VaadinSession;

import ch.xwr.seicentobilling.business.auth.SeicentoUser;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.entities.CostAccount;


public class Seicento
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Seicento.class);

	public static String getUserName()
	{
		final Subject sub = com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute(Subject.class);
		
		// if(sub != null && sub instanceof AzureUser)
		// {
		// final AzureUser usr = (AzureUser)sub;
		// return usr.name();
		// }

		if(sub != null && sub instanceof SeicentoUser)
		{
			final SeicentoUser usr = (SeicentoUser)sub;
			return usr.name();
		}

		return "unknown";
	}

	public static SeicentoUser getSeicentoUser()
	{
		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);

		if(sub != null && sub instanceof SeicentoUser)
		{
			final SeicentoUser usr = (SeicentoUser)sub;
			return usr;
		}

		return null;
	}

	public static boolean hasRole(final String roleName)
	{
		final Subject sub = VaadinSession.getCurrent().getAttribute(Subject.class);
		if(sub == null)
		{
			return true; // Dev Mode 1
		}

		// if (sub instanceof MockupUser)
		// {
		// return true;
		// }

		/*
		 * if(sub instanceof SeicentoUser)
		 * {
		 * final SeicentoUser usr = (SeicentoUser)sub;
		 *
		 * if(usr != null && usr.getClaimSet().getClaims() != null)
		 * {
		 *
		 * @SuppressWarnings("unchecked")
		 * final List<String> roleNames = (List<String>)usr.getClaimSet().getClaim("roles");
		 * if(roleNames != null)
		 * {
		 * for(final String role : roleNames)
		 * {
		 * if(roleName.equalsIgnoreCase(role))
		 * {
		 * return true;
		 * }
		 * }
		 * }
		 * else
		 * {
		 * System.out.println("*** Azure User Roles are empty: ");
		 * }
		 * }
		 *
		 * }
		 */

		return true;
	}

	public static CostAccount getLoggedInCostAccount(final String name)
	{
		final CostAccountDAO dao  = new CostAccountDAO();
		CostAccount          bean = null;
		try
		{
			bean = dao.findByName(Seicento.getUserName()).get(0);

		}
		catch(final Exception e)
		{
			// ignore
			Seicento.LOG.warn("No User Found on CostAccount with Name: " + Seicento.getUserName());

			// fallback DEV. Take first active account found
			final List<CostAccount> lst = dao.findAll();
			for(final Iterator<CostAccount> iterator = lst.iterator(); iterator.hasNext();)
			{
				final CostAccount csaObj = iterator.next();

				if(csaObj.getCsaState().equals(LovState.State.active))
				{
					return csaObj;
				}
			}
		}
		return bean;
	}

	public static CostAccount getLoggedInCostAccount()
	{
		return Seicento.getLoggedInCostAccount(Seicento.getUserName());
	}

	public static void removeGelfAppender()
	{
		Appender                      ap   = null;
		final org.apache.log4j.Logger root = LogManager.getRootLogger();
		final Enumeration<?>          lsa  = root.getAllAppenders();
		while(lsa.hasMoreElements())
		{
			ap = (Appender)lsa.nextElement();
			if(ap.getName().equalsIgnoreCase("gelf"))
			{
				root.removeAppender(ap);
			}
		}
	}

	public static long getMemory()
	{
		System.gc();
		final Runtime rt     = Runtime.getRuntime();
		final long    usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
		Seicento.LOG.debug("memory usage MB: " + usedMB);
		return usedMB;
	}

	public static String getLoginMethod()
	{
		String lm = "azure";
		if(System.getenv("SEICENTO_LOGIN_METHOD") != null)
		{
			lm = System.getenv("SEICENTO_LOGIN_METHOD");
		}

		return lm;
	}

	public static String calculateThemeHeight(float height, String theme)
	{
		theme = theme.toLowerCase();

		int x = 0;

		switch(theme)
		{
			case "facebook":
				x = Math.round(height / 5);
				height = height - x;
			break;

			case "darksb":
				x = Math.round(height / 20);
				height = height + x;

			break;

			default:
			break;
		}

		return "" + height;
	}

}
