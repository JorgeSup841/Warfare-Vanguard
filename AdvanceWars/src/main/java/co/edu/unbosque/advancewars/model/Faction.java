package co.edu.unbosque.advancewars.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "faction")
public class Faction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String code;

	@Column(nullable = false, unique = true, length = 50)
	private String name;

	@Column(length = 1000)
	private String description;

	protected Faction() {
	}

	public Faction(String code, String name, String description) {
		this.code = code;
		this.name = name;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Faction other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Faction.class.hashCode();
	}

	@Override
	public String toString() {
		return "Faction{" + "id=" + id + ", code='" + code + '\'' + ", name='" + name + '\'' + ", description='"
				+ description + '\'' + '}';
	}
}
