
package ch.xwr.seicentobilling.dal;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.ItemGroup;

/**
 * Home object for domain model class ItemGroup.
 * 
 * @see ItemGroup
 */
public class ItemGroupDAO extends JpaDataAccessObject.Default<ItemGroup, Long> {
	public ItemGroupDAO() {
		super(ItemGroup.class);
	}
}