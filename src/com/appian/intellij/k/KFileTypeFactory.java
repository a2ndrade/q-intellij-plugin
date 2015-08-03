package com.appian.intellij.k;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;

public class KFileTypeFactory extends FileTypeFactory {

  @Override
  public void createFileTypes(FileTypeConsumer consumer) {
    consumer.consume(KFileType.INSTANCE, "k");
  }
}
