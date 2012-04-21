package server;

public interface GameSubject {
    public void addObserver(Player observer);
    public void removeObserver(Player observer);
    public void notifyObservers(Player player, String command);

}
