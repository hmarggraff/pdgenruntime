// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.JoriaType;
import org.pdgen.util.ErrorHint;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public abstract class JoriaIF
{
	protected static Env theInstance;

	public abstract void handle(Throwable t, Window frame);

	public abstract void handle(Throwable t, String msg, Window frame);

	public abstract void handle(Throwable t);

	public abstract void handle(Throwable t, String msg);

	public abstract void handle(Throwable t, ErrorHint[] hints, final String defaultMessage);

	public abstract void collectI18nKeys(String filter, JoriaType scope, boolean commentOnlyAllowed, String message, HashMap<String, List<I18nKeyHolder>> bag);
}
