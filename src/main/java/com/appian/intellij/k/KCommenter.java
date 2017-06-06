package com.appian.intellij.k;

import com.intellij.lang.Commenter;

public final class KCommenter implements Commenter {

  @Override
  public String getLineCommentPrefix() {
    return "/";
  }


  @Override
  public String getBlockCommentPrefix() {
    return null;
  }


  @Override
  public String getBlockCommentSuffix() {
    return null;
  }


  @Override
  public String getCommentedBlockCommentPrefix() {
    return null;
  }


  @Override
  public String getCommentedBlockCommentSuffix() {
    return null;
  }
}
