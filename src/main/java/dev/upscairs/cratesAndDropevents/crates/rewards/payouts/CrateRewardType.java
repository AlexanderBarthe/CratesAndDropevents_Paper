package dev.upscairs.cratesAndDropevents.crates.rewards.payouts;

public enum CrateRewardType {
    ITEM("item"),
    SOUND( "sound"),
    MESSAGE( "message"),
    DELAY( "delay"),
    COMMAND( "command"),
    MONEY("money");

    private final String stringRepresentation;

    CrateRewardType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public static CrateRewardType fromString(String stringRepresentation) {
        for (CrateRewardType type : CrateRewardType.values()) {
            if (type.getStringRepresentation().equalsIgnoreCase(stringRepresentation)) {
                return type;
            }
        }
        return null;
    }
}
