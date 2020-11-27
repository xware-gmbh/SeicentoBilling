
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.Language;
import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;

/**
 * Home object for domain model class Language.
 * 
 * @see Language
 */
public class LanguageDAO extends JpaDataAccessObject.Default<Language, Long> {
	public LanguageDAO() {
		super(Language.class);
	}
}