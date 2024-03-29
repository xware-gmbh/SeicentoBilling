
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLine_;

/**
 * Home object for domain model class ProjectLine.
 *
 * @see ProjectLine
 */
public class ProjectLineDAO extends JPADAO<ProjectLine, Long> {
	public ProjectLineDAO() {
		super(ProjectLine.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<ProjectLine> findByPeriode(final Periode dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Periode> daoParameter = criteriaBuilder.parameter(Periode.class, "dao");

		final CriteriaQuery<ProjectLine> criteriaQuery = criteriaBuilder.createQuery(ProjectLine.class);

		final Root<ProjectLine> root = criteriaQuery.from(ProjectLine.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(ProjectLine_.periode), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(ProjectLine_.prlReportDate)),
				criteriaBuilder.asc(root.get(ProjectLine_.prlTimeFrom)));

		final TypedQuery<ProjectLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<ProjectLine> findByProject(final Project dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Project> daoParameter = criteriaBuilder.parameter(Project.class, "dao");

		final CriteriaQuery<ProjectLine> criteriaQuery = criteriaBuilder.createQuery(ProjectLine.class);

		final Root<ProjectLine> root = criteriaQuery.from(ProjectLine.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(ProjectLine_.project), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(ProjectLine_.prlReportDate)),
				criteriaBuilder.asc(root.get(ProjectLine_.prlTimeFrom)));

		final TypedQuery<ProjectLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

	public int disableTrigger(final boolean disable) {
		final String template = "ALTER TABLE %s %s TRIGGER %s";
		final String sql = String.format(template, ProjectLine.class.getSimpleName(), disable?"DISABLE":"ENABLE", "tr_seicento_postSavePLine");

	    final Query nativeQuery = em().createNativeQuery(sql);
	    final int ires = nativeQuery.executeUpdate();
	    return ires;
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<ProjectLine> findByPeriodeAndProject(final Periode dao, final Project pro) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Periode> daoParameter = criteriaBuilder.parameter(Periode.class, "dao");
		final ParameterExpression<Project> proParameter = criteriaBuilder.parameter(Project.class, "pro");

		final CriteriaQuery<ProjectLine> criteriaQuery = criteriaBuilder.createQuery(ProjectLine.class);

		final Root<ProjectLine> root = criteriaQuery.from(ProjectLine.class);

		criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(ProjectLine_.periode), daoParameter),
				criteriaBuilder.equal(root.get(ProjectLine_.project), proParameter)));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(ProjectLine_.prlReportDate)),
				criteriaBuilder.asc(root.get(ProjectLine_.prlTimeFrom)));

		final TypedQuery<ProjectLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		query.setParameter(proParameter, pro);
		return query.getResultList();
	}
}