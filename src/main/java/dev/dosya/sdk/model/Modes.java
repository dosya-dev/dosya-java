package dev.dosya.sdk.model;

/** Lenient wire-string to enum mapping for models that keep the raw string. */
final class Modes {

    private Modes() {}

    static LockMode lock(String value) {
        if (value != null) {
            for (LockMode m : LockMode.values()) if (m.value().equals(value)) return m;
        }
        return LockMode.NONE;
    }

    static HiddenMode hidden(String value) {
        if (value != null) {
            for (HiddenMode m : HiddenMode.values()) if (m.value().equals(value)) return m;
        }
        return HiddenMode.NONE;
    }
}
