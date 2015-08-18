package com.appian.intellij.k;

import junit.framework.Assert;

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

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.ParsingTestCase;

public final class KParserTest extends ParsingTestCase {

  private static final ParserDefinition SPEC = new KParserDefinition();
  private static final PsiParser PARSER = SPEC.createParser(null);

  private static final String S0 = "=============";
  private static final String S1 = "-------------";

  public KParserTest(String dataPath) {
    super("", "k", SPEC);
    setName("testParser");
  }

  @Override
  protected String getTestDataPath() {
    return "test-data";
  }

  public void testParser() throws Exception {
    final File f = new File("test-data/test-cases.txt");
    final String[] testCases = readFileIntoSections(f, S0);
    for(String testCase : testCases) {
      final String[] inOut = readFileIntoSections(testCase, S1);
      final String actual = toString(parse(inOut[0]));
      final String expected = inOut[1];
      Assert.assertEquals(expected, actual);
    }
  }

  @NotNull
  private String toString(ASTNode tree) {
    return DebugUtil.nodeTreeToString(tree, true);
  }

  @NotNull
  private ASTNode parse(String expression) {
    final IFileElementType fileType = KParserDefinition.FILE;
    final PsiBuilder builder = newPsiBuilder(expression);
    return PARSER.parse(fileType, builder);
  }

  @NotNull
  private PsiBuilder newPsiBuilder(String expression) {
    final Lexer lexer = SPEC.createLexer(null);
    final PsiBuilderFactory factory = PsiBuilderFactory.getInstance();
    return factory.createBuilder(SPEC, lexer, expression);
  }

  static String[] readFileIntoSections(Object source, String sectionsSeparator)
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

  static List<String> readLinesIncludingLineTerminators(Object o)
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

}
