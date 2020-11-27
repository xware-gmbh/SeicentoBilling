package ch.xwr.seicentobilling.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.rapidclipse.framework.server.data.DAO;
import com.rapidclipse.framework.server.resources.Caption;

import ch.xwr.seicentobilling.dal.ItemGroupDAO;

/**
 * ItemGroup
 */
@DAO(ItemGroupDAO.class)
@Caption("{%itgName}")
@Entity
@Table(name = "ItemGroup", schema = "dbo", uniqueConstraints = @UniqueConstraint(columnNames = "itgNumber"))
public class ItemGroup implements java.io.Serializable {

	private Long itgId;
	private ItemGroup itemGroup;
	private Integer itgNumber;
	private String itgName;
	private Short itgState;
	private Set<Item> items = new HashSet<>(0);
	private Set<ItemGroup> itemGroups = new HashSet<>(0);

	public ItemGroup() {
	}

	@Caption("ItgId")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "itgId", unique = true, nullable = false, columnDefinition = "bigint identity")
	public Long getItgId() {
		return this.itgId;
	}

	public void setItgId(final Long itgId) {
		this.itgId = itgId;
	}

	@Caption("ItemGroup")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "itgitgParent", columnDefinition = "bigint")
	public ItemGroup getItemGroup() {
		return this.itemGroup;
	}

	public void setItemGroup(final ItemGroup itemGroup) {
		this.itemGroup = itemGroup;
	}

	@Caption("ItgNumber")
	@Column(name = "itgNumber", unique = true, columnDefinition = "int")
	public Integer getItgNumber() {
		return this.itgNumber;
	}

	public void setItgNumber(final Integer itgNumber) {
		this.itgNumber = itgNumber;
	}

	@Caption("ItgName")
	@Column(name = "itgName", columnDefinition = "nvarchar")
	public String getItgName() {
		return this.itgName;
	}

	public void setItgName(final String itgName) {
		this.itgName = itgName;
	}

	@Caption("ItgState")
	@Column(name = "itgState", columnDefinition = "smallint")
	public Short getItgState() {
		return this.itgState;
	}

	public void setItgState(final Short itgState) {
		this.itgState = itgState;
	}

	@Caption("Items")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "itemGroup")
	public Set<Item> getItems() {
		return this.items;
	}

	public void setItems(final Set<Item> items) {
		this.items = items;
	}

	@Caption("ItemGroups")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "itemGroup")
	public Set<ItemGroup> getItemGroups() {
		return this.itemGroups;
	}

	public void setItemGroups(final Set<ItemGroup> itemGroups) {
		this.itemGroups = itemGroups;
	}

}
