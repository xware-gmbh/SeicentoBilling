package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.dal.DatabaseVersionDAO;

/**
 * DatabaseVersion
 */
@DAO(daoClass = DatabaseVersionDAO.class)
@Caption("{%dbvMicro}")
@Entity
@Table(name = "DatabaseVersion", schema = "dbo")
public class DatabaseVersion implements java.io.Serializable {

	private Long dbvId;
	private Integer dbvMajor;
	private Integer dbvMinor;
	private String dbvMicro;
	private Date dbvReleased;
	private String dbvDescription;
	private Short dbvState;
	private Set<RowObject> rowObjects = new HashSet<>(0);

	public DatabaseVersion() {
	}

	@Caption("DbvId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "dbvId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getDbvId() {
		return this.dbvId;
	}

	public void setDbvId(final Long dbvId) {
		this.dbvId = dbvId;
	}

	@Caption("DbvMajor")
	@Column(name = "dbvMajor", columnDefinition = "int")
	public Integer getDbvMajor() {
		return this.dbvMajor;
	}

	public void setDbvMajor(final Integer dbvMajor) {
		this.dbvMajor = dbvMajor;
	}

	@Caption("DbvMinor")
	@Column(name = "dbvMinor", columnDefinition = "int")
	public Integer getDbvMinor() {
		return this.dbvMinor;
	}

	public void setDbvMinor(final Integer dbvMinor) {
		this.dbvMinor = dbvMinor;
	}

	@Caption("DbvMicro")
	@Column(name = "dbvMicro", columnDefinition = "nvarchar")
	public String getDbvMicro() {
		return this.dbvMicro;
	}

	public void setDbvMicro(final String dbvMicro) {
		this.dbvMicro = dbvMicro;
	}

	@Caption("DbvReleased")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dbvReleased", columnDefinition = "datetime", length = 23)
	public Date getDbvReleased() {
		return this.dbvReleased;
	}

	public void setDbvReleased(final Date dbvReleased) {
		this.dbvReleased = dbvReleased;
	}

	@Caption("DbvDescription")
	@Lob
	@Column(name = "dbvDescription", columnDefinition = "ntext")
	public String getDbvDescription() {
		return this.dbvDescription;
	}

	public void setDbvDescription(final String dbvDescription) {
		this.dbvDescription = dbvDescription;
	}

	@Caption("DbvState")
	@Column(name = "dbvState", columnDefinition = "smallint")
	public Short getDbvState() {
		return this.dbvState;
	}

	public void setDbvState(final Short dbvState) {
		this.dbvState = dbvState;
	}

	@Caption("RowObjects")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "databaseVersion")
	public Set<RowObject> getRowObjects() {
		return this.rowObjects;
	}

	public void setRowObjects(final Set<RowObject> rowObjects) {
		this.rowObjects = rowObjects;
	}

}
