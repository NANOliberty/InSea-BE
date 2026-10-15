package checkmate.insea.fish_info.model.EnumClass;

public enum FishRarity {
    COMMON("common"),
    UNCOMMON("uncommon"), 
    RARE("rare"),
    LEGENDARY("legendary");
    
    private final String value;
    
    FishRarity(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}