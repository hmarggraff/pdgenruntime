package org.pdgen.datasources.java;

import org.jetbrains.annotations.NotNull;
import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import javax.swing.*;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.function.Function;

public class JavaInternalExtensionMethods {
    Function<InputStream, ImageIcon> streamIntoImageFunction = JavaInternalExtensionMethods::streamIntoImage;

    public static ImageIcon streamIntoImage(InputStream in)  {
        try {
            byte[] bytes = in.readAllBytes();
            if (bytes == null || bytes.length == 0)
                return null;
            return new ImageIcon(bytes);
        } catch (Throwable e) {
            throw new Error("getting picture data from stream", e);
        }
    }
}


