package ch.xwr.seicentobilling.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;

public class TheVersion {
	/** Logger initialized */
	private static final Logger _logger = LoggerFactory.getLogger(TheVersion.class);

	private final Properties prop;

	public TheVersion() {
		final InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
		this.prop = new Properties();
		try {
			this.prop.load(resourceAsStream);
		} catch (final IOException e) {
			_logger.error("could not load version.properties");
			_logger.error(e.getStackTrace().toString());
		}

//		System.out.println("Version: " + getEntryById("version"));
//		System.out.println("groupId: " + getEntryById("groupId"));
//		System.out.println("artifactId: " + getEntryById("artifactId"));
	}

	public String getEntryById(final String id) {
		return this.prop.getProperty(id);
	}

}
