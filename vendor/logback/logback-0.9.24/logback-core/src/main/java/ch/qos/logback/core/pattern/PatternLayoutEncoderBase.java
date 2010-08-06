package ch.qos.logback.core.pattern;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class PatternLayoutEncoderBase<E> extends LayoutWrappingEncoder<E> {

  String pattern;

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public void setLayout(Layout<E> layout) {
    throw new UnsupportedOperationException("one cannot set the layout of "
        + this.getClass().getName());
  }

}
