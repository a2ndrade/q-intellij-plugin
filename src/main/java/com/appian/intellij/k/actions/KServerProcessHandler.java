package com.appian.intellij.k.actions;

import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.USER_INPUT;

import java.io.IOException;
import java.io.OutputStream;

import org.jetbrains.annotations.Nullable;

import com.appian.intellij.k.settings.KServerSpec;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;

import kx.c;

public class KServerProcessHandler extends ProcessHandler {
  private final KServerSpec serverSpec;
  private c connection;

  KServerProcessHandler(KServerSpec serverSpec) {
    this.serverSpec = serverSpec;
  }

  KServerSpec getServerSpec() {
    return serverSpec;
  }

  @Override
  protected void destroyProcessImpl() {
    closeConnection();
    notifyProcessTerminated(0);
  }

  @Override
  protected void detachProcessImpl() {
    closeConnection();
    notifyProcessDetached();
  }

  private void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException e) {
        // ignore
      }
      connection = null;
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
    if (connection == null) {
      connection = new c(serverSpec.getHost(), serverSpec.getPort(),
          serverSpec.getUser() + ":" + serverSpec.getPassword(), serverSpec.useTLS());
    }
    return connection;
  }

  void execute(ConsoleView console, String q) {
    try {
      c conn = getConnection();
      try {
        console.print("q) " + q + "\n", USER_INPUT);
        String s = new String((char[])conn.k("{.Q.s value x}", q.toCharArray()));
        console.print(s, NORMAL_OUTPUT);
      } catch (c.KException e) {
        console.print("'" + e.getMessage() + "\n", ERROR_OUTPUT);
      }
    } catch (c.KException e) {
      console.print("'" + e.getMessage() + "\n", ERROR_OUTPUT);
    } catch (Exception e) {
      console.print(e.getMessage() + "\n", ERROR_OUTPUT);
      // close connection to force reconnection on any error other than
      // the error from q instance
      closeConnection();
    }
  }
}
