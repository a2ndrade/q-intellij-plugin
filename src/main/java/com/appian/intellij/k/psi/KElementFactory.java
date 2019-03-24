package com.appian.intellij.k.psi;

import com.appian.intellij.k.KFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;

public final class KElementFactory {
  public static KUserId createKUserId(Project project, String name) {
    final KFile file = createFile(project, name);
    // file -> expression -> user_id
    return (KUserId)file.getFirstChild().getFirstChild();
  }

  public static KSymbol createKSymbol(Project project, String name) {
    if (name.charAt(0) != '`') {
      throw new RuntimeException(name + " is not a symbol");
    }
    final KFile file = createFile(project, name);
    // file -> expression -> user_id
    return (KSymbol)file.getFirstChild().getFirstChild();
  }

  public static KFile createFile(Project project, String text) {
    String name = "dummy.k";
    return (KFile)PsiFileFactory.getInstance(project).
        createFileFromText(name, KFileType.INSTANCE, text);
  }
}
