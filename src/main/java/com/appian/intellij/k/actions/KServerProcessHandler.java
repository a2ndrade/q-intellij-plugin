package com.appian.intellij.k.actions;

import static com.appian.intellij.k.actions.KActionUtil.runInBackground;
import static com.appian.intellij.k.actions.KActionUtil.showInformationNotification;
import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.USER_INPUT;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.settings.KServerSpec;
import com.appian.intellij.k.settings.KSettings;
import com.appian.intellij.k.settings.KSettingsService;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

import kx.c;

public class KServerProcessHandler extends ProcessHandler {
  private final String serverId;
  private final AtomicBoolean running = new AtomicBoolean();

  KServerProcessHandler(String serverId) {
    this.serverId = requireNonNull(serverId);
  }

  String getServerId() {
    return serverId;
  }

  @Override
  protected void destroyProcessImpl() {
    notifyProcessTerminated(0);
  }

  @Override
  protected void detachProcessImpl() {
    notifyProcessDetached();
  }

  private void closeConnection(c conn) {
    try {
      conn.close();
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public boolean detachIsDefault() {
    return false;
  }

  @Nullable
  @Override
  public OutputStream getProcessInput() {
    return null;
  }

  public boolean isSilentlyDestroyOnClose() {
    return true;
  }

  private c getConnection() throws IOException, c.KException {
    KSettings settings = KSettingsService.getInstance().getSettings();
    KServerSpec server = settings
        .getServers()
        .stream()
        .filter(s -> s.getId().equals(serverId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(
            "Connection details for server " + serverId + " not found, please close the console and try again"));

    return server.createConnection(settings.getAuthenticator(server.getAuthDriverName()));
  }

  void execute(Project project, ConsoleView console, String q) {
    if (!running.compareAndSet(false, true)) {
      showInformationNotification(project, "Server is already running a query");
      return;
    }

    runInBackground(() -> {
      try {
        doExecute(console, q);
      } finally {
        running.set(false);
      }
    });
  }

  private void doExecute(ConsoleView console, String q) {
    try {
      c conn = getConnection();
      try {
        console.print("q) " + q + "\n", USER_INPUT);
        Object r = conn.k("{.Q.s value x}", q.toCharArray());
        console.print(toString(r) + "\n", NORMAL_OUTPUT);
      } catch (c.KException e) {
        console.print("'" + e.getMessage() + "\n", ERROR_OUTPUT);
      } finally {
        closeConnection(conn);
      }
    } catch (c.KException e) {
      console.print("'" + e.getMessage() + "\n", ERROR_OUTPUT);
    } catch (Exception e) {
      console.print(e.getMessage() + "\n", ERROR_OUTPUT);
    } finally {
      if (console instanceof ConsoleViewImpl) {
        ((ConsoleViewImpl)console).requestScrollingToEnd();
      }
    }
  }

  /*
   * Convert result to String. Under normal circumstances result will
   * be char[] as the request that gets sent to the server is constructed
   * to always return String.
   * If someone installs a custom .z.pg handler that does not return
   * the value of expression sent, but chooses to return something else (a fairly
   * obscure use case imo) - try to handle it gracefully.
   */
  private static String toString(Object o) {
    if (o instanceof char[]) {
      return new String((char[])o);
    }

    if (o instanceof Object[]) {
      return "[" + Arrays.stream(((Object[])o)).map(KServerProcessHandler::toString).collect(Collectors.joining(",")) +
          "]";
    }

    if (o instanceof int[]) {
      return Arrays.toString((int[])o);
    }

    if (o instanceof byte[]) {
      return Arrays.toString((byte[])o);
    }

    if (o instanceof long[]) {
      return Arrays.toString((long[])o);
    }

    if (o instanceof float[]) {
      return Arrays.toString((float[])o);
    }

    if (o instanceof short[]) {
      return Arrays.toString((short[])o);
    }

    if (o instanceof double[]) {
      return Arrays.toString((double[])o);
    }

    if (o instanceof boolean[]) {
      return Arrays.toString((boolean[])o);
    }

    return Objects.toString(o);
  }
}
