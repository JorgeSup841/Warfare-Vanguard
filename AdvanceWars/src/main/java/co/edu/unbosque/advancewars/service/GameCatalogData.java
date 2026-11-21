package co.edu.unbosque.advancewars.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;

/** Lee las definiciones del catalogo desde src/main/resources/catalogo.txt. */
final class GameCatalogData {

    record FactionData(String code, String name, String description, String color) { }

    record CommanderData(String code, String name, String description, String factionCode,
            String passiveAbilityName, String passiveAbilityDescription, String passiveEffectCode,
            String powerName, String powerDescription, String powerEffectCode, int powerCost) { }

    record WeaponData(String code, String name, int minRange, int maxRange,
            boolean usesAmmunition, int ammunitionCapacity, Map<UnitType, Integer> damages) { }

    record UnitData(UnitType type, String name, UnitCategory category, MovementType movementType,
            int productionCost, int maxHealth, int movementPoints, boolean usesFuel, int maxFuel,
            int fuelConsumptionPerTurn, int fuelPerMovementPoint, boolean captureCapable,
            List<WeaponData> weapons) { }

    private final List<FactionData> factions;
    private final List<CommanderData> commanders;
    private final List<UnitData> units;

    private GameCatalogData(List<FactionData> factions, List<CommanderData> commanders, List<UnitData> units) {
        this.factions = List.copyOf(factions);
        this.commanders = List.copyOf(commanders);
        this.units = List.copyOf(units);
    }

    List<FactionData> factions() { return factions; }
    List<CommanderData> commanders() { return commanders; }
    List<UnitData> units() { return units; }

    static GameCatalogData load() {
        Map<String, FactionData> factions = new LinkedHashMap<>();
        Map<String, CommanderData> commanders = new LinkedHashMap<>();
        Map<UnitType, UnitData> units = new LinkedHashMap<>();
        Set<String> weaponCodes = new HashSet<>();

        // getResourceAsStream funciona igualmente en el IDE y en un WAR empaquetado.
        InputStream resource = GameCatalogData.class.getClassLoader().getResourceAsStream("catalogo.txt");
        if (resource == null) {
            throw new IllegalStateException("No se encontro src/main/resources/catalogo.txt en el classpath");
        }

        String section = "";
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line;
                    if (!List.of("[FACTIONS]", "[COMMANDERS]", "[UNITS]", "[WEAPONS]").contains(section)) {
                        throw new IllegalArgumentException("Seccion desconocida: " + section);
                    }
                    continue;
                }
                try {
                    String[] f = line.split("\\|", -1);
                    for (int i = 0; i < f.length; i++) {
                        f[i] = f[i].trim();
                    }
                    switch (section) {
                        case "[FACTIONS]" -> {
                            fields(f, 4);
                            FactionData data = new FactionData(f[0], f[1], f[2], f[3]);
                            if (factions.putIfAbsent(data.code(), data) != null) {
                                throw new IllegalArgumentException("Faccion repetida: " + data.code());
                            }
                        }
                        case "[COMMANDERS]" -> {
                            fields(f, 11);
                            CommanderData data = new CommanderData(f[0], f[1], f[2], f[3], f[4],
                                    f[5], f[6], f[7], f[8], f[9], number(f[10]));
                            if (commanders.putIfAbsent(data.code(), data) != null) {
                                throw new IllegalArgumentException("Comandante repetido: " + data.code());
                            }
                        }
                        case "[UNITS]" -> {
                            fields(f, 12);
                            UnitData data = new UnitData(UnitType.valueOf(f[0]), f[1],
                                    UnitCategory.valueOf(f[2]), MovementType.valueOf(f[3]),
                                    number(f[4]), number(f[5]), number(f[6]), yesNo(f[7]),
                                    number(f[8]), number(f[9]), number(f[10]), yesNo(f[11]),
                                    new ArrayList<>());
                            if (units.putIfAbsent(data.type(), data) != null) {
                                throw new IllegalArgumentException("Unidad repetida: " + data.type());
                            }
                        }
                        case "[WEAPONS]" -> {
                            fields(f, 8);
                            UnitData owner = units.get(UnitType.valueOf(f[0]));
                            if (owner == null) {
                                throw new IllegalArgumentException("Unidad duena de arma inexistente: " + f[0]);
                            }
                            if (!weaponCodes.add(f[1])) {
                                throw new IllegalArgumentException("Codigo de arma repetido: " + f[1]);
                            }
                            owner.weapons().add(new WeaponData(f[1], f[2], number(f[3]), number(f[4]),
                                    yesNo(f[5]), number(f[6]), damages(f[7])));
                        }
                        default -> throw new IllegalArgumentException("Falta una seccion antes de los datos");
                    }
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("catalogo.txt, linea " + lineNumber + " " + section
                            + ": " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer catalogo.txt", ex);
        }
        if (factions.isEmpty() || commanders.isEmpty() || units.isEmpty() || weaponCodes.isEmpty()) {
            throw new IllegalStateException("catalogo.txt esta incompleto: faltan facciones, comandantes, unidades o armas");
        }
        for (CommanderData commander : commanders.values()) {
            if (!factions.containsKey(commander.factionCode())) {
                throw new IllegalStateException("Faccion inexistente para " + commander.code()
                        + ": " + commander.factionCode());
            }
        }
        return new GameCatalogData(new ArrayList<>(factions.values()),
                new ArrayList<>(commanders.values()), new ArrayList<>(units.values()));
    }

    private static void fields(String[] data, int length) {
        if (data.length != length) {
            throw new IllegalArgumentException("Se esperaban " + length + " campos y llegaron " + data.length);
        }
        for (String field : data) {
            if (field.isEmpty()) {
                throw new IllegalArgumentException("No se admiten campos vacios");
            }
        }
    }

    private static int number(String value) {
        return Integer.parseInt(value);
    }

    private static boolean yesNo(String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Booleano invalido: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    private static Map<UnitType, Integer> damages(String value) {
        Map<UnitType, Integer> result = new EnumMap<>(UnitType.class);
        for (String entry : value.split(",")) {
            String[] pair = entry.trim().split(":", -1);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Dano invalido: " + entry);
            }
            UnitType target = UnitType.valueOf(pair[0].trim());
            int damage = number(pair[1].trim());
            if (damage < 0 || result.putIfAbsent(target, damage) != null) {
                throw new IllegalArgumentException("Dano negativo o objetivo repetido: " + entry);
            }
        }
        return Map.copyOf(result);
    }
}
