package moe.nea.libautoupdate;

public interface CurrentVersion {
    int getCurrentVersionNumber();

    static CurrentVersion of(int number) {
        return () -> number;
    }
}
