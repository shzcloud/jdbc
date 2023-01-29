package shz.jdbc.generate;

import shz.core.io.FileHelp;

public final class GenInfo {
    String folder;
    String path;
    boolean delete;
    public String packageName;

    GenInfo(String packageName) {
        this.packageName = packageName;
    }

    public static GenInfo of(String packageName) {
        return new GenInfo(packageName);
    }

    public GenInfo folder(String folder) {
        this.folder = FileHelp.formatPath(folder);
        return this;
    }

    public GenInfo path(String path) {
        this.path = FileHelp.formatPath(path);
        return this;
    }

    public GenInfo delete() {
        this.delete = true;
        return this;
    }
}
