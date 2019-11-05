package com.appian.intellij.k;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a list of locally-available files to use as test inputs
 */
final class TestInputFiles {

  private static final String DL = System.getProperty("user.home") + "/repo/data-layer/";
  private static final String[] TARGET_FOLDERS = new String[] {DL + "appian-data-server/src"};

  private TestInputFiles() {
  }

  static List<File> asFiles() throws IOException {
    final List<File> inputs = new ArrayList<>();
    for (String folderPath : TARGET_FOLDERS) {
      final File folder = new File(folderPath);
      if (!folder.exists()) {
        // TARGET_FOLDERS point to user's home dir,
        // not every user has the tests there
        continue;
      }
      Files.walk(Paths.get(folder.toURI())).forEach(path -> {
        final File file = path.toFile();
        final String fileName = file.getName();
        if (file.isFile() && (fileName.endsWith(".k") || fileName.endsWith(".q"))) {
          inputs.add(file);
        }
      });
    }
    return inputs;
  }

  static String[] asFilesPaths() throws IOException {
    return asFiles().stream().map(File::getAbsolutePath).toArray(String[]::new);
  }

}
