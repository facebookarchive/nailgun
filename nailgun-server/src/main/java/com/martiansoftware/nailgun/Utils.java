package com.martiansoftware.nailgun;

import java.io.InputStream;
import java.io.IOException;

class Utils {
    /** Close a possibly-null InputStream and swallow any resulting IOException. */
    static void closeQuietly(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (IOException ioe) {
            // swallow exception
        }
    }

    private Utils() {
        throw new AssertionError("Cannot instantiate this class");
    }
}
