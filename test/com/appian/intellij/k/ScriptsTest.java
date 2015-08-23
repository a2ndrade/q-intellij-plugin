package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.DebugUtil;

public class ScriptsTest extends KParserTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final File folder = new File("/Users/antonio.andrade/ae/c/server/_lib");
    for(String fileName : folder.list()) {
      final File f = new File(folder, fileName);
      if (f.isFile()) {
        suite.addTest(new ScriptsTest(fileName));
      }
    }
    return suite;
  }

  ScriptsTest(String testFileName) {
    super(testFileName);
    setName("testScripts");
  }

  public void testScripts() throws Exception {
    final String f = "/Users/antonio.andrade/ae/c/server/_lib/"+testFileName;
    final String content = new String(Files.readAllBytes(Paths.get(f)));
    final long start = System.currentTimeMillis();
    final ASTNode tree = parse(content);
    final long time = System.currentTimeMillis() - start;
    final String msg = testFileName + ":\t\t\t\t\t\t" + time + "ms\t\t" + content.length();
    final String s = DebugUtil.nodeTreeToString(tree, true);
    if (hasParseError(s)) {
      throw new RuntimeException(msg);
    } else {
      System.out.println(msg);
    }
  }


}
