package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.DebugUtil;

public class ScriptsTest extends KParserTest {

  private static final String[] TARGET_FOLDERS = new String[] {
    "/Users/antonio.andrade/ae/c/server/_lib",
    "/Users/antonio.andrade/ae/c/server/process/common",
    "/Users/antonio.andrade/ae/c/server/process/exec",
    "/Users/antonio.andrade/ae/c/server/process/design",
    "/Users/antonio.andrade/ae/c/server/process/events",
    "/Users/antonio.andrade/ae/c/server/process/exec",
    "/Users/antonio.andrade/ae/c/server/process/migration",
    "/Users/antonio.andrade/ae/c/server/process/analytics"
  };

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    for(String folderPath : TARGET_FOLDERS) {
      final File folder = new File(folderPath);
      for(String fileName : folder.list()) {
        final File f = new File(folder, fileName);
        if (f.isFile() && fileName.endsWith(".k")) {
          suite.addTest(new ScriptsTest(f));
        }
      }
    }
    return suite;
  }

  private final File file;

  ScriptsTest(File file) {
    super(file.getName());
    this.file = file;
    setName("testScripts");
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
      throw new RuntimeException(msg);
    } else {
      System.out.println(msg);
    }
  }


}
