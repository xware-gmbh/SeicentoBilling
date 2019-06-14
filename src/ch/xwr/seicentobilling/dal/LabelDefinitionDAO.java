
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.LabelDefinition;
import ch.xwr.seicentobilling.entities.Vat;

/**
 * Home object for domain model class Vat.
 *
 * @see Vat
 */
public class LabelDefinitionDAO extends JPADAO<LabelDefinition, Long> {
	public LabelDefinitionDAO() {
		super(LabelDefinition.class);
	}
}