package checkmate.insea.fish_info.model.EnumClass;

public enum BanType {
    FULL_BAN("full_ban"),
    SIZE_RESTRICTION("size_restriction");
    
    private final String value;
    
    BanType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    } 
}
