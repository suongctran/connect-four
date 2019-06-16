/**
 *
 * @author Suong Tran
 */

public class ReturnState {
    int[] move;
    int alpha_beta;
    int[][] board;
    
    public ReturnState(int alpha_beta, int[] move){
        this.alpha_beta = alpha_beta;
        this.move = move;
    }
}
