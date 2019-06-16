import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.ArrayList;

/**
 *
 * @author Suong Tran
 */

public class Task implements Callable<ConnectFour>{
       private static final int FOUR_VALUE = 24*8*8;
    private final int MAX_DEPTH;
    private final int ai, opp;
    private int score;
    int[][] board;
    private static final int[] Weights={0,1,3,100000,1000000000};
    int turn;
    ConnectFour res;
    int[] choice;
    State istate;
    HashMap<String, int[]> empty_positions;
    public Task(int[][] board,  HashMap<String, int[]> empty_positions, int MAX_DEPTH, int ai, ConnectFour res, State istate){
        this.board = board;
        this.empty_positions = empty_positions;
        this.MAX_DEPTH = MAX_DEPTH;
        this.res = res;
        this.ai = ai;
        this.opp = ai == 1 ? 0 : 1;
        score = 0;
        this.istate = istate;
    }
     
    @Override
    public ConnectFour call() throws Exception {
        int[] choice = alpha_beta_search(istate);
        System.out.println("Finished!");
        return new ConnectFour(board, choice);
    }
    
    private int[] alpha_beta_search(State state){
        int depth = 0;
        UtilityValue v = max_value(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        System.out.println(v.value);
        return v.state.action;
    }
    
    private UtilityValue max_value(State state, int alpha, int beta, int depth){
        if(terminal_test(state, depth)){
            return utility(state);
        }
        UtilityValue v = new UtilityValue(state, Integer.MIN_VALUE);
        for(int[] a : actions(state)){
            State result = result(state, a, ai);
            v = max(v, min_value(result, alpha, beta, depth+1));
            if(v.value >= beta){
                res.move = a;
                return v;
            }
            alpha = Math.max(alpha, v.value);
        }
        return v;
    }
    
    private UtilityValue min_value(State state, int alpha, int beta, int depth){
        if(terminal_test(state, depth)){
            return utility(state);
        }
        UtilityValue v = new UtilityValue(state, Integer.MAX_VALUE);
        for(int[] a : actions(state)){
            State result = result(state, a, ai);
            v = min(v, max_value(result, alpha, beta, depth+1));
            if(v.value <= alpha){
                return v;
            }
            beta = Math.min(beta, v.value);
        }
        return v;
    }
    
    private UtilityValue max(UtilityValue v, UtilityValue min){
        return v.value > min.value ? v : min;
    }
    
    private UtilityValue min(UtilityValue v, UtilityValue min){
        return v.value < min.value ? v : min;
    }
    private boolean terminal_test(State state, int depth){
        if(depth > MAX_DEPTH){
            return true;
        }
        int ai_consecutive = 0, opp_consecutive = 0, total = 0;
        // checking consecutive horizontal tokens
        for(int i = 0; i < board[0].length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(board[i][j] == ai){
                    ai_consecutive++;
                    opp_consecutive = 0;

                }else if(board[i][j] == -1){
                    ai_consecutive = 0;
                    opp_consecutive = 0;
                    total++;
                }else{
                    opp_consecutive++;
                    ai_consecutive = 0;
                }
                if(ai_consecutive >= 4 || opp_consecutive >= 4){
                    return true;
                }

            }
        }
        // checking for tie
        if(total == 0){
            return true;
        }
        ai_consecutive = 0;
        opp_consecutive = 0;
        // checking consecutive vertical tokens
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[j][i] == ai){
                    ai_consecutive++;
                    opp_consecutive = 0;
                }else if(board[i][j] == -1){
                    ai_consecutive = 0;
                    opp_consecutive = 0;
                }else{
                    opp_consecutive++;
                    ai_consecutive = 0;
                }
                if(ai_consecutive >= 4 || opp_consecutive >= 4){
                    return true;
                }
            }
        }
        return false;
    }
    
    private UtilityValue utility(State state) {
        return new UtilityValue(state, (int)evaluate(state));
    }
    
      protected double evaluate(State state)
   {
      // NOTE: Positive answer is good for the computer.
      int row, column;
      int answer = 0;

      // For each possible starting spot, calculate the value of the spot for
      // a potential four-in-a-row, heading down, left, and to the lower-left.
      // Normally, this value is in the range [-3...+3], but if a
      // four-in-a-row is found for the player, the result is FOUR_VALUE which
      // is large enough to make the total answer larger than any evaluation
      // that occurs with no four-in-a-row.

      // Value moving down from each spot:
      for (row = 3; row < state.board.length; ++row)
         for (column = 0; column < state.board.length; ++column)
            answer += value(row, column, -1, 0, state);

      // Value moving left from each spot:
      for (row = 0; row <state.board.length; ++row)
         for (column = 3; column < state.board.length; ++column)
            answer += value(row, column, 0, -1, state);


      return answer;
   }
      
       private int value(int row, int column, int deltar, int deltac, State state)
   {
      // NOTE: Positive return value is good for the computer.    
      int i;
      int endRow = row + 3*deltar;
      int endColumn = column + 3*deltac;
      int playerCount= 0;
      int opponentCount = 0;

      if (
         (row < 0) || (column < 0) || (endRow < 0) || (endColumn < 0)
         ||
         (row >= state.board.length) || (endRow >= state.board.length)
         ||   
         (column >= state.board.length) || (endColumn >= state.board.length)
         )
	    return 0;

      for (i = 1; i <= 4; ++i)
      {
         if (state.board[row][column] == ai)
             ++playerCount;
         else if (state.board[row][column] != opp)
             ++opponentCount;
         row += deltar;
         column += deltac;
      }

      if ((playerCount > 0) && (opponentCount > 0))
         return 0; // Neither player can get four-in-a-row here.
      else if (playerCount == 4)
         return FOUR_VALUE;
      else if (opponentCount == 4)
         return -FOUR_VALUE;
      else
         return playerCount - opponentCount;
   }
    
    private ArrayList<int[]> actions(State state){
        ArrayList<int[]> actions = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++){
            for(int j = 0; j < state.board.length; j++){
                if(state.board[i][j] == -1){
                    actions.add(new int[]{i,j});
                }
            }
        }
        return actions;
    }
    
    
    
    private State result(State state, int[] a, int player){
        int[][] successor_board = new int[state.board.length][];
        for(int i = 0; i <state.board.length; i++){
            int[] temp = state.board[i];
            successor_board[i] = new int[temp.length];
            System.arraycopy(temp, 0, successor_board[i], 0, temp.length);
        }
        successor_board[a[0]][a[1]] = player;
        return new State(a, successor_board);
    }



    
}
