package com.appian.intellij.k;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.ParsingTestCase;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class KParserTest extends ParsingTestCase {

  private static final ParserDefinition SPEC = new KParserDefinition();
  private static final PsiParser PARSER = SPEC.createParser(null);

  static final String TEST_DATA_FOLDERS_PATH = "src/test/resources/" + KParserTest.class.getName().replace('.', '/');
  static final String INPUT_OUTPUT_SEPARATOR = "------------>";
  static final String TEST_CASE_SEPARATOR = "============|";

  public static Test suite() {
    final TestSuite suite = new TestSuite();
    final File folder = new File(TEST_DATA_FOLDERS_PATH);
    final String[] fileNames = folder.list();
    if (fileNames == null) {
      throw new RuntimeException("Folder not found: " + folder);
    }
    for (String fileName : fileNames) {
      if (fileName.startsWith("_")) { // e.g. sandbox
        continue;
      }
      suite.addTest(new KParserTest(fileName));
    }
    return suite;
  }

  final String testFileName;

  KParserTest(String testFileName) {
    this(testFileName, "k");
  }

  KParserTest(String testFileName, String ext) {
    super("", ext, SPEC);
    setName("testParser");
    this.testFileName = testFileName;
  }

  @Override
  public String getName() {
    return testFileName;
  }

  public void testParser() throws Exception {
    final File input = new File(getTestDataPath() + "/" + testFileName);
    final String[] testCases = readFileIntoSections(input, TEST_CASE_SEPARATOR);
    for (String testCase : testCases) {
      if (testCase.isEmpty()) {
        continue;
      }
      final String[] inOut = readFileIntoSections(testCase, INPUT_OUTPUT_SEPARATOR);
      final String expression = inOut[0].trim();
      final String actual = parseAsString(expression).trim();
      final String expected = inOut[1].trim();
      final boolean errorExpected = hasParseError(expected);
      if (!errorExpected && hasParseError(actual)) {
        throw new RuntimeException(actual);
      }
      Assert.assertEquals(expected, actual);
    }
  }

  final boolean hasParseError(String treeAsString) {
    return treeAsString.contains("PsiErrorElement");
  }

  String parseAsString(String expression) {
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

  final ASTNode parse(String expression) {
    final IFileElementType fileType = KParserDefinition.FILE;
    final PsiBuilder builder = newPsiBuilder(expression);
    return PARSER.parse(fileType, builder);
  }

  private PsiBuilder newPsiBuilder(String expression) {
    final Lexer lexer = SPEC.createLexer(null);
    final PsiBuilderFactory factory = PsiBuilderFactory.getInstance();
    return factory.createBuilder(SPEC, lexer, expression);
  }

  static String[] readFileIntoSections(Object source, String sectionsSeparator) throws IOException {
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

  private static List<String> readLinesIncludingLineTerminators(Object o) throws IOException {
    try (Reader fr = o instanceof File ? new FileReader((File)o) : new StringReader((String)o);
        BufferedReader br = new BufferedReader(fr);
        PushbackReader pr = new PushbackReader(br)) {
      List<String> linesList = new ArrayList<String>();
      int c;
      do {
        StringBuffer lineSb = new StringBuffer();
        while ((c = pr.read()) != -1) {
          lineSb.append((char)c);
          if (c == '\n') {
            break;
          } else if (c == '\r') {
            c = pr.read();
            if (c == '\n') {
              lineSb.append((char)c);
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
    return TEST_DATA_FOLDERS_PATH;
  }

}
