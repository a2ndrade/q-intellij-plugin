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
    new AttributesDescriptor("Number", KSyntaxHighlighter.NUMBER),
    new AttributesDescriptor("String", KSyntaxHighlighter.STRING),
    new AttributesDescriptor("Symbol", KSyntaxHighlighter.SYMBOL),
    new AttributesDescriptor("Identifier", KSyntaxHighlighter.IDENTIFIER),
    new AttributesDescriptor("Verb", KSyntaxHighlighter.VERB),
    new AttributesDescriptor("SysFunction", KSyntaxHighlighter.IDENTIFIER_SYS),
    new AttributesDescriptor("Adverb", KSyntaxHighlighter.ADVERB),
    new AttributesDescriptor("Keyword", KSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor("Command", KSyntaxHighlighter.IDENTIFIER_SYS)
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
    return "1 2 2.4 0N 3 43 0i / numbers\n" +
      "\n" +
      "\"string\"\n" +
      "\n" +
      "`symbol\n" +
      "\n" +
      "identifier\n" +
      "\n" +
      "(+;*:;~=) / verbs\n" +
      "\n" +
      "+/ / derived verb\n" +
      "\n" +
      "_in / system function\n" +
      "\n" +
      "/:\\:' / adverb\n" +
      "\n" +
      "(:[];if[];do[];while[];0:)  / keywords and io commands\n" +
      "\n" +
      "\\d .some.dir";
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
