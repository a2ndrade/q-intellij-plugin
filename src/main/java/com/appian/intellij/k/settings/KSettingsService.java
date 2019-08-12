package com.appian.intellij.k.settings;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@State(name = "Settings", storages = {@Storage("q-plugin-settings.xml")})
public class KSettingsService implements PersistentStateComponent<KSettings> {
  private static final KSettings INITIAL_SETTINGS = new KSettings();

  private final List<Listener> listeners = new ArrayList<>();
  private KSettings settings = INITIAL_SETTINGS;

  public static KSettingsService getInstance() {
    return ServiceManager.getService(KSettingsService.class);
  }

  @Override
  public KSettings getState() {
    return this.settings;
  }

  public KSettings getSettings() {
    return getState();
  }

  public void setSettings(KSettings newSettings) {
    KSettings old = this.settings;

    PasswordSafe safe = PasswordSafe.getInstance();
    if (old == INITIAL_SETTINGS) {
      // initialization, read credentials
      for (KServerSpec s : newSettings.getServers()) {
        Credentials credentials = safe.get(new CredentialAttributes(getCredentialsServiceName(s.getName())));
        if (credentials != null) {
          s.setUser(credentials.getUserName());
          s.setPassword(credentials.getPasswordAsString());
        }
      }
    } else {
      // new settings, delete unnecessary credentials, update
      Set<String> toDelete = Sets.difference(old.getServers().stream().map(KServerSpec::getName).collect(toSet()),
          newSettings.getServers().stream().map(KServerSpec::getName).collect(toSet()));

      // delete credentials for the servers that are no longer needed
      for (String n : toDelete) {
        safe.set(new CredentialAttributes(getCredentialsServiceName(n)), null);
      }

      // store credentials
      for (KServerSpec spec : newSettings.getServers()) {
        safe.set(new CredentialAttributes(getCredentialsServiceName(spec.getName())),
            new Credentials(spec.getUser(), spec.getPassword()));
      }
    }

    this.settings = newSettings;
    fireSettingsChange(old);
  }

  @NotNull
  private static String getCredentialsServiceName(String serverName) {
    return "Q Server:" + serverName;
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

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public interface Listener {
    void settingsChanged(KSettings oldSettings, KSettings newSettings);
  }
}

