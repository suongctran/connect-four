/**
 *
 * @author Suong Tran
 */
public class State {
    int[] action;
    int[][] board;
    int value;
    
    public State(int[] action, int[][] board){
        this.action = action;
        this.board = board;
    }
}
