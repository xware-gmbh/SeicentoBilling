
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.ItemGroup;

/**
 * Home object for domain model class ItemGroup.
 * 
 * @see ItemGroup
 */
public class ItemGroupDAO extends JPADAO<ItemGroup, Long> {
	public ItemGroupDAO() {
		super(ItemGroup.class);
	}
}