package moe.nea.libautoupdate;

/**
 * Provider interface for getting the current version of this jar.
 */
public interface CurrentVersion {
    /**
     * @return the version number
     */
    int getCurrentVersionNumber();

    /**
     * Create a constant {@link CurrentVersion}
     *
     * @param number the constant version number
     * @return
     */
    static CurrentVersion of(int number) {
        return () -> number;
    }
}
