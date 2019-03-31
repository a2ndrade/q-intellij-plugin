package com.appian.intellij.k.settings;

import org.junit.Assert;
import org.junit.Test;

public class KSettingsTest {
  @Test
  public void testDefaultSettings() {
    Assert.assertTrue(new KSettings().isInternalName(".i.var"));
  }

  @Test
  public void testSettings() {
    KSettings settings = new KSettings();
    settings.setInternalPrefixes(".priv; .i.; foo");
    Assert.assertTrue(settings.isInternalName(".i.var"));
    Assert.assertTrue(settings.isInternalName(".priv.var"));
    Assert.assertTrue(settings.isInternalName("fooBar"));
    Assert.assertTrue(settings.isInternalName("foo"));
    Assert.assertFalse(settings.isInternalName("barfoo"));
  }
}
