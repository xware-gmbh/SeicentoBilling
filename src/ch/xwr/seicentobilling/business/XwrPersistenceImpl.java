package ch.xwr.seicentobilling.business;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.servlet.ServletContext;

import com.xdev.persistence.PersistenceManager;
import com.xdev.persistence.PersistenceManager.Factory;
import com.xdev.persistence.PersistenceManager.Implementation;

//@see: https://www.rapidclipse.com/en/forum/index.php/forum/jpa-hibernate/132-jpa-dynamisch
public class XwrPersistenceImpl implements Factory {

		@Override
		public PersistenceManager createPersistenceManager(final ServletContext context) {
			return new XwrImplementation(context);
		}

		public static class XwrImplementation extends Implementation {
			private final Map<String, EntityManagerFactory>	entityManagerFactories	= new HashMap<>();

			public XwrImplementation(final ServletContext servletContext) throws PersistenceException {
				super(servletContext);
			}


			@Override
			public EntityManagerFactory getEntityManagerFactory(final String persistenceUnit)
			{
				EntityManagerFactory factory = this.entityManagerFactories.get(persistenceUnit);
				if(factory == null)
				{
					factory = this.createEntityManagerFactory(persistenceUnit);
					this.entityManagerFactories.put(persistenceUnit,factory);
				}
				return factory;
			}


			private EntityManagerFactory createEntityManagerFactory(final String persistenceUnit)
			{
			    final Map<String, String> properties = new HashMap<>();
				String stage = System.getenv("APP_STAGE");
				if (stage != null) {
					stage = stage.toUpperCase();
					System.setProperty("APP_STAGE", stage);  //for log4J
					final String dburl = System.getenv("DB_URL_" + stage);
					if (dburl != null && dburl.length()>3) {
						System.out.println("Read DB Connection from Environment: " + stage);
						final String url = System.getenv("DB_URL_" + stage);
					    properties.put("javax.persistence.jdbc.url", url);
					    properties.put("javax.persistence.jdbc.user", System.getenv("DB_USR_" + stage));
					    properties.put("javax.persistence.jdbc.password", System.getenv("DB_PWD_" + stage));

					    if (url.startsWith("jdbc:postgresql:")) {
						    properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
						    properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
					    }

					} else {
						System.out.println("Read DB Connection from persistence.xml");
					}
				} else {
					System.out.println("Read DB Connection from persistence.xml");
				}

				return Persistence.createEntityManagerFactory(persistenceUnit, properties);
			}


			@Override
			public void close()
			{
				this.entityManagerFactories.values().forEach(factory -> {
					if(factory.isOpen())
					{
						factory.close();
					}
				});
				this.entityManagerFactories.clear();
			}

		}
}
