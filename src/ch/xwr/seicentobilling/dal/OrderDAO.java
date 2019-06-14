
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Order_;

/**
 * Home object for domain model class Order.
 *
 * @see Order
 */
public class OrderDAO extends JPADAO<Order, Long> {
	public OrderDAO() {
		super(Order.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Order> findByCustomer(final Customer dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Customer> daoParameter = criteriaBuilder.parameter(Customer.class, "dao");

		final CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);

		final Root<Order> root = criteriaQuery.from(Order.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Order_.customer), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Order_.ordBillDate)));

		final TypedQuery<Order> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}
}