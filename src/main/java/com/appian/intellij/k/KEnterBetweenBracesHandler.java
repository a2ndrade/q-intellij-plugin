package com.appian.intellij.k;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesHandler;

public class KEnterBetweenBracesHandler extends EnterBetweenBracesHandler {

  @Override
  protected boolean isBracePair(char c1, char c2) {
    return super.isBracePair(c1, c2) || (c1 == '[' && c2 == ']');
  }

}
