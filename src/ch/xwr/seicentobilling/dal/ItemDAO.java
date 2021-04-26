
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.Item_;

/**
 * Home object for domain model class Item.
 *
 * @see Item
 */
public class ItemDAO extends JPADAO<Item, Long> {
	public ItemDAO() {
		super(Item.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Item> findByIdent(final String ident) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<String> identParameter = criteriaBuilder.parameter(String.class, "ident");

		final CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);

		final Root<Item> root = criteriaQuery.from(Item.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Item_.itmIdent), identParameter));

		final TypedQuery<Item> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(identParameter, ident);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Item> findAllSortedByName() {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);

		final Root<Item> root = criteriaQuery.from(Item.class);

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(Item_.itmName)));

		final TypedQuery<Item> query = entityManager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Item> findAllActiveSortedByName() {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);

		final Root<Item> root = criteriaQuery.from(Item.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Item_.itmState), criteriaBuilder.literal(1)));

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(Item_.itmName)));

		final TypedQuery<Item> query = entityManager.createQuery(criteriaQuery);
		return query.getResultList();
	}
}