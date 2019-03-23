package com.appian.intellij.k;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KTypes;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

public class KCompletionContributor extends CompletionContributor {

  static final String[] SYSTEM_FNS_Q = new String[] {"abs", "acos", "aj", "aj0", "all", "and", "any", "asc", "asin",
      "asof", "atan", "attr", "avg", "avgs", "bin", "binr", "by", "ceiling", "cols", "cor", "cos", "count", "cov",
      "cross", "csv", "cut", "delete", "deltas", "desc", "dev", "differ", "distinct", "div", "dsave", "each", "ej",
      "ema", "enlist", "eval", "except", "exec", "exit", "exp", "fby", "fills", "first", "fkeys", "flip", "floor",
      "from", "get", "getenv", "group", "gtime", "hclose", "hcount", "hdel", "hopen", "hsym", "iasc", "idesc", "ij",
      "in", "insert", "inter", "inv", "key", "keys", "last", "like", "lj", "ljf", "load", "log", "lower", "lsq",
      "ltime", "ltrim", "mavg", "max", "maxs", "mcount", "md5", "mdev", "med", "meta", "min", "mins", "mmax", "mmin",
      "mmu", "mod", "msum", "neg", "next", "not", "null", "or", "over", "parse", "peach", "pj", "prd", "prds", "prev",
      "prior", "rand", "rank", "ratios", "raze", "read0", "read1", "reciprocal", "reval", "reverse", "rload", "rotate",
      "rsave", "rtrim", "save", "scan", "scov", "sdev", "select", "set", "setenv", "show", "signum", "sin", "sqrt",
      "ss", "ssr", "string", "sublist", "sum", "sums", "sv", "svar", "system", "tables", "tan", "til", "trim", "type",
      "uj", "ujf", "ungroup", "union", "update", "upper", "upsert", "value", "var", "view", "views", "vs", "wavg",
      "where", "within", "wj", "wj1", "wsum", "ww", "xasc", "xbar", "xcol", "xcols", "xdesc", "xexp", "xgroup", "xkey",
      "xlog", "xprev", "xrank"
  };

  static {
    Arrays.sort(SYSTEM_FNS_Q);
  }

  public static boolean isSystemFn(String fnName) {
    if (Arrays.binarySearch(SYSTEM_FNS_Q, fnName) >= 0) {
      return true;
    }
    if (fnName.startsWith(".z.")) {
      return true;
    }
    return false;
  }

  public KCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(KTypes.USER_IDENTIFIER).withLanguage(KLanguage.INSTANCE),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(
              @NotNull CompletionParameters parameters,
              ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            final PsiElement element = parameters.getOriginalPosition();
            if (element == null) {
              return;
            }

            final CompletionResultSet caseInsensitiveResultSet = resultSet.caseInsensitive();
            final Map<String,LookupElementBuilder> uniques = new LinkedHashMap<>();

            contributeSystemFunctions(element, uniques);
            if (caseInsensitiveResultSet.isStopped()) {
              return;
            }

            contributeParams(element, uniques);
            if (caseInsensitiveResultSet.isStopped()) {
              return;
            }

            contributeLocals(element, uniques);
            if (caseInsensitiveResultSet.isStopped()) {
              return;
            }

            contributeSameFileGlobals(element, uniques);
            if (caseInsensitiveResultSet.isStopped()) {
              return;
            }

            // globals (other files)
            contributeOtherFilesGlobals(element, uniques);
            if (caseInsensitiveResultSet.isStopped()) {
              return;
            }

            uniques.values().forEach(caseInsensitiveResultSet::addElement);
          }
        });
  }

  @NotNull
  private static String getInput(PsiElement element) {
    return element.getText() == null ? "" : element.getText();
  }

  private static void contributeOtherFilesGlobals(PsiElement element, Map<String,LookupElementBuilder> uniques) {
    String input = getInput(element);
    String sameFilePath = element.getContainingFile().getVirtualFile().getCanonicalPath();
    KUserIdCache cache = KUserIdCache.getInstance();
    Collection<VirtualFile> otherFiles = FileTypeIndex.getFiles(KFileType.INSTANCE,
        GlobalSearchScope.allScope(element.getProject()));
    for (VirtualFile otherFile : otherFiles) {
      if (sameFilePath != null && sameFilePath.equals(otherFile.getCanonicalPath())) {
        continue; // already processed above
      }
      cache.findAllIdentifiers(element.getProject(), otherFile, input, new KUtil.PrefixMatcher(input))
          .forEach(global -> uniques.computeIfAbsent(global.getName(), g -> Optional.ofNullable(KUtil.getFqn(global))
              .map(fqn -> LookupElementBuilder.create(global, fqn))
              .orElseGet(() -> LookupElementBuilder.create(global))));
    }
  }

  private static void contributeSameFileGlobals(PsiElement element, Map<String,LookupElementBuilder> uniques) {
    String input = getInput(element);
    KUtil.findIdentifiers(element.getProject(), element.getContainingFile().getVirtualFile())
        .stream()
        .filter(id -> id.getName().contains(input))
        .forEach(global -> uniques.computeIfAbsent(global.getName(), LookupElementBuilder::create));
  }

  private static void contributeLocals(PsiElement element, Map<String,LookupElementBuilder> uniques) {
    String input = getInput(element);
    KLambda enclosingLambda = PsiTreeUtil.getContextOfType(element, KLambda.class);
    PsiTreeUtil.findChildrenOfType(enclosingLambda, KAssignment.class)
        .stream()
        .map(KAssignment::getUserId)
        .filter(id -> id.getName().contains(input))
        .forEach(local -> uniques.computeIfAbsent(local.getName(), n -> LookupElementBuilder.create(local)));
  }

  private static void contributeParams(PsiElement element, Map<String,LookupElementBuilder> uniques) {
    String input = getInput(element);
    Optional.ofNullable(PsiTreeUtil.getContextOfType(element, KLambda.class))
        .map(KLambda::getLambdaParams)
        .map(params -> params.getUserIdList().stream().filter(param -> param.getName().contains(input)))
        .orElse(Stream.empty())
        .forEach(param -> uniques.computeIfAbsent(param.getName(), n -> LookupElementBuilder.create(param)));
  }

  private static void contributeSystemFunctions(PsiElement element, Map<String,LookupElementBuilder> uniques) {
    String input = getInput(element);
    if (input.charAt(0) == '.') {
      return; // ignore for completion b/c real declarations are in q.k or in app code as handle fns
    }
    int i = Math.abs(Arrays.binarySearch(SYSTEM_FNS_Q, input) + 1);
    while (i < SYSTEM_FNS_Q.length && SYSTEM_FNS_Q[i].startsWith(input)) {
      uniques.computeIfAbsent(SYSTEM_FNS_Q[i++], LookupElementBuilder::create);
    }
  }

}
