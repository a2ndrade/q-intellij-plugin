package com.appian.intellij.k.struct;

import org.jetbrains.annotations.NotNull;

import com.appian.intellij.k.psi.KUserId;
import com.appian.intellij.k.settings.KSettings;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;

final class KStructureViewModel extends StructureViewModelBase
    implements StructureViewModel.ElementInfoProvider, KSettingsService.Listener {
  @SuppressWarnings("WeakerAccess")
  public KStructureViewModel(PsiFile psiFile) {
    super(psiFile, new KStructureViewElement(psiFile));
  }

  @Override
  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[] {KVisibilitySorter.INSTANCE, Sorter.ALPHA_SORTER};
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return element.getValue() instanceof KUserId;
  }

  @NotNull
  @Override
  public Filter[] getFilters() {
    return new Filter[] {new KGlobalsFilter(), new KPublicItemsFilter()};
  }

  public void registerListeners() {
    KSettingsService.getInstance().addListener(this);
  }

  @Override
  public void dispose() {
    KSettingsService.getInstance().removeListener(this);
    super.dispose();
  }

  @Override
  public void settingsChanged(KSettings oldSettings, KSettings newSettings) {
    fireModelUpdate();
  }
}
