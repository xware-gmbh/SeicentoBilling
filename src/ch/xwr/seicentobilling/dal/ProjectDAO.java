
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.Project_;

/**
 * Home object for domain model class Project.
 *
 * @see Project
 */
public class ProjectDAO extends JPADAO<Project, Long> {
	public ProjectDAO() {
		super(Project.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Project> findByName(final String name) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class, "name");

		final CriteriaQuery<Project> criteriaQuery = criteriaBuilder.createQuery(Project.class);

		final Root<Project> root = criteriaQuery.from(Project.class);

		criteriaQuery.where(criteriaBuilder.like(root.get(Project_.proName), nameParameter));

		final TypedQuery<Project> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(nameParameter, name);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Project> findAllActive() {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final CriteriaQuery<Project> criteriaQuery = criteriaBuilder.createQuery(Project.class);

		final Root<Project> root = criteriaQuery.from(Project.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Project_.proState), criteriaBuilder.literal(1)));

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(Project_.proName)));

		final TypedQuery<Project> query = entityManager.createQuery(criteriaQuery);
		return query.getResultList();
	}
}