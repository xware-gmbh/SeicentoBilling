package ch.xwr.seicentobilling.business.helper;

import java.net.URI;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.vaadin.server.VaadinServlet;
import com.xdev.server.aa.openid.azure.AzurePopupConfig;


public class AzureHelper {
	public static final String CALLBACK_URI = "appCallbackUri";
	public static final String TENANT_ID ="tenantid";

	public void setCallBackUri(final URI page) {
		final ServletContext context = VaadinServlet.getCurrent().getServletContext();
		String cbs = page.getScheme() + "://" + page.getHost();
		if (page.getPort() > 0) {
			cbs = cbs + ":" +  page.getPort();
		}
		cbs = cbs + page.getPath();
		cbs = cbs.replace("#!", "");

		context.setAttribute(CALLBACK_URI, cbs);
		//context.setAttribute(CALLBACK_URI, this.getPage().getLocation().toString());
	}

	public AzurePopupConfig getAzureConfig() {
		final String enval = System.getenv(TENANT_ID);
		AzurePopupConfig config = null;

		if (enval != null && enval.length() > 3) {
			config = AzurePopupConfig.getAzurePopupConfig(getAzureEnvProps());
		} else {
			config = AzurePopupConfig.getAzurePopupConfig(getCallBackUri());
		}

		return config;
	}

	private URI getCallBackUri() {
		URI uri = null;
		final ServletContext context = VaadinServlet.getCurrent().getServletContext();
		try {
			uri = new URI((String)context.getAttribute(CALLBACK_URI));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return uri;
	}

	private Properties getAzureEnvProps() {
		final Properties prop = new Properties();
		prop.put(TENANT_ID, getEnvValue(TENANT_ID));
		prop.put("clientid", getEnvValue("clientid"));
		prop.put("clientkey", getEnvValue("clientkey"));
		prop.put("callbackURL", getCallBackUri().toString());
		return prop;
	}

	private String getEnvValue(final String key) {
		final String value = System.getenv(key);
		if (value == null) {
			return "";
		}
		return value;
	}


}
