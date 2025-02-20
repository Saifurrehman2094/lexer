package automata;

public class State {
    private int stateId;

    public State(int stateId) {
        this.stateId = stateId;
    }

    public int getStateId() {
        return stateId;
    }

    @Override
    public String toString() {
        return "State(" + stateId + ")";
    }
}
