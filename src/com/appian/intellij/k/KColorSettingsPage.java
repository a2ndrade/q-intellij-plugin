package com.appian.intellij.k;

import java.util.Map;

import javax.swing.*;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;

public final class KColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor("Keyword", KSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor("SysFunction", KSyntaxHighlighter.IDENTIFIER_SYS),
    new AttributesDescriptor("Operator", KSyntaxHighlighter.VERB),
    new AttributesDescriptor("Adverb", KSyntaxHighlighter.ADVERB),
    new AttributesDescriptor("String", KSyntaxHighlighter.STRING),
    new AttributesDescriptor("Symbol", KSyntaxHighlighter.SYMBOL),
    new AttributesDescriptor("Number", KSyntaxHighlighter.NUMBER),
    new AttributesDescriptor("Identifier", KSyntaxHighlighter.IDENTIFIER)
  };

  @Override
  public Icon getIcon() {
    return KIcons.FILE;
  }

  @Override
  public SyntaxHighlighter getHighlighter() {
    return new KSyntaxHighlighter();
  }

  @Override
  public String getDemoText() {
    return "/ comment\n" +
      "\n" +
      "1 2 2.4 0N 3 43 0i / numbers\n" +
      "\n" +
      "\"string\"\n" +
      "\n" +
      "`symbol\n" +
      "\n" +
      "identifier\n" +
      "\n" +
      "_systemFunction";
  }

  @Override
  public Map<String,TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public String getDisplayName() {
    return "K";
  }
}
