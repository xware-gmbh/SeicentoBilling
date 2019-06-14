
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.Language;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class Language.
 * 
 * @see Language
 */
public class LanguageDAO extends JPADAO<Language, Long> {
	public LanguageDAO() {
		super(Language.class);
	}
}