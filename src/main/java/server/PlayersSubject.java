package server;

public interface PlayersSubject {
    public void addObserver(Player observer);
    public void removeObserver(Player observer);
    public void notifyObservers(String command);
}