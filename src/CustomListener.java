/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import org.antlr.v4.runtime.*;

/**
 *
 * @author kogayou
 */

public class CustomListener {
    
    //UnderlineListener
    public static class UnderlineListener extends BaseErrorListener {
        
        public static int errorNum;
        
        public UnderlineListener() {
            errorNum=0;
        }
        
        @Override public void syntaxError(Recognizer<?,?> recognizer,Object offendingSymbol,int line,int charPositionInLine,String msg,RecognitionException e)
        {
            System.err.println("line "+line+":"+charPositionInLine+" "+msg);
            underlineError(recognizer,(Token)offendingSymbol,line,charPositionInLine);
            errorNum++;
        }
        
        private void underlineError(Recognizer recognizer,Token offendingToken,int line,int charPositionInLine){
            CommonTokenStream tokens=(CommonTokenStream)recognizer.getInputStream();
            String input=tokens.getTokenSource().getInputStream().toString();
            String[] lines=input.split("\n");
            String errorLine="";
            if (line-1<lines.length) errorLine=lines[line-1];
            System.err.println(errorLine);
            for (int i=0;i<charPositionInLine;i++) {
                if (errorLine.charAt(i)=='\t') {
                    System.err.print("\t");
                } else {
                    System.err.print(" ");
                }
            }
            int start=offendingToken.getStartIndex();
            int stop=offendingToken.getStopIndex();
            if (start>=0&&stop>=0 ) {
                for (int i=start;i<=stop;i++)
                    System.err.print("^");
            }
            System.err.println("\n");
        }
    }

    //SemanticListener

    public static class SemanticListener extends TigerBaseListener {
        
        private static class IDInfo {
            String name;
            String type;
            int start;
            int end;
        }
    
        private static class FunInfo {
            String name;
            String field;
            String type;
            int start;
            int end;
        }
    
        private static HashMap<ParserRuleContext,String> expType;
        private static ArrayList<IDInfo> ids;
        private static ArrayList<FunInfo> funs;
        private static CommonTokenStream tokens;
        private static boolean isError;
        public static int errorNum;
        public static int round;
        
    
        public SemanticListener(CommonTokenStream inputTokens) {
            tokens=inputTokens;
            expType=new HashMap();
            ids=new ArrayList();
            funs=new ArrayList();
            errorNum=0;
            addIDType("int","int");
            addIDType("string","string");
            addFunType("print","string","");
            addFunType("flush","","");
            addFunType("getchar","","string");
            addFunType("ord","string","int");
            addFunType("chr","int","string");
            addFunType("size","string","int");
            addFunType("substring","string,int,int","string");
            addFunType("concat","string,string","string");
            addFunType("not","int","int");
            addFunType("exit","int","");
        }
    
        private static void showError(ParserRuleContext prc,String msg) {
            int line=prc.start.getLine();
            int charPositionInLine=prc.start.getCharPositionInLine();
            int len=prc.stop.getStopIndex()-prc.start.getStartIndex()+1;
            showError(line,charPositionInLine,len,msg);
        }
        
        private static void showError(Token token,String msg) {
            int line=token.getLine();
            int charPositionInLine=token.getCharPositionInLine();
            int len=token.getText().length();
            showError(line,charPositionInLine,len,msg);
        }
        
        private static void showError(int line,int charPositionInLine,int len,String msg) {
            if (round==1) {
                return;
            }
            System.err.println("line "+line+":"+charPositionInLine+": "+msg);
            String input=tokens.getTokenSource().getInputStream().toString();
            String[] lines=input.split("\n");
            String errorLine="";
            if (line-1<lines.length) errorLine=lines[line-1];
            System.err.println(errorLine);
            for (int i=0;i<charPositionInLine;i++) {
                if (errorLine.charAt(i)=='\t') {
                    System.err.print("\t");
                } else {
                    System.err.print(" ");
                }
            }
            for (int i=1;i<=len;i++)
                System.err.print("^");
            System.err.println("\n");
            errorNum++;
        }

        private static void addIDType(String name,String type) {
            IDInfo id=new IDInfo();
            id.name=name;
            id.type=type;
            id.start=Integer.MIN_VALUE;
            id.end=Integer.MAX_VALUE;
            ids.add(id);
        }
        
        private static String getIDType(ParserRuleContext prc) {
            return getIDType(prc.start);
        }
    
        private static String getIDType(Token token) {
            String name=token.getText();
            return getIDType(name,token);
        }
        
        private static String getIDType(String name,Token token) {
            int pos=token.getStartIndex();
            boolean find=false;
            IDInfo id=new IDInfo();
            for (int i=0;i<ids.size();i++) {
                IDInfo tmp=ids.get(i);
                if (!name.equals(tmp.name)) continue;
                if (pos<tmp.start) continue;
                if (pos>tmp.end) continue;
                if (!find) {
                    find=true;
                    id.name=tmp.name;
                    id.start=tmp.start;
                    id.end=tmp.end;
                    id.type=tmp.type;
                } else {
                    if (tmp.start>=id.start) {
                        id.name=tmp.name;
                        id.start=tmp.start;
                        id.end=tmp.end;
                        id.type=tmp.type;
                    }
                }
            }
            isError=false;
            if (!find) {
                isError=true;
                showError(token,"undefined identifier or type");
                return "";
            }
            return id.type;            
        }
        
        private static String getIDName(String type,int pos) {
            for (int i=0;i<ids.size();i++) {
                IDInfo tmp=ids.get(i);
                if (!type.equals(tmp.type)) continue;
                if (pos<tmp.start) continue;
                if (pos>tmp.end) continue;
                return tmp.name;
            }
            return "";            
        }
        
        private static void addFunType(String name,String field,String type) {
            FunInfo fun=new FunInfo();
            fun.name=name;
            fun.field=field;
            fun.type=type;
            fun.start=Integer.MIN_VALUE;
            fun.end=Integer.MAX_VALUE;
            funs.add(fun);
        }

        private static String getFunType(Token token,String field) {
            String name=token.getText();
            int pos=token.getStartIndex();
            boolean find=false;
            FunInfo fun=new FunInfo();
            for (int i=0;i<funs.size();i++) {
                FunInfo tmp=funs.get(i);
                if (!name.equals(tmp.name)) continue;
                if (!field.equals(tmp.field)) continue;
                if (pos<tmp.start) continue;
                if (pos>tmp.end) continue;
                if (!find) {
                    find=true;
                    fun.name=tmp.name;
                    fun.field=tmp.field;
                    fun.start=tmp.start;
                    fun.end=tmp.end;
                    fun.type=tmp.type;
                } else {
                    if (tmp.start>=fun.start) {
                        fun.name=tmp.name;
                        fun.field=tmp.field;
                        fun.start=tmp.start;
                        fun.end=tmp.end;
                        fun.type=tmp.type;
                    }
                }
            }
            isError=false;
            if (!find) {
                isError=true;
                showError(token,"undefined function");
                return "";
            }
            return fun.type;            
        }
        
	@Override public void enterFundec(TigerParser.FundecContext ctx) {
            FunInfo fun=new FunInfo();
            fun.name=ctx.ID().getText();
            fun.field="";
            for (int i=0;i<ctx.tyfields().typeid().size();i++) {
                if (i>0) {
                    fun.field+=",";
                }
                fun.field+=getIDType(ctx.tyfields().typeid(i));
            }
            fun.start=ctx.getParent().start.getStartIndex();
            fun.end=ctx.getParent().getParent().getParent().getParent().stop.getStopIndex();
            if (ctx.typeid()==null) {
                fun.type="";                               
            } else {                
                fun.type=getIDType(ctx.typeid());
            }
            funs.add(fun);
            for (int i=0;i<ctx.tyfields().ID().size();i++) {
                IDInfo id=new IDInfo();
                id.name=ctx.tyfields().ID(i).getText();
                id.start=ctx.exp().start.getStartIndex();
                id.end=ctx.exp().stop.getStopIndex();
                id.type=getIDType(ctx.tyfields().typeid(i));
                ids.add(id);
            }
        }         
	@Override public void exitFundec(TigerParser.FundecContext ctx) {
            String funField="";
            for (int i=0;i<ctx.tyfields().typeid().size();i++) {
                if (i>0) {
                    funField+=",";
                }
                funField+=getIDType(ctx.tyfields().typeid(i));
            }
            String type=getFunType(ctx.ID().getSymbol(),funField);
            if (!expType.get(ctx.exp()).equals(type)) {
                showError(ctx.EQUAL().getSymbol(),"the function and its function body have different types");
            }
        }

	@Override public void enterIfexp(TigerParser.IfexpContext ctx) { }
	@Override public void exitIfexp(TigerParser.IfexpContext ctx) {
            if (ctx.ELSE()==null) {
                if (!expType.get(ctx.exp(0)).equals("int")) {
                    showError(ctx.exp(0),"exp1 must be integer-valued in \"if exp1 then exp2\"");
                }
                if (!expType.get(ctx.exp(1)).equals("")) {
                showError(ctx.exp(1),"exp2 must procedure no value in \"if exp1 then exp2\"");
                }
                expType.put(ctx,"");
            } else {
                if (!expType.get(ctx.exp(0)).equals("int")) {
                    showError(ctx.exp(0),"exp1 must be integer-valued in \"if exp1 then exp2 else exp3\"");
                }
                if (!expType.get(ctx.exp(1)).equals(expType.get(ctx.exp(2)))&&!((expType.get(ctx.exp(1)).startsWith("{")&&expType.get(ctx.exp(2)).equals("nil"))||(expType.get(ctx.exp(2)).startsWith("{")&&expType.get(ctx.exp(1)).equals("nil")))) {
                    showError(ctx.exp(2),"exp3 must have the same type as exp2 in \"if exp1 then exp2 else exp3\"");
                }
                expType.put(ctx,expType.get(ctx.exp(1)));
            }
        }

	@Override public void enterDecs(TigerParser.DecsContext ctx) { }
	@Override public void exitDecs(TigerParser.DecsContext ctx) { }

	@Override public void enterLetexp(TigerParser.LetexpContext ctx) { }
	@Override public void exitLetexp(TigerParser.LetexpContext ctx) {
            if (ctx.exp().isEmpty()) {
                expType.put(ctx,"");
            } else {
                expType.put(ctx,expType.get(ctx.exp(ctx.exp().size()-1)));
            }
        }

	@Override public void enterForexp(TigerParser.ForexpContext ctx) {
            IDInfo id=new IDInfo();
            id.name=ctx.ID().getText();
            id.start=ctx.exp(2).start.getStartIndex();
            id.end=ctx.exp(2).stop.getStopIndex();
            id.type="int";
            ids.add(id);
        }
	@Override public void exitForexp(TigerParser.ForexpContext ctx) {
            if (!expType.get(ctx.exp(0)).equals("int")) {
                showError(ctx.exp(0),"exp1 must be integer-valued in \"for id := exp1 to exp2 do exp3\"");
            }
            if (!expType.get(ctx.exp(1)).equals("int")) {
                showError(ctx.exp(1),"exp2 must be integer-valued in \"for id := exp1 to exp2 do exp3\"");
            }
            if (!expType.get(ctx.exp(2)).equals("")) {
                showError(ctx.exp(2),"exp3 must procedure no value in \"for id := exp1 to exp2 do exp3\"");
            }
            expType.put(ctx,"");
        }

	@Override public void enterTyfields(TigerParser.TyfieldsContext ctx) { }
	@Override public void exitTyfields(TigerParser.TyfieldsContext ctx) { }

	@Override public void enterRecordcreation(TigerParser.RecordcreationContext ctx) { }
	@Override public void exitRecordcreation(TigerParser.RecordcreationContext ctx) {
            String type=getIDType(ctx.typeid());
            if (!type.startsWith("{")) {
                showError(ctx.typeid(),"this is not a record type");
            } else {
                for (int i=0;i<ctx.ID().size();i++) {
                    String fieldName=ctx.typeid().getText()+"."+ctx.ID(i).getText();
                    String fieldType=getIDType(fieldName,ctx.ID(i).getSymbol());
                    if (fieldType.isEmpty()) {
                        showError(ctx.ID(i).getSymbol(),"no such field in the record");
                    } else {
                        if (!fieldType.equals(expType.get(ctx.exp(i)))&&!(fieldType.startsWith("{")&&expType.get(ctx.exp(i)).equals("nil"))) {
                            showError(ctx.exp(i),"the field can be not initialized with a different type");
                        }
                    }
                }                   
            }
            expType.put(ctx,type);
        }

	@Override public void enterTydec(TigerParser.TydecContext ctx) {
            if (ctx.typeid().getText().equals("int")) {
                showError(ctx.typeid(),"type \"int\" is reserved");
                return;
            }
            if (ctx.typeid().getText().equals("string")) {
                showError(ctx.typeid(),"type \"string\" is reserved");
                return;
            }
            IDInfo id=new IDInfo();
            id.name=ctx.typeid().getText();
            id.start=ctx.getParent().start.getStartIndex();
            id.end=ctx.getParent().getParent().getParent().getParent().stop.getStopIndex();
            if (ctx.ty().ARRAY()!=null) {
                id.type="arrayof"+getIDType(ctx.ty().typeid());
                ids.add(id);
            } else {
                if (ctx.ty().LEFTBRACE()!=null) {
                    id.type="{";
                    int fieldNum=ctx.ty().tyfields().ID().size();
                    for (int i=0;i<fieldNum;i++) {
                        String fieldType=getIDType(ctx.ty().tyfields().typeid(i));
                        if (isError) {
                            fieldType=ctx.ty().tyfields().typeid(i).getText();
                        }
                        if (i==0) {
                            id.type+=ctx.ty().tyfields().ID(i).getText()+":"+fieldType;
                        } else {
                            id.type+=","+ctx.ty().tyfields().ID(i).getText()+":"+fieldType;
                        }
                    }
                    id.type+="}";
                    ids.add(id);
                    for (int i=0;i<fieldNum;i++) {
                        IDInfo fieldID=new IDInfo();
                        fieldID.name=id.name+"."+ctx.ty().tyfields().ID(i).getText();
                        fieldID.start=id.start;
                        fieldID.end=id.end;
                        fieldID.type=getIDType(ctx.ty().tyfields().typeid(i));
                        ids.add(fieldID);
                    }
                } else {
                    id.type=getIDType(ctx.ty().typeid());
                    if (!isError&&id.type.isEmpty()) {
                        showError(ctx.EQUAL().getSymbol(),"recursion cycle must pass through a record or array type");
                    }
                    ids.add(id);
                }
            }    
        }
	@Override public void exitTydec(TigerParser.TydecContext ctx) { }
        
        @Override public void enterFundecs(TigerParser.FundecsContext ctx) { }
	@Override public void exitFundecs(TigerParser.FundecsContext ctx) {
            for (int i=1;i<ctx.fundec().size();i++)
                for (int j=0;j<i;j++) {
                    if (ctx.fundec(i).ID().getText().equals(ctx.fundec(j).ID().getText())) {
                        showError(ctx.fundec(i).ID().getSymbol(),"two functions with the same name in the same batch of function declarations");
                        break;
                    }
                }
        }

	@Override public void enterBreakexp(TigerParser.BreakexpContext ctx) { }
	@Override public void exitBreakexp(TigerParser.BreakexpContext ctx) {
            boolean insideLoop=false;
            ParserRuleContext prc=ctx;
            while (prc!=null) {
                ParserRuleContext parent=prc.getParent();
                if (parent==null) {
                    break;
                }
                if (parent.start.getText().equals("for")) {
                    try {
                        TigerParser.ForexpContext fc=(TigerParser.ForexpContext)parent;
                        TigerParser.ExpContext ec=(TigerParser.ExpContext)prc;
                        if (fc.exp(2).equals(ec)) {
                            insideLoop=true;
                            break;
                        }
                    } catch (Exception e) {                        
                    }
                }
                if (parent.start.getText().equals("while")) {
                    try {
                        TigerParser.WhileexpContext wc=(TigerParser.WhileexpContext)parent;
                        TigerParser.ExpContext ec=(TigerParser.ExpContext)prc;
                        if (wc.exp(1).equals(ec)) {
                            insideLoop=true;
                            break;                       
                        }
                    } catch (Exception e) {                       
                    }
                }
                prc=prc.getParent();
            }
            if (!insideLoop) {
                showError(ctx,"\"break\" must be inside a loop");
            }
            expType.put(ctx,"");
        }

	@Override public void enterAssignexp(TigerParser.AssignexpContext ctx) { }
	@Override public void exitAssignexp(TigerParser.AssignexpContext ctx) {
            if (!expType.get(ctx.lvalue()).equals(expType.get(ctx.exp()))&&!(expType.get(ctx.lvalue()).startsWith("{")&&expType.get(ctx.exp()).equals("nil"))) {
                showError(ctx.ASSIGN().getSymbol(),"assign cannot be used between different types");
            }
            expType.put(ctx,"");
        }

	@Override public void enterVardec(TigerParser.VardecContext ctx) { }
	@Override public void exitVardec(TigerParser.VardecContext ctx) {
            IDInfo id=new IDInfo();
            id.name=ctx.ID().getText();
            id.start=ctx.getStop().getStopIndex();
            id.end=ctx.getParent().getParent().getParent().getStop().getStopIndex();
            if (ctx.COLON()!=null) {
                id.type=getIDType(ctx.typeid());
                if (!id.type.equals(expType.get(ctx.exp()))&&!(id.type.startsWith("{")&&expType.get(ctx.exp()).equals("nil"))) {
                    showError(ctx.exp(),"the variable cannot be initialized with a different type");
                }
            } else {
                id.type=expType.get(ctx.exp());
                if (id.type.equals("nil")) {
                    showError(ctx.exp(),"\"nil\" cannot be used to initialize a variable in short form");
                }
            }
            ids.add(id);
        }

	@Override public void enterTy(TigerParser.TyContext ctx) { }
	@Override public void exitTy(TigerParser.TyContext ctx) { }

	@Override public void enterExp(TigerParser.ExpContext ctx) { }
	@Override public void exitExp(TigerParser.ExpContext ctx) {
            if (ctx.lvalue()!=null) {
                expType.put(ctx,expType.get(ctx.lvalue()));
                return;
            }
            if (ctx.NIL()!=null) {
                expType.put(ctx,"nil");
                return;
            }
            if (ctx.INT()!=null) {
                expType.put(ctx,"int");
                return;
            }
            if (ctx.STRING()!=null) {
                expType.put(ctx,"string");
                return;
            }
            if (ctx.LEFTPARENTHESIS()!=null) {
                if (ctx.exp().size()>0) {
                    expType.put(ctx,expType.get(ctx.exp(ctx.exp().size()-1)));
                } else {
                    expType.put(ctx,"");
                }
                return;
            }
            if (ctx.functioncall()!=null) {
                expType.put(ctx,expType.get(ctx.functioncall()));
                return;
            }
            if (ctx.recordcreation()!=null) {
                expType.put(ctx,expType.get(ctx.recordcreation()));
                return;
            }
            if (ctx.arraycreation()!=null) {
                expType.put(ctx,expType.get(ctx.arraycreation()));
                return;
            }
            if (ctx.assignexp()!=null) {
                expType.put(ctx,expType.get(ctx.assignexp()));
                return;
            }
            if (ctx.ifexp()!=null) {
                expType.put(ctx,expType.get(ctx.ifexp()));
                return;
            }
            if (ctx.whileexp()!=null) {
                expType.put(ctx,expType.get(ctx.whileexp()));
                return;
            }
            if (ctx.forexp()!=null) {
                expType.put(ctx,expType.get(ctx.forexp()));
                return;
            }
            if (ctx.breakexp()!=null) {
                expType.put(ctx,expType.get(ctx.breakexp()));
                return;
            }
            if (ctx.letexp()!=null) {
                expType.put(ctx,expType.get(ctx.letexp()));
                return;
            }
            if (ctx.MINUS()!=null&&ctx.exp(1)==null) {
                if (!expType.get(ctx.exp(0)).equals("int")) {
                    showError(ctx.MINUS().getSymbol(),"only integer-valued expression may be prefixed by a minus sign");
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.TIMES()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.TIMES().getSymbol(),"only integer-valued expressions may appear in arithmetic");
                }
                expType.put(ctx,"int");
                return;
            }
            if (ctx.DIV()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.DIV().getSymbol(),"only integer-valued expressions may appear in arithmetic");
                }
                expType.put(ctx,"int");
                return;
            }
            if (ctx.PLUS()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.PLUS().getSymbol(),"only integer-valued expressions may appear in arithmetic");
                }
                expType.put(ctx,"int");
                return;
            }
            if (ctx.MINUS()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.MINUS().getSymbol(),"only integer-valued expressions may appear in arithmetic");
                }
                expType.put(ctx,"int");
                return;
            }
            if (ctx.EQUAL()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))&&!(expType.get(ctx.exp(0)).startsWith("{")&&expType.get(ctx.exp(1)).equals("nil"))) {
                    showError(ctx.EQUAL().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                        showError(ctx.EQUAL().getSymbol(),"the comparison operators do not associate");
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.NE()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))&&!(expType.get(ctx.exp(0)).startsWith("{")&&expType.get(ctx.exp(1)).equals("nil"))) {
                    showError(ctx.NE().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                        showError(ctx.NE().getSymbol(),"the comparison operators do not associate");
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.GT()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))) {
                    showError(ctx.GT().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                            showError(ctx.GT().getSymbol(),"the comparison operators do not associate");
                    } else {
                        if (!expType.get(ctx.exp(0)).equals("int")&&!expType.get(ctx.exp(0)).equals("string")) {
                            showError(ctx.GT().getSymbol(),"only integer-valued or string-valued expressions may be compared by \">\"");
                        }                        
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.LT()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))) {
                    showError(ctx.LT().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                        showError(ctx.LT().getSymbol(),"the comparison operators do not associate");
                    } else {
                        if (!expType.get(ctx.exp(0)).equals("int")&&!expType.get(ctx.exp(0)).equals("string")) {
                            showError(ctx.LT().getSymbol(),"only integer-valued or string-valued expressions may be compared by \">\"");
                        }                        
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.GE()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))) {
                    showError(ctx.GE().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                        showError(ctx.GE().getSymbol(),"the comparison operators do not associate");
                    } else {
                        if (!expType.get(ctx.exp(0)).equals("int")&&!expType.get(ctx.exp(0)).equals("string")) {
                            showError(ctx.GE().getSymbol(),"only integer-valued or string-valued expressions may be compared by \">\"");
                        }                        
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.LE()!=null) {
                if (!expType.get(ctx.exp(0)).equals(expType.get(ctx.exp(1)))) {
                    showError(ctx.LE().getSymbol(),"comparison cannot be used between different types");
                } else {
                    if (ctx.exp(0).EQUAL()!=null||ctx.exp(0).NE()!=null||ctx.exp(0).GT()!=null||ctx.exp(0).LT()!=null||ctx.exp(0).GE()!=null||ctx.exp(0).LE()!=null) {
                        showError(ctx.LE().getSymbol(),"the comparison operators do not associate");
                    } else {
                        if (!expType.get(ctx.exp(0)).equals("int")&&!expType.get(ctx.exp(0)).equals("string")) {
                            showError(ctx.LE().getSymbol(),"only integer-valued or string-valued expressions may be compared by \">\"");
                        }                        
                    }
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.AND()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.AND().getSymbol(),"only integer-valued expressions may appear in boolean operation");
                }
                expType.put(ctx,"int");
                return;                
            }
            if (ctx.OR()!=null) {
                if (!expType.get(ctx.exp(0)).equals("int")||!expType.get(ctx.exp(1)).equals("int")) {
                    showError(ctx.OR().getSymbol(),"only integer-valued expressions may appear in boolean operation");
                }
                expType.put(ctx,"int");
                return;                
            }
        }

	@Override public void enterFunctioncall(TigerParser.FunctioncallContext ctx) { }
	@Override public void exitFunctioncall(TigerParser.FunctioncallContext ctx) {
            String funField="";
            for (int i=0;i<ctx.exp().size();i++) {
                if (i>0) {
                    funField+=",";
                }
                funField+=expType.get(ctx.exp(i));
            }
            expType.put(ctx,getFunType(ctx.ID().getSymbol(),funField));
        }

	@Override public void enterProg(TigerParser.ProgContext ctx) { }
	@Override public void exitProg(TigerParser.ProgContext ctx) { }
        
        @Override public void enterTydecs(TigerParser.TydecsContext ctx) { }
	@Override public void exitTydecs(TigerParser.TydecsContext ctx) {
            for (int i=1;i<ctx.tydec().size();i++)
                for (int j=0;j<i;j++) {
                    if (ctx.tydec(i).typeid().getText().equals(ctx.tydec(j).typeid().getText())) {
                        showError(ctx.tydec(i).typeid(),"two types with the same name in the same batch of type declarations");
                        break;
                    }
                }
        }

	@Override public void enterDec(TigerParser.DecContext ctx) { }
	@Override public void exitDec(TigerParser.DecContext ctx) { }

	@Override public void enterLvalue(TigerParser.LvalueContext ctx) { }
	@Override public void exitLvalue(TigerParser.LvalueContext ctx) {
            if (ctx.LEFTBRACKET()!=null) {
                if (!expType.get(ctx.exp()).equals("int")) {
                    showError(ctx.exp(),"array subscript must be integer-valued");
                }
                String type=expType.get(ctx.lvalue());
                if (!type.startsWith("arrayof")) {
                    showError(ctx.lvalue(),"this is not an array type");
                    expType.put(ctx,type);
                } else {
                    expType.put(ctx,type.substring(7));
                }
            } else {
                if (ctx.DOT()!=null) {
                    String type=expType.get(ctx.lvalue());
                    if (!type.startsWith("{")) {
                        showError(ctx.lvalue(),"this is not a record type");
                        expType.put(ctx,type);
                    } else {
                        String name=getIDName(type,ctx.ID().getSymbol().getStartIndex());
                        String fieldType=getIDType(name+"."+ctx.ID().getText(),ctx.ID().getSymbol());
                        if (fieldType.isEmpty()) {
                            showError(ctx.ID().getSymbol(),"no such field in the record");
                            expType.put(ctx,type);
                        } else{
                            expType.put(ctx,fieldType);
                        }
                    }  
                } else {
                    expType.put(ctx,getIDType(ctx.ID().getSymbol()));
                }
            }
        }

	@Override public void enterTypeid(TigerParser.TypeidContext ctx) { }
	@Override public void exitTypeid(TigerParser.TypeidContext ctx) { }

	@Override public void enterArraycreation(TigerParser.ArraycreationContext ctx) { }
	@Override public void exitArraycreation(TigerParser.ArraycreationContext ctx) {
            String type=getIDType(ctx.typeid());
            if (!type.startsWith("arrayof")) {
                showError(ctx.typeid(),"this is not an array type");
            } else {
                if (!expType.get(ctx.exp(0)).equals("int")) {
                    showError(ctx.exp(0),"size of an array must be integer-valued");
                }
                if (!type.substring(7).equals(expType.get(ctx.exp(1)))&&!(type.substring(7).startsWith("{")&&expType.get(ctx.exp(1)).equals("nil"))) {
                    showError(ctx.exp(1),"the array element can be not initialized with a different type");
                }                
            }
            expType.put(ctx,type);
        }

	@Override public void enterWhileexp(TigerParser.WhileexpContext ctx) { }
	@Override public void exitWhileexp(TigerParser.WhileexpContext ctx) {
            if (!expType.get(ctx.exp(0)).equals("int")) {
                showError(ctx.exp(0),"exp1 must be integer-valued in \"while exp1 do exp2\"");
            }
            if (!expType.get(ctx.exp(1)).equals("")) {
                showError(ctx.exp(1),"exp2 must procedure no value in \"while exp1 do exp2\"");
            }
            expType.put(ctx,"");
        }
        
	@Override public void enterEveryRule(ParserRuleContext ctx) { }
	@Override public void exitEveryRule(ParserRuleContext ctx) { }
    }
    
}