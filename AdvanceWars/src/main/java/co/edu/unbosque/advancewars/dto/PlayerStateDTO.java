package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Player;

/** Estado de cada jugador, incluida su economia. */
public class PlayerStateDTO {
	private String name;
	private int turnOrder;
	private int money;
	private String status;
	private String factionCode;
	private String commanderCode;
	private int powerCharge;
	private boolean powerActive;

	/** Construye el estado público del jugador desde el modelo persistido. */
	public PlayerStateDTO(Player player) {
		name = player.getName();
		turnOrder = player.getTurnOrder();
		money = player.getMoney();
		status = player.getStatus().name();
		factionCode = player.getFaction() == null ? null : player.getFaction().getCode();
		commanderCode = player.getCommander() == null ? null : player.getCommander().getCode();
		powerCharge = player.getPowerCharge();
		powerActive = player.isPowerActive();
	}

	public String getName() {
		return name;
	}

	public int getTurnOrder() {
		return turnOrder;
	}

	public int getMoney() {
		return money;
	}

	public String getFactionCode() {
		return factionCode;
	}

	public String getCommanderCode() { return commanderCode; }
	public int getPowerCharge() { return powerCharge; }
	/** Indica si el poder del comandante está en efecto. */
	public boolean isPowerActive() { return powerActive; }

	public String getStatus() {
		return status;
	}
}
