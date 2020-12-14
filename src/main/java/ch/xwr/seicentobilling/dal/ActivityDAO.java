
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;

import ch.xwr.seicentobilling.entities.Activity;
import ch.xwr.seicentobilling.entities.Activity_;
import ch.xwr.seicentobilling.entities.Customer;


/**
 * Home object for domain model class Bank.
 *
 * @see Acitivity
 */
public class ActivityDAO extends JpaDataAccessObject.Default<Activity, Long>
{
	public ActivityDAO()
	{
		super(Activity.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Activity> findByCustomer(final Customer dao)
	{
		final CriteriaQuery<Activity> criteriaQuery = this.findByCustomerQuery();
		
		final EntityManager entityManager = this.em();
		
		final TypedQuery<Activity> query = entityManager.createQuery(criteriaQuery);
		query.setParameter("dao", dao);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public CriteriaQuery<Activity> findByCustomerQuery()
	{
		final EntityManager entityManager = this.em();
		
		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		final ParameterExpression<Customer> daoParameter = criteriaBuilder.parameter(Customer.class, "dao");
		
		final CriteriaQuery<Activity> criteriaQuery = criteriaBuilder.createQuery(Activity.class);
		
		final Root<Activity> root = criteriaQuery.from(Activity.class);
		
		criteriaQuery.where(criteriaBuilder.equal(root.get(Activity_.customer), daoParameter));
		
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Activity_.actDate)),
			criteriaBuilder.asc(root.get(Activity_.actType)));
		
		return criteriaQuery;
	}
}