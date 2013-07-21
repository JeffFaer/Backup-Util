package falgout.backup;

public enum StandardSyntax implements Syntax {
    GLOB, REGEX;
    
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
