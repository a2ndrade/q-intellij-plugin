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

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    try {
      for (File input : TestInputFiles.asFiles()) {
        suite.addTest(new ScriptsTest(input));
      }
    } catch (IOException e) {
      // ignore
    }
    return suite;
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
    final String msg = String.format("%-30s %8sms  %10.2fKB", testFileName, time, bytes.length / 1024.0);
    final String s = DebugUtil.nodeTreeToString(tree, true);
    if (hasParseError(s)) {
      throw new RuntimeException(s);
    } else {
      System.out.println(msg);
    }
  }

}
