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

	private SeicentoUserXml xmluser = null;

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

	public SeicentoUserXml getXmlUser(final String name) {
		setXmlUser(name);
		return this.xmluser;
	}

	private boolean validateUser(final CredentialsUsernamePassword credentials, final SeicentoUsersXml xml) {
		setXmlUser(credentials.username());
		if (this.xmluser == null) {
			return false;
		}

		final byte[] bar = this.xmluser.getPassword().getBytes(StandardCharsets.UTF_8);
		if (Arrays.equals(bar, credentials.password())) {
			LOG.debug("User " + this.xmluser.getName() + " authenticated");
			return true;
		}

		LOG.warn("Invalid User or Password");
		return false;
	}

	private void setXmlUser(final String name) {
		this.xmluser = null;
		final SeicentoUsersXml xml = readUsersFromXml();

		if (xml == null) {
			return;
		}

		for (int i = 0; i < xml.getUsers().size(); i++) {
			final SeicentoUserXml user = xml.getUsers().get(i);
			if (name.equalsIgnoreCase(user.getName())) {
				this.xmluser = user;
			}
		}
	}
}
