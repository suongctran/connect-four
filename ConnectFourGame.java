import java.util.Scanner;
import java.util.concurrent.*;
import java.util.HashMap;
/**
 *
 * @author Suong Tran
 */
public class ConnectFourGame {
    private static final int MAX_TIME = 30;
    private static int USER_TIME = 1;
    private static final int DEPTH = 5;
    private static final Scanner kb = new Scanner(System.in);
    // -1 for unassigned, 0 for O (user), 1 for X (computer)
    private static int[][] board;
    private static State state;
    private static HashMap<String, int[]> empty_positions;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        init_game_assets();
        boolean user_first = prompt_user_turn();
        USER_TIME = prompt_user_time();
        print_board();
        if (user_first) {
            for (;;) {
                int[] user_move = user_move();
                System.out.println("USER" + empty_positions.size());
                print_board();
                if (check_game_over(0)) {
                    break;
                }
                ConnectFour res = computer_move(copy(board), new HashMap<>(empty_positions), user_move);
                System.out.println("BOT" + empty_positions.size());
                board[res.move[0]][res.move[1]] = 1;
                print_board();
                if (check_game_over(1)) {
                    break;
                }
            }
        }else{
              for (;;) {
                ConnectFour res = computer_move(copy(board), new HashMap<>(empty_positions), new int[]{0,0});
                System.out.println("BOT" + empty_positions.size());
                board[res.move[0]][res.move[1]] = 1;
                print_board();
                if (check_game_over(1)) {
                    break;
                }
                user_move();
                System.out.println("USER" + empty_positions.size());
                print_board();
                if (check_game_over(0)) {
                    break;
                }
            }
        }
        kb.close();
        System.exit(0);
    }
    private static int prompt_user_time(){
        int choice = -1;
        while (choice < 0) {
            try {
                System.out.print("Enter time for AI turn (max 30 seconds)\n");
                choice = Integer.parseInt(kb.nextLine());
            } catch (Exception e) {
                //System.out.println("Input error:" + e);
            }
        }
        return choice <= MAX_TIME ? choice : MAX_TIME;
    }
    
    private static boolean prompt_user_turn(){
        int choice = -1;
        while (choice != 1 && choice != 2) {
            try {
                System.out.print("Menu\n1: User goes first\n2: AI goes first\n");
                choice = Integer.parseInt(kb.nextLine());
            } catch (Exception e) {
                //System.out.println("Input error:" + e);
            }
        }
        return choice == 1;
    }
    
    private static void init_game_assets(){
        char letter_move = 'A';
        empty_positions = new HashMap<>();
        board = new int[][]{
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1}};
        for(int i = 0; i < board[0].length; i++){
            for(int j = 0; j < board[0].length; j++){
                empty_positions.put(letter_move + "" + (j+1), new int[]{i, j});
            }
            letter_move++;
        }
    }
    
    private static int[] user_move(){
        boolean valid = false;
        String user_move = "";
        while(!valid){
            try{
                System.out.print("Choose your next move: ");
                user_move = kb.nextLine();
                valid = is_valid_usermove(user_move);
            }catch(Exception e){
                System.out.println("Bad user input " + e);
            }
        }
        return translate_move(user_move);
    }
 
    private static ConnectFour computer_move(int[][] board, HashMap<String, int[]> empty_positions, int[] user_move) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        int[] move = user_move;
        ConnectFour res = new ConnectFour(board, move);
        Future<ConnectFour> future = executor.submit(new Task(board, empty_positions, DEPTH, 1, res, new State(user_move, board)));
        executor.shutdown();
        try {
            res = future.get(USER_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            System.out.println("Did not reach a leaf, returning best found" + e);
        }
        if (!executor.isTerminated()) {
            executor.shutdownNow();
        }
        //System.out.println(move == null);
        update_empty_positions(translate_move(res.move));
        System.out.println("My current move is: " + translate_move(res.move) + " " + res.move[0] + res.move[1]);
        return res;
        
    }
    
    private static void update_empty_positions(String move){
        empty_positions.remove(move);
    }
    
    
    private static boolean check_game_over(int player){
        int consecutive = 0, total = 0;
        // checking consecutive horizontal tokens
        for(int i = 0; i < board[0].length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(board[i][j] == player){
                    consecutive++;
                    total++;
                }else if(board[i][j] == -1){
                    consecutive = 0;
                }else{
                    consecutive = 0;
                    total++;
                }
                if(consecutive >= 4){
                    System.out.println("Game Over!");
                    return true;
                }
            }
        }
        // checking for tie
        if(total == (board[0].length*board[0].length)){
            System.out.println("Game Over! : Tie");
            return true;
        }
        consecutive = 0;
        // checking consecutive vertical tokens
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[j][i] == player) {
                    consecutive++;
                } else {
                    consecutive = 0;
                }
                if (consecutive >= 4) {
                    System.out.println("Game Over! : ");
                    return true;
                }
            }
        }
        return false;
    }
     
    private static boolean is_valid_usermove(String move){
        if(move.length() != 2){
            return false;
        }
        int[] rowcol = translate_move(move);
        if(rowcol[0] < 0 || rowcol[0] >= board[0].length || rowcol[1] < 0 || rowcol[1] >= board[0].length){
            return false;
        }
        else if(board[rowcol[0]][rowcol[1]] != -1){
            return false;
        }
        update_empty_positions(move);
        //System.out.println(empty_positions.size());
        board[rowcol[0]][rowcol[1]] = 0;
        return true;
    }

    private static int[] translate_move(String move) {
        char letter = Character.toUpperCase(move.charAt(0));
        int row = letter - 65;
        int col = Character.getNumericValue(move.charAt(1));
        return new int[]{row, col-1};
    }
    
    private static String translate_move(int[] move) {
        char row = (char)(move[0] + 65);
        int col = move[1] + 1;
        return row + "" + col;
    }

    private static void print_board(){
        char letter = 'A';
        char marker = '-';
        System.out.print("  ");
        for(int i = 0; i < board[0].length; i++){
            System.out.printf("%2d", i+1);
        }
        System.out.println();
        for(int j = 0; j < board[0].length; j++){
            System.out.printf("%2c", letter++);
            for(int k = 0; k < board[0].length; k++){
                switch (board[j][k]) {
                    case -1:
                        marker = '-';
                        break;
                    case 0:
                        marker = 'O';
                        break;
                    case 1:
                        marker = 'X';
                        break;
                    default:
                        marker = '?';
                        break;
                }
                System.out.printf("%2c", marker);
            }
            System.out.println();
        }
    }
    
    private static int[][] copy(int[][] board){
        int[][] clonedboard = new int[board.length][];
        for(int i = 0; i < board.length; i++){
            clonedboard[i] = board[i].clone();
        }
        return clonedboard;
    }

    
}
