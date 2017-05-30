package com.appian.intellij.k;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLocalAssignment;
import com.appian.intellij.k.psi.KUserId;
import com.google.common.base.Strings;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

public class KCompletionContributor extends CompletionContributor {

  public KCompletionContributor() {
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement().withLanguage(KLanguage.INSTANCE),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet resultSet) {
            final CompletionResultSet caseInsensitiveResultSet = resultSet.caseInsensitive();
            final PsiElement element = parameters.getOriginalPosition();
            final String input = Strings.nullToEmpty(element.getText());
            final KLambda enclosingLambda = PsiTreeUtil.getContextOfType(element, KLambda.class);
            // params
            final Map<String,KUserId> uniques = new LinkedHashMap<>();
            Optional.ofNullable(enclosingLambda)
                .map(KLambda::getLambdaParams)
                .map(params -> params.getUserIdList()
                    .stream()
                    .filter(param -> param.getName().contains(input)))
                .orElse(Stream.empty())
                .forEach(param -> uniques.putIfAbsent(param.getName(), param));
            // locals
            Optional.ofNullable(PsiTreeUtil.findChildrenOfType(enclosingLambda, KLocalAssignment.class).stream()
                .map(KLocalAssignment::getUserId)
                .filter(id -> id.getName().contains(input)))
                .orElse(Stream.empty())
                .forEach(local -> uniques.putIfAbsent(local.getName(), local));
            for (KUserId local : uniques.values()) {
              caseInsensitiveResultSet.addElement(LookupElementBuilder.create(local));
            }
            // globals (same file)
            final Project project = element.getProject();
            final VirtualFile sameFile = element.getContainingFile().getVirtualFile();
            Optional.ofNullable(KUtil.findFileIdentifiers(project, sameFile).stream()
                .filter(id -> id.getName().contains(input)))
                .orElse(Stream.empty())
                .forEach(global -> caseInsensitiveResultSet.addElement(LookupElementBuilder.create(global)));
            // globals (other files)
            if (caseInsensitiveResultSet.isStopped() || input.charAt(0) != '.') {
              return;
            }
            final Collection<VirtualFile> otherFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
            for (VirtualFile otherFile : otherFiles) {
              Optional.ofNullable(KUtil.findAllMatchingIdentifiers(project, otherFile, input, false, false).stream())
                  .orElse(Stream.empty())
                  .forEach(global -> {
                    final String fqn = global.getUserData(KUtil.FQN);
                    final LookupElementBuilder lookup = fqn == null ?
                        LookupElementBuilder.create(global) :
                        LookupElementBuilder.create(global, fqn);
                    caseInsensitiveResultSet.addElement(lookup);
                  });
            }
          }
        }
    );
  }

}
