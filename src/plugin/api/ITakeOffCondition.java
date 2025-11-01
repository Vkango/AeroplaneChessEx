package plugin.api;

public interface ITakeOffCondition {

    boolean canTakeOff(int diceValue);

    String getConditionDescription();
}
