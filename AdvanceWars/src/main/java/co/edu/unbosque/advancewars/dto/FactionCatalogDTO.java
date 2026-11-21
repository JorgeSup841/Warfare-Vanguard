package co.edu.unbosque.advancewars.dto;


/** Contrato tipado del catalogo del juego, sin entidades JPA en las respuestas REST. */
public class FactionCatalogDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String color;

    /** Constructor requerido por serializadores y herramientas de mapeo. */
    public FactionCatalogDTO() {
    }

    /** Crea un DTO de catalogo con los valores de la capa de servicio. */
    public FactionCatalogDTO(Long id, String code, String name, String description, String color) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.color = color;
    }

    /** Obtiene id. */
    public Long getId() {
        return id;
    }

    /** Actualiza id. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene code. */
    public String getCode() {
        return code;
    }

    /** Actualiza code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Obtiene name. */
    public String getName() {
        return name;
    }

    /** Actualiza name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene description. */
    public String getDescription() {
        return description;
    }

    /** Actualiza description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Obtiene color. */
    public String getColor() {
        return color;
    }

    /** Actualiza color. */
    public void setColor(String color) {
        this.color = color;
    }

}
