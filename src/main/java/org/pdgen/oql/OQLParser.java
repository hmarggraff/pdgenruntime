// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.CastAccess;
import org.pdgen.data.view.ClassView;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaUserException;
import org.pdgen.env.Res;
import org.pdgen.oql.Lexer.Token;
import org.pdgen.util.StringUtils;

import java.util.*;

public class OQLParser {
    // fields
    Lexer lex;
    JoriaQuery root;
    Hashtable<String, SymbolEntry> itVars;// the iterator symbols usd in subqueries
    Hashtable<String, Exec> functions = new Hashtable<String, Exec>();
    JoriaType scope;
    String query;
    /**
     * some expressions can not be evaluated completely during data access phase, but must run when page is built
     * because they use page number, running total etc.
     * but at the time of actual page building the data fields are not available any more. Therefore the are prepared,
     * and field values are stored in the run env.
     * When the expression is evaluated in the context these values are retrieved and used.
     */
    boolean deferredContext;//
    boolean needsAllPages;
    protected ObjectRef thisRef;
    private boolean emptyIsTrue;

    private OQLParser(String queryText, JoriaType t) {
        query = queryText;
        scope = calcScope(t);
    }

    public JoriaQuery parseUnparented() throws OQLParseException {
        root = lookInCache(query, scope);
        if (root != null)
            return root;
        if (StringUtils.isEmpty(query)) {
            root = new JoriaQuery(getScopeClass(), new NilNode(), null);
            return root;
        }
        if (lex == null) {
            lex = new Lexer(query);
            itVars = new Hashtable<String, SymbolEntry>();
            for (Exec x : BuiltIns.builtinList) {
                functions.put(x.getName(), x);
            }
        }
        if (scope != null) {
            thisRef = new ObjectRef("this", scope);//trdone
        }
        NodeInterface topNode = parseQuestion(next());
        Token nTok = next();
        if (nTok != Token.EOF)
            throw new OQLParseException(Res.str("Unexpected_tokens_after_end"), this);
        if (deferredContext) {
            root = new JoriaDeferedQuery(getScopeClass(), topNode, query, needsAllPages);
        } else
            root = new JoriaQuery(getScopeClass(), topNode, query);
        putInCache(query, scope, root);
        return root;
    }

    public void collectI18nKeys(String s, HashMap<String, List<I18nKeyHolder>> bag) throws JoriaUserException {
        try {
            parseUnparented();
        } catch (OQLParseException e) {
            throw new JoriaUserException("Error_when_finding_localisation_keys_Please_use_the_editor_to_change_it: " + s);
        }
        root.i18nKeys(bag);
    }

    public static Set<RuntimeParameter> checkForVars(String query, JoriaType scope) throws OQLParseException {
        return parse(query, scope).getVariables();
    }

    public static Set<JoriaAccess> checkForUsedAccessors(String query, JoriaType scope) throws OQLParseException {
        JoriaQuery root = lookInCache(query, scope);
        Set<JoriaAccess> ret = new HashSet<JoriaAccess>();
        if (root == null) {
            root = parse(query, scope);
        }
        root.getUsedAccessors(ret);
        return ret;
    }

    public static void checkForI18nKeys(String expression, JoriaType scope, HashMap<String, List<I18nKeyHolder>> collect) throws OQLParseException {
        parse(expression, scope).i18nKeys(collect);
    }

    public static JoriaQuery parse(String queryText, JoriaType rawScope) throws OQLParseException {
        return parse(queryText, rawScope, false);
    }

    public static JoriaQuery parse(String queryText, JoriaType scope, boolean emptyIsTrue) throws OQLParseException {
        JoriaQuery query = lookInCache(queryText, scope);
        if (query != null)
            return query;
        if (StringUtils.isEmpty(queryText)) {
            if (emptyIsTrue)
                query = new JoriaQuery(calcScope(scope), new TrueNode(), queryText);
            else
                query = new JoriaQuery(calcScope(scope), new NilNode(), queryText);
            return query;
        }
        OQLParser parser = new OQLParser(queryText, scope);
        parser.setEmptyIsTrue(emptyIsTrue);
        return parser.parseUnparented();
    }


    protected static JoriaClass calcScope(JoriaType t) {
        if (t.isCollection())
            return ((JoriaCollection) t).getElementType();
        else
            return (JoriaClass) t;// if this fails we must correct the caller
    }

    public static OQLParseException check(String expression, JoriaType scope, boolean forBool) {
        OQLParser parser = new OQLParser(expression, scope);
        try {
            OQLNode jq = parser.parseUnparented();
            if (forBool && !jq.isBoolean())
                throw new OQLParseException(Res.str("Filter_expression_must_be_boolean"), expression, 0);
        } catch (OQLParseException ex) {
            return ex;// return error for processing
        }
        return null;// parse succesful
    }

    JoriaAccess findMember(JoriaType t, String member) throws OQLParseException {
        if (t.isCollection())
            t = ((JoriaCollection) t).getElementType();
        if (!(t instanceof JoriaClass))
            throw new OQLParseException(Res.msg("Left_operand_of_must_be_an_object_Not_a", t.getName()), this);
        JoriaAccess ret = null;
        if (t instanceof ClassView) {
            ClassView v = (ClassView) t;
            ret = v.findMemberIncludingSuperclass(member);
            JoriaClass b = v.getBase();
            while (ret == null && b instanceof ClassView) {
                v = (ClassView) b;
                ret = v.findMemberIncludingSuperclass(member);
                b = v.getBase();
            }
            if (ret == null) {
                ret = b.findMemberIncludingSuperclass(member);
            }
        } else {
            JoriaClass jc = (JoriaClass) t;
            ret = jc.findMemberIncludingSuperclass(member);
        }
        if (ret != null) {
            return ret;
        } else {
            throw new OQLParseException(Res.msg("Member_not_found_in", member, t.getName()), this);
        }
    }

    JoriaType findType(String className) {
        Repository repo = Env.instance().repo();
        final JoriaSchema schema = Env.schemaInstance;
        JoriaType joriaType = schema.findClass(className);
        if (joriaType != null)
            return joriaType;
        joriaType = repo.classProjections.find(className);
        if (joriaType != null)
            return joriaType;
        final ArrayList<CastAccess> userRoots = repo.userRoots.getData();
        for (CastAccess userRoot : userRoots) {
            if (className.equals(userRoot.getType().getName()))
                return userRoot.getType();
        }

        return null;
    }

    public int getPos() {
        return lex.getPos();
    }

    public String getQueryString() {
        return query;
    }

    protected Token next() throws OQLParseException {
        return lex.nextToken();
    }

    NodeInterface parseAnd(Token atToken) throws OQLParseException {
        if (atToken == Token.Exists || atToken == Token.ForAll || atToken == Token.All) {
            Token t1 = next();
            if (atToken == Token.ForAll) {
                if (t1 != Token.All)
                    throw new OQLParseException(Res.str("for_must_be_followed_by_all"), this);
                else
                    t1 = next();
            }
            if (t1 != Token.Name)
                throw new OQLParseException(Res.str("for_all_exists_all_all_must_be_followed_by_a_name"), this);
            String refName = lex.nameVal;
            Token t3 = next();
            if (t3 != Token.In)
                throw new OQLParseException(Res.str("in_expected_after_iterator_variable"), this);
            NodeInterface coll = parsePlus(next());
            if (!coll.isCollection())
                throw new OQLParseException(Res.str("type_of_iterator_variable_must_be_a_collection_expression"), this);
            Token t4 = next();
            if (t4 != Token.Colon)
                throw new OQLParseException(Res.str("colon_expected_after_iterator_definition"), this);
            JoriaTypedNode ctn = (JoriaTypedNode) coll;
            JoriaCollection tColl = (JoriaCollection) ctn.getType();
            IteratorRef it = new IteratorRef(refName, tColl);
            NodeInterface query = parseInner(it);
            if (!query.isBoolean())
                throw new OQLParseException(Res.str("Condition_part_of_forall_exists_all_must_be_a_boolean_expression"), this);
            if (atToken == Token.ForAll)
                return new ForallNode(coll, query, it);
            else if (atToken == Token.All)
                return new FilterNode(ctn, query, it);
            else
                return new ExistsNode(coll, query, it);
        } else {
            NodeInterface l = parseEquals(atToken);
            Token t = next();
            if (t == Token.And) {
                NodeInterface r = parseAnd(next());
                if (!(l.isBoolean() && r.isBoolean()))
                    throw new OQLParseException(Res.str("Operands_to_or_must_be_boolean"), this);
                return new AndNode(NodeInterface.booleanType, l, r);
            } else
                return pushBack(l);
        }
    }

    NodeInterface parseAtom(Token atToken) throws OQLParseException {
        if (atToken == Token.LBrace)
            return parseCast(next());
        else if (atToken == Token.Name)
            return parseNav(atToken, null);
        else if (atToken == Token.Int)
            return new IntNode(lex.intVal);
        else if (atToken == Token.Float)
            return new FloatNode(lex.floatVal);
        else if (atToken == Token.String)
            return new StringNode(lex.stringVal);
        else if (atToken == Token.Char)
            return new CharNode(lex.charVal);
        else if (atToken == Token.True)
            return new TrueNode();
        else if (atToken == Token.False)
            return new FalseNode();
        else if (atToken == Token.Nil)
            return new NilNode();
        else if (atToken == Token.EOFComment) {
            if (emptyIsTrue)
                return new TrueNode();
            else
                return new NilNode();
        } else if (atToken == Token.EOF)
            throw new OQLParseException(Res.str("Unexpected_EOF_The_formula_appears_to_be_incomplete"), this);
        throw new OQLParseException(Res.str("Unexpected_token_The_character_at_this_position_cannot_be_understood"), this);
    }

    NodeInterface parseCast(Token atToken) throws OQLParseException {
        NodeInterface l = parseType(atToken);
        Token t = next();
        if (t != Token.RBrace)
            throw new OQLParseException(Res.str("Expected"), this);
        if (l instanceof ClassNameNode) {
            NodeInterface r = parseQuestion(next());
            if (!r.isObject())
                throw new OQLParseException(Res.str("Can_only_cast_objects"), this);
            return new ExpressionCastNode((ClassNameNode) l, r);
        } else {
            atToken = next();
            if (atToken != Token.Dot)
                return pushBack(l);
            if (l instanceof JoriaTypedNode) {
                return parseNav(next(), (JoriaTypedNode) l);
            } else
                throw new OQLParseException(Res.msg("Operand_at_left_must_denote_an_object_The_left_operand_is_a", l.getTypeName()), this);
        }
    }

    NodeInterface parseType(Token t) throws OQLParseException {
        if (t == Token.Name) {
            final String className = lex.nameVal;
            final JoriaType joriaType = findType(className);
            if (joriaType != null)
                return new ClassNameNode(joriaType);
        }
        return parseQuestion(t);
    }

    NodeInterface parseEquals(Token atToken) throws OQLParseException {
        NodeInterface l = parseLess(atToken);
        Token t = next();
        if (t == Token.Equal || t == Token.Unequal) {
            NodeInterface r = parseLess(next());
            int commonType = commonTypeKey(l, r);
            if (t == Token.Unequal)
                return new UnequalsNode(commonType, l, r);
            else
                return new EqualsNode(commonType, l, r);
        } else if (t == Token.Like) {
            Token t2 = next();
            if (t2 != Token.String)
                throw new OQLParseException(Res.str("Pattern_of_like_expression_must_be_a_literal_string"), this);
            if (!l.isString())
                throw new OQLParseException(Res.str("Left_operand_of_like_must_be_a_string_expression"), this);
            return new LikeNode(l, lex.stringVal);
        } else
            return pushBack(l);
    }

    protected int commonTypeKey(NodeInterface l, NodeInterface r) throws OQLParseException {
        int commonType = -1;
        if (l instanceof NilNode) {
            if (r.isString())
                commonType = NodeInterface.stringType;
            else if (r.isObject())
                commonType = NodeInterface.objectType;
            else if (r.isInteger())
                commonType = NodeInterface.intType;
            else if (r.isReal())
                commonType = NodeInterface.realType;
            else if (r.isDate())
                commonType = NodeInterface.dateType;
            else if (r.isCollection())
                commonType = NodeInterface.collectionType;
            else if (r.isLiteralCollection())
                commonType = NodeInterface.literalCollectionType;
            else if (r.isDictionary())
                commonType = NodeInterface.dictionaryType;
        } else if (r instanceof NilNode) {
            if (l.isString())
                commonType = NodeInterface.stringType;
            else if (l.isObject())
                commonType = NodeInterface.objectType;
            else if (l.isInteger())
                commonType = NodeInterface.intType;
            else if (l.isReal())
                commonType = NodeInterface.realType;
            else if (l.isDate())
                commonType = NodeInterface.dateType;
            else if (l.isCollection())
                commonType = NodeInterface.collectionType;
            else if (l.isLiteralCollection())
                commonType = NodeInterface.literalCollectionType;
            else if (l.isDictionary())
                commonType = NodeInterface.dictionaryType;
        } else if (l.isBoolean() && r.isBoolean())
            commonType = NodeInterface.booleanType;
        else if (l.isString() && (r.isString()))
            commonType = NodeInterface.stringType;
        else if (l.isInteger()) {
            if (r.isInteger())
                commonType = NodeInterface.intType;
            else if (r.isReal())
                commonType = NodeInterface.realType;
        } else if (l.isReal() && (r.isReal() || r.isInteger()))
            commonType = NodeInterface.realType;
        else if (l.isDate() && (r.isDate()))
            commonType = NodeInterface.dateType;
        else if (l.isObject() && r.isObject())
            commonType = NodeInterface.objectType;
        else if (l.isCollection() && (r.isCollection()))
            commonType = NodeInterface.collectionType;
        else if (l.isDictionary() && (r.isDictionary()))
            commonType = NodeInterface.dictionaryType;
        else if (l.isLiteralCollection() && (r.isLiteralCollection()))
            commonType = NodeInterface.literalCollectionType;
        if (commonType == -1)
            throw new OQLParseException(Res.str("Operand_types_of_do_not_match"), this);
        return commonType;
    }

    NodeInterface parseIn(Token atToken) throws OQLParseException {
        NodeInterface l = parseNot(atToken);
        Token t = next();
        if (t == Token.In) {
            NodeInterface r = parseNot(next());
            if (r.isCollection() || r.isLiteralCollection())
                return new InNode(l, r);
            else
                throw new OQLParseException(Res.str("In_operator_must_have_a_collection_as_right_operand"), this);
        } else if (t == Token.Instanceof) {
            String c = parseClassName();
            JoriaType ct = Env.schemaInstance.findClass(c);
            if (ct == null)
                throw new OQLParseException(Res.msg("Type_not_found", c), this);
            if (ct.isCollection())
                ct = ((JoriaCollection) ct).getElementType();
            if (!ct.isClass())
                throw new OQLParseException(Res.str("instanceof_can_only_be_applied_to_class_types_and_collections_of_class_typed_elements"), this);
            return new InstanceofNode(l, (JoriaClass) ct);
        } else
            return pushBack(l);
    }

    String parseClassName() throws OQLParseException {
        Token t;
        StringBuffer nameBuf = new StringBuffer();
        do {
            t = next();
            if (t != Token.Name)
                throw new OQLParseException(Res.str("Class_package_name_expected"), this);
            nameBuf.append(lex.nameVal);
            t = next();
            if (t == Token.Dot)
                nameBuf.append('.');
        }
        while (t == Token.Dot);
        lex.pushBack();
        return nameBuf.toString();
    }

    NodeInterface parseInner(IteratorRef it) throws OQLParseException {
        SymbolEntry old = itVars.get(it.getName());
        itVars.put(it.getName(), it);
        //JoriaType outerScope = scope;
        //scope = it.getCollectionType().getElementType();
        NodeInterface inner = parseQuestion(next());
        itVars.remove(it.getName());
        if (old != null)
            itVars.put(old.getName(), old);
        //scope = outerScope;
        return inner;
    }

    NodeInterface parseLess(Token atToken) throws OQLParseException {
        NodeInterface l = parsePlus(atToken);
        Token t = next();
        if (t != Token.Less && t != Token.LessOrEqual && t != Token.Greater && t != Token.GreaterOrEqual)
            return pushBack(l);
        Token tc = next();
        if (tc == Token.Any || tc == Token.Some || tc == Token.All) {
            throw new OQLParseException(Res.str("Any_Some_All_not_supported_in_comparison"), this);
        }
        NodeInterface r = parsePlus(tc);
        int commonType = -1;
        if (l.isString() && r.isString())
            commonType = NodeInterface.stringType;
        else if (l.isInteger()) {
            if (r.isInteger())
                commonType = NodeInterface.intType;
            else if (r.isReal())
                commonType = NodeInterface.realType;
        } else if (l.isReal() && r.isReal())
            commonType = NodeInterface.realType;
        else if (l.isDate() && r.isDate())
            commonType = NodeInterface.dateType;
        if (commonType == -1)
            throw new OQLParseException(Res.str("Operand_types_of_do_not_match"), this);
        if (t == Token.Less)
            return new LessNode(commonType, l, r);
        else if (t == Token.LessOrEqual)
            return new LessOrEqualNode(commonType, l, r);
        else if (t == Token.Greater)
            return new GreaterNode(commonType, l, r);
        else
            /*if (t == Token.GreaterOrEqual)*/
            return new GreaterOrEqualNode(commonType, l, r);
    }

    NodeInterface parseMultiply(Token atToken) throws OQLParseException {
        NodeInterface l = parseIn(atToken);
        for (; ; ) {
            Token t = next();
            if (t == Token.Intersect)
                throw new OQLParseException(Res.str("Intersection_operator_not_supported"), this);
            if (t != Token.Mod && t != Token.Star && t != Token.Slash)
                return pushBack(l);
            NodeInterface r = parseIn(next());
            if (t == Token.Mod) {
                if (l.isInteger() && r.isInteger())
                    l = new ModuloNode(NodeInterface.intType, l, r);
                else
                    throw new OQLParseException(Res.str("Operands_of_mod_must_be_int"), this);
            } else {
                int type;
                if (l.isInteger() && r.isInteger())
                    type = NodeInterface.intType;
                else if ((l.isReal() || l.isInteger()) && (r.isReal() || r.isInteger()))
                    type = NodeInterface.realType;
                else
                    throw new OQLParseException(Res.str("Operands_of_must_be_numbers"), this);
                if (t == Token.Star)
                    l = new MultiplyNode(type, l, r);
                else
                    l = new DivideNode(type, l, r);
            }
        }
    }

    JoriaTypedNode parseNav(Token startTok, JoriaTypedNode left) throws OQLParseException {
        Token tok = startTok;
        if (tok != Token.Name)
            throw new JoriaAssertionError("parseNav must start with a name token");
        JoriaType currentScope;
        if (left == null)
            currentScope = scope;
        else
            currentScope = left.getType();
        //Token tOp = next();
        do {
            String symName = lex.nameVal;
            tok = next();
            if (tok == Token.LBrace)// argList
            {
                ArrayList<NodeInterface> argList = new ArrayList<NodeInterface>();
                Token t1 = next();
                while (t1 != Token.RBrace) {
                    NodeInterface arg = parseQuestion(t1);
                    argList.add(arg);
                    t1 = next();
                    if (t1 == Token.RBrace)
                        break;
                    else if (t1 == Token.Comma)
                        t1 = next();
                    else
                        throw new OQLParseException(Res.str("Expected_or"), this);
                }
                if (left == null)// level 0
                {
                    Exec ex = functions.get(symName);
                    if (ex == null)
                        throw new OQLParseException(Res.msg("Function_not_found", symName), this);
                    final NodeInterface[] argNodes;
                    if (argList.size() > 0) {
                        argNodes = new NodeInterface[argList.size()];
                        argList.toArray(argNodes);
                    } else
                        argNodes = new NodeInterface[0];
                    if (ex != BuiltIns.biPrev)// biPrev does not change the scope
                        try {
                            currentScope = ex.parse(argNodes);
                        } catch (OQLParseException oqlParseExcpetion) {
                            throw new OQLParseException(oqlParseExcpetion.getMessage(), this);
                        }
                    if (currentScope == null)
                        throw new OQLParseException(Res.msg("Function_can_not_be_called_with_these_argument_types", symName), this);
                    final BuiltinNode bNode = new BuiltinNode(argNodes, ex, getScopeClass());
                    deferredContext |= bNode.isPageRelative();
                    needsAllPages |= bNode.isNeedsAllPages();
                    left = bNode;
                    currentScope = bNode.getType();
                } else
                    //todo support calling methods from OQL
                    throw new OQLParseException(Res.str("Methods_not_yet_not_supported"), this);
                tok = next();
            } else if (left == null) {
                left = parseSymbol(symName);
                if (left != null)
                    currentScope = left.getType();
            } else {
                if (!(left.isObject() || left.isDate()))
                    throw new OQLParseException(Res.msg("Name_to_left_of_dot_must_denote_an_object_not", left.getTypeName()), this);
                FieldNode fn = new FieldNode(findMember(currentScope, symName));
                left = new DotNode(getNodeType(fn), left, fn);
                currentScope = fn.getType();
            }
            //after the symbol next operator
            if (tok == Token.LBracket) {
                if (!(left.isCollection() || left.isDictionary()) || left.isLiteralCollection())
                    throw new OQLParseException(Res.str("Left_operand_of_index_operator_must_be_a_collection_or_dictionary"), this);
                NodeInterface index0 = parseQuestion(next());
                tok = next();
                if (tok == Token.RBracket) {
                    if ((left.isCollection() || left.isLiteralCollection()) && !(index0.isInteger()))
                        throw new OQLParseException(Res.str("Index_of_a_collection_must_be_an_integer"), this);
                    else if (left.isDictionary() && !index0.isString())
                        throw new OQLParseException(Res.str("Index_of_a_dictionary_can_only_be_a_String"), this);
                    JoriaType et = left.getElementType();
                    if (!et.isClass())
                        throw new OQLParseException(Res.str("Element_type_of_a_colection_must_be_a_class_type"), this);
                    left = new IndexOpNode(NodeInterface.objectType, left, index0);
                    currentScope = et;
                } else if (tok == Token.Elipses || tok == Token.Colon) {
                    if ((left.isCollection() || left.isLiteralCollection()) && !(index0.isInteger()))
                        throw new OQLParseException(Res.str("Index_of_a_collection_slice_must_be_an_integer"), this);
                    tok = next();
                    if (tok == Token.Int) {
                        NodeInterface index1 = parseQuestion(tok);
                        if (!(index1.isInteger()))
                            throw new OQLParseException(Res.str("Index_of_a_collection_slice_must_be_an_integer"), this);
                        left = new CollectionSliceNode(left, index0, index1);
                        tok = next();
                    } else if (tok == Token.RBracket) {
                        left = new CollectionSliceNode(left, index0, new IntNode(Integer.MAX_VALUE));
                    }
                }
                if (tok != Token.RBracket)
                    throw new OQLParseException(Res.str("Missing_right_bracket_after_index_expression"), this);
                tok = next();
            }
            if (tok == Token.Dot) {
                if (!(left.isObject() || left.isDate()))
                    throw new OQLParseException(Res.msg("Name_to_left_of_dot_must_denote_an_object_not", left.getTypeName()), this);
                tok = next();
            }
        }
        while (tok == Token.Name);
        lex.pushBack();
        return left;
    }

    NodeInterface parseSymbolOrType(String symName) throws OQLParseException {
        JoriaType t = findType(symName);
        if (t != null)// type cast
        {
            return new ClassNameNode(t);
        }
        return parseSymbol(symName);
    }

    private JoriaTypedNode parseSymbol(String symName) throws OQLParseException {
        if (symName.charAt(0) == '$') {
            // if we are parsing in the loading phase, because there were schema changes, then we must use the loding instance of the repository
            SortedNamedVector<RuntimeParameter> vars = Env.instance().repo().variables;
            String varName = symName.substring(1);
            if (varName == null || varName.length() == 0)
                throw new OQLParseException(Res.str("Parameters_must_have_a_name"), this);
            RuntimeParameter v = vars.find(varName);
            if (v == null) {
                throw new OQLParseException(Res.msg("UnknownParameter_0", symName), this);
            }
            return new VarNode(v);
        } else if ("this".equals(symName))//trdone
        {
            return new ThisNode(scope);
        } else {
            final IteratorRef it = (IteratorRef) itVars.get(symName);
            if (it != null) {
                return new ThisNode(it.getCollectionType().getElementType());
            } else if ("access_to_base_of_view".equals(symName))//trdone
            {
                if (!(scope.isView())) {
                    throw new OQLParseException(Res.msg("AccessToBase_only_view_0", scope.getName()), this);
                }
                return new ThisNode(scope.getAsParent());
            }
            return new FieldNode(findMember(scope, symName));
        }
    }

    NodeInterface parseNot(Token atToken) throws OQLParseException {
        if (atToken != Token.Not && atToken != Token.Minus && atToken != Token.Plus)
            return parseAtom(atToken);
        NodeInterface l = parseAtom(next());
        if (atToken == Token.Not) {
            if (l.isBoolean())
                return new NotNode(l);
            else
                throw new OQLParseException(Res.str("Operand_of_not_must_be_boolean"), this);
        } else {
            if (!l.isReal())
                throw new OQLParseException(Res.str("Operand_of_unary_must_be_a_number"), this);
            if (atToken == Token.Minus)
                return new UnaryMinusNode(l);
            else
                return l;
        }
    }

    NodeInterface parseQuestion(Token nextToken) throws OQLParseException {
        NodeInterface cond = parseOr(nextToken);
        Token t = next();
        if (t != Token.Question)
            return pushBack(cond);
        if (!cond.isBoolean())
            throw new OQLParseException(Res.str("Condition_of_question_must_be_a_boolean"), this);
        NodeInterface l = parseOr(next());
        t = next();
        if (t != Token.Colon)
            throw new OQLParseException(Res.str("Colon_expected_after_question"), this);
        NodeInterface r = parseOr(next());
        int commonType = commonTypeKey(l, r);
        if (commonType == NodeInterface.objectType || commonType == NodeInterface.collectionType)
            throw new OQLParseException(Res.str("Result_type_of_question_must_be_a_literal"), this);
        return new QuestionNode(commonType, cond, l, r);
    }

    protected NodeInterface parseOr(Token atToken) throws OQLParseException {
        NodeInterface l = parseAnd(atToken);
        Token t = next();
        if (t != Token.Or)
            return pushBack(l);
        NodeInterface r = parseOr(next());
        if (!(l.isBoolean() && r.isBoolean()))
            throw new OQLParseException(Res.str("Operands_to_or_must_be_boolean"), this);
        return new OrNode(NodeInterface.booleanType, l, r);
    }

    NodeInterface parsePlus(Token atToken) throws OQLParseException {
        NodeInterface l = parseMultiply(atToken);
        for (; ; ) {
            Token t = next();
            if (t == Token.Union || t == Token.Except)
                throw new OQLParseException(Res.str("Union_Except_operators_not_supported"), this);
            if (t != Token.Plus && t != Token.Minus)
                return pushBack(l);
            NodeInterface r = parseMultiply(next());
            int type;
            if (l.isInteger() && r.isInteger())
                type = NodeInterface.intType;
            else if ((l.isReal() || l.isInteger()) && (r.isReal() || r.isInteger()))
                type = NodeInterface.realType;
            else if (l.isString() && r.isString())
                type = NodeInterface.stringType;
            else if (l.isCollection() && r.isCollection() && t == Token.Plus) {
                type = NodeInterface.collectionType;
                if (l instanceof JoriaTypedNode && r instanceof JoriaTypedNode) {
                    JoriaTypedNode lt = (JoriaTypedNode) l;
                    JoriaTypedNode rt = (JoriaTypedNode) r;
                    final JoriaType let = lt.getElementType();

                    final JoriaType ret = rt.getElementType();
                    if (let != ret) {
                        final JoriaClass lc = (JoriaClass) lt.getElementType();
                        Set<JoriaClass> leftBaseClasses = new HashSet<JoriaClass>();
                        AbstractJoriaClass.buildBaseClasses(leftBaseClasses, lc);
                        JoriaClass rc = (JoriaClass) ret;
                        final JoriaClass baseClass = AbstractJoriaClass.findBestBaseClass(leftBaseClasses, rc);
                        if (baseClass == null)
                            throw new OQLParseException(Res.str("Element_types_type_of_added_collections_must_match"), this);
                        l = new ConcatCollNode(type, l, r, baseClass);
                        continue;
                    }
                } else
                    throw new OQLParseException(Res.str("Parser_limitation_Cannot_determine_type_of_added_collections"), this);
            } else if (l.isLiteralCollection() && r.isLiteralCollection())
                type = NodeInterface.literalCollectionType;
            else if (t == Token.Plus)
                throw new OQLParseException(Res.str("Operands_of_must_be_numbers_strings_collections"), this);
            else
                throw new OQLParseException(Res.str("Operands_of_must_be_numbers"), this);
            if (t == Token.Plus)
                l = new PlusNode(type, l, r);
            else if (type == NodeInterface.intType || type == NodeInterface.realType)
                l = new SubtractNode(type, l, r);
            else
                throw new OQLParseException(Res.str("Operands_of_must_be_numbers"), this);
        }
    }

    protected NodeInterface pushBack(NodeInterface l) {
        lex.pushBack();
        return l;
    }

    protected JoriaClass getScopeClass() {
        if (scope.isClass())
            return (JoriaClass) scope;
        else
            return null;
    }

    protected int getNodeType(NodeInterface l) {
        int type;
        if (l.isInteger())
            type = NodeInterface.intType;
        else if (l.isReal())
            type = NodeInterface.realType;
        else if (l.isString())
            type = NodeInterface.stringType;
        else if (l.isDictionary())
            type = NodeInterface.dictionaryType;
        else if (l.isBoolean())
            type = NodeInterface.booleanType;
        else if (l.isCharacter())
            type = NodeInterface.charType;
        else if (l.isCollection())
            type = NodeInterface.collectionType;
        else if (l.isLiteralCollection())
            type = NodeInterface.literalCollectionType;
        else if (l.isDate())
            type = NodeInterface.dateType;
        else if (l.isObject())
            type = NodeInterface.objectType;
        else if (l.isBlob())
            type = NodeInterface.blobType;
        else
            throw new JoriaAssertionError("Uncovered type");
        return type;
    }

    public static class QueryKey {
        public boolean equals(Object obj) {
            if (!(obj instanceof QueryKey))
                return false;
            QueryKey qk = (QueryKey) obj;
            return qk.t == qk.t && qk.p0.equals(qk.p0);
        }

        public int hashCode() {
            return t.hashCode() ^ p0.hashCode();
        }

        private final String p0;
        private final JoriaType t;

        QueryKey(String p0, JoriaType t) {
            this.p0 = p0;
            this.t = t;
        }
    }

    public static JoriaQuery lookInCache(String p0, JoriaType t) {
        if (t == null || p0 == null)
            return null;
        HashMap<QueryKey, JoriaQuery> cache = Env.instance().theQueryCache;
        return cache.get(new QueryKey(p0, t));
    }

    public static void clearFromCache(String p0, JoriaType t) {
        if (t == null || p0 == null)
            return;
        HashMap<QueryKey, JoriaQuery> cache = Env.instance().theQueryCache;
        cache.remove(new QueryKey(p0, t));
    }

    void putInCache(String p0, JoriaType t, JoriaQuery q) {
        if (q != null) {
            HashMap<QueryKey, JoriaQuery> cache = Env.instance().theQueryCache;
            cache.put(new QueryKey(p0, t), q);
        }
    }

    public static void clearCache() {
        HashMap<QueryKey, JoriaQuery> cache = Env.instance().theQueryCache;
        cache.clear();
    }

    public static void getUsedAccessors(Set<JoriaAccess> ret, String expression, JoriaType type) throws OQLParseException {
        if (StringUtils.isEmpty(expression))
            return;
        final JoriaQuery joriaQuery = parse(expression, type);
        joriaQuery.getUsedAccessors(ret);
    }

    public void setEmptyIsTrue(boolean emptyIsTrue) {
        this.emptyIsTrue = emptyIsTrue;
    }
}
