package co.edu.unbosque.advancewars.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.advancewars.service.GameCatalogService;

@RestController
@RequestMapping("/api/catalog")
public class GameCatalogController {
    private final GameCatalogService catalog;
    public GameCatalogController(GameCatalogService catalog) { this.catalog = catalog; }
    @GetMapping("/factions")
    public List<Map<String, Object>> factions() { return catalog.getFactions(); }
    @GetMapping("/commanders")
    public List<Map<String, Object>> commanders() { return catalog.getCommanders(); }
    @GetMapping("/units")
    public List<Map<String, Object>> units() { return catalog.getUnits(); }
}
