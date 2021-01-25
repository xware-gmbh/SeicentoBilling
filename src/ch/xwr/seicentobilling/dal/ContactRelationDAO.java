
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.ContactRelation;
import ch.xwr.seicentobilling.entities.ContactRelation_;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Vat;

/**
 * Home object for domain model class Vat.
 *
 * @see Vat
 */
public class ContactRelationDAO extends JPADAO<ContactRelation, Long> {
	public ContactRelationDAO() {
		super(ContactRelation.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<ContactRelation> findByCustomer(final Customer dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Customer> daoParameter = criteriaBuilder.parameter(Customer.class, "dao");

		final CriteriaQuery<ContactRelation> criteriaQuery = criteriaBuilder.createQuery(ContactRelation.class);

		final Root<ContactRelation> root = criteriaQuery.from(ContactRelation.class);

		criteriaQuery.where(criteriaBuilder.or(criteriaBuilder.equal(root.get(ContactRelation_.customerOne), daoParameter),
				criteriaBuilder.equal(root.get(ContactRelation_.customerTwo), daoParameter)));

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(ContactRelation_.corTypeOne)));

		final TypedQuery<ContactRelation> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

}