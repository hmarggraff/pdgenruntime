// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.util.HashSet;

public interface Exec extends SymbolEntry {

    JoriaType getType(NodeInterface[] a);

    JoriaType parse(NodeInterface[] a) throws OQLParseException;

    boolean isPageRelative();

    boolean isNeedsAllPages();

    void i18nKeys(HashSet<String> collect);

    String getGroup();

    String getDescription();

    interface ExecChar extends Exec {

        char execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecString extends Exec {

        String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecReal extends Exec {

        double execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecInteger extends Exec {

        long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecBoolean extends Exec {

        boolean execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecDBData extends Exec {

        DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException;
    }

    interface ExecDate extends ExecDBData {

    }

    abstract class ExecIntegerAbstract extends WithName implements Exec.ExecInteger {

        protected ExecIntegerAbstract(String name, String group, String description) {
            super(name, group, description);
        }

        public JoriaType getDefaultType() {
            return DefaultIntLiteral.instance();
        }

        public JoriaType parse(NodeInterface[] a) {
            return getType(a);
        }
    }

    abstract class ExecRealAbstract extends WithName implements Exec.ExecReal {

        protected ExecRealAbstract(String name, String group, String description) {
            super(name, group, description);
        }

        public JoriaType getDefaultType() {
            return DefaultRealLiteral.instance();
        }

        public JoriaType parse(NodeInterface[] a) {
            return getType(a);
        }
    }

    abstract class ExecStringAbstract extends WithName implements Exec.ExecString {

        protected ExecStringAbstract(String name, String group, String description) {
            super(name, group, description);
        }

        public JoriaType getDefaultType() {
            return DefaultStringLiteral.instance();
        }

        public JoriaType parse(NodeInterface[] a) throws OQLParseException {
            return getType(a);
        }
    }

    abstract class ExecDBDataAbstract extends WithName implements Exec.ExecDBData {

        protected ExecDBDataAbstract(String name, String group, String description) {
            super(name, group, description);
        }

        public JoriaType parse(NodeInterface[] a) {
            return getType(a);
        }
    }

    abstract class ExecDateAbstract extends ExecDBDataAbstract implements Exec.ExecDate {

        protected ExecDateAbstract(String name, String group, String description) {
            super(name, group, description);
        }

    }

    abstract class ExecBooleanAbstract extends WithName implements Exec.ExecBoolean {

        protected ExecBooleanAbstract(String name, String group, String description) {
            super(name, group, description);
        }

        public JoriaType parse(NodeInterface[] a) {
            return getType(a);
        }

    }

    abstract class ExecBoolean4String extends ExecBooleanAbstract {

        public ExecBoolean4String(String name, String group, String description) {
            super(name, group, description);
        }

        public boolean execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            //noinspection SimplifiableIfStatement
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return false;
            return compare(((DBString) args[0]).getStringValue(), ((DBString) args[1]).getStringValue());
        }

        protected abstract boolean compare(String s1, String s2);

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 && a[0].isString() && a[1].isString())
                return DefaultBooleanLiteral.instance();
            return null;
        }
    }

    class WithName {

        String name;
        String group;
        String description;

        protected WithName(String name, String group, String description) {
            this.description = description;
            this.group = group;
            this.name = name;
        }

		/*
		public WithName(String name)
		{
			this.name = name;
		}
		*/

        public String getDescription() {
            return description;
        }

        public String getGroup() {
            return group;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        public boolean isPageRelative() {
            return false;
        }

        public boolean isNeedsAllPages() {
            return false;
        }

        public void i18nKeys(HashSet<String> collect) {
            //nothing to do in base
        }
    }
}
