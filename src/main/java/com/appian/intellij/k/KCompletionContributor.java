package com.appian.intellij.k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.psi.KAssignment;
import com.appian.intellij.k.psi.KLambda;
import com.appian.intellij.k.psi.KNamedElement;
import com.appian.intellij.k.psi.KTypes;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionService;
import com.intellij.codeInsight.completion.CompletionSorter;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.PriorityWeigher;
import com.intellij.codeInsight.completion.impl.PreferStartMatching;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.navigation.ItemPresentation;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.Weigher;
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

  static boolean isSystemFn(String fnName) {
    return Arrays.binarySearch(SYSTEM_FNS_Q, fnName) >= 0 || fnName.startsWith(".z.");
  }

  public KCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(KTypes.USER_IDENTIFIER).withLanguage(KLanguage.INSTANCE),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(
              @NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            final PsiElement element = parameters.getOriginalPosition();
            if (element == null) {
              return;
            }

            // customize sorting:
            // 1. give preference to items that start with user input
            // 2. use explicit priorities as defined by CompletionContributionType, order being:
            // locals, params, same file globals, system functions and finally globals from other files
            CompletionSorter sorter = CompletionService.getCompletionService()
                .emptySorter()
                .weigh(new PreferStartMatching())
                .weighAfter("middleMatching", new LookupElementWeigher("priority") {
                  final Weigher<LookupElement,CompletionLocation> priorityWeigher = new PriorityWeigher();

                  @Nullable
                  @Override
                  public Comparable weigh(@NotNull LookupElement element) {
                    return priorityWeigher.weigh(element, new CompletionLocation(parameters));
                  }
                });

            final CompletionResultSet caseInsensitiveResultSet = resultSet.caseInsensitive()
                .withRelevanceSorter(sorter);
            final Map<String,LookupElement> uniques = new LinkedHashMap<>();

            for (CompletionContributionType ct : CompletionContributionType.values()) {
              for (ItemPresentation p : ct.getCompletions(element)) {
                uniques.computeIfAbsent(p.getPresentableText(), t -> ct.createLookupElement(p));
                if (caseInsensitiveResultSet.isStopped()) {
                  return;
                }
              }
            }
            uniques.values().forEach(caseInsensitiveResultSet::addElement);
          }
        });
  }

  @NotNull
  private static String getText(PsiElement element) {
    return element.getText() == null ? "" : element.getText();
  }

  /**
   * Different types of completions
   */
  private enum CompletionContributionType {/**
   * Local variable completions
   */
  LOCAL {
        @Override
        List<ItemPresentation> getCompletions(PsiElement element) {
          String input = getText(element);
          return PsiTreeUtil.findChildrenOfType(PsiTreeUtil.getContextOfType(element, KLambda.class), KAssignment.class)
              .stream()
              .map(KAssignment::getUserId)
              .filter(id -> id.getName().contains(input))
              .map(id -> suppressLocationString(id.getPresentation()))
              .collect(Collectors.toList());
        }
      },
    /**
     * Lambda parameter completions
     */
    PARAM {
      @Override
      List<ItemPresentation> getCompletions(PsiElement element) {
        String input = getText(element);
        return Optional.ofNullable(PsiTreeUtil.getContextOfType(element, KLambda.class))
            .map(KLambda::getLambdaParams)
            .map(params -> params.getUserIdList().stream().filter(param -> param.getName().contains(input)))
            .orElse(Stream.empty())
            .map(id -> suppressLocationString(id.getPresentation()))
            .collect(Collectors.toList());
      }
    },
    /**
     * Global from the same file completion
     */
    SAME_FILE_GLOBAL {
      @Override
      List<ItemPresentation> getCompletions(PsiElement input) {
        String inputText = getText(input);
        return KUtil.findIdentifiers(input.getProject(), input.getContainingFile().getVirtualFile())
            .stream()
            .filter(id -> id.getName().contains(inputText) && Arrays.binarySearch(SYSTEM_FNS_Q, id.getName()) < 0)
            .map(id -> suppressLocationString(id.getPresentation()))
            .collect(Collectors.toList());
      }
    },
    /**
     * System function completions
     */
    SYSTEM_FUNCTION {
      @Override
      List<ItemPresentation> getCompletions(PsiElement element) {
        String input = getText(element);
        if (input.charAt(0) == '.') {
          return Collections.emptyList(); // ignore for completion b/c real declarations are in q.k or in app code as handle fns
        }

        List<ItemPresentation> completions = new ArrayList<>();
        int i = Math.abs(Arrays.binarySearch(SYSTEM_FNS_Q, input) + 1);
        while (i < SYSTEM_FNS_Q.length && SYSTEM_FNS_Q[i].startsWith(input)) {
          completions.add(new SystemFunctionPresentation(SYSTEM_FNS_Q[i++]));
        }
        return completions;
      }
    },
    /**
     * Global from some other file completion suggestion
     */
    EXTERNAL_GLOBAL {
      @Override
      List<ItemPresentation> getCompletions(PsiElement element) {
        String input = getText(element);
        String elementFilePath = element.getContainingFile().getVirtualFile().getCanonicalPath();
        KUserIdCache cache = KUserIdCache.getInstance();
        return FileTypeIndex.getFiles(KFileType.INSTANCE, GlobalSearchScope.allScope(element.getProject()))
            .stream()
            .filter(file -> elementFilePath == null || !elementFilePath.equals(file.getCanonicalPath()))
            .flatMap(file -> cache.findAllIdentifiers(element.getProject(), file, input, new KUtil.PrefixMatcher(input))
                .stream())
            .map(KNamedElement::getPresentation)
            .collect(Collectors.toList());
      }
    };

    LookupElement createLookupElement(ItemPresentation presentation) {
      //noinspection ConstantConditions
      LookupElement element = LookupElementBuilder.create(presentation.getPresentableText())
          .withIcon(presentation.getIcon(false))
          .withTypeText(presentation.getLocationString(), true);
      return PrioritizedLookupElement.withPriority(element, getPriority());
    }

    /**
     * @return the priority for PriorityWeigher.
     * For simplicity
     * relying on the order of items in the enum
     * to determine which types of completions
     * will be prioritized.
     */
    double getPriority() {
      return ordinal();
    }

    /**
     * @param input an element representing user input
     * @return a list of presentations for completion suggestions of this type
     */
    abstract List<ItemPresentation> getCompletions(PsiElement input);}

  /**
   * @param presentation a presentation
   * @return identical presentation without location string
   */
  private static ItemPresentation suppressLocationString(ItemPresentation presentation) {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return presentation.getPresentableText();
      }

      @Nullable
      @Override
      public String getLocationString() {
        return "";
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return presentation.getIcon(unused);
      }
    };
  }

  private static class SystemFunctionPresentation implements ItemPresentation {
    private final String name;

    private SystemFunctionPresentation(String name) {
      this.name = Objects.requireNonNull(name);
    }

    @NotNull
    @Override
    public String getPresentableText() {
      return name;
    }

    @NotNull
    @Override
    public String getLocationString() {
      return "";
    }

    @NotNull
    @Override
    public Icon getIcon(boolean b) {
      return KIcons.SYSTEM_FUNCTION;
    }
  }
}

