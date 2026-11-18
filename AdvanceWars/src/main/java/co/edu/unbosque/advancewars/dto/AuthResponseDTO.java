package co.edu.unbosque.advancewars.dto;

import java.util.Objects;

/**
 * Transporta la respuesta de autenticación después del segundo factor.
 */
public class AuthResponseDTO {

    private String accessToken;

    private String tokenType;

    private UserResponseDTO user;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public AuthResponseDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param accessToken token de acceso
     * @param tokenType tipo de token
     * @param user usuario autenticado
     */
    public AuthResponseDTO(String accessToken, String tokenType, UserResponseDTO user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user;
    }

    /**
     * Obtiene token de acceso.
     * @return token de acceso
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Establece token de acceso.
     * @param accessToken token de acceso
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Obtiene tipo de token.
     * @return tipo de token
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Establece tipo de token.
     * @param tokenType tipo de token
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * Obtiene usuario autenticado.
     * @return usuario autenticado
     */
    public UserResponseDTO getUser() {
        return user;
    }

    /**
     * Establece usuario autenticado.
     * @param user usuario autenticado
     */
    public void setUser(UserResponseDTO user) {
        this.user = user;
    }

    /**
     * Compara los valores de los campos de este DTO.
     * @param object objeto que se compara
     * @return true cuando ambos DTO contienen los mismos valores
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AuthResponseDTO other)) {
            return false;
        }
        return Objects.equals(accessToken, other.accessToken)
                && Objects.equals(tokenType, other.tokenType)
                && Objects.equals(user, other.user);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, user);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "AuthResponseDTO [" + "tokenType=" + tokenType + "]";
    }

}
