package streamblocks.tycho.simulator;

public interface Simulator {
    /**
     * Executes instructions in the actor machine controller until an Exec or Wait instruction is executed.
     *
     * @return - true if an Exec is executed, false if Wait was executed last,
     * i.e. when step() return false there is no need to call it again until other actors has put new tokens in this actors input channels.
     */
    public boolean step();

    /**
     * Adds the content of all live scopes to the string buffer.
     *
     * @param sb
     */
    public void scopesToString(StringBuffer sb);
}
