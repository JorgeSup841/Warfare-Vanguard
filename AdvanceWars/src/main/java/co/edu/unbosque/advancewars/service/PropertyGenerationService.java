package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.PropertyDefinition;
import co.edu.unbosque.advancewars.model.Tile;
import co.edu.unbosque.advancewars.repository.PropertyDefinitionRepository;

@Service
public class PropertyGenerationService {

    private final PropertyDefinitionRepository definitionRepository;

    public PropertyGenerationService(PropertyDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    // Genera edificios para una partida que ya tiene jugadores y mapa.
    public List<Property> generateProperties(GameState game) {
        GameMap map = game.getGameMap();
        int width = map.getWidth();
        int height = map.getHeight();

        // Crea los tipos de edificios en MySQL si todavia no existen.
        PropertyDefinition headquarters = getDefinition(PropertyType.HEADQUARTERS);
        PropertyDefinition factory = getDefinition(PropertyType.FACTORY);
        PropertyDefinition city = getDefinition(PropertyType.CITY);
        PropertyDefinition airport = getDefinition(PropertyType.AIRPORT);
        PropertyDefinition port = getDefinition(PropertyType.PORT);

        List<Property> properties = new ArrayList<>();
        Set<Position> occupied = new HashSet<>();

        // Orden: jugador 1 arriba izquierda, jugador 2 abajo derecha.
        int[][] bases = {
            {1, 1},
            {width - 2, height - 2},
            {width - 2, 1},
            {1, height - 2}
        };

        // Una base propia para cada jugador.
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            Tile tile = map.findTileAt(bases[i][0], bases[i][1]).orElseThrow();
            tile.setTerrainType(TerrainType.PLAIN);
            properties.add(new Property(headquarters, tile, player));
            occupied.add(tile.getPosition());
        }

        // Mezclamos casillas para variar las propiedades con la semilla.
        List<Tile> candidates = new ArrayList<>(map.getTiles());
        Collections.shuffle(candidates, new Random(game.getSeed() ^ 987654321L));

        // Cada jugador tiene una fabrica neutral cerca de su base.
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Tile nearest = findNearest(candidates, bases[i], occupied, width, height);
            if (nearest == null) {
                throw new IllegalStateException("No hay espacio para una fabrica");
            }
            addProperty(properties, occupied, factory, nearest);
        }

        int area = width * height;

        // Los puertos necesitan agua en una casilla vecina.
        if (area >= 100) {
            addNeutral(properties, occupied, candidates, port,
                    Math.max(1, area / 250), map);
        }

        // Los aeropuertos se generan en mapas medianos y grandes.
        if (area >= 100) {
            addNeutral(properties, occupied, candidates, airport,
                    Math.max(1, area / 160), map);
        }

        // Distribuimos ciudades neutrales por todo el mapa.
        addNeutral(properties, occupied, candidates, city,
                Math.max(1, area / 40), map);

        return properties;
    }

    // Encuentra una casilla disponible lo mas cerca posible de la base.
    private Tile findNearest(List<Tile> candidates, int[] base,
                             Set<Position> occupied, int width, int height) {
        Tile nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Tile tile : candidates) {
            if (!canBuild(tile, occupied, width, height)) {
                continue;
            }

            int distance = Math.abs(tile.getPosition().getX() - base[0])
                    + Math.abs(tile.getPosition().getY() - base[1]);

            if (distance < minDistance) {
                nearest = tile;
                minDistance = distance;
            }
        }
        return nearest;
    }

    // Coloca propiedades neutrales en casillas disponibles.
    private void addNeutral(List<Property> properties, Set<Position> occupied,
                            List<Tile> candidates, PropertyDefinition definition,
                            int quantity, GameMap map) {
        int added = 0;

        // Primero intentamos repartir los edificios. En mapas pequenos
        // permitimos colocarlos mas cerca para no quedarnos sin espacio.
        for (int pass = 0; pass < 2 && added < quantity; pass++) {
            for (Tile tile : candidates) {
                if (added == quantity) {
                    break;
                }
                if (!canBuild(tile, occupied, map.getWidth(), map.getHeight())) {
                    continue;
                }
                if (definition.getType() == PropertyType.PORT && !isCoastal(tile, map)) {
                    continue;
                }
                if (pass == 0 && isCloseToBuilding(tile, properties)) {
                    continue;
                }
                addProperty(properties, occupied, definition, tile);
                added++;
            }
        }
    }

    // Evita que ciudades y edificios neutrales aparezcan amontonados.
    private boolean isCloseToBuilding(Tile candidate, List<Property> properties) {
        int x = candidate.getPosition().getX();
        int y = candidate.getPosition().getY();
        for (Property property : properties) {
            int otherX = property.getTile().getPosition().getX();
            int otherY = property.getTile().getPosition().getY();
            if (Math.abs(x - otherX) + Math.abs(y - otherY) < 3) {
                return true;
            }
        }
        return false;
    }

    // Coloca el edificio y deja su casilla como llanura.
    private void addProperty(List<Property> properties, Set<Position> occupied,
                             PropertyDefinition definition, Tile tile) {
        tile.setTerrainType(TerrainType.PLAIN);
        properties.add(new Property(definition, tile));
        occupied.add(tile.getPosition());
    }

    // Una propiedad no puede ocupar agua, montanas ni posiciones reservadas.
    private boolean canBuild(Tile tile, Set<Position> occupied, int width, int height) {
        Position p = tile.getPosition();
        int x = p.getX();
        int y = p.getY();

        boolean reserved = (x == 1 || x == width - 2)
                && (y == 1 || y == height - 2);

        return !reserved && !occupied.contains(p)
                && tile.getTerrainType() != TerrainType.WATER
                && tile.getTerrainType() != TerrainType.MOUNTAIN;
    }

    // Comprueba que una casilla terrestre tenga agua al lado.
    private boolean isCoastal(Tile tile, GameMap map) {
        for (Tile neighbor : map.findNeighborTiles(tile.getPosition())) {
            if (neighbor.getTerrainType() == TerrainType.WATER) {
                return true;
            }
        }
        return false;
    }

    // Reutiliza la definicion existente o la crea con valores iniciales.
    private PropertyDefinition getDefinition(PropertyType type) {
        Optional<PropertyDefinition> existing = definitionRepository.findByType(type);
        if (existing.isPresent()) {
            return existing.get();
        }

        String name = type.name();
        int income = type == PropertyType.HEADQUARTERS ? 0 : 1000;
        int defense = type == PropertyType.HEADQUARTERS ? 4 : 3;
        Set<UnitCategory> production = new HashSet<>();

        if (type == PropertyType.FACTORY) {
            production.add(UnitCategory.LAND);
        } else if (type == PropertyType.AIRPORT) {
            production.add(UnitCategory.AIR);
        } else if (type == PropertyType.PORT) {
            production.add(UnitCategory.NAVAL);
        }

        PropertyDefinition definition = new PropertyDefinition(
                type, name, income, defense, 20, production);
        return definitionRepository.save(definition);
    }
}
