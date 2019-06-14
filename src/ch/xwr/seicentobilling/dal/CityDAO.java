
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.City;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class City.
 * 
 * @see City
 */
public class CityDAO extends JPADAO<City, Long> {
	public CityDAO() {
		super(City.class);
	}
}