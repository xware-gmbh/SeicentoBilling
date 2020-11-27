package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.dal.RowTextDAO;

/**
 * RowText
 */
@DAO(RowTextDAO.class)
@Caption("{%txtFreetext}")
@Entity
@Table(name = "RowText", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = {
		"txtobjId", "txtlngId", "txtNumber" }))
public class RowText implements java.io.Serializable {

	private Long txtId;
	private Language language;
	private RowObject rowObject;
	private Integer txtNumber;
	private String txtFreetext;
	private LovState.State txtState;

	public RowText() {
	}

	@Caption("TxtId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "txtId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getTxtId() {
		return this.txtId;
	}

	public void setTxtId(final Long txtId) {
		this.txtId = txtId;
	}

	@Caption("Language")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "txtlngId", nullable = false, columnDefinition = "bigint")
	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(final Language language) {
		this.language = language;
	}

	@Caption("RowObject")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "txtobjId", nullable = false, columnDefinition = "bigint")
	public RowObject getRowObject() {
		return this.rowObject;
	}

	public void setRowObject(final RowObject rowObject) {
		this.rowObject = rowObject;
	}

	@Caption("TxtNumber")
	@Column(name = "txtNumber", columnDefinition = "int")
	public Integer getTxtNumber() {
		return this.txtNumber;
	}

	public void setTxtNumber(final Integer txtNumber) {
		this.txtNumber = txtNumber;
	}

	@Caption("TxtFreetext")
	@Lob
	@Column(name = "txtFreetext", columnDefinition = "ntext")
	public String getTxtFreetext() {
		return this.txtFreetext;
	}

	public void setTxtFreetext(final String txtFreetext) {
		this.txtFreetext = txtFreetext;
	}

	@Caption("TxtState")
	@Column(name = "txtState", columnDefinition = "smallint")
	public LovState.State getTxtState() {
		return this.txtState;
	}

	public void setTxtState(final LovState.State active) {
		this.txtState = active;
	}

}
