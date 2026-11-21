package co.edu.unbosque.advancewars.dto;

public class PlayerSelectionRequestDTO {
    private Long factionId;
    private Long commanderId;
    public Long getFactionId() { return factionId; }
    public void setFactionId(Long factionId) { this.factionId = factionId; }
    public Long getCommanderId() { return commanderId; }
    public void setCommanderId(Long commanderId) { this.commanderId = commanderId; }
}
