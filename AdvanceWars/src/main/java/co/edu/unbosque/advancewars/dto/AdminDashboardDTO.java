package co.edu.unbosque.advancewars.dto;

public class AdminDashboardDTO {
	private final long totalUsers;
	private final int onlineUsers;
	private final long totalGames;
	private final long gamesInProgress;
	private final long finishedGames;

	public AdminDashboardDTO(long totalUsers, int onlineUsers, long totalGames, long gamesInProgress,
			long finishedGames) {
		this.totalUsers = totalUsers;
		this.onlineUsers = onlineUsers;
		this.totalGames = totalGames;
		this.gamesInProgress = gamesInProgress;
		this.finishedGames = finishedGames;
	}

	public long getTotalUsers() {
		return totalUsers;
	}

	public int getOnlineUsers() {
		return onlineUsers;
	}

	public long getTotalGames() {
		return totalGames;
	}

	public long getGamesInProgress() {
		return gamesInProgress;
	}

	public long getFinishedGames() {
		return finishedGames;
	}
}
