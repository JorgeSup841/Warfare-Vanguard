package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.advancewars.controller.AdminController;
import co.edu.unbosque.advancewars.controller.AuthController;
import co.edu.unbosque.advancewars.controller.GameCatalogController;
import co.edu.unbosque.advancewars.controller.GameController;
import co.edu.unbosque.advancewars.controller.ReportController;
import co.edu.unbosque.advancewars.controller.SessionController;
import co.edu.unbosque.advancewars.controller.UserController;
import co.edu.unbosque.advancewars.model.User;

/** Garantiza que ninguna ruta REST existente desaparezca durante la revisión DeepSource. */
class ControllerRouteCompatibilityTest {

    private static final Map<Class<?>, Set<String>> ROUTES = Map.of(
            AuthController.class, Set.of(
                    "GET /api/auth/me", "POST /api/auth/register", "POST /api/auth/login",
                    "POST /api/auth/verify-email", "POST /api/auth/verify-otp",
                    "POST /api/auth/resend-otp", "POST /api/auth/forgot-password",
                    "POST /api/auth/reset-password"),
            UserController.class, Set.of(
                    "GET /api/users/me", "GET /api/users", "GET /api/users/{id}",
                    "PATCH /api/users/{id}/role", "PATCH /api/users/{id}/enabled"),
            AdminController.class, Set.of(
                    "GET /api/admin/dashboard", "GET /api/admin/users", "GET /api/admin/users/{id}",
                    "POST /api/admin/users", "PATCH /api/admin/users/{id}/role",
                    "PATCH /api/admin/users/{id}/enabled", "GET /api/admin/games",
                    "GET /api/admin/games/{id}", "GET /api/admin/games/{id}/map",
                    "GET /api/admin/games/{id}/units", "GET /api/admin/games/{id}/history",
                    "DELETE /api/admin/games/{id}", "GET /api/admin/sessions",
                    "GET /api/admin/sessions/active", "DELETE /api/admin/sessions/{id}",
                    "GET /api/admin/activity", "GET /api/admin/users/{id}/activity"),
            GameController.class, Set.of(
                    "POST /api/games", "GET /api/games", "GET /api/games/{id}/map",
                    "GET /api/games/{id}", "PUT /api/games/{id}/players/{order}/selection",
                    "GET /api/games/{id}/history", "POST /api/games/{id}/power",
                    "POST /api/games/{id}/start", "POST /api/games/{id}/end-turn",
                    "GET /api/games/{id}/units", "GET /api/games/{id}/units/{unitId}/reachable",
                    "POST /api/games/{id}/units/{unitId}/move",
                    "POST /api/games/{id}/units/{unitId}/attack",
                    "POST /api/games/{id}/units/{unitId}/capture",
                    "POST /api/games/{id}/units/produce"),
            GameCatalogController.class, Set.of(
                    "GET /api/catalog/factions", "GET /api/catalog/commanders", "GET /api/catalog/units"),
            ReportController.class, Set.of(
                    "GET /api/games/{gameId}/report.pdf", "GET /api/admin/games/{gameId}/report.pdf"),
            SessionController.class, Set.of(
                    "POST /api/sessions/heartbeat", "POST /api/sessions/logout"));

    /** Asegura que los siete controladores y sus 52 rutas permanezcan publicados. */
    @Test
    void preservesAllOriginalControllerRoutes() {
        assertEquals(7, ROUTES.size());
        int total = 0;
        for (Map.Entry<Class<?>, Set<String>> item : ROUTES.entrySet()) {
            Class<?> controller = item.getKey();
            assertTrue(controller.isAnnotationPresent(RestController.class), controller.getSimpleName());
            assertEquals(item.getValue(), mappedEndpoints(controller), controller.getSimpleName());
            total += item.getValue().size();
        }
        assertEquals(52, total);
    }

    /** Los controladores delegan: no tienen repositorios ni reciben entidades JPA como argumentos REST. */
    @Test
    void controllersDoNotDependOnRepositoriesOrJPARequestArguments() {
        for (Class<?> controller : ROUTES.keySet()) {
            for (Field field : controller.getDeclaredFields()) {
                assertFalse(field.getType().getPackageName().endsWith(".repository"),
                        controller.getSimpleName() + "." + field.getName());
            }
            for (Method method : controller.getDeclaredMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    assertFalse(parameter.getType() == User.class,
                            controller.getSimpleName() + "." + method.getName());
                }
            }
        }
    }

    /** Recopila las rutas efectivas usando los mismos metadatos de Spring MVC. */
    private Set<String> mappedEndpoints(Class<?> controller) {
        String root = "";
        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            root = classMapping.value()[0];
        }
        Set<String> result = new HashSet<>();
        for (Method method : controller.getDeclaredMethods()) {
            GetMapping get = method.getAnnotation(GetMapping.class);
            if (get != null) add(result, "GET", root, get.value(), get.path());
            PostMapping post = method.getAnnotation(PostMapping.class);
            if (post != null) add(result, "POST", root, post.value(), post.path());
            PutMapping put = method.getAnnotation(PutMapping.class);
            if (put != null) add(result, "PUT", root, put.value(), put.path());
            PatchMapping patch = method.getAnnotation(PatchMapping.class);
            if (patch != null) add(result, "PATCH", root, patch.value(), patch.path());
            DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
            if (delete != null) add(result, "DELETE", root, delete.value(), delete.path());
        }
        return result;
    }

    /** Convierte una anotación de Spring MVC en un conjunto de verbos y rutas normalizados. */
    private void add(Set<String> result, String verb, String root, String[] values, String[] paths) {
        String[] mappings = values.length > 0 ? values : (paths.length > 0 ? paths : new String[] {""});
        for (String mapping : mappings) {
            String path = mapping.startsWith("/api/") ? mapping : root + mapping;
            result.add(verb + " " + path);
        }
    }
}
