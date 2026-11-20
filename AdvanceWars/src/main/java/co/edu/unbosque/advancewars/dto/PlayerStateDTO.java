package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Player;

/** Estado de cada jugador, incluida su economia. */
public class PlayerStateDTO {
	private String name;
	private int turnOrder;
	private int money;
	private String status;
	private String factionCode;

	public PlayerStateDTO(Player player) {
		name = player.getName();
		turnOrder = player.getTurnOrder();
		money = player.getMoney();
		status = player.getStatus().name();
		factionCode = player.getFaction() == null ? null : player.getFaction().getCode();
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

	public String getStatus() {
		return status;
	}
}
