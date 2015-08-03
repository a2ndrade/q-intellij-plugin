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
    new AttributesDescriptor("Key", KSyntaxHighlighter.KEY),
    new AttributesDescriptor("Separator", KSyntaxHighlighter.SEPARATOR),
    new AttributesDescriptor("Value", KSyntaxHighlighter.VALUE),
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
    return "# You are reading the \".properties\" entry.\n" +
      "! The exclamation mark can also mark text as comments.\n" +
      "website = http://en.wikipedia.org/\n" +
      "language = English\n" +
      "# The backslash below tells the application to continue reading\n" +
      "# the value onto the next line.\n" +
      "message = Welcome to \\\n" +
      "          Wikipedia!\n" +
      "# Add spaces to the key\n" +
      "key\\ with\\ spaces = This is the value that could be looked up with the key \"key with spaces\".\n" +
      "# Unicode\n" +
      "tab : \\u0009";
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
