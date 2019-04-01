package com.appian.intellij.k.settings;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@State(name = "Settings", storages = {@Storage("q-plugin-settings.xml")})
public class KSettingsService implements PersistentStateComponent<KSettings> {
  private final List<Listener> listeners = new ArrayList<>();
  private KSettings settings = new KSettings();

  public static KSettingsService getInstance() {
    return ServiceManager.getService(KSettingsService.class);
  }

  public KSettings getState() {
    return this.settings;
  }

  public KSettings getSettings() {
    return getState();
  }

  void setSettings(KSettings newSettings) {
    KSettings old = this.settings;
    this.settings = newSettings;
    fireSettingsChange(old);
  }

  @Override
  public void loadState(@NotNull KSettings state) {
    setSettings(state);
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  private void fireSettingsChange(KSettings old) {
    for (Listener listener : listeners) {
      listener.settingsChanged(old, this.settings);
    }
  }

  public interface Listener {
    void settingsChanged(KSettings oldSettings, KSettings newSettings);
  }
}

