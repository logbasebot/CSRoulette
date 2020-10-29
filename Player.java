public class Player {

    // ATTRIBUTES ======================
    // ---------------------------------
    String name;            // THE PLAYER'S NAME
    int score;              // THE PLAYER'S SCORE
    boolean lost;           // HAS THE PLAYER LOST THIS ROUND?
    boolean smart;          // DID THE PLAYER MAKE A DUMB MOVE?

    // CONSTRUCTORS ====================
    // ---------------------------------
    public Player() {
        name = "UNKNOWN";
        score = 0;
        lost = false;
        smart = false;
    }

    public Player(String name) {
        this.name = name;
        score = 0;
        lost = false;
        smart = false;
    }

    // METHODS =========================
    // ---------------------------------
    public String GetName() {
        return name + (smart ? " (teh smart)" : "");
    }

}