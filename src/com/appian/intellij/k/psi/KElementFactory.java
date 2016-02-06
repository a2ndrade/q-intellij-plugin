package com.appian.intellij.k.psi;

import com.appian.intellij.k.KFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;

public final class KElementFactory {
  public static KUserId createProperty(Project project, String name) {
    final KFile file = createFile(project, name);
    return (KUserId)file.getFirstChild();
  }

  public static KFile createFile(Project project, String text) {
    String name = "dummy.k";
    return (KFile) PsiFileFactory.getInstance(project).
      createFileFromText(name, KFileType.INSTANCE, text);
  }
}
