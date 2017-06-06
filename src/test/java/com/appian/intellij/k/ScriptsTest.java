package com.appian.intellij.k;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.DebugUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ScriptsTest extends KParserTest {
  
  private static final String AE = System.getProperty("user.home") + "/repo/ae/";
  private static final String DL = System.getProperty("user.home") + "/repo/data-layer/";

  private static final String[] TARGET_FOLDERS = new String[] {
    AE + "server/",
    DL + "appian-data-server/src"
  };

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    try {
      configureTestSuite(suite);
    } catch (IOException e) {
      // ignore
    }
    return suite;
  }

  private static void configureTestSuite(TestSuite suite) throws IOException {
    for(String folderPath : TARGET_FOLDERS) {
      final File folder = new File(folderPath);
      if (!folder.exists()) {
        throw new RuntimeException("Folder not found: " + folder);
      }
      Files.walk(Paths.get(folder.toURI())).forEach(path -> {
        final File file = path.toFile();
        final String fileName = file.getName();
        if (file.isFile() && (fileName.endsWith(".k") || fileName.endsWith(".q"))) {
          suite.addTest(new ScriptsTest(file));
        }
      });
    }
  }

  private final File file;

  ScriptsTest(File file) {
    super(file.getName());
    this.file = file;
    setName("testScripts");
  }

  @Override
  public String getName() {
    return file.toString();
  }

  public void testScripts() throws Exception {
    final String f = file.getAbsolutePath();
    final byte[] bytes = Files.readAllBytes(Paths.get(f));
    final String content = new String(bytes);
    final long start = System.currentTimeMillis();
    final ASTNode tree = parse(content);
    final long time = System.currentTimeMillis() - start;
    final String msg = String.format(
      "%-30s %8sms  %10.2fKB",
      testFileName, time, bytes.length/1024.0
    );
    final String s = DebugUtil.nodeTreeToString(tree, true);
    if (hasParseError(s)) {
      throw new RuntimeException(s);
    } else {
      System.out.println(msg);
    }
  }


}
