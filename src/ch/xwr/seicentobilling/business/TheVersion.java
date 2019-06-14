package ch.xwr.seicentobilling.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TheVersion {

	private final Properties prop;

	public TheVersion() {
		final InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
		this.prop = new Properties();
		try {
			this.prop.load(resourceAsStream);
		} catch (final IOException e) {
			// FIXME: This should be done by using a logging framework like
			// log4j etc.
			e.printStackTrace();
		}

//		System.out.println("Version: " + getEntryById("version"));
//		System.out.println("groupId: " + getEntryById("groupId"));
//		System.out.println("artifactId: " + getEntryById("artifactId"));
	}

	public String getEntryById(final String id) {
		return this.prop.getProperty(id);
	}

}
