package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KArgs;
import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KExpression;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KLambdaParams;
import com.appian.intellij.k.psi.KTypes;
import com.appian.intellij.k.psi.KUserId;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

public class KParameterInfoHandler implements DumbAware,
    ParameterInfoHandlerWithTabActionSupport<KArgs,KParameterInfoHandler.KParameterInfo,KExpression> {

  private static final Pattern PARAM = Pattern.compile("@param\\s+(\\w+)\\s+(\\(.+\\))");

  @NotNull
  @Override
  public KExpression[] getActualParameters(@NotNull KArgs o) {
    return o.getExpressionList().toArray(new KExpression[0]);
  }

  @NotNull
  @Override
  public IElementType getActualParameterDelimiterType() {
    return KTypes.SEMICOLON;
  }

  @NotNull
  @Override
  public IElementType getActualParametersRBraceType() {
    return KTypes.CLOSE_BRACKET;
  }

  @NotNull
  @Override
  public Set<Class> getArgumentListAllowedParentClasses() {
    return Collections.singleton(KExpression.class);
  }

  @NotNull
  @Override
  public Set<? extends Class> getArgListStopSearchClasses() {
    return Collections.emptySet();
  }

  @NotNull
  @Override
  public Class<KArgs> getArgumentListClass() {
    return KArgs.class;
  }

  @Override
  public boolean couldShowInLookup() {
    return false;
  }

  @Nullable
  @Override
  public Object[] getParametersForLookup(
      LookupElement item, ParameterInfoContext context) {
    item.putCopyableUserData(null, null);
    return new Object[0];
  }

  @Nullable
  @Override
  public KArgs findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
    PsiElement element = context.getFile().findElementAt(context.getOffset());
    if (element == null) {
      return null;
    }
    final KArgs args = PsiTreeUtil.getParentOfType(element, KArgs.class);
    if (args == null) {
      return null;
    }
    final KUserId declaration = Optional.ofNullable(PsiTreeUtil.getPrevSiblingOfType(args, KUserId.class))
        .map(id -> new KReference(id, null).resolve())
        .filter(KUserId.class::isInstance)
        .map(KUserId.class::cast)
        .orElse(null);
    if (declaration == null) {
      return null;
    }
    return Optional.of(declaration)
        .map(PsiElement::getParent)
        .filter(KAssignment.class::isInstance)
        .map(KAssignment.class::cast)
        .map(KAssignment::getExpression)
        .map(e -> PsiTreeUtil.findChildOfType(e, KLambda.class))
        .map(lambda -> {
          KParameterInfo info = new KParameterInfo(declaration, lambda, args);
          context.setItemsToShow(new Object[] {info});
          return args;
        })
        .orElse(null);
  }

  @Override
  public void showParameterInfo(@NotNull KArgs element, @NotNull CreateParameterInfoContext context) {
    context.showHint(element, context.getOffset(), this);
  }

  @Nullable
  @Override
  public KArgs findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
    if (isOutsideOfInvocation(context)) {
      return null;
    }
    PsiElement element = context.getFile().findElementAt(context.getOffset());
    if (element == null) {
      return null;
    }
    return PsiTreeUtil.getParentOfType(element, KArgs.class);
  }

  private boolean isOutsideOfInvocation(UpdateParameterInfoContext context) {
    PsiElement currentElement = context.getFile().findElementAt(context.getOffset());
    PsiElement currentArgs = PsiTreeUtil.getParentOfType(currentElement, KArgs.class);
    PsiElement expectedArgs = context.getParameterOwner();
    return !Objects.equals(expectedArgs, currentArgs);
  }

  @Override
  public void updateParameterInfo(@NotNull KArgs args, @NotNull UpdateParameterInfoContext context) {
    int i = 0;
    KExpression arg = PsiTreeUtil.getChildOfType(args, KExpression.class);
    while (arg != null && arg.getTextOffset() + arg.getTextLength() + ";".length() <= context.getOffset()) {
      i++;
      arg = PsiTreeUtil.getNextSiblingOfType(arg, KExpression.class);
    }
    context.setCurrentParameter(i);
  }

  @Override
  public void updateUI(KParameterInfo p, @NotNull ParameterInfoUIContext context) {
    final KLambdaParams params = p.params.getLambdaParams();
    final KUserId id = p.declaration;
    if (params == null) { // implicit x;y;z params
      setupParameterInfoPresentation(id, Arrays.asList("x", "y", "z"), context);
      return;
    }
    if (params.getUserIdList().isEmpty()) { // no params
      context.setupRawUIComponentPresentation("no params");
      return;
    }
    final List<String> paramNames = params.getUserIdList().stream().map(KUserId::getName).collect(Collectors.toList());
    setupParameterInfoPresentation(id, paramNames, context);
  }

  private void setupParameterInfoPresentation(KUserId id, List<String> names, ParameterInfoUIContext context) {
    final Map<String,String> types = new HashMap<>();
    for (String line : KDocumentationProvider.getFunctionDocs(id)) {
      final Matcher matcher = PARAM.matcher(line);
      if (!matcher.find()) {
        continue;
      }
      final String n = matcher.group(1);
      final String t = matcher.group(2);
      types.put(n, t);
    }
    final List<String> namesAndTypes = new ArrayList<>();
    for (String name : names) {
      if (types.containsKey(name)) {
        namesAndTypes.add(name + " " + types.get(name));
      } else {
        namesAndTypes.add(name);
      }
    }
    final String display = String.format("%s[%s]", KUtil.getFqnOrName(id), String.join(";", namesAndTypes));
    int i = context.getCurrentParameterIndex();
    int start = -1, end = -1;
    if (i > -1 && i < namesAndTypes.size()) {
      start = display.indexOf('[') + 1;
      for (int j = 0; j < i; j++) {
        String name = namesAndTypes.get(j);
        start += name.length() + ";".length();
      }
      end = start + namesAndTypes.get(i).length();
    }
    context.setupUIComponentPresentation(display, start, end, false, false, false, context.getDefaultParameterColor());
  }

  static class KParameterInfo {
    final KUserId declaration;
    final KLambda params;
    final KArgs arguments;

    KParameterInfo(KUserId declaration, KLambda params, KArgs arguments) {
      this.declaration = declaration;
      this.params = params;
      this.arguments = arguments;
    }
  }
}
