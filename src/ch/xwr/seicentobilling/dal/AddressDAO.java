
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Address;
import ch.xwr.seicentobilling.entities.Address_;
import ch.xwr.seicentobilling.entities.Customer;

/**
 * Home object for domain model class Bank.
 *
 * @see Acitivity
 */
public class AddressDAO extends JPADAO<Address, Long> {
	public AddressDAO() {
		super(Address.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Address> findByCustomer(final Customer dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Customer> daoParameter = criteriaBuilder.parameter(Customer.class, "dao");

		final CriteriaQuery<Address> criteriaQuery = criteriaBuilder.createQuery(Address.class);

		final Root<Address> root = criteriaQuery.from(Address.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Address_.customer), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Address_.adrValidFrom)));

		final TypedQuery<Address> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Address> findByCustomerAndType(final Customer dao, final Enum type) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Customer> daoParameter = criteriaBuilder.parameter(Customer.class, "dao");
		final ParameterExpression<Enum> typeParameter = criteriaBuilder.parameter(Enum.class, "type");

		final CriteriaQuery<Address> criteriaQuery = criteriaBuilder.createQuery(Address.class);

		final Root<Address> root = criteriaQuery.from(Address.class);

		criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(Address_.customer), daoParameter),
				criteriaBuilder.equal(root.get(Address_.adrType), typeParameter)));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Address_.adrValidFrom)));

		final TypedQuery<Address> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		query.setParameter(typeParameter, type);
		return query.getResultList();
	}
}