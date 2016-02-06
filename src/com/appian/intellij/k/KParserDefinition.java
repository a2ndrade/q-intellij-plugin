package com.appian.intellij.k;

import java.io.Reader;

import com.appian.intellij.k.parser.KParser;
import com.appian.intellij.k.psi.KFile;
import com.appian.intellij.k.psi.KTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;

public final class KParserDefinition implements ParserDefinition {

  public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
  public static final TokenSet COMMENTS = TokenSet.create(KTypes.COMMENT);
  public static final TokenSet STRING_LITERALS = TokenSet.create(KTypes.SYMBOL, KTypes.STRING,
    KTypes.SYMBOL_VECTOR);

  public static final IFileElementType FILE = new IFileElementType(
    Language.<KLanguage>findInstance(KLanguage.class));

  @Override
  public Lexer createLexer(Project project) {
    return new FlexAdapter(new KLexer((Reader)null));
  }

  @Override
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACES;
  }

  @Override
  public TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @Override
  public TokenSet getStringLiteralElements() {
    return STRING_LITERALS;
  }

  @Override
  public PsiParser createParser(final Project project) {
    return new KParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new KFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override
  public PsiElement createElement(ASTNode node) {
    return KTypes.Factory.createElement(node);
  }
}
