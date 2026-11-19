package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Player;

/** Estado de cada jugador, incluida su economia. */
public class PlayerStateDTO {
	private String name;
	private int turnOrder;
	private int money;
	private String status;

	public PlayerStateDTO(Player player) {
		name = player.getName();
		turnOrder = player.getTurnOrder();
		money = player.getMoney();
		status = player.getStatus().name();
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

	public String getStatus() {
		return status;
	}
}
