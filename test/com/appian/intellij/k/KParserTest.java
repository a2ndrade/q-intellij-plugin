package com.appian.intellij.k;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.ParsingTestCase;

public class KParserTest extends ParsingTestCase {

  private static final ParserDefinition SPEC = new KParserDefinition();
  private static final PsiParser PARSER = SPEC.createParser(null);

  private static final String TEST_DATA_FOLDER_NAME = "test-data";
  private static final String INPUT_OUTPUT_SEPARATOR = "------------>";
  private static final String TEST_CASE_SEPARATOR = "============|";

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final File folder = new File(TEST_DATA_FOLDER_NAME);
    for(String fileName : folder.list()) {
      suite.addTest(new KParserTest(fileName));
    }
    return suite;
  }

  public static Test suite0() {
    final TestSuite suite = new TestSuite();
    final File folder = new File("/Users/antonio.andrade/ae/c/server/_lib");
    for(String fileName : folder.list()) {
      final File f = new File(folder, fileName);
      if (f.isFile()) {
        suite.addTest(new KParserTest(fileName));
      }
    }
    return suite;
  }

  private final String testFileName;

  KParserTest(String testFileName) {
    super("", "k", SPEC);
    setName("testScripts");
    setName("testParser");
    this.testFileName = testFileName;
  }

  @Override
  public String getName() {
    return testFileName;
  }

  public void testScripts() throws Exception {
    final String f = "/Users/antonio.andrade/ae/c/server/_lib/"+testFileName;
    final String content = new String(Files.readAllBytes(Paths.get(f)));
    final long start = System.currentTimeMillis();
    final ASTNode tree = parse(content);
    final long time = System.currentTimeMillis() - start;
    final String msg = testFileName + ":\t\t\t\t\t\t" + time + "ms\t\t" + content.length();
    final String s = DebugUtil.nodeTreeToString(tree, true);
    if (s.contains("PsiErrorElement")) {
      throw new RuntimeException(msg);
    } else {
      System.out.println(msg);
    }
  }

  public void testParser() throws Exception {
    final File input = new File(getTestDataPath() + "/" + testFileName);
    final String[] testCases = readFileIntoSections(input, TEST_CASE_SEPARATOR);
    for(String testCase : testCases) {
      if (testCase.isEmpty()) {
        continue;
      }
      final String[] inOut = readFileIntoSections(testCase, INPUT_OUTPUT_SEPARATOR);
      final String expression = inOut[0].trim();
      final String actual = parseAsString(expression).trim();
      final String expected = inOut[1].trim();
      Assert.assertEquals(expected, actual);
    }
  }

  private String parseAsString(String expression) {
    if (expression.isEmpty()) {
      return expression;
    }
    final ASTNode tree = parse(expression);
    final ASTNode root;
    if (tree.getFirstChildNode() == tree.getLastChildNode()) {
      root = tree.getFirstChildNode();
    } else {
      root = tree;
    }
    return DebugUtil.nodeTreeToString(root, true);
  }

  private ASTNode parse(String expression) {
    final IFileElementType fileType = KParserDefinition.FILE;
    final PsiBuilder builder = newPsiBuilder(expression);
    return PARSER.parse(fileType, builder);
  }

  private PsiBuilder newPsiBuilder(String expression) {
    final Lexer lexer = SPEC.createLexer(null);
    final PsiBuilderFactory factory = PsiBuilderFactory.getInstance();
    return factory.createBuilder(SPEC, lexer, expression);
  }

  private static String[] readFileIntoSections(Object source, String sectionsSeparator)
    throws IOException {
    final List<String> linesList = readLinesIncludingLineTerminators(source);
    final List<String> sectionsList = new ArrayList<String>();
    StringBuffer currentSection = null;
    final Iterator<String> it = linesList.iterator();
    while (it.hasNext()) {
      String line = it.next();
      if (line.startsWith(sectionsSeparator)) {
        if (currentSection != null) {
          sectionsList.add(currentSection.toString());
        }
        currentSection = new StringBuffer();
      } else {
        if (currentSection == null) {
          currentSection = new StringBuffer();
        }
        currentSection.append(line);
      }
    }
    if (currentSection != null) {
      sectionsList.add(currentSection.toString());
    }
    return sectionsList.toArray(new String[0]);
  }

  private static List<String> readLinesIncludingLineTerminators(Object o)
    throws IOException {
    try(
      Reader fr = o instanceof File
        ? new FileReader((File)o)
        : new StringReader((String)o);
      BufferedReader br = new BufferedReader(fr);
      PushbackReader pr = new PushbackReader(br)) {
      List<String> linesList = new ArrayList<String>();
      int c;
      do {
        StringBuffer lineSb = new StringBuffer();
        while ((c = pr.read()) != -1) {
          lineSb.append((char) c);
          if (c == '\n') {
            break;
          } else if (c == '\r') {
            c = pr.read();
            if (c == '\n') {
              lineSb.append((char) c);
            } else if (c != -1) {
              pr.unread(c);
            }
            break;
          }
        }
        linesList.add(lineSb.toString());
      } while (c != -1);
      return linesList;
    }
  }

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_FOLDER_NAME;
  }

}
