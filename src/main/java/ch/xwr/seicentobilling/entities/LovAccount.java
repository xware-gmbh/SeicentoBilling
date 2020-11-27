package ch.xwr.seicentobilling.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.dal.LovAccountDAO;

/**
 * Bank
 */
@DAO(LovAccountDAO.class)
@Caption("LovAccount")
@Entity
//@Table(name = "Bank", schema = "dbo", catalog = "seicento")
public class LovAccount implements java.io.Serializable {
	private String id;
	private String name;

	public LovAccount() {
	}

	public LovAccount(final String value, final String caption) {
		this.setId(value);
		this.setName(caption);
	}


	@Caption("id")
	@Id
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique=true, columnDefinition = "varchar", length = 50)
	@Transient
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Caption("name")
	@Column(name = "name", columnDefinition = "varchar", length = 50)
	@Transient
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
