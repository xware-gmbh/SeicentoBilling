package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;

import ch.xwr.seicentobilling.dal.LanguageDAO;

/**
 * Language
 */
@DAO(daoClass = LanguageDAO.class)
@Caption("{%lngName}")
@Entity
@Table(name = "Language", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "lngCode"))
public class Language implements java.io.Serializable {

	private Long lngId;
	private int lngCode;
	private String lngName;
	private String lngIsocode;
	private String lngKeyboard;
	private Boolean lngDefault;
	private Short lngState;
	private Set<RowText> rowTexts = new HashSet<>(0);
	private Set<RowLabel> rowLabels = new HashSet<>(0);

	public Language() {
	}

	@Caption("LngId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "lngId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getLngId() {
		return this.lngId;
	}

	public void setLngId(final Long lngId) {
		this.lngId = lngId;
	}

	@Caption("LngCode")
	@Column(name = "lngCode", unique = true, nullable = false, columnDefinition = "int")
	public int getLngCode() {
		return this.lngCode;
	}

	public void setLngCode(final int lngCode) {
		this.lngCode = lngCode;
	}

	@Caption("LngName")
	@Column(name = "lngName", nullable = false, columnDefinition = "nvarchar")
	public String getLngName() {
		return this.lngName;
	}

	public void setLngName(final String lngName) {
		this.lngName = lngName;
	}

	@Caption("LngIsocode")
	@Column(name = "lngIsocode", columnDefinition = "nvarchar")
	public String getLngIsocode() {
		return this.lngIsocode;
	}

	public void setLngIsocode(final String lngIsocode) {
		this.lngIsocode = lngIsocode;
	}

	@Caption("LngKeyboard")
	@Column(name = "lngKeyboard", columnDefinition = "nvarchar")
	public String getLngKeyboard() {
		return this.lngKeyboard;
	}

	public void setLngKeyboard(final String lngKeyboard) {
		this.lngKeyboard = lngKeyboard;
	}

	@Caption("LngDefault")
	@Column(name = "lngDefault", columnDefinition = "bit")
	public Boolean getLngDefault() {
		return this.lngDefault;
	}

	public void setLngDefault(final Boolean lngDefault) {
		this.lngDefault = lngDefault;
	}

	@Caption("LngState")
	@Column(name = "lngState", columnDefinition = "smallint")
	public Short getLngState() {
		return this.lngState;
	}

	public void setLngState(final Short lngState) {
		this.lngState = lngState;
	}

	@Caption("RowTexts")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "language")
	public Set<RowText> getRowTexts() {
		return this.rowTexts;
	}

	public void setRowTexts(final Set<RowText> rowTexts) {
		this.rowTexts = rowTexts;
	}

	@Caption("RowLabels")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "language")
	public Set<RowLabel> getRowLabels() {
		return this.rowLabels;
	}

	public void setRowLabels(final Set<RowLabel> rowLabels) {
		this.rowLabels = rowLabels;
	}

}
