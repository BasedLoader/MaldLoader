package io.github.mald.env;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface SidedInterface {
	String iface();

	Side side();
}
