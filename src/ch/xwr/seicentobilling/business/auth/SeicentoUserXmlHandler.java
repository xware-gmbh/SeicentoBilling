package ch.xwr.seicentobilling.business.auth;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jfree.util.Log;

import com.xdev.security.authentication.CredentialsUsernamePassword;

public class SeicentoUserXmlHandler {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SeicentoUserXmlHandler.class);

	private SeicentoUsersXml readUsersFromXml() {
		SeicentoUsersXml xml = null;

		try {
			final String url = this.getClass().getClassLoader().getResource("SeicentoUsers.xml").getPath();
			final File file = new File(url);

			final JAXBContext jaxbContext = JAXBContext.newInstance(SeicentoUsersXml.class);
		    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    xml = (SeicentoUsersXml) jaxbUnmarshaller.unmarshal(file);

		    return xml;

		} catch (final JAXBException e) {
			LOG.error("Lokale User Datenbank SeicentoUsers.xml konnte nicht gelesen werden");
			Log.error(e);
		}

		return null;
	}

	public boolean isAuthenticated(final CredentialsUsernamePassword credentials) {
		final SeicentoUsersXml xml = readUsersFromXml();
		return (validateUser(credentials, xml));
	}

	private boolean validateUser(final CredentialsUsernamePassword credentials, final SeicentoUsersXml xml) {
		if (xml == null) {
			return false;
		}

		for (int i = 0; i < xml.getUsers().size(); i++) {
			final SeicentoUserXml user = xml.getUsers().get(i);
			if (credentials.username().equalsIgnoreCase(user.getName())) {
				final byte[] bar = user.getPassword().getBytes(StandardCharsets.UTF_8);
				if (Arrays.equals(bar, credentials.password())) {
					LOG.debug("User " + user.getName() + " authenticated");
					return true;
				}
			}
		}

		LOG.warn("Invalid User or Password");
		return false;
	}
}
