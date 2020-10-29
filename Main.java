import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import java.util.Scanner;

public class Main {

    // ATTRIBUTES ======================
    // ---------------------------------
    static Player[] player;         // THIS ARRAY CONTAINS THE PLAYERS
    static Scanner input;           // SCANNER OBJECT TO READ PLAYER INPUT
    static int player_count;        // HOW MANY PLAYERS?
    static int number;              // THE MAGIC NUMBER
    static int rounds;              // HOW MANY ROUNDS HAVE BEEN PLAYED
    static int next_player;         // WHICH PLAYER GOES NEXT?
    static int max_rounds;          // HOW MANY ROUNDS WILL BE PLAYED?
    static boolean[] picked;        // KEEPS TRACK OF WHICH NUMBERS HAVE BEEN PICKED
    static boolean lightning_round; // INPUT.....NOW! (MORE INFO IN UPDATE LOG)
    static boolean sudden_round;    // SUDDEN DEATH ROUND (MORE INFO IN UPDATE LOG)


    // MAIN METHOD =====================
    // ---------------------------------
    public static void main(String[] args) throws IOException {

        // CREATE A NEW INSTANCE OF A SCANNER, WHICH
        // ALLOWS US TO ACCEPT INPUT FROM USERS
        input = new Scanner(System.in);

        // PRINT A WELCOME MESSAGE
        WelcomeMessage();

        // GET THE NUMBER OF PLAYERS
        player_count = GetNumberOfPlayers();

        // SET THE SIZE OF THE PLAYER ARRAY
        player = new Player[player_count];

        // SET PLAYER NAMES
        SetPlayerNames();

        // DEFAULT MAX # OF ROUNDS (CUSTOMIZABLE)
        max_rounds = 5;

        // CHOOSE GAME MODE
        ChooseMode();

        // CHOOSE A RANDOM PLAYER TO START
        System.out.println("Choosing who goes first...\n");
        next_player = (int)(Math.random() * (double)player_count);


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // ~~~~~~~~~~~~~~MAIN LOOP~~~~~~~~~~~~~~
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


        // THIS LOOP RUNS THE WHOLE GAME
        while (rounds < max_rounds) {

            // "PRESS ANY KEY TO CONTINUE"
            System.out.println("Input to proceed...");
            input.next();

            // SETUP A NEW ROUND
            StartNewRound();

            // THIS LOOP RUNS A SINGLE ROUND
            while (GetPlayersLeft() > 1) {


                // ~~~~~~~~~~~~~~~OBTAINING INPUT~~~~~~~~~~~~~~~


                // DID THEY SURVIVE?
                boolean survived = true;

                // DETECTS INPUT (LIGHTNING ROUND)
                boolean input_present = true;

                // PROMPT THE NEXT PLAYER
                System.out.println(player[next_player].GetName().toUpperCase() + ", IT IS YOUR TURN.");
                System.out.print("Choose a number (0-9): ");

                // 3, 2, 1 SECS TO INPUT, VARIES INVERSELY WITH ROUND NUMBER (LIGHTNING ROUND)
                if (lightning_round) input_present = TimeInput(GetTimeFrame() );

                // STORE THE PLAYER'S CHOICE
                int choice = 0;

                // NO INPUT? NO MERCY (LIGHTNING ROUND)
                if (lightning_round && !input_present) {
                    survived = false;
                    System.out.println("NOT FAST ENOUGH!");
                }
                // GET THEIR CHOICE
                else {
                    choice = input.nextInt();
                    input.nextLine();
                }

                // THIS HOLDS A MESSAGE IN CASE THEY CHOSE POORLY
                String message = "";


                // ~~~~~~~~~~~~~~~INTERPRETING CHOICE~~~~~~~~~~~~~~~


                if (choice == number) {
                    // THEY PICKED THE MAGIC NUMBER
                    survived = false;
                    message = "Tough luck!";
                }
                else if (choice < 0) {
                    // THEY PICKED A NUMBER TOO LOW (BELOW RANGE)
                    survived = false;
                    message = choice + " was never an option...";
                    player[next_player].smart = true;
                    System.out.println("Uh, OK...");
                }
                else if (choice > 9) {
                    // THEY PICKED A NUMBER TOO HIGH (ABOVE RANGE)
                    survived = false;
                    message = choice + " was never an option...";
                    player[next_player].smart = true;
                    System.out.println("Uh, OK...");
                }
                else if (picked[choice]) {
                    // THEY PICKED A NUMBER THAT HAD ALREADY BEEN PICKED
                    survived = false;
                    message = choice + " has already been picked!";
                    player[next_player].smart = true;
                    System.out.println("Uh, OK...");
                }

                // GIVE SOME SPACE...
                System.out.println();


                // ~~~~~~~~~~~~~~~DETERMINING SURVIVAL~~~~~~~~~~~~~~~


                // THEY DIDN'T SURVIVE!
                if (!survived) {

                    // LET THE USER KNOW THAT THEY FAILED
                    System.out.println("( { K A B O O M } )");
                    System.out.println(player[next_player].GetName().toUpperCase() + " HAS BEEN ELIMINATED.");
                    System.out.println(message);
                    System.out.println();
                    java.awt.Toolkit.getDefaultToolkit().beep(); // <-- BEEP!

                    // REMEMBER THAT THEY LOST
                    player[next_player].lost = true;

                    // CHECK TO SEE IF THIS ENDED THE ROUND
                    // AND PREP THE NEXT ROUND IF SO
                    if (GetPlayersLeft() > 1) {
                        SetNewNumber();
                        ResetPickedNumbers();
                    }
                }

                // THEY DID SURVIVE!
                else {
                    // RECORD THAT THIS NUMBER IS NO LONGER AVAILABLE
                    picked[choice] = true;

                    // RANDOM CHANCE TO CLOWN ON THE PLAYER (2% CHANCE)
                    if ((int) (Math.random() * 50) == 0) CheekyMessage();

                }

                // WHO'S NEXT?
                ChooseNextPlayer();
            }

            // INCREASE THE ROUND COUNT
            rounds++;
            System.out.println();

            // INCREASE SCORE AND RESET PLAYER STATES
            for (int i = 0; i < player_count; i++) {

                if (!player[i].lost) {
                    // ADD A POINT TO THE WINNER
                    player[i].score++;

                    // LET EVERYONE KNOW WHO WON
                    System.out.println("-----------------------------------------------");
                    System.out.println(player[i].GetName().toUpperCase() + " WINS ROUND " + rounds + ".");
                    System.out.println("-----------------------------------------------");
                }

                // RESET THIS FOR THE NEXT ROUND
                player[i].lost = false;
            }

            // SHOW THE SCORES
            PrintScores();

            // MAKE SOME SPACE...
            System.out.println();

            // TAKE TOP TWO PLAYERS IN THE LAST ROUND AND START SUDDEN DEATH
            if ((player[player.length - 1].score == player[player.length - 2].score)
                    && (rounds == max_rounds - 1) ) { SuddenDeath(); }
        }


        System.out.println("-----------------------------------------------");
        System.out.println(player[player.length - 1].GetName().toUpperCase() + " WINS THE GAME!!!");
        System.out.println("-----------------------------------------------");

        // THE GAME HAS ENDED
        System.out.println("!!! GAME OVER !!!");
    }




    // MISCELLANEOUS METHOD =====================
    // ---------------------------------
    public static void PrintScores () {
        // GET THE LENGTH OF THE LONGEST NAME
        int longest_name = LongestName();

        // SORT THE PLAYERS BEFOREHAND
        SortPlayers();

        // LOOP THROUGH ALL PLAYERS (BACKWARDS INCREASING ORDER -> DECREASING)
        for (int i = player_count - 1; i >= 0; i--) {

            // DETERMINE HOW MANY SPACES WE SHOULD ADD IN
            // ORDER TO GET THE NAMES AND SCORES TO ALIGN
            int difference = longest_name - player[i].GetName().length();

            // PRINT THE PLAYER'S NAME
            System.out.print(player[i].GetName());

            // PRINT EXTRA SPACES
            for (int j = 0; j < difference; j++) {
                System.out.print(" ");
            }

            // PRINT POINTS
            System.out.println(" -  " + player[i].score + (player[i].score == 1 ? " pt." : " pts."));
        }
    }

    public static int LongestName () {

        // ASSUME THE LONGEST NAME IS 0 CHARACTERS
        int result = 0;

        // LOOP THROUGH THE PLAYERS
        for (int i = 0; i < player_count; i++) {
            // DID WE FIND A LONGER NAME?
            if (player[i].GetName().length() > result)
                result = player[i].GetName().length();
        }

        // RETURN THE RESULT
        return result;
    }

    public static int GetPlayersLeft () {

        // ASSUME THERE'S NOBODY LEFT...
        int players_left = 0;

        // LOOK AT EACH PLAYER AND CHECK IF THEY HAVEN'T LOST
        for (int i = 0; i < player_count; i++) {
            if (!player[i].lost)
                players_left++;
        }

        // RETURN THE RESULTS
        return players_left;
    }

    public static void ChooseNextPlayer () {

        // LOOP UNTIL WE FIND A VALID PLAYER
        do {

            // MOVE TO THE NEXT PLAYER
            next_player++;

            // IF WE MOVED TOO FAR, WRAP AROUND TO ZERO
            if (next_player >= player_count)
                next_player = 0;

        } while (player[next_player].lost);

    }

    public static void ResetPickedNumbers () {
        // RESET THE PICKED ARRAY
        picked = new boolean[10];
    }

    public static void SetNewNumber () {

        // CHOOSE A RANDOM NUMBER [0, 9]
        number = (int)(Math.random() * 9.1);

        // PROMPT THE USER
        System.out.println("A new number has been chosen.");
        System.out.println();

        // SAVED ME WHEN DEBUGGING
        // System.out.println(number);
    }

    public static void StartNewRound () {

        // ANNOUNCE THE START OF A NEW ROUND
        System.out.println("The game " + (rounds == 0 ? "begins!" : "continues..."));
        System.out.println("===============================================");

        // GENERATE A NEW NUMBER
        SetNewNumber();

        // RESET PICKED ARRAY
        ResetPickedNumbers();
    }

    public static void SetPlayerNames () {

        // LOOP THROUGH THE PLAYERS
        for (int i = 0; i < player_count; i++) {

            // PROMPT THE USER
            System.out.print("Player " + (i+1) + ", enter your name: ");

            // INSTANTIATE A NEW PLAYER USING THE NAME THAT'S PROVIDED
            player[i] = new Player(input.nextLine());

            // IF THE PLAYER DOESN'T ENTER A NAME, CALL THEM "BIG BRAIN"
            if (player[i].name.length() == 0) {
                player[i].name = "Big Brain";
                System.out.println("Uh, OK...");
            }

        }

        // MAKE SOME SPACE...
        System.out.println();
    }

    public static int GetNumberOfPlayers () {

        // ASSUME THERE ARE NO PLAYERS
        int result;

        // LOOP UNTIL A VALID NUMBER IS ENTERED
        do {
            // PROMPT THE USER
            System.out.print("Enter the number of players: ");

            // GET THE RESULT & MAKE SOME SPACE
            result = input.nextInt();
            input.nextLine();
            System.out.println();

            // MUST HAVE AT LEAST TWO PLAYERS
            if (result < 2) {
                System.out.println("You need at least two players!\n");
            }

        } while (result < 2);

        // RETURN THE RESULT
        return result;
    }

    public static void ChooseMode () {

        // CHOOSE GAME MODE
        int choice;

        // KILL LOOP UPON VALID OPTION
        boolean kill_switch = false;

        while (!kill_switch) {
            System.out.println("====GAMEPLAY OPTIONS====");
            System.out.println("[1] CLASSIC ROUND\n[2] LIGHTNING ROUND (2 players)\n[3] SET ROUNDS");
            System.out.print("Enter your choice: ");

            // CHOICES - NORMAL MODE, SPEED MODE, SET # OF ROUNDS
            choice = input.nextInt();

            if (choice == 1) {
                lightning_round = false;
                kill_switch = true;
            }
            if (choice == 2 && player_count == 2) {
                lightning_round = true;
                kill_switch = true;
            }
            if (choice == 3) {
                // REMEMBER CURRENT # OF ROUNDS
                // SO THAT IT DOES NOT UPDATE IF THE PLAYER PUTS AN INVALID NUMBER
                int temp = max_rounds;

                do {
                    System.out.println("Set the number of max. rounds (currently " + temp + "): ");
                    max_rounds = input.nextInt();
                    if (max_rounds < 3) System.out.println("Must play at least 3 rounds!");
                }
                while (max_rounds < 3);

            }
            // IF KILL SWITCH IS STILL OFF, THEY MUST HAVE NOT PUT A VALID INPUT
            else if (!kill_switch) {
                System.out.println("Invalid Option!\n");
            }

            System.out.println();
        }

    }

    public static void SuddenDeath () {

        // YOU'VE BEEN AVOIDING THE NUMBER THE WHOLE TIME; NOW YOU MUST CHOOSE IT!
        sudden_round = true;

        // USED AS A WORK AROUND TO THE ChooseNextPlayer() METHOD WHEN THERE ARE >2 PLAYERS
        int times_looped = 2;

        // TAKE THE TOP 2 PLAYERS (NOT MOST OPTIMAL WAY BUT DEFINITELY SIMPLER)
        String[] players_left = {player[player.length - 1].name, player[player.length - 2].name};

        // LET THE PLAYERS KNOW THAT THE SUDDEN DEATH ROUND HAS BEGUN
        System.out.println();
        System.out.println("===============================================");
        System.out.println("SUDDEN DEATH HAS BEGUN!");
        System.out.println("NOW IT IS TIME TO GUESS THE NUMBER!");
        System.out.println("-----------------------------------------------");
        System.out.println(players_left[0] + " AND " + players_left[1] + "\nGET READY!");
        System.out.println("===============================================");


        // RESET ANY NUMBERS BEFORE, SET THE FINAL NUMBER
        ResetPickedNumbers();
        SetNewNumber();

        while (sudden_round) {

            // TAKE TURNS BETWEEN THE TWO PLAYERS
            System.out.println(players_left[times_looped%2]+ ", IT IS YOUR TURN.");
            System.out.print("Choose a number (0-9): ");

            // STORE THEIR CHOICE
            int choice = input.nextInt();

            if (choice == number) {
                // UPDATE (SO THAT THE WINNER IS IN THE LAST INDEX)
                player[player.length - 1].name = players_left[times_looped%2];

                // THEY PICKED THE WINNING NUMBER, END SUDDEN DEATH
                sudden_round = false;
                rounds++;
            }
            else if (choice < 0) {
                // THEY PICKED A NUMBER TOO LOW (BELOW RANGE)
                System.out.println(choice + " was never an option...");
                player[next_player].smart = true;
                System.out.println("Uh, OK...");
            }
            else if (choice > 9) {
                // THEY PICKED A NUMBER TOO HIGH (ABOVE RANGE)
                System.out.println(choice + " was never an option...");
                player[next_player].smart = true;
                System.out.println("Uh, OK...");
            }
            else if (picked[choice]) {
                // THEY PICKED A NUMBER THAT HAD ALREADY BEEN PICKED
                System.out.println(choice + " has already been picked!");
                player[next_player].smart = true;
                System.out.println("Uh, OK...");
            }
            else {
                // REMEMBER CHOSEN NUMBER
                picked[choice] = true;

                // INCREMENT EACH TIME
                times_looped++;

                // LEAVE SOME SPACE...
                System.out.println();
            }

        }

    }

    public static void SortPlayers () {
        /* BUBBLE SORTING ALGORITHM IN INCREASING ORDER,
         IMPLEMENTING DESCENDING ORDER CREATES STRANGE BEHAVIOR:
         ~KNOW~ THAT THE ARRAY WILL BE IN THIS ORDER WHEN DOING ANYTHING */
        boolean sorted = false;
        Player temp;

        while (!sorted) {
            sorted = true;
            for (int i = 0; i < player.length - 1; i++) {
                if (player[i].score > player[i + 1].score) {

                    // SWAP THE OBJECTS' POSITION IN THE ARRAY
                    temp = player[i];
                    player[i] = player[i + 1];
                    player[i + 1] = temp;

                    sorted = false;
                }

            }
        }

    }

    public static boolean TimeInput (int time_frame) throws IOException {
        // BufferedReader OBJECT
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // STORE THE STARTING TIME
        long startTime = System.currentTimeMillis();

        // RUN WHILE ([THE TIME SINCE THIS METHOD WAS CALLED] IS LESS THAN [TIME DESIRED]);
        // INTENTIONALLY EMPTY, JUST RUNS IN THE BACKGROUND
        while ((System.currentTimeMillis() - startTime) < time_frame * 1000
                && !in.ready()) {}
        return in.ready();
    }

    public static int GetTimeFrame () {
        if ( (rounds > (max_rounds/1.6)) ) return 1;
        else if ( (rounds > (max_rounds/3)) ) return 2;

        else return 3;
    }

    public static void CheekyMessage () {
        // ADD GAGS HERE
        int random_number = (int)(Math.random() * 6.1 + 1);
        String victim = player[next_player].name.toUpperCase();

        // LIST OF MESSAGES
        switch (random_number) {
            case 1:
                System.out.println("PLAYER " + victim + "... HAVE A NICE DAY!");
                break;
            case 2:
                System.out.println("TOASTY!");
                break;
            case 3:
                System.out.println("BOO, " + victim);
                break;
            case 4:
                System.out.println("DNS SPOOKED YOU!");
                break;
            case 5:
                System.out.println("INTERESTING CHOICE.");
                break;
            case 6:
                System.out.println("DID " + victim + " REALLY CHOOSE THAT?");
                break;
        }

        System.out.println();
        java.awt.Toolkit.getDefaultToolkit().beep(); // <-- BEEP!
    }

    public static void WelcomeMessage () {
        // PRINTS THE TITLE AND VERSION OF THE GAME
        System.out.println("===============================================");
        System.out.println(" C O M P U T E R");
        System.out.println("     S C I E N C E");
        System.out.println("         R O U L E T T E");
        System.out.println("====================================== v1.2 ===");
        System.out.println();
    }



}


// Santi's v1.2 =========================================================================
// * Menu: Classic Round or Lightning Round. -> ChooseMode()
//   * Classic Round: Mode we all know and love!
//   * Lightning Round: Limited time for input, no mercy for the slow. -> TimeInput(time)
//   * Set Rounds: Let's the player set the maximum # of rounds (default at 5).
// * Sudden Death: Activates on the last round, takes top two players. -> SuddenDeath()
// * Array Sorting Algorithm: Bubble sort (increasing). -> SortPlayers()
//   * Players sorted by score! -> PrintScores() loop is now backwards
//   * Winner announced! -> player.length - 1
// * Number now between [0, 9] -> iN cS yOu sTaRt cOuNTinG fRoM 0
//   * Just more convenient in actual gameplay; feel free to change it back.
// * Gags! Great chance to scare the player. -> CheekyMessage()
//   * Definitely add more jokes!
// * More comments :~)
// * Removed Herobrine.